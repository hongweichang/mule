/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.connection.factory.jndi;

import org.mule.runtime.extension.api.annotation.param.ExclusiveOptionals;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;

import javax.naming.spi.InitialContextFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link JndiNameResolver} or the set of properties required to
 * create one, represented by {@link JndiNameResolverProperties}
 *
 * @since 4.0
 */
@ExclusiveOptionals(isOneRequired = true)
public class JndiNameResolverProvider {

  private static final Logger logger = LoggerFactory.getLogger(JndiNameResolverProvider.class);

  @Parameter
  @Optional
  private JndiNameResolver customJndiNameResolver;

  @Parameter
  @Optional
  private JndiNameResolverProperties nameResolverBuilder;

  /**
   * If a {@code customJndiNameResolver} is not provided,
   * then one will be created by default
   */
  private JndiNameResolver defaultNameResolver;

  public JndiNameResolver getCustomJndiNameResolver() {
    return customJndiNameResolver;
  }

  public JndiNameResolverProperties getNameResolverBuilder() {
    return nameResolverBuilder;
  }

  JndiNameResolver getJndiNameResolver() {
    if (customJndiNameResolver != null) {
      return customJndiNameResolver;
    }

    if (defaultNameResolver == null) {
      defaultNameResolver = createDefaultJndiResolver();
    }

    return defaultNameResolver;
  }

  private JndiNameResolver createDefaultJndiResolver() {
    if (logger.isDebugEnabled()) {
      logger.debug("Creating default JndiNameResolver");
    }

    SimpleJndiNameResolver nameResolver = new SimpleJndiNameResolver();
    nameResolver.setJndiProviderUrl(nameResolverBuilder.getJndiProviderUrl());
    nameResolver.setJndiInitialFactory(nameResolverBuilder.getJndiInitialFactory());
    nameResolver.setJndiProviderProperties(nameResolverBuilder.getProviderProperties());

    InitialContextFactory initialContextFactory = nameResolverBuilder.getInitialContextFactory();
    if (initialContextFactory != null) {
      nameResolver.setContextFactory(initialContextFactory);
    }

    return nameResolver;
  }
}
