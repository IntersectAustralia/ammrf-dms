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
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import au.org.intersect.dms.util.DateFormatter;
import au.org.intersect.dms.webapp.json.AjaxResponse;

/**
 * Utility controller to format dates
 */
@RequestMapping("/dateformat/**")
@Controller
public class DateFormatController
{
    @RequestMapping(method = RequestMethod.GET, value = "/convertDates")
    @AjaxMethod
    @ResponseBody
    public AjaxResponse convertDates(@RequestParam Map<String, String> dates)
    {
        Map<String, String> result = new HashMap<String, String>();

        for (Entry<String, String> property : dates.entrySet())
        {
            String propertyName = property.getKey();
            DateTime propertyValue = DateFormatter.parseDateTime(property.getValue());
            result.put(propertyName, DateFormatter.formatDate2UTC(propertyValue));
        }

        return new AjaxResponse(result, null);
    }
}
