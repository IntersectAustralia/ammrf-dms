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

$(function()
{
    var $messageDialog = $("#messageDialog");
    
    function loadOptions($element, url, mapping)
    {
        $.ajax(
        {
            url : url,
            dataType : 'json',
            success : function(ajaxResponse)
            {
                $element.html('');
                if (ajaxResponse.data == null)
                {
                    $messageDialog.showMessage(ajaxResponse.error);
                    // TODO hide form
                }
                else
                {
                    var data = ajaxResponse.data;
                    $element.data('data', data);
                    $(data).each(function(i, item)
                    {
                        $('<option/>').attr('value', item[mapping.value]).html(item[mapping.text]).appendTo($element);
                    });
                    $element.change();
                }
            }
        });
    }

    var $stockServerSelect = $('#stockServer');
    loadOptions($stockServerSelect, 'stockServers',
    {
        value : 'id',
        text : 'description'
    });

    $stockServerSelect.change(function(e)
    {
        var data = $(e.currentTarget).data('data');
        if (data == undefined)
        {
            return false;
        }
        var index = e.currentTarget.selectedIndex;
        if (index < 0 || index >= data.length)
        {
            return false;
        }
        var selection = data[index];
        
        $('#protocol').val(selection.protocol);
    	
        if ((selection.server == null || selection.server == '') && selection.protocol != 'hdd')
        {
        	$('#server').val('');
            $('.server_fields').show();
        }
        else
        {
        	$('#server').val(selection.server);
            $('.server_fields').hide();
        }
        if (selection.credentials == 'ASK')
        {
            $('.credentials_fields').show();
        }
        else
        {
        	$('#username').val('');
        	$('#password').val('');
            $('.credentials_fields').hide();
        }
        
        $('#type').val(selection.type);
    });
});
