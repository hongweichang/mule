/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
///*
// * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
// * The software in this package is published under the terms of the CPAL v1.0
// * license, a copy of which has been included with this distribution in the
// * LICENSE.txt file.
// */
//package org.mule.extensions.jms.api.source;
//
//import org.mule.extensions.jms.api.config.AckMode;
//import org.mule.extensions.jms.api.connection.JmsConnection;
//import org.mule.extensions.jms.api.destination.DestinationType;
//import org.mule.extensions.jms.api.destination.QueueDestination;
//import org.mule.extensions.jms.api.message.JmsAttributes;
//import org.mule.extensions.jms.internal.message.JmsMuleMessageFactory;
//import org.mule.runtime.api.exception.MuleException;
//import org.mule.runtime.core.api.MuleContext;
//import org.mule.runtime.extension.api.annotation.execution.OnError;
//import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
//import org.mule.runtime.extension.api.annotation.param.Connection;
//import org.mule.runtime.extension.api.annotation.param.NullSafe;
//import org.mule.runtime.extension.api.annotation.param.Optional;
//import org.mule.runtime.extension.api.annotation.param.Parameter;
//import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
//import org.mule.runtime.extension.api.annotation.param.display.Placement;
//import org.mule.runtime.extension.api.runtime.source.Source;
//import org.mule.runtime.extension.api.runtime.source.SourceCallback;
//import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
//
//import com.sun.deploy.net.HttpResponse;
//
//import javax.inject.Inject;
//import javax.jms.Destination;
//import javax.jms.JMSException;
//import javax.jms.MessageConsumer;
//import javax.jms.Session;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * //TODO
// */
//public class JmsSubscriber extends Source<Object, JmsAttributes> {
//
//  private static final Logger logger = LoggerFactory.getLogger(JmsSubscriber.class);
//
//  @Parameter
//  @Optional(defaultValue = "AUTO")
//  private AckMode ackMode;
//
//  @Parameter
//  @Optional
//  private Integer maxRedelivery;
//
//  @Parameter
//  @Optional(defaultValue = "false")
//  private boolean noLocal;
//
//  @Parameter
//  @Optional
//  private String selector;
//
//  @Parameter
//  private String destination;
//
//  @Parameter
//  @Optional
//  private DestinationType destinationType = new QueueDestination();
//
//  @Connection
//  private JmsConnection connection;
//
//  @Inject
//  private MuleContext muleContext;
//
//  private JmsMuleMessageFactory jmsMuleMessageFactory = new JmsMuleMessageFactory();
//  private MessageConsumer consumer;
//  private Session session;
//
//
//  private JmsConnection.AckMode convertAckMode(AckMode ackMode)
//  {
//    if (ackMode.equals(AckMode.MANUAL))
//    {
//      return JmsConnection.AckMode.CLIENT;
//    }
//    else if (ackMode.equals(AckMode.AUTO))
//    {
//      return JmsConnection.AckMode.AUTO;
//    }
//    else if (ackMode.equals(AckMode.DUPS_OK))
//    {
//      return JmsConnection.AckMode.DUPS_OK;
//    }
//    else
//    {
//      throw new RuntimeException();
//    }
//  }
//
//  @Override
//  public void onStart(SourceCallback<Object, JmsAttributes> sourceCallback) throws MuleException {
//
//    try
//    {
//      session = connection.createSession(convertAckMode(ackMode));
//      Destination jmsDestination = connection.getJmsSupport().createDestination(session, destination, destinationType.isTopic());
//      java.util.Optional<String> durableName = java.util.Optional.ofNullable(destinationType.isTopic() && destinationType.useDurableTopicSubscription() ? destinationType.getDurableSubscriptionName() : null);
//      consumer = connection.getJmsSupport().createConsumer(session, jmsDestination, selector, noLocal, durableName, destinationType.isTopic());
//      consumer.setMessageListener(message -> {
//        if (ackMode.equals(AckMode.NONE))
//        {
//          try
//          {
//            message.acknowledge();
//          }
//          catch (JMSException e)
//          {
//            throw new RuntimeException(e);
//          }
//        }
//        try
//        {
//          sourceContext.getMessageHandler().handle(jmsMuleMessageFactory.createMessage(message, connection.getJmsSupport().getSpecification(), muleContext), new CompletionHandler<MuleMessage<Object,JmsAttributes>, Exception>()
//          {
//            @Override
//            public void onCompletion(MuleMessage muleMessage)
//            {
//              if (ackMode.equals(AckMode.AUTO))
//              {
//                try
//                {
//                  message.acknowledge();
//                }
//                catch (JMSException e)
//                {
//                  throw new RuntimeException(e);
//                }
//              }
//            }
//
//            @Override
//            public void onFailure(Exception exception)
//            {
//              //Nothing to do.
//            }
//          });
//        }
//        catch(Exception e)
//        {
//          sourceContext.getExceptionCallback().onException(e);
//        }
//      });
//    }
//    catch (Exception e)
//    {
//      sourceContext.getExceptionCallback().onException(e);
//    }
//
//    //try
//    //{
//    //  session = connection.createSession(convertAckMode(ackMode));
//    //  Destination jmsDestination = connection.getJmsSupport().createDestination(session, destination, destinationType.isTopic());
//    //  java.util.Optional<String> durableName = java.util.Optional.ofNullable(destinationType.isTopic() && destinationType.useDurableTopicSubscription() ? destinationType.getDurableSubscriptionName() : null);
//    //  consumer = connection.getJmsSupport().createConsumer(session, jmsDestination, selector, noLocal, durableName, destinationType.isTopic());
//    //  consumer.setMessageListener(message -> {
//    //    if (ackMode.equals(AckMode.NONE))
//    //    {
//    //      try
//    //      {
//    //        message.acknowledge();
//    //      }
//    //      catch (JMSException e)
//    //      {
//    //        throw new RuntimeException(e);
//    //      }
//    //    }
//    //    try
//    //    {
//    //      sourceContext.getMessageHandler().handle(jmsMuleMessageFactory.createMessage(message, connection.getJmsSupport().getSpecification(), muleContext), new CompletionHandler<MuleMessage<Object,JmsAttributes>, Exception>()
//    //      {
//    //        @Override
//    //        public void onCompletion(MuleMessage muleMessage)
//    //        {
//    //          if (ackMode.equals(AckMode.AUTO))
//    //          {
//    //            try
//    //            {
//    //              message.acknowledge();
//    //            }
//    //            catch (JMSException e)
//    //            {
//    //              throw new RuntimeException(e);
//    //            }
//    //          }
//    //        }
//    //
//    //        @Override
//    //        public void onFailure(Exception exception)
//    //        {
//    //          //Nothing to do.
//    //        }
//    //      });
//    //    }
//    //    catch(Exception e)
//    //    {
//    //      sourceContext.getExceptionCallback().onException(e);
//    //    }
//    //  });
//    //}
//    //catch (Exception e)
//    //{
//    //  connection.closeQuietly(consumer);
//    //  connection.closeQuietly(session);
//    //  sourceContext.getExceptionCallback().onException(e);
//    //}
//  }
//
//
//  @OnSuccess
//  public void onSuccess(@Optional @DisplayName(RESPONSE_SETTINGS) @Placement(
//    group = RESPONSE_SETTINGS) @NullSafe HttpListenerSuccessResponseBuilder responseBuilder,
//                        SourceCallbackContext callbackContext)
//    throws Exception {
//
//    HttpResponseContext context = callbackContext.getVariable(RESPONSE_CONTEXT);
//    HttpResponse httpResponse = buildResponse(responseBuilder, context.isSupportStreaming());
//    final HttpResponseReadyCallback responseCallback = context.getResponseCallback();
//    responseCallback.responseReady(httpResponse, getResponseFailureCallback(responseCallback));
//  }
//
//  @OnError
//  public void onError(SourceCallbackContext callbackContext, Error error) {
//
//      //connection.closeQuietly(consumer);
//      //connection.closeQuietly(session);
//      logger.error(error.getLocalizedMessage());
//  }
//
//  @Override
//  public void onStop() {
//    connection.closeQuietly(consumer);
//    connection.closeQuietly(session);
//  }
//}
