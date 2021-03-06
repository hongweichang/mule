/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.connector;

import org.mule.runtime.core.MessageExchangePattern;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.client.OperationOptions;
import org.mule.runtime.core.api.processor.Processor;

/**
 * Locator for a MessageProcessor which is an operation from a Mule connector that fulfills the operation required.
 */
public interface ConnectorOperationLocator {

  /**
   * Lookup for an operation from a connector
   *
   * @param url the URL that identifies the operation
   * @param operationOptions the options to use to execute the operation
   * @param exchangePattern the exchange pattern to use for the operation
   * @return the operation configured for the url and options
   * @throws MuleException
   */
  Processor locateConnectorOperation(String url, OperationOptions operationOptions, MessageExchangePattern exchangePattern)
      throws MuleException;

}
