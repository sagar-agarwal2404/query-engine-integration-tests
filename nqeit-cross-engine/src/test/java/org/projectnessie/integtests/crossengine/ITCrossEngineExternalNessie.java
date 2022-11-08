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
package org.projectnessie.integtests.crossengine;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.projectnessie.integtests.dremio.DremioHelper;
import org.projectnessie.integtests.dremio.IcebergDremioExtension;
import org.projectnessie.integtests.flink.Flink;
import org.projectnessie.integtests.flink.FlinkHelper;
import org.projectnessie.integtests.flink.IcebergFlinkExtension;
import org.projectnessie.integtests.iceberg.spark.IcebergSparkExtension;
import org.projectnessie.integtests.iceberg.spark.Spark;
import org.projectnessie.integtests.nessie.NessieTestsExtension;

@ExtendWith({
  IcebergSparkExtension.class,
  IcebergFlinkExtension.class,
  NessieTestsExtension.class,
  IcebergDremioExtension.class
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ITCrossEngineExternalNessie {

  @BeforeAll
  public static void dropTableIfExists(@Spark SparkSession spark, DremioHelper dremioHelper) {
    spark.sql("DROP TABLE IF EXISTS nessie.db.from_spark");
    spark.sql("DROP TABLE IF EXISTS nessie.db.from_sonar");
    spark.sql("DROP TABLE IF EXISTS nessie.db.from_flink");
  }

  // create
  // table(spark)--->insert(spark)--->insert(sonar)---->insert(flink)--->select(spark)--->select(sonar)--->select(flink)
  @Order(110)
  @Test
  public void createTablesUsingSpark(@Spark SparkSession spark) {
    spark.sql("CREATE TABLE nessie.db.from_spark (id int, val string)");
  }

  private static final List<List<Object>> tableRows = new ArrayList<>();

  @Order(120)
  @Test
  public void insertUsingSpark(@Spark SparkSession spark) {
    spark.sql(format("INSERT INTO nessie.db.%s VALUES (456, \"bar\")", "from_spark"));
    tableRows.add(asList(456, "bar"));
  }

  @Order(130)
  @Test
  public void insertUsingDremio(DremioHelper dremioHelper) {
    dremioHelper.runInsertQuery("INSERT INTO keith.db.from_spark VALUES (123,'foo')");
    tableRows.add(asList(123, "foo"));
  }

  @Order(140)
  @Test
  public void insertUsingFlink(@Flink FlinkHelper flink) {
    flink.sql(
        "INSERT INTO %s (id, val) VALUES (789, 'cool')", flink.qualifiedTableName("from_spark"));
    tableRows.add(asList(789, "cool"));
  }

  @Order(150)
  @Test
  public void selectFromSpark(@Spark SparkSession spark) {
    assertThat(
            spark
                .sql(format("SELECT id, val FROM nessie.db.%s", "from_spark"))
                .collectAsList()
                .stream()
                .map(r -> asList(r.get(0), r.get(1))))
        .containsExactlyInAnyOrderElementsOf(tableRows);
  }

  @Order(160)
  @Test
  public void selectFromDremio(DremioHelper dremioHelper) {
    List<List<Object>> rows = dremioHelper.runSelectQuery("SELECT * FROM keith.db.from_spark");
    assertThat(tableRows).containsAll(rows);
  }

  @Order(170)
  @Test
  public void selectFromFlink(@Flink FlinkHelper flink) {
    assertThat(flink.sql("SELECT * FROM %s", flink.qualifiedTableName("from_spark")))
        .hasSize(3)
        .map(r -> Arrays.asList(r.getField(0), r.getField(1)))
        .containsExactlyInAnyOrder(
            Arrays.asList(123, "foo"), Arrays.asList(456, "bar"), Arrays.asList(789, "cool"));
  }

  // create
  // table(dremio)--->insert(dremio)--->insert(spark)---->insert(flink)---->select(spark)--->select(dremio)---->select(flink)
  @Order(200)
  @Test
  public void createTableUsingdremio(DremioHelper dremioHelper) {
    dremioHelper.executeDmlStatement("CREATE TABLE keith.db.from_sonar (id INT, val VARCHAR)");
  }

  private static final List<List<Object>> tableRows2 = new ArrayList<>();

  @Order(210)
  @Test
  public void insertUsingDremio2(DremioHelper dremioHelper) {
    dremioHelper.runInsertQuery("INSERT INTO keith.db.from_sonar VALUES (456,'bar')");
    tableRows2.add(asList(456, "bar"));
  }

  @Order(220)
  @Test
  public void insertUsingSpark2(@Spark SparkSession spark) {
    spark.sql(format("INSERT INTO nessie.db.%s VALUES (123, \"foo\")", "from_sonar"));
    tableRows2.add(asList(123, "foo"));
  }

  @Order(230)
  @Test
  public void insertUsingFlink2(@Flink FlinkHelper flink) {
    flink.sql(
        "INSERT INTO %s (id, val) VALUES (789, 'cool')", flink.qualifiedTableName("from_sonar"));
    tableRows2.add(asList(789, "cool"));
  }

  @Order(240)
  @Test
  public void selectFromSpark2(@Spark SparkSession spark) {
    assertThat(
            spark
                .sql(format("SELECT id, val FROM nessie.db.%s", "from_sonar"))
                .collectAsList()
                .stream()
                .map(r -> asList(r.get(0), r.get(1))))
        .containsExactlyInAnyOrderElementsOf(tableRows2);
  }

  @Order(250)
  @Test
  public void selectFromDremio2(DremioHelper dremioHelper) {
    List<List<Object>> rows = dremioHelper.runSelectQuery("SELECT * FROM keith.db.from_sonar");
    assertThat(tableRows2).containsAll(rows);
  }

  @Order(260)
  @Test
  public void selectFromFlink2(@Flink FlinkHelper flink) {
    assertThat(flink.sql("SELECT * FROM %s", flink.qualifiedTableName("from_sonar")))
        .hasSize(3)
        .map(r -> Arrays.asList(r.getField(0), r.getField(1)))
        .containsExactlyInAnyOrder(
            Arrays.asList(123, "foo"), Arrays.asList(456, "bar"), Arrays.asList(789, "cool"));
  }

  // create
  // table(flink)--->insert(flink)--->insert(dremio)---->insert(spark)---->select(flink)--->select(spark)---->select(dremio)
  @Order(300)
  @Test
  public void createTableUsingFlink(@Flink FlinkHelper flink) {
    flink.sql("CREATE TABLE %s (id INT, val VARCHAR)", flink.qualifiedTableName("from_flink"));
  }

  private static final List<List<Object>> tableRows3 = new ArrayList<>();

  @Order(310)
  @Test
  public void insertUsingFlink3(@Flink FlinkHelper flink) {
    flink.sql(
        "INSERT INTO %s (id, val) VALUES (123, 'foo')", flink.qualifiedTableName("from_flink"));
    tableRows3.add(asList(123, "foo"));
  }

  @Order(320)
  @Test
  public void insertUsingDremio3(DremioHelper dremioHelper) {
    dremioHelper.runInsertQuery("INSERT INTO keith.db.from_flink VALUES (456,'bar')");
    tableRows3.add(asList(456, "bar"));
  }

  @Order(330)
  @Test
  public void insertUsingSpark3(@Spark SparkSession spark) {
    spark.sql(format("INSERT INTO nessie.db.%s VALUES (789, \"cool\")", "from_flink"));
    tableRows3.add(asList(789, "cool"));
  }

  @Order(340)
  @Test
  public void selectFromFlink3(@Flink FlinkHelper flink) {
    assertThat(flink.sql("SELECT * FROM %s", flink.qualifiedTableName("from_flink")))
        .hasSize(3)
        .map(r -> Arrays.asList(r.getField(0), r.getField(1)))
        .containsExactlyInAnyOrder(
            Arrays.asList(123, "foo"), Arrays.asList(456, "bar"), Arrays.asList(789, "cool"));
  }

  @Order(350)
  @Test
  public void selectFromSpark3(@Spark SparkSession spark) {
    assertThat(
            spark
                .sql(format("SELECT id, val FROM nessie.db.%s", "from_flink"))
                .collectAsList()
                .stream()
                .map(r -> asList(r.get(0), r.get(1))))
        .containsExactlyInAnyOrderElementsOf(tableRows3);
  }

  @Order(360)
  @Test
  public void selectFromDremio3(DremioHelper dremioHelper) {
    List<List<Object>> rows = dremioHelper.runSelectQuery("SELECT * FROM keith.db.from_flink");
    assertThat(tableRows3).containsAll(rows);
  }
}
