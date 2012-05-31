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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import au.org.intersect.dms.applet.domain.JsResponse;

/**
 * Applet to browse HDD
 */
public class BrowseApplet extends DmsApplet
{
    private static final Color RED = new Color(255, 70, 60);
    private static final Color GREEN = new Color(96, 255, 100);
    private static final Font FONT = new Font("serif", Font.PLAIN, 10);
    private static final int OFFSET = FONT.getSize() - 1;

    public void init()
    {
        super.init();
        setLayout(new BorderLayout(0, 0));
    }

    public void paint(Graphics g)
    {
        Dimension appletSize = getSize();
        int appletHeight = appletSize.height;
        int appletWidth = appletSize.width;
        char[] textToBeDisplayed = {'P', 'C'};

        if (isDisabled())
        {
            g.setColor(RED);
        }
        else
        {
            g.setColor(GREEN);
        }

        g.fillRoundRect(0, 0, appletWidth, appletHeight, 1, 1);
        g.setColor(Color.black);
        g.setFont(FONT);

        g.drawChars(textToBeDisplayed, 0, textToBeDisplayed.length, 1, OFFSET);
    }

    public String send(String method, String data)
    {
        if (isDisabled())
        {
            return new JsResponse("disabled", false).toJson();
        }
     // TODO CHECKSTYLE-OFF: IllegalCatch
        try
        {
            return getProcessor().perform(method, data).toJson();
        }
        catch (Exception e)
        {
            LOGGER.error("Exception during data sending", e);
            if (e.getMessage() != null && e.getMessage().length() > 0)
            {
                return new JsResponse(e.getMessage(), false).toJson();
            }
            else
            {
                return new JsResponse(e.getClass().toString(), false).toJson();
            }
        }
     // TODO CHECKSTYLE-ON: IllegalCatch
    }

    public String[][] getParameterInfo()
    {
        String[][] info = {};
        return info;
    }

}
