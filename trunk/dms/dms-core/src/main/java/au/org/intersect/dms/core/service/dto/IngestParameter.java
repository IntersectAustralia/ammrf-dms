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

package au.org.intersect.dms.core.service.dto;

import java.util.Arrays;
import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

import au.org.intersect.dms.core.domain.InstrumentProfile;

import de.saxsys.roo.equals.addon.RooEquals;

/**
 * Ingest params
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooSerializable
@RooEquals(callSuper = true)
@RooToString
public class IngestParameter extends CopyParameter
{
    private InstrumentProfile instrumentProfile;
    private boolean copyToWorkstation;

    public IngestParameter(String username, Long jobId, Integer fromConnectionId, List<String> fromFiles,
            Integer toConnectionId, String toDir, InstrumentProfile instrumentProfile)
    {
        super(username, jobId, fromConnectionId, fromFiles, toConnectionId, toDir);
        this.instrumentProfile = instrumentProfile;
    }

    public IngestParameter(String username, Long jobId, Integer fromConnectionId, String fromDir,
            Integer toConnectionId, String toDir, InstrumentProfile instrumentProfile)
    {
        this(username, jobId, fromConnectionId, Arrays.asList(fromDir), toConnectionId, toDir, instrumentProfile);
    }
}