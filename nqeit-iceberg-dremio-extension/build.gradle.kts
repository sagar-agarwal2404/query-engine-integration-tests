plugins {
    id("java")
}

group = "org.projectnessie.integrations-tools-tests"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

  compileOnly(platform(rootProject))

  compileOnly(project(":nqeit-nessie-common"))
  compileOnly("com.google.code.findbugs", "jsr305")
  compileOnly("org.eclipse.microprofile.openapi", "microprofile-openapi-api")
  compileOnly("org.jetbrains", "annotations")

  compileOnly(platform("org.junit:junit-bom"))
  compileOnly("org.junit.jupiter", "junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
