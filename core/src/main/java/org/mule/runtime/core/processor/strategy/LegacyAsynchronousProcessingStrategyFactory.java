/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static reactor.core.Exceptions.propagate;
import static reactor.core.publisher.Flux.from;
import static reactor.core.publisher.Flux.just;
import static reactor.core.scheduler.Schedulers.fromExecutorService;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.DefaultMuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Pipeline;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.runtime.core.api.scheduler.Scheduler;
import org.mule.runtime.core.api.scheduler.SchedulerService;
import org.mule.runtime.core.exception.MessagingException;
import org.mule.runtime.core.processor.strategy.AsynchronousProcessingStrategyFactory.AsynchronousProcessingStrategy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.resource.spi.work.WorkManager;

import org.reactivestreams.Publisher;

/**
 * This factory's strategy uses a {@link WorkManager} to schedule the processing of the pipeline of message processors in a single
 * worker thread.
 */
public class LegacyAsynchronousProcessingStrategyFactory implements ProcessingStrategyFactory {

  public static final String SYNCHRONOUS_EVENT_ERROR_MESSAGE = "Unable to process a synchronous event asynchronously";

  @Override
  public ProcessingStrategy create(MuleContext muleContext) {
    return new LegacyAsynchronousProcessingStrategy(() -> {
      try {
        return muleContext.getRegistry().lookupObject(SchedulerService.class).ioScheduler();
      } catch (RegistrationException e) {
        throw new MuleRuntimeException(e);
      }
    }, scheduler -> scheduler.stop(muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS), muleContext);
  }

  static class LegacyAsynchronousProcessingStrategy extends AsynchronousProcessingStrategy {


    public LegacyAsynchronousProcessingStrategy(Supplier<Scheduler> schedulerSupplier, Consumer<Scheduler> schedulerStopper,
                                                MuleContext muleContext) {
      super(schedulerSupplier, schedulerStopper, muleContext);
    }

    public Function<Publisher<Event>, Publisher<Event>> onPipeline(Pipeline pipeline,
                                                                   Function<Publisher<Event>, Publisher<Event>> publisherFunction) {

      // Conserve existing 3.x async processing strategy behaviuor:
      // i) The request event is echoed rather than the the result of async processing returned
      // ii) Any exceptions that occur due to async processing are not propagated upwards
      return publisher -> from(publisher)
          .doOnNext(assertCanProcessAsync())
          .doOnNext(fireAsyncScheduledNotification(pipeline))
          .doOnNext(event -> just(event).publishOn(fromExecutorService(getScheduler()))
              .transform(publisherFunction)
              .doOnNext(request -> fireAsyncCompleteNotification(request, pipeline, null))
              .doOnError(MessagingException.class, e -> fireAsyncCompleteNotification(event, pipeline, e))
              .onErrorResumeWith(MessagingException.class, pipeline.getExceptionListener()).subscribe());
    }

    private Consumer<Event> assertCanProcessAsync() {
      return event -> {
        if (event.isSynchronous() || event.isTransacted()) {
          throw propagate(new DefaultMuleException(createStaticMessage(SYNCHRONOUS_EVENT_ERROR_MESSAGE)));
        }
      };
    }

  }

}
