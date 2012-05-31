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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.unitils.reflectionassert.ReflectionAssert.assertLenientEquals;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.xml.sax.SAXException;

import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileType;
import au.org.intersect.dms.core.instrument.UrlCreator;
import au.org.intersect.dms.core.service.dto.MetadataEventItem;

@RunWith(MockitoJUnitRunner.class)
public class AtomProbeHarvesterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AtomProbeHarvesterTest.class);
    
    private static final String NAME_PROPERTY = "Name";

    private static final String BASE_URL = "ftp://locahost/atomProbe";

    public static class TestUrlCreator implements UrlCreator
    {

        @Override
        public String getUrl(String path)
        {
            return BASE_URL + path;
        }

    }

    private UrlCreator urlCreator = new TestUrlCreator();

    @Mock
    private SimpleJdbcTemplate jdbcTemplate;

    @InjectMocks
    private AtomProbeHarvester harvester = new AtomProbeHarvester();

    private long experimentId = 123;

    private String expectedDateString = "2010-11-15T02:52:35Z";

    private Timestamp date;

    @Before
    public void setUp() throws ParseException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        date = new Timestamp(dateFormat.parse("2010-11-15T02:52:35-0000").getTime());
        harvester.setUrlCreator(urlCreator);
        XMLUnit.setIgnoreWhitespace(true);
    }

    private String getAtomProbeMetadata(List<MetadataEventItem> metadataItems)
    {
        String metadata = null;
        for (MetadataEventItem metadataItem : metadataItems)
        {
            if (MetadataSchema.ATOM_PROBE == metadataItem.getSchema())
            {
                metadata = metadataItem.getMetadata();
                break;
            }
        }
        return metadata;
    }

    @Test
    public void harvestFileEmptyMetadata() throws SAXException, IOException
    {
        long size = 123;
        String fileName = "/test_123.RHIT";
        String url = BASE_URL + fileName;
        String metadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<atomProbe xmlns=\"http://www.acmm.sydney.edu.au/schemata/atomprobe\">"
            + "<experiment/><specimen/><machine/><operator/>" + "</atomProbe>";

        harvester.harvestFile(fileName, size);

        List<MetadataEventItem> metadataItems = (List<MetadataEventItem>) ReflectionTestUtils
                .getField(harvester, "metadataItems");
        List<MetadataEventItem> expected = new LinkedList<MetadataEventItem>();
        expected.add(new MetadataEventItem(url, FileType.FILE, size, null, null));
        expected.add(new MetadataEventItem(url, FileType.FILE, size, MetadataSchema.ATOM_PROBE, null));
        assertLenientEquals(expected, metadataItems);
        
        String atomProbeMetadata = getAtomProbeMetadata(metadataItems);
        LOGGER.info("Got XML:\n{}", atomProbeMetadata);
        
        XMLAssert.assertXMLEqual("Incorrect metadata XML", metadata, atomProbeMetadata);
    }
    
 // TODO CHECKSTYLE-OFF: JavaNCSS|ExecutableStatementCount
    @Test
    public void harvestFileAllPropertiesMetadata() throws SAXException, IOException, ParseException
    {
        int specimenId = 123;
        int machineId = 45;
        int opertatorId = 6;
        long size = 1;
        String fileName = "/test_" + experimentId + ".RHIT";
        String url = BASE_URL + fileName;
        String name = "Test_123";
        String comment = "Test experiment comment";
        final String specimenName = "Test specimen";
        final String operatorFirstName = "TestFirstName";
        final String opertatorLastName = "TestLastName";
        final String machineName = "Test Atom Probe";

        int i = 1;
        String metadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<atomProbe xmlns=\"http://www.acmm.sydney.edu.au/schemata/atomprobe\">" 
                + "<experiment>"
                + propery("Comment", comment)
                + propery("ExperimentDATE", expectedDateString)
                + propery("FlightPathLength", i++) 
                + propery("GoodHits", i++) 
                + propery("LaserPower", i++) 
                + propery(NAME_PROPERTY, name)
                + propery("PerCentGoodHits", i++) 
                + propery("PulseFraction", i++) 
                + propery("PulseFrequency", i++) 
                + propery("StopVoltage", i++) 
                + propery("Temp", i++) 
                + propery("Vacuum", i++) 
                + "</experiment>"
                + "<machine>"
                + propery(NAME_PROPERTY, machineName) 
                + "</machine>"
                + "<operator>"
                + propery("Last", opertatorLastName)
                + propery("First", operatorFirstName)
                + "</operator>"
                + "<specimen>"
                + propery(NAME_PROPERTY, specimenName) 
                + "</specimen>"
                + "</atomProbe>";
        StringBuilder experimentQuery = new StringBuilder();
        experimentQuery
                .append("select Name, ExperimentDATE, Comment, GoodHits, Temp, Vacuum, StopVoltage, "
                        + " FlightPathLength, PulseFrequency, PulseFraction, PerCentGoodHits, LaserPower, "
                        + " SpecimenID, OperatorID, MachineID").append(" from experiments ")
                .append(" where ExperimentID = ? and RecordStatusID = 1");

        Map<String, Object> metadataProperties = new TreeMap<String, Object>();
        metadataProperties.put(NAME_PROPERTY, name);
        metadataProperties.put("ExperimentDATE", date);
        metadataProperties.put("Comment", comment);
        i = 1;
        metadataProperties.put("FlightPathLength", i++);
        metadataProperties.put("GoodHits", i++);
        metadataProperties.put("LaserPower", i++);
        metadataProperties.put("PerCentGoodHits", i++);
        metadataProperties.put("PulseFraction", i++);
        metadataProperties.put("PulseFrequency", i++);
        metadataProperties.put("StopVoltage", i++);
        metadataProperties.put("Temp", i++);
        metadataProperties.put("Vacuum", i++);
        metadataProperties.put("SpecimenID", specimenId);
        metadataProperties.put("MachineID", machineId);
        metadataProperties.put("OperatorID", opertatorId);

        when(jdbcTemplate.queryForMap(experimentQuery.toString(), experimentId)).thenReturn(metadataProperties);

        String specimenQuery = "select Name from specimens where SpecimenID = ?";
        when(jdbcTemplate.queryForMap(specimenQuery, specimenId)).thenReturn(new HashMap<String, Object>()
        {
            {
                put(NAME_PROPERTY, specimenName);
            }
        });
        
        String operatorQuery = "select Last, First from operators where OperatorID = ?";
        when(jdbcTemplate.queryForMap(operatorQuery, opertatorId)).thenReturn(new HashMap<String, Object>()
        {
            {
                put("First", operatorFirstName);
                put("Last", opertatorLastName);
            }
        });
        
        String machineQuery = "select Name from machines where MachineID = ?";
        when(jdbcTemplate.queryForMap(machineQuery, machineId)).thenReturn(new HashMap<String, Object>()
        {
            {
                put(NAME_PROPERTY, machineName);
            }
        });

        harvester.harvestFile(fileName, size);
        
        List<MetadataEventItem> metadataItems = (List<MetadataEventItem>) ReflectionTestUtils
                .getField(harvester, "metadataItems");

        verify(jdbcTemplate).queryForMap(experimentQuery.toString(), experimentId);
        verify(jdbcTemplate).queryForMap(specimenQuery.toString(), specimenId);
        verify(jdbcTemplate).queryForMap(operatorQuery.toString(), opertatorId);
        verify(jdbcTemplate).queryForMap(machineQuery.toString(), machineId);
        verifyNoMoreInteractions(jdbcTemplate);

        List<MetadataEventItem> expected = new LinkedList<MetadataEventItem>();
        expected.add(new MetadataEventItem(url, FileType.FILE, size, null, null));
        expected.add(new MetadataEventItem(url, FileType.FILE, size, MetadataSchema.ATOM_PROBE, null));
        assertLenientEquals(expected, metadataItems);
        String atomProbeMetadata = getAtomProbeMetadata(metadataItems);

        LOGGER.info("Got XML:\n{}", atomProbeMetadata);
        XMLAssert.assertXMLEqual("Incorrect metadata XML", metadata, atomProbeMetadata);
    }
    
 // CHECKSTYLE-ON: JavaNCSS|ExecutableStatementCount
    
    private String propery(String name, Object value)
    {
        return "<property name=\"" + name + "\" value=\"" + value + "\"/>";
    }
}
