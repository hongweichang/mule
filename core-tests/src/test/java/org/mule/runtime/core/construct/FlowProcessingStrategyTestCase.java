/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.core.construct;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tck.util.MuleContextUtils.mockContextWithServices;

import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategy;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.runtime.core.processor.strategy.DefaultFlowProcessingStrategyFactory.DefaultFlowProcessingStrategy;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class FlowProcessingStrategyTestCase extends AbstractMuleTestCase {

  protected MuleContext muleContext = mockContextWithServices();

  @Mock
  private MuleConfiguration configuration;

  private AbstractPipeline flow;

  @Before
  public void before() throws RegistrationException {
    when(muleContext.getConfiguration()).thenReturn(configuration);
    createFlow();
  }

  @Test
  public void fixedProcessingStrategyIsHonoured() throws Exception {
    ProcessingStrategy processingStrategy = mock(ProcessingStrategy.class);
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    when(processingStrategyFactory.create(any(MuleContext.class))).thenReturn(processingStrategy);
    createFlow();
    flow.setProcessingStrategyFactory(processingStrategyFactory);
    flow.initialise();

    assertThat(flow.getProcessingStrategy(), is(sameInstance(processingStrategy)));
  }

  @Test
  public void defaultProcessingStrategyInConfigIsHonoured() throws Exception {
    ProcessingStrategy processingStrategy = mock(ProcessingStrategy.class);
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    when(processingStrategyFactory.create(any(MuleContext.class))).thenReturn(processingStrategy);
    when(configuration.getDefaultProcessingStrategyFactory()).thenReturn(processingStrategyFactory);

    createFlow();
    flow.initialise();
    assertThat(flow.getProcessingStrategy(), is(sameInstance(processingStrategy)));
  }

  @Test
  public void fixedProcessingStrategyTakesPrecedenceOverConfig() throws Exception {
    ProcessingStrategy configProcessingStrategy = mock(ProcessingStrategy.class);
    ProcessingStrategyFactory configProcessingStrategyFactory = mock(ProcessingStrategyFactory.class);
    when(configProcessingStrategyFactory.create(any(MuleContext.class))).thenReturn(configProcessingStrategy);
    when(configuration.getDefaultProcessingStrategyFactory()).thenReturn(configProcessingStrategyFactory);

    ProcessingStrategy processingStrategy = mock(ProcessingStrategy.class);
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    when(processingStrategyFactory.create(any(MuleContext.class))).thenReturn(processingStrategy);
    createFlow();
    flow.setProcessingStrategyFactory(processingStrategyFactory);
    flow.initialise();

    assertThat(flow.getProcessingStrategy(), is(sameInstance(processingStrategy)));
  }

  @Test
  public void createDefaultProcessingStrategyIfNoneSpecified() throws Exception {
    flow.initialise();
    assertThat(flow.getProcessingStrategy(), is(instanceOf(DefaultFlowProcessingStrategy.class)));
  }

  private void createFlow() {
    flow = new Flow("test", muleContext);
  }
}
