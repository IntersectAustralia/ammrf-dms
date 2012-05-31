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

package au.org.intersect.dms.instrument.olympus;

import java.util.Arrays;

import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.domain.InstrumentProfile;

/**
 * Polls directory (recursively) on the main server for new FV1000 data.
 * 
 */
public class FV1000Poller extends RepositoryServerPoller
{

    @Override
    protected DatasetParams processFile(String username, FileInfo directory, FileInfo file, String fileURL)
    {
        DatasetParams datasetParams = null;
        
        LOGGER.trace("DIR: {}, FILE(FV1000): {}", directory.getAbsolutePath(), file.getName());
        
        if (file.getName().toLowerCase().endsWith(".oif"))
        {
            if (!getMetadataRepository().isUrlCatalogued(fileURL))
            {
                datasetParams = new DatasetParams(username, file.getAbsolutePath(), file.getModificationDate(),
                        InstrumentProfile.OLYMPUS_FV1000);
                datasetParams.setFromFiles(Arrays.asList(file.getAbsolutePath(), file.getAbsolutePath() + ".files"));
            }
        }

        return datasetParams;
    }

}
