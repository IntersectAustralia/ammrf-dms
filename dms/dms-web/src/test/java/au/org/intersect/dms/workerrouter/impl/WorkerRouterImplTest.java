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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.workerrouter.WorkerNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class WorkerRouterImplTest
{

    private static final String FTP = "ftp";

    private static final String LOCALHOST = "localhost";

    @Mock
    private CamelContext context;

    @InjectMocks
    private WorkerRouterImpl router = new WorkerRouterImpl();

    private WorkerNode[] proxies;

    @Before
    public void setUp() throws Exception
    {
        List<WorkerDefinition> definitions = new ArrayList<WorkerDefinition>();
        definitions.add(makeWorkerDefinition("queue.1"));
        definitions.add(makeWorkerDefinition("queue.2"));
        definitions.add(makeWorkerDefinition("queue.3"));
        definitions.add(makeWorkerDefinition("queue.4"));
        router.setRoutingTable(definitions);
        proxies = mockAndLink(router, definitions.size());
    }

    private WorkerDefinition makeWorkerDefinition(String queue)
    {
        List<WorkerRoute> routes = new ArrayList<WorkerRoute>();
        routes.add(new WorkerRoute(FTP, "*"));
        WorkerDefinition workerDef = new WorkerDefinition();
        workerDef.setQueue(queue);
        workerDef.setRoutes(routes);
        return workerDef;
    }

    private WorkerNode[] mockAndLink(WorkerRouterImpl router2, int size)
    {
        WorkerNode[] resp = new WorkerNode[size];
        for (int i = 0; i < size; i++)
        {
            WorkerNode proxy = mock(WorkerNode.class);
            when(proxy.openConnection(any(OpenConnectionParameter.class))).thenReturn((i + 1) * 3 + 10);
            ((WorkerNodeProxy) router.findWorkerById(i)).setProxy(proxy);
        }
        return resp;
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testFindRouterForFtp()
    {
        WorkerNode worker = router.findWorker(FTP, LOCALHOST);
        assertNotNull(worker);
        assertNotNull(worker.openConnection(new OpenConnectionParameter(FTP, LOCALHOST, "user", "password")));
    }

    @Test
    public void testFindRouterForUnknownProtocol()
    {
        try
        {
            router.findWorker("blah", LOCALHOST);
            fail("Should throw WorkerNotFoundException");
        }
        catch (Exception e)
        {
            assertTrue(e instanceof WorkerNotFoundException);
        }
    }

    @Test
    public void testWorkerConnectAndFindWorkerAgain()
    {
        WorkerNode worker = router.findWorker(FTP, LOCALHOST);
        assertNotNull(worker);
        Integer connectionId = worker
                .openConnection(new OpenConnectionParameter(FTP, LOCALHOST, "user", "password"));
        assertNotNull(connectionId);
        assertTrue(worker == router.findWorker(connectionId));
    }

}
