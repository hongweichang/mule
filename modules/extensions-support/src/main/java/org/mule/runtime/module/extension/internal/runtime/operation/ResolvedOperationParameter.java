/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.operation;

import org.mule.runtime.api.meta.model.parameter.ParameterModel;


public class ResolvedOperationParameter
{

    private final ParameterModel parameterModel;
    private String parameterName;
    private Object value;

    public ResolvedOperationParameter(String parameterName, Object value, ParameterModel parameterModel)
    {
        this.parameterName = parameterName;
        this.value = value;
        this.parameterModel = parameterModel;
    }

    public String getOperationParameter()
    {
        return parameterName;
    }

    public Object getValue()
    {
        return value;
    }

    public ParameterModel getParameterModel()
    {
        return parameterModel;
    }
}
