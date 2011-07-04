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
	var $messageDialog = $("#messageDialog").createMessageDialog({title:"Error"});
	
	$(".expandable").expandable({
		startopen: true,
		tooltip: "Click to expand/collapse"
	});
	$("#change").button();
	$(".editMetadataButton").button();
	
	//Add state to 'Back to catalogue' link
	var state = $.bbq.getState();
	$("#goBack").fragment(state);
	
	var $editMetadataForm = $("#editMetadataForm");
	
	var options = {
		dataType: 'json',
        success: function(response) {
        	$(this).dialog("close");
            if(response.data == true) {
            	document.location.reload();
            } else if(response.data == false){
            	$messageDialog.showMessage('Unable to change metadata.', {title:'Error'})
            } else {
            	$messageDialog.showMessage(response.error, {title:'Error'})
            }
        }
    };
	
	$editMetadataForm.submit(function(){
    	$(this).ajaxSubmit(options);
    	return false;
    });
	
	$('#editMetadataDialog').dialog({
    	modal: true,
    	autoOpen: false,
    	width: 730, 
    	height: 600,
    	buttons: {
    		Save: function() {
    			$editMetadataForm.ajaxSubmit(options);
    			$(this).dialog('close');
    		},
    		Cancel: function() {
    			$(this).dialog('close');
    		}
    	}
    });
	
	var changeOptions = {
    		dataType: 'json',
            success: function(response) {
            	$(this).dialog("close");
                if(response.data == true) {
                	document.location.reload();
                } else if(response.data == false){
                	$messageDialog.showMessage('Error changing Dataset.', {title:'Error'});
                } else {
                	$messageDialog.showMessage(response.error, {title:'Error'});
                }
            }
	};
	
	$("#changeProject").button();
	
	$('#projectForm, #ownerForm').submit(function(){
    	$(this).ajaxSubmit(changeOptions);
    	return false;
    });
	
	$('#projectDialogue').dialog({
    	modal: true,
    	autoOpen: false,
    	width: 600, 
    	height: 450,
    	buttons: {
    		Save: function() {
    			$('#projectForm').ajaxSubmit(changeOptions);
    			$(this).dialog('close');
    		},
    		Cancel: function() {
    			$(this).dialog('close');
    		}
    	}
    });
	
	$("#changeOwner").button();
	
	$('#ownerDialogue').dialog({
    	modal: true,
    	autoOpen: false,
    	width: 600, 
    	height: 250,
    	buttons: {
    		Save: function() {
    			$('#ownerForm').ajaxSubmit(changeOptions);
    			$(this).dialog('close');
    		},
    		Cancel: function() {
    			$(this).dialog('close');
    		}
    	}
    });
});

function makePublic(url){
	$.ajax({
		url : 'previewData',
		dataType : 'html',
		data : {url : url},
		success:function(response) {
			$('#preview').html(response);
			$('#dialogue').dialog({
                modal: true,
                autoOpen: true,
                width: 730, 
                height: 600,
                buttons: {
                	"Confirm": function() {
                        $.ajax({
                        	url : 'visibility',
                        	data : {url:url},
                        	dataType:'json',
                        	success:function(response){
                        		document.location.reload();
                        	}
                        }); 
                        $(this).dialog('close');
                	},
                    Cancel: function() {
                        $(this).dialog('close');
                    }
                }
			});
		},
		error:function() {
			
		}
	});
}

function makePrivate(url) {
	$('#preview').html('Are you sure you want to make this record private?');
	
	$('#dialogue').dialog({
        modal: true,
        autoOpen: true,
        width: 400, 
        height: 200,
        buttons: {
            "Yes": function() {
                $.ajax({
                	url : 'visibility',
                	dataType:'json',
                	data : {url:url},
                	success:function(response){
                		document.location.reload();
                	},
                	error:function() {
                		alert('Request failed');
                	}
                }); 
                $(this).dialog('close');
            },
            Cancel: function() {
                $(this).dialog('close');
            }
        }
	});
}

function modifyMetadata(url, schema) {
	$.ajax({
		type: 'GET',
		url: 'editMetadata',
		dataType: 'json',
		data: {url:url, schema:schema},
		success:function(response) {
			if(response.data != null) {
		    	$("#metadata").html(response.data);
		    	$("#schema").val(schema);
		    	$('#editMetadataDialog').dialog('open');
		    } else {
		    	$("#messageDialog").showMessage(response.error);
		    }
		},
		error:function() {
			$("#messageDialog").showMessage("Can't retrive metadata");
		}
	});
}

function createRadioItem(name, value, label, currentValue) {
	var $li = $('<li>');
	var $radio = $('<input>').attr('type','radio').attr('name','code');
	if (value == currentValue) {
		$radio.attr('checked','checked');
	}
	$radio.val(value);
	$li.append($radio);
	$li.append(label);
	return $li;
}

function populateProjectOptions(projects, currentProject) {
	var $theList = $('<ul>');
	$theList.append(createRadioItem('code',0,'None', currentProject));
	for(var i = 0; i < projects.length; i++) {
		$theList.append(createRadioItem('code', projects[i].projectCode, projects[i].title, currentProject));
	}
	$('#projectOptions').empty().append($theList);
}

function changeProject(url, currentProject) {
	$.ajax({
		type: 'GET',
		url: 'getProjects',
		dataType: 'json',
		success:function(response) {
			if(response.data != null) {
				if (response.data.length > 0) {
				   populateProjectOptions(response.data, currentProject);
				   $('#projectDialogue').dialog('open');
				} else {
			    	$("#messageDialog").showMessage('You have no associated projects');
				}
		    } else {
		    	$("#messageDialog").showMessage(response.error);
		    }
		},
		error:function() {
			$("#messageDialog").showMessage("Can't retrive projects for user");
		}
	});
}

function changeOwner(url, currentOwner) {
	$('#ownerDialogue').dialog('open');
}
