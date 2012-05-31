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

//
// REQUIRES loadTree from browse.js
//
$(function() {
	
var $messageDialog = $("#messageDialog");	

//copy wizard
createWizard($('#copyWizard'), '#copyDialog', 'copy');
//ingest wizard
createWizard($('#ingestWizard'), '#ingestDialog', 'ingest');

function createWizard($wizardNode, dialogSelector, action) {
    var $dialog = $(dialogSelector).dialog( {
	modal : true,
	autoOpen : false,
	width : action== 'ingest' ? 820 :700,
	height : 500,
	title : action == 'ingest' ? 'Ingest Files' : 'Copy Files',
	buttons : {}
    });

    $dialog.find('#copyFrom_cancel').click(function(e) {
	$dialog.dialog('close');
	return false;
    });
	    
    var $copyWizard = $wizardNode.wizard(
	    {
		autoNextDisabled : true,
		historyEnabled : false,
		validationEnabled : false,
		next : '.wizard_next',
		back : '.wizard_back',
		textNext : 'Next',
		textBack : 'Back',
		textSubmit : 'Confirm',
		formPluginEnabled : true,
		formSettings :
		{
		beforeSubmit : function (arr, $form, option) {
			var protoFrom = $form.find('#treeFrom').data('connectionData')['protocol'];
			var protoTo = $form.find('#treeTo').data('connectionData')['protocol'];
			var projectCode = $wizardNode.find("#projectCode").val();
			if (projectCode != null && projectCode != 'null') {
				arr.push({name:'projectCode', value:projectCode});
			}
			if (protoFrom == 'hdd' || protoTo == 'hdd') {
				// pop up window				
				var appletWindow = null; // initialised before use to validate !appletWindow check.
				var vars = {source_item:[]};
				for(var i = 0; i < arr.length; i++) {
					if (arr[i].name == 'source_item') {
						vars.source_item[vars.source_item.length] = arr[i].value;
					} else {
						vars[arr[i].name] = arr[i].value;
					}
				}
				var data = $.param(vars, true);
		    	appletWindow = window.open('../transfer/index?' + data, '_blank', 'toolbar=0,location=0,menubar=0,status=0,width=450,height=250');
		    		
		    	/*IF THE WINDOW DIDN'T OPEN (APPLETWINDOW == null) THEN ALERT USER THAT THEIR POPUP BLOCKER HAS STOPPED THE SCRIPT RUNNING*/
			    if(appletWindow == null) { //  && appletWindow.open == null
			    	$messageDialog.showMessage("Unable to copy to your PC, This may be due to your browser's popup blocker. Please allow popups and try again.",{title:'Failure To submit Job'});
			    }
			    // we stop the request
			    $dialog.dialog('close');
			    return false;
			}
		},
		method : 'POST',
		dataType : 'json',
		success : function(response)
		{
		if (response.data == null)
		{
		    $messageDialog.showMessage(response.error);
		}
		else
		{
		    $dialog.dialog('close');
		    		    
	    	var jobId = response.data;

	    	$messageDialog.showMessage("<div id=\"jobId" + jobId +"\"><!-- ffx3 --></div>" + "<br>" + "<br>" + "<br>" + "You may close this Message even if the job is not complete and your job will continue in the background. To track it's progress go to the jobs tab.  "+ "<b>"+"Job ID: "+response.data +"</b>",{title:'Job submitted'});
	    	
	    	var $cell = $('#jobId' + jobId);
	    	var fakeJob = {status:'CREATED', jobId:jobId, percentage:0, currentNumberOfFiles:0, totalNumberOfFiles: 0};
	    	$.renderProgress(fakeJob, $cell);		    

		}
		}
		},
		
	
		animated : "show",
		show : function(event, ui)
		{
		    // from documentation
		    // ui.step : jQuery object, the element being displayed.
		    // ui.stepInx : int, 0-based index of the DIV.
		    // ui.backwards : boolean - true if reached by clicking "back",
		    // false otherwise.
		    
		    $(dialogSelector + ' .wizard-navigation-step .dot_active').removeClass('dot_active').addClass('dot_inactive');
		    var $navSteps = $(dialogSelector + ' .wizard-navigation-step .dot_inactive');
		    var maxSteps = $navSteps.size();
		    $navSteps.eq(ui.stepInx).removeClass('dot_inactive').addClass('dot_active');
		    
		    $(dialogSelector + ' .wizard-navigation-step .stage_active').removeClass('stage_active').addClass('stage_inactive');
		    $(dialogSelector + ' .wizard-navigation-step .stage_inactive').eq(ui.stepInx).removeClass('stage_inactive').addClass('stage_active');
		    
		    $wizardNode.wizard('disableNext');
		    $wizardNode.wizard(ui.stepInx > 0 ? 'enableBack' : 'disableBack');
		    
		    var $copyFrom = $wizardNode.find('#copyFrom');
		    var $copyTo = $wizardNode.find('#copyTo');
		    
		    function checkTree($tree, isFrom) {
				var num = $tree.jstree('get_selected').size();
				var enableIt = false;
				if (num > 0) {
				    if (action == 'copy' && isFrom) {
				    	enableIt = true;
				    } else {
						var fileType = $tree.jstree('get_selected').eq(0).data().jstree.fileType;
						enableIt = (fileType == 'DIRECTORY');
				    }
				}
				if (enableIt) {
				    $wizardNode.wizard('enableNext');
				} else {
				    $wizardNode.wizard('disableNext');
				}
		    }
		    
		    function populateTree(wizardTreeId, $copyPopup, buttonText, isFrom)
		    {
			var $treeNode = $wizardNode.find(wizardTreeId);
			var data = $('#' + $copyPopup.val()).data('connectionData');
			data.multiSelect = isFrom && action == 'copy' ? true : false;
			$treeNode.data('connectionData', data);
			// TODO !!! Inject custom Applet XHR factory !!!
			$.dms_location_loadTree($treeNode);
			$wizardNode.find('#copyFrom_next').html(buttonText);
			$treeNode.bind("select_node.jstree deselect_node.jstree", function(e,data) {
			    checkTree($treeNode, isFrom);
			});
			return $treeNode;
		    }
		    
		    switch(ui.stepInx) {
		    case 0:
			if (!ui.isBackNavigation && $wizardNode.data('originalTab') != undefined)
			{
			    var fromTreeId = $('#' + $wizardNode.data('originalTab')).find('._TREE_NODE_').data('connectionData').treeId;
			    $copyFrom.html('');
			    $copyTo.html('');
			    $('#tabs').tabs('widget').find('._TREE_NODE_').each(function(i, item)
				    {
        				var treeData = $(item).data('connectionData');
        				if(action == 'copy' || (action == 'ingest' && treeData.serverType == 'INSTRUMENT')) {
        				    $('<option/>').attr('value', treeData.treeId).attr('selected', treeData.treeId == fromTreeId).html(treeData.tabLabel).appendTo($copyFrom);
        				}
        				if((action == 'copy' && treeData.serverType != 'INSTRUMENT') || (action == 'ingest' && treeData.serverType == 'REPOSITORY')) {
        				    $('<option/>').attr('value', treeData.treeId).html(treeData.tabLabel).appendTo($copyTo);
        				}
				    });
			    $('#copyFrom_next').html('Next');
			    $wizardNode.wizard('enableNext');
			}
			break;
		    case 1:
		    	var dataFrom = $('#' + $copyFrom.val()).data('connectionData');
		    	var dataTo = $('#' + $copyTo.val()).data('connectionData');
		    	
		    	// disable wizard while waiting for the Ajax response
		    	$wizardNode.wizard('disableNext');
		    	$wizardNode.wizard('disableBack');
		    	
		    	$.ajax({
					type: 'GET',
					url: 'checkCommonWorker',
					dataType: 'json',
					data: {connectionIdFrom: dataFrom.connectionId, connectionIdTo: dataTo.connectionId},		    		
		    		success: function(response) {
		    			if (response.data != null && response.data) {
		    				$wizardNode.wizard('enableNext');
		    				$wizardNode.wizard('enableBack');
				    		checkTree(populateTree('#treeFrom', $copyFrom, action == 'ingest' ? 'Select Directory': 'Select File(s)', true));		    				
		    			} else {
		    				$messageDialog.showMessage("There is no worker that has the appropriate access to be able to complete your chosen copy/ingest, this is due to server type(s) you chose. Please try another server type.");
		    				$wizardNode.wizard('enableBack');
		    			}
		    		},
		    	    error : function() {
		    	    	$messageDialog.showMessage("There has been an error while attempting to process your copy/ingest job. Please try again.");
		    	    	$wizardNode.wizard('enableBack');
		    	    }
		    	});
			break;
		    case 2:
		    checkTree(populateTree('#treeTo', $copyTo, 'Select Directory', false));
		    
		    var dataFrom = $('#' + $copyFrom.val()).data('connectionData');
		    if(action == 'ingest' && dataFrom.instrumentProfile == 'OLYMPUS_CELL_R') {
		    	$wizardNode.find("#copyToWorkstationDiv").show();
		    	$wizardNode.find("#copyToWorkstation").removeClass('disabled').attr('checked', false);
		    } else {
		    	$wizardNode.find("#copyToWorkstationDiv").hide();
		    	$wizardNode.find("#copyToWorkstation").attr('checked', false).addClass('disabled');
		    }
		    
			break;
		    case 3:
			// attach the selected paths, connectionId to the form via hidden input fields
			// prefixed by fieldName
			function transferInfo($treeNode, $labelNode, $confirmUL, fieldName)
			{
			    $labelNode.html($treeNode.data('connectionData').tabLabel);
			    $confirmUL.empty();
			    $wizardNode.find("input[name='" + fieldName + "_item']").remove();
			    $treeNode.jstree('get_selected').each(function(i, item)
				    {
				var thePath = $(item).data().jstree.absolutePath;
				$('<li/>').html(thePath).appendTo($confirmUL);
				$('<input/>')
				.attr('type','hidden')
				.attr('name',fieldName + '_item')
				.val(thePath)
				.appendTo($wizardNode);
				    });
			    var data = $treeNode.data('connectionData');
			    var fields = ['connectionId'];
			    for(var index in fields) {
				var field = fields[index];
				$wizardNode.find("input[name='" + fieldName + "_" + field + "']").remove();
				$('<input/>')
				.attr('type','hidden')
				.attr('name',fieldName + '_' + field)
				.val(data[field])
				.appendTo($wizardNode);
			    }
			}
			
			transferInfo($wizardNode.find('#treeFrom'), $wizardNode.find('#fromServerLabel'), $wizardNode.find('#confirmFrom'),'source');
			transferInfo($wizardNode.find('#treeTo'), $wizardNode.find('#toServerLabel'), $wizardNode.find('#confirmTo'),'destination');
			
			if(action == 'ingest') {
			    //select project & booking in ingest wizard
			    function retrive(type) {
				var requestData = null;
				if(type == 'bookings') {
				    requestData = {
					    'source_connectionId': $wizardNode.find("input[name='source_connectionId']").val(),
					    'fromDate': $wizardNode.find("#fromDate").val(),
					    'toDate': $wizardNode.find("#toDate").val()
					    };
				}
				$.ajax({
					type: 'GET',
					url: type,
					dataType: 'json',
					data: requestData,
					success:function(response) {
					    if(response.data != null) {
						var $select = $wizardNode.find(type == 'projects' ? '#projectCode' : '#bookingId');
						$select.html('');
						$.each(response.data, function(i, item) {
						    var $option = $('<option/>');
						    if(type == 'bookings') {
							$option.attr('value', item.bookingId).html(item.bookingId);
						    } else {
							$option.attr('value', item.projectCode).html(item.title);
						    }
						    $option.appendTo($select);
						});
					    } else {
						$messageDialog.showMessage(response.error);
					    }
					},
					error:function() {
					    $messageDialog.showMessage("Can't retrive " + type);
					}
				    });
			    }
			    //init fromDate and toDate fields
			    var today = new Date();
			    $wizardNode.find('#fromDate, #toDate').datepicker({ 
										changeMonth: true,
										changeYear: true
			    }).change(function(){
				var fromDate = $wizardNode.find('#fromDate').datepicker('getDate');
				var toDate = $wizardNode.find('#toDate').datepicker('getDate');
				if(fromDate != null && toDate != null && fromDate <= toDate) {
				    retrive('bookings');
				}
			    });
			    $wizardNode.find('#fromDate').datepicker('setDate', $.getMonthBoundDate(today, "first"));
			    $wizardNode.find('#toDate').datepicker('setDate', $.getMonthBoundDate(today, "last"));
			    
			    retrive('projects');
			    retrive('bookings');
			}
			$wizardNode.wizard('enableNext');
			break;
		    case 4:
			//prefill metadata
			var requestData = {
				    'source_item': $wizardNode.find("input[name='source_item']").val(),
				    'destination_connectionId': $wizardNode.find("input[name='destination_connectionId']").val(),
				    'destination_item': $wizardNode.find("input[name='destination_item']").val()
			};
			
			var projectCode = $wizardNode.find("#projectCode").val();
			if(projectCode != null) {
			    requestData['projectCode'] = projectCode;
			}
			
			var bookingId = $wizardNode.find("#bookingId").val();
			if(bookingId != null) {
			    requestData['bookingId'] = bookingId;
			}
			
			$.ajax({
				type: 'GET',
				url: 'metadata',
				dataType: 'json',
				data: requestData,
				success:function(response) {
				    if(response.data != null) {
					$wizardNode.find('#prefillMetadata').html(response.data);
				    } else {
					$messageDialog.showMessage(response.error);
				    }
				},
				error:function() {
				    $messageDialog.showMessage("Can't retrive metadata from booking system");
				}
			    });
			$wizardNode.wizard('enableNext');
			break;
		    case 5:
			//confirm metadata
		    var $copyToWorkstation = $wizardNode.find("input[name='copyToWorkstation']");
		    
		    if($copyToWorkstation.hasClass('disabled')) {
		    	$wizardNode.find("#confirmCopyToWorkstationDiv").hide();
		    } else {
		    	$confrimCopyToWorkstation = $wizardNode.find("#confrimCopyToWorkstation").html($copyToWorkstation.is(':checked') ? "Yes" : "No");
		    	$wizardNode.find("#confirmCopyToWorkstationDiv").show();
		    }
		    
			var requestData = {
				    'source_item': $wizardNode.find("input[name='source_item']").val(),
				    'destination_connectionId': $wizardNode.find("input[name='destination_connectionId']").val(),
				    'destination_item': $wizardNode.find("input[name='destination_item']").val(),
				    'copyToWorkstation': $copyToWorkstation.is(':checked')
			};
			$wizardNode.find("#prefillMetadata :input").each(function(index){
			    requestData[$(this).attr('name')] = $(this).val();
			});
			$.ajax({
				type: 'POST',
				url: 'metadata',
				dataType: 'json',
				data: requestData,
				success:function(response) {
				    if(response.data != null) {
					$wizardNode.find('#confirmMetadata').html(response.data);
				    } else {
					$messageDialog.showMessage(response.error);
				    }
				},
				error:function() {
				    $messageDialog.showMessage("Can't retrive metadata from booking system");
				}
			    });
			$wizardNode.wizard('enableNext');
		    }
		}
	    });
}
});

