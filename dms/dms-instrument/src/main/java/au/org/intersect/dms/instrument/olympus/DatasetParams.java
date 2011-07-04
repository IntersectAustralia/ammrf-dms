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
import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.dms.core.domain.InstrumentProfile;

/**
 * Wrapper to hold information about new data to ingest
 * 
 */
@RooJavaBean
@RooToString
public class DatasetParams
{
    private String username;
    private String absolutePath;
    private String modificationDate;
    private InstrumentProfile instrumentProfile;
    private List<String> fromFiles;

    public DatasetParams(String username, String absolutePath, String modificationDate,
            InstrumentProfile instrumentProfile)
    {
        super();
        this.username = username;
        this.absolutePath = absolutePath;
        this.modificationDate = modificationDate;
        this.instrumentProfile = instrumentProfile;
    }

    public List<String> getFromFiles()
    {
        if (fromFiles != null)
        {
            return fromFiles;
        }
        else
        {
            return Arrays.asList(absolutePath);
        }
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((absolutePath == null) ? 0 : absolutePath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        DatasetParams other = (DatasetParams) obj;
        if (absolutePath == null)
        {
            if (other.absolutePath != null)
            {
                return false;
            }
        }
        else if (!absolutePath.equals(other.absolutePath))
        {
            return false;
        }
        return true;
    }

}
