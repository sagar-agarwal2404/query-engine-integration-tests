/*
 * Copyright (C) 2022 Dremio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  `java-library`
  `tools-integrations-conventions`
  id("org.projectnessie")
}

repositories { mavenCentral() }

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

tasks.getByName<Test>("test") { useJUnitPlatform() }
