plugins {
  `java-library`
  `tools-integrations-conventions`
  id("org.projectnessie")
}

repositories {
    mavenCentral()
}

dependencies {

  compileOnly(platform(rootProject))

  compileOnly(project(":nqeit-nessie-common"))
  compileOnly(libs.findbugs.jsr305)
  compileOnly(libs.microprofile.openapi)
  compileOnly(libs.jetbrains.annotations)
  compileOnly(libs.errorprone.annotations)

  compileOnly(platform(libs.junit.bom))
  compileOnly(libs.junit.jupiter.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
