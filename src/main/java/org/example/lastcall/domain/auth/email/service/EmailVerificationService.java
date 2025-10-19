package org.example.lastcall.domain.auth.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lastcall.common.util.GeneratorUtil;
import org.example.lastcall.domain.auth.email.dto.SendEmailVerificationCodeDto;
import org.example.lastcall.domain.auth.email.entity.EmailVerification;
import org.example.lastcall.domain.auth.email.repository.EmailRepository;
import org.example.lastcall.domain.auth.email.util.VerificationCodeGenerator;
import org.example.lastcall.domain.user.repository.UserRepository;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final EmailRepository emailRepository;

    @Transactional
    public void sendEmailVerificationCode(final SendEmailVerificationCodeDto.Request request) {
        final String verificationCode = VerificationCodeGenerator.generateVerificationCode();

        EmailVerification emailVerification = EmailVerification.create(
                GeneratorUtil.generatePublicId(),
                verificationCode,
                request.email()
        );

        emailRepository.save(emailVerification);

        // TODO:: 하루에 메일 당 최대 10번 요청 가능, 한번 요청 후 30초 후 요청 가능
        sendEmail(request.email(), verificationCode);
    }

    private void sendEmail(final String userEmail, final String verificationCode) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(userEmail);
            mail.setSubject("LastCall! 인증 코드");
            mail.setText(verificationCode);
            javaMailSender.send(mail);
        } catch (MailException e) {
            log.error("이메일 발송 실패. 수신자: {}, 원인: {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("메일 전송 실패");
        }

    }

    @Transactional(readOnly = true)
    public void validateDuplicateEmail(final String email) {
        boolean existsAlreadyEmail = userRepository.existsByEmail(email);
        if (existsAlreadyEmail) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
    }
}
