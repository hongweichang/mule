/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.http.policy.internal;

import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.lifecycle.Initialisable;
import org.mule.runtime.core.api.lifecycle.InitialisationException;
import org.mule.runtime.core.policy.OperationPolicyInstance;
import org.mule.runtime.core.policy.Policy;
import org.mule.runtime.dsl.api.component.ComponentIdentifier;

import java.util.HashMap;
import java.util.Map;

public class HttpProxyPolicy implements Policy, Initialisable
{

    private HttpRequest request;
    private HttpSource source;

    private Map<String, HttpOperationPolicyInstance> policiesInstances = new HashMap<>();

    public HttpRequest getRequest()
    {
        return request;
    }

    public void setRequest(HttpRequest request)
    {
        this.request = request;
    }

    public HttpSource getSource()
    {
        return source;
    }

    public void setSource(HttpSource source)
    {
        this.source = source;
    }

    @Override
    public OperationPolicyInstance createOperationPolicyInstance(String id, ComponentIdentifier sourceIdentifie )
    {
        return policiesInstances.get(id).createOperationPolicyInstance();
    }

    @Override
    public OperationPolicyInstance createSourcePolicyInstance(String id, final ComponentIdentifier sourceIdentifier)
    {
        HttpOperationPolicyInstance httpOperationPolicyInstance = new HttpOperationPolicyInstance();
        policiesInstances.put(id, httpOperationPolicyInstance);
        return httpOperationPolicyInstance.createSourcePolicyInstance();
    }

    //TODO see when this must be called
    public void clear(String id) {
        policiesInstances.remove(id);
    }

    @Override
    public void initialise() throws InitialisationException
    {
        request.initialise();
        source.initialise();
    }

    private class HttpOperationPolicyInstance {

        private Event lastEvent;

        public OperationPolicyInstance createOperationPolicyInstance()
        {
            return (event, nextOperation) -> {
                lastEvent = request.nextOperation(event.getContext().getId(), (beforeExecuteNextEvent) -> lastEvent = beforeExecuteNextEvent, nextOperation).execute(Event.builder(lastEvent).message(event.getMessage()).build());
                return lastEvent;
            };
        }

        public OperationPolicyInstance createSourcePolicyInstance() {
            return (event,nextOperation) -> {
                lastEvent = source.nextOperation(event.getContext().getId(), (beforeExecuteNextEvent) -> lastEvent = beforeExecuteNextEvent, nextOperation).execute(event);
                return lastEvent;
            };
        }

    }
}
