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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.BookingGatewayMetadataService;
import au.org.intersect.dms.bookinggw.Project;
import au.org.intersect.dms.bookinggw.ProjectDetails;
import au.org.intersect.dms.catalogue.Dataset;
import au.org.intersect.dms.catalogue.DatasetNotFoundException;
import au.org.intersect.dms.catalogue.DatasetSearchResult;
import au.org.intersect.dms.catalogue.MetadataItem;
import au.org.intersect.dms.catalogue.MetadataRepository;
import au.org.intersect.dms.catalogue.MetadataXmlConverter;
import au.org.intersect.dms.catalogue.MetadataXmlConverter.ConversionParams;
import au.org.intersect.dms.catalogue.MetadataXmlConverter.Format;
import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.errors.NotAuthorizedError;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.integration.search.Property;
import au.org.intersect.dms.service.domain.DmsUser;
import au.org.intersect.dms.webapp.SecurityContextFacade;
import au.org.intersect.dms.webapp.json.AjaxResponse;

/**
 * Home controller, nothing for now
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexity
// TODO Refactor to fix ClassFanOutComplexity violation
@RequestMapping("/catalogue/**")
@Controller
@Transactional("web")
public class CatalogueController
{
    private static final String OWNER = "owner";

    @Autowired
    @Qualifier("metadataRepository")
    private MetadataRepository metadataRepository;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    @Qualifier("dmsClient")
    private DmsService dmsService;

    @Autowired
    private MetadataXmlConverter metadataXmlConverter;

    @Autowired
    private AdvancedSearchPropertiesBuilder advancedSearchPropertiesBuilder;

    @Autowired
    private BookingGatewayInterface bookingSystem;

    @Autowired
    private BookingGatewayMetadataService bookingSystemMetadataService;

    @RequestMapping(value = "/index")
    public String index()
    {
        return "catalogue/index";
    }

    /**
     * Method to show metadata in the catalogue module
     * 
     * @param url
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public ModelAndView view(@RequestParam(value = "url") String url) throws IOException, TransformerException
    {
        return findMetadataForURL(url, "catalogue/view", true);
    }

    /**
     * Returns list of datasets belonging to the currently logged in user filtered by search terms
     * 
     * @param query
     *            search terms
     * @param startIndex
     *            start index of the result (used for pagination)
     * @param pageSize
     *            number of datasets per page
     * @param token
     *            pagination conversation token (see http://www.datatables.net/usage/server-side for details)
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/search")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse search(@RequestParam(value = "sSearch", defaultValue = "") String query,
            @RequestParam(value = "iDisplayStart") int startIndex,
            @RequestParam(value = "iDisplayLength") int pageSize, @RequestParam(value = "sEcho") int token)
    {
        String username = getSessionUsername();
        Project[] projects = bookingSystem.getProjects(username);
        List<Long> userProjectCodes = new ArrayList<Long>(projects.length);
        for (Project project : projects)
        {
            userProjectCodes.add(project.getProjectCode());
        }

        DatasetSearchResult searchResult;
        if (isAdminUser(username))
        {
            searchResult = metadataRepository.findDatasets(query, startIndex, pageSize);
        }
        else
        {
            searchResult = metadataRepository.findDatasets(username, userProjectCodes, query, startIndex, pageSize);
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalRecords", searchResult.getTotalSize());
        result.put("sEcho", token);
        result.put("iDisplayStart", startIndex);
        result.put("iDisplayLength", pageSize);
        result.put("datasets", searchResult.getDatasets());

        return new AjaxResponse(result, null);
    }

    private boolean isAdminUser(String username)
    {
        DmsUser user = DmsUser.findDmsUsersByUsername(username).getSingleResult();
        return user.isAdmin();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/advancedSearch")
    public ModelAndView displayAdvancedSearch()
    {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<MetadataSchema, List<Property>> properties = advancedSearchPropertiesBuilder.getProperties();
        model.put("properties", properties);

        return new ModelAndView("catalogue/advancedSearch", model);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/visibility")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse changeVisibility(@RequestParam(value = "url") String url)
    {
        Dataset dataset = metadataRepository.findDatasetByUrl(url);

        return new AjaxResponse(dataset.isVisible() ? metadataRepository.makePrivate(url)
                : metadataRepository.makePublic(url), null);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getProjects")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse getProjects()
    {
        String username = getSessionUsername();
        Project[] projects = bookingSystem.getProjects(username);
        return new AjaxResponse(projects, null);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/changeProject")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse changeProject(@RequestParam(value = "url") String url, @RequestParam(value = "code") Long code)
    {
        Dataset dataset = metadataRepository.findDatasetByUrl(url);
        dataset.setProjectCode(code);
        metadataRepository.updateDataset(dataset);
        return new AjaxResponse(true, null);
    }

    /**
     * Method to show metadata in the location module
     * 
     * @param path
     * @param connectionId
     * @param response
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/metadata")
    public ModelAndView getMetadata(@RequestParam(value = "path") String path,
            @RequestParam(value = "connectionId") int connectionId, HttpServletResponse response) throws IOException,
        TransformerException
    {
        String url = dmsService.getUrl(connectionId, path);
        return findMetadataForURL(url, "catalogue/viewMetadata", true);
    }

    /**
     * Method to preview metadata prior to publishing
     * 
     * @param path
     * @param connectionId
     * @param response
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = "/previewData")
    public ModelAndView previewData(@RequestParam(value = "url") String url) throws IOException, TransformerException
    {
        return findMetadataForURL(url, "catalogue/previewMetadata", false);
    }

    /**
     * Populates editable metadata for user edit.
     * 
     * @param url
     * @param schema
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    @RequestMapping(value = "/editMetadata")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse editMetadata(@RequestParam(value = "url") String url,
            @RequestParam(value = "schema") MetadataSchema schema) throws IOException, TransformerException
    {
        Dataset dataset = metadataRepository.findDatasetByUrl(url);

        if (metadataRepository.isEditableSchema(schema))
        {
            ConversionParams conversionParams = new ConversionParams();
            for (MetadataItem metadataItem : dataset.getMetadata())
            {
                if (schema == metadataItem.getSchema())
                {
                    conversionParams.metadata(metadataItem.getMetadata());
                    break;
                }
            }
            conversionParams.schema(schema).desinationFormat(Format.HTML).edit(true)
                    .extraParam(OWNER, dataset.getOwner()).extraParam("date", dataset.getCreationDate());

            String metadata = metadataXmlConverter.convert(conversionParams);
            return new AjaxResponse(metadata, null);
        }
        else
        {
            throw new IllegalArgumentException("Metadata for schema " + schema + " is not editable.");
        }

    }

    /**
     * Updates metadata.
     * 
     * @param url
     * @param schema
     * @param request
     * @return
     */
    @RequestMapping(value = "/updateMetadata")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse updateMetadata(@RequestParam(value = "url") String url,
            @RequestParam(value = "schema") MetadataSchema schema, HttpServletRequest request)
    {
        Dataset dataset = metadataRepository.findDatasetByUrl(url);

        Map<String, Object> params = extractMetadataParameters(url, request);
        String metadata = bookingSystemMetadataService.getMetadata(params);
        dataset.setMetadata(schema, metadata);
        metadataRepository.updateDataset(dataset);

        return new AjaxResponse(true, null);
    }

    /**
     * Updates metadata.
     * 
     * @param url
     * @param schema
     * @param request
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "/changeOwner")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse changeOwner(@RequestParam(value = "url") String url, 
            @RequestParam(value = "owner") String owner)
    {
        Dataset dataset = metadataRepository.findDatasetByUrl(url);
        dataset.setOwner(owner);
        metadataRepository.updateDataset(dataset);
        return new AjaxResponse(true, null);
    }

    /**
     * Helper method to extract metadata fields from request for display/edit
     * 
     * @param datasetUrl
     * @param request
     * @return
     */
    public static Map<String, Object> extractMetadataParameters(String datasetUrl, HttpServletRequest request)
    {
        Map<String, Object> params = new HashMap<String, Object>();
        Map requestParams = request.getParameterMap();
        for (Object param : requestParams.entrySet())
        {
            Entry paramEntry = (Entry) param;
            String name = (String) paramEntry.getKey();
            if (name.startsWith("md_"))
            {
                params.put(name, paramEntry.getValue());
            }
        }
        params.put("url", datasetUrl);
        return params;
    }

    /**
     * Returns ModelAndView, note that viewBase is appended "None" if the dataset is not found (one still has the url in
     * the model)
     * 
     * @param url
     * @param viewBase
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    private ModelAndView findMetadataForURL(String url, String viewBase, boolean everything) throws IOException,
        TransformerException
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("url", url);
        try
        {
            Dataset dataset = metadataRepository.findDatasetByUrl(url);
            model.put("projectCode", dataset.getProjectCode());
            model.put("visible", dataset.isVisible());
            model.put("projectTitle", "None");
            model.put(OWNER, dataset.getOwner());
            if (dataset.getProjectCode() != null && dataset.getProjectCode().longValue() > 0)
            {
                ProjectDetails projectDetails = bookingSystem.getProjectDetails(dataset.getProjectCode(), 0);
                model.put("projectTitle", projectDetails.getTitle());
            }
            List<Map<String, Object>> metadata = new LinkedList<Map<String, Object>>();
            for (MetadataItem metadataItem : dataset.getMetadata())
            {
                if (metadataXmlConverter.isConvertionSupported(metadataItem.getSchema(), Format.HTML)
                        && (everything || metadataRepository.isPublicSchema(metadataItem.getSchema())))
                {
                    ConversionParams conversionParams = new ConversionParams();
                    conversionParams.schema(metadataItem.getSchema()).desinationFormat(Format.HTML)
                            .extraParam(OWNER, dataset.getOwner()).extraParam("date", dataset.getCreationDate())
                            .metadata(metadataItem.getMetadata());

                    String strItem = metadataXmlConverter.convert(conversionParams);
                    Map<String, Object> item = new HashMap<String, Object>();
                    item.put("data", strItem);
                    item.put("schema", metadataItem.getSchema());
                    item.put("isEditable", metadataRepository.isEditableSchema(metadataItem.getSchema()));
                    metadata.add(item);
                }
            }
            model.put("metadata", metadata);
            return new ModelAndView(viewBase, model);
        }
        catch (DatasetNotFoundException e)
        {
            return new ModelAndView(viewBase + "None", model);
        }
    }

    private String getSessionUsername()
    {
        Authentication authentication = securityContextFacade.getContext().getAuthentication();
        if (authentication == null)
        {
            throw new NotAuthorizedError("User is not logged in.");
        }
        return authentication.getName();
    }

}
