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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import au.org.intersect.dms.bookinggw.Booking;
import au.org.intersect.dms.bookinggw.BookingGatewayInterface;
import au.org.intersect.dms.bookinggw.BookingGatewayMetadataService;
import au.org.intersect.dms.bookinggw.Project;
import au.org.intersect.dms.catalogue.MetadataXmlConverter;
import au.org.intersect.dms.catalogue.MetadataXmlConverter.ConversionParams;
import au.org.intersect.dms.catalogue.MetadataXmlConverter.Format;
import au.org.intersect.dms.core.catalogue.MetadataSchema;
import au.org.intersect.dms.core.domain.FileInfo;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.core.service.WorkerNode;
import au.org.intersect.dms.core.service.dto.IngestParameter;
import au.org.intersect.dms.encrypt.EncryptionAgent;
import au.org.intersect.dms.encrypt.EncryptionAgentException;
import au.org.intersect.dms.tunnel.HddUtil;
import au.org.intersect.dms.webapp.SecurityContextFacade;
import au.org.intersect.dms.webapp.domain.StockServer;
import au.org.intersect.dms.webapp.domain.StockServerType;
import au.org.intersect.dms.webapp.json.AjaxResponse;
import au.org.intersect.dms.webapp.json.TreeNode;

/**
 * Controller to serve location tab.
 */
// TODO CHECKSTYLE-OFF: ClassFanOutComplexity
// TODO Refactor to fix ClassFanOutComplexity violation
@RequestMapping("/location/**")
@Controller
@Transactional("web")
public class LocationController
{

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationController.class);

    private static final String CONNECTIONS = "connections";

    @Autowired
    @Qualifier("dmsClient")
    private DmsService dmsService;

    @Autowired
    private BookingGatewayInterface bookingSystem;

    @Autowired
    private BookingGatewayMetadataService bookingSystemMetadataService;

    @Autowired
    private MetadataXmlConverter metadataConverter;

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    private EncryptionAgent agent;

    @RequestMapping(method = RequestMethod.POST, value = "/connect")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse connect(@ModelAttribute LocationConnectForm form)
    {
        StockServer serverDesc = StockServer.findStockServer(form.getStockId());
        if (serverDesc == null)
        {
            return new AjaxResponse(null, "Invalid argument");
        }
        String protocol = serverDesc.getProtocol();
        String server = StringUtils.isEmpty(serverDesc.getServer()) ? form.getServer() : serverDesc.getServer();
        String username = null;
        String password = null;
        switch (serverDesc.getCredentialsOption())
        {
            case NONE:
                break;
            case ASK:
                username = form.getUsername();
                password = form.getPassword();
                break;
            case FIXED:
                username = serverDesc.getUsername();
                password = serverDesc.getPassword();
                break;
            default:
                return new AjaxResponse(null, "Internal error: Unknown credentials option in the database");
        }
        Integer connectionId = dmsService.openConnection(protocol, server, username, password);
        // we need these two data for the getConnections reply (used in browse.js)
        form.setConnectionId(connectionId);
        form.setDescription(serverDesc.getDescription());
        form.setProtocol(serverDesc.getProtocol());
        form.setType(serverDesc.getType());
        form.setInstrumentProfile(serverDesc.getInstrumentProfile());
        addConnection(form);
        return new AjaxResponse(connectionId, null);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/closeConnection")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse closeConnecton(@RequestParam Integer connectionId)
    {
        deleteConnection(connectionId);
        return new AjaxResponse(connectionId, null);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/getConnections")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse getConnections()
    {
        return new AjaxResponse(getCurrentConnections().toArray(), null);
    }

    private List<LocationConnectForm> getCurrentConnections()
    {
        List<LocationConnectForm> connections = (List<LocationConnectForm>) RequestContextHolder
                .currentRequestAttributes().getAttribute(CONNECTIONS, RequestAttributes.SCOPE_SESSION);
        if (connections == null)
        {
            connections = new LinkedList<LocationConnectForm>();
        }
        return connections;
    }

    private void updateCurrectConnections(List<LocationConnectForm> connections)
    {
        RequestContextHolder.currentRequestAttributes().setAttribute(CONNECTIONS, connections,
                RequestAttributes.SCOPE_SESSION);
    }

    private void addConnection(LocationConnectForm form)
    {
        List<LocationConnectForm> connections = getCurrentConnections();
        connections.add(form);
        updateCurrectConnections(connections);
    }

    private void deleteConnection(Integer connectionId)
    {
        List<LocationConnectForm> connections = getCurrentConnections();

        for (Iterator<LocationConnectForm> iterator = connections.iterator(); iterator.hasNext();)
        {
            LocationConnectForm locationConnectForm = iterator.next();
            if (connectionId.equals(locationConnectForm.getConnectionId()))
            {
                iterator.remove();
                updateCurrectConnections(connections);
                break;
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/list")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse list(@RequestParam Integer connectionId, @RequestParam String path)
    {
        List<FileInfo> resp = dmsService.getList(connectionId, path);
        List<TreeNode> result = new LinkedList<TreeNode>();
        for (FileInfo fileInfo : resp)
        {
            result.add(new TreeNode(fileInfo));
        }
        return new AjaxResponse(result, null);
    }

    /**
     * Renames file/directory
     * 
     * @param protocol
     *            connection protocol
     * @param connectionId
     *            connection ID
     * @param from
     *            absolute path of the file/directory to rename
     * @param to
     *            new (relative) name
     * @return true if rename was successful, false otherwise
     */
    @RequestMapping(method = RequestMethod.POST, value = "/rename")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse rename(@RequestParam Integer connectionId, @RequestParam String from, @RequestParam String to)
    {
        boolean response = dmsService.rename(connectionId, from, to);
        return new AjaxResponse(response, null);
    }

    /**
     * Creates directory
     * 
     * @param protocol
     *            connection protocol
     * @param connectionId
     *            connection ID
     * @param parent
     *            absolute path to the parent directory (where new one should be created)
     * @param name
     *            name of the new directory
     * @return true if directory was created, false otherwise
     */
    @RequestMapping(method = RequestMethod.POST, value = "/create")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse create(@RequestParam Integer connectionId, @RequestParam String parent,
            @RequestParam String name)
    {
        boolean response = dmsService.createDir(connectionId, parent, name);
        return new AjaxResponse(response, null);
    }

    /**
     * Deletes files and directories
     * 
     * @param protocol
     *            connection protocol
     * @param connectionId
     *            connection ID
     * @param fileSelection
     *            files and directories to delete
     * @return true if deleted successfully, false otherwise
     */
    @RequestMapping(method = RequestMethod.POST, value = "/delete")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse delete(@RequestParam Integer connectionId, FileListWrapper fileSelection)
    {
        boolean response = dmsService.delete(connectionId, fileSelection.getFiles());
        return new AjaxResponse(response, null);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/stockServers")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse listStockServers()
    {
        List<StockServer> base = StockServer.findAllStockServers();
        Collections.sort(base, new Comparator<StockServer>()
        {

            @Override
            public int compare(StockServer o1, StockServer o2)
            {
                if (o1.getType() == o2.getType())
                {
                    return o1.getDescription().compareTo(o2.getDescription());
                }
                else
                {
                    return o1.getType().compareTo(o2.getType());
                }
            }
        });

        List<Map<String, Object>> resp = new ArrayList<Map<String, Object>>();
        Iterator<StockServer> i = base.iterator();
        while (i.hasNext())
        {
            resp.add(i.next().toMap());
        }
        return new AjaxResponse(resp, null);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/checkCommonWorker")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse checkCommonWorker(@RequestParam(value = "connectionIdFrom") Integer connectionIdFrom,
            @RequestParam(value = "connectionIdTo") Integer connectionIdTo)

    {
        Boolean checkValidWorkerExists = dmsService.checkForValidRouting(connectionIdFrom, connectionIdTo);
        return new AjaxResponse(checkValidWorkerExists, null);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/copyJob")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse copy(@RequestParam(value = "source_connectionId") Integer sourceConnectionId,
            @RequestParam(value = "source_item") List<String> sourceItem,
            @RequestParam(value = "destination_connectionId") Integer destinationConnectionId,
            @RequestParam(value = "destination_item") String destinationItem,
            @RequestParam(value = "encode", required = false) Boolean encode)
    {
        StockServer toServer = getStockServer(destinationConnectionId);

        if (StockServerType.INSTRUMENT == toServer.getType())
        {
            throw new IllegalArgumentException("Instrument is read only.");
        }
        String username = securityContextFacade.getAuthorizedUsername();
        Long jobId = dmsService
                .copy(username, sourceConnectionId, sourceItem, destinationConnectionId, destinationItem);

        if (encode != null && encode)
        {
            try
            {
                byte[] encjobId = agent.process((jobId.toString()).getBytes());
                String jobIdAsHexString = HddUtil.convertByteToHexString(encjobId);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("jobId", jobId);
                map.put("encJobId", jobIdAsHexString);
                return new AjaxResponse(map, null);

            }
            catch (EncryptionAgentException e)
            {
                throw new RuntimeException("The web-app could not encrypt your Job ID correctly; "
                        + "Therefore this job will not be executed.");
            }
        }
        return new AjaxResponse(jobId, null);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/ingest")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse ingest(@RequestParam(value = "source_connectionId") Integer sourceConnectionId,
            @RequestParam(value = "source_item") String sourceItem,
            @RequestParam(value = "destination_connectionId") Integer destinationConnectionId,
            @RequestParam(value = "destination_item") String destinationItem,
            @RequestParam(required = false) Long projectCode,
            @RequestParam(value = "copyToWorkstation", defaultValue = "false") boolean copyToWorkstation,
            HttpServletRequest request)
    {
        StockServer fromServer = getStockServer(sourceConnectionId);
        if (StockServerType.INSTRUMENT != fromServer.getType())
        {
            throw new IllegalArgumentException("Ingestion is possible only from an instrument.");
        }
        StockServer toServer = getStockServer(destinationConnectionId);
        if (StockServerType.REPOSITORY != toServer.getType())
        {
            throw new IllegalArgumentException("Ingestion is possible only to catalogued data storage.");
        }
        LOGGER.debug("Ingestion to server: {} and copy to workstation: {}", toServer.getDescription(),
                copyToWorkstation);

        String username = securityContextFacade.getAuthorizedUsername();
        String url = getDatasetURL(destinationConnectionId, sourceItem, destinationItem);
        Map<String, Object> params = CatalogueController.extractMetadataParameters(url, request);

        String metadata = bookingSystemMetadataService.getMetadata(params);

        IngestParameter ingestParameters = new IngestParameter(username, null, sourceConnectionId, sourceItem,
                destinationConnectionId, destinationItem, fromServer.getInstrumentProfile());
        ingestParameters.setCopyToWorkstation(copyToWorkstation);
        Long jobId = dmsService.ingest(username, projectCode, metadata, ingestParameters);
        return new AjaxResponse(jobId, null);
    }

    /**
     * Displays projects from the booking system of the currently logged-in user
     * 
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/projects")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse getProjects()
    {
        String username = securityContextFacade.getAuthorizedUsername();
        Project[] projects = bookingSystem.getProjects(username);
        return new AjaxResponse(projects, null);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/bookings")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse getBookings(@RequestParam(value = "source_connectionId") Integer sourceConnectionId,
            @RequestParam Date fromDate, @RequestParam Date toDate)
    {
        String username = securityContextFacade.getAuthorizedUsername();
        StockServer fromServer = getStockServer(sourceConnectionId);
        if (StockServerType.INSTRUMENT != fromServer.getType())
        {
            throw new IllegalArgumentException("Bookings can be retrived only for an instrument.");
        }
        LOGGER.debug("Retriving bookings for user {}, instrument {}, from {}, to {}", new Object[] {username,
            fromServer.getInstrumentId(), fromDate, toDate});
        Booking[] bookings = bookingSystem.getBookingsRange(username, fromServer.getInstrumentId(), fromDate, toDate);
        return new AjaxResponse(bookings, null);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/metadata")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse prefillMetadata(@RequestParam(required = false) Long projectCode,
            @RequestParam(required = false) Long bookingId, @RequestParam(value = "source_item") String sourceItem,
            @RequestParam(value = "destination_connectionId") Integer destinationConnectionId,
            @RequestParam(value = "destination_item") String destinationItem) throws IOException, TransformerException
    {
        String username = securityContextFacade.getAuthorizedUsername();

        String destinationURL = getDatasetURL(destinationConnectionId, sourceItem, destinationItem);
        String metadata = bookingSystemMetadataService.getMetadata(projectCode, bookingId, destinationURL);
        ConversionParams conversionParams = new ConversionParams();
        conversionParams.schema(MetadataSchema.RIF_CS).desinationFormat(Format.HTML).edit(true)
                .extraParam("owner", username).metadata(metadata);
        String form = metadataConverter.convert(conversionParams);
        return new AjaxResponse(form, null);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/metadata")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse confirmMetadata(@RequestParam(value = "source_item") String sourceItem,
            @RequestParam(value = "destination_connectionId") Integer destinationConnectionId,
            @RequestParam(value = "destination_item") String destinationItem, HttpServletRequest request)
        throws IOException, TransformerException
    {
        String username = securityContextFacade.getAuthorizedUsername();

        String url = getDatasetURL(destinationConnectionId, sourceItem, destinationItem);
        Map<String, Object> params = CatalogueController.extractMetadataParameters(url, request);

        String metadata = bookingSystemMetadataService.getMetadata(params);
        ConversionParams conversionParams = new ConversionParams();
        conversionParams.schema(MetadataSchema.RIF_CS).desinationFormat(Format.HTML).extraParam("owner", username)
                .metadata(metadata);
        String convertedMetadata = metadataConverter.convert(conversionParams);
        return new AjaxResponse(convertedMetadata, null);
    }

    // TODO wrong URL is returned if copy to '/'. Fix it and extract method into central URL manipulation utility class
    private String getDatasetURL(Integer destinationConnectionId, String sourcePath, String targetPath)
    {
        StringBuilder path = new StringBuilder(targetPath);
        path.append(sourcePath.substring(sourcePath.lastIndexOf("/")));
        return dmsService.getUrl(destinationConnectionId, path.toString());
    }

    @RequestMapping
    public String index()
    {
        return "location/index";
    }

    private StockServer getStockServer(Integer connectionId)
    {
        if (WorkerNode.HDD_CONNECTION_ID.equals(connectionId))
        {
            return StockServer.findStockServersByProtocol("hdd").getSingleResult();
        }
        List<LocationConnectForm> currentConnections = getCurrentConnections();
        StockServer server = null;
        for (LocationConnectForm locationConnectForm : currentConnections)
        {
            if (connectionId.equals(locationConnectForm.getConnectionId()))
            {
                server = StockServer.findStockServer(locationConnectForm.getStockId());
                break;
            }
        }
        if (server == null)
        {
            throw new IllegalArgumentException("Can't resolve server by connection ID " + connectionId);
        }
        else
        {
            return server;
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }

}
