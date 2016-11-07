/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extensions.jms.api.message;

import java.util.Map;

/**
 * TODO
 */
public interface JmsMessageProperties {

  Map<String, Object> asMap();

  Map<String, Object> getUserProperties();

  Map<String, Object> getJmsProperties();

  JmsxProperties getJmsxProperties();

  String getAckId();

}
