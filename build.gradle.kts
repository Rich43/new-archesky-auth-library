import java.lang.System.getenv

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.70"
    id("com.gradle.plugin-publish") version "0.9.10"
    id("java-library")
    `maven-publish`
    maven
}

apply(plugin = "com.apollographql.apollo")

buildscript {
    tasks.findByName("build")?.dependsOn("bootJar")
    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.apollographql.apollo:apollo-gradle-plugin:1.4.4")
        classpath("org.jfrog.buildinfo:build-info-extractor-gradle:4+")
    }
}

group = "com.archesky.auth.library"
version = "0.0.${getenv().getOrDefault("GITHUB_RUN_ID", "1")}-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
}

repositories {
    jcenter()
    maven("https://maven.pkg.github.com/Rich43/archesky-common-library") {
        credentials {
            username = "Rich43"
            password = getenv()["GITHUB_TOKEN"]
        }
    }
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.apollographql.apollo:apollo-runtime:1.4.4")
    implementation("com.squareup.okio:okio:2.5.0")
    implementation("org.springframework:spring-context:5.2.5.RELEASE")
    implementation("org.springframework.security:spring-security-core:5.3.1.RELEASE")
    implementation("org.springframework.security:spring-security-config:5.3.1.RELEASE")
    implementation("org.springframework.security:spring-security-web:5.3.1.RELEASE")
    implementation("com.archesky.common.library:archesky-common-library:0.0.251130938-SNAPSHOT")
    implementation("com.graphql-java-kickstart:graphql-java-tools:6.1.0")
    implementation("javax.servlet:javax.servlet-api:4.0.1")
}

val sourceJar = task<Jar>("sourceJar") {
    from(sourceSets.main.get().allJava)
    from(kotlin.sourceSets.main.get().kotlin)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Rich43/archesky-content-server")
            credentials {
                username = getenv().getOrDefault("GITHUB_ACTOR", "Rich43")
                password = getenv()["GITHUB_TOKEN"]
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(sourceJar) {
                classifier = "sources"
            }
        }
    }
}
