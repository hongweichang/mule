/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.message;

import org.mule.extensions.jms.api.message.JmsxProperties;

/**
 * //TODO
 */
public final class JmsxPropertiesBuilder {

  private String JMSXUserID;

  private String JMSXAppID;

  private int JMSXDeliveryCount;

  private String JMSXGroupID;

  private int JMSXGroupSeq;

  private String JMSXProducerTXID;

  private String JMSXConsumerTXID;

  private long JMSXRcvTimestamp;

  private int JMSXState;

  private JmsxPropertiesBuilder() {}

  public static JmsxPropertiesBuilder create() {
    return new JmsxPropertiesBuilder();
  }

  public JmsxPropertiesBuilder add(String key, Object value) {

    try {
      switch (key) {
        case "JMSXConsumerTXID":
          this.JMSXConsumerTXID = (String) value;
        case "JMSXState":
          this.JMSXState = (int) value;
          //TODO ...
      }
    } catch (ClassCastException e) {
      //FIXME
      throw e;
    }

    return this;
  }

  public JmsxProperties build() {
    return new JmsxProperties(JMSXUserID, JMSXAppID, JMSXDeliveryCount, JMSXGroupID, JMSXGroupSeq,
                              JMSXProducerTXID, JMSXConsumerTXID, JMSXRcvTimestamp, JMSXState);
  }
}
