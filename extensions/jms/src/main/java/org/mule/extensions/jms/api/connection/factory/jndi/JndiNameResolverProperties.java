/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.connection.factory.jndi;

import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import java.util.Map;

import javax.naming.spi.InitialContextFactory;

/**
 * Declares the properties required to create a {@link JndiNameResolver}
 *
 * @since 4.0
 */
public class JndiNameResolverProperties {

  @Parameter
  private String jndiInitialFactory;

  @Parameter
  @Optional
  private InitialContextFactory initialContextFactory;

  @Parameter
  @Optional
  private String jndiProviderUrl;

  @Parameter
  @Optional
  private Map<String, Object> providerProperties;

  public String getJndiInitialFactory() {
    return jndiInitialFactory;
  }

  public Map<String, Object> getProviderProperties() {
    return providerProperties;
  }

  public String getJndiProviderUrl() {
    return jndiProviderUrl;
  }

  public InitialContextFactory getInitialContextFactory() {
    return initialContextFactory;
  }
}
