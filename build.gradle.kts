plugins {
    id("maven-publish")
    id("java-library")
}

group = "com.abstract-mind"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal();
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

dependencies {

    compileOnly("com.fasterxml.jackson.core:jackson-core:2.14.2")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    compileOnly("org.jetbrains:annotations:23.1.0")

    implementation("software.amazon.awssdk:dynamodb:2.20.22")
    implementation("org.ow2.asm:asm:9.5")

    testCompileOnly("org.jetbrains:annotations:23.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.openjdk.jmh:jmh-core:1.35")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.35")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}


publishing {

    publications.create<MavenPublication>("maven") {

        groupId = "com.abstract-mind"
        artifactId = "dooq"
        version = "1.0"

        from(components["java"])
    }

    repositories {
        maven {
            name = "Github"
            url = uri("https://maven.pkg.github.com/alex-cova/dooq")
            credentials {
                username = System.getenv("MAVEN_USER")
                password = System.getenv("MAVEN_SECRET")
            }
        }
    }
}
