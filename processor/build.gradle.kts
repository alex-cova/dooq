plugins {
    id("java")
    id("maven-publish")
}

group = "org.example"
version = "1.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    implementation("org.dooq:dooq:1.0.0-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications.create<MavenPublication>("maven") {

        groupId = "org.dooq"
        artifactId = "processor"
        version = "1.0.0-SNAPSHOT"

        from(components["java"])
    }


}