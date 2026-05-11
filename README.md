# 🛠️ Spring Advanced - 리팩토링 및 성능 개선 프로젝트

이 프로젝트는 기존 레거시 코드의 에러를 해결하고, 코드의 가독성 및 성능을 향상시키며, 테스트 코드를 보완하는 리팩토링 과제임.

## 📋 진행 내역 (Progress)

### 🚨 Lv 0. 프로젝트 환경 세팅 및 에러 해결
* **문제:** 프로젝트 초기 실행 시 `Application run failed` 에러 및 DB 연결 예외 발생.
* **원인:** `application.yml` (또는 `properties`) 내 JWT 시크릿 키(`jwt.secret.key`)와 데이터베이스(MySQL) 연결 설정(`url`, `username`, `password`) 누락.
* **해결:** 필수 환경 설정값을 명시적으로 추가하여 데이터베이스가 정상적으로 띄워지도록 조치함.

### 🧩 Lv 1. ArgumentResolver 등록
* **문제:** `@Auth` 어노테이션과 `AuthUser` 타입을 컨트롤러 파라미터로 사용할 때, 인증 정보가 정상적으로 주입되지 않는 문제.
* **해결:** `AuthUserArgumentResolver` 로직은 존재하나 스프링 빈으로 등록되지 않은 것을 파악함. `WebMvcConfigurer`를 구현한 `WebConfig` 클래스를 생성하여 `addArgumentResolvers` 메서드를 통해 리졸버를 명시적으로 등록함.

### ✨ Lv 2. 코드 가독성 및 유지보수성 개선
* **Early Return 적용 (`AuthService.signup`)**
  * 이메일 중복 체크 로직을 최상단으로 끌어올려, 중복 시 불필요한 패스워드 암호화 로직(`encode`)이 실행되지 않도록 리소스 낭비를 방지함.
* **불필요한 if-else 제거 (`WeatherClient`)**
  * 불필요한 `else` 블록을 제거하여 코드의 길이를 줄임.
* **Validation 책임 분리 (`UserService.changePassword`)**
  * 더러운 비밀번호 검증 로직(`if`문 정규식 체크)을 제거함.
  * `UserChangePasswordRequest`에 `@Pattern`, `@Size`, `@NotBlank` 등의 Validation 어노테이션을 적용하고, 컨트롤러에 `@Valid`를 붙여 책임을 분리함.

### 🚀 Lv 3. JPA N+1 문제 해결 (@EntityGraph)
* **문제:** `Todo` 엔티티를 조회할 때, 연관된 `User` 엔티티를 개별적으로 다시 조회하는 N+1 쿼리 문제 발생.
* **해결:** `TodoRepository`에 JPQL `JOIN FETCH` 대신, JPA가 제공하는 `@EntityGraph(attributePaths = {"user"})` 어노테이션을 사용하여 깔끔하게 미리 가져오기를 수행하도록 리팩토링함.

### 🧪 Lv 4. 테스트 코드 수정 및 서비스 버그 픽스
기존에 실패하던 테스트 코드의 원인을 분석하고 정상 통과하도록 수정함.
* **`PasswordEncoderTest`:** `matches()` 메서드의 인자 순서(평문, 암호문)가 잘못 들어가 있던 것을 올바르게 수정함.
* **예외 처리 검증 수정:** `ManagerServiceTest`와 `CommentServiceTest`에서 기대하는 예외 클래스(`ServerException` -> `InvalidRequestException`)와 에러 메시지가 실제 서비스 로직과 다르던 모순을 수정함.
* **`ManagerService` 로직 추가:** Todo를 생성한 유저가 없을 경우(`null`), `todo.getUser().getId()` 호출 시 발생하는 치명적인 `NullPointerException`을 발견하고, 앞단에서 `null` 체크를 먼저 하도록 로직을 추가함.

### 🛡️ Lv 5. 관리자 API 로깅 (AOP 적용)
* **요구사항:** 어드민 전용 API(`CommentAdminController`, `UserAdminController`) 접근 시 사용자 ID, 요청 시각, URL, 요청/응답 본문(Body)을 로깅해야 함.
* **해결 전략 (Interceptor vs AOP):**  HTTP Body는 스트림 형태라 한 번 읽으면 사라지기 때문에 Interceptor에서 처리하기 까다로움.
  * 따라서 AOP(`@Around`)를 채택함.
* **구현:** `AdminLoggingAspect`를 생성하여 어드민 컨트롤러를 지정하고, `ProceedingJoinPoint`를 활용해 `ObjectMapper`로 안전하게 JSON 로깅을 구현함.