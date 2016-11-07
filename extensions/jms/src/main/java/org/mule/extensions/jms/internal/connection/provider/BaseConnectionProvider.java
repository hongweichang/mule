/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.connection.provider;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.mule.extensions.jms.api.connection.JmsSpecification.JMS_1_0_2b;
import org.mule.extensions.jms.api.connection.JmsConnection;
import org.mule.extensions.jms.internal.connection.param.GenericConnectionParameters;
import org.mule.extensions.jms.internal.support.Jms102bSupport;
import org.mule.extensions.jms.internal.support.Jms11Support;
import org.mule.extensions.jms.internal.support.JmsSupport;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.lifecycle.LifecycleManager;
import org.mule.runtime.core.api.lifecycle.Startable;
import org.mule.runtime.core.api.lifecycle.Stoppable;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import java.util.Optional;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * //TODO
 */
public abstract class BaseConnectionProvider
    implements ConnectionProvider<JmsConnection>, ExceptionListener, Startable, Stoppable, Initialisable {

  private static final Logger logger = LoggerFactory.getLogger(BaseConnectionProvider.class);

  @ParameterGroup
  private GenericConnectionParameters connectionParameters;

  public GenericConnectionParameters getConnectionParameters() {
    return connectionParameters;
  }

  private JmsSupport jmsSupport;

  private Connection connection;

  /**
   * Used to ignore handling of ExceptionListener#onException when in the process of disconnecting.  This is
   * required because the Connector {@link LifecycleManager} does not include
   * connection/disconnection state.
   */
  private volatile boolean disconnecting;


  public abstract ConnectionFactory getConnectionFactory();

  @Override
  public JmsConnection connect() throws ConnectionException {
    return new JmsConnection(jmsSupport, connection);
  }

  @Override
  public void disconnect(JmsConnection connection) {

  }

  @Override
  public ConnectionValidationResult validate(JmsConnection connection) {
    return ConnectionValidationResult.success();
  }

  @Override
  public void start() throws MuleException {
    try {
      this.connection = createConnection();
      this.connection.start();
    } catch (MuleException e) {
      throw e;
    } catch (Exception e) {
      throw new DefaultMuleException(e);
    }
  }

  @Override
  public void initialise() throws InitialisationException {
    createJmsSupport();
  }

  /**
   * A factory method to create various JmsSupport class versions.
   *
   * @return JmsSupport instance
   * @see JmsSupport
   */
  protected void createJmsSupport() {
    try {
      if (JMS_1_0_2b.equals(getConnectionParameters().getSpecification())) {
        jmsSupport = new Jms102bSupport();
      } else {
        jmsSupport = new Jms11Support();
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Connection createConnection() throws Exception {
    Connection connection;

    String username = getConnectionParameters().getUsername();
    String password = getConnectionParameters().getPassword();
    if (!isBlank(username) && !isBlank(password)) {
      connection = jmsSupport.createConnection(getConnectionFactory(), username, password);
    } else {
      if (!isBlank(username) || !isBlank(password)) {
        logger.error(
                     format("Connection requires both username and password to be present, but one is missing: [username: %s], [password: %s]",
                            username, password));
      }

      connection = jmsSupport.createConnection(getConnectionFactory());
    }

    if (connection != null) {
      // EE-1901: only sets the clientID if it was not already set
      Optional<String> configuredClientIdOptional = ofNullable(getConnectionParameters().getClientId());
      if (configuredClientIdOptional.isPresent() && !configuredClientIdOptional.get().equals(connection.getClientID())) {
        connection.setClientID(configuredClientIdOptional.get());
      }

      //TODO review embeddedMode improvement
      if (connection.getExceptionListener() == null) {
        connection.setExceptionListener(this);
      }
    }
    return connection;
  }

  @Override
  public void stop() throws MuleException {
    disconnecting = true;
    try {
      connection.close();
    } catch (Exception e) {
      logger.warn(e.getMessage());
      if (logger.isDebugEnabled()) {
        logger.debug(e.getMessage(), e);
      }
    }
  }

  @Override
  public void onException(JMSException e) {
    if (!disconnecting) {
      //TODO implement
      //Map<Object, MessageReceiver> receivers = getReceivers();
      //boolean isMultiConsumerReceiver = false;
      //
      //if (!receivers.isEmpty())
      //{
      //    MessageReceiver reciever = receivers.values().iterator().next();
      //    if (reciever instanceof MultiConsumerJmsMessageReceiver)
      //    {
      //        isMultiConsumerReceiver = true;
      //    }
      //}
      //
      //int expectedReceiverCount = isMultiConsumerReceiver ? 1 :
      //                            (getReceivers().size() * getNumberOfConcurrentTransactedReceivers());
      //
      //if (logger.isDebugEnabled())
      //{
      //    logger.debug("About to recycle myself due to remote JMS connection shutdown but need "
      //                 + "to wait for all active receivers to report connection loss. Receiver count: "
      //                 + (receiverReportedExceptionCount.get() + 1) + '/' + expectedReceiverCount);
      //}
      //
      //if (receiverReportedExceptionCount.incrementAndGet() >= expectedReceiverCount)
      //{
      //    receiverReportedExceptionCount.set(0);
      //    muleContext.getExceptionListener().handleException(new ConnectException(jmsException, this));
      //}
    }
  }

  public JmsSupport getJmsSupport() {
    return jmsSupport;
  }

  public void setJmsSupport(JmsSupport jmsSupport) {
    this.jmsSupport = jmsSupport;
  }

  public Connection getConnection() {
    return connection;
  }

  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  public boolean isDisconnecting() {
    return disconnecting;
  }

  public void setDisconnecting(boolean disconnecting) {
    this.disconnecting = disconnecting;
  }
}
