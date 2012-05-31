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
    var $messageDialog = $("#messageDialog");
    var $form = $('#createForm');
    
    var $createDialog = $('#createDialog').dialog({
        modal: true,
        autoOpen: false,
        width: 400, 
        height: 200,
        open: function() {
            var $tree = $form.data('tree');
            var $selectedDirectory = $tree.jstree('get_selected');
            $form.data('selectedNode', $selectedDirectory);
            $form.find('#connectionId').val($tree.data('connectionData').connectionId);
            $form.find('#parent').val($selectedDirectory.data().jstree.absolutePath);
            $form.find('#name').val('');
        },
        buttons: {
            "Create Folder": function() {
                $form.ajaxSubmit(options);    
                $(this).dialog('close');
            },
            Cancel: function() {
                $(this).dialog('close');
            }
        }
    });
    var options = {
    		dataType: 'json',
    		success: function(response) {
    			$createDialog.dialog('close');
    			if(response.data == true) {
    				var tree = $.jstree._reference($form.data('tree'));
    				var $selectedNode = $form.data('selectedNode');
    				tree.refresh($selectedNode);
                }else if(response.data == false) {
                	$(this).dialog("close"); 
    				$messageDialog.showMessage('Unable to Create Folder. Please ensure your input is valid and is different from all other folder names in your selected directory.', {title:'Error'});
    			} else {
    				$(this).dialog("close");
    				$messageDialog.showMessage(response.error, {title:'Error'});
    			}
    		}
    };
    
    $form.submit(function(){
    	$(this).ajaxSubmit(options);
    	return false;
    });
});
