/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.destination;

/**
 * //TODO
 */
public class QueueDestination implements DestinationType {

  @Override
  public boolean isTopic() {
    return false;
  }

  @Override
  public boolean useDurableTopicSubscription() {
    throw new IllegalStateException("cannot use this method with queue destination type");
  }

  @Override
  public String getDurableSubscriptionName() {
    throw new IllegalStateException("cannot use this method with queue destination type");
  }
}
