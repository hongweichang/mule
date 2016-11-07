/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.connection.factory;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import javax.jms.ConnectionFactory;

/**
 * //TODO
 */
public class CachingConnectionFactory {

  @Parameter
  private ConnectionFactory connectionFactory;

  /**
   * Defines the maximum amount of sessions that can be in the pool
   */
  @Parameter
  @Optional(defaultValue = "1")
  private int sessionCacheSize;

  /**
   * Indicates whether to cache JMS MessageProducers for the JMS connection
   */
  @Parameter
  @Optional(defaultValue = "true")
  private boolean cacheProducers;

}
