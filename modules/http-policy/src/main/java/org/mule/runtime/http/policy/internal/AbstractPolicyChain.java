/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.http.policy.internal;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.lifecycle.LifecycleUtils;
import org.mule.runtime.core.api.processor.MessageProcessorChain;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.execution.NextOperation;
import org.mule.runtime.core.policy.PolicyManager;
import org.mule.runtime.core.processor.chain.DefaultMessageProcessorChainBuilder;
import org.mule.runtime.dsl.api.component.ComponentIdentifier;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

public abstract class AbstractPolicyChain implements Initialisable {

  @Inject
  private MuleContext muleContext;
  @Inject
  private PolicyManager policyManager;

  private List<Processor> processors;
  private MessageProcessorChain processorChain;

  public void setProcessors(List<Processor> processors) {
    this.processors = processors;
  }

  @Override
  public final void initialise() throws InitialisationException {
    LifecycleUtils.initialiseIfNeeded(processors, muleContext);
    // TODO verify MPs are not created twice
    processorChain = new DefaultMessageProcessorChainBuilder().chain(this.processors).build();
    processorChain.setMuleContext(muleContext);
    processorChain.initialise();

  }

  public NextOperation nextOperation(String id, Consumer<Event> eventStackConsumer, NextOperation next) {
    for (Processor processor : processors) {
      if (processor instanceof PolicyNextActionMessageProcessor) {
            ((PolicyNextActionMessageProcessor) processor).setNext(id, eventStackConsumer, next);
      }
    }
    return (event) -> processorChain.process(event);
  }

  abstract ComponentIdentifier getTargetComponentIdentifier();

}
