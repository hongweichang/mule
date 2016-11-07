/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.support;

import org.mule.extensions.jms.api.connection.JmsSpecification;

import java.util.Optional;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * <code>JmsSupport</code> is an interface that provides a polymorphic facade to
 * the JMS 1.0.2b and 1.1 API specifications. this interface is not intended for
 * general purpose use and should only be used with the Mule JMS connector.
 */

public interface JmsSupport {

  Connection createConnection(ConnectionFactory connectionFactory) throws JMSException;

  Connection createConnection(ConnectionFactory connectionFactory, String username, String password)
      throws JMSException;

  Session createSession(Connection connection, boolean topic, boolean transacted, int ackMode, boolean noLocal)
      throws JMSException;

  MessageProducer createProducer(Session session, Destination destination, boolean topic)
      throws JMSException;

  MessageConsumer createConsumer(Session session, Destination destination, String messageSelector, boolean noLocal,
                                 Optional<String> durableName, boolean topic)
      throws JMSException;

  MessageConsumer createConsumer(Session session, Destination destination, boolean topic)
      throws JMSException;

  Destination createDestination(Session session, String name, boolean topic) throws JMSException;

  /**
   * Creates a destination from an address. The address may contain a topic:// prefix if it's a topic.
   * If not it will be resolved to a queue.
   */
  Destination createDestinationFromAddress(Session session, String address, boolean isTopic)
      throws JMSException;

  Destination createTemporaryDestination(Session session, boolean topic) throws JMSException;

  void send(MessageProducer producer, Message message, boolean persistentDelivery, boolean topic)
      throws JMSException;

  void send(MessageProducer producer, Message message, boolean persistent, int priority, long ttl, boolean topic)
      throws JMSException;

  void send(MessageProducer producer, Message message, boolean persistentDelivery, Destination dest, boolean topic)
      throws JMSException;

  void send(MessageProducer producer, Message message, Destination destination, boolean persistent, int priority,
            long ttl, boolean topic)
      throws JMSException;

  JmsSpecification getSpecification();

}
