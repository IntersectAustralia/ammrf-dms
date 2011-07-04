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

import org.apache.camel.CamelContext;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Starts polling directory for marker file.
 * 
 * @version $Rev: 5 $
 * 
 */
public class FilePoller
{
    private static final String BEAN_ROUTESTOPPER_STOP = "bean:routeStopper?method=stop";

    private static final Logger LOGGER = LoggerFactory.getLogger(FilePoller.class);

    @Autowired
    @Qualifier("workerCamelContext")
    private CamelContext camelContext;

    /**
     * Time in millis to wait before marker file can be processed and polling stopped. Used to make sure marker file is
     * not accessed by writing process any longer.
     */
    private long delay;

    /**
     * Time in millis between polls.
     */
    private long period;

    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    public void setPeriod(long period)
    {
        this.period = period;
    }

    /**
     * Starts polling directory for marker file. This is just timer that calls passed in fileWatcher to do actual check.
     * 
     * @param fileWatcher
     *            actual file watcher that checks directory for marker file
     */
    public void start(final FileWatcher fileWatcher)
    {
        final Integer connectionId = fileWatcher.getFromConnectionId();
        final String dir = fileWatcher.getMonitoredDirectory();
        LOGGER.debug("Starting file watcher for connection {}, watching directory {}", connectionId, dir);
        // TODO CHECKSTYLE-OFF: IllegalCatch
        try
        {
            camelContext.addRoutes(new RouteBuilder()
            {

                @Override
                public void configure() throws Exception
                {
                    StringBuilder timerURL = new StringBuilder("timer:fileWatcher");
                    timerURL.append(connectionId).append(dir).append("?period=").append(period);
                    from(timerURL.toString()).bean(fileWatcher, "checkMarkerFile").choice()
                            .when(body().isEqualTo(FileWatcherResult.ERROR)).bean(fileWatcher, "abortIngest")
                            .to(BEAN_ROUTESTOPPER_STOP).when(body().isEqualTo(FileWatcherResult.CANCELLED))
                            .bean(fileWatcher, "cancelIngest").to(BEAN_ROUTESTOPPER_STOP)
                            .when(body().isEqualTo(FileWatcherResult.FOUND)).delay(delay).multicast()
                            .parallelProcessing().bean(fileWatcher, "ingest").to(BEAN_ROUTESTOPPER_STOP);
                }
            });
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to start file watcher for connection {}, and directory {}", connectionId, dir);
            throw new RuntimeCamelException("Failed to start file watcher", e);
        }
        // CHECKSTYLE-ON: IllegalCatch
    }
}
