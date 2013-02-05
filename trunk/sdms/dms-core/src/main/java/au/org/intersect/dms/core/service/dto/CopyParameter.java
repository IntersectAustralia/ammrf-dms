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

import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

import de.saxsys.roo.equals.addon.RooEquals;

/**
 * Copy params
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooSerializable
@RooEquals
@RooToString
public class CopyParameter
{

    private Long jobId;

    private Integer fromConnectionId;

    private List<String> fromFiles;

    private Integer toConnectionId;

    private String toDir;

    private String username;

    public CopyParameter(String username, Long jobId, Integer fromConnectionId, List<String> fromFiles,
            Integer toConnectionId, String toDir)
    {
        this.username = username;
        this.jobId = jobId;
        this.fromConnectionId = fromConnectionId;
        this.fromFiles = fromFiles;
        this.toConnectionId = toConnectionId;
        this.toDir = toDir;
    }
}
