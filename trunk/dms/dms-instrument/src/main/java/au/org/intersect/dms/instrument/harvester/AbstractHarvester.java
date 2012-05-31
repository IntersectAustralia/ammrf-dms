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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.MetadataAccumulator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

/**
 * Base class for harvesters that provides them with a ThreadPool and few utility methods.
 * 
 * @version $Rev: 29 $
 */
public abstract class AbstractHarvester implements FileHarvester
{
    private ExecutorService executor;
    private MetadataSchema schema;
    private String targetUrl;
    private MetadataAccumulator accumulator;
    private FileType targetType;

    public AbstractHarvester(ExecutorService executorService, MetadataSchema schema, String targetUrl,
            FileType type, MetadataAccumulator accumulator)
    {
        this.executor = executorService;
        this.schema = schema;
        this.targetUrl = targetUrl;
        this.targetType = type;
        this.accumulator = accumulator;
    }

    public ExecutorService getExecutor()
    {
        return executor;
    }

    /**
     * Sub-classes implement only this method to grab the MD from the provided input stream.
     * 
     * @param input
     * @return
     * @throws IOException
     */
    abstract String process(InputStream input) throws IOException;

    @Override
    public OutputStream getSink()
    {
        try
        {
            final PipedOutputStreamWithFuture pos = new PipedOutputStreamWithFuture();
            final PipedInputStream pis = new PipedInputStream(pos);
            Callable<String> proc = new Callable<String>()
            {

                @Override
                public String call() throws Exception
                {
                    return process(pis);
                }
            };
            pos.setFuture(getExecutor().submit(proc));
            return pos;
        }
        catch (IOException e1)
        {
            throw new RuntimeException(e1);
        }
    }

    @Override
    public void closeSink(OutputStream harvester, boolean ok)
    {
        try
        {
            PipedOutputStreamWithFuture obj = (PipedOutputStreamWithFuture) harvester;
            obj.close();
            if (ok)
            {
                String metadata = obj.getFuture().get();
                MetadataEventItem item = new MetadataEventItem(targetUrl, targetType, 0L, schema, metadata);
                accumulator.addMetadataItem(item);
            }
        }
        catch (IOException e)
        {
            throw new HarvestException(e);
        }
        catch (InterruptedException e)
        {
            throw new HarvestException(e);
        }
        catch (ExecutionException e)
        {
            throw new HarvestException(e);
        }
    }

    protected static String toXML(String value)
    {
        CharacterIterator i = new StringCharacterIterator(value);
        StringBuffer result = new StringBuffer();
        char character = i.current();
        while (character != CharacterIterator.DONE)
        {
            if (character == '<')
            {
                result.append("&lt;");
            }
            else if (character == '>')
            {
                result.append("&gt;");
            }
            else if (character == '\"')
            {
                result.append("&quot;");
            }
            else if (character == '\'')
            {
                result.append("&#039;");
            }
            else if (character == '&')
            {
                result.append("&amp;");
            }
            else
            {
                result.append(character);
            }
            character = i.next();
        }
        return result.toString();
    }

}
