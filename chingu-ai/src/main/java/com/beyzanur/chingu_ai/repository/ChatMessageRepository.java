package com.beyzanur.chingu_ai.repository;

import com.beyzanur.chingu_ai.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 💬 SADECE o seansa ait mesajları kronolojik getirir
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    // 🗑️ SADECE o seans geçmişini siler
    void deleteBySessionId(String sessionId);

    // 🚀 DÜZELTİLEN YER: Tablo adı "chat_messages" yapıldı ve SQL Server alias uyumluluğu sağlandı!
    @Query(value = "SELECT m.session_id AS sessionId, MIN(m.user_message) AS title " +
            "FROM chat_messages m " +
            "WHERE m.user_email = :email " +
            "GROUP BY m.session_id", nativeQuery = true)
    List<Map<String, Object>> getAllSessionsByUserBounds(@Param("email") String email);
}