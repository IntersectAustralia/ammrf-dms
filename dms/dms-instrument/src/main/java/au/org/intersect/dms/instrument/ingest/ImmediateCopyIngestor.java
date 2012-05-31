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

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.IngestionNode;
import au.org.intersect.dms.core.service.Ingestor;
import au.org.intersect.dms.core.service.dto.CopyParameter;
import au.org.intersect.dms.core.service.dto.IngestParameter;

/**
 * Ingestor for immediate ingestions with copy to repo server.
 *  
 * <p>
 * Use this ingestor for instruments where for example: 
 * <ul>
 * <li>user initiates ingestion through interface</li>
 * <li>scheduler creates ingestions jobs</li>
 * </ul>
 * and ingestion should happen straight away without any polling, delay or scheduling.
 * 
 * @version $Rev: 29 $
 */
public class ImmediateCopyIngestor implements Ingestor
{

    @Override
    public void ingest(final IngestParameter ingestParams, final IngestionNode workerNode,
            final InstrumentProfile instrumentProfile)
    {
        workerNode.copyAndOptionallyHarvest(new CopyParameter(ingestParams.getUsername(), ingestParams.getJobId(),
                ingestParams.getFromConnectionId(), ingestParams.getFromFiles(), ingestParams.getToConnectionId(),
                ingestParams.getToDir()), instrumentProfile);

    }

    @Override
    public void stopIngest(Long jobId)
    {
    }

    @Override
    public boolean startIngest(Long jobId)
    {
        return true;
    }

}
