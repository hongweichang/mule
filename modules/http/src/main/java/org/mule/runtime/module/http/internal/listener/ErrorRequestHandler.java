/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.http.internal.listener;

import org.mule.extension.http.internal.listener.HttpResponseContext;
import org.mule.extension.http.internal.listener.HttpResponseFactory;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.context.WorkManager;
import org.mule.runtime.core.api.source.MessageSource;
import org.mule.runtime.core.api.transaction.TransactionConfig;
import org.mule.runtime.core.execution.MessageProcessContext;
import org.mule.runtime.core.execution.MessageProcessingManager;
import org.mule.runtime.module.http.internal.domain.HttpProtocol;
import org.mule.runtime.module.http.internal.domain.InputStreamHttpEntity;
import org.mule.runtime.module.http.internal.domain.request.HttpRequestContext;
import org.mule.runtime.module.http.internal.listener.async.HttpResponseReadyCallback;
import org.mule.runtime.module.http.internal.listener.async.RequestHandler;
import org.mule.runtime.module.http.internal.listener.async.ResponseStatusCallback;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorRequestHandler implements RequestHandler {

  private final Charset encoding;
  private final MessageProcessingManager messageProcessingManager;
  private final HttpResponseFactory httpResponseFactory;
  private Logger logger = LoggerFactory.getLogger(getClass());

  private int statusCode;
  private String reasonPhrase;
  private String entityFormat;

  public ErrorRequestHandler(Charset encoding, int statusCode, String reasonPhrase, String entityFormat, MessageProcessingManager messageProcessingManager, HttpResponseFactory httpResponseFactory)
  {
    this.encoding = encoding;
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
    this.entityFormat = entityFormat;
    this.messageProcessingManager = messageProcessingManager;
    this.httpResponseFactory = httpResponseFactory;
  }

  private boolean supportsTransferEncoding(String httpVersion) {
    return !(HttpProtocol.HTTP_0_9.asString().equals(httpVersion) || HttpProtocol.HTTP_1_0.asString().equals(httpVersion));
  }

  @Override
  public void handleRequest(HttpRequestContext requestContext, HttpResponseReadyCallback responseCallback) {
    String resolvedEntity = String.format(entityFormat, requestContext.getRequest().getUri());
    try
    {
      //TODO the HttpResponseContext creation code is duplicated in HttpListener
      HttpResponseContext responseContext = new HttpResponseContext();
      final String httpVersion = requestContext.getRequest().getProtocol().asString();
      responseContext.setHttpVersion(httpVersion);
      responseContext.setSupportStreaming(supportsTransferEncoding(httpVersion));
      responseContext.setResponseCallback(responseCallback);
      this.messageProcessingManager.processMessage(new ExtensionHttpMessageProcessTemplate(createMessage(requestContext), null, responseContext, httpResponseFactory), new MessageProcessContext()
      {
        @Override
        public boolean supportsAsynchronousProcessing()
        {
          return false;
        }

        @Override
        public MessageSource getMessageSource()
        {
          return null;
        }

        @Override
        public FlowConstruct getFlowConstruct()
        {
          return null;
        }

        @Override
        public WorkManager getFlowExecutionWorkManager()
        {
          return null;
        }

        @Override
        public TransactionConfig getTransactionConfig()
        {
          return null;
        }

        @Override
        public ClassLoader getExecutionClassLoader()
        {
          return null;
        }
      });
    }
    catch (HttpRequestParsingException e)
    {
      //TODO define what to do with this exception since
      throw new MuleRuntimeException(e);
    }
    responseCallback.responseReady(new org.mule.runtime.module.http.internal.domain.response.HttpResponseBuilder()
        .setStatusCode(statusCode).setReasonPhrase(reasonPhrase)
        .setEntity(new InputStreamHttpEntity(new ByteArrayInputStream(resolvedEntity.getBytes()))).build(),
                                   new ResponseStatusCallback() {

                                     @Override
                                     public void responseSendFailure(Throwable exception) {
                                       logger.warn(String.format("Error while sending %s response %s", statusCode,
                                                                 exception.getMessage()));
                                       if (logger.isDebugEnabled()) {
                                         logger.debug("exception thrown", exception);
                                       }
                                     }

                                     @Override
                                     public void responseSendSuccessfully() {}
                                   });
  }

  @Override
  public Message createMessage(HttpRequestContext requestContext) throws HttpRequestParsingException
  {
    return HttpRequestToMuleEvent.transform(requestContext, encoding, false,
                                            new ListenerPath(requestContext.getRequest().getPath(), ""));
  }
}
