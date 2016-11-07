/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.jms;

import org.mule.runtime.core.api.message.InternalMessage;

import org.junit.Test;

/**
 * //TODO
 */
public class JmsPublishTestCase extends AbstractJmsTestCase {

  @Override
  protected String getConfigFile() {
    return "basic-jms-config.xml";
  }

  @Test
  public void publishMessage(){
    InternalMessage simple = null;
    try {
      simple = flowRunner("simple").run().getMessage();
    } catch (Exception e) {
      e.printStackTrace();
    }
    String value = (String) simple.getPayload().getValue();

  }
}
