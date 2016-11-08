/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.api.processor.strategy;

import static reactor.core.publisher.Flux.from;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.Pipeline;
import org.mule.runtime.core.api.exception.MessagingExceptionHandler;
import org.mule.runtime.core.api.processor.Processor;

import java.util.function.Function;

import org.reactivestreams.Publisher;

/**
 * Determines how a list of message processors should processed.
 */
public interface ProcessingStrategy {

  default Function<Publisher<Event>, Publisher<Event>> onPipeline(Pipeline pipeline,
                                                                  Function<Publisher<Event>, Publisher<Event>> publisherFunction) {
    return publisher -> from(publisher).transform(publisherFunction);
  }

  default Function<Publisher<Event>, Publisher<Event>> onPipeline(Pipeline pipeline,
                                                                  Function<Publisher<Event>, Publisher<Event>> publisherFunction,
                                                                  MessagingExceptionHandler messagingExceptionHandler) {
    return onPipeline(pipeline, publisherFunction);
  }


  default Function<Publisher<Event>, Publisher<Event>> onProcessor(Processor messageProcessor,
                                                                   Function<Publisher<Event>, Publisher<Event>> publisherFunction) {
    return publisher -> from(publisher).transform(publisherFunction);
  }

  /**
   * Whether the processing that has this instance is synchronous or not
   */
  default boolean isSynchronous() {
    return false;
  }

}
