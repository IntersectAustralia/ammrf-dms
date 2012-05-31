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

package au.org.intersect.dms.email;

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.intersect.dms.bookinggw.UserDetails;
import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.MetadataItem;
import au.org.intersect.dms.core.catalogue.MetadataSchema;

public class ReminderEmailBuilderTest
{
    private static final String USER1 = "user1";

    private static final Logger LOGGER = LoggerFactory.getLogger(ReminderEmailBuilderTest.class);
    
    private UserDetails userDetails;

    private List<Dataset> datasets;
    
    private ReminderEmailBuilder emailBuilder = new ReminderEmailBuilder();

    @Before
    public void setUp() throws Exception
    {
        userDetails = new UserDetails();
        userDetails.setEmail("user1@dms-test.org.au");
        userDetails.setFirstName("FirstName " + USER1);
        userDetails.setLastName("LastName " + USER1);

        emailBuilder.setDmsLocation("http://example/");
        
        datasets = createDatasets(USER1, 3);
    }
    
    private List<Dataset> createDatasets(String owner, int number)
    {
        List<Dataset> datasets = new ArrayList<Dataset>(number);
        for (int i = 1; i <= number; i++)
        {
            Dataset dataset = new Dataset();
            dataset.setId(String.valueOf(i));
            dataset.setOwner(owner);
            dataset.setUrl("ftp://localhost/datsets/" + owner + "/" + i);
            dataset.setProjectCode((long) i);
            dataset.setCreationDate(new Date());
            final String metadata = "<registryObjects xmlns=\"http://ands.org.au/standards/rif-cs/registryObjects\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                    + "xsi:schemaLocation=\"http://ands.org.au/standards/rif-cs/registryObjects "
                    + "http://services.ands.org.au/home/orca/schemata/registryObjects.xsd\">\n"
                    + "  <registryObject group=\"AMMRF\">\n" 
                    + "    <key>bb9fdfc0-5303-48c9-99f5-b41723097fdf</key>\n"
                    + "    <originatingSource>http://ammrf.org.au</originatingSource>\n"
                    + "    <collection type=\"dataset\">\n"
                    + "      <identifier type=\"local\">bb9fdfc0-5303-48c9-99f5-b41723097fdf</identifier>\n"
                    + "      <name type=\"primary\">\n" 
                    + "        <namePart type=\"full\">Test project" + i + "</namePart>\n"
                    + "      </name>\n" 
                    + "      <location>\n" 
                    + "        <address>\n"
                    + "          <electronic type=\"url\">\n"
                    + "            <value>ftp://localhost/tmp/20062010</value>\n" 
                    + "          </electronic>\n"
                    + "          <physical>\n" 
                    + "            <addressPart type=\"text\"></addressPart>\n"
                    + "          </physical>\n" 
                    + "        </address>\n" 
                    + "      </location>\n"
                    + "      <description type=\"about\">Test description.</description>\n" 
                    + "    </collection>\n"
                    + "  </registryObject>\n" 
                    + "</registryObjects>";
            List<MetadataItem> metadataItems = new LinkedList<MetadataItem>()
            {
                {
                    add(createMetadataItem(MetadataSchema.RIF_CS, metadata));
                }

            };
            
            dataset.setMetadata(metadataItems);
            datasets.add(dataset);
        }
        return datasets;
    }

    private MetadataItem createMetadataItem(MetadataSchema schema, String metadata)
    {
        MetadataItem metadataItem = new MetadataItem();
        metadataItem.setSchema(schema);
        metadataItem.setMetadata(metadata);
        return metadataItem;
    }
    
    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testBuildEmail() throws UnsupportedEncodingException
    {
        String emailContent = emailBuilder.buildEmail(userDetails, datasets);
        LOGGER.info("Reminder email content:\n{}", emailContent);

        assertTrue("First name not found", emailContent.contains(userDetails.getFirstName()));
        assertTrue("Last name not found", emailContent.contains(userDetails.getLastName()));
        assertTrue("Project title not found", emailContent.contains("Test project3"));
        assertTrue("Dataset URL not found"
                , emailContent.contains(URLEncoder.encode(datasets.get(2).getUrl(), "UTF-8")));
    }

}
