package org.example.lastcall.domain.auth.email.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lastcall.domain.auth.email.dto.SendEmailVerificationCodeDto;
import org.example.lastcall.domain.auth.email.service.EmailVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/api/v1/email-verifications")
    public ResponseEntity<Void> sendEmailVerificationCode(
            @Valid @RequestBody SendEmailVerificationCodeDto.Request request) {
        emailVerificationService.sendEmailVerificationCode(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
