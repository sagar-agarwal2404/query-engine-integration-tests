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

package org.projectnessie.integtests.dremio;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectnessie.integtests.nessie.NessieTestsExtension;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
@ExtendWith({NessieTestsExtension.class, IcebergDremioExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class ITIcebergDremio{

  String baseUrl =  "https://app.test1.dremio.site";
  String token = "tAHxjsGOSPCfzNfrVhQ4auT0uEw3RGbvSrKriUKincyswR938naEJN9e/zG9ew==";
  String projectId = "35770c76-b747-4151-a9a2-06f0302f0805";
  @BeforeEach
  public void setUp(String url, String token, String projectId){
    this.baseUrl = url;
    this.token = token;
    this.projectId = projectId;
  }

  @Order(100)
  @Test
  public void createTable(){
    String payload = "{" +
      "    \"sql\": \"CREATE TABLE keith.sagar.foo_bar (id INT, val VARCHAR)\"" +
      "}";
    String endPoint = "/ui/projects/" + projectId + "/sql";
    RestAssured.baseURI = baseUrl;
    RequestSpecification httpRequest =  RestAssured.given()
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
    Response response = httpRequest.body(payload).post(endPoint);
    assertEquals(200, response.getStatusCode());
  }

  private static final List<List<Object>> tableRows = new ArrayList<>();
  @Order(110)
  @Test
  public void insertInto(){
    String payload = "{" +
      "    \"sql\": \"INSERT INTO keith.sagar.foo_bar VALUES (456,\'bar\') \"" +
      "}";
    String endPoint = "/ui/projects/" + projectId + "/sql";
    RestAssured.baseURI = baseUrl;
    RequestSpecification httpRequest =  RestAssured.given()
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
    Response response1 = httpRequest.body(payload).post(endPoint);
    assertEquals(200, response1.getStatusCode());

    String jsonString = response1.getBody().asString();
    String jobId = JsonPath.from(jsonString).get("jobId.id");
    RestAssured.baseURI = "https://app.test1.dremio.site/ui/projects/" + projectId + "/jobs-listing/v1.0/" + jobId + "/jobDetails";
    String jobStatus = "RUNNING";
    Map response2;
    while(Objects.equals(jobStatus, "RUNNING")) {
      response2 = RestAssured.given()
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/json")
        .when()
        .get()
        .as(Map.class);
      jobStatus = (String)response2.get("jobStatus");
    }
    tableRows.add(asList(456, "bar"));
  }
  @Order(120)
  @Test
  public void selectFrom(){
    String payload = "SELECT * FROM keith.sagar.\"foo_bar\"";

    String endPoint = "/ui/projects/" + projectId + "/sql";
    RestAssured.baseURI = baseUrl;
    RequestSpecification httpRequest =  RestAssured.given()
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
    Map response = httpRequest.body(payload).post(endPoint).as(Map.class);
  }

  @Order(130)
  @Test
  public void insertInto2(){
    //for inserting the data
    String payload = "{" +
      "    \"sql\": \"INSERT INTO keith.sagar.foo_bar VALUES (123,\'foo\') \"" +
      "}";

    //end point for create table query
    String endPoint = "/ui/projects/" + projectId + "/sql";

    RestAssured.baseURI = baseUrl;
    RequestSpecification httpRequest =  RestAssured.given()
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
    Map response1 = httpRequest.body(payload).post(endPoint).as(Map.class);

    String jobId = (String)response1.get("jobId.id");
    String response_url = "https://app.test1.dremio.site/ui/projects/" + projectId + "/jobs-listing/v1.0/" + jobId + "/jobDetails";
    RestAssured.baseURI = response_url;
    String jobStatus = "RUNNING";
    Map response2;
    while(Objects.equals(jobStatus, "RUNNING")) {
      response2 = RestAssured.given()
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/json")
        .when()
        .get()
        .as(Map.class);
      jobStatus = (String) response2.get("jobStatus");
    }
  }

  @Order(140)
  @Test
  public void selectFrom2(){
    String payload = "SELECT * FROM keith.sagar.\"foo_bar\"";

    String endPoint = "/ui/projects/" + projectId + "/sql";
    RestAssured.baseURI = baseUrl;
    RequestSpecification httpRequest =  RestAssured.given()
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
    Map response = httpRequest.body(payload).post(endPoint).as(Map.class);
  }
}
