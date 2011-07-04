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

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * HDD download/upload panel to put on the applet.
 * 
 */
public class TransferPannel extends JPanel
{
    private JLabel labelWithJobId;
    private String jobId = "not set";

    public TransferPannel()
    {
        labelWithJobId = new JLabel("<html>Creating new job for this copy.<br/>Please don't close this window.</html>");
        add(labelWithJobId);
    }

    public void setJob(String jobId)
    {
        this.jobId = jobId;
        labelWithJobId.setText("<html>Transfering data for job [" + jobId
                + "]...<br/>Please don't close this window.</html>");
    }

    public void finished()
    {
        labelWithJobId
                .setText("<html>Job [" + jobId + "] no longer transfering.<br/>You can close this window.</html>");
    }

}
