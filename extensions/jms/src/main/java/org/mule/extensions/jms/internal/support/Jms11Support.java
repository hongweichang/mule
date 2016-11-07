/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.support;

import static org.mule.extensions.jms.api.connection.LookupJndiDestination.ALWAYS;
import static org.mule.extensions.jms.api.connection.LookupJndiDestination.TRY_ALWAYS;
import org.mule.extensions.jms.api.connection.JmsSpecification;
import org.mule.extensions.jms.api.connection.LookupJndiDestination;

import com.google.common.base.Preconditions;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.function.Function;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>Jms11Support</code> is a template class to provide an abstraction to to
 * the JMS 1.1 API specification.
 */

public class Jms11Support implements JmsSupport {

  private Logger logger = LoggerFactory.getLogger(Jms11Support.class);
  private final static String PROTOCOL = "jms";

  private final Function<String, Destination> jndiObjectSupplier;
  private final LookupJndiDestination lookupJndiDestination;

  public Jms11Support() {
    this(LookupJndiDestination.NEVER, (name) -> null);
  }

  public Jms11Support(LookupJndiDestination lookupJndiDestination, Function<String, Destination> jndiObjectSupplier) {
    Preconditions.checkNotNull(lookupJndiDestination);
    Preconditions.checkNotNull(jndiObjectSupplier);
    this.lookupJndiDestination = lookupJndiDestination;
    this.jndiObjectSupplier = jndiObjectSupplier;
  }

  @Override
  public Connection createConnection(ConnectionFactory connectionFactory, String username, String password)
      throws JMSException {
    if (connectionFactory == null) {
      throw new IllegalArgumentException("connectionFactory cannot be null");
    }
    return connectionFactory.createConnection(username, password);
  }

  @Override
  public Connection createConnection(ConnectionFactory connectionFactory) throws JMSException {
    if (connectionFactory == null) {
      throw new IllegalArgumentException("connectionFactory cannot be null");
    }
    return connectionFactory.createConnection();
  }

  @Override
  public Session createSession(Connection connection,
                               boolean topic,
                               boolean transacted,
                               int ackMode,
                               boolean noLocal)
      throws JMSException {
    return connection.createSession(transacted, (transacted ? Session.SESSION_TRANSACTED : ackMode));
  }

  @Override
  public MessageProducer createProducer(Session session, Destination destination, boolean topic)
      throws JMSException {
    return session.createProducer(destination);
  }

  @Override
  public MessageConsumer createConsumer(Session session, Destination destination, boolean topic)
      throws JMSException {
    return createConsumer(session, destination, null, false, null, topic);
  }

  @Override
  public MessageConsumer createConsumer(Session session,
                                        Destination destination,
                                        String messageSelector,
                                        boolean noLocal,
                                        Optional<String> durableName,
                                        boolean topic)
      throws JMSException {
    if (!durableName.isPresent()) {
      if (topic) {
        return session.createConsumer(destination, messageSelector, noLocal);
      } else {
        return session.createConsumer(destination, messageSelector);
      }
    } else {
      if (topic) {
        return session.createDurableSubscriber((Topic) destination, durableName.get(), messageSelector,
                                               noLocal);
      } else {
        throw new JMSException(
                               "A durable subscriber name was set but the destination was not a Topic");
      }
    }
  }

  @Override
  public Destination createDestinationFromAddress(Session session, String address, boolean isTopic) throws JMSException {
    if (address.contains(JmsConstants.TOPIC_PROPERTY + ":")) {
      // cut prefixes
      address = address.substring((PROTOCOL + "://" + JmsConstants.TOPIC_PROPERTY + ":").length());
      // cut any endpoint uri params, if any
      if (address.contains("?")) {
        address = address.substring(0, address.indexOf('?'));
      }
    }
    return createDestination(session, address, isTopic);
  }

  @Override
  public Destination createDestination(Session session, String name, boolean topic) throws JMSException {
    if (lookupJndiDestination.equals(ALWAYS) || lookupJndiDestination.equals(TRY_ALWAYS)) {
      try {
        Destination dest = getJndiDestination(name);
        if (dest != null) {
          if (logger.isDebugEnabled()) {
            logger.debug(MessageFormat.format("Destination {0} located in JNDI, will use it now", name));
          }
          return dest;
        } else {
          throw new JMSException("JNDI destination not found with name: " + name);
        }
      } catch (JMSException e) {
        if (lookupJndiDestination.equals(ALWAYS)) {
          throw e;
        } else {
          logger.warn("Unable to look up JNDI destination " + name + ": " + e.getMessage());
        }
      }
    }

    if (logger.isDebugEnabled()) {
      logger.debug("Using non-JNDI destination " + name + ", will create one now");
    }

    if (session == null) {
      throw new IllegalArgumentException("Session cannot be null when creating a destination");
    }
    if (name == null) {
      throw new IllegalArgumentException("Destination name cannot be null when creating a destination");
    }

    if (topic) {
      return session.createTopic(name);
    } else {
      return session.createQueue(name);
    }
  }

  protected Destination getJndiDestination(String name) throws JMSException {
    Object temp;
    try {
      if (logger.isDebugEnabled()) {
        logger.debug(MessageFormat.format("Looking up {0} from JNDI", name));
      }
      temp = jndiObjectSupplier.apply(name);
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      String message = MessageFormat.format("Failed to look up destination {0}. Reason: {1}",
                                            name, e.getMessage());
      throw new JMSException(message);
    }

    if (temp != null) {
      if (temp instanceof Destination) {
        return (Destination) temp;
      }
    }
    return null;
  }

  @Override
  public Destination createTemporaryDestination(Session session, boolean topic) throws JMSException {
    if (session == null) {
      throw new IllegalArgumentException("Session cannot be null when creating a destination");
    }

    if (topic) {
      return session.createTemporaryTopic();
    } else {
      return session.createTemporaryQueue();
    }
  }

  @Override
  public void send(MessageProducer producer, Message message, boolean persistentDelivery, boolean topic) throws JMSException {
    send(producer, message, persistentDelivery, Message.DEFAULT_PRIORITY,
         Message.DEFAULT_TIME_TO_LIVE, topic);
  }

  @Override
  public void send(MessageProducer producer, Message message, boolean persistentDelivery, Destination dest, boolean topic)
      throws JMSException {
    send(producer, message, dest, persistentDelivery, Message.DEFAULT_PRIORITY,
         Message.DEFAULT_TIME_TO_LIVE, topic);
  }

  @Override
  public void send(MessageProducer producer,
                   Message message,
                   boolean persistent,
                   int priority,
                   long ttl,
                   boolean topic)
      throws JMSException {
    producer.send(message, (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT),
                  priority, ttl);
  }

  @Override
  public void send(MessageProducer producer,
                   Message message,
                   Destination dest,
                   boolean persistent,
                   int priority,
                   long ttl,
                   boolean topic)
      throws JMSException {
    producer.send(dest, message, (persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT),
                  priority, ttl);
  }

  @Override
  public JmsSpecification getSpecification() {
    return JmsSpecification.JMS_1_1;
  }


  public Function<String, Destination> getJndiObjectSupplier() {
    return jndiObjectSupplier;
  }

  public LookupJndiDestination getLookupJndiDestination() {
    return lookupJndiDestination;
  }
}
