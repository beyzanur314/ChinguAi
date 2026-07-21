package com.beyzanur.chingu_ai.controller;

import com.beyzanur.chingu_ai.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 🔐 Kullanıcının mailini OAuth2 nesnesinden çeken yardımcı metot
    private String getUserEmail(Object principal) {
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            return oauth2User.getAttribute("email");
        }
        return "anonim@chingu.com";
    }

    // 1. CHAT EKRANI (GET) - Sidebar listesi ve Aktif Seansa ait mesajlar yüklenir
    @GetMapping("/chingu/ui")
    public String getChatUI(@RequestParam(value = "sessionId", required = false) String sessionId,
                            Model model,
                            @AuthenticationPrincipal Object principal) {

        String username = "Geliştirici";
        String email = getUserEmail(principal);

        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            username = oauth2User.getAttribute("name");
        }

        // 🔄 AKILLI SEANS YÖNETİMİ: Eğer URL'de bir sessionId yoksa, yeni bir UUID oluşturup oraya yönlendiriyoruz
        if (sessionId == null || sessionId.trim().isEmpty()) {
            String newSessionId = UUID.randomUUID().toString();
            return "redirect:/chingu/ui?sessionId=" + newSessionId;
        }

        model.addAttribute("username", username);
        model.addAttribute("currentSessionId", sessionId);

        // 📁 SIDEBAR GEÇMİŞİ: Kullanıcının açtığı tüm eski benzersiz sohbet oturumlarının listesi
        model.addAttribute("sessions", chatService.getAllSessionsByUser(email));

        // 💬 ANA CHAT PENCERESİ: Sadece o anki aktif seansa ait mesaj geçmişi yüklenir
        model.addAttribute("history", chatService.getMessagesBySession(sessionId));

        return "chat";
    }

    // 2. METİN VE DOSYA TABANLI MESAJ GÖNDERME (POST)
    @PostMapping("/chingu/ui/chat")
    public String handleChat(@RequestParam("sessionId") String sessionId,
                             @RequestParam(value = "message", required = false) String message,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             @AuthenticationPrincipal Object principal) {

        String email = getUserEmail(principal);

        // Parametrelerin safe-check kontrolleri (Boş form gönderiminde çakılmayı önler)
        boolean hasMessage = message != null && !message.trim().isEmpty();
        boolean hasFile = file != null && !file.isEmpty();

        if (hasMessage || hasFile) {
            // 🚀 Service katmanına mesajı, varsa PDF/Görsel dosyasını ve seans parametrelerini uçuruyoruz
            chatService.askChingu(message, file, email, sessionId);
        }

        // Kullanıcının bulunduğu seans penceresinde kalmasını sağlıyoruz
        return "redirect:/chingu/ui?sessionId=" + sessionId;
    }

    // 3. SOHBETİ TEMİZLEME METODU (POST) - Sadece o anki aktif seansı siler!
    @PostMapping("/chingu/ui/clear")
    public String clearChat(@RequestParam("sessionId") String sessionId,
                            @AuthenticationPrincipal Object principal) {

        String email = getUserEmail(principal);

        if (email != null && !email.equals("anonim@chingu.com")) {
            // 🚀 Diğer sohbet odalarına dokunmadan, sadece o anki seans geçmişini temizliyoruz
            chatService.clearMessagesBySession(sessionId);
        }

        return "redirect:/chingu/ui?sessionId=" + sessionId;
    }

    // 4. SESLİ MESAJ YÖNETİMİ (POST)
    @PostMapping("/chingu/ui/chat/voice")
    public ResponseEntity<String> handleVoiceChat(@RequestParam("sessionId") String sessionId,
                                                  @RequestParam("audio") MultipartFile audio,
                                                  @RequestParam(value = "file", required = false) MultipartFile file,
                                                  @AuthenticationPrincipal Object principal) {

        String email = getUserEmail(principal);

        if (audio != null && !audio.isEmpty()) {
            // 1. Sesi Groq Whisper ile çözüp metne dönüştür
            String convertedMessage = chatService.convertVoiceToText(audio);

            // 2. Çözülen metni o anki aktif seans ID'siyle birlikte RAG akışına besle
            if (convertedMessage != null && !convertedMessage.equals("Ses anlaşılamadı.")) {
                chatService.askChingu(convertedMessage, file, email, sessionId);
                return ResponseEntity.ok("Ses başarıyla işlendi.");
            }
        }

        return ResponseEntity.badRequest().body("Ses işlenemedi.");
    }

    // 🗑️ SOL MENÜDEN SOHBETİ TAMAMEN SİLMEK (DELETE ROOM)
    @PostMapping("/chingu/ui/delete-session")
    public String deleteSessionFully(@RequestParam("sessionId") String sessionId,
                                     @AuthenticationPrincipal Object principal) {
        String email = getUserEmail(principal);

        if (email != null && !email.equals("anonim@chingu.com")) {
            // Service katmanındaki silme metodunu çağırıyoruz
            chatService.clearMessagesBySession(sessionId);
        }

        // Oda tamamen yok edildiği için kullanıcıyı temiz bir ana sayfaya fırlatıyoruz
        return "redirect:/chingu/ui";
    }
}