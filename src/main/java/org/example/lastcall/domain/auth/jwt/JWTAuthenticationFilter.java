//package org.example.lastcall.domain.auth.jwt;
//
//import io.jsonwebtoken.Claims;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.example.lastcall.domain.auth.utils.CookieUtil;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.UUID;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JWTAuthenticationFilter extends OncePerRequestFilter {
//
//    private final JWTUtil jwtUtil;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
//            throws ServletException, IOException {
//
//        // 이미 다른 필터에서 인증이 설정되어 있으면 그대로 진행한다.
//        if (SecurityContextHolder.getContext().getAuthentication() != null) {
//            chain.doFilter(req, res);
//            return;
//        }
//
//        String token = extractAccessTokenFromCookie(req);
//        if (token == null || token.isBlank()) {
//            // 토큰이 없으면 "인증 없음" 상태로 다음 단계로 넘긴다.
//            // (이후 인가 규칙에서 401/403 판단)
//            chain.doFilter(req, res);
//            return;
//        }
//
//        try {
//            Claims claims = jwtUtil.validateAndGetClaims(token);
//
//            /** TODO:: 프로젝트에서 코드 제거 (JWT 장점 극대화)
//             * User user = userMapper.getUserByPublicId(userPublicId)
//             *                     .orElseThrow(() -> new GravyException(Status.USER_NOT_FOUND));
//             */
//
//            UUID userPublicId = UUID.fromString(claims.getSubject());
//            Grade grade = Grade.valueOf(claims.get("grade", String.class));
//
//            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + grade.name()));
//
//            //  인증 객체 생성 후 컨텍스트 저장한다.
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPublicId, null, authorities);
//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        } catch (Exception e) {
//            log.error("JWT 인증 실패: {}", e.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        chain.doFilter(req, res);
//    }
//
//    private String extractAccessTokenFromCookie(HttpServletRequest request) {
//        Cookie[] cookies = request.getCookies();
//        if (cookies == null) {
//            return null;
//        }
//
//        for (Cookie cookie : cookies) {
//            if (CookieUtil.ACCESS_COOKIE.equals(cookie.getName())) {
//                // TODO:: 보안 속성 검증 - XSS 공격 방지
//                if (!isSecureCookie(cookie)) {
//                    log.warn("=== 보안되지 않은 쿠키 발견: {} ===", cookie.getValue());
//                    return null;
//                }
//                return cookie.getValue();
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 쿠키의 보안 속성을 검증합니다.
//     * 환경에 따라 HttpOnly와 Secure 속성을 적절히 검증합니다.
//     */
//    private boolean isSecureCookie(Cookie cookie) {
//        // 1. 쿠키 값 유효성 검증
//        if (cookie.getValue() == null || cookie.getValue().trim().isEmpty()) {
//            log.warn("빈 쿠키 값 발견: {}", cookie.getName());
//            return false;
//        }
//
//        // TODO:: HttpOnly & Secure 검증
//
//
//        return true;
//    }
//
//}
