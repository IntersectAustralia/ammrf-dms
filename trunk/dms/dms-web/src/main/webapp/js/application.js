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

$(function() {
    $("#spinner").ajaxSend(function(event, request, settings) {
        $(this).fadeIn();
    }).ajaxComplete(function(event, request, settings) {
        $(this).fadeOut();
    });
    
    $.ajaxSetup({
    	timeout: 300000, // global AJAX requests timeout (60 seconds)
    	error: function(request, textStatus, errorThrown) {
		$.dms_defaultAjaxErrorHandler(request, textStatus, errorThrown);
	}
    });
});

$.dms_defaultAjaxErrorHandler = function(request, textStatus, errorThrown) {
    if (request.status == 401) {
        document.location.reload();
    } else {
	//TODO use proper jquery messaging plugin
        if(textStatus=='timeout') {
    		alert('Request Time out.');
        } else {
    		alert('Server error happened while processing your request.');
        }
    }
};
