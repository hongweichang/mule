/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.operation;

import org.mule.runtime.api.message.MuleEvent;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.module.extension.internal.runtime.ExecutionContextAdapter;

/**
 * An implementation of {@link AbstractReturnDelegate} which sets the output message on a variable which key is taken from the
 * {@link #target} field.
 * <p>
 * The target variable will always contain a {@link Message}, even if the operation returned a simple value
 * <p>
 * The original message is left untouched.
 *
 * @since 4.0
 */
final class TargetReturnDelegate extends AbstractReturnDelegate {

  private final String target;

  /**
   * Creates a new instance
   *
   * @param target the name of the variable in which the output message will be set
   * @param muleContext the current {@link MuleContext}
   */
  TargetReturnDelegate(String target, MuleContext muleContext) {
    super(muleContext);
    this.target = target;
  }

  @Override
  public MuleEvent asReturnValue(Object value, ExecutionContextAdapter operationContext) {
    return org.mule.runtime.core.api.Event.builder(operationContext.getEvent())
        .addVariable(target, toMessage(value, operationContext)).build();
  }
}
