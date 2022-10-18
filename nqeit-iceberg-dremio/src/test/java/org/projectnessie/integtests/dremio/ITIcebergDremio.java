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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
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
  @BeforeEach
  public void setUp(String baseUrl, String token, String projectId){
    this.baseUrl = baseUrl;
    this.token = token;
    this.projectId = projectId;
  }

  @BeforeAll
  public static void dropTableIfExists(String baseUrl, String token, String projectId){
    String payload = "{ \"sql\": \"DROP TABLE IF EXISTS keith.sagar.foo_bar\" }";
    String endPoint = "/ui/projects/" + projectId + "/sql";
    RestAssured.baseURI = baseUrl;
    RequestSpecification httpRequest =  RestAssured.given()
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json");
    httpRequest.body(payload).post(endPoint).then().statusCode(200).extract().as(Map.class);
  }

  private RequestSpecification rest(){
    return RestAssured.given()
      .baseUri(baseUrl)
      .header("Authorization","Bearer " + token)
      .header("Content-Type","application/json")
      .basePath("ui/projects/" + projectId);
  }

  private String getJobStatus( String jobId, String projectId){
    RestAssured.baseURI = "https://app.test1.dremio.site/ui/projects/" + projectId + "/jobs-listing/v1.0/" + jobId + "/jobDetails";
    String jobStatus = "RUNNING";
    while(Objects.equals(jobStatus, "RUNNING")) {
      jobStatus = (String)RestAssured
        .given()
        .header("Authorization","Bearer " + token)
        .header("Content-Type","application/json")
        .when()
        .get()
        .as(Map.class).get("jobStatus");
    }
    return jobStatus;
  }

  @Order(100)
  @Test
  public void createTable(){
    String payload = "{ \"sql\": \"CREATE TABLE keith.sagar.foo_bar (id INT, val VARCHAR)\" }";
    rest().body(payload).post("/sql").then().statusCode(200).extract().as(Map.class);
  }

  private static final List<List<Object>> tableRows = new ArrayList<>();
  private static final List<List<Object>> insertedRows = new ArrayList<>();
  @Order(110)
  @Test
  public void insertInto() {
    String payload = "{ \"sql\": \"INSERT INTO keith.sagar.foo_bar VALUES (456,\'bar\') \" }";
    Response response = rest().body(payload).post("/datasets/new_untitled_sql_and_run?newVersion=0008001390760");
    String jsonString = response.getBody().asString();
    String jobId = JsonPath.from(jsonString).get("jobId.id");
    if(Objects.equals(getJobStatus(jobId,projectId), "COMPLETED")) {
      tableRows.add(asList(456, "bar"));
    }
  }

  @Order(120)
  @Test
  public void selectFrom(){
    String payload = "{ \"sql\": \"SELECT * FROM keith.sagar.foo_bar\" }";
    Response response = rest().body(payload).post("/sql");
    String jsonString = response.getBody().asString();
    int id1 = JsonPath.from(jsonString).get("rows[0].row[0].v");
    String val1 = JsonPath.from(jsonString).get("rows[0].row[1].v");
    assertThat(tableRows.contains(Arrays.asList(id1, val1))).isTrue();
  }

  @Order(130)
  @Test
  public void insertInto2() {
    String payload = "{ \"sql\": \"INSERT INTO keith.sagar.foo_bar VALUES (123,\'foo\') \" }";
    Response response = rest().body(payload).post("/datasets/new_untitled_sql_and_run?newVersion=0008001390760");
    String jsonString = response.getBody().asString();
    String jobId = JsonPath.from(jsonString).get("jobId.id");
    if(Objects.equals(getJobStatus(jobId,projectId), "COMPLETED")) {
      tableRows.add(asList(123, "foo"));
    }
  }

  @Order(140)
  @Test
  public void selectFrom2() {
    String payload = "{ \"sql\": \"SELECT * FROM keith.sagar.foo_bar \"}";
    Response response = rest().body(payload).post("/sql");
    String jsonString = response.getBody().asString();
    int id1 = JsonPath.from(jsonString).get("rows[0].row[0].v");
    String val1 = JsonPath.from(jsonString).get("rows[0].row[1].v");
    int id2 = JsonPath.from(jsonString).get("rows[0].row[0].v");
    String val2 = JsonPath.from(jsonString).get("rows[0].row[1].v");
    insertedRows.add(asList(id1,val1));
    insertedRows.add(asList(id2,val2));

    assertThat(tableRows.containsAll(insertedRows)).isTrue();
  }

  @Order(150)
  @Test
  public void dropTable(){
    String payload = "{ \"sql\": \"DROP TABLE keith.sagar.foo_bar\" }";
    rest().body(payload).post("/sql").then().statusCode(200).extract().as(Map.class);
  }
}
