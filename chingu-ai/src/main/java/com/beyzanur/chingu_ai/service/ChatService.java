package com.beyzanur.chingu_ai.service;

import com.beyzanur.chingu_ai.model.ChatMessage;
import com.beyzanur.chingu_ai.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private final ChatMessageRepository repository;
    private final WebClient webClient;

    @Value("${groq.api.key}")
    private String apiKey;

    public ChatService(ChatMessageRepository repository, WebClient.Builder builder) {
        this.repository = repository;
        this.webClient = builder.build();
    }

    public String askChingu(String message, MultipartFile file) {
        String finalMessage = (message == null || message.trim().isEmpty()) ? "Bu görseli analiz et." : message;
        String response = "Chingu şu an yanıt veremiyor.";
        String savedImagePath = null;

        try {
            List<Object> contentList = new ArrayList<>();

            // Metin
            Map<String, Object> textMap = new LinkedHashMap<>();
            textMap.put("type", "text");
            textMap.put("text", finalMessage);
            contentList.add(textMap);

            // Görsel
            if (file != null && !file.isEmpty()) {
                // Kaydet
                String uploadDir = "uploads/";
                new java.io.File(uploadDir).mkdirs();
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadDir + fileName);
                Files.write(path, file.getBytes());
                savedImagePath = "/uploads/" + fileName;

                // Base64
                byte[] bytes = file.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
                String dataUri = "data:" + contentType + ";base64," + base64Image;

                Map<String, Object> imageLinkMap = new LinkedHashMap<>();
                imageLinkMap.put("url", dataUri);

                Map<String, Object> imageMap = new LinkedHashMap<>();
                imageMap.put("type", "image_url");
                imageMap.put("image_url", imageLinkMap);
                contentList.add(imageMap);
            }

            Map<String, Object> messageMap = new LinkedHashMap<>();
            messageMap.put("role", "user");
            messageMap.put("content", contentList);

            List<Object> messagesList = new ArrayList<>();
            messagesList.add(messageMap);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", "meta-llama/llama-4-scout-17b-16e-instruct");
            body.put("temperature", 0.2);
            body.put("messages", messagesList);

            Map<?, ?> result = webClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (result != null && result.containsKey("choices")) {
                List<?> choices = (List<?>) result.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> messageObj = (Map<?, ?>) firstChoice.get("message");
                    if (messageObj != null) {
                        response = (String) messageObj.get("content");
                    }
                }
            }

        } catch (WebClientResponseException e) {
            System.out.println("GROQ HATA: " + e.getResponseBodyAsString());
            response = "Groq servis hatası: " + e.getStatusCode();
        } catch (Exception e) {
            System.out.println("GENEL HATA: " + e.getMessage());
            response = "Bir hata oluştu: " + e.getMessage();
        }

        try {
            ChatMessage chat = new ChatMessage();
            chat.setUserMessage(finalMessage);
            chat.setAiResponse(response);
            chat.setImagePath(savedImagePath);
            repository.save(chat);
        } catch (Exception dbEx) {
            System.out.println("DB HATA: " + dbEx.getMessage());
        }

        return response;
    }

    public List<ChatMessage> getAllMessages() {
        return repository.findAll();
    }
}