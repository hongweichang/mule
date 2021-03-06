/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tck.testmodels.mule;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.core.api.agent.Agent;
import org.mule.runtime.core.api.lifecycle.InitialisationException;

import java.util.Collections;
import java.util.List;

/**
 * Mock agent
 */
public class TestAgent implements Agent {

  private String frobbit;

  public String getName() {
    return "Test Agent";
  }

  public void setName(String name) {
    // nothing to do
  }

  public String getDescription() {
    return "Test JMX Agent";
  }

  public void initialise() throws InitialisationException {
    // nothing to do
  }

  public void start() throws MuleException {
    // nothing to do
  }

  public void stop() throws MuleException {
    // nothing to do
  }

  public void dispose() {
    // nothing to do
  }

  public List<Class<? extends Agent>> getDependentAgents() {
    return Collections.emptyList();
  }

  public String getFrobbit() {
    return frobbit;
  }

  public void setFrobbit(String frobbit) {
    this.frobbit = frobbit;
  }
}
