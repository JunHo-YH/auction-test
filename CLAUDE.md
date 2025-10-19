# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 언어 설정 (Language Settings)

**항상 한국어로 답변할 것** - Always respond in Korean to this user.

## 프로젝트 개요

LastCall은 Spring Boot 3.5.6와 Java 17 기반의 경매 시스템입니다. Gradle 빌드 도구를 사용하며, JPA, Spring Security, MySQL을 핵심 기술로 사용합니다.

## 빌드 및 테스트 명령어

```bash
# 프로젝트 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 애플리케이션 실행
./gradlew bootRun

# 특정 테스트 클래스 실행
./gradlew test --tests "ClassName"

# 특정 테스트 메서드 실행
./gradlew test --tests "ClassName.methodName"

# 빌드 캐시 클리어
./gradlew clean
```

## 아키텍처 및 코드 구조

### 패키지 구조

프로젝트는 **도메인 기반 레이어드 아키텍처**로 구성되어 있습니다:

```
org.example.lastcall
├── common/               # 공통 기능 및 인프라
│   ├── config/          # Spring 설정 (Security, JPA Auditing)
│   ├── entity/          # 공통 엔티티 (BaseEntity, SoftDelete)
│   ├── exception/       # 전역 예외 처리 (GlobalExceptionHandler, ErrorCode)
│   ├── response/        # 공통 응답 포맷 (ApiResponse, PageResponse)
│   └── validation/      # 커스텀 유효성 검증 (UniqueNickname 등)
└── domain/              # 도메인별 비즈니스 로직
    ├── auction/         # 경매 도메인
    ├── auth/            # 인증 도메인 (회원가입 등)
    ├── bid/             # 입찰 도메인
    ├── point/           # 포인트 도메인
    ├── product/         # 상품 도메인
    └── user/            # 사용자 도메인
```

### 핵심 설계 패턴

1. **공통 엔티티 상속 구조**
   - `SoftDelete` → `BaseEntity` → 도메인 엔티티
   - `SoftDelete`: `deleted` 필드와 `softDelete()`, `restore()` 메서드 제공
   - `BaseEntity`: `createdAt`, `modifiedAt`, `deletedAt` 필드 포함
   - JPA Auditing을 통한 생성/수정 시각 자동 관리 (`@EnableJpaAuditing` 설정 필요)

2. **통일된 API 응답 형식**
   - 모든 API는 `ApiResponse<T>` 래퍼로 응답
   - 성공/실패, 메시지, 데이터, 타임스탬프를 포함
   - `@JsonInclude(JsonInclude.Include.NON_NULL)`로 null 필드 제외

3. **전역 예외 처리**
   - `GlobalExceptionHandler`가 모든 예외를 중앙 처리
   - `BusinessException`: 비즈니스 로직 예외 (ErrorCode 사용)
   - `MethodArgumentNotValidException`: 유효성 검증 실패 시 상세 에러 메시지 반환
   - 일반 예외는 500 에러로 처리

4. **커스텀 유효성 검증**
   - `@UniqueNickname` 등 도메인 특화 검증 어노테이션 사용
   - Spring Validation을 활용한 요청 데이터 검증

### 도메인 구조

각 도메인은 다음 구조를 따릅니다:
- `entity/`: JPA 엔티티
- `repository/`: Spring Data JPA 레포지토리
- `service/`: 비즈니스 로직 (일부 도메인에서 API 인터페이스 패턴 사용)
- `controller/` 또는 도메인명 + `Controller`: REST API 컨트롤러
- `dto/request`, `dto/response`: 요청/응답 DTO
- `enums/`: 도메인 관련 열거형

### 엔티티 네이밍 규칙

- **주의**: 일부 엔티티는 `Entity` 접미사를 사용 (예: `AuctionEntity`)
- 최근 변경사항(c0d1f5c): `UserEntity` → `User`로 변경
- 새 엔티티 작성 시 `Entity` 접미사 없이 도메인 이름 사용

## 데이터베이스

- **DBMS**: MySQL
- **프로파일 관리**: `application.yml`에서 `spring.profiles.active: local` 설정
- 환경별 설정 파일은 `application-{profile}.yml` 형식으로 관리 가능

## 주요 의존성

- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Web
- Spring Boot Starter Validation
- Lombok (컴파일 타임 코드 생성)
- MySQL Connector

## 개발 시 주의사항

1. **엔티티 작성**:
   - `BaseEntity`를 상속받아 공통 필드 자동 관리
   - **주의**: `BaseEntity`에 `deletedAt` 필드가 있지만, 논리적 삭제는 `SoftDelete`의 `deleted` 필드 사용
   - 삭제 시 `softDelete()` 메서드 호출, 복구 시 `restore()` 메서드 사용

2. **예외 처리**: 비즈니스 예외는 `BusinessException`과 `ErrorCode` 사용

3. **API 응답**:
   - 항상 `ApiResponse<T>`로 래핑하여 반환
   - 성공: `ApiResponse.success(message, data)` 또는 `ApiResponse.success(message)`
   - 실패: `ApiResponse.error(message)`

4. **유효성 검증**: `@Valid`와 커스텀 검증 어노테이션 활용

5. **서비스 레이어**: 일부 도메인에서 인터페이스 기반 서비스 패턴 사용 (예: `BidServiceApi` → `BidService`)
