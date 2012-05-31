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

package au.org.intersect.dms.instrument.ingest;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unitils.reflectionassert.ReflectionAssert;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.dto.IngestParameter;

public class CellRIngestorTest
{
    private static final String SLASH = "/";
    private static final String COPY_FROM_LIST_ERROR_MESSAGE = "From list for copy to workstation job is not correct";
    private CellRIngestor ingestor = new CellRIngestor();
    private List<String> fromFiles;
    private IngestParameter ingestParams;
    private String fromDir = "/testSrc/test Source Dir";
    private String toDir = "/testDest/test Dest Dir";

    @Before
    public void setUp() throws Exception
    {
        fromFiles = new ArrayList<String>()
        {
            {
                add(fromDir);
                add(fromDir + "/somethig else");
            }
        };
        ingestParams = new IngestParameter("test", 1L, 1, fromFiles, 2, toDir, InstrumentProfile.OLYMPUS_CELL_R);
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void copyToWorkstationFromListIngestFromSomeDirectory()
    {
        List<String> fromList = ingestor.createCopyFromList(ingestParams);
        List<String> expectedList = new ArrayList<String>()
        {
            {
                add(toDir + "/test Source Dir");
            }
        };
        ReflectionAssert.assertReflectionEquals(COPY_FROM_LIST_ERROR_MESSAGE, expectedList, fromList);
    }

    @Test
    public void copyToWorkstationFromListIngestFromRoot()
    {
        ingestParams.setFromFiles(new ArrayList<String>()
        {
            {
                add(SLASH);
            }
        });
        List<String> fromList = ingestor.createCopyFromList(ingestParams);
        List<String> expectedList = new ArrayList<String>()
        {
            {
                add(toDir);
            }
        };
        ReflectionAssert.assertReflectionEquals(COPY_FROM_LIST_ERROR_MESSAGE, expectedList, fromList);
    }

    @Test
    public void copyToWorkstationFromListIngestToRoot()
    {
        ingestParams.setToDir(SLASH);

        List<String> fromList = ingestor.createCopyFromList(ingestParams);
        List<String> expectedList = new ArrayList<String>()
        {
            {
                add("/test Source Dir");
            }
        };
        ReflectionAssert.assertReflectionEquals(COPY_FROM_LIST_ERROR_MESSAGE, expectedList, fromList);
    }

}
