/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.runtime.core.api.Event;

import java.util.HashMap;
import java.util.Map;

public class ExtensionResolverSetVisitor
{

    private Map<String, Object> configurationValues = new HashMap<>();

    public ResolverSetVisitor createVisitor(String parameterName)
    {
        return new InnerResolverSetVisitor(parameterName);
    }

    public class InnerResolverSetVisitor implements ResolverSetVisitor {

        private String parameterName;
        private Object configurationValue;

        public InnerResolverSetVisitor(String parameterName)
        {
            this.parameterName = parameterName;
        }

        @Override
        public void setConfigurationValue(Object configurationValue)
        {
            configurationValues.put(parameterName, configurationValue);
        }
    }

}
