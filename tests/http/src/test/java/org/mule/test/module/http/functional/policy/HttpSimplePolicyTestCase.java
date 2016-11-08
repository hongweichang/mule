/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.module.http.functional.policy;

import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Error;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.core.functional.Either;
import org.mule.runtime.core.util.IOUtils;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.test.module.http.functional.AbstractHttpTestCase;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Optional;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class HttpSimplePolicyTestCase extends AbstractHttpTestCase
{

    @Rule
    public DynamicPort proxyPort = new DynamicPort("proxyPort");

    @Rule
    public DynamicPort httpPort = new DynamicPort("httpPort");

    @Override
    protected String getConfigFile()
    {
        return "http-policy-config.xml";
    }

    @Test
    public void test() throws MuleException
    {
        Either<Error, InternalMessage> responseEither = muleContext.getClient().send("http://localhost:" + proxyPort.getNumber() + "/", "", new HashMap<String, Serializable>(), 999999);
        Assert.assertThat(IOUtils.toString((InputStream) responseEither.getRight().getPayload().getValue()), Is.is("newValue"));
    }
}
