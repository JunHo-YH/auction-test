package org.example.lastcall.domain.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.lastcall.common.config.PasswordEncoder;
import org.example.lastcall.common.util.DateTimeUtil;
import org.example.lastcall.common.util.GeneratorUtil;
import org.example.lastcall.domain.auth.dto.request.SignupRequest;
import org.example.lastcall.domain.auth.dto.request.UserLoginDto;
import org.example.lastcall.domain.auth.email.entity.EmailVerification;
import org.example.lastcall.domain.auth.email.model.EmailVerificationStatus;
import org.example.lastcall.domain.auth.email.repository.EmailVerificationRepository;
import org.example.lastcall.domain.auth.entity.RefreshToken;
import org.example.lastcall.domain.auth.jwt.JWTUtil;
import org.example.lastcall.domain.auth.model.RefreshTokenStatus;
import org.example.lastcall.domain.auth.repository.RefreshTokenRepository;
import org.example.lastcall.domain.user.entity.User;
import org.example.lastcall.domain.user.enums.Role;
import org.example.lastcall.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

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

    @Transactional
    public UserLoginDto.Response userLogin(final UserLoginDto.Request request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.email().trim())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 유저입니다."));

        user.validatePassword(passwordEncoder, request.password());

        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        Date refreshTokenExpiredDate = jwtUtil.getExpiration(refreshToken);
        LocalDateTime refreshTokenExpiredAt = DateTimeUtil.convertToLocalDateTime(refreshTokenExpiredDate);

        RefreshToken newRefreshToken = RefreshToken.create(
                user.getId(),
                refreshToken,
                RefreshTokenStatus.ACTIVE,
                refreshTokenExpiredAt
        );

        // 기존 ACTIVE 토큰들을 REVOKED로 변경
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndStatus(user.getId(), RefreshTokenStatus.ACTIVE);
        activeTokens.forEach(RefreshToken::revoke);

        refreshTokenRepository.save(newRefreshToken);

        return new UserLoginDto.Response(accessToken, refreshToken);
    }

    @Transactional
    public void userLogout(final String requestedRefreshToken) {
        // refresh token 유효성 검증 및 조회
        RefreshToken refreshToken = validateRefreshToken(requestedRefreshToken);

        // 해당 사용자의 모든 활성 refresh token 무효화 (REVOKED)
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndStatus(
                refreshToken.getUserId(),
                RefreshTokenStatus.ACTIVE
        );
        activeTokens.forEach(RefreshToken::revoke);
    }

    private RefreshToken validateRefreshToken(String requestedRefreshToken) {
        RefreshToken refreshToken =
                refreshTokenRepository.findByTokenAndStatus(requestedRefreshToken, RefreshTokenStatus.ACTIVE)
                        .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));

        final LocalDateTime now = LocalDateTime.now();
        if (refreshToken.getExpiredAt().isBefore(now)) {
            throw new RuntimeException("만료된 토큰입니다.");
        }

        return refreshToken;
    }
}
