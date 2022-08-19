plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("java")
    id("war")
    id("org.jetbrains.kotlin.jvm")
}

group = "org.openeuler.sbom"
version = "1.0-SNAPSHOT"

val commonsIoVersion: String by project
val commonsLang3Version: String by project
val commonsCollections4Version: String by project
val packageUrlJavaVersion: String by project
val hibernateTypesVersion: String by project

repositories {
    exclusiveContent {
        forRepository {
            maven("https://repo.gradle.org/gradle/libs-releases/")
        }

        filter {
            includeGroup("org.gradle")
        }
    }

    exclusiveContent {
        forRepository {
            maven("https://repo.eclipse.org/content/repositories/sw360-releases/")
        }

        filter {
            includeGroup("org.eclipse.sw360")
        }
    }
}

dependencies {
    implementation(project(":analyzer"))
    implementation(project(":utils"))
    implementation(project(":clients:cve-manager"))
    implementation(project(":clients:oss-index"))

    implementation("oss-review-toolkit:model")
    implementation("oss-review-toolkit:utils:spdx-utils")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("org.apache.commons:commons-collections4:$commonsCollections4Version")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("com.github.package-url:packageurl-java:$packageUrlJavaVersion")
    implementation("com.vladmihalcea:hibernate-types-55:$hibernateTypesVersion")
    testImplementation("org.bgee.log4jdbc-log4j2:log4jdbc-log4j2-jdbc4.1:1.16")
    implementation("org.postgresql:postgresql")

    providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

springBoot {
    mainClass.set("org.openeuler.sbom.manager.SbomManagerApplication")
}

allprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.apache.logging.log4j:log4j-api")
        implementation("org.apache.logging.log4j:log4j-core")
        implementation("org.apache.logging.log4j:log4j-slf4j-impl")
        implementation("org.slf4j:slf4j-api")
    }

    configurations {
        all {
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        }
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}