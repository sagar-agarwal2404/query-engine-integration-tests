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

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.*;

public class DremioHelper {
  String token;
  String projectId;
  String baseUrl;

  DremioHelper(String projectId, String token, String baseUrl) {
    this.projectId = projectId;
    this.token = token;
    this.baseUrl = baseUrl;
  }

  public RequestSpecification rest() {
    return RestAssured.given()
        .baseUri(baseUrl)
        .header("Authorization", "Bearer " + token)
        .header("Content-Type", "application/json")
        .basePath("ui/projects/" + projectId);
  }

  public String waitForJobStatus(String jobId) {
    String jobStatus = "RUNNING";
    while (Objects.equals(jobStatus, "RUNNING")) {
      jobStatus =
          (String)
              rest()
                  .when()
                  .get("/jobs-listing/v1.0/" + jobId + "/jobDetails")
                  .as(Map.class)
                  .get("jobStatus");
    }
    return jobStatus;
  }

  public List<List<Object>> runSelectQuery(String query) {
    String payload = "{ \"sql\": \"" + query + "\" }";
    Response response = rest().body(payload).post("/sql");
    return parseQueryResult(response);
  }

  public void runInsertQuery(String query) {
    String payload = "{ \"sql\": \"" + query + "\" }";
    Response response =
        rest().body(payload).post("/datasets/new_untitled_sql_and_run?newVersion=0008001390760");
    String jsonString = response.getBody().asString();
    String jobId = JsonPath.from(jsonString).get("jobId.id");
    assertThat("COMPLETED").isEqualTo(waitForJobStatus(jobId));
  }

  public List<List<Object>> parseQueryResult(Response response) {
    String jsonString = response.getBody().asString();
    int noOfRows = JsonPath.from(jsonString).get("returnedRowCount");
    List<List<Object>> list = new ArrayList<>();
    for (int i = 0; i < noOfRows; i++) {
      int id = JsonPath.from(jsonString).get(String.format("rows[%d].row[0].v", i));
      String val = JsonPath.from(jsonString).get(String.format("rows[%d].row[1].v", i));
      list.add(asList(id, val));
    }
    return list;
  }

  public void executeDmlStatement(String query) {
    String payload = "{ \"sql\": \"" + query + "\" }";
    rest().body(payload).post("/sql").getBody().asPrettyString();
  }
}
