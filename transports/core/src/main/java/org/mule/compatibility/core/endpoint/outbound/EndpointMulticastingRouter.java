/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.compatibility.core.endpoint.outbound;

import org.mule.compatibility.core.api.endpoint.EndpointException;
import org.mule.compatibility.core.api.endpoint.EndpointURI;
import org.mule.compatibility.core.api.endpoint.ImmutableEndpoint;
import org.mule.compatibility.core.api.endpoint.OutboundEndpoint;
import org.mule.compatibility.core.endpoint.DynamicURIOutboundEndpoint;
import org.mule.compatibility.core.endpoint.MuleEndpointURI;
import org.mule.runtime.core.api.Event;
import org.mule.runtime.core.api.message.InternalMessage;
import org.mule.runtime.api.lifecycle.InitialisationException;
import org.mule.runtime.core.api.processor.Processor;
import org.mule.runtime.core.api.routing.CouldNotRouteOutboundMessageException;
import org.mule.runtime.core.config.i18n.CoreMessages;
import org.mule.runtime.core.routing.outbound.MulticastingRouter;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EndpointMulticastingRouter extends MulticastingRouter {

  private static final Logger logger = LoggerFactory.getLogger(MulticastingRouter.class);

  @Override
  protected Processor getTemplateRoute(int index, Event event) throws CouldNotRouteOutboundMessageException {
    InternalMessage message = event.getMessage();
    Processor mp = routes.get(index);
    if (!(mp instanceof ImmutableEndpoint)) {
      return routes.get(index);
    }
    OutboundEndpoint ep = (OutboundEndpoint) mp;
    String uri = ep.getAddress();

    if (logger.isDebugEnabled()) {
      logger.debug("Uri before parsing is: " + uri);
    }

    if (!parser.isContainsTemplate(uri)) {
      logger.debug("Uri does not contain template(s)");
      return ep;
    } else {
      Map<String, Object> props = new HashMap<>();
      // Also add the endpoint properties so that users can set fallback values
      // when the property is not set on the event
      props.putAll(ep.getProperties());
      for (String propertyKey : message.getOutboundPropertyNames()) {
        Object value = message.getOutboundProperty(propertyKey);
        props.put(propertyKey, value);
      }

      String newUriString = parser.parse(props, uri);
      if (parser.isContainsTemplate(newUriString)) {
        newUriString = this.getMuleContext().getExpressionLanguage().parse(newUriString, event, flowConstruct);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Uri after parsing is: " + uri);
      }
      try {
        EndpointURI newUri = new MuleEndpointURI(newUriString, muleContext);
        EndpointURI endpointURI = ep.getEndpointURI();
        if (endpointURI != null && !newUri.getScheme().equalsIgnoreCase(endpointURI.getScheme())) {
          throw new CouldNotRouteOutboundMessageException(CoreMessages
              .schemeCannotChangeForRouter(ep.getEndpointURI().getScheme(), newUri.getScheme()), ep);
        }
        newUri.initialise();

        return new DynamicURIOutboundEndpoint(ep, newUri);
      } catch (EndpointException e) {
        throw new CouldNotRouteOutboundMessageException(CoreMessages.templateCausedMalformedEndpoint(uri, newUriString),
                                                        ep, e);
      } catch (InitialisationException e) {
        throw new CouldNotRouteOutboundMessageException(CoreMessages.templateCausedMalformedEndpoint(uri, newUriString),
                                                        ep, e);
      }
    }
  }
}
