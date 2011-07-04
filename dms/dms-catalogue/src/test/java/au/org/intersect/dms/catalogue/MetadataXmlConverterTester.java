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

package au.org.intersect.dms.catalogue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import au.org.intersect.dms.catalogue.MetadataXmlConverter.ConversionParams;
import au.org.intersect.dms.catalogue.MetadataXmlConverter.Format;
import au.org.intersect.dms.core.catalogue.MetadataSchema;

public class MetadataXmlConverterTester
{
    private static final String NEW_LINE = "    \n";

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataXmlConverterTester.class);

    private static final String[] CONFIG_FILES = {"classpath*:META-INF/spring/applicationContext-*.xml"};

    public static void main(String[] args)
    {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_FILES);
        context.registerShutdownHook();
        context.start();

        String metadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<atomProbe xmlns=\"http://www.acmm.sydney.edu.au/schemata/atomprobe\">\n" + "  <experiment>\n"
                + "    <property name=\"Comment\" value=\"Test experiment comment\"/>\n"
                + "      <property name=\"ExperimentDATE\" value=\"2010-11-15T08:21:44Z\"/>\n"
                + "      <property name=\"FlightPathLength\" value=\"1\"/>\n"
                + "      <property name=\"GoodHits\" value=\"2\"/>\n"
                + "      <property name=\"LaserPower\" value=\"3\"/>\n"
                + "      <property name=\"Name\" value=\"Test_123\"/>\n"
                + "      <property name=\"PerCentGoodHits\" value=\"4\"/>\n"
                + "      <property name=\"PulseFraction\" value=\"5\"/>\n"
                + "      <property name=\"PulseFrequency\" value=\"6\"/>\n"
                + "      <property name=\"StopVoltage\" value=\"7\"/>\n"
                + "      <property name=\"Temp\" value=\"8\"/>\n" + "      <property name=\"Vacuum\" value=\"9\"/>\n"
                + NEW_LINE + "  </experiment>\n" + "  <machine>\n"
                + "    <property name=\"Name\" value=\"Test Atom Probe\"/>\n" + NEW_LINE + "  </machine>\n"
                + "  <operator>\n" + "    <property name=\"Last\" value=\"TestLastName\"/>\n"
                + "      <property name=\"First\" value=\"TestFirstName\"/>\n" + NEW_LINE + "  </operator>\n"
                + "  <specimen>\n" + "    <property name=\"Name\" value=\"Test specimen\"/>\n" + NEW_LINE
                + "  </specimen>\n" + "\n" + "</atomProbe>";

        try
        {
            MetadataXmlConverter converter = context.getBean(MetadataXmlConverter.class);
            ConversionParams params = new ConversionParams();
            params.schema(MetadataSchema.ATOM_PROBE).desinationFormat(Format.HTML).metadata(metadata);
            String html = converter.convert(params);
            LOGGER.info("XML:\n{}\n\nHTML:\n{}", metadata, html);
        }
        catch (Exception e)
        {
            LOGGER.error("Failed", e);
        }
        finally
        {
            context.stop();
            System.exit(0);
        }
    }
}