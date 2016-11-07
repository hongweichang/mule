/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.message;


import static java.nio.charset.Charset.forName;
import org.mule.extensions.jms.api.connection.JmsSpecification;
import org.mule.extensions.jms.api.message.JmsAttributes;
import org.mule.extensions.jms.api.message.JmsHeaders;
import org.mule.extensions.jms.internal.JmsMessageUtils;
import org.mule.runtime.api.metadata.DataType;
import org.mule.runtime.api.metadata.DataTypeParamsBuilder;
import org.mule.runtime.api.metadata.MediaType;
import org.mule.runtime.extension.api.runtime.operation.Result;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsMuleMessageFactory {

  private static final Logger logger = LoggerFactory.getLogger(JmsMuleMessageFactory.class);

  public Result<Object, JmsAttributes> createMessage(Message message, JmsSpecification specification, String contentType,
                                                     String encoding)
      throws IOException, JMSException {

    DefaultJmsAttributes jmsAttributes = new DefaultJmsAttributes(new DefaultJmsProperties(createMessageProperties(message),
                                                                                           message.getJMSMessageID()), // FIXME message Id
                                                                  createJmsHeaders(message));

    Object payload = getPayload(message, specification, encoding);

    return Result.<Object, JmsAttributes>builder()
        .output(payload).mediaType(getMediaType(contentType, encoding))
        .attributes(jmsAttributes).build();
  }

  private MediaType getMediaType(String contentType, String encoding) {
    DataTypeParamsBuilder builder = DataType.builder().mediaType(contentType);
    if (encoding != null) {
      builder.charset(forName(encoding));
    }
    return builder.build().getMediaType();
  }

  private Object getPayload(Message message, JmsSpecification specification, String encoding) throws IOException, JMSException {
    if (logger.isDebugEnabled()) {
      logger.debug("Message type received is: " + message.getClass().getSimpleName());
    }
    return JmsMessageUtils.toObject(message, specification, encoding);
  }

  private static Map<String, Object> createMessageProperties(Message jmsMessage) {
    Map<String, Object> properties = new HashMap<>();
    try {
      Enumeration<?> e = jmsMessage.getPropertyNames();
      while (e.hasMoreElements()) {
        String key = (String) e.nextElement();
        try {
          Object value = jmsMessage.getObjectProperty(key);
          if (value != null) {
            properties.put(key, value);
          }
        } catch (JMSException e1) {
          // ignored
        }
      }
    } catch (JMSException e1) {
      // ignored
    }
    return properties;
  }


  private JmsHeaders createJmsHeaders(Message jmsMessage) {
    DefaultJmsHeaders.Builder headersBuilder = new DefaultJmsHeaders.Builder();
    addCorrelationProperties(jmsMessage, headersBuilder);
    addDeliveryModeProperty(jmsMessage, headersBuilder);
    addDestinationProperty(jmsMessage, headersBuilder);
    addExpirationProperty(jmsMessage, headersBuilder);
    addMessageIdProperty(jmsMessage, headersBuilder);
    addPriorityProperty(jmsMessage, headersBuilder);
    addRedeliveredProperty(jmsMessage, headersBuilder);
    addJMSReplyTo(jmsMessage, headersBuilder);
    addTimestampProperty(jmsMessage, headersBuilder);
    addTypeProperty(jmsMessage, headersBuilder);
    return headersBuilder.build();
  }

  private void addTypeProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      String value = jmsMessage.getJMSType();
      if (value != null) {
        jmsHeadersBuilder.setType(value);
      }
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addTimestampProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      long value = jmsMessage.getJMSTimestamp();
      jmsHeadersBuilder.setTimestamp(value);
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addJMSReplyTo(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      Destination replyTo = jmsMessage.getJMSReplyTo();
      if (replyTo != null) {
        jmsHeadersBuilder.setReplyTo(getDestinationName(replyTo));
      }

      //TODO here old code set the reply to into the MuleMessage, verify if we need to do something else
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addRedeliveredProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      boolean value = jmsMessage.getJMSRedelivered();
      jmsHeadersBuilder.setRedelivered(value);
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addPriorityProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      int value = jmsMessage.getJMSPriority();
      jmsHeadersBuilder.setPriority(value);
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addMessageIdProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      String value = jmsMessage.getJMSMessageID();
      if (value != null) {
        jmsHeadersBuilder.setMessageId(value);
        //TODO here mule sets the MULE_MESSAGE_ID see if we have to do somthing
      }
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addExpirationProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      long value = jmsMessage.getJMSExpiration();
      jmsHeadersBuilder.setExpiration(value);
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addDestinationProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      Destination value = jmsMessage.getJMSDestination();
      if (value != null) {
        jmsHeadersBuilder.setDestination(getDestinationName(value));
      }
    } catch (JMSException e) {
      // ignored
    }
  }

  private String getDestinationName(Destination value) throws JMSException {
    return value instanceof Queue ? ((Queue) value).getQueueName() : ((Topic) value).getTopicName();
  }

  private void addDeliveryModeProperty(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      int value = jmsMessage.getJMSDeliveryMode();
      jmsHeadersBuilder.setDeliveryMode(value);
    } catch (JMSException e) {
      // ignored
    }
  }

  private void addCorrelationProperties(Message jmsMessage, DefaultJmsHeaders.Builder jmsHeadersBuilder) {
    try {
      String value = jmsMessage.getJMSCorrelationID();
      if (value != null) {
        jmsHeadersBuilder.setCorrelactionId(value);
        //TODO previously here the MULE_CORRELATION_ID was set also, see what to do with that.
      }
    } catch (JMSException e) {
      // ignored
    }
  }

}
