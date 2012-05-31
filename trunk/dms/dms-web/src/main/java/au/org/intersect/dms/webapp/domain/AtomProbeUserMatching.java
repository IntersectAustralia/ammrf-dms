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

package au.org.intersect.dms.webapp.domain;

import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.roo.addon.entity.RooEntity;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

/**
 * Match between username in atom probe DB and booking system.
 * 
 */
@RooJavaBean
@RooToString
@RooEntity(finders = {"findAtomProbeUserMatchingsByInstrumentAndAtomProbeUsername"}, persistenceUnit = "webPU")
//TODO CHECKSTYLE-OFF: MagicNumber
public class AtomProbeUserMatching
{

    /**
     * Stock server id of the instrument.
     */
    @NotNull
    @OneToOne
    private StockServer instrument;
    
    @NotNull
    @Size(min = 1, max = 30)
    private String atomProbeUsername;

    @NotNull
    @Size(min = 1, max = 30)
    private String bookingSystemUsername;
}
