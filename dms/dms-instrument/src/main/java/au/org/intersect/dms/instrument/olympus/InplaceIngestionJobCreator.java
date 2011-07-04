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

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import au.org.intersect.dms.bookinggw.Booking;
import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.BookingGatewayMetadataService;
import au.org.intersect.dms.bookinggw.Project;
import au.org.intersect.dms.core.domain.InstrumentProfile;
import au.org.intersect.dms.core.errors.TransportError;
import au.org.intersect.dms.core.service.ConfigurationService;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.core.service.dto.OpenConnectionParameter;
import au.org.intersect.dms.util.DateFormatter;
import au.org.intersect.dms.util.URLBuilder;

/**
 * Creates in-place ingestion jobs for data from FV1000 and TIRF Olympus microscopes.
 * 
 */
public class InplaceIngestionJobCreator
{
    private static final String SLASH = "/";

    private static final Logger LOGGER = LoggerFactory.getLogger(InplaceIngestionJobCreator.class);

    @Autowired
    private DmsService dmsService;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private BookingGatewayInterface bookingSystem;

    @Autowired
    private BookingGatewayMetadataService bookingSystemMetadataService;

    private Long repositoryId;

    private Long instrumentId;
    
    private InstrumentProfile instrumentProfile;

    @Required
    public void setRepository(Long repositoryId)
    {
        this.repositoryId = repositoryId;
    }

    @Required
    public void setInstrumentId(Long instrumentId)
    {
        this.instrumentId = instrumentId;
    }
    
    @Required
    public void setInstrumentProfile(InstrumentProfile instrumentProfile)
    {
        this.instrumentProfile = instrumentProfile;
    }

    public void createJobs(Collection<DatasetParams> datasets)
    {
        if (datasets.isEmpty())
        {
            return;
        }

        OpenConnectionParameter repositoryConnectionParams = configurationService
                .getServerConnectionParameters(repositoryId);

        Integer fromConnectionId = dmsService.openConnection(repositoryConnectionParams.getProtocol(),
                repositoryConnectionParams.getServer(), repositoryConnectionParams.getUsername(),
                repositoryConnectionParams.getPassword());

        for (DatasetParams dataset : datasets)
        {
            try
            {
                String username = dataset.getUsername();
                String toDir = getToDir(dataset);
                
                LOGGER.info("Creating ingestion job for user <{}> and path <{}>", username, toDir);

                Long projectCode = getProjectCode(username);
                String metadata = getMetadata(dataset, projectCode, repositoryConnectionParams);

                IngestParameter ingestParameters = new IngestParameter(username, null, fromConnectionId,
                        dataset.getFromFiles(), fromConnectionId, toDir, instrumentProfile);

                Long jobId = dmsService.ingest(username, projectCode, metadata, ingestParameters);
                if (jobId != null)
                {
                    LOGGER.info("Ingestion job with ID {} has been created.", jobId);
                }
                else
                {
                    LOGGER.info("Couldn't create ingestion job for user <{}> and path <{}>." + " Processing skipped.",
                            dataset.getUsername(), dataset.getAbsolutePath());
                }
            }
            catch (TransportError e)
            {
                LOGGER.error("Couldn't create ingestion job for user <" + dataset.getUsername() + "> and path <"
                        + dataset.getAbsolutePath() + ">. Processing has been stopped.", e);
                break;
            }
        }
    }

    private String getMetadata(DatasetParams dataset, Long projectCode,
            OpenConnectionParameter repositoryConnectionParams)
    {
        String username = dataset.getUsername();
        Date date = DateFormatter.parseDateTime(dataset.getModificationDate()).toDate();

        LOGGER.debug("Searching for bookings for date: {}", date);

        Booking booking = bookingSystem.getBooking(username, instrumentId, date);

        Long bookingId = booking != null ? booking.getBookingId() : null;
        LOGGER.debug("Found booking <{}> for user <{}> and project <{}>", new Object[] {bookingId, username,
            projectCode});

        String url = URLBuilder.buildURL(repositoryConnectionParams, dataset.getAbsolutePath());
        String metadata = bookingSystemMetadataService.getMetadata(projectCode, bookingId, url);
        return metadata;
    }

    private Long getProjectCode(String username)
    {
        Project[] projects = bookingSystem.getProjects(username);
        Long projectCode = projects.length > 0 ? projects[0].getProjectCode() : null;
        LOGGER.debug("Found project <{}> for user <{}>", projectCode, username);
        return projectCode;
    }
    
    private String getToDir(DatasetParams dataset)
    {
        String absolutePath = dataset.getAbsolutePath();
        return absolutePath.substring(0, absolutePath.lastIndexOf(SLASH));
    }
}
