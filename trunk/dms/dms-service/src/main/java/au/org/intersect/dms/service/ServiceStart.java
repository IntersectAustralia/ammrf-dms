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

package au.org.intersect.dms.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Runnable class to test JMS in isolation
 * 
 * @version $Rev: 29 $
 */
public class ServiceStart
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceStart.class);

    /**
     * @param args
     */
    private static final String[] CONFIG_FILES = {"classpath*:META-INF/spring/applicationContext*.xml"};

    private static final int RESULTS_PAGE_SIZE = 10;

    /**
     * Loads the beans in the Spring container.
     * 
     * @param args
     *            not used
     * @throws IOException
     */
    // TODO CHECKSTYLE-OFF: UncommentedMain|IllegalCatch
    public static void main(String[] args) throws IOException
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_FILES);
        context.registerShutdownHook();
        context.start();

        LOGGER.info("Service started. Press Enter to exit ...");
        System.in.read();

        context.stop();
        System.exit(0);

    }
    // CHECKSTYLE-ON: UncommentedMain
}
