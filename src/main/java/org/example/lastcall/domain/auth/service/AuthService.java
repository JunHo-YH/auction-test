package org.example.lastcall.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lastcall.common.config.PasswordEncoder;
import org.example.lastcall.common.util.GeneratorUtil;
import org.example.lastcall.domain.auth.dto.request.SignupRequest;
import org.example.lastcall.domain.auth.email.entity.EmailVerification;
import org.example.lastcall.domain.auth.email.model.EmailVerificationStatus;
import org.example.lastcall.domain.auth.email.repository.EmailVerificationRepository;
import org.example.lastcall.domain.user.entity.User;
import org.example.lastcall.domain.user.enums.Role;
import org.example.lastcall.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signUp(final SignupRequest request) {
        EmailVerification emailVerification = emailVerificationRepository.findByPublicId(request.getVerificationPublicId())
                .orElseThrow(() -> new RuntimeException("잘못된 요청입니다."));
        validateEmailVerifiedStatus(emailVerification.getStatus());
        emailVerification.updateStatus(EmailVerificationStatus.CONSUMED);

        User user = User.createForSignUp(
                GeneratorUtil.generatePublicId(),
                request.getUsername(),
                request.getNickname(),
                emailVerification.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getAddress(),
                request.getPostcode(),
                request.getDetailAddress(),
                request.getPhoneNumber(),
                Role.USER
        );

        userRepository.save(user);
    }

    private void validateEmailVerifiedStatus(final EmailVerificationStatus verificationStatus) {
        if (!Objects.equals(verificationStatus, EmailVerificationStatus.VERIFIED)) {
            throw new RuntimeException("인증되지 않은 이메일입니다.");
        }
    }

}
