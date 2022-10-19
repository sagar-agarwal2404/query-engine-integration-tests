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

group = "org.projectnessie.integrations-tools-tests"

version = "0.1-SNAPSHOT"

val versionRestAsssured = "5.2.0"

repositories { mavenCentral() }

dependencies {
  compileOnly(platform(rootProject))
  testCompileOnly(platform(rootProject))

  implementation(project(":nqeit-nessie-common"))
  implementation(project(":nqeit-iceberg-dremio-extension"))

  testImplementation("io.rest-assured:rest-assured:$versionRestAsssured")
  testImplementation("io.rest-assured:json-schema-validator:$versionRestAsssured")
  testImplementation("io.rest-assured:kotlin-extensions:5.2.0")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

  compileOnly(libs.findbugs.jsr305)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.bundles.junit.testing)
  testRuntimeOnly(libs.junit.jupiter.engine)
  testRuntimeOnly(libs.logback.classic)
  testRuntimeOnly(libs.slf4j.log4j.over.slf4j)
}

tasks.getByName<Test>("test") { useJUnitPlatform() }
