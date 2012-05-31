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
    var $renameForm = $('#renameForm');
    
    var $renameDialog = $('#renameDialog').dialog({
    	modal: true,
    	autoOpen: false,
    	width: 400, 
    	height: 200,
    	open: function() {
    		var $tree = $renameForm.data('tree');
    		var $selectedFile = $tree.jstree('get_selected');
    		$renameForm.data('selectedNode', $selectedFile);
    		$renameForm.find('#connectionId').val($tree.data('connectionData').connectionId);
    		$renameForm.find('#from').val($selectedFile.data().jstree.absolutePath);
    		$renameForm.find('#to').val($selectedFile.data().jstree.name);
    	},
    	buttons: {
    		Rename: function() {
    			$renameForm.ajaxSubmit(options);
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
            	$renameDialog.dialog('close');
                if(response.data == true) {
                    var $treeNode = $renameForm.data('tree');
                    var tree = $.jstree._reference($treeNode);
                    var $selectedNode = $renameForm.data('selectedNode');
                    tree.refresh(tree._get_parent($selectedNode));
                    $.dms_location_hideMetadata($treeNode.data('metadataId'));
                } else if(response.data == false){
                	$(this).dialog("close"); 
                	$messageDialog.showMessage('Unable to Rename. Please ensure your input is valid and is different from the original name.', {title:'Error'})
                }/*else if(response.data == false) {
                	$renameDialog.dialog("Failed to rename selected file/directory.", {title:'Error'});   
                } */else {
                	$(this).dialog("close"); 
                	$messageDialog.showMessage(response.error, {title:'Error'})
                	//$renameDialog.showMessage(response.error, {title:'Error'});
                }
            }
    };
    
    $renameForm.submit(function(){
    	$(this).ajaxSubmit(options);
    	return false;
    });
    
});
