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

import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import java.util.Objects;

public class IcebergDremioExtension implements ParameterResolver{

  private static String sonarBaseUrl(){
    return Objects.requireNonNull(
      System.getProperty("dremio.base-url"),
      "Base URL not set correctly: dremio.base-url");
  }

  private static String sonarPAT(){
    return Objects.requireNonNull(
      System.getProperty("dremio.PAT"),
      "PAT not set correctly: dremio.endPAT");
  }

  private static String sonarProjectId(){
    return Objects.requireNonNull(
      System.getProperty("dremio.project-id"),
      "Project Id not set correctly: dremio.project-id");
  }

//  private static String SonarQueryUrl(){
//    return String.format(sonarBaseUrl() + "/ui/projects/" + sonarProjectId() + "/sql");
//  }


  private boolean isBaseURL(ParameterContext paramCtx) {
    return paramCtx.getParameter().getName().equals("baseUrl")
      && paramCtx.getParameter().getType().equals(String.class);
  }

  private boolean isToken(ParameterContext paramCtx) {
    return paramCtx.getParameter().getName().equals("token")
      && paramCtx.getParameter().getType().equals(String.class);
  }

  private boolean isProjectId(ParameterContext paramCtx) {
    return paramCtx.getParameter().getName().equals("projectId")
      && paramCtx.getParameter().getType().equals(String.class);
  }

  @Override
  public boolean supportsParameter(ParameterContext paramCtx, ExtensionContext extensionCtx)
    throws ParameterResolutionException {
    return isBaseURL(paramCtx) || isToken(paramCtx) || isProjectId(paramCtx);
  }

  @Override
  public Object resolveParameter(ParameterContext paramCtx, ExtensionContext extensionCtx)
    throws ParameterResolutionException {
    if (isBaseURL(paramCtx)) {
      return sonarBaseUrl();
    }
    if (isToken(paramCtx)) {
      return sonarPAT();
    }
    if (isProjectId(paramCtx)) {
      return sonarProjectId();
    }
    throw new ParameterResolutionException(
      "Unsupported parameter " + paramCtx.getParameter() + " on " + paramCtx.getTarget());
  }
}
