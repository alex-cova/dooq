plugins {
    id("java")
    id("maven-publish")
}

group = "org.dooq"
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
        artifactId = "dooq-processor"
        version = "1.0.0-SNAPSHOT"

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
