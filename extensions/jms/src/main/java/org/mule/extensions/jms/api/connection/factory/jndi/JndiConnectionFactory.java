/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.connection.factory.jndi;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import org.mule.extensions.jms.api.JmsConnectorException;
import org.mule.extensions.jms.api.connection.LookupJndiDestination;
import org.mule.extensions.jms.internal.i18n.JmsMessages;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.transaction.Transaction;
import org.mule.runtime.core.api.transaction.TransactionException;
import org.mule.runtime.core.transaction.TransactionCoordination;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.text.MessageFormat;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.naming.CommunicationException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ConnectionFactory} that wraps a {@link ConnectionFactory delegate}
 * that is discovered using a {@link JndiNameResolver}
 *
 * @since 4.0
 */
public class JndiConnectionFactory implements ConnectionFactory, Initialisable {

  private static final Logger logger = LoggerFactory.getLogger(JndiConnectionFactory.class);

  /**
   * Name of the ConnectionFactory to be discovered using Jndi
   * and used as a delegate of {@code this} {@link ConnectionFactory}
   */
  @Parameter
  private String connectionFactoryJndiName;

  /**
   * NEVER. Will never lookup for jndi destinations.
   * ALWAYS. Will always lookup the destinations through JNDI. It will fail if the destination does not exists.
   * TRY_ALWAYS. Will always try to lookup the destinations through JNDI but if it does not exists it will create a new one.
   */
  @Parameter
  @Optional(defaultValue = "NEVER")
  private LookupJndiDestination lookupDestination;

  @ParameterGroup
  private JndiNameResolverProvider nameResolverProvider;


  /**
   * The actual {@link ConnectionFactory} in which {@code this} {@link ConnectionFactory}
   * delegates.
   * This {@code delegate} was discovered using the {@code connectionFactoryJndiName}
   * and {@code nameResolverProvider}
   */
  private ConnectionFactory delegate;

  public String getConnectionFactoryJndiName() {
    return connectionFactoryJndiName;
  }

  public LookupJndiDestination getLookupDestination() {
    return lookupDestination;
  }

  public JndiNameResolverProvider getNameResolverProvider() {
    return nameResolverProvider;
  }

  @Override
  public void initialise() throws InitialisationException {
    try {
      Object temp = getJndiNameResolver().lookup(connectionFactoryJndiName);
      if (temp instanceof ConnectionFactory) {
        delegate = (ConnectionFactory) temp;
      } else {
        throw new RuntimeException(
                                   JmsMessages.invalidResourceType(ConnectionFactory.class, temp).getMessage());
      }
    } catch (NamingException e) {
      e.printStackTrace();
      throw new InitialisationException(e, this);
    }
  }

  private Object lookupFromJndi(String jndiName) throws NamingException {
    try {
      return getJndiNameResolver().lookup(jndiName);
    } catch (CommunicationException ce) {
      try {
        final Transaction tx = TransactionCoordination.getInstance().getTransaction();
        if (tx != null) {
          tx.setRollbackOnly();
        }
      } catch (TransactionException e) {
        throw new MuleRuntimeException(createStaticMessage("Failed to mark transaction for rollback: "), e);
      }

      throw ce;
    }
  }

  public Destination getJndiDestination(String name) {
    Object temp;
    try {
      if (logger.isDebugEnabled()) {
        logger.debug(MessageFormat.format("Looking up {0} from JNDI", name));
      }
      temp = lookupFromJndi(name);
    } catch (NamingException e) {
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
      String message = MessageFormat.format("Failed to look up destination {0}. Reason: {1}",
                                            name, e.getMessage());
      throw new JmsConnectorException(message);
    }

    return temp instanceof Destination ? (Destination) temp : null;
  }

  private JndiNameResolver getJndiNameResolver() {
    return nameResolverProvider.getJndiNameResolver();
  }

  @Override
  public Connection createConnection() throws JMSException {
    return delegate.createConnection();
  }

  @Override
  public Connection createConnection(String userName, String password) throws JMSException {
    return delegate.createConnection(userName, password);
  }

  @Override
  public JMSContext createContext() {
    return delegate.createContext();
  }

  @Override
  public JMSContext createContext(String userName, String password) {
    return delegate.createContext(userName, password);
  }

  @Override
  public JMSContext createContext(String userName, String password, int sessionMode) {
    return delegate.createContext(userName, password, sessionMode);
  }

  @Override
  public JMSContext createContext(int sessionMode) {
    return delegate.createContext(sessionMode);
  }

}
