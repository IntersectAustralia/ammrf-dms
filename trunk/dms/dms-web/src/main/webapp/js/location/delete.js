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
    
    var $deleteDialog = $('#deleteDialog').dialog({
        modal: true,
        autoOpen: false,
        width: 700, 
        height: 400,
        open: function() {
            var $selectedFiles = $(this).data('tree').jstree('get_selected');
            var list = $(this).find("ul").empty();
            $selectedFiles.each(function(index, file){
                $("<li>" + $(file).data().jstree.absolutePath + "</li>").appendTo(list);
             });
        },
        buttons: {
            Delete: function() {
                var $tree = $(this).data('tree');
                var $selectedFiles = $tree.jstree('get_selected');
                var postData = [{name:"connectionId", value:$tree.data('connectionData').connectionId}];
                $selectedFiles.each(function(index, file){
                   postData.push({name:"files[" + index + "].fileType", value:$(file).data().jstree.fileType}); 
                   postData.push({name:"files[" + index + "].absolutePath", value:$(file).data().jstree.absolutePath});
                });
                
                $.ajax({
                    url:'delete',
                    type:'POST',
                    dataType:'json',
                    data:postData,
                    success: function(response) {
                        if(response.data == true) {
                            $tree.jstree('refresh');
                            $.dms_location_hideMetadata($tree.data('metadataId'));
                        } else if(response.data == false) {
                            $messageDialog.showMessage("Failed to delete selected files/directories.", {title:'Error'});
                        } else {
                            $messageDialog.showMessage(response.error, {title:'Error'});
                        }
                    }
                });
                $(this).dialog('close');
            },
            Cancel: function() {
                $(this).dialog('close');
            }
        }
    });
});
