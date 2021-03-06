/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.extension.internal.runtime;

import static org.mule.runtime.api.util.Preconditions.checkArgument;
import static org.mule.runtime.module.extension.internal.util.MuleExtensionUtils.hasAnyDynamic;
import static org.springframework.util.ReflectionUtils.setField;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.module.extension.internal.runtime.objectbuilder.ObjectBuilder;
import org.mule.runtime.module.extension.internal.runtime.resolver.ValueResolver;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for implementations of {@link ObjectBuilder}. It implements all of the contract's behavior, except for how to
 * actually create the instance to be returned. Implementors must provide that piece through the abstract
 * {@link #instantiateObject()} method
 *
 * @since 3.7.0
 */
public abstract class BaseObjectBuilder<T> implements ObjectBuilder<T> {

  private final Map<Field, ValueResolver<Object>> resolvers = new HashMap<>();

  /**
   * Returns the instance to be returned before the properties have been applied to it
   */
  protected abstract T instantiateObject();

  /**
   * {@inheritDoc}
   */
  @Override
  public ObjectBuilder<T> addPropertyResolver(Field field, ValueResolver<? extends Object> resolver) {
    checkArgument(field != null, "field cannot be null");
    checkArgument(resolver != null, "resolver cannot be null");

    field.setAccessible(true);
    resolvers.put(field, (ValueResolver<Object>) resolver);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isDynamic() {
    return hasAnyDynamic(resolvers.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T build(Event event) throws MuleException {
    T object = instantiateObject();

    for (Map.Entry<Field, ValueResolver<Object>> resolver : resolvers.entrySet()) {
      setField(resolver.getKey(), object, resolver.getValue().resolve(event));
    }

    return object;
  }
}
