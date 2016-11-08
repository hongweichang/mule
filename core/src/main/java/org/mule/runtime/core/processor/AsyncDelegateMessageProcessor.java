/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.processor;

import static java.util.Collections.singletonList;
import static org.mule.runtime.core.MessageExchangePattern.ONE_WAY;
import static org.mule.runtime.core.api.Event.setCurrentEvent;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.startIfNeeded;
import static org.mule.runtime.core.api.lifecycle.LifecycleUtils.stopIfNeeded;
import static org.mule.runtime.core.config.i18n.CoreMessages.asyncDoesNotSupportTransactions;
import static org.mule.runtime.core.config.i18n.CoreMessages.objectIsNull;
import static org.mule.runtime.core.util.rx.Exceptions.checkedConsumer;
import static org.mule.runtime.core.util.rx.Exceptions.rxExceptionToMuleException;
import static reactor.core.publisher.Flux.from;
import static reactor.core.publisher.Flux.just;
import static reactor.core.scheduler.Schedulers.fromExecutorService;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.construct.FlowConstruct;
import org.mule.runtime.core.api.construct.Pipeline;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.lifecycle.Startable;
import org.mule.runtime.core.api.lifecycle.Stoppable;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.api.processor.MessageProcessorChain;
import org.mule.runtime.core.api.processor.MessageProcessorPathElement;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.runtime.core.api.routing.RoutingException;
import org.mule.runtime.core.api.scheduler.SchedulerService;
import org.mule.runtime.core.processor.strategy.LegacyAsynchronousProcessingStrategyFactory;
import org.mule.runtime.core.util.NotificationUtils;
import org.mule.runtime.core.work.MuleWorkManager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Processes {@link Event}'s asynchronously using a {@link MuleWorkManager} to schedule asynchronous processing of
 * MessageProcessor delegate configured the next {@link Processor}. The next {@link Processor} is therefore be executed in a
 * different thread regardless of the exchange-pattern configured on the inbound endpoint. If a transaction is present then an
 * exception is thrown.
 */
public class AsyncDelegateMessageProcessor extends AbstractMessageProcessorOwner
    implements Processor, Initialisable, Startable, Stoppable {

  protected Logger logger = LoggerFactory.getLogger(getClass());
  private AtomicBoolean consumablePayloadWarned = new AtomicBoolean(false);

  protected MessageProcessorChain delegate;

  protected ProcessingStrategyFactory processingStrategyFactory = new LegacyAsynchronousProcessingStrategyFactory();
  protected ProcessingStrategy processingStrategy;
  protected String name;

  public AsyncDelegateMessageProcessor(MessageProcessorChain delegate) {
    this.delegate = delegate;
  }

  public AsyncDelegateMessageProcessor(MessageProcessorChain delegate,
                                       ProcessingStrategyFactory processingStrategyFactory,
                                       String name) {
    this.delegate = delegate;
    this.processingStrategyFactory = processingStrategyFactory;
    this.name = name;
  }

  @Override
  public void initialise() throws InitialisationException {
    if (delegate == null) {
      throw new InitialisationException(objectIsNull("delegate message processor"), this);
    }
    if (processingStrategyFactory == null) {
      throw new InitialisationException(objectIsNull("processingStrategy"), this);
    }
    processingStrategy = processingStrategyFactory.create(muleContext);
    super.initialise();
  }

  @Override
  public void start() throws MuleException {
    startIfNeeded(processingStrategy);
    super.start();
  }

  @Override
  public void stop() throws MuleException {
    stopIfNeeded(processingStrategy);
    super.stop();
  }

  @Override
  public Event process(Event event) throws MuleException {
    try {
      return Mono.just(event).transform(this).block();
    } catch (Throwable e) {
      throw rxExceptionToMuleException(e);
    }
  }

  private void assertNotTransactional(Event event) throws RoutingException {
    if (event.isTransacted()) {
      throw new RoutingException(asyncDoesNotSupportTransactions(), delegate);
    }
  }

  @Override
  public Publisher<Event> apply(Publisher<Event> publisher) {
    return from(publisher)
        .doOnNext(checkedConsumer(event -> assertNotTransactional(event)))
        .doOnNext(event -> warnConsumablePayload(event.getMessage()))
        .doOnNext(request -> {
          just(request).map(event -> updateEventForAsync(event))
              .transform(
                         flowConstruct instanceof Pipeline ? processingStrategy.onPipeline((Pipeline) flowConstruct, delegate)
                             : delegate)
              .subscribe();
        });
  }

  private Event updateEventForAsync(Event event) {
    // Clone event, make it async and remove ReplyToHandler
    Event newEvent = Event.builder(event).synchronous(false).exchangePattern(ONE_WAY).replyToHandler(null).build();
    // Update RequestContext ThreadLocal for backwards compatibility
    setCurrentEvent(newEvent);
    return newEvent;
  }

  private void warnConsumablePayload(InternalMessage message) {
    if (consumablePayloadWarned.compareAndSet(false, true) && message.getPayload().getDataType().isStreamType()) {
      logger.warn(String.format("Using 'async' router with consumable payload (%s) may lead to unexpected results." +
          " Please ensure that only one of the branches actually consumes the payload, or transform it by using an <object-to-byte-array-transformer>.",
                                message.getPayload().getValue().getClass().getName()));
    }
  }

  @Override
  protected List<Processor> getOwnedMessageProcessors() {
    return singletonList(delegate);
  }

  public ProcessingStrategy getProcessingStrategy() {
    return processingStrategy;
  }

  @Override
  public void addMessageProcessorPathElements(MessageProcessorPathElement pathElement) {
    NotificationUtils.addMessageProcessorPathElements(delegate, pathElement.addChild(this));
  }

}
