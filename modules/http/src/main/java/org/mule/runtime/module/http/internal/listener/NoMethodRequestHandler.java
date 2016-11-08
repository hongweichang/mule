/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.module.http.internal.listener;

import static org.mule.runtime.module.http.api.HttpConstants.HttpStatus.METHOD_NOT_ALLOWED;

import org.mule.extension.http.internal.listener.HttpResponseFactory;
import org.mule.runtime.core.execution.MessageProcessingManager;

import java.nio.charset.Charset;

public class NoMethodRequestHandler extends ErrorRequestHandler {

  protected NoMethodRequestHandler(Charset encoding, MessageProcessingManager messageProcessingManager, HttpResponseFactory httpResponseFactory) {
    super(encoding, METHOD_NOT_ALLOWED.getStatusCode(), METHOD_NOT_ALLOWED.getReasonPhrase(), "Method not allowed for endpoint: %s", messageProcessingManager, httpResponseFactory);
  }

}
