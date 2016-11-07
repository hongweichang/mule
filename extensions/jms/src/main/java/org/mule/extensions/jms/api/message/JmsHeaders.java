/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.message;

//TODO review if we should add a Map<String, Object> getAllHeaders() for jms 2.0
public interface JmsHeaders {

  String getJMSMessageID();

  long getJMSTimestamp();

  String getJMSCorrelationID();

  String getJMSReplyTo();

  String getJMSDestination();

  int getJMSDeliveryMode();

  boolean getJMSRedelivered();

  String getJMSType();

  long getJMSExpiration();

  int getJMSPriority();

}
