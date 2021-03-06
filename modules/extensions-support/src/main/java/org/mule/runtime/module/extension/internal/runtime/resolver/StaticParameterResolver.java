/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime.resolver;

import org.mule.runtime.extension.api.runtime.operation.ParameterResolver;

import java.util.Optional;

import static java.util.Optional.empty;

/**
 * {@link ParameterResolver} implementation for the parameter values that are resolved statically
 *
 * @param <T> Type of the value to resolve.
 * @since 4.0
 */
final class StaticParameterResolver<T> implements ParameterResolver<T> {

  private T value;

  StaticParameterResolver(T value) {
    this.value = value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T resolve() {
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<String> getExpression() {
    return empty();
  }
}
