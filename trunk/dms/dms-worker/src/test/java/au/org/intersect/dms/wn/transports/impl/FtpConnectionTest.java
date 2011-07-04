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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.errors.ConnectionClosedError;
import au.org.intersect.dms.core.errors.PathNotFoundException;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.wn.TransportFactory;

@RunWith(MockitoJUnitRunner.class)
public class FtpConnectionTest
{
    private static final String SUCCESS_MESSAGE = "Method should return true";
    private static final String FAIL_MESSAGE = "Method should return false";
    
    @Mock
    private FTPClient client;
    
    @Mock
    private TransportFactory factory;

    @InjectMocks
    private FtpConnection connection = new FtpConnection();

    private String workingDirectory = "/test";
    
    private FileInfo file = new FileInfo();
    
    private FileInfo dir = new FileInfo();
    
    @Before
    public void setUp() throws Exception
    {   
        file = new FileInfo();
        file.setFileType(FileType.FILE);
        file.setAbsolutePath("/test.txt");
        
        dir = new FileInfo();
        dir.setFileType(FileType.DIRECTORY);
        dir.setAbsolutePath("/test");
    }
    
    @Test(expected = PathNotFoundException.class)
    public void changeWorkingDirectoryPathNotFound() throws IOException
    {
        when(client.changeWorkingDirectory(workingDirectory)).thenReturn(false);
        connection.rename(workingDirectory, "", "");
    }

    @Test(expected = ConnectionClosedError.class)
    public void changeWorkingDirectoryConnectionClosed() throws IOException
    {
        when(client.changeWorkingDirectory(workingDirectory)).thenThrow(new FTPConnectionClosedException());
        when(client.isConnected()).thenReturn(true);
        connection.rename(workingDirectory, "", "");
    }

    @Test(expected = TransportError.class)
    public void changeWorkingDirectoryIOException() throws IOException
    {
        when(client.changeWorkingDirectory(workingDirectory)).thenThrow(new IOException());
        connection.rename(workingDirectory, "", "");
    }

    @Test
    public void testRenameSuccess() throws IOException
    {
        String oldName = "testFile.txt";
        String newName = "testFile-renamed.txt";

        when(client.changeWorkingDirectory(workingDirectory)).thenReturn(true);
        when(client.rename(oldName, newName)).thenReturn(true);

        assertTrue(SUCCESS_MESSAGE, connection.rename(workingDirectory, oldName, newName));
    }
    
    @Test
    public void testRenameFail() throws IOException
    {
        String oldName = "testFile.txt";
        String newName = "testFile-renamed.txt";

        when(client.changeWorkingDirectory(workingDirectory)).thenReturn(true);
        when(client.rename(oldName, newName)).thenReturn(false);

        assertFalse(FAIL_MESSAGE, connection.rename(workingDirectory, oldName, newName));
    }
    
    @Test
    public void testDeleteFileSuccess() throws IOException
    {
        when(client.deleteFile(file.getAbsolutePath())).thenReturn(true);
        assertTrue(SUCCESS_MESSAGE, connection.delete(file));
    }
    
    @Test
    public void testDeleteDirectorySuccess() throws IOException
    {
        when(client.removeDirectory(dir.getAbsolutePath())).thenReturn(true);
        assertTrue(SUCCESS_MESSAGE, connection.delete(dir));
    }
    
    @Test
    public void createDirSuccess() throws IOException
    {
        String name = "testDir";

        when(client.changeWorkingDirectory(workingDirectory)).thenReturn(true);
        when(client.makeDirectory(name)).thenReturn(true);

        assertTrue(SUCCESS_MESSAGE, connection.createDir(workingDirectory, name));
    }
    
    @Test
    public void createDirFail() throws IOException
    {
        String name = "testDir";

        when(client.changeWorkingDirectory(workingDirectory)).thenReturn(true);
        when(client.makeDirectory(name)).thenReturn(false);

        assertFalse(FAIL_MESSAGE, connection.createDir(workingDirectory, name));
    }
}
