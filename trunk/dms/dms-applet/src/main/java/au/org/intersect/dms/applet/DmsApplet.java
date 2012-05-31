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

package au.org.intersect.dms.applet;

import javax.swing.JApplet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for DMS applets. Contains base functionality to check that applet is loaded from allowed page.
 */
public class DmsApplet extends JApplet
{
    protected static final Logger LOGGER = LoggerFactory.getLogger(DmsApplet.class);
    
    private MethodProcessor processor = new MethodProcessor();

    private boolean disabled = true;

    
    public boolean isDisabled()
    {
        return disabled;
    }
    
    public MethodProcessor getProcessor()
    {
        return processor;
    }


    protected void enableForTest()
    {
        disabled = false;
    }

    public void init()
    {
        disabled = false;
        LOGGER.info("Applet initialized");
    }

    @Override
    public boolean isActive()
    {
        return super.isActive() && !disabled;
    }

    public String getAppletInfo()
    {
        return "Title: Access to user's local disk\n" + "(c) Intersect Pty Ltd (Australia)";
    }

}
