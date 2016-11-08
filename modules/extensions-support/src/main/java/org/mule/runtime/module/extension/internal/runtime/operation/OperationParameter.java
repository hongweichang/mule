/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.operation;

public class OperationParameter
{

    private String name;
    private String configurationValue;
    private Class type;

    public OperationParameter(String name, String configurationValue, Class type)
    {
        this.name = name;
        this.configurationValue = configurationValue;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public String getConfigurationValue()
    {
        return configurationValue;
    }

    public Class getType()
    {
        return type;
    }
}
