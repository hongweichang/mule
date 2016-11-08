/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.executor;

import org.mule.runtime.api.meta.model.operation.OperationModel;
import org.mule.runtime.extension.api.runtime.operation.OperationParametersResolver;
import org.mule.runtime.extension.api.runtime.operation.OperationParametersResolverFactory;

import java.lang.reflect.Method;

public class ReflectiveOperationParametersResolverFactory implements OperationParametersResolverFactory
{

    private final Method operationMethod;
    private final Class<?> operationImplementation;

    public ReflectiveOperationParametersResolverFactory(Class<?> operationImplementation, Method operationMethod)
    {
        this.operationImplementation = operationImplementation;
        this.operationMethod = operationMethod;
    }

    @Override
    public OperationParametersResolver createResolver(OperationModel operationModel)
    {
        return new ReflectiveOperationParametersResolver(operationModel);
    }
}
