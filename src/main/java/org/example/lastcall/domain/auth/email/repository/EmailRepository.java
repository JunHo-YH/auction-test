package org.example.lastcall.domain.auth.email.repository;

import org.example.lastcall.domain.auth.email.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<EmailVerification, Long> {

    EmailVerification email(String email);
}
