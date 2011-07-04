/**
 * Project: Platforms for Collaboration at the AMMRF
 *
 * Copyright (c) Intersect Pty Ltd, 2011
 *
 * @see http://www.ammrf.org.au
 * @see http://www.intersect.org.au
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * This program contains open source third party libraries from a number of
 * sources, please read the THIRD_PARTY.txt file for more details.
 */

package au.org.intersect.dms.instrument.ingest.filewatcher;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stops and removes camel route
 * 
 * @version $Rev: 5 $
 * 
 */
public class RouteStopper
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteStopper.class);

    public void stop(final Exchange exchange, final CamelContext context) throws Exception
    {
        (new Thread()
        {
            public void run()
            {
                String routeId = exchange.getFromRouteId();
                RouteDefinition routeDefinition = context.getRouteDefinition(routeId);
                LOGGER.debug("Shutting down route {} with ID {}", routeDefinition, routeId);
                Collection<RouteDefinition> routes2Remove = new LinkedList<RouteDefinition>();
                routes2Remove.add(routeDefinition);
                // TODO CHECKSTYLE-OFF: IllegalCatch
                try
                {
                    context.shutdownRoute(routeId);
                    context.removeRouteDefinitions(routes2Remove);
                }
                catch (Exception e)
                {
                    throw new RuntimeCamelException("Failed to stop route " + routeId , e);
                }
                // CHECKSTYLE-ON: IllegalCatch
            }
        }).start();
    }
}
