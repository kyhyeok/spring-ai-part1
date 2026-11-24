import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    // Kotlin 플러그인 버전 = Spring Boot 4.1.2 BOM이 관리하는 Kotlin 버전 (Boot를 올리면 함께 맞춘다)
    kotlin("jvm") version "2.3.21"
    // @Component 등 Spring 빈 클래스를 자동으로 open 처리 (프록시 생성용)
    kotlin("plugin.spring") version "2.3.21"
    id("org.springframework.boot") version "4.1.2"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain(25)
    compilerOptions {
        // -Xjsr305=strict: JSR-305 null 애너테이션도 엄격하게 반영 (Spring 7의 JSpecify는 Kotlin 2.1+가 기본으로 엄격 적용)
        // -Xannotation-default-target=param-property: 생성자 프로퍼티 애너테이션 대상 규칙 (Boot 4.1 문서 권장)
        // 두 플래그 모두 ch4~ch7 Kotlin과 같게 둔다
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot BOM: 버전 없이 선언한 Boot·Kotlin·Jackson·PostgreSQL 의존성의 버전을 정한다
    // (Java build.gradle의 io.spring.dependency-management 플러그인과 같은 역할)
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))

    // Spring MVC (Java의 spring-boot-starter-web은 Boot 4에서 deprecated → 대체 스타터, 구성은 같음)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // Spring AI BOM
    implementation(platform("org.springframework.ai:spring-ai-bom:2.0.2"))

    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Spring AI Vector Store API / SimpleVectorStore
    implementation("org.springframework.ai:spring-ai-vector-store")
    implementation("org.springframework.ai:spring-ai-starter-vector-store-pgvector")
    implementation("org.springframework.ai:spring-ai-vector-store-advisor")

    implementation("org.springframework.ai:spring-ai-pdf-document-reader")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // 평가 세트(evaluation-set.json)를 읽는 Jackson 2 ObjectMapper (Java와 같음)
    implementation("com.fasterxml.jackson.core:jackson-databind")
    // Jackson 2로 Kotlin data class(EvaluationSet)를 역직렬화하기 위한 모듈 (jacksonObjectMapper())
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Spring MVC 응답 직렬화(Boot 4 = Jackson 3)용 Kotlin 모듈
    implementation("tools.jackson.module:jackson-module-kotlin")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Gradle 9부터 JUnit Platform 런처를 직접 선언해야 테스트가 실행된다
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// 평가 테스트는 LLM 호출 비용이 발생하므로 기본 실행에서 제외한다.
//   ./gradlew test           -> 평가 테스트 제외
//   ./gradlew test -Peval    -> 평가 테스트 포함
tasks.test {
    useJUnitPlatform {
        if (!project.hasProperty("eval")) {
            excludeTags("eval")
        }
    }

    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}
