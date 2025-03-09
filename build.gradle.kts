plugins {
	java
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.securefromscratch"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.owasp.safetypes:safetypes-java:1.0.0")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	implementation("javax.validation:validation-api:2.0.1.Final")
	implementation("com.j2html:j2html:1.6.0")
	implementation("org.apache.commons:commons-collections4:4.1")
	implementation("org.apache.tika:tika-core:2.9.0")
	implementation("org.springframework.security:spring-security-core:6.3.4")
	implementation("org.springframework.security:spring-security-config:6.3.4")
	implementation("org.springframework.security:spring-security-web:6.3.4")
	implementation("org.springframework.boot:spring-boot-starter-actuator")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")


	implementation("com.googlecode.owasp-java-html-sanitizer:owasp-java-html-sanitizer:20220608.1")
	implementation("javax.annotation:javax.annotation-api:1.3.2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
