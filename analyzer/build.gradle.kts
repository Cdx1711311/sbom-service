val commonsLang3Version: String by project
val commonsIoVersion: String by project

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
    implementation(project(":utils"))
    implementation(project(":clients:vcs"))

    implementation("oss-review-toolkit:model")
    implementation("oss-review-toolkit:analyzer")
    implementation("oss-review-toolkit:reporter")

    implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.gradle:gradle-tooling-api:${gradle.gradleVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}