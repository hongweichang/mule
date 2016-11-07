/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.connection.factory;

import static org.mule.runtime.core.util.ClassUtils.instanciateClass;
import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import javax.jms.ConnectionFactory;

/**
 * //TODO
 */
@ExclusiveOptionals(isOneRequired = true)
public class ActiveMQConnectionFactoryProvider {

  private static final String ACTIVEMQ_CONNECTION_FACTORY_CLASS = "org.apache.activemq.ActiveMQConnectionFactory";
  private static final String ACTIVEMQ_XA_CONNECTION_FACTORY_CLASS = "org.apache.activemq.ActiveMQXAConnectionFactory";
  private static final String DEFAULT_BROKER_URL = "vm://localhost?broker.persistent=false&broker.useJmx=false";

  @Parameter
  @Optional
  private ConnectionFactory connectionFactory;

  @Parameter
  @Optional(defaultValue = DEFAULT_BROKER_URL)
  private String brokerUrl;


  public ConnectionFactory getConnectionFactory() {
    return connectionFactory;
  }

  public String getBrokerUrl() {
    return brokerUrl;
  }

  public ConnectionFactory createDefaultConnectionFactory(boolean enableXA) {

    if (connectionFactory != null) {
      return connectionFactory;
    }

    try {
      this.connectionFactory = (ConnectionFactory) instanciateClass(getFactoryClass(enableXA), getBrokerUrl());
      return connectionFactory;
    } catch (Exception e) {
      //FIXME handling
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private String getFactoryClass(boolean enableXA) {
    return enableXA ? ACTIVEMQ_XA_CONNECTION_FACTORY_CLASS : ACTIVEMQ_CONNECTION_FACTORY_CLASS;
  }

}
