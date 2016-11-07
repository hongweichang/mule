package org.mule.extensions.jms;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


import static org.mule.runtime.api.meta.ExpressionSupport.NOT_SUPPORTED;
import static org.mule.runtime.extension.api.annotation.param.display.Placement.ADVANCED;
import org.mule.extensions.jms.api.config.AckMode;
import org.mule.extensions.jms.api.operation.JmsConsume;
import org.mule.extensions.jms.api.operation.JmsPublish;
import org.mule.extensions.jms.internal.connection.provider.GenericConnectionProvider;
import org.mule.extensions.jms.internal.connection.provider.activemq.ActiveMQConnectionProvider;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * @since 4.0
 */

@Extension(name = "JMS Extension")
@Xml(namespace = "jmsn")
@ConnectionProviders({GenericConnectionProvider.class, ActiveMQConnectionProvider.class})
@Operations({JmsPublish.class, JmsConsume.class})
//@Sources({JmsSubscriber.class})
public class JmsExtension implements Initialisable {


  @Inject
  private MuleContext muleContext;

  //TODO MULE-10904: remove this logic
  @Override
  public void initialise() throws InitialisationException {
    if (encoding == null) {
      encoding = muleContext.getConfiguration().getDefaultEncoding();
    }
  }

  /**
   * No redelivery. -1 mean infinite re deliveries accepted.
   * Can be overridden at the message source level.
   */
  @Parameter
  @Optional(defaultValue = "0")
  @Expression(NOT_SUPPORTED)
  private int maxRedelivery;

  /**
   * AUTO. Mule acks the message only if the flow is finished successfully.
   * MANUAL. This is JMS client ack mode. The user must do the ack manually within the flow.
   * DUPS_OK. JMS message is ack automatically but in a lazy fashion which may lead to duplicates.
   * NONE. Automatically acks the message upon reception.
   * Can be overridden at the message source level.
   * This attribute has to be NONE if transactionType is LOCAL or MULTI
   */
  @Parameter
  @Optional(defaultValue = "AUTO")
  @Expression(NOT_SUPPORTED)
  private AckMode ackMode;

  /**
   * If by default, topic subscriptions must be made durable or not.
   * Can be overridden at the message source level.
   * Requires clientId attribute set.
   */
  @Parameter
  @Optional(defaultValue = "false")
  @Expression(NOT_SUPPORTED)
  // FIXME Source only
  private boolean durableTopicSubscriber;

  /**
   * If by default, the message must be sent using persistent mode.
   * Can be overridden at the operation level.
   */
  @Parameter
  @Optional(defaultValue = "true")
  @Expression(NOT_SUPPORTED)
  private boolean persistentDelivery;

  /**
   * Defines the default message priority to use when sending messages
   */
  @Parameter
  @Optional(defaultValue = "4")
  @Expression(NOT_SUPPORTED)
  private int priority;

  /**
   * Defines the default time the message will be in the broker before being discarded.
   */
  @Parameter
  @Optional(defaultValue = "0")
  @Expression(NOT_SUPPORTED)
  private long timeToLive;

  /**
   * A {@link TimeUnit} which qualifies the {@link #timeToLive} attribute.
   * <p>
   * Defaults to {@code MILLISECONDS}
   */
  @Parameter
  @Optional(defaultValue = "MILLISECONDS")
  @Expression(NOT_SUPPORTED)
  @Placement(tab = ADVANCED, group = "TIMEOUT_CONFIGURATION", order = 1) //FIXME
  @Summary("Time unit to be used in the timeToLive configurations")
  private TimeUnit timeToLiveUnit;

  /**
   * Defines if it should be possible or not to consume messages
   * published by this connector connection.
   */
  @Parameter
  @Optional(defaultValue = "false")
  @Expression(NOT_SUPPORTED)
  private boolean noLocal;

  /**
   * Defines the default value to use, when producing messages,
   * for disable message id generation in the broker.
   * Depending on the provider it may or may not have effect
   */
  //TODO producer only?
  @Parameter
  @Optional(defaultValue = "false")
  @Expression(NOT_SUPPORTED)
  private boolean disableMessageId;

  /**
   * Defines the default value to use, when producing messages,
   * for disable message timestamp generation in the broker.
   * Depending on the provider it may or may not have effect.
   */
  //TODO producer only?
  @Parameter
  @Optional(defaultValue = "false")
  @Expression(NOT_SUPPORTED)
  private boolean disableMessageTimestamp;

  /**
   * Defines if publish operations should run asynchronously
   * which means the operation will not care if the broker were able
   * to store the message successfully or not.
   * This can be used for performance boost.
   * Only supported with JMS 2.0
   */
  //TODO producer only?
  @Parameter
  @Optional(defaultValue = "false")
  @Expression(NOT_SUPPORTED)
  private boolean publishAsynchronously; //TODO JMS 2.0

  @Parameter
  @Optional
  private String encoding;

  @Parameter
  @Optional(defaultValue = "text/plain")
  private String contentType;


  public String getContentType() {
    return contentType;
  }

  public String getEncoding() {
    return encoding;
  }

  public int getMaxRedelivery() {
    return maxRedelivery;
  }

  public AckMode getAckMode() {
    return ackMode;
  }

  public boolean isDurableTopicSubscriber() {
    return durableTopicSubscriber;
  }

  public boolean isPersistentDelivery() {
    return persistentDelivery;
  }

  public int getPriority() {
    return priority;
  }

  public long getTimeToLive() {
    return timeToLive;
  }

  public TimeUnit getTimeToLiveUnit() {
    return timeToLiveUnit;
  }

  public boolean isNoLocal() {
    return noLocal;
  }

  public boolean isDisableMessageId() {
    return disableMessageId;
  }

  public boolean isDisableMessageTimestamp() {
    return disableMessageTimestamp;
  }

  public boolean isPublishAsynchronously() {
    return publishAsynchronously;
  }

}
