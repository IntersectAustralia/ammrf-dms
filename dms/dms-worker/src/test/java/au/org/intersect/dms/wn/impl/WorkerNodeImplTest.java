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

package au.org.intersect.dms.wn.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool.KeyedObjectPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.CreateDirectoryParameter;
import au.org.intersect.dms.core.service.dto.DeleteParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.core.service.dto.RenameParameter;
import au.org.intersect.dms.wn.CacheWrapper;
import au.org.intersect.dms.wn.ConnectionParams;
import au.org.intersect.dms.wn.TransportConnection;

@RunWith(MockitoJUnitRunner.class)
public class WorkerNodeImplTest
{
    private static final String SUCCESS_MESSAGE = "Method should return true";
    private static final String FAIL_MESSAGE = "Method should return false";

    @Mock
    private KeyedObjectPool pool;

    @Mock
    private Map<String, KeyedObjectPool> protoMapping;

    @Mock
    private CacheWrapper<Integer, ConnectionParams> activeConnectionsCache;

    @Mock
    private TransportConnection connection;

    @InjectMocks
    private WorkerNodeImpl workerNodeImpl = new WorkerNodeImpl();

    private String protocol = "ftp";
    private Integer connectionId = 123;
    private String directory = "/testDir";
    private String fromRelative = "test.txt";
    private String fromAbsolute = directory + "/" + fromRelative;
    private String to = "test-renamed.txt";

    private ConnectionParams key;

    private FileInfo fileFileInfo = new FileInfo();
    private FileInfo dirFileInfo = new FileInfo();
    private List<FileInfo> files = new ArrayList<FileInfo>();

    @Before
    public void setUp() throws Exception
    {
        fileFileInfo = new FileInfo();
        fileFileInfo.setFileType(FileType.FILE);
        fileFileInfo.setAbsolutePath(fromAbsolute);
        files.add(fileFileInfo);

        dirFileInfo = new FileInfo();
        dirFileInfo.setFileType(FileType.DIRECTORY);
        dirFileInfo.setAbsolutePath(directory);

        key = new ConnectionParams(protocol, "server", "username", "password");

        when(protoMapping.get(protocol)).thenReturn(pool);
        when(activeConnectionsCache.get(connectionId)).thenReturn(key);
        when(pool.borrowObject(key)).thenReturn(connection);
        workerNodeImpl.setProtoMapping(protoMapping);
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testGetConnectionDetails()
    {
        OpenConnectionParameter connParam = new OpenConnectionParameter(key.getProtocol(), key.getHostname(),
                key.getUsername(), key.getPassword());
        Integer connId = workerNodeImpl.openConnection(connParam);
        when(activeConnectionsCache.get(connId)).thenReturn(key);
        OpenConnectionParameter connParam2 = workerNodeImpl.getConnectionDetails(connId);
        assertEquals(connParam.getProtocol(), connParam2.getProtocol());
        assertEquals(connParam.getServer(), connParam2.getServer());
        assertEquals(connParam.getUsername(), connParam2.getUsername());
        assertEquals(connParam.getPassword(), connParam2.getPassword());
    }

    @Test
    public void testLogin()
    {
        assertNotNull(workerNodeImpl.openConnection(new OpenConnectionParameter(key.getProtocol(), key.getHostname(),
                key.getUsername(), key.getPassword())));
    }

    @Test
    public void testPotentialReuseTransport()
    {
        Integer conn1 = workerNodeImpl.openConnection(new OpenConnectionParameter(key.getProtocol(), key.getHostname(),
                key.getUsername(), key.getPassword()));
        Integer conn2 = workerNodeImpl.openConnection(new OpenConnectionParameter(key.getProtocol(), key.getHostname(),
                key.getUsername(), key.getPassword()));
        // the connections are different, but the used key in the pool is the same
        assertTrue(conn1.intValue() != conn2.intValue());
        assertEquals(activeConnectionsCache.get(conn1), activeConnectionsCache.get(conn2));
    }

    @Test
    public void testRenameSuccess() throws IOException
    {
        when(connection.rename(directory, fromRelative, to)).thenReturn(true);
        assertTrue(SUCCESS_MESSAGE, workerNodeImpl.rename(new RenameParameter(connectionId, fromAbsolute, to)));
    }

    @Test
    public void testRenameFail() throws IOException
    {
        when(connection.rename(directory, fromRelative, to)).thenReturn(false);
        assertFalse(FAIL_MESSAGE, workerNodeImpl.rename(new RenameParameter(connectionId, fromAbsolute, to)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRenameWrongRootDir()
    {
        String from = "testDir/test.txt";
        workerNodeImpl.rename(new RenameParameter(connectionId, from, to));
    }

    @Test
    public void testDeleteFileSuccess() throws IOException
    {
        when(connection.delete(fileFileInfo)).thenReturn(true);
        assertTrue(SUCCESS_MESSAGE, workerNodeImpl.delete(new DeleteParameter(connectionId, files)));
    }

    @Test
    public void testDeleteFileFail() throws IOException
    {
        when(connection.delete(fileFileInfo)).thenReturn(false);
        assertFalse(FAIL_MESSAGE, workerNodeImpl.delete(new DeleteParameter(connectionId, files)));
    }

    @Test
    public void testDeleteEmptyDirSuccess() throws IOException
    {
        files.clear();
        files.add(dirFileInfo);
        when(connection.delete(dirFileInfo)).thenReturn(true);
        when(connection.getList(dirFileInfo.getAbsolutePath())).thenReturn(new ArrayList<FileInfo>());
        assertTrue(SUCCESS_MESSAGE, workerNodeImpl.delete(new DeleteParameter(connectionId, files)));
    }

    @Test
    public void testDeleteNonEmptyDirSuccess() throws IOException
    {
        List<FileInfo> toDelete = new ArrayList<FileInfo>();
        toDelete.add(dirFileInfo);

        when(connection.delete(files.get(0))).thenReturn(true);
        when(connection.delete(dirFileInfo)).thenReturn(true);
        when(connection.getList(dirFileInfo.getAbsolutePath())).thenReturn(files);
        assertTrue(SUCCESS_MESSAGE, workerNodeImpl.delete(new DeleteParameter(connectionId, toDelete)));
    }

    @Test
    public void testDeleteNonEmptyDirFail() throws IOException
    {
        List<FileInfo> toDelete = new ArrayList<FileInfo>();
        toDelete.add(dirFileInfo);

        when(connection.delete(files.get(0))).thenReturn(true);
        when(connection.delete(dirFileInfo)).thenReturn(false);
        when(connection.getList(dirFileInfo.getAbsolutePath())).thenReturn(files);
        assertFalse(FAIL_MESSAGE, workerNodeImpl.delete(new DeleteParameter(connectionId, toDelete)));
    }

    @Test
    public void createSuccess() throws IOException
    {
        String name = "newDir";
        when(connection.createDir(directory, name)).thenReturn(true);
        assertTrue(SUCCESS_MESSAGE,
                workerNodeImpl.createDir(new CreateDirectoryParameter(connectionId, directory, name)));
    }

    @Test
    public void createFail() throws IOException
    {
        String name = "newDir";
        when(connection.createDir(directory, name)).thenReturn(false);
        boolean created = workerNodeImpl.createDir(new CreateDirectoryParameter(connectionId, directory, name));
        assertFalse(FAIL_MESSAGE, created);
    }

    @Test(expected = ConnectionClosedError.class)
    public void withConnectionNullKey()
    {
        assertFalse(FAIL_MESSAGE, workerNodeImpl.rename(new RenameParameter(1, fromAbsolute, to)));
    }

    @Test
    public void testConnectionReturnsToPool() throws Exception
    {
        when(connection.rename(directory, fromRelative, to)).thenReturn(true);
        workerNodeImpl.rename(new RenameParameter(connectionId, fromAbsolute, to));
        verify(pool).returnObject(key, connection);
    }

    @Test
    public void withConnectionTransportError() throws Exception
    {
        when(connection.rename(directory, fromRelative, to)).thenThrow(new IOException());
        try
        {
            workerNodeImpl.rename(new RenameParameter(connectionId, fromAbsolute, to));
            fail("TransportError expected");
        }
        catch (TransportError e)
        {
            verify(pool).invalidateObject(key, connection);
        }
    }

    @Test
    public void getConnectionDetailsSuccess()
    {
        Integer connectionId = 1000;
        OpenConnectionParameter expectedConnectionParams = new OpenConnectionParameter("ftp", "localhost", "test-user",
                "password");
        when(activeConnectionsCache.get(connectionId)).thenReturn(
                new ConnectionParams(expectedConnectionParams.getProtocol(), expectedConnectionParams.getServer(),
                        expectedConnectionParams.getUsername(), expectedConnectionParams.getPassword()));
        OpenConnectionParameter connectionParams = workerNodeImpl.getConnectionDetails(connectionId);
        assertReflectionEquals(expectedConnectionParams, connectionParams);
    }

    @Test(expected = ConnectionClosedError.class)
    public void getConnectionDetailsForNotActiveConnection()
    {
        Integer connectionId = 0;
        when(activeConnectionsCache.get(connectionId)).thenReturn(null);
        workerNodeImpl.getConnectionDetails(connectionId);
    }

    @Test
    public void getConnectionDetails4Hdd()
    {
        Integer connectionId = WorkerNode.HDD_CONNECTION_ID;
        OpenConnectionParameter connectionParams = workerNodeImpl.getConnectionDetails(connectionId);
        OpenConnectionParameter expectedConnectionParams = new OpenConnectionParameter("file", "/", null, null);
        verify(activeConnectionsCache, never()).get(connectionId);
        assertReflectionEquals(expectedConnectionParams, connectionParams);
    }
}
