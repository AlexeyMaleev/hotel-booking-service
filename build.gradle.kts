plugins {
	java
	// Добавляем Kotlin
	kotlin("jvm") version "1.9.23"
	kotlin("plugin.spring") version "1.9.23"

	id("org.springframework.boot") version "3.5.8"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Final work for SpringFramework course from Skillbox"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

// Настройка Kotlin
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "21"
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.kafka:spring-kafka")
	implementation("io.projectreactor.kafka:reactor-kafka")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.15")

	implementation("org.flywaydb:flyway-core:10.0.0")
	implementation("org.flywaydb:flyway-database-postgresql:10.0.0")
	runtimeOnly("org.postgresql:postgresql")

	// Kotlin Runtime
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	implementation("org.mapstruct:mapstruct:1.5.3.Final")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.5.3.Final")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:mongodb")
	testImplementation("org.testcontainers:kafka")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.awaitility:awaitility:4.2.0")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// --- KOTEST ---
	testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
	testImplementation("io.kotest:kotest-assertions-core:5.8.0")
	testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
	testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
	testImplementation("io.kotest:kotest-framework-datatest:5.8.0")
}

tasks.withType<Test> {
	// Sequential execution to prevent OOM in CI/CD
	maxParallelForks = 1

	useJUnitPlatform()
}
