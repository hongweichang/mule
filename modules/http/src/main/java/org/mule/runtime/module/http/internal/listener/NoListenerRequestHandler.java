/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.http.internal.listener;

import static org.mule.runtime.module.http.api.HttpConstants.HttpStatus.NOT_FOUND;

import org.mule.extension.http.internal.listener.HttpResponseFactory;
import org.mule.runtime.core.execution.MessageProcessingManager;

import java.nio.charset.Charset;

/**
 * Request handle for request calls to paths with no listener configured.
 */
public class NoListenerRequestHandler extends ErrorRequestHandler {

  protected NoListenerRequestHandler(Charset encoding, MessageProcessingManager messageProcessingManager, HttpResponseFactory httpResponseFactory)
  {
    super(encoding, NOT_FOUND.getStatusCode(), NOT_FOUND.getReasonPhrase(), "No listener for endpoint: %s", messageProcessingManager, httpResponseFactory);
  }

}
