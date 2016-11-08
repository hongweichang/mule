/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.executor;

import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.api.meta.model.parameter.ParameterModel;
import org.mule.runtime.extension.api.runtime.operation.OperationParameter;
import org.mule.runtime.extension.api.runtime.operation.OperationParametersResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReflectiveOperationParametersResolver implements OperationParametersResolver {

  private final OperationModel operationModel;

  public ReflectiveOperationParametersResolver(OperationModel operationModel) {
    this.operationModel = operationModel;
  }

  @Override
  public List<OperationParameter> resolveParameters(Map<String, Object> parameters) {
    ArrayList<OperationParameter> operationParameters = new ArrayList<>();
    parameters.forEach((name, configValue) -> {
      ParameterModel foundParameterModel = operationModel.getParameterModels().stream()
          .filter(parameterModel -> parameterModel.getName().equals(name))
          .findAny()
          .orElseThrow(() -> new IllegalStateException("No parameter model for configuration attribute with name: " + name));
      // TODO resolve null from the method
      operationParameters
          .add(new OperationParameter(null, configValue != null ? configValue : foundParameterModel.getDefaultValue(), name));
    });
    return operationParameters;
  }
}
