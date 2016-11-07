/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.operation;

import org.mule.extensions.jms.JmsExtension;
import org.mule.extensions.jms.api.connection.JmsConnection;
import org.mule.extensions.jms.api.destination.DestinationType;
import org.mule.extensions.jms.api.destination.QueueDestination;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.UseConfig;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * //TODO
 */
public class JmsPublish {

  /*
    <jms:publish              config-ref=?jmsConfig?
        destination=?myQueue?
        transactionlAction=?ALWAYS_JOIN|JOIN_IF_POSSIBLE|NOT_SUPPORTED?
        persistentDelivery=?true?
        priority=?5?
        timeToLive=?20000?
        deliveryDelay=?1000?
        disableMessageTimestamp=?true? disableMessageId=?false?>
    </jms:publish>
   */

  //private static final Method setDeliveryDelayMethod =
  //    ClassUtils.getMethodIfAvailable(MessageProducer.class, "setDeliveryDelay", long.class);


  public void publish(@Connection JmsConnection connection, @UseConfig JmsExtension config,
                      String destination,
                      MessageBuilder messageBuilder,
                      @Optional boolean persistentDelivery,
                      @Optional DestinationType destinationType,
                      @Optional Integer priority,
                      @Optional Long timeToLive,
                      // JMS 2.0
                      @Optional Long deliveryDelay,
                      @Optional(defaultValue = "false") boolean publishAsynchronously)
      throws Exception {
    //this.deliveryDelay = deliveryDelay;
    //Preconditions.checkState(deliveryDelayOnlyConfiguredForJms2, "Delivery delay is only supported when working with JMS spec 2.0");
    Session session = null;
    MessageProducer producer = null;
    try {
      session = connection.createSession(config.getAckMode());
      destinationType = resolveDestinationType(destinationType);
      priority = resolvePriority(config.getPriority(), priority);
      timeToLive = resolveTimeToLive(config.getTimeToLive(), timeToLive);
      Destination jmsDestination = connection.getJmsSupport()
          .createDestinationFromAddress(session, destination, destinationType.isTopic());
      producer = connection.getJmsSupport().createProducer(session, jmsDestination, destinationType.isTopic());
      //deliveryDelay = resolveDeliveryDelay(deliveryDelay);
      //ReflectionUtils.invokeMethod(setDeliveryDelayMethod, producer, deliveryDelay);

      Message message = messageBuilder.build(session);
      connection.getJmsSupport().send(producer, message, persistentDelivery, priority, timeToLive, destinationType.isTopic());

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      connection.closeQuietly(producer);
      connection.closeQuietly(session);
    }
  }

  //boolean deliveryDelayOnlyConfiguredForJms2 = connection.getJmsSupport().getSpecification().equals(JmsSupport.JmsSpecification.JMS_2_0) || deliveryDelay == null;
  //
  //private Long resolveDeliveryDelay(Long deliveryDelay)
  //{
  //  return deliveryDelay != null ? deliveryDelay : DEFAULT_DELIVERY_DELAY;
  //}
  //
  private Long resolveTimeToLive(Long jmsConfigTimeToLive, Long operationTimeToLive) {
    return operationTimeToLive == null ? jmsConfigTimeToLive : operationTimeToLive;
  }

  private Integer resolvePriority(Integer jmsConfigPriority, Integer operationPriority) {
    return operationPriority == null ? jmsConfigPriority : operationPriority;
  }

  private DestinationType resolveDestinationType(DestinationType destinationType) {
    return destinationType != null ? destinationType : new QueueDestination();
  }

}
