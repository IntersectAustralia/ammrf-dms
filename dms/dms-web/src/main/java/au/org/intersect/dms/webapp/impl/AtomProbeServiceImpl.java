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

package au.org.intersect.dms.webapp.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import au.org.intersect.dms.core.service.AtomProbeService;
import au.org.intersect.dms.webapp.domain.AtomProbeJobSettings;
import au.org.intersect.dms.webapp.domain.AtomProbeUserMatching;
import au.org.intersect.dms.webapp.domain.StockServer;

/**
 * Implementation of AtomProbe Service.
 * 
 */
public class AtomProbeServiceImpl implements AtomProbeService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomProbeServiceImpl.class);


    @Transactional("web")
    @Override
    public Long getLastProcessedExperiment(Long instrumentId)
    {
        LOGGER.info("Got request for last processed experiment for instrument {}", instrumentId);

        StockServer instrument = StockServer.findStockServer(instrumentId);
        AtomProbeJobSettings atomProbeJobSettings = AtomProbeJobSettings.findAtomProbeJobSettingsesByInstrument(
                instrument).getSingleResult();
        Long lastProcessedExperiment = atomProbeJobSettings.getLastProcessedExperiment();
        LOGGER.info("Last successfully processed expriment for instrument {} is {}", instrumentId,
                lastProcessedExperiment);

        return lastProcessedExperiment;
    }

    @Transactional("web")
    @Override
    public void setLastProcessedExperiment(Long instrumentId, Long experimentId)
    {
        StockServer instrument = StockServer.findStockServer(instrumentId);
        AtomProbeJobSettings atomProbeJobSettings = AtomProbeJobSettings.findAtomProbeJobSettingsesByInstrument(
                instrument).getSingleResult();
        atomProbeJobSettings.setLastProcessedExperiment(experimentId);
        atomProbeJobSettings.merge();
    }

    @Transactional("web")
    @Override
    public String getBookingSystemUsername(Long instrumentId, String username)
    {
        String mappedUserName = null;
        StockServer instrument = StockServer.findStockServer(instrumentId);

        List<AtomProbeUserMatching> result = AtomProbeUserMatching
                .findAtomProbeUserMatchingsByInstrumentAndAtomProbeUsername(instrument, username).getResultList();
        if (!result.isEmpty())
        {
            AtomProbeUserMatching matching = result.get(0);
            mappedUserName = matching.getBookingSystemUsername();
        }
        return mappedUserName;
    }

}
