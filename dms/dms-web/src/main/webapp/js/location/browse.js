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

(function($){
    // creates tree
	
	function installApplet(appletId){
		var $appletContainer = $('#appletContainer');
		var url = $appletContainer.data('jarDir') + '/dms-applet.jar';
		$.insertApplet(appletId, $appletContainer, url, 'au.org.intersect.dms.applet.BrowseApplet.class', {width:16,height:12}, null);
		return $.findApplet(appletId);
	}
	

	/**
 	 * function makeXhrForApplet(appletId) returns a XHR object with the following implementation
 	 * 
 	 * xhr.open(type, s.url, s.async) : stores the URL as the 'method' to call on the applet (see send below)
 	 * xhr.abort() : just call the onreadystatechange as it should, send is 'almost' synchronous
 	 * xhr.onreadystatechange : initially jQuery.noop
 	 * xhr.readyState : just the readyState property as per specs. 
 	 * xhr.getResponseHeader(header) : just returns content-type json; no other headers are reported
 	 * xhr.responseText : the json text from the applet
 	 * xhr.responseXML : nothing
 	 * xhr.status : 200 after the applet returns
 	 * xhr.send : calls the applet send(url, data), where url is set in the open; url can be used to determine which functionality is being called.
 	 * 
 	 * Specs for readyState (from wikipedia):
 	 * The onreadystatechange event listener
 	 * If the open method of the XMLHttpRequest object was invoked with the third parameter set to true for an asynchronous request, the onreadystatechange event listener will be automatically invoked for each of the following actions that change the readyState property of the XMLHttpRequest object.
 	 *
 	 * - After the open method has been invoked successfully, the readyState property of the XMLHttpRequest object should be assigned a value of 1.
 	 * - After the send method has been invoked and the HTTP response headers have been received, the readyState property of the XMLHttpRequest object should be assigned a value of 2.
 	 * - Once the HTTP response content begins to load, the readyState property of the XMLHttpRequest object should be assigned a value of 3.
 	 * - Once the HTTP response content has finished loading, the readyState property of the XMLHttpRequest object should be assigned a value of 4.
 	 * 
 	 * The major user agents are inconsistent with the handling of the onreadystatechange event listener.
 	 */

 	function makeXhrForApplet(applet) {
 		    // we may allow the applet to return a different content-type in the future, for the time being, assumes this
 		    var headers = {'content-type' : 'application/json;charset=UTF-8'};
 		    var xhr = {readyState: 0};
 		    var appletParams = {};
 		    var open = function(type, url, async) {
 		    	    appletParams.method = url;
 					xhr.readyState = 1;
 				};
 		    var abort = function() {
 		    	xhr.readyState = 4;
 		    	xhr.onreadystatechange("abort");
 		    };
 		    var getResponseHeader = function(header) {
 					return headers[header];
 			};
 			var send = function(data) {
 				var times = 0;
 				var tryOnce = function(){
 		 		    if (times > 5) {
 		 		    	xhr.readyState = 4;
 		 		    	xhr.status = 500;
 		 		    	xhr.responseText = '{"error":"applet not loaded or not working, try again."}';
 	 					xhr.onreadystatechange("timeout");
 	 					return;
 		 		    }
 		 		    if (applet != null && applet.isActive())
 		 		    {
 					    xhr.responseText = applet.send(appletParams.method, data);
 					    xhr.status = 200;
 					    xhr.readyState = 4;
 	 					xhr.onreadystatechange(4);
 		 		    }
 		 		    else
 		 		    {
 		 		    	times++;
 		 		    	setTimeout(tryOnce, 1000);
 		 		    	return; 		 		    	
 		 		    }
 				};
 				setTimeout(tryOnce, 10);
 			};
 	 		xhr.open = open;
 	 		xhr.setRequestHeader = function(){};
 	 		xhr.abort = abort;
 	 		xhr.getResponseHeader = getResponseHeader;
 	 		xhr.onreadystatechange = function(){};
 	 		xhr.send = send;
 	 		return xhr;
 	}
 	
 	
	
 	
    $.dms_location_loadTree = function($treeNode) {
        var connectionId = $treeNode.data('connectionData').connectionId;
        var multiSelect = $treeNode.data('connectionData').multiSelect;
        var protocol = $treeNode.data('connectionData').protocol;
        var useApplet = protocol == 'hdd';
 		var xhrFactory = function() {return jQuery.noop;};
        if (useApplet) {
        	var applet = $.findApplet('dms-applet');
        	if (applet == null) {
        		applet = installApplet('dms-applet');
        	}
 			xhrFactory = function() {return makeXhrForApplet(applet);};
        } else {
 			xhrFactory = $.ajaxSettings.xhr;
        }
        var treeNodeId = $treeNode.attr('id');
        var rootNodeId = treeNodeId + "-n_";
        var saveOpenedCookieName = "jstree_open_" + treeNodeId;
        // delete tree state cookie
        $.cookie(saveOpenedCookieName, null);
        
        $treeNode.jstree({
            "core" : { 
                "initially_open" : [ rootNodeId ] 
            },
            "json_data" : {
                data: {
                    "state": "closed",
                    "data": {"icon":"directory", "title":"/"},
                    "metadata":{"name":"/", "absolutePath":"/", "fileType":"DIRECTORY", "size":"4096", "creationDate":""},
                    "attr":{"id":rootNodeId}
                },
                ajax : {
                    url : "list",
                    xhr : xhrFactory,
                    data: function(node) {
                        return {
                            "connectionId": connectionId,
                            "path": node == -1 ? "/" : $(node).data().jstree.absolutePath
                        };
                    },
                    success: function(response) {
                        var treeId = $treeNode.attr('id');
                        if(response.data != null) { // TODO: put proper check for errors when server code implements it
                        	response.data.sort(function(obj1,obj2){
                        		var name1 = obj1.metadata.name.toUpperCase(); 
                        		var name2 = obj2.metadata.name.toUpperCase();
                        		
                        		if (obj1.metadata.fileType=="FILE" && obj2.metadata.fileType=="DIRECTORY")  // .metadata.fileType = DIRECTORY | FILE
                        			return 1;
                        		
                        		if (obj1.metadata.fileType=="DIRECTORY" && obj2.metadata.fileType=="FILE")
                        			return -1;
                        		
                        		if (name1.substring(0,1)==".") {
                            		if (name2.substring(0,1)==".") {
                                		if (name1 > name2)
                                			return 1;
                                		if (name1 < name2)
                                			return -1;
                                		return 0;                            			
                            		}
                            		return 1;                        			
                        		}
                        		if (name2.substring(0,1)==".")
                        			return -1;
                        		if (name1 > name2)
                        			return 1;
                        		if (name1 < name2)
                        			return -1;
                        		return 0;
                         	});
                            $.each(response.data, function(index, node){
                                node.attr.id = treeId + "-" + node.attr.id
                                });
                            return response.data;
                        } else {
                            $("#messageDialog").showMessage(response.error, {title:'Connection Failed'});
                        }
                    },
                    error: function(request, textStatus, errorThrown) {
                	$.dms_defaultAjaxErrorHandler(request, textStatus, errorThrown);
                    }
                }
            },
            "ui": {
                "select_limit" : (multiSelect ? -1 : 1),
                "disable_selecting_children" : true
            },
            "themes": {
                "theme": "classic"
            },
            "cookies": {
                "save_selected": false,
                "save_opened": saveOpenedCookieName 
            },
            "plugins" : [ "json_data", "ui", "themes", "cookies" ]
        });

    };
    
    // hides metadata div content and shows initial welcome message instead.
    $.dms_location_hideMetadata = function(metadataDivId) {
        var $metadataDiv = $('#' + metadataDivId);
        $metadataDiv.find("#metaInfo").hide();
        $metadataDiv.find("#explanation").show();
    };
})(jQuery);

$(function() {
    
    var $messageDialog = $("#messageDialog");

	// create tabs
	var tab_counter = 2;

    function closeConnection(connId) {
    	$.ajax({
            type: 'POST',
	    url: 'closeConnection',
	    dataType: 'json',
	    data: {connectionId: connId},
	    success:function() {
	    },
	    error:function() {
		$messageDialog.showMessage("Your user session has expired. You may have to log in again to continue.");
	    }
	});
    }

   	var $tabs = $('#tabs').tabs({
		tabTemplate: '<li><a href="#{href}">#{label}</a> <span id="close-#{href}"class="ui-icon ui-icon-close" title="Disconnect">Disconnect</span></li>',

		add: function(event, ui) {
			$tabs.tabs('select', '#' + ui.panel.id);
		},
		
		show: function(event, ui) {
		    var $tabContent = $(ui.panel).children('div').first().show();
		    var $splitContainer = $tabContent.data('splitContainer');
		    if($splitContainer != null) {
			$splitContainer.resizeAll();
		    }
		}
	});

 	$('#tabs span.ui-icon-close').live('click', function() {
		var index = $('li',$tabs).index($(this).parent());
		if(confirm("Do you really want to disconnect from this server?")) {
                        // beware this long query works because of the way we name things in createTree.
			var connId = $('#tree-' + $('#tabs .ui-tabs-nav li:eq('+index+') span')[0].id.substr(7))
					.data('connectionData').connectionId;
			if(connId == -1){
	    		$.cookie("hddCookie", null);
	    	}
			else if (connId > 0) {
				closeConnection(connId);
			}
			$tabs.tabs('remove', index);
		}
	});
 	
 	$('#connectionForm').submit(function(){
 		var $stockServer = $('#stockServer');
 		var serverDesc = $stockServer.data('data')[$stockServer[0].selectedIndex];
 		var xhrFactory = function() {return jQuery.noop;};
 		if (serverDesc.protocol == 'hdd')
 		{
 			var data = {protocol:serverDesc.protocol,connectionId:-1, server:null, description:serverDesc.description, type:'OTHER'};
 		    $.cookie("hddCookie", JSON.stringify(data));
 			createTree(data);
 		}
 		else
 		{
 	 		callAjaxToConnect(this, $.extend({}, serverDesc));
 		}
 		return false; // prevent normal submit 
 	});

 	// data : {connectionId, protocol, server, description, type (from stock_servers)}
 	function createTree(data) {
			 var tabId = 'tabs-' + tab_counter;

             var $content = $("#tabContent").clone().removeAttr('id');
             var $treeNode = $content.find("#tree");
             var $metadataNode = $content.find("#metadata");

             var treeId = "tree-" + tabId;
             $treeNode.attr('id', treeId);
             $treeNode.addClass('_TREE_NODE_'); // this allow us to quickly find all connection trees
             var label = 'New tab';
             if (data.protocol == 'hdd') {
            	label = 'My PC';
             } else {
            	 label = data.description;
             }
             
             switch (data.type)
             {
             case "INSTRUMENT":
            	 $content.find('#actions #create, #actions #delete, #actions #rename').css({'opacity': 0.4, 'cursor': 'default', 'background-image': "url('../images/Inactive_actionicons_v16_3.png')"});
               break;
             case "REPOSITORY":
               break;
             case "OTHER":

            	 if(data.protocol == "hdd"){
                	 $content.find('#actions #ingest, #actions #create, #actions #delete, #actions #rename').css({'opacity': 0.4, 'cursor': 'default', 'background-image': "url('../images/Inactive_actionicons_v16_3.png')"});

            	 } else{
                	 $content.find('#actions #ingest').css({'opacity': 0.4, 'cursor': 'default', 'background-image': "url('../images/Inactive_actionicons_v16_3.png')"});
            	 }
               break;
             default:
               /*Dont "grey-out" any buttons*/
             }

             
             $treeNode.data('connectionData', {
                 connectionId:data.connectionId,
                 tabLabel:label,
                 treeId:treeId,
                 multiSelect:true,
                 protocol:data.protocol,
                 serverType:data.type,
                 instrumentProfile:data.instrumentProfile
             });

             $metadataNode.attr('id', "metadata-" + tabId);
             $treeNode.data('metadataId', $metadataNode.attr('id'));
			 $tabs.tabs('add', '#' + tabId, label);
			 $content.appendTo('#' + tabId).show();
			 tab_counter++;
                           
             $.dms_location_loadTree($treeNode);
             bindMetadata($treeNode, $metadataNode);
             var splitContainer = $content.layout({applyDefaultStyles: true, east__minSize:"550"});
             $content.data('splitContainer', splitContainer);
             initActions($treeNode, tabId, data); 
 	}

        function reloadConnections() {
        	var hddCookie = $.cookie('hddCookie');
        	if (hddCookie != null && hddCookie != " ") {
        		createTree(JSON.parse(hddCookie));
        	}
             $.ajax({
                type: 'POST',
	        url: 'getConnections',
	        dataType: 'json',
	        success:function(resp) {
                    if (resp.data != null) {
                    	$.each(resp.data, function(index, connDesc) {
                    		createTree(connDesc);
                        });
                    } else {
		        // alert("Couldn't restore connections");
                    }
	        },
	        error:function() {
		    // alert("Couldn't restore connections");
	        }
             });
        }

        // this will trigger a new thread (ajax) to reload
        reloadConnections();
	
 	function callAjaxToConnect(formObj, popupData) {
 	    var $form = $(formObj);
 	    popupData.server = $form.find('input[name=server]').text();
 	    popupData.username = $form.find('input[name=username]').text();
 	    popupData.description = $form.find('#stockServer option:selected').text();
 	    popupData.type = $form.find('#type').val();
 	    $(formObj).ajaxSubmit({ 
 		dataType: 'json', 
 	        success: function(response) {
 			if(response.data != null) {
 			    popupData.connectionId = response.data;
 			    createTree(popupData);
 			} else {			
 				if(popupData.username != true) {//u/p for connecting to servers.
 					$messageDialog.showMessage("The system was unable to create this connection. This may be due to the entering of an incorrect username/password or simply that the server did not respond. If the problem persists please contact your system administrator.", {title: "Connection Failed"});
 				} else {
 					$messageDialog.showMessage("The system was unable to connect to this server, please close the connection and try again.", {title: "Connection Failed"});
 				}
 			}
 		}/*,
 		error: function(error) {
 		    $messageDialog.showMessage("Internal Server Error.");
 	       }*/ 
 	    });
 	}
 	
 	
    function bindMetadata($treeNode, $metadataNode) {
    	var connectionData = $treeNode.data('connectionData');
        $treeNode.bind("select_node.jstree", function(event, data) {
            var $node = data.inst.get_selected();
            var metadata = $node.data().jstree;
            $metadataNode.find("#explanation").hide();

            $metadataNode.find("#metaName").html(metadata.name);
            $metadataNode.find("#metaType").html(metadata.fileType);

            if(metadata.fileType == 'DIRECTORY') {
                $metadataNode.find("#metaFileNumber").html($node.children('ul').children('li').size()).closest("div").show();
            } else {
                $metadataNode.find("#metaFileNumber").closest("div").hide();
            }

            $metadataNode.find("#metaSize").html($.formatBytes(metadata.size) + " (" + metadata.size + " bytes)");
            if(metadata.modificationDate) {
                $metadataNode.find("#metaModificationDate").html(metadata.modificationDate);
            } else {
                $metadataNode.find("#metaModificationDate").html("");
            }
            
            if(connectionData.serverType == 'REPOSITORY') {
                // load extra metadata for repository
                $.get('../catalogue/metadata', 
            	  {path:metadata.absolutePath, connectionId:connectionData.connectionId},
            	  function(data) {
            		$metadataNode.find("#extraMetadata").html(data);
                	  },
                	'html'
                );
            }
            
            $metadataNode.find("#metaInfo").show();
        });
    }
    
    function countConnections(type) {
    	var counter = 0;
    	$('#tabs').tabs('widget').find('._TREE_NODE_').each(function(i, item)
			    {
    				var treeData = $(item).data('connectionData');
    				if (treeData.serverType == type) {
    					counter++;
    				}
			    });
    	return counter;
    }
    
    function numberOfConnections() {
    	var numConnections = 0;
    	$('#tabs').tabs('widget').find('._TREE_NODE_').each(function(i, item)
			    {
    				numConnections++;
    			
			    });
    	return numConnections;
    }
	
	/* Inits action buttons */
	function initActions($treeNode, tabId, data) {

        	// reload
        	var $actionsContainer = $('#' + tabId).find('#actions');
        	$actionsContainer.find('#reload').click(function() {
        	    $treeNode.jstree('refresh');
        	});
        
        	// create
        	if(data.type == "REPOSITORY" || data.type == "OTHER"){
        		if(data.protocol != "hdd"){
        		$actionsContainer
        		.find('#create')
        		.click(function() {
        			    var $node = $treeNode.jstree('get_selected');
        			    if ($node.size() == 0
        				    || $node.data().jstree.fileType != 'DIRECTORY') {
        				$messageDialog
        					.showMessage('No directory selected. Please select one parent directory.');
        				return false;
        			    }
        			    if ($node.size() > 1) {
        				$messageDialog
        					.showMessage('Please select only one parent directory.');
        				return false;
        			    }
        
        			    $('#createForm').data('tree', $treeNode);
        			    $('#createDialog').dialog('open');
        			});
        		}
        	}
        
        	// rename
			if(data.type == "REPOSITORY" || data.type == "OTHER"){
				if(data.protocol != "hdd"){
				$actionsContainer
        		.find('#rename')
        		.click(function() {
    			    var $node = $treeNode.jstree('get_selected');
    			    if ($node.size() == 0) {
    				$messageDialog
    					.showMessage('No file or directory selected');
    				return false;
    			    }
    			    if ($node.size() > 1) {
    				$messageDialog
    					.showMessage('Cannot rename multiple files. Please select only one file or directory.');
    				return false;
    			    }
    
    			    $('#renameForm').data('tree', $treeNode);
    			    $('#renameDialog').dialog('open');
        			});
				}
			}
        
        	//delete
    		if(data.type == "REPOSITORY" || data.type == "OTHER"){
    			if(data.protocol != "hdd"){
	        	$actionsContainer.find('#delete').click(function() {
	
	        	    var $selectedNodes = $treeNode.jstree('get_selected');
	        	    if ($selectedNodes.size() == 0) {
	        		$messageDialog.showMessage('No files or directories selected');
	        		return false;
	        	    }       
	        	    $('#deleteDialog').data('tree', $treeNode).dialog('open');
	         	});
    			}
    		}
        
        	// copy from
        	$actionsContainer.find('#copyFrom').click(function() {
        		if (countConnections('INSTRUMENT') == numberOfConnections()){ 
        			$messageDialog.showMessage('You do not currently have sufficient connections open to be able to complete a copy.');	        			
        		} else {
        	    bindWizard('#copyDialog', '#copyWizard');
        		}
        	});
        
        	// ingest
        	if(data.type == "REPOSITORY" || data.type == "INSTRUMENT"){
	        	$actionsContainer.find('#ingest').click(function() {  
	        		if (countConnections('INSTRUMENT') == 0 || countConnections('REPOSITORY') == 0) {
	        			$messageDialog.showMessage('You must be connected to both an instrument and a repository to complete an ingestion.');	        			
	        		} else {
	        			bindWizard('#ingestDialog', '#ingestWizard');
	        		}
	        	});
        	}
        	
        	function bindWizard(dialogSelector, wizardSelector) {
        	    var $wizard = $(wizardSelector);
        	    $wizard.find('#treeFrom').data('connectionData', undefined);
        	    $wizard.find('#treeTo').data('connectionData', undefined);
        	    $(dialogSelector).dialog('open');
        	    $wizard.data('originalTab', tabId);
        	    $wizard.wizard('gotoStep', 0);
        	}
	
	}
});
