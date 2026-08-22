package com.croi.contact.repository;

import com.croi.contact.entity.ContactSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContactRepository extends JpaRepository<ContactSubmission, UUID> {
}
