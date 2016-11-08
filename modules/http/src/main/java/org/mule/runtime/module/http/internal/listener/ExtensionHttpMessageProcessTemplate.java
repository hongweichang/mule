/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.http.internal.listener;

import org.mule.extension.http.api.listener.builder.HttpListenerSuccessResponseBuilder;
import org.mule.extension.http.internal.listener.HttpListenerResponseSender;
import org.mule.extension.http.internal.listener.HttpResponseContext;
import org.mule.extension.http.internal.listener.HttpResponseFactory;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.policy.PolicyOperationParametersTransformer;
import org.mule.runtime.core.exception.MessagingException;
import org.mule.runtime.core.execution.ExtensionFlowProcessingPhaseTemplate;
import org.mule.runtime.core.execution.ResponseCompletionCallback;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ExtensionHttpMessageProcessTemplate implements ExtensionFlowProcessingPhaseTemplate
{

    private final HttpResponseContext httpResponseContext;
    private Message message;
    private Supplier<Message> createResponseMessageFunction;
    private org.mule.extension.http.internal.listener.HttpListenerResponseSender httpListenerResponseSender;
    private PolicyOperationParametersTransformer eventToHttpResponseParameters;

    public ExtensionHttpMessageProcessTemplate(final Message message, Supplier<Message> createResponseMessageFunction, HttpResponseContext httpResponseContext, HttpResponseFactory httpResponseFactory) {
        this.httpListenerResponseSender = new HttpListenerResponseSender(httpResponseFactory);
        this.message = message;
        this.createResponseMessageFunction = createResponseMessageFunction;
        this.httpResponseContext = httpResponseContext;
    }

    @Override
    public Message getMessage() throws MuleException
    {
        return message;
    }

    @Override
    public Event routeEvent(Event event) throws MuleException
    {
        return Event.builder(event).message((InternalMessage) createResponseMessageFunction.get()).build();
    }

    @Override
    public void sendResponseToClient(Event flowExecutionResponse, Map<String, Object> parameters, ResponseCompletionCallback responseCompletionCallback) throws MuleException
    {
        Object responseBuilder = parameters.get("responseBuilder");
        try
        {
            httpListenerResponseSender.sendResponse(httpResponseContext, (HttpListenerSuccessResponseBuilder) responseBuilder);
        }
        catch (Exception e)
        {
            throw new DefaultMuleException(e);
        }
    }

    @Override
    public void sendFailureResponseToClient(MessagingException exception, ResponseCompletionCallback responseCompletionCallback) throws MuleException
    {

    }

    @Override
    public Function<Event, Map<String, Object>> getSuccessfulExecutionResponseParametersFunction()
    {
        return null;
    }

    @Override
    public Function<Event, Map<String, Object>> getFailedExecutionResponseParametersFunction()
    {
        return null;
    }

    @Override
    public Optional<Object> getMessagePolicyDescriptor()
    {
        return null;
    }
}
