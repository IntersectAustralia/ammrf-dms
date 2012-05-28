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

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

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

    /**
     * Time in millis to wait before marker file can be processed and polling stopped. Used to make sure marker file is
     * not accessed by writing process any longer.
     */
    private long delay;

    /**
     * Time in millis between polls.
     */
    private long period;
    
    @Required
    public void setDelay(long delay)
    {
        this.delay = delay;
    }
    
    @Required
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
            final Timer timer = new Timer();

            TimerTask task = new FileCheckTask(timer, fileWatcher);

            timer.schedule(task, 0, period);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to start file watcher for connection {}, and directory {}", connectionId, dir);
            throw new RuntimeException("Failed to start file watcher", e);
        }
        // CHECKSTYLE-ON: IllegalCatch
    }
    
    /**
     * Timer task which checks if marker file appeared.
     * If marker file found that it also initiates ingest.
     * 
     */
    private final class FileCheckTask extends TimerTask
    {
        private final Timer timer;
        private final FileWatcher fileWatcher;

        private FileCheckTask(Timer timer, FileWatcher fileWatcher)
        {
            this.timer = timer;
            this.fileWatcher = fileWatcher;
        }

        @Override
        public void run()
        {
            FileWatcherResult markerFileFound = fileWatcher.checkMarkerFile();
            switch (markerFileFound)
            {
                case ERROR:
                    timer.cancel();
                    fileWatcher.abortIngest();
                    break;
                case CANCELLED:
                    timer.cancel();
                    fileWatcher.cancelIngest();
                    break;
                case FOUND:
                    timer.cancel();
                    try
                    {
                        Thread.sleep(delay);
                    }
                    catch (InterruptedException e)
                    {
                        throw new RuntimeException("Delay thread was interrupted", e);
                    }
                    fileWatcher.ingest();
                    break;
                default:
                    break;
            }
        }
    }
}
