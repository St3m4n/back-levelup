package com.levelup.backend.repository;

import com.levelup.backend.model.ContactMessage;
import com.levelup.backend.model.ContactMessageStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, String>, JpaSpecificationExecutor<ContactMessage> {
    List<ContactMessage> findByStatus(ContactMessageStatus status);
}