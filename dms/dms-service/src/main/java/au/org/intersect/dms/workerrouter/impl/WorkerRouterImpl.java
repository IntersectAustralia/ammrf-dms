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

package au.org.intersect.dms.workerrouter.impl;

import static org.apache.commons.collections.CollectionUtils.intersection;
import static org.apache.commons.collections.CollectionUtils.select;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.bean.ProxyHelper;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.workerrouter.WorkerNotFoundException;
import au.org.intersect.dms.workerrouter.WorkerRouter;

/**
 * Router implementation.
 * 
 * @version $Rev: 29 $
 */
public class WorkerRouterImpl implements WorkerRouter
{
    private static final String ROUTING_FILENAME = "dms_routing.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerRouterImpl.class);

    private static final Random RNG = new Random();

    private static final int REPLY_THREADS_SIZE = 10;

    private WorkerNodeProxy[] workers;

    @Autowired
    @Qualifier("serviceCamelContext")
    private CamelContext context;

    @Required
    public void setRoutingTableLocation(String fname) throws IOException
    {
        List<WorkerDefinition> definitions = new WorkerRoutesLoader().loadRoutesDefinitions(fname + "/"
                + ROUTING_FILENAME);
        setRoutingTable(definitions);
    }

    void setRoutingTable(List<WorkerDefinition> definitions)
    {
        workers = new WorkerNodeProxy[definitions.size()];
        int i = 0;
        for (WorkerDefinition definition : definitions)
        {
            WorkerNodeProxy facade = new WorkerNodeProxy(i, definition.getRoutes(), definition.getQueue());
            createRouteAndSetProxy(facade, definition.getQueue());
            workers[i++] = facade;
        }
    }

    private void createRouteAndSetProxy(final WorkerNodeProxy facade, final String queue)
    {
        try
        {
            context.addRoutes(new RouteBuilder()
            {

                @Override
                public void configure() throws Exception
                {
                    Endpoint endpoint = context.getEndpoint("direct:worker" + facade.getWorkerId());
                    WorkerNode proxy = ProxyHelper.createProxy(endpoint, WorkerNode.class);
                    from(endpoint).errorHandler(defaultErrorHandler().logStackTrace(false)).threads(REPLY_THREADS_SIZE)
                            .to("activemq:" + queue + "?transferException=true");
                    facade.setProxy(proxy);
                }

            });
        }
        // TODO CHECKSTYLE-OFF: IllegalCatch
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        // CHECKSTYLE-ON: IllegalCatch
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorkerNode findWorker(final String protocol, final String server)
    {
        return pickOne(select(Arrays.asList(workers), new Predicate()
        {

            @Override
            public boolean evaluate(Object object)
            {
                return ((WorkerNodeProxy) object).canHandle(protocol, server);
            }
        }));
    }

    @Override
    public WorkerNode findWorker(Integer connectionId)
    {
        return findWorkerById(getWorkerId(connectionId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public WorkerNode findCommonWorker(final String protocol, final String server, final String protocol2,
            final String server2)
    {
        return pickOne(intersection(select(Arrays.asList(workers), new Predicate()
        {

            @Override
            public boolean evaluate(Object object)
            {
                return ((WorkerNodeProxy) object).canHandle(protocol, server);
            }
        }), select(Arrays.asList(workers), new Predicate()
        {

            @Override
            public boolean evaluate(Object object)
            {
                return ((WorkerNodeProxy) object).canHandle(protocol2, server2);
            }
        })));
    }

    @Override
    public int getWorkerId(Integer connection)
    {
        return WorkerNodeProxy.getWorkerId(connection);
    }

    @Override
    public WorkerNode findWorkerById(int workerId)
    {
        return workers[workerId];
    }

    private WorkerNode pickOne(Collection<WorkerNodeProxy> coll)
    {
        WorkerNodeProxy[] arr = (WorkerNodeProxy[]) coll.toArray(new WorkerNodeProxy[coll.size()]);
        if (arr.length == 0)
        {
            throw new WorkerNotFoundException("Worker not found");
        }
        int p = RNG.nextInt(arr.length);
        LOGGER.info("pickOne: " + arr[p]);
        return arr[p];
    }

}
