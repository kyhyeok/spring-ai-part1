# Spring AI Part 1 실습

Spring AI 강의 실습 코드를 챕터별 프로젝트로 정리한 저장소입니다.
같은 실습을 **Java**로 작성하고, AI(Claude Code)를 활용해 **Kotlin**으로 변환한 버전을 나란히 둡니다.

## 기술 스택

| 항목 | 버전 |
|---|---|
| Java | 25 |
| Kotlin | 2.3.21 (Spring Boot 4.1.2 관리 버전) |
| Spring Boot | 4.1.2 |
| Spring AI | 2.0.2 (OpenAI) |
| Maven | 3.9.16 (Maven Wrapper 포함) |

## 프로젝트 구성

```
spring-ai-part1/
├── Java/
│   └── ch3_begin/     # Java 원본
└── Kotlin/
    └── ch3_begin/     # Java 원본을 Kotlin으로 변환
```

| 폴더 | 언어 | 챕터 | 내용 |
|---|---|---|---|
| [Java/ch3_begin](Java/ch3_begin) | Java | ch3 | Spring AI 시작하기 — ChatClient 기본 호출, 스트리밍, 옵션(temperature·max-tokens), 사용 모델 조회 API |
| [Kotlin/ch3_begin](Kotlin/ch3_begin) | Kotlin | ch3 | 위 Java 프로젝트의 Kotlin 변환본 (동일한 API) |

각 폴더는 독립된 Maven 프로젝트입니다. IntelliJ에서는 폴더 안의 `pom.xml`을 프로젝트로 열면 됩니다.

### Java → Kotlin 변환 시 주요 차이

| Java | Kotlin |
|---|---|
| `record Question(String question)` | `data class Question(val question: String)` — non-null이라 `POST /ask`에 `{}`·`{"question":null}`이 오면 Java는 통과, Kotlin은 400 |
| `record Answer(String answer)` | `data class Answer(val answer: String?)` — Spring AI `getText()`가 `@Nullable`이라 null 허용 (Java와 동일하게 `{"answer": null}`) |
| `response.getResult().getOutput()` (결과가 없으면 NPE) | `checkNotNull(response.result)` — 같은 500이지만 원인 메시지가 남음 |
| `String` 반환 (null 가능) | `String?` — Spring AI의 `content()`가 `@Nullable`이라 Kotlin 타입에 그대로 드러남 |
| 생성자 + `private final` 필드 | 주 생성자 프로퍼티 (`class Foo(private val bar: Bar)`) |
| Lombok | 사용하지 않음 (Kotlin 문법으로 대체) |
| `@Value("${...}")` | `@Value("\${...}")` — `$`는 Kotlin 문자열 템플릿이라 이스케이프 |
| `produces = MediaType.X` | `produces = [MediaType.X]` — 애너테이션 배열 문법 |
| — | `kotlin-maven-plugin` + `spring` 컴파일러 플러그인(Spring 빈 클래스를 자동으로 `open`), `jackson-module-kotlin` |

## 실행 방법

### 1. OpenAI API 키 설정

API 키는 저장소에 커밋하지 않고 환경변수 `OPENAI_API_KEY`로 주입합니다.
`application.yml`은 `${OPENAI_API_KEY}`를 참조만 합니다.

**macOS** — 키체인에 저장하고 셸 시작 시 불러오기:

```zsh
# 프롬프트가 뜨면 키를 붙여넣기 (화면에 표시되지 않음, -U: 이미 있으면 덮어쓰기)
security add-generic-password -U -a "$USER" -s OPENAI_API_KEY -w

# ~/.zshrc에는 키체인 조회 명령만 추가 (키 값은 파일에 남지 않음)
# 최초 1회만 실행 — 다시 실행하면 같은 줄이 중복으로 추가됨
echo 'export OPENAI_API_KEY="$(security find-generic-password -a "$USER" -s OPENAI_API_KEY -w 2>/dev/null)"' >> ~/.zshrc
source ~/.zshrc
```

**현재 터미널에서만 사용** (터미널을 닫으면 사라짐):

```zsh
# zsh (macOS 기본 셸)
read -rs "OPENAI_API_KEY?OpenAI API Key: " && echo && export OPENAI_API_KEY
```

```bash
# bash (Linux 등)
read -rsp "OpenAI API Key: " OPENAI_API_KEY && echo && export OPENAI_API_KEY
```

### 2. 실행

```bash
cd Java/ch3_begin      # Kotlin 버전은 cd Kotlin/ch3_begin
./mvnw spring-boot:run
```

두 프로젝트 모두 기본 포트 8080을 사용합니다. 동시에 띄우려면 한쪽을 `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`로 실행하세요.

시작할 때 `AiRunner`가 샘플 질문을 한 번 보내고 답변을 콘솔에 출력합니다. 이때 OpenAI API 사용량이 발생합니다.

> 테스트(`./mvnw test`)와 패키징(`./mvnw package`)도 `@SpringBootTest`로 애플리케이션을 띄우므로
> `AiRunner`가 실행됩니다. 즉 `OPENAI_API_KEY`가 필요하고 API 호출이 발생합니다.
> 키 없이 빌드만 하려면 `./mvnw package -DskipTests`를 사용하세요.

### 3. API

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/ai/ask?question=...` | 질문을 보내고 답변 전체를 한 번에 받음 (`call()`) |
| GET | `/api/ai/stream?question=...` | 답변을 SSE(`text/event-stream`)로 조각조각 받음 (`stream()`) |
| GET | `/api/ai/model` | 현재 사용 중인 ChatModel 클래스와 모델 이름 |
| POST | `/ask` | JSON 본문 `{"question": "..."}` → `{"answer": "..."}` (서비스 계층에서 `ChatModel` + `PromptTemplate` 사용) |

각 프로젝트의 `http/chat.http`에서 IntelliJ HTTP Client로 바로 호출해 볼 수 있습니다.

## 참고

- 강의 예제 저장소: [joneconsulting/spring-ai-part1](https://github.com/joneconsulting/spring-ai-part1)
