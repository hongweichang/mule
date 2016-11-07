/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.operation;

import static org.mule.extensions.jms.internal.function.JmsSupplier.fromJmsSupplier;
import org.mule.extensions.jms.JmsExtension;
import org.mule.extensions.jms.api.config.AckMode;
import org.mule.extensions.jms.api.connection.JmsConnection;
import org.mule.extensions.jms.api.connection.JmsSpecification;
import org.mule.extensions.jms.api.destination.DestinationType;
import org.mule.extensions.jms.api.destination.QueueDestination;
import org.mule.extensions.jms.api.message.JmsAttributes;
import org.mule.extensions.jms.internal.message.JmsMuleMessageFactory;
import org.mule.extensions.jms.internal.metadata.JmsOutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.IOException;
import java.util.function.Supplier;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;


public class JmsConsume {

  private JmsMuleMessageFactory messageFactory = new JmsMuleMessageFactory();

  /*
      <jms:consume
      destination=?myQueue?
      config-ref=?jmsConfig? selector=?expression?
      target=?vars[?myVar?]/>
   */

  @OutputResolver(output = JmsOutputResolver.class)
  public Result<Object, JmsAttributes> consume(@Connection JmsConnection connection,
                                               @UseConfig JmsExtension config,
                                               String destination,
                                               //TODO change to use default values using defautlValues Optional attribute
                                               @Optional DestinationType destinationType,
                                               @Optional AckMode ackMode,
                                               @Optional String selector,
                                               @Optional Boolean noLocal,
                                               @Optional String contentType,
                                               @Optional String encoding,
                                               @Optional(defaultValue = "10000") Long maximumWaitTime) {
    try {
      Session session = null;
      MessageConsumer consumer = null;
      try {
        session = connection.createSession(resolveAckMode(config.getAckMode(), ackMode));
        destinationType = resolveDestinationType(destinationType);
        Destination jmsDestination =
            connection.getJmsSupport().createDestination(session, destination, destinationType.isTopic());


        consumer = connection.getJmsSupport().createConsumer(session, jmsDestination, selector,
                                                             resolveOverride(config.isNoLocal(), noLocal),
                                                             resolveDurableName(destinationType),
                                                             destinationType.isTopic());

        Message receive = resolveConsumeMessage(maximumWaitTime, consumer).get();

        return convertJmsMessageToResult(receive, connection.getJmsSupport().getSpecification(),
                                         resolveOverride(config.getContentType(), contentType),
                                         resolveOverride(config.getEncoding(), encoding));

      } finally {
        connection.closeQuietly(consumer);
        connection.closeQuietly(session);
      }
    } catch (Exception e) {
      //TODO throw proper exception
      throw new RuntimeException(e);
    }
  }

  private java.util.Optional<String> resolveDurableName(DestinationType destinationType) {
    return java.util.Optional.ofNullable(destinationType.isTopic() ? destinationType.getDurableSubscriptionName() : null);
  }

  private AckMode resolveAckMode(AckMode configAckMode, AckMode operationAckMode) {
    return resolveOverride(configAckMode, operationAckMode);
  }

  private <T> T resolveOverride(T configValue, T operationValue) {
    return operationValue == null ? configValue : operationValue;
  }

  private Supplier<Message> resolveConsumeMessage(Long maximumWaitTime, MessageConsumer consumer) {
    if (maximumWaitTime == -1) {
      return fromJmsSupplier(consumer::receive);
    } else if (maximumWaitTime == 0) {
      return fromJmsSupplier(consumer::receiveNoWait);
    } else {
      return fromJmsSupplier(() -> consumer.receive(maximumWaitTime));
    }
  }

  private DestinationType resolveDestinationType(DestinationType destinationType) {
    return destinationType != null ? destinationType : new QueueDestination();
  }

  private Result<Object, JmsAttributes> convertJmsMessageToResult(Message message, JmsSpecification specification,
                                                                  String contentType, String encoding)
      throws IOException, JMSException {
    return messageFactory.createMessage(message, specification, contentType, encoding);
  }

}
