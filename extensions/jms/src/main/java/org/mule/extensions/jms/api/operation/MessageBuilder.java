/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.operation;


import org.mule.extensions.jms.internal.JmsMessageUtils;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;

import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

public class MessageBuilder {

  @Parameter
  @Content
  private Object content;

  @Parameter
  @Optional(defaultValue = "true")
  private boolean sendContentType;

  @Parameter
  @Optional(defaultValue = "text/plain") //TODO take this from the message
  @DisplayName("ContentType")
  private String contentType;

  @Parameter
  @Optional
  private Map<String, Object> properties;

  Message build(Session session) throws JMSException {
    //TODO review with MG how to deal with properties
    Message message = JmsMessageUtils.toMessage(content, session);
    //if (sendContentType) {
    //
    //}

    return message;
  }

  public Object getContent() {
    return content;
  }

  public boolean isSendContentType() {
    return sendContentType;
  }

  public String getContentType() {
    return contentType;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}
