package org.example.lastcall.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.lastcall.domain.auth.dto.request.SignupRequest;
import org.example.lastcall.domain.auth.dto.request.UserLoginDto;
import org.example.lastcall.domain.auth.service.AuthService;
import org.example.lastcall.domain.auth.utils.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/users")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignupRequest request) {
        authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/tokens")
    public ResponseEntity<Void> userLogin(@Valid @RequestBody UserLoginDto.Request request) {
        UserLoginDto.Response loginResponse = authService.userLogin(request);

        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(loginResponse.accessToken());
        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(loginResponse.refreshToken());

        return ResponseEntity.status(HttpStatus.OK)
                .headers(httpHeaders -> {
                    httpHeaders.add("Set-Cookie", accessCookie.toString());
                    httpHeaders.add("Set-Cookie", refreshCookie.toString());
                })
                .build();
    }

    @DeleteMapping("/tokens")
    public ResponseEntity<Void> userLogout(@CookieValue(name = CookieUtil.REFRESH_COOKIE) String refreshToken) {
        authService.userLogout(refreshToken);
        //  HTTP 사이트 -> TLS(AWS) -> HTTPS
        // gravy.kr -> http:80 -> TLS(보안 검증 문서) -> https:443
        ResponseCookie deleteAccessCookie = cookieUtil.deleteCookieOfAccessToken();
        ResponseCookie deleteRefreshCookie = cookieUtil.deleteCookieOfRefreshToken();

        return ResponseEntity.status(HttpStatus.OK)
                .headers(httpHeaders -> {
                    httpHeaders.add("Set-Cookie", deleteAccessCookie.toString());
                    httpHeaders.add("Set-Cookie", deleteRefreshCookie.toString());
                })
                .build();
    }


}
