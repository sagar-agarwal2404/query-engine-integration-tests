plugins {
  `java-library`
  `tools-integrations-conventions`
  id("org.projectnessie")
}

repositories {
    mavenCentral()
}
val versionRestAsssured = "5.2.0"
dependencies {

  compileOnly(platform(rootProject))

  compileOnly(project(":nqeit-nessie-common"))
  compileOnly(libs.findbugs.jsr305)
  compileOnly(libs.microprofile.openapi)
  compileOnly(libs.jetbrains.annotations)
  compileOnly(libs.errorprone.annotations)

  compileOnly("io.rest-assured:rest-assured:$versionRestAsssured")
  compileOnly("io.rest-assured:json-schema-validator:$versionRestAsssured")

  compileOnly(platform(libs.junit.bom))
  compileOnly(libs.junit.jupiter.engine)

  compileOnly(libs.findbugs.jsr305)
  compileOnly(libs.bundles.junit.testing)
  compileOnly(libs.logback.classic)
  compileOnly(libs.slf4j.log4j.over.slf4j)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
