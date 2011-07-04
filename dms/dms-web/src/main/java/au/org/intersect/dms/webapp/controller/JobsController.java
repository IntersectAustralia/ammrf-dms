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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.core.domain.JobItem;
import au.org.intersect.dms.core.domain.JobSearchResult;
import au.org.intersect.dms.core.errors.NotAuthorizedError;
import au.org.intersect.dms.core.service.DmsService;
import au.org.intersect.dms.webapp.SecurityContextFacade;
import au.org.intersect.dms.webapp.json.AjaxResponse;

/**
 * Home controller, nothing for now
 */
@RequestMapping("/jobs/**")
@Controller
@Transactional("web")
public class JobsController
{
    @Autowired
    private DmsService dmsService;

    @Autowired
    private SecurityContextFacade securityContextFacade;
    
    @RequestMapping
    public String index()
    {
        return "jobs/index";
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/jobStatus")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse jobStatus(@RequestParam(value = "jobId") long jobId)
    {
        String username = getUsernameFromContext();
        JobItem job = dmsService.getJobStatus(username, jobId);
        return new AjaxResponse(job, null);
    }
    
    
    @RequestMapping(method = RequestMethod.GET, value = "/listRecentJobs")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse listRecentJobs(@RequestParam(value = "iDisplayStart") int startIndex,
            @RequestParam(value = "iDisplayLength") int pageSize, @RequestParam(value = "sEcho") int token)
    {
        String username = getUsernameFromContext();
        JobSearchResult resp = dmsService.getJobs(username, startIndex, pageSize);
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("totalRecords", resp.getTotalSize());
        result.put("sEcho", token);
        result.put("jobs", resp.getJobs());
        
        return new AjaxResponse(result, null);
    }    

    @RequestMapping(method = RequestMethod.GET, value = "/jobCancel")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse jobCancel(@RequestParam(value = "jobId") long jobId)
    {
        String username = getUsernameFromContext();
        Boolean resp = dmsService.stopJob(username, jobId);
        return new AjaxResponse(resp, null);
    }
    
    private String getUsernameFromContext()
    {
        Authentication authentication = securityContextFacade.getContext().getAuthentication();
        if (authentication == null)
        {
            throw new NotAuthorizedError("User is not logged in.");
        }
        return authentication.getName();
    }
        
}
