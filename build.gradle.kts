plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.lombok)
}

group = "com.github.daniel-shuy"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    annotationProcessor(platform(libs.spring.boot.dependencies))

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.springdoc.openapi.ui)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.oauth2.authorization.server)
    implementation(libs.mapstruct.core)

    annotationProcessor(libs.hibernate.jpamodelgen)
    annotationProcessor(libs.mapstruct.processor)

    runtimeOnly(libs.spring.boot.devtools)
    runtimeOnly(libs.h2.database)

    testImplementation(libs.spring.boot.starter.test)
}

lombok {
    version = libs.versions.lombok.get()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
