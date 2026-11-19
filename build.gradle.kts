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
        // 두 플래그 모두 ch4·ch5 Kotlin과 같게 둔다
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot BOM: 버전 없이 선언한 Boot·Kotlin·Jackson 의존성의 버전을 정하고,
    // 전이 의존성(openai-java가 쓰는 Jackson 2 등)도 Boot 관리 버전으로 맞춘다
    // (Maven 챕터의 spring-boot-starter-parent와 같은 역할. Java build.gradle에는 없어서 일부 라이브러리 버전이 Java와 다르다)
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")

    // Spring AI BOM
    implementation(platform("org.springframework.ai:spring-ai-bom:2.0.2"))

    // Spring AI OpenAI - Chat / Embedding
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Spring AI Vector Store API / SimpleVectorStore
    implementation("org.springframework.ai:spring-ai-vector-store")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Gradle 9부터 JUnit Platform 런처를 직접 선언해야 테스트가 실행된다
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
