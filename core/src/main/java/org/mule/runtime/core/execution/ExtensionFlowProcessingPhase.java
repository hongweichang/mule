/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.execution;

import static org.mule.runtime.core.DefaultEventContext.create;
import static org.mule.runtime.core.context.notification.ConnectorMessageNotification.MESSAGE_ERROR_RESPONSE;
import static org.mule.runtime.core.context.notification.ConnectorMessageNotification.MESSAGE_RECEIVED;
import static org.mule.runtime.core.context.notification.ConnectorMessageNotification.MESSAGE_RESPONSE;
import static org.mule.runtime.core.execution.TransactionalErrorHandlingExecutionTemplate.createMainExecutionTemplate;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.DefaultEventContext;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.exception.MessagingExceptionHandler;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.policy.PolicyOperationParametersTransformer;
import org.mule.runtime.core.api.source.MessageSource;
import org.mule.runtime.core.exception.MessagingException;
import org.mule.runtime.core.policy.OperationPolicyInstance;
import org.mule.runtime.core.policy.Policy;
import org.mule.runtime.core.policy.PolicyManager;
import org.mule.runtime.core.transaction.MuleTransactionConfig;
import org.mule.runtime.dsl.api.component.ComponentIdentifier;

import java.util.Map;
import java.util.Optional;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This phase routes the message through the flow.
 * <p>
 * To participate of this phase, {@link MessageProcessTemplate} must implement
 * {@link FlowProcessingPhaseTemplate}
 */
public class ExtensionFlowProcessingPhase
    extends NotificationFiringProcessingPhase<ExtensionFlowProcessingPhaseTemplate> {

  private final PolicyManager policyManager;
  protected transient Logger logger = LoggerFactory.getLogger(getClass());

  public ExtensionFlowProcessingPhase(PolicyManager policyManager)
  {
    this.policyManager = policyManager;
  }

  @Override
  public boolean supportsTemplate(MessageProcessTemplate messageProcessTemplate) {
    return messageProcessTemplate instanceof ExtensionFlowProcessingPhaseTemplate;
  }

  @Override
  public void runPhase(final ExtensionFlowProcessingPhaseTemplate template, final MessageProcessContext messageProcessContext,
                       final PhaseResultNotifier phaseResultNotifier) {
    Work flowExecutionWork = new Work() {

      @Override
      public void release() {

      }

      @Override
      public void run() {
        try {
          MessageSource messageSource = messageProcessContext.getMessageSource();
          try {
            final MessagingExceptionHandler exceptionHandler = messageProcessContext.getFlowConstruct().getExceptionListener();
            final Event templateEvent = Event.builder(create(messageProcessContext.getFlowConstruct(), "WTF????")).message((InternalMessage) template.getMessage()).build();

            NextOperation nextOperation = (muleEvent) -> {
              TransactionalErrorHandlingExecutionTemplate transactionTemplate =
                      createMainExecutionTemplate(messageProcessContext.getFlowConstruct().getMuleContext(),
                                                  messageProcessContext.getFlowConstruct(),
                                                  (messageProcessContext.getTransactionConfig() == null ? new MuleTransactionConfig()
                                                                                                        : messageProcessContext.getTransactionConfig()),
                                                  exceptionHandler);
              final Event response = transactionTemplate.execute(() -> {

                fireNotification(messageSource, muleEvent, messageProcessContext.getFlowConstruct(), MESSAGE_RECEIVED);
                return template.routeEvent(muleEvent);
              });
              return response;
            };

            Event flowExecutionResponse;
            ComponentIdentifier sourceIdentifier = new ComponentIdentifier.Builder().withName("listener").withNamespace("httpn").build();
            Optional<Policy> policy = policyManager.lookupPolicy(sourceIdentifier, template.getMessagePolicyDescriptor());
            if (policy.isPresent()) {
              OperationPolicyInstance policyInstance = policy.get().createSourcePolicyInstance(templateEvent.getContext().getId(), sourceIdentifier);
              nextOperation = buildFlowExecutionWithPolicyFunction(nextOperation, templateEvent, template, sourceIdentifier);
              flowExecutionResponse = policyInstance.process(templateEvent, nextOperation);
            } else {
              flowExecutionResponse = nextOperation.execute(templateEvent);
            }
            fireNotification(messageSource, flowExecutionResponse, messageProcessContext.getFlowConstruct(), MESSAGE_RESPONSE);
            template.sendResponseToClient(flowExecutionResponse, policyManager.lookupOperationParametersTransformer(sourceIdentifier).get().fromMessageToParameters(flowExecutionResponse.getMessage()), createResponseCompletationCallback(phaseResultNotifier, exceptionHandler));
          } catch (final MessagingException e) {
            fireNotification(messageSource, e.getEvent(), messageProcessContext.getFlowConstruct(), MESSAGE_ERROR_RESPONSE);
            template.sendFailureResponseToClient(e, createSendFailureResponseCompletationCallback(phaseResultNotifier));
          }
        } catch (Exception e) {
          phaseResultNotifier.phaseFailure(e);
        }
      }
    };

    if (messageProcessContext.supportsAsynchronousProcessing()) {
      try {
        messageProcessContext.getFlowExecutionWorkManager().scheduleWork(flowExecutionWork);
      } catch (WorkException e) {
        phaseResultNotifier.phaseFailure(e);
      }
    } else {
      flowExecutionWork.run();
    }
  }

  private NextOperation buildFlowExecutionWithPolicyFunction(NextOperation nextOperation, Event sourceEvent, ExtensionFlowProcessingPhaseTemplate template, ComponentIdentifier sourceIdentifier)
  {
    return (processEvent) -> {
      Event flowExecutionResponse = nextOperation.execute(sourceEvent);
      Optional<PolicyOperationParametersTransformer> policyOperationParametersTransformer = policyManager.lookupOperationParametersTransformer(sourceIdentifier);
      if (policyOperationParametersTransformer.isPresent()) {
        Map<String, Object> responseParameters = template.getSuccessfulExecutionResponseParametersFunction().apply(flowExecutionResponse);
        return Event.builder(processEvent.getContext()).message((InternalMessage) policyOperationParametersTransformer.get().fromParametersToMessage(responseParameters)).build();
      } else {
        return Event.builder(flowExecutionResponse).build();
      }
    };
  }

  private ResponseCompletionCallback createSendFailureResponseCompletationCallback(final PhaseResultNotifier phaseResultNotifier) {
    return new ResponseCompletionCallback() {

      @Override
      public void responseSentSuccessfully() {
        phaseResultNotifier.phaseSuccessfully();
      }

      @Override
      public Event responseSentWithFailure(MessagingException e, Event event) {
        phaseResultNotifier.phaseFailure(e);
        return event;
      }
    };
  }

  private ResponseCompletionCallback createResponseCompletationCallback(final PhaseResultNotifier phaseResultNotifier,
                                                                        final MessagingExceptionHandler exceptionListener) {
    return new ResponseCompletionCallback() {

      @Override
      public void responseSentSuccessfully() {
        phaseResultNotifier.phaseSuccessfully();
      }

      @Override
      public Event responseSentWithFailure(final MessagingException e, final Event event) {
        return executeCallback((processEvent) -> {
          Event handleException = exceptionListener.handleException(e, processEvent);
          phaseResultNotifier.phaseSuccessfully();
          return handleException;
        }, phaseResultNotifier);
      }
    };
  }

  private Event executeCallback(final NextOperation callback, PhaseResultNotifier phaseResultNotifier) {
    try {
      return callback.execute(null);
    } catch (Exception callbackException) {
      phaseResultNotifier.phaseFailure(callbackException);
      throw new MuleRuntimeException(callbackException);
    }
  }

  @Override
  public int compareTo(MessageProcessPhase messageProcessPhase) {
    if (messageProcessPhase instanceof ValidationPhase) {
      return 1;
    }
    return 0;
  }

}
