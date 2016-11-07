/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.jms;

import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;

import javax.jms.ConnectionFactory;

/**
 * //TODO
 */
@ArtifactClassLoaderRunnerConfig(exportPluginClasses = {ConnectionFactory.class})
public abstract class AbstractJmsTestCase extends MuleArtifactFunctionalTestCase {

}
