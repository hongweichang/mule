/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.extension.http.api.policy;

import org.mule.extension.http.api.HttpRequestAttributes;
import org.mule.extension.http.api.HttpResponseAttributes;
import org.mule.extension.http.api.listener.builder.HttpListenerResponseBuilder;
import org.mule.extension.http.api.listener.builder.HttpListenerSuccessResponseBuilder;
import org.mule.extension.http.api.request.builder.HttpRequesterRequestBuilder;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.core.api.policy.PolicyOperationParametersTransformer;
import org.mule.runtime.core.model.ParameterMap;
import org.mule.runtime.dsl.api.component.ComponentIdentifier;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class HttpPolicyRequestParametersTransformer implements PolicyOperationParametersTransformer
{

    @Override
    public boolean supports(ComponentIdentifier componentIdentifier)
    {
        //TODO add support for namespace
        return componentIdentifier.getName().equals("request");
    }

    @Override
    public Message fromParametersToMessage(Map<String, Object> parameters)
    {
        HttpRequesterRequestBuilder requestBuilder = (HttpRequesterRequestBuilder) parameters.get("requestBuilder");
        //TODO create a different HttpRequestAttributes with a sub-set of the supported attributes
        return Message.builder().payload("empty body").attributes(new HttpRequestAttributes(new ParameterMap(requestBuilder.getHeaders()),
                                                                                                        null, null, null, null, null, null, null, null,
                                                                                            new ParameterMap(requestBuilder.getQueryParams()),
                                                                                            new ParameterMap(requestBuilder.getQueryParams()), null, null)).build();
    }

    //TODO this method is not required for sources, we need two types of PolicyOperationParametersTransformer
    @Override
    public Map<String, Object> fromMessageToParameters(Message message)
    {
        HttpRequestAttributes requestAttributes = (HttpRequestAttributes) message.getAttributes();
        HttpRequesterRequestBuilder httpRequesterRequestBuilder = new HttpRequesterRequestBuilder();
        //TODO this fails because httpRequesterRequestBuilder doesn't support a list as values
        //httpRequesterRequestBuilder.getHeaders().putAll(requestAttributes.getHeaders());
        httpRequesterRequestBuilder.getQueryParams().putAll(requestAttributes.getQueryParams());
        httpRequesterRequestBuilder.getUriParams().putAll(requestAttributes.getUriParams());
        return ImmutableMap.<String, Object>builder().put("requestBuilder", httpRequesterRequestBuilder).build();
    }
}
