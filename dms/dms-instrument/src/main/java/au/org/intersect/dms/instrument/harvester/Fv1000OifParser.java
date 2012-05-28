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

import org.joda.time.format.DateTimeFormatter;

/**
 * Parses FV1000 oif log file.
 */
public class Fv1000OifParser extends WindowsIniLogParser
{

    public Fv1000OifParser(Mode mode, String fieldsFilePath, String typesFilePath, String templateFilePath,
            DateTimeFormatter instrumentDateFormat)
    {
        super(mode, fieldsFilePath, typesFilePath, templateFilePath, instrumentDateFormat);
    }

    @Override
    protected MetadataField mapField(String name, String value)
    {
        MetadataField field = new MetadataField();

        if ("DyeName".equals(name))
        {
            field.setName("Method");
        }
        else
        {
            field.setName(name);
        }

        if ("LaserTransmissivity".equals(name) && !"0".equals(value))
        {
            field.setValue(value.substring(0, value.length() - 1));
        }
        else
        {
            field.setValue(value);
        }

        return field;
    }

}
