# Flowery-auth-server

# 인증 API 문서

기본 URL: `/api/auth`

## API 엔드포인트 목록

### 1. 회원가입
신규 사용자 계정을 생성합니다.

**URL:** `POST /api/auth/users`

**요청 본문:**
```json
{
  ident: String,
  userEmail: String,
  password: String,
  userName: String
}
```

**응답:**
- `200 OK`: 회원가입 성공
    - SignUpResponseDto 반환
- `400 Bad Request`: 잘못된 입력값
- `409 Conflict`: 이미 존재하는 사용자
- `500 Internal Server Error`: 서버 오류

### 2. 로그인
사용자 인증 후 액세스 토큰을 발급합니다.

**URL:** `POST /api/auth`

**요청 본문:**
```json
{
  ident: String,
  password: String
}
```

**응답:**
- `200 OK`: 로그인 성공
    - LoginResponseDto(액세스 토큰 포함) 반환
- `401 Unauthorized`: 잘못된 인증 정보
- `500 Internal Server Error`: 서버 오류

### 3. 이메일 인증코드 전송
이메일로 인증 코드를 전송합니다.

**URL:** `POST /api/auth/emails`

**요청 본문:**
```json
{
  userEmail: String,
  userName: String
}
```

**응답:**
- 이메일 전송 결과를 문자열로 반환

### 4. 이메일 인증코드 확인
전송된 이메일 인증 코드를 검증합니다.

**URL:** `POST /api/auth/verifications`

**요청 본문:**
```json
{
  userEmail: String,
  userCode: String
}
```

**응답:**
- 인증 코드 검증 결과를 문자열로 반환

## 참고사항
- 모든 요청은 JSON 형식을 사용합니다
- 인증 관련 엔드포인트는 Spring Security로 보호됩니다
- `@Validated` 어노테이션을 통한 요청 유효성 검사를 수행합니다
- Project Reactor(Mono)를 사용한 리액티브 프로그래밍 기반입니다

## 에러 처리
API는 다음과 같은 표준 HTTP 상태 코드를 사용합니다:
- `200`: 성공
- `400`: 잘못된 요청 - 유효하지 않은 입력값
- `401`: 인증 실패 - 잘못된 인증 정보
- `409`: 충돌 - 이미 존재하는 리소스
- `500`: 서버 오류 - 서버 측 에러