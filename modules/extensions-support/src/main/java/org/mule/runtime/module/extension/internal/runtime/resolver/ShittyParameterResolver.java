/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;

public class ShittyParameterResolver implements ParameterResolver {

  private final Event event;
  private MuleContext muleContext;

  public ShittyParameterResolver(MuleContext muleContext, Event event) {
    this.muleContext = muleContext;
    this.event = event;
  }

  @Override
  public Object resolve(String parameterName, String configurationValue, Class expectedType) {
    return new TypeSafeExpressionValueResolver<>(configurationValue, expectedType, muleContext);
  }
}
