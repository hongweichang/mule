/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.message;


import org.mule.extensions.jms.api.message.JmsAttributes;
import org.mule.extensions.jms.api.message.JmsHeaders;
import org.mule.extensions.jms.api.message.JmsMessageProperties;

public class DefaultJmsAttributes implements JmsAttributes {

  private JmsMessageProperties properties;
  private JmsHeaders headers;

  public DefaultJmsAttributes(JmsMessageProperties properties, JmsHeaders headers) {
    this.properties = properties;
    this.headers = headers;
  }

  public JmsMessageProperties getProperties() {
    return properties;
  }

  public JmsHeaders getHeaders() {
    return headers;
  }

  @Override
  public void acknowlewdge() {

  }

}
