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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mule.tck.util.MuleContextUtils.mockContextWithServices;
import org.mule.runtime.core.api.MuleContext;
import org.mule.runtime.core.api.config.MuleConfiguration;
import org.mule.runtime.core.api.processor.strategy.ProcessingStrategyFactory;
import org.mule.runtime.core.api.registry.RegistrationException;
import org.mule.runtime.core.api.scheduler.SchedulerService;
import org.mule.runtime.core.processor.strategy.DefaultFlowProcessingStrategyFactory;
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

  @Mock
  private SchedulerService schedulerService;

  private AbstractPipeline flow;

  @Before
  public void before() throws RegistrationException {
    when(muleContext.getConfiguration()).thenReturn(configuration);
    createFlow();
  }

  @Test
  public void fixedProcessingStrategyIsHonoured() throws Exception {
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    createFlow();
    flow.setProcessingStrategyFactory(processingStrategyFactory);
    flow.initialise();

    assertThat(flow.getProcessingStrategyFactory(), is(sameInstance(processingStrategyFactory)));
  }

  @Test
  public void defaultProcessingStrategyInConfigIsHonoured() throws Exception {
    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    when(configuration.getDefaultProcessingStrategyFactory()).thenReturn(processingStrategyFactory);

    createFlow();
    flow.initialise();
    assertThat(flow.getProcessingStrategyFactory(), is(sameInstance(processingStrategyFactory)));
  }

  @Test
  public void fixedProcessingStrategyTakesPrecedenceOverConfig() throws Exception {
    ProcessingStrategyFactory configProcessingStrategyFactory = mock(ProcessingStrategyFactory.class);
    when(configuration.getDefaultProcessingStrategyFactory()).thenReturn(configProcessingStrategyFactory);

    ProcessingStrategyFactory processingStrategyFactory = mock(ProcessingStrategyFactory.class);
    createFlow();
    flow.setProcessingStrategyFactory(processingStrategyFactory);
    flow.initialise();

    assertThat(flow.getProcessingStrategyFactory(), is(sameInstance(processingStrategyFactory)));
  }

  @Test
  public void createDefaultProcessingStrategyIfNoneSpecified() throws Exception {
    flow.initialise();
    assertThat(flow.getProcessingStrategyFactory(), is(instanceOf(DefaultFlowProcessingStrategyFactory.class)));
  }

  private void createFlow() {
    flow = new Flow("test", muleContext);
  }
}
