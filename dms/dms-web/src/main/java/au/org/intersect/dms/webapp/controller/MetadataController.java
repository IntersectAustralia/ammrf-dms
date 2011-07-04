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

package au.org.intersect.dms.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.org.ands.standards.rif_cs.registryobjects.RegistryObjects;
import au.org.ands.standards.rif_cs.registryobjects.RegistryObjects.RegistryObject;
import au.org.ands.standards.rif_cs.registryobjects.RegistryObjects.RegistryObject.OriginatingSource;

import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.DatasetSearchResult;
import au.org.intersect.dms.catalogue.MetadataItem;
import au.org.intersect.dms.catalogue.MetadataRepository;
import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.rifcs.RifcsMarshallService;

/**
 * Home controller, nothing for now
 */
@RequestMapping("/metadata/**")
@Controller
@Transactional("web")
public class MetadataController
{
    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private RifcsMarshallService rifcsService;

    @Autowired
    private BookingGatewayInterface bookingGateway;

    private MetadataControllerHelper helper;
    
    private synchronized MetadataControllerHelper getHelper()
    {
        if (helper == null)
        {
            helper = new MetadataControllerHelper(bookingGateway);
        }
        return helper;
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public void index(HttpServletResponse response)
    {
        DatasetSearchResult datasets = metadataRepository.findPublicDatasets();
        List<MetadataWithOwner> items = mergeRifcsItems(datasets);
        RegistryObjects resp = buildRegistryObjects(items);
        response.setContentType("text/xml");
        try
        {
            rifcsService.marshallRegistryObjects(resp, response.getOutputStream());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Catched IOException: " + e.getMessage(), e.getCause() != null ? e.getCause()
                    : e);
        }
    }

    private List<MetadataWithOwner> mergeRifcsItems(DatasetSearchResult datasets)
    {
        List<MetadataWithOwner> items = new ArrayList<MetadataWithOwner>();
        if (datasets != null && datasets.getDatasets() != null && datasets.getDatasets().size() > 0)
        {
            for (int i = 0; i < datasets.getDatasets().size(); i++)
            {
                Dataset dataset = datasets.getDatasets().get(i);
                for (MetadataItem metadata : dataset.getMetadata())
                {
                    if (metadata.getSchema() == MetadataSchema.RIF_CS)
                    {
                        items.add(new MetadataWithOwner(metadata.getMetadata(), dataset.getOwner()));
                    }
                }
            }
        }
        return items;
    }

    private RegistryObjects buildRegistryObjects(List<MetadataWithOwner> items)
    {
        OriginatingSource origSource = new RegistryObjects.RegistryObject.OriginatingSource();
        origSource.setValue("http://www.ammrf.org.au/");
        RegistryObjects resp = new RegistryObjects();
        Map<String, RegistryObject> owners = new HashMap<String, RegistryObject>();
        for (MetadataWithOwner item : items)
        {
            RegistryObjects objs = rifcsService.unmarshallRegistryObjects(item.getMetadata());
            if (objs != null)
            {
                RegistryObject owner = getHelper().returnOrCreateOwner(origSource, owners, item.getOwner());
                for (RegistryObject collection : objs.getRegistryObject())
                {
                    getHelper().linkOwner(owner, collection);
                }
                resp.getRegistryObject().addAll(objs.getRegistryObject());
            }
        }
        resp.getRegistryObject().addAll(owners.values());
        return resp;
    }

    /**
     * Method to show metadata in the catalogue module
     * 
     * @param url
     * @return
     */
    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public ModelAndView view(@RequestParam(value = "id") Long id)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        return new ModelAndView("metadata/view", model);
    }

    /**
     * Just a data holder
     * 
     * @author carlos
     * 
     */
    private static class MetadataWithOwner
    {
        private String metadata;
        private String owner;

        MetadataWithOwner(String metadata, String owner)
        {
            this.metadata = metadata;
            this.owner = owner;
        }

        public String getMetadata()
        {
            return metadata;
        }

        public String getOwner()
        {
            return owner;
        }
    }

}
