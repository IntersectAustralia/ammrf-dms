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

package au.org.intersect.dms.test;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 * The <code>DBUnitConfiguration</code> annotation defines class-level metadata used to provide DBUnit configuration
 * data to an instance of {@link DBUnitTestExecutionListener}.
 * </p>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
public @interface DBUnitConfiguration
{

    /**
     * The DBUnit data set configuration file locations.
     * If not specified TestClassName-dataset.xml is used.
     * 
     * @return the location of the DBUnit data set configuration files
     */
    String[] locations() default {};

    /**
     * The DBUnit DataSet type of the configuration files. If not specified the default will be assumed to be
     * {@link org.dbunit.dataset.xml.FlatXmlDataSet}.
     * 
     * @return the data set type of the configuration files
     */
    Class type() default org.dbunit.dataset.xml.FlatXmlDataSet.class;

}
