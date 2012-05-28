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

package au.org.intersect.dms.wn.transports.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.errors.TransportError;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocalConnection.class)
public class LocalConnectionTest
{
    
    private static final String ROOT_PATH = "/";
    private static final String A_PATH = "test";
    private static final String USERNAME = "username";

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testLocalConnectionOk() throws Exception
    {
        String path = A_PATH;
        File rootDir = mock(File.class);
        utilSetupForLocalConnectionTests(rootDir, path, true, true);

        new LocalConnection(rootDir, path, USERNAME);
    }

    @Test(expected = TransportError.class)
    public void testLocalConnectionNoPath() throws Exception
    {
        String path = A_PATH;
        File rootDir = mock(File.class);
        utilSetupForLocalConnectionTests(rootDir, path, false, false);

        new LocalConnection(rootDir, path, USERNAME);
    }

    @Test(expected = TransportError.class)
    public void testLocalConnectionNotDir() throws Exception
    {
        String path = A_PATH;
        File rootDir = mock(File.class);
        utilSetupForLocalConnectionTests(rootDir, path, false, true);

        new LocalConnection(rootDir, path, USERNAME);
    }

    private File utilSetupForLocalConnectionTests(File rootDir, String path, boolean isDir, boolean exists)
        throws Exception
    {
        File basedir = mock(File.class);
        whenNew(File.class).withParameterTypes(File.class, String.class)
                .withArguments(Matchers.eq(rootDir), Matchers.eq(path)).thenReturn(basedir);
        when(basedir.isDirectory()).thenReturn(isDir);
        when(basedir.exists()).thenReturn(exists);
        return basedir;
    }

    @Test
    public void testGetListEmptyRoot() throws Exception
    {
        String path = A_PATH;
        File rootDir = mock(File.class);
        File basedir = utilSetupForLocalConnectionTests(rootDir, path, true, true);
        when(basedir.isDirectory()).thenReturn(true);
        when(basedir.listFiles()).thenReturn(new File[]{});

        LocalConnection connection = new LocalConnection(rootDir, path, USERNAME);
        
        List<FileInfo> list = connection.getList(ROOT_PATH);
        assertTrue(list.size() == 0);
    }
    
    @Test
    public void testGetListTwoFiles() throws Exception
    {
        String path = A_PATH;
        File rootDir = mock(File.class);
        File basedir = utilSetupForLocalConnectionTests(rootDir, path, true, true);
        File[] files = utilMockFiles(new String[]{"get-one.txt", "get-two.txt"});
        when(basedir.isDirectory()).thenReturn(true);
        when(basedir.listFiles()).thenReturn(files);

        LocalConnection connection = new LocalConnection(rootDir, path, USERNAME);
        List<FileInfo> list = connection.getList(ROOT_PATH);
        assertEquals(2, list.size());
        for (int i = 0; i < list.size(); i++)
        {
            assertEquals(files[i].getName(), list.get(i).getName());
            assertEquals(ROOT_PATH + files[i].getName(), list.get(i).getAbsolutePath());
        }
    }
    
    @Test
    public void testRenameNormalFile() throws Exception
    {
        String path = A_PATH;
        File rootDir = mock(File.class);
        File basedir = utilSetupForLocalConnectionTests(rootDir, A_PATH, true, true);
        File[] files = utilMockFiles(new String[]{".", "..", "ren-one.txt", "ren-two.txt"});
        when(basedir.isDirectory()).thenReturn(true);
        when(basedir.listFiles()).thenReturn(files);
        
        File from = mock(File.class);
        whenNew(File.class).withParameterTypes(File.class, String.class)
        .withArguments(Matchers.eq(basedir), Matchers.eq("/ren-one.txt")).thenReturn(from);
        when(from.exists()).thenReturn(true);

        File to = mock(File.class);
        whenNew(File.class).withParameterTypes(File.class, String.class)
        .withArguments(Matchers.eq(basedir), Matchers.eq("/one-a.txt")).thenReturn(to);
        when(to.exists()).thenReturn(false);
        
        when(from.renameTo(to)).thenReturn(true);
        
        LocalConnection connection = new LocalConnection(rootDir, path, USERNAME);
        assertTrue(connection.rename(ROOT_PATH, "ren-one.txt", "one-a.txt"));
    }

    private File[] utilMockFiles(String[] names)
    {
        File[] resp = new File[names.length];
        int i = 0;
        for (String name : names)
        {
            File aMock = mock(File.class);
            when(aMock.exists()).thenReturn(true);
            when(aMock.getName()).thenReturn(names[i]);
            when(aMock.isDirectory()).thenReturn(i <= 1 ? true : name.indexOf('.') == 0);
            when(aMock.lastModified()).thenReturn(0L);
            resp[i] = aMock;
            i++;
        }
        return resp;
    }

    @Test
    public void testDelete()
    {
        // TODO fail("Not yet implemented");
    }

    @Test
    public void testCreate()
    {
        // TODO fail("Not yet implemented");
    }

}
