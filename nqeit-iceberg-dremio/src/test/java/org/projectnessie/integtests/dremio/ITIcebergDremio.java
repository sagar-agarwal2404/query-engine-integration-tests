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

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
@ExtendWith({ IcebergDremioExtension.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class ITIcebergDremio{

  String baseUrl =  null;
  String token = null;
  String projectId = null;
  String sqlEndPoint = null;
  @BeforeEach
  public void setUp(String baseUrl, String token, String projectId){
    this.baseUrl = baseUrl;
    this.token = token;
    this.projectId = projectId;
    this.sqlEndPoint = "/ui/projects/" + projectId + "/sql";
  }

  @BeforeAll
  public static void dropTableIfExists(String baseUrl, String token, String projectId){
    String payload = "{ \"sql\": \"DROP TABLE IF EXISTS keith.sagar.foo_bar\" }";
    String endPoint = "/ui/projects/" + projectId + "/sql";
    RestAssured.baseURI = baseUrl;
    RequestSpecification httpRequest =  RestAssured.given()
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
    assertEquals(200,httpRequest.body(payload).post(endPoint).getStatusCode());
  }

  private RequestSpecification rest(){
    return RestAssured.given()
      .baseUri(baseUrl)
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
  }

//  private String getJobStatus( String jobId){
//    String jobStatusUri = "https://app.test1.dremio.site/ui/projects/" + projectId + "/jobs-listing/v1.0/" + jobId + "/jobDetails";
//    String jobStatus = "RUNNING";
//    Response response;
//    while(Objects.equals(jobStatus, "RUNNING")) {
//      response = rest()
//        .baseUri(jobStatusUri)
//        .when()
//        .get();
//      String jsonString = response.getBody().asString();
//      jobStatus = JsonPath.from(jsonString).get("jobStatus");
//    }
//    return jobStatus;
//  }
  @Order(100)
  @Test
  public void createTable(){
    String payload = "{ \"sql\": \"CREATE TABLE keith.sagar.foo_bar (id INT, val VARCHAR)\" }";
    assertEquals(200,rest().body(payload).post(sqlEndPoint).getStatusCode());
  }

  @Order(110)
  @Test
  public void insertInto() {
    String payload = "{ \"sql\": \"INSERT INTO keith.sagar.foo_bar VALUES (456,\'bar\') \" }";
    Response response = rest().body(payload).post(sqlEndPoint);
    String jsonString = response.getBody().asString();
    int returnedRowCount = JsonPath.from(jsonString).get("returnedRowCount");
    assertEquals(1, returnedRowCount);
  }

  @Order(120)
  @Test
  public void selectFrom(){
    String payload = "{ \"sql\": \"SELECT * FROM keith.sagar.foo_bar\" }";
    Map response = rest().body(payload).post(sqlEndPoint).as(Map.class);
  }

  @Order(130)
  @Test
  public void insertInto2() {
    String payload = "{ \"sql\": \"INSERT INTO keith.sagar.foo_bar VALUES (123,\'foo\') \" }";
    Response response = rest().body(payload).post(sqlEndPoint);
    String jsonString = response.getBody().asString();
    int returnedRowCount = JsonPath.from(jsonString).get("returnedRowCount");
    assertEquals(1, returnedRowCount);
  }

  @Order(140)
  @Test
  public void selectFrom2() {
    String payload = "{ \"sql\": \"SELECT * FROM keith.sagar.foo_bar }";
    Response response = rest().body(payload).post(sqlEndPoint);
  }

  @Order(150)
  @Test
  public void dropTable(){
    String payload = "{ \"sql\": \"DROP TABLE keith.sagar.foo_bar\" }";
    assertEquals(200,rest().body(payload).post(sqlEndPoint).getStatusCode());
  }
}
