/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.connection;

import static org.mule.extensions.jms.api.config.AckMode.TRANSACTED;
import org.mule.extensions.jms.api.config.AckMode;
import org.mule.extensions.jms.internal.support.JmsSupport;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //TODO
 */
public class JmsConnection {

  private static final Logger logger = LoggerFactory.getLogger(JmsConnection.class);


  //public enum AckMode {
  //  AUTO(Session.AUTO_ACKNOWLEDGE),
  //  CLIENT(Session.CLIENT_ACKNOWLEDGE),
  //  DUPS_OK(Session.DUPS_OK_ACKNOWLEDGE),
  //  TRANSACTED(Session.SESSION_TRANSACTED);
  //  private final int jmsAckMode;
  //
  //  AckMode(int jmsAckMode) {
  //    this.jmsAckMode = jmsAckMode;
  //  }
  //
  //  public int getJmsAckMode() {
  //    return jmsAckMode;
  //  }
  //}

  private JmsSupport jmsSupport;
  private Connection connection;

  public JmsConnection(JmsSupport jmsSupport, Connection connection) {
    this.jmsSupport = jmsSupport;
    this.connection = connection;
  }

  public JmsSupport getJmsSupport() {
    return jmsSupport;
  }

  public Session createSession(AckMode ackMode) throws JMSException {
    return this.connection.createSession(ackMode.equals(TRANSACTED), ackMode.getAckMode());
  }

  /**
   * Closes the MessageConsumer
   *
   * @param consumer
   * @throws JMSException
   */
  public void close(MessageConsumer consumer) throws JMSException {
    if (consumer != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Closing consumer: " + consumer);
      }
      consumer.close();
    } else if (logger.isDebugEnabled()) {
      logger.debug("Consumer is null, nothing to close");
    }
  }

  /**
   * Closes the MessageConsumer without throwing an exception (an error message is
   * logged instead).
   *
   * @param consumer
   */
  public void closeQuietly(MessageConsumer consumer) {
    try {
      close(consumer);
    } catch (Exception e) {
      logger.warn("Failed to close jms message consumer: " + e.getMessage());
    }
  }

  /**
   * Closes the MuleSession
   *
   * @param session
   * @throws JMSException
   */
  public void close(Session session) throws JMSException {
    if (session != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Closing session " + session);
      }
      session.close();
    }
  }

  /**
   * Closes the MuleSession without throwing an exception (an error message is logged
   * instead).
   *
   * @param session
   */
  public void closeQuietly(Session session) {
    if (session != null) {
      try {
        close(session);
      } catch (Exception e) {
        logger.warn("Failed to close jms session consumer: " + e.getMessage());
      } finally {
        session = null;
      }
    }
  }

  /**
   * Closes the MessageProducer
   *
   * @param producer
   * @throws JMSException
   */
  public void close(MessageProducer producer) throws JMSException {
    if (producer != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Closing producer: " + producer);
      }
      producer.close();
    } else if (logger.isDebugEnabled()) {
      logger.debug("Producer is null, nothing to close");
    }
  }

  /**
   * Closes the MessageProducer without throwing an exception (an error message is
   * logged instead).
   *
   * @param producer
   */
  public void closeQuietly(MessageProducer producer) {
    try {
      close(producer);
    } catch (Exception e) {
      logger.warn("Failed to close jms message producer: " + e.getMessage());
    }
  }


}
