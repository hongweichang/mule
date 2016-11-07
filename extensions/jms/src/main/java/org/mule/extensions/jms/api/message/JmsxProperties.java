/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.message;

/**
 * //TODO
 */
public class JmsxProperties {

  /**
   *   Identity of the user sending message
   */
  private String JMSXUserID;


  /**
   * id of the app sending message
   */
  private String JMSXAppID;

  // Mandatory in JMS2.0
  /**
   * num of delivery attemps
   */
  private int JMSXDeliveryCount;

  /**
   *
   */
  private String JMSXGroupID;

  private int JMSXGroupSeq;

  private String JMSXProducerTXID;

  private String JMSXConsumerTXID;

  private long JMSXRcvTimestamp;

  /**
   * Assume there exists a message warehouse that contains a separate copy of each message
   * sent to each consumer and that these copies exist from the time the original message was sent.
   * Each copy's state is one of: 1(waiting), 2(ready), 3(expired) or 4(retained).
   * Since state is of no interest to producers and consumers, it is not provided to either.
   * It is only relevant to messages looked up in a warehouse, and JMS provides no API for this.
   */
  private int JMSXState;

  public JmsxProperties(String JMSXUserID, String JMSXAppID, int JMSXDeliveryCount, String JMSXGroupID, int JMSXGroupSeq,
                        String JMSXProducerTXID, String JMSXConsumerTXID, long JMSXRcvTimestamp, int JMSXState) {
    this.JMSXUserID = JMSXUserID;
    this.JMSXAppID = JMSXAppID;
    this.JMSXDeliveryCount = JMSXDeliveryCount;
    this.JMSXGroupID = JMSXGroupID;
    this.JMSXGroupSeq = JMSXGroupSeq;
    this.JMSXProducerTXID = JMSXProducerTXID;
    this.JMSXConsumerTXID = JMSXConsumerTXID;
    this.JMSXRcvTimestamp = JMSXRcvTimestamp;
    this.JMSXState = JMSXState;
  }

  public String getJMSXUserID() {
    return JMSXUserID;
  }

  public String getJMSXAppID() {
    return JMSXAppID;
  }

  public int getJMSXDeliveryCount() {
    return JMSXDeliveryCount;
  }

  public String getJMSXGroupID() {
    return JMSXGroupID;
  }

  public int getJMSXGroupSeq() {
    return JMSXGroupSeq;
  }

  public String getJMSXProducerTXID() {
    return JMSXProducerTXID;
  }

  public String getJMSXConsumerTXID() {
    return JMSXConsumerTXID;
  }

  public long getJMSXRcvTimestamp() {
    return JMSXRcvTimestamp;
  }

  public int getJMSXState() {
    return JMSXState;
  }

  //public Map<String, Object> asMap(){
  //  return new HashMap<>();
  //}
}
