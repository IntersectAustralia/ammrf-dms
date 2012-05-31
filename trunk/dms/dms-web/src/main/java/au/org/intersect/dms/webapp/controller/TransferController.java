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
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Home controller, nothing for now
 */
@RequestMapping("/transfer/**")
@Controller
public class TransferController
{

    @Value("#{dmsProperties['dms.applet.tunnelUrl']}")
    private String tunnelUrl;
    
    @RequestMapping
    @AjaxMethod
    public ModelAndView index(@RequestParam(value = "source_connectionId") Integer sourceConnectionId,
            @RequestParam(value = "source_item") List<String> sourceItem,
            @RequestParam(value = "destination_connectionId") Integer destinationConnectionId,
            @RequestParam(value = "destination_item") String destinationItem)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("source_connectionId", sourceConnectionId);
        map.put("source_item", sourceItem);
        map.put("destination_connectionId", destinationConnectionId);
        map.put("destination_item", destinationItem);
        map.put("tunnelUrl", tunnelUrl);
        return new ModelAndView("transfer/index", map);
    }

}
