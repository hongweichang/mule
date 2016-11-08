/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.model.property;

import org.mule.runtime.api.meta.model.ModelProperty;
import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.extension.api.runtime.operation.OperationExecutorFactory;
import org.mule.runtime.extension.api.runtime.operation.OperationParametersResolverFactory;

public final class OperationParametersResolverModelProperty implements ModelProperty {

  private final OperationParametersResolverFactory operationParametersResolverFactory;

  public OperationParametersResolverModelProperty(
          OperationParametersResolverFactory operationParametersResolverFactory) {
    this.operationParametersResolverFactory = operationParametersResolverFactory;
  }

  public OperationParametersResolverFactory getOperationParametersResolverFactory() {
    return operationParametersResolverFactory;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public boolean isExternalizable() {
    return false;
  }
}
