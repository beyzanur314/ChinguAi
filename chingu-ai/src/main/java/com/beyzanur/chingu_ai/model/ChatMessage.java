package com.beyzanur.chingu_ai.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

    @Entity
    @Table(name="chat_messages")
    @Data
    public  class  ChatMessage{
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(columnDefinition = "NVARCHAR(MAX)")
        private String userMessage;

        @Column(columnDefinition = "NVARCHAR(MAX)")
        private String aiResponse;
        @Column(columnDefinition = "NVARCHAR(MAX)")
        private String imagePath;

        private LocalDateTime createdAt;

        // Kayıt atılırken zamanı otomatik eklesin
        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
        }
    }
