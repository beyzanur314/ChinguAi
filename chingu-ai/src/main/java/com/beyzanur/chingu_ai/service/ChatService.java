package com.beyzanur.chingu_ai.service;

import com.beyzanur.chingu_ai.model.ChatMessage;
import com.beyzanur.chingu_ai.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.ai.document.Document;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final ChatMessageRepository repository;
    private final WebClient webClient;

    // 🧠 SessionID bazlı döküman parçalarını RAM'de saklayacak geçici hafıza (RAG Belleği)
    private final Map<String, List<Document>> sessionPdfCache = new ConcurrentHashMap<>();

    @Autowired
    private PredictionService predictionService;

    @Autowired
    private PdfService pdfService;

    private final String groqApiKey = "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";
    private final String tavilyApiKey = "tvly-YOUR_ACTUAL_TAVILY_API_KEY";

    public ChatService(ChatMessageRepository repository, WebClient.Builder builder) {
        this.repository = repository;
        this.webClient = builder.build();
    }

    // 🔍 JAVA TARAFINDA METİN BENZERLİK MOTORU (Simüle Edilmiş Vektör Arama)
    private String findMostRelevantChunks(List<Document> chunks, String userQuery) {
        if (chunks == null || chunks.isEmpty()) return "";

        String[] queryWords = userQuery.toLowerCase().split("\\s+");

        return chunks.stream()
                .map(doc -> {
                    String content = doc.getContent().toLowerCase();
                    long score = Arrays.stream(queryWords)
                            .filter(word -> word.length() > 2 && content.contains(word))
                            .count();
                    return new AbstractMap.SimpleEntry<>(doc.getContent(), score);
                })
                .filter(entry -> entry.getValue() > 0)
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(3) // Top-3 RAG Context
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("\n---\n"));
    }

    private String searchWebWithRAG(String query) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "api_key", tavilyApiKey,
                    "query", query,
                    "search_depth", "basic",
                    "include_answer", true
            );
            Map<?, ?> response = webClient.post()
                    .uri("https://api.tavily.com/search")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            return (response != null && response.containsKey("answer")) ? (String) response.get("answer") : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String askChingu(String message, MultipartFile file, String userEmail, String sessionId) {
        String finalMessage = (message == null || message.trim().isEmpty()) ? "Dosyayı analiz et." : message;
        String response = "";

        System.out.println("====== CHINGU DEBUG ======");
        System.out.println("Gelen Session ID: " + sessionId);
        System.out.println("Cache'deki Oturumlar: " + sessionPdfCache.keySet());
        System.out.println("==========================");

        // 🏠 1. ADIM: EV FİYATI TAHMİN AJANI
        if (finalMessage.toLowerCase().contains("ev fiyat") || finalMessage.toLowerCase().contains("tahmin et")) {
            try {
                float tahminiFiyat = predictionService.predictHousePrice(120.0f, 3.0f, 5.0f);
                response = "🤖 Chingu Yapay Zeka Tahmin Motoru (Matematiksel Regresyon) Çalıştı!\n\n" +
                        "İstediğiniz ev özellikleri:\n" +
                        "• Metrekare: 120\n" +
                        "• Oda Sayısı: 3\n" +
                        "• Bina Yaşı: 5\n\n" +
                        "💰 Yapay zekanın hesapladığı tahmini piyasa değeri: " +
                        String.format("%,.2f", tahminiFiyat) + " TL";

                ChatMessage chat = new ChatMessage();
                chat.setUserMessage(finalMessage);
                chat.setAiResponse(response);
                chat.setUserEmail(userEmail);
                chat.setSessionId(sessionId);
                chat.setCreatedAt(LocalDateTime.now());
                repository.save(chat);
                return response;
            } catch (Exception e) {
                return "Model hesaplama yaparken bir sorunla karşılaştı: " + e.getMessage();
            }
        }

        // 🌐 2. ADIM: GENEL LLM VE YENİ AKILLI RAG AKIŞI
        try {
            List<Map<String, Object>> messagesList = new ArrayList<>();

            messagesList.add(Map.of("role", "system", "content",
                    "You are 'Chingu AI'. RULES: Use provided Search Context or Document Context. If not found in context, say 'Bu konuda kesin bir bilgim yok, uydurmak istemem.'. DO NOT fabricate facts. Respond in Turkish unless requested otherwise. You are also an expert at Korean and Russian languages. CRITICAL: When writing in Korean, you MUST always output the actual Hangul characters (e.g., 보고 싶ored어) along with their romanization. Never leave the Korean text blank or empty."));

            String documentContext = "";
            boolean isImage = false;
            String fileName = "";

            // 🛠️ AKILLI CACHE ENTEGRASYONU BURADA BAŞLIYOR
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType();
                fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Files.createDirectories(Paths.get("uploads/"));
                Files.write(Paths.get("uploads/" + fileName), file.getBytes());

                if (contentType != null && contentType.equalsIgnoreCase("application/pdf")) {
                    // 📄 PDF parçalara ayrılıyor (Chunking)
                    List<Document> chunks = pdfService.extractAndSplitPdf(file);

                    // Hafızaya (Cache) atıyoruz ki sonraki mesajlarda dökümanı hatırlayalım
                    sessionPdfCache.put(sessionId, chunks);

                    // Kullanıcının sorusuna göre en alakalı parçalar filtreleniyor (Retrieval)
                    documentContext = findMostRelevantChunks(chunks, finalMessage);

                    if (documentContext.isEmpty()) {
                        documentContext = "PDF yüklendi ancak kullanıcının sorusuyla eşleşen spesifik bir alan bulunamadı.";
                    }
                } else if (contentType != null && contentType.startsWith("image/")) {
                    isImage = true;
                }
            } else {
                // 🔥 SİHİRLİ DOKUNUŞ: Yeni dosya gelmediyse, mevcut session'da daha önce yüklenmiş PDF var mı diye bak!
                if (sessionPdfCache.containsKey(sessionId)) {
                    List<Document> mevcutChunks = sessionPdfCache.get(sessionId);
                    documentContext = findMostRelevantChunks(mevcutChunks, finalMessage);
                }
            }

            // 🚀 Akıllıca daraltılmış bağlam LLM'e besleniyor
            if (!documentContext.trim().isEmpty()) {
                messagesList.add(Map.of("role", "system", "content", "Document Context (Yüklenen PDF'ten En Alakalı Kesitler):\n" + documentContext));
            }

            if (documentContext.trim().isEmpty()) {
                String webContext = searchWebWithRAG(finalMessage);
                if (webContext != null && !webContext.trim().isEmpty()) {
                    messagesList.add(Map.of("role", "system", "content", "Search Context (Web Araması):\n" + webContext));
                }
            }

            // Güvenli Hafıza Mekanizması
            List<ChatMessage> oldMessages = repository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            if (oldMessages != null && !oldMessages.isEmpty()) {
                int startIndex = Math.max(0, oldMessages.size() - 6);
                for (int i = startIndex; i < oldMessages.size(); i++) {
                    ChatMessage m = oldMessages.get(i);
                    if (m.getUserMessage() != null && !m.getUserMessage().trim().isEmpty()) {
                        messagesList.add(Map.of("role", "user", "content", m.getUserMessage()));
                    }
                    if (m.getAiResponse() != null && !m.getAiResponse().trim().isEmpty()) {
                        messagesList.add(Map.of("role", "assistant", "content", m.getAiResponse()));
                    }
                }
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("temperature", 0.5);

            if (isImage) {
                String b64 = Base64.getEncoder().encodeToString(file.getBytes());
                messagesList.add(Map.of("role", "user", "content", List.of(
                        Map.of("type", "text", "text", finalMessage),
                        Map.of("type", "image_url", "image_url", Map.of("url", "data:image/jpeg;base64," + b64))
                )));
                body.put("model", "meta-llama/llama-4-scout-17b-16e-instruct");
            } else {
                messagesList.add(Map.of("role", "user", "content", finalMessage));
                body.put("model", "llama-3.3-70b-versatile");
            }
            body.put("messages", messagesList);

            byte[] responseBytes = webClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + groqApiKey.trim())
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            String rawResponse = new String(responseBytes, java.nio.charset.StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<?, ?> convertedMap = mapper.readValue(rawResponse, Map.class);
            response = (String) ((Map<?, ?>) ((Map<?, ?>) ((List<?>) convertedMap.get("choices")).get(0)).get("message")).get("content");

            ChatMessage chat = new ChatMessage();
            chat.setUserMessage(message != null && !message.trim().isEmpty() ? message : "[PDF/Görsel]");
            chat.setAiResponse(response);
            chat.setUserEmail(userEmail);
            chat.setSessionId(sessionId);
            chat.setCreatedAt(LocalDateTime.now());
            if (isImage) chat.setImagePath("/uploads/" + fileName);
            repository.save(chat);

        } catch (Exception e) {
            e.printStackTrace();
            response = "Sistem şu an meşgul, lütfen tekrar dene.";
        }
        return response;
    }

    public List<ChatMessage> getMessagesBySession(String sessionId) { return repository.findBySessionIdOrderByCreatedAtAsc(sessionId); }
    public List<Map<String, Object>> getAllSessionsByUser(String userEmail) { return repository.getAllSessionsByUserBounds(userEmail); }

    @Transactional
    public void clearMessagesBySession(String sessionId) {
        repository.deleteBySessionId(sessionId);
        // Oturum silindiğinde RAM'deki PDF parçalarını da temizleyerek hafızayı koruyoruz
        sessionPdfCache.remove(sessionId);
    }

    public String convertVoiceToText(MultipartFile audioFile) {
        try {
            LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", audioFile.getResource());
            body.add("model", "distil-whisper-large-v3-en");
            return webClient.post().uri("https://api.groq.com/openai/v1/audio/transcriptions")
                    .header("Authorization", "Bearer " + groqApiKey.trim()).bodyValue(body).retrieve().bodyToMono(String.class).block();
        } catch (Exception e) { return "Ses anlaşılamadı."; }
    }
}