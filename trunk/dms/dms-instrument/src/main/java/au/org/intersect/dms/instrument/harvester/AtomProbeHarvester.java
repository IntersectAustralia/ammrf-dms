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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.antlr.stringtemplate.AttributeRenderer;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.FileHarvester;
import au.org.intersect.dms.core.instrument.InstrumentHarvester;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;
import au.org.intersect.dms.util.DateFormatter;

/**
 * Implements the harvest profile for AtomProbe. It harvest atom probe DB. Dataset is just one file.
 */
public class AtomProbeHarvester extends SimpleJdbcDaoSupport implements InstrumentHarvester
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomProbeHarvester.class);

    /**
     * Template engine
     */
    private static final StringTemplateGroup ST_GROUP = new StringTemplateGroup("metadata");

    static
    {
        ST_GROUP.registerRenderer(Timestamp.class, new DateRenderer());
    }

    private UrlCreator urlCreator;

    private List<MetadataEventItem> metadataItems = new ArrayList<MetadataEventItem>();

    @Override
    public FileHarvester getHarvesterFor(String path)
    {
        return null;
    }

    @Override
    public void harvestStart(String toDir)
    {
    }
    
    @Override
    public void harvestEnd()
    {
    }

    @Override
    public List<MetadataEventItem> getMetadata()
    {
        return metadataItems;
    }

    @Override
    public void setUrlCreator(UrlCreator urlCreator)
    {
        this.urlCreator = urlCreator;
    }

    @Override
    public void addMetadataItem(MetadataEventItem item)
    {
        synchronized (metadataItems)
        {
            metadataItems.add(item);
        }
    }

    @Override
    public void harvestDirectory(String to)
    {
        synchronized (metadataItems)
        {
            MetadataEventItem item = new MetadataEventItem(urlCreator.getUrl(to), FileType.DIRECTORY, 0L, null, null);
            metadataItems.add(item);
        }
    }

    @Override
    public void harvestFile(String to, long size)
    {
        LOGGER.debug("Extracting metadata for file {}", to);
        StringTemplate template = ST_GROUP.getInstanceOf("META-INF/atom-probe/metadataTemplate");
        StringBuilder query = new StringBuilder();
        query.append(
                "select Name, ExperimentDATE, Comment, GoodHits, Temp, Vacuum, StopVoltage, "
                        + " FlightPathLength, PulseFrequency, PulseFraction, PerCentGoodHits, LaserPower, "
                        + " SpecimenID, OperatorID, MachineID").append(" from experiments ")
                .append(" where ExperimentID = ? and RecordStatusID = 1");
        String fileName = to.substring(to.lastIndexOf('/') + 1);
        String experimentId = fileName.substring(fileName.indexOf('_') + 1, fileName.indexOf('.'));
        Map<String, Object> experiment = getSimpleJdbcTemplate().queryForMap(query.toString(),
                Long.valueOf(experimentId));

        Map<String, Object> experimentProperties = new TreeMap<String, Object>();

        for (Entry<String, Object> propery : experiment.entrySet())
        {
            String name = propery.getKey();
            if (!name.endsWith("ID"))
            {
                experimentProperties.put(name, propery.getValue());
            }
        }

        Map<String, Map<String, Object>> values = new TreeMap<String, Map<String, Object>>();
        values.put("experiment", experimentProperties);

        Map<String, Object> specimen = getSimpleJdbcTemplate().queryForMap(
                "select Name from specimens where SpecimenID = ?", experiment.get("SpecimenID"));
        values.put("specimen", specimen);

        Map<String, Object> operator = getSimpleJdbcTemplate().queryForMap(
                "select Last, First from operators where OperatorID = ?", experiment.get("OperatorID"));
        values.put("operator", operator);

        Map<String, Object> machine = getSimpleJdbcTemplate().queryForMap(
                "select Name from machines where MachineID = ?", experiment.get("MachineID"));
        values.put("machine", machine);

        template.setAttribute("sections", values);
        synchronized (metadataItems)
        {
            metadataItems.add(new MetadataEventItem(urlCreator.getUrl(to), FileType.FILE, size, null, null));
            MetadataEventItem item = new MetadataEventItem(urlCreator.getUrl(to), FileType.FILE, size,
                    MetadataSchema.ATOM_PROBE, template.toString());
            metadataItems.add(item);
        }
    }

    /**
     * Formats Dates
     */
    private static class DateRenderer implements AttributeRenderer
    {
        @Override
        public String toString(Object o)
        {
            return DateFormatter.formatDate2UTC(((Timestamp) o).getTime());
        }

        @Override
        public String toString(Object o, String formatName)
        {
            return toString(o);
        }
    }

}
