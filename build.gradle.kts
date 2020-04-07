import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.61"
  id("java-gradle-plugin")
}

repositories {
  mavenLocal()
  mavenCentral()
  jcenter()
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "11"
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

group = "uk.gov.justice.digital.hmpps"

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  implementation("org.springframework.boot:spring-boot-gradle-plugin:2.2.6.RELEASE")

  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.1")
}
