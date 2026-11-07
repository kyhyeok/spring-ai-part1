# Spring AI Part 1 실습

Spring AI 강의 실습 코드를 챕터별 프로젝트로 정리한 저장소입니다.

## 기술 스택

| 항목 | 버전 |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.2 |
| Spring AI | 2.0.2 (OpenAI) |
| Maven | 3.9.16 (Maven Wrapper 포함) |

## 프로젝트 구성

| 폴더 | 챕터 | 내용 |
|---|---|---|
| [spring-start-ai](spring-start-ai) | ch3 | Spring AI 시작하기 — ChatClient 기본 호출, 사용 모델 조회 API |

각 폴더는 독립된 Maven 프로젝트입니다. IntelliJ에서는 폴더 안의 `pom.xml`을 프로젝트로 열면 됩니다.

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
cd spring-start-ai
./mvnw spring-boot:run
```

시작할 때 `AiRunner`가 샘플 질문을 한 번 보내고 답변을 콘솔에 출력합니다. 이때 OpenAI API 사용량이 발생합니다.

> 테스트(`./mvnw test`)와 패키징(`./mvnw package`)도 `@SpringBootTest`로 애플리케이션을 띄우므로
> `AiRunner`가 실행됩니다. 즉 `OPENAI_API_KEY`가 필요하고 API 호출이 발생합니다.
> 키 없이 빌드만 하려면 `./mvnw package -DskipTests`를 사용하세요.

### 3. API

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/ai/model` | 현재 사용 중인 ChatModel 클래스와 모델 이름 |
| POST | `/ask` | 질문/답변 (구현 예정) |

## 참고

- 강의 예제 저장소: [joneconsulting/spring-ai-part1](https://github.com/joneconsulting/spring-ai-part1)
