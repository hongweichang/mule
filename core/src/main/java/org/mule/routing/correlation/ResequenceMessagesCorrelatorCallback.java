/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.routing.correlation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.routing.AggregationException;
import org.mule.routing.EventGroup;

import java.util.Arrays;
import java.util.Comparator;

/**
 * A Correlator that correlates messages based on Mule correlation settings
 * Note that the {@link #aggregateEvents(org.mule.routing.EventGroup)} method only resequences the events and
 * returns an MuleEvent[] wrapped in a MuleMessage impl.  This means that this callback can ONLY be used with a
 * {@link org.mule.routing.inbound.CorrelationEventResequencer}
 */
public class ResequenceMessagesCorrelatorCallback extends CollectionCorrelatorCallback
{
    /**
     * logger used by this class
     */
    protected transient final Log logger = LogFactory.getLog(ResequenceMessagesCorrelatorCallback.class);

    protected Comparator eventComparator;
    protected MuleContext muleContext;

    public ResequenceMessagesCorrelatorCallback(Comparator eventComparator, MuleContext muleContext)
    {
        super(muleContext);
        this.eventComparator = eventComparator;
        this.muleContext = muleContext;
    }

    /**
     * This method is invoked if the shouldAggregate method is called and returns
     * true. Once this method returns an aggregated message, the event group is
     * removed from the router.
     *
     * @param events the event group for this request
     * @return an aggregated message
     * @throws org.mule.routing.AggregationException
     *          if the aggregation fails. in this scenario the
     *          whole event group is removed and passed to the exception handler
     *          for this componenet
     */
    public MuleMessage aggregateEvents(EventGroup events) throws AggregationException
    {
        MuleEvent[] results = (events == null) ? new MuleEvent[0] : events.toArray();
        Arrays.sort(results, eventComparator);
        //This is a bit of a hack since we return a collection of events on one message
        return new DefaultMuleMessage(results, muleContext);
    }

}