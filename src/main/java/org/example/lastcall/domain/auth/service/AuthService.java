package org.example.lastcall.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lastcall.domain.auth.dto.request.SignupRequest;
import org.example.lastcall.domain.auth.repository.AuthRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;

    public void signUp(final SignupRequest request) {
        // 1. 사용자로 부터 이메일을 받아서 DB에 실제 있는지 검증
        String email = request.getEmail();


    }

}
