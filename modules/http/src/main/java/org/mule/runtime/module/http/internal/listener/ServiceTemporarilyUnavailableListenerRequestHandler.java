/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.http.internal.listener;

import static org.mule.runtime.module.http.api.HttpConstants.HttpStatus.SERVICE_UNAVAILABLE;

import org.mule.extension.http.internal.listener.HttpResponseFactory;
import org.mule.runtime.core.execution.MessageProcessingManager;

import java.nio.charset.Charset;

/**
 * Request handle for request calls to paths with no listener configured.
 */
public class ServiceTemporarilyUnavailableListenerRequestHandler extends ErrorRequestHandler {

  protected ServiceTemporarilyUnavailableListenerRequestHandler(Charset defaultEncoding, MessageProcessingManager messageProcessingManager, HttpResponseFactory httpResponseFactory) {
    super(defaultEncoding, SERVICE_UNAVAILABLE.getStatusCode(), SERVICE_UNAVAILABLE.getReasonPhrase(),
          "Service not available for request uri: %s", messageProcessingManager, httpResponseFactory);
  }

}
