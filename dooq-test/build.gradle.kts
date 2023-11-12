plugins {
    id("java")
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    implementation("org.dooq:dooq:1.0.0-SNAPSHOT")
    implementation("software.amazon.awssdk:dynamodb:2.20.22")

    annotationProcessor("org.dooq:processor:1.0.0-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}