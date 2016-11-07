/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.message;


import org.mule.extensions.jms.api.message.JmsHeaders;

public class DefaultJmsHeaders implements JmsHeaders {

  private String messageId;
  private long timestamp;
  private String correlactionId;
  private String replyTo;
  private String destination;
  private int deliveryMode;
  private boolean redelivered;
  private String type;
  private long expiration;
  private int priority;

  @Override
  public String getJMSMessageID() {
    return messageId;
  }

  @Override
  public long getJMSTimestamp() {
    return timestamp;
  }

  @Override
  public String getJMSCorrelationID() {
    return correlactionId;
  }

  @Override
  public String getJMSReplyTo() {
    return replyTo;
  }

  @Override
  public String getJMSDestination() {
    return destination;
  }

  @Override
  public int getJMSDeliveryMode() {
    return deliveryMode;
  }

  @Override
  public boolean getJMSRedelivered() {
    return redelivered;
  }

  @Override
  public String getJMSType() {
    return type;
  }

  @Override
  public long getJMSExpiration() {
    return expiration;
  }

  @Override
  public int getJMSPriority() {
    return priority;
  }

  public static class Builder {

    private DefaultJmsHeaders jmsHeaders = new DefaultJmsHeaders();

    public Builder setMessageId(String messageId) {
      jmsHeaders.messageId = messageId;
      return this;
    }

    public Builder setTimestamp(long timestamp) {
      jmsHeaders.timestamp = timestamp;
      return this;
    }

    public Builder setCorrelactionId(String correlactionId) {
      jmsHeaders.correlactionId = correlactionId;
      return this;
    }

    public Builder setReplyTo(String replyTo) {
      jmsHeaders.replyTo = replyTo;
      return this;
    }

    public Builder setDestination(String destination) {
      jmsHeaders.destination = destination;
      return this;
    }

    public Builder setDeliveryMode(int deliveryMode) {
      jmsHeaders.deliveryMode = deliveryMode;
      return this;
    }

    public Builder setRedelivered(boolean redelivered) {
      jmsHeaders.redelivered = redelivered;
      return this;
    }

    public Builder setType(String type) {
      jmsHeaders.type = type;
      return this;
    }

    public Builder setExpiration(long expiration) {
      jmsHeaders.expiration = expiration;
      return this;
    }

    public Builder setPriority(int priority) {
      jmsHeaders.priority = priority;
      return this;
    }

    public DefaultJmsHeaders build() {
      return jmsHeaders;
    }
  }
}
