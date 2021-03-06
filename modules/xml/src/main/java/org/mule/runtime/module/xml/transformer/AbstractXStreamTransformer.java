/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.xml.transformer;

import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.transformer.TransformerException;
import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.core.transformer.AbstractMessageTransformer;
import org.mule.runtime.module.xml.transformer.datatype.CollectionDataTypeXStreamConverter;
import org.mule.runtime.module.xml.transformer.datatype.SimpleDataTypeXStreamConverter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;

/**
 * <code>AbstractXStreamTransformer</code> is a base class for all XStream based transformers. It takes care of creating and
 * configuring the XStream parser.
 */

public abstract class AbstractXStreamTransformer extends AbstractMessageTransformer {

  private final AtomicReference<XStream> xstream = new AtomicReference<>();
  private volatile String driverClass = XStreamFactory.XSTREAM_XPP_DRIVER;
  private volatile Map<String, Class<?>> aliases = new HashMap<>();
  private volatile Set<Class<? extends Converter>> converters = new HashSet<>();

  @Override
  public void initialise() throws InitialisationException {
    super.initialise();
    try {
      addConverter(SimpleDataTypeXStreamConverter.class);
      addConverter(CollectionDataTypeXStreamConverter.class);

      // Create XStream instance as part of initialization so that we can set
      // the context classloader that will be required to load classes.
      XStream xStreamInstance = getXStream();
      xStreamInstance.setClassLoader(Thread.currentThread().getContextClassLoader());
    } catch (TransformerException e) {
      throw new InitialisationException(e, this);
    }
  }

  public final XStream getXStream() throws TransformerException {
    XStream instance = xstream.get();

    if (instance == null) {
      try {
        instance = new XStreamFactory(driverClass, aliases, converters).getInstance();
        if (!xstream.compareAndSet(null, instance)) {
          instance = xstream.get();
        }
      } catch (Exception e) {
        throw new TransformerException(I18nMessageFactory.createStaticMessage("Unable to initialize XStream"), e);
      }
    }

    return instance;
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    AbstractXStreamTransformer clone = (AbstractXStreamTransformer) super.clone();
    clone.setDriverClass(driverClass);

    if (aliases != null) {
      clone.setAliases(new HashMap<>(aliases));
    }

    if (converters != null) {
      clone.setConverters(new HashSet<>(converters));
    }

    return clone;
  }

  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
    // force XStream instance update
    this.xstream.set(null);
  }

  public Map<String, Class<?>> getAliases() {
    return aliases;
  }

  public void setAliases(Map<String, Class<?>> aliases) {
    this.aliases = aliases;
    // force XStream instance update
    this.xstream.set(null);
  }

  public Set<Class<? extends Converter>> getConverters() {
    return converters;
  }

  public void setConverters(Set<Class<? extends Converter>> converters) {
    this.converters = converters;
    // force XStream instance update
    this.xstream.set(null);
  }

  public void addAlias(String alias, Class<?> aClass) {
    aliases.put(alias, aClass);
  }

  public Class<?> removeAlias(String alias) {
    return aliases.remove(alias);
  }

  public void addConverter(Class<? extends Converter> converterClass) {
    converters.add(converterClass);
  }

  public boolean removeAlias(Class<? extends Converter> converterClass) {
    return converters.remove(converterClass);
  }
}
