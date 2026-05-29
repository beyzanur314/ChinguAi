package com.beyzanur.chingu_ai.controller;

import com.beyzanur.chingu_ai.model.ChatMessage;
import com.beyzanur.chingu_ai.repository.ChatMessageRepository;
import com.beyzanur.chingu_ai.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal; // Java'nın kendi çekirdek kütüphanesi, ASLA hata vermez!
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageRepository repository;
    private static final Map<Long, String> imageCache = new ConcurrentHashMap<>();

    public ChatController(ChatService chatService, ChatMessageRepository repository) {
        this.chatService = chatService;
        this.repository = repository;
    }

    // 1. Chat Ekranını İlk Açan Metot
    @GetMapping("/chingu/ui")
    public String getChatPage(Model model, Principal principal) {
        try {
            List<ChatMessage> history = chatService.getAllMessages();
            model.addAttribute("history", history);
            model.addAttribute("images", imageCache);

            // Eğer Google ile giriş yapıldıysa kullanıcının mailini/adını güvenle çekiyoruz
            if (principal != null) {
                model.addAttribute("userName", principal.getName());
                // Profil resmi kütüphane olmadığı için null geçiyoruz, hata vermesini engeller
                model.addAttribute("userPicture", null);
            }
        } catch (Exception e) {
            System.out.println("Geçmiş yüklenirken hata: " + e.getMessage());
        }
        return "chat";
    }
    @GetMapping("/login")
    public String loginPage() {

        return "login"; // templates/login.html dosyasını açar
    }

    // 2. Mesaj ve Görsel Gönderen Metot
    @PostMapping("/chingu/ui/chat")
    public String handleChat(@RequestParam(value = "message", required = false) String message,
                             @RequestParam(value = "file", required = false) MultipartFile file) {
        String finalMessage = (message == null || message.trim().isEmpty()) ? "Bu görseli analiz et." : message;
        try {
            chatService.askChingu(finalMessage, file);
            if (file != null && !file.isEmpty()) {
                List<ChatMessage> history = chatService.getAllMessages();
                if (!history.isEmpty()) {
                    ChatMessage lastSavedMessage = history.get(history.size() - 1);
                    String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                    String contentType = file.getContentType();
                    String dataUri = "data:" + (contentType != null ? contentType : "image/jpeg") + ";base64," + base64Image;
                    imageCache.put(lastSavedMessage.getId(), dataUri);
                }
            }
        } catch (Exception e) {
            System.out.println("Controller mesajı işlerken hata yakaladı: " + e.getMessage());
        }
        return "redirect:/chingu/ui";
    }

    // 3. Tüm sohbeti temizle
    @PostMapping("/chingu/ui/clear")
    public String clearAll() {
        try {
            repository.deleteAll();
            imageCache.clear();
        } catch (Exception e) {
            System.out.println("Sohbet temizlenemedi: " + e.getMessage());
        }
        return "redirect:/chingu/ui";
    }

    // 4. Tek mesaj sil
    @PostMapping("/chingu/ui/delete/{id}")
    public String deleteMessage(@PathVariable Long id) {
        try {
            repository.deleteById(id);
            imageCache.remove(id);
        } catch (Exception e) {
            System.out.println("Mesaj silinemedi: " + e.getMessage());
        }
        return "redirect:/chingu/ui";
    }

    // 5. JSON geçmiş
    @GetMapping("/chingu/history")
    @ResponseBody
    public List<ChatMessage> getHistory() {
        return chatService.getAllMessages();
    }
}