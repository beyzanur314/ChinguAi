package com.beyzanur.chingu_ai.repository;

import com.beyzanur.chingu_ai.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // Burada süslü parantez içine kod yazmıyoruz.
    // JpaRepository bizim yerimize findAll() gibi işleri zaten yapıyor.
}