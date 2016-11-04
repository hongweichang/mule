/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor.strategy;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mule.runtime.core.context.notification.AsyncMessageNotification.PROCESS_ASYNC_COMPLETE;
import static org.mule.runtime.core.context.notification.AsyncMessageNotification.PROCESS_ASYNC_SCHEDULED;
import static reactor.core.publisher.Flux.from;
import static reactor.core.publisher.Flux.just;
import static reactor.core.scheduler.Schedulers.fromExecutorService;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.construct.Pipeline;
import org.mule.runtime.core.api.lifecycle.Startable;
import org.mule.runtime.core.api.lifecycle.Stoppable;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.runtime.core.api.scheduler.Scheduler;
import org.mule.runtime.core.api.scheduler.SchedulerService;
import org.mule.runtime.core.context.notification.AsyncMessageNotification;
import org.mule.runtime.core.exception.MessagingException;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.resource.spi.work.WorkManager;

import org.reactivestreams.Publisher;

/**
 * This factory's strategy uses a {@link WorkManager} to schedule the processing of the pipeline of message processors in a single
 * worker thread.
 */
public class AsynchronousProcessingStrategyFactory implements ProcessingStrategyFactory {

  @Override
  public ProcessingStrategy create(MuleContext muleContext) {
    return new AsynchronousProcessingStrategy(() -> {
      try {
        return muleContext.getRegistry().lookupObject(SchedulerService.class).ioScheduler();
      } catch (RegistrationException e) {
        throw new MuleRuntimeException(e);
      }
    }, scheduler -> scheduler.stop(muleContext.getConfiguration().getShutdownTimeout(), MILLISECONDS), muleContext);
  }

  static class AsynchronousProcessingStrategy implements ProcessingStrategy, Startable, Stoppable {

    protected ProcessingStrategy synchronousProcessingStrategy;

    private Supplier<Scheduler> schedulerSupplier;
    private Consumer<Scheduler> schedulerStopper;
    private Scheduler scheduler;
    private MuleContext muleContext;

    public AsynchronousProcessingStrategy(Supplier<Scheduler> schedulerSupplier, Consumer<Scheduler> schedulerStopper,
                                          MuleContext muleContext) {
      this.schedulerSupplier = schedulerSupplier;
      this.schedulerStopper = schedulerStopper;
      this.muleContext = muleContext;
    }

    @Override
    public void start() throws MuleException {
      this.scheduler = schedulerSupplier.get();
    }

    public Function<Publisher<Event>, Publisher<Event>> onPipeline(Pipeline pipeline,
                                                                   Function<Publisher<Event>, Publisher<Event>> publisherFunction) {

      // Conserve existing 3.x async processing strategy behaviuor:
      // i) The request event is echoed rather than the the result of async processing returned
      // ii) Any exceptions that occur due to async processing are not propagated upwards
      return publisher -> from(publisher).concatMap(request -> just(request)
          .doOnNext(event -> fireAsyncScheduledNotification(event, pipeline))
          .publishOn(fromExecutorService(scheduler))
          .transform(publisherFunction)
          .map(response -> request)
          .doOnNext(event -> fireAsyncCompleteNotification(event, pipeline, null))
          .doOnError(MessagingException.class, e -> fireAsyncCompleteNotification(request, pipeline, e))
          .onErrorResumeWith(MessagingException.class, pipeline.getExceptionListener())
          .onErrorReturn(MessagingException.class, request));
    }

    @Override
    public void stop() throws MuleException {
      if (scheduler != null) {
        schedulerStopper.accept(scheduler);
      }
    }

    protected void fireAsyncScheduledNotification(Event event, Pipeline pipeline) {
      muleContext.getNotificationManager()
          .fireNotification(new AsyncMessageNotification(pipeline, event, null, PROCESS_ASYNC_SCHEDULED));
    }

    protected void fireAsyncCompleteNotification(Event event, Pipeline pipeline, MessagingException exception) {
      // Async completed notification uses same event instance as async listener
      muleContext.getNotificationManager()
          .fireNotification(new AsyncMessageNotification(pipeline, event, null, PROCESS_ASYNC_COMPLETE, exception));
    }

    protected Scheduler getScheduler() {
      return this.scheduler;
    }
  }
}
