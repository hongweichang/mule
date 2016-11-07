/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.destination;

public interface DestinationType {

  //FIXME
  /*
    How can we have a dynamic destination in a publish that can dynamically declare if it?s a topic or queue.
    Since this use case is very advanced we can assume that to define a topic the destination value needs to be ?topic://myTopic?.
   */
  boolean isTopic();

  boolean useDurableTopicSubscription();

  String getDurableSubscriptionName();

}
