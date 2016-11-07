/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.function;

import java.util.function.Supplier;

import javax.jms.JMSException;

public interface JmsSupplier<K> {

  K get() throws JMSException;

  public static <K> Supplier<K> fromJmsSupplier(JmsSupplier<K> supplier) {
    return () -> {
      try {
        return supplier.get();
      } catch (JMSException e) {
        throw new RuntimeException(e);
      }
    };
  }

}
