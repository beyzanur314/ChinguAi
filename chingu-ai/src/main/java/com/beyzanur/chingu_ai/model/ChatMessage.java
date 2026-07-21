package com.beyzanur.chingu_ai.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Nationalized // 🔮 SQL Server'a veriyi gönderirken "Bunu Unicode (N) olarak işaretle" der.
    @Column(columnDefinition = "NVARCHAR(MAX)") // 🇰🇷 Korece, Japonca gibi tüm uluslararası alfabeleri destekler.
    private String userMessage;

    @Nationalized // 🔮 Yapay zekadan gelen Hangıl harflerini koruma altına alır.
    @Column(columnDefinition = "NVARCHAR(MAX)") // 🇺🇸 Flawless İngilizce ve Korece metinler için devasa alan.
    private String aiResponse;

    private String imagePath;
    private LocalDateTime createdAt;

    // 🔐 Her mesajı atan kullanıcının mail adresini burada izole ediyoruz.
    private String userEmail;

    // 📁 YENİ: Her mesajın hangi sohbet odasına (oturumuna) ait olduğunu belirten kritik alan!
    private String sessionId;

    // ==================== GETTER & SETTER METOTLARI ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // 🚀 YENİ EKLEME: ChatService'deki o sinsi kırmızı çizgiyi yok eden kritik Getter & Setter metotları
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}