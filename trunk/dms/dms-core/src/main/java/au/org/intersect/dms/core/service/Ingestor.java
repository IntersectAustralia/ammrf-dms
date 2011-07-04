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

package au.org.intersect.dms.core.service;

import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.service.dto.IngestParameter;

/**
 * Dataset ingestor for instrument data. Encapsulates different ingestion strategies and allows to ingest data from
 * different instrument different way.
 */
public interface Ingestor
{
    /**
     * Main ingestion method. Each individual ingestor should implement ingestion logic here.
     * 
     * @param ingestParams
     */
    void ingest(final IngestParameter ingestParams, final IngestionNode workerNode, 
            final InstrumentProfile instrumentProfile);

    /**
     * Method to notify ingestor that ingestion should be stopped(aborted).
     * This callback method is usefull for polling ingestions.
     * 
     * @param jobId
     *            ingestion job ID
     */
    void stopIngest(Long jobId);

    /**
     * Method to notify ingestor that ingestion is about to start. After this method is called files processing happens.
     * This callback method is usefull for polling ingestions.
     * 
     * Ingestor implementation should return <b>true</b> if ingestion should happen and <b>false</b> if ingestion was
     * cancelled
     * 
     * @param jobId
     *            ingestion job ID
     * @return true if ingestion should "go" or false if it was canceled
     */
    boolean startIngest(Long jobId);
}
