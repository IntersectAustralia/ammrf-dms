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

package au.org.intersect.dms.instrument.harvester;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.core.instrument.MetadataAccumulator;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MicroCTHarvester.class)
public class MicroCTHarvesterTest
{
    private static final int BUFFER_SIZE = 512;

    @Mock
    private ExecutorService exec;

    @Mock
    private UrlCreator urlCreator;

    @InjectMocks
    private InstrumentHarvester instrument = new MicroCTHarvester();

    @Test
    public void testGetHarvesterFor()
    {
        assertNull(instrument.getHarvesterFor("/dir/image.jpg"));
        assertNotNull(instrument.getHarvesterFor("/dir/info.log"));
    }

    @Test
    public void testMetadataEvent() throws Exception
    {
        String toDir = "/dir1/dir2";
        final String serverUrl = "TOP://blah";
        final FromToData[] files = createFromToFiles(toDir);
        final String topUrl = serverUrl + files[0].getTo();
        MicroCTLogHarvester logHarvester = new MicroCTLogHarvester(Executors.newSingleThreadExecutor(), topUrl,
                instrument);
        whenNew(MicroCTLogHarvester.class)
                .withParameterTypes(ExecutorService.class, String.class, MetadataAccumulator.class)
                .withArguments(Matchers.any(), Matchers.any(), Matchers.any()).thenReturn(logHarvester);
        UrlCreator urlCreator = createUrlCreator(serverUrl);
        instrument.setUrlCreator(urlCreator);
        instrument.harvestStart(toDir);
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].getType() == FileType.DIRECTORY)
            {
                instrument.harvestDirectory(files[i].getTo());
            }
            else
            {
                FileHarvester harvester = instrument.getHarvesterFor(files[i].getFrom());
                if (harvester != null)
                {
                    OutputStream sink = harvester.getSink();
                    streamCopy(getClass().getResourceAsStream("/micro-ct/C4-01_.log"), sink);
                    harvester.closeSink(sink, true);
                }
                instrument.harvestFile(files[i].getTo(), files[i].getSize());
            }
        }
        List<MetadataEventItem> metadata = instrument.getMetadata();
        validateMetadataItems(serverUrl, toDir, files, metadata);
    }

    /*
     * WE SHOULD BE TESTING CONCURRENT WORK, BUT POSTPONE THIS FOR LATER
     * 
     * @throws Exception
     * 
     * @Test public void testThreadSafety() throws Exception { final long jobId = 10; final String serverUrl =
     * "TOP://blah"; final String toDir = "/dir1/dir3"; MetadataListener mdListener = new MetadataListener() {
     * 
     * @Override public void attach(MetadataEvent metadataEvent) { assertEquals(1000, metadataEvent.getItems().size());
     * }
     * 
     * }; instrument.setJobId(jobId); instrument.setMetadataListener(mdListener); UrlCreator urlCreator =
     * createUrlCreator(serverUrl); instrument.setUrlCreator(urlCreator); final ExecutorService threadPool =
     * Executors.newFixedThreadPool(20); threadPool.submit(new Runnable() {
     * 
     * @Override public void run() { instrument.harvestStart(toDir); walkThruTheFuture(makeTasksForDirs(threadPool,
     * toDir, 1)); instrument.harvestFinished(); }
     * 
     * }).get(); }
     * 
     * private Future<?>[] makeTasksForDirs(ExecutorService threadPool, String toDir, int depth) { Future<?>[] tasks =
     * new Future[4]; for (int i = 0; i < 4; i++) { tasks[i] = makeTaskForDir(threadPool, toDir + "/D" + i, depth); }
     * return tasks; }
     * 
     * private Future<?> makeTaskForDir(final ExecutorService threadPool, final String toDir, final int depth) {
     * Future<?> fut = threadPool.submit(new Runnable() {
     * 
     * @Override public void run() { System .out .println(">> DIRE:" + toDir); instrument.harvestDirectory(toDir);
     * Future<?>[] tasks = new Future[8]; for (int i = 0; i < 8; i++) { if (i % 4 == 0 && depth > 0) { tasks[i] =
     * makeTaskForDir(threadPool, toDir + "/D" + depth + "_" + (i / 4), depth - 1); } else { String file = i == 7 ?
     * toDir + "/a.log" : toDir + "/" + i + ".jpg"; tasks[i] = makeTaskForFile(threadPool, file); } }
     * walkThruTheFuture(tasks); }
     * 
     * }); return fut; }
     * 
     * private Future<?> makeTaskForFile(ExecutorService threadPool, final String file) { return threadPool.submit(new
     * Runnable() { public void run() { System .out .println(">> FILE:" + file); FileHarvester harvester =
     * instrument.getHarvesterFor(file); if (harvester != null) { OutputStream sink = harvester.getSink();
     * streamCopy(getClass().getResourceAsStream("/micro-ct/C4-01_.log"), sink); harvester.closeSink(sink, true); }
     * instrument.harvestFile(file, 200); } }); }
     * 
     * private void walkThruTheFuture(Future<?>[] tasks) { for (int i = 0; i < tasks.length; i++) { Object resp; try {
     * resp = tasks[i].get(); } catch (Exception e) { throw new RuntimeException(e); } if (resp == null) { throw new
     * RuntimeException("Task Failed [" + i + "]"); } }
     * 
     * 
     * }
     */

    private void streamCopy(InputStream is, OutputStream os)
    {
        byte[] buffer = new byte[BUFFER_SIZE];
        int readBytes;
        try
        {
            readBytes = is.read(buffer);
            while (readBytes > 0)
            {
                os.write(buffer, 0, readBytes);
                readBytes = is.read(buffer);
            }
            is.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private UrlCreator createUrlCreator(final String topUrl)
    {
        UrlCreator urlCreator = new UrlCreator()
        {
            @Override
            public String getUrl(String path)
            {
                return topUrl + path;
            }
        };
        return urlCreator;
    }

    private void validateMetadataItems(String serverUrl, String toDir, FromToData[] files,
            List<MetadataEventItem> items)
    {
        validateAllFiles(serverUrl, files, items);
        validateMicroCTExists(serverUrl + toDir, items);
    }

    private void validateMicroCTExists(String topUrl, List<MetadataEventItem> items)
    {
        boolean found = false;
        for (int j = 0; j < items.size() && !found; j++)
        {
            MetadataEventItem item = items.get(j);
            found = item.getUrl().equals(topUrl) && item.getType() == FileType.DIRECTORY
                    && item.getSchema() == MetadataSchema.MICRO_CT && item.getMetadata().length() > 0;
        }
        assertTrue("MICRO_CT schema not found", found);
    }

    private void validateAllFiles(String topUrl, FromToData[] files, List<MetadataEventItem> items)
    {
        boolean found = false;
        for (FromToData file : files)
        {
            for (int j = 0; j < items.size() && !found; j++)
            {
                MetadataEventItem item = items.get(j);
                // url must be last part, same type, and if FILE, then same size
                found = item.getUrl().equals(topUrl + file.getTo()) && item.getType() == item.getType()
                        && (item.getType() == FileType.DIRECTORY || file.getSize() == item.getSize());
            }
            assertTrue(file.getTo() + " not found", found);
        }
    }

    private FromToData[] createFromToFiles(String toDir)
    {
        final FromToData[] files = new FromToData[] {new FromToData(FileType.DIRECTORY, "/x", toDir, 0),
            new FromToData(FileType.FILE, "/x/im1.jpg", toDir + "/im1.jpg", 100),
            new FromToData(FileType.DIRECTORY, "/x/z", toDir + "/z", 0),
            new FromToData(FileType.FILE, "/x/info.log", toDir + "/info.log", 101),
            new FromToData(FileType.FILE, "/x/z/other.log", toDir + "/z/other.log", 101),
            new FromToData(FileType.DIRECTORY, "/x/y", toDir + "/y", 0),
            new FromToData(FileType.FILE, "/x/y/img1.jpg", toDir + "/y/img1.jpg", 101),
            new FromToData(FileType.FILE, "/x/z/img2.log", toDir + "/z/img2.jpg", 101)};
        return files;
    }
    
    /**
     * Represents a copy task for an individual source item.
     * Copy of the same class from worker. Used here just as convenient wrapper.
     */
    private static class FromToData
    {
        private String from;
        private String to;
        private FileType type;
        private long size;

        public FromToData(FileType type, String from, String to, long size)
        {
            this.type = type;
            this.from = from;
            this.to = to;
            this.size = size;
        }

        public String getFrom()
        {
            return from;
        }

        public String getTo()
        {
            return to;
        }

        public FileType getType()
        {
            return type;
        }

        public long getSize()
        {
            return size;
        }
    }

}
