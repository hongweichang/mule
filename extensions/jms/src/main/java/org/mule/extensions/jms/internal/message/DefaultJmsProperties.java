/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.internal.message;


import static java.util.Collections.unmodifiableMap;
import org.mule.extensions.jms.api.message.JmsMessageProperties;
import org.mule.extensions.jms.api.message.JmsxProperties;

import java.util.HashMap;
import java.util.Map;

public class DefaultJmsProperties implements JmsMessageProperties {

  private final String ackId;
  private Map<String, Object> allPropertiesMap = new HashMap<>();
  private Map<String, Object> userProperties = new HashMap<>();
  private Map<String, Object> jmsProperties = new HashMap<>();
  private JmsxProperties jmsxProperties;

  public DefaultJmsProperties(Map<String, Object> messageProperties, String ackId) {
    this.ackId = ackId;

    allPropertiesMap = unmodifiableMap(messageProperties);
    JmsxPropertiesBuilder jmsxPropertiesBuilder = JmsxPropertiesBuilder.create();
    for (Map.Entry<String, Object> entry : messageProperties.entrySet()) {

      if (entry.getKey().startsWith("JMSX")) {
        jmsxPropertiesBuilder.add(entry.getKey(), entry.getValue());

      } else if (entry.getKey().startsWith("JMS")) {//TODO change startWith to "JMS_"?
        jmsProperties.put(entry.getKey(), entry.getValue());

      } else {
        userProperties.put(entry.getKey(), entry.getValue());
      }
    }
    userProperties = unmodifiableMap(userProperties);
    jmsProperties = unmodifiableMap(jmsProperties);
    jmsxProperties = jmsxPropertiesBuilder.build();
  }

  @Override
  public Map<String, Object> asMap() {
    return unmodifiableMap(allPropertiesMap);
  }

  @Override
  public Map<String, Object> getUserProperties() {
    return userProperties;
  }

  @Override
  public Map<String, Object> getJmsProperties() {
    return jmsProperties;
  }

  @Override
  public JmsxProperties getJmsxProperties() {
    return jmsxProperties;
  }

  @Override
  public String getAckId() {
    return ackId;
  }

  @Override
  public boolean equals(Object o) {
    return allPropertiesMap.equals(o);
  }

  @Override
  public int hashCode() {
    return allPropertiesMap.hashCode();
  }

}
