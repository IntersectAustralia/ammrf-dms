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

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * 
 * 
 * @version $Rev: 29 $
 */
@RooJavaBean
@RooSerializable
@RooToString
public class JobUpdate extends JobEvent
{
    private long currentBytes;
    private int currentNumberOfFiles;
    private int currentNumberOfDirectories;

    public JobUpdate(Long jobId, int currentNumberOfDirectories, int currentNumberOfFiles, long currentBytes)
    {
        this(jobId, currentNumberOfDirectories, currentNumberOfFiles, currentBytes, System.currentTimeMillis());
    }

    public JobUpdate(Long jobId, int currentNumberOfDirectories, int currentNumberOfFiles, long currentBytes,
            long timeStamp)
    {
        super(jobId, timeStamp);
        this.currentBytes = currentBytes;
        this.currentNumberOfFiles = currentNumberOfFiles;
        this.currentNumberOfDirectories = currentNumberOfDirectories;
    }
}
