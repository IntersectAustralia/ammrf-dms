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

package au.org.intersect.dms.rifcs.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.org.ands.standards.rif_cs.registryobjects.RegistryObjects;

import au.org.intersect.dms.rifcs.RifcsMarshallService;


/**
 * Implementation of RifcsMarshallService
 * 
 * @author carlos
 */
public class RifcsMarshallServiceImpl implements RifcsMarshallService
{

    private static JAXBContext jaxbCtx;

    private static final Logger LOGGER = LoggerFactory.getLogger(RifcsMarshallServiceImpl.class);

    static
    {
        try
        {
            jaxbCtx = JAXBContext.newInstance("au.org.ands.standards.rif_cs.registryobjects");
        }
        catch (JAXBException e)
        {
            LOGGER.error("Jaxb Context init error", e);
        }
    }

    @Override
    public RegistryObjects unmarshallRegistryObjects(String theXml)
    {
        try
        {
            return (RegistryObjects) jaxbCtx.createUnmarshaller().unmarshal(
                    new ByteArrayInputStream(theXml.getBytes()));
        }
        catch (JAXBException e)
        {
            LOGGER.error("unmarshall error (xml input: see trace if enabled)", e);
            if (LOGGER.isTraceEnabled())
            {
                LOGGER.trace(theXml);
            }
            return null;
        }
    }

    @Override
    public String marshallRegistryObjects(RegistryObjects obj)
    {
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            marshallRegistryObjects(obj, bos);
            bos.flush();
            return bos.toString();
        }
        catch (IOException e)
        {
            LOGGER.error("error marshaling rif-cs object", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void marshallRegistryObjects(RegistryObjects resp, OutputStream os)
    {
        try
        {
            jaxbCtx.createMarshaller().marshal(resp, os);
        }
        catch (JAXBException e)
        {
            LOGGER.error("error marshalling rif-cs object", e);
            throw new RuntimeException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
        }
    }

}
