/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.http.policy.internal;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.execution.NextOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PolicyNextActionMessageProcessor implements Processor {

  private Map<String, NextOperation> nextOperationMap = new HashMap<>();
  private Map<String, Consumer<Event>> eventStackConsumerMap = new HashMap<>();

  @Override
  public Event process(Event event) throws MuleException {
    eventStackConsumerMap.get(event.getContext().getId()).accept(event);
    NextOperation nextOperation = nextOperationMap.get(event.getContext().getId());
    if (nextOperation == null) {
      throw new MuleRuntimeException(createStaticMessage("There's not next operation configured for event context id " + event.getContext().getId()));
    }
    try
    {
      return nextOperation.execute(event);
    }
    catch (Exception e)
    {
      throw new DefaultMuleException(e);
    }
  }

  public void setNext(String id, Consumer<Event> eventStackConsumer, NextOperation next)
  {
    nextOperationMap.put(id, next);
    eventStackConsumerMap.put(id, eventStackConsumer);
  }
}
