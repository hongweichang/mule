/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.connection.provider.activemq;

import org.mule.extensions.jms.api.connection.factory.ActiveMQConnectionFactoryProvider;
import org.mule.extensions.jms.internal.connection.provider.BaseConnectionProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;

import javax.jms.ConnectionFactory;

/**
 * //TODO
 */
@Alias("active-mq")
public class ActiveMQConnectionProvider extends BaseConnectionProvider {

  @ParameterGroup
  private ActiveMQConnectionFactoryProvider connectionFactoryProvider;

  @Parameter
  @Alias("enable-xa")
  @Optional(defaultValue = "false")
  private boolean enableXA;

  private ConnectionFactory connectionFactory;

  @Override
  public ConnectionFactory getConnectionFactory() {
    if (connectionFactory != null) {
      return connectionFactory;
    }

    createConnectionFactory();
    return connectionFactory;
  }

  private void createConnectionFactory() {
    connectionFactory = connectionFactoryProvider.getConnectionFactory();
    if (connectionFactory == null) {
      connectionFactory = connectionFactoryProvider.createDefaultConnectionFactory(enableXA);
    }
  }

  public ActiveMQConnectionFactoryProvider getConnectionFactoryProvider() {
    return connectionFactoryProvider;
  }

  public boolean isEnableXA() {
    return enableXA;
  }

}
