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

package au.org.intersect.dms.rifcs.impl;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import au.org.intersect.dms.rifcs.RifcsMarshallService;

public class RifcsMarshallServiceImplTest
{
    private RifcsMarshallService service = new RifcsMarshallServiceImpl();

    @Test
    public void testUnmarshallRegistryObject()
    {
        assertNotNull(service.unmarshallRegistryObjects(readFile("/registry-object-1.xml")));
    }

    private String readFile(String path)
    {
        try
        {
            InputStream in = getClass().getResourceAsStream(path);
            byte[] b = new byte[1024];
            StringBuffer sb = new StringBuffer();
            int result = in.read(b);
            while (result != -1)
            {
                sb.append(new String(b, 0, result));
                result = in.read(b);
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

}
