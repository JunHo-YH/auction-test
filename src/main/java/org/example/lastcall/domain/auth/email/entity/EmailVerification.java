package org.example.lastcall.domain.auth.email.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.lastcall.common.entity.BaseEntity;
import org.example.lastcall.domain.auth.email.model.EmailVerificationStatus;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "email_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID publicId;

    @Column(nullable = false)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailVerificationStatus status;

    @Column(nullable = false)
    private String email;

    private EmailVerification(UUID publicId, String verificationCode, String email) {
        this.publicId = publicId;
        this.verificationCode = verificationCode;
        this.status = EmailVerificationStatus.SENT;
        this.email = email;
    }

    public static EmailVerification create(UUID publicId, String verificationCode, String email) {
        return new EmailVerification(publicId, verificationCode, email);
    }

    public void updateStatus(EmailVerificationStatus status) {
        this.status = status;
    }

    public void validateVerificationCode(final String requestedVerificationCode) {
        if (!Objects.equals(requestedVerificationCode, verificationCode)) {
            throw new RuntimeException("인증번호가 일치하지 않습니다.");
        }
    }
}
