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

package au.org.intersect.dms.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.IngestParameter;

public class DmsServiceTester
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DmsServiceTester.class);

    private static final String[] CONFIG_FILES = {"classpath*:META-INF/spring/applicationContext-*.xml"};

    public static void main(String[] args)
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_FILES);
        context.registerShutdownHook();
        context.start();

        try
        {
            DmsService dmsService = context.getBean("dmsClient", DmsService.class);

            Integer sourceConnectionId = dmsService.openConnection("local", "microCT", "dms", "password");
            // Integer sourceConnectionId = dmsService.openConnection("ftp", "localhost", "ftpuser1", "password");
            LOGGER.debug("Source connection {}", sourceConnectionId);

            Integer destConnectionId = dmsService.openConnection("ftp", "localhost", "ftpuser", "password");
            LOGGER.debug("Dest connection {}", destConnectionId);
            IngestParameter ingestParameters = new IngestParameter("andrey", null, sourceConnectionId, "/C3-01",
                    destConnectionId, "/", InstrumentProfile.MICRO_CT);
            Long jobId = dmsService.ingest("andrey", null, null, ingestParameters);
            LOGGER.debug("Ingest job {}", jobId);
            // jobId = dmsService.ingest("carlosayam", sourceConnectionId, "/test2", destConnectionId, "/Spring");
            // LOGGER.debug("Ingest job2 {}", jobId);

            LOGGER.info("Press Enter to exit ...");
            System.in.read();
        }
        catch (Exception e)
        {
            LOGGER.error("Failed", e);
        }
        finally
        {
            context.stop();
            System.exit(0);
        }
    }
}
