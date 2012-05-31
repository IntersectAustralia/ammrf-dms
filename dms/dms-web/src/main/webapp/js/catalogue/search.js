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
	
    var advancedSearh = $("#searchForm").hasClass('advanced');
    
    $("#searchForm").validate({
    	onfocusout: false,
		errorElement: "div",
        wrapper: "div",  // a wrapper around the error message
        errorPlacement: function(error, element) {
        	var $element = $(element);
        	if($element.hasClass("from")) {
        		var toInput = $element.parent().children("input.to:visible");
        		if(toInput.length) {
        			element = toInput;
        		}
        	}
        	
            offset = element.offset();
            error.insertBefore(element)
            error.addClass('message');  // add a class to the wrapper
            error.css('position', 'absolute');
            error.css('left', offset.left + element.outerWidth());
            error.css('top', offset.top);
        }
	});
	
    var $datasetsTable;
    
    $("#searchForm.simple").ajaxForm(
    		{
    			"beforeSubmit" : function() {
    				var fullTextQuery = escapeQuery($.trim($("#fullTextQuery").val()));
    				var $query = $("#query");
    	    		$query.val(fullTextQuery.length > 0 ? "dataset.metadata_t:(" + fullTextQuery + ")" : "");
    	    		
    	    		if(!$datasetsTable) {
	    				initResultsTable();
	    			} else {
	    				$datasetsTable.fnFilter($query.val());
	    			}
    	    		
    	    		storeState();
    	    		
    	    		return false;
    			}
    		}
    );
    
    $("#searchForm.advanced").ajaxForm(
    		{
    			"beforeSubmit" : function(data, $form, options) {
    				removeFormState();
			    	var $query = $("#query");
		    		$(".advancedSearchInputField > input.solrInput:hidden").each(function() {
		    			$(this).val("");
		    		});
		    		
		    		var success = $form.valid() && translateRangeProperties();
		    		if(success) {
		    			var queryString = "";
		    			$(".advancedSearchInputField > input.solrInput").each(function(index) {
		    				if(index > 0) {
		    					queryString += " AND ";
		    				}
		    				
		    				var filedValue = $(this).val().length > 0 ? $(this).val() : "*";
		    				queryString += $(this).attr("name") + ":(" + ($(this).is(":visible") ? escapeQuery(filedValue) : filedValue) + ")";
		    			});
		    			$query.val(queryString);
		    			if(!$datasetsTable) {
		    				initResultsTable();
		    			} else {
		    				$datasetsTable.fnFilter($query.val());
		    			}
		    			$("#searchResults").show();
		    			
		    			storeState();
		    		} else {
		    			$("#searchResults").hide();
		    		}
		    		
		    		return false;
    			}
    		}
    );
    
    
    if(advancedSearh) {
    	restoreAdvancedSearchForm();
    	$.validator.addMethod("scientificNumber", function(value, element) {
    		return this.optional(element) || $.isNumber(value);
    	}, "Please enter a valid number.");
    } else {
    	restoreBasicSearchForm();
    }
    
    function storeState() {
    	var userInputQuery = $('#searchForm :visible').fieldSerialize();
		$.bbq.pushState(userInputQuery);
    }
    
    function restoreBasicSearchForm() {
    	var userInput = $.bbq.getState("fullTextQuery");
    	$("#fullTextQuery").val(userInput);
    	$("#searchForm").submit();
    }
    
    function restoreAdvancedSearchForm() {
    	var userInputQuery = $.bbq.getState();
    	if(!$.isEmptyObject(userInputQuery)) {
	    	var $inputDiv;
	    	$.each(userInputQuery, function(name, value) {
	    		// console.log(name + ":" + value); //enable if you need to debug
	    		
	    		if(name == "iDisplayStart" || name == "iDisplayLength") {
	    			return;
	    		}
	    		
	    		if(name.match(/(From|To)$/) == null) {
	    			var $property = $("#" + name)
	    			$inputDiv = addProperty($property.attr("title"), name, $property.attr("class"));
	    		}
	    		$input = $inputDiv.children('[name="' + name + '"]').val(value).trigger('change');
	    	});
	    	$("#searchForm").submit();
    	}
    }
   
    function removeFormState() {
    	var displayStart = $.bbq.getState("iDisplayStart", true);
    	var displayLength = $.bbq.getState("iDisplayLength", true);
    	$.bbq.removeState();
    	
    	if(displayStart != undefined && displayLength != undefined) {
	    	$.bbq.pushState({"iDisplayStart":displayStart});
	    	$.bbq.pushState({"iDisplayLength":displayLength});
    	}
    }
    
    function initResultsTable() {
	    $datasetsTable = $('#datasets').dataTable( {
			"bSort": false,
			"bFilter": true,
			"oSearch": {"sSearch": $("#query").val()},
			"bJQueryUI": true,
			"bProcessing" : true,
			"sPaginationType": "full_numbers",
			"bServerSide": true,
			"sAjaxSource" : 'search',
			"sDom": '<"H"lr>t<"F"ip>',
			"fnServerData" : function(url, data, callback) {

				var displayStart = $.bbq.getState("iDisplayStart", true);
		    	var displayLength = $.bbq.getState("iDisplayLength", true);
		    	
		    	if($datasetsTable == undefined && displayStart != undefined && displayLength != undefined) {
		    		data = $.grep(data, function(element, index) {
		    			switch (element.name) {
						case "iDisplayStart":
							element.value = displayStart;
							break;
						case "iDisplayLength":
							element.value = displayLength;
							break;
						}
		    			return element;
					});
		    	}
		    	
		    	
			    $.ajax( {
				"dataType" : 'json',
				"type" : "GET",
				"url" : url,
				"data" : data,
				"success" : function(response) {
				    if (response.data != null) {
					    if (response.data.datasets == null || response.data.datasets.length == 0) {
					    	callback({"aaData":[], "iTotalRecords":0, "iTotalDisplayRecords":0});
					    }
						var tableData = new Array(response.data.datasets.length);
						$.each(response.data.datasets, function(index, dataset) {
						    tableData[index] = [dataset.id,
						                        "<a class='viewDetails' href=\"view?url=" + 
						                        escape(dataset.url) + 
						                        "&back=" + (advancedSearh ? "advancedSearch" : "index") + 
						                        "\">" + dataset.url +"</a>",
						                        (dataset.visible ? 'Public' : 'Private'),
						                        dataset.owner
						    ];
						});
						var transformedResponse = {
							"iTotalRecords":response.data.totalRecords,
							"iTotalDisplayRecords":response.data.totalRecords,
							"aaData":tableData
						};
						$.bbq.pushState({"iDisplayStart":response.data.iDisplayStart, "iDisplayLength":response.data.iDisplayLength});
						
						return callback(transformedResponse);
				    } else {
				    	return callback({"aaData":[], "iTotalRecords":0, "iTotalDisplayRecords":0});
				    }
				}
			    });
			},
			"fnDrawCallback": function(){
				//Add state to viewDetails links
				var state = $.bbq.getState();
				$("a.viewDetails").fragment(state);
			}
	    });
    }
    
    
    function validateForm() {
    	return $("#searchForm").validate({
    		errorElement: "div",
	        wrapper: "div",  // a wrapper around the error message
	        errorPlacement: function(error, element) {
	        	var $element = $(element);
	        	if($element.hasClass("from")) {
	        		var toInput = $element.parent().children("input.to:visible");
	        		if(toInput.length) {
	        			element = toInput;
	        		}
	        	}
	        	
	            offset = element.offset();
	            error.insertBefore(element)
	            error.addClass('message');  // add a class to the wrapper
	            error.css('position', 'absolute');
	            error.css('left', offset.left + element.outerWidth());
	            error.css('top', offset.top);
	        }
    	}).form();
    }
    
    function escapeQuery(query) {
    	return query.replace(/\\/g,"\\\\")
    				.replace(/\+/g,"\\+")
    				.replace(/\-/g,"\\-")
    				.replace(/&&/g,"\\&&")
    				.replace(/!/g,"\\!")
    				.replace(/\|\|/g,"\\|\|")
    				.replace(/\(/g,"\\\(")
    				.replace(/\)/g,"\\\)")
    				.replace(/{/g,"\\{")
    				.replace(/}/g,"\\}")
    				.replace(/\[/g,"\\\[")
    				.replace(/\]/g,"\\\]")
    				.replace(/\^/g,"\\\^")
    				//.replace(/\"/g,"\\\"")
    				.replace(/~/g,"\\~")
    				//.replace(/\*/g,"\\\*")
    				//.replace(/\?/g,"\\\?")
    				.replace(/:/g,"\\:");
    }
    
    function translateRangeProperties() {
    	var success = true;
    	var dateProperties = new Array();
    	
    	$(".advancedSearchInputField > select").each(function() {
    		var fromVal = getPropertyValue("from", this, dateProperties);
			var toVal = getPropertyValue("to", this, dateProperties);
			
			var type = $(this).data("type");

			var $solrInput = $(this).parent().children(".solrInput");
			
			if(type != "DATE") {
				switch ($(this).val()) {
				case "gt":
					$solrInput.val("[" + fromVal + " TO *]");
					break;
				case "lt":
					$solrInput.val("[* TO " + toVal + "]");
					break;
				case "range":
					$solrInput.val("[" + fromVal + " TO " + toVal + "]");
					break;
				}
			}
    	});
    	
    	if(dateProperties.length > 0) {
    		$.ajax( {
    			"async" : false,
    			"dataType" : 'json',
    			"type" : "GET",
    			"url" : '../dateformat/convertDates',
    			"data" : dateProperties,
    			"success" : function(response) {
    			    if (response.data != null) {
    				    dateProperties = response.data;
    			    } else {
    			    	$("#messageDialog").showMessage(response.error, {
    					    title : 'Could not process your request.'
    					});
    			    	success = false;
    			    }
    			}
    		    });
    	}
    	
    	$.each(dateProperties, function(name, value) {
    		var $dateInput = $("#" + name);
    		var $solrInput = $dateInput.parent().children(".solrInput");
    		var solrInputVal = $solrInput.val();
    		
    		$solrInput.val(solrInputVal.replace("*", value));
    		
    		if(solrInputVal.length == 0) {
	    		if($dateInput.hasClass("from")) {
	    				$solrInput.val("[" + value + " TO *]");
	    		} else if($dateInput.hasClass("to")) {
	    				$solrInput.val("[* TO " + value + "]");
	    		}
    		}
    	});
    	
    	return success;
    }
    
    function getPropertyValue(name, select, dateProperties) {
    	var type = $(select).data("type");
    	var $property = $(select).parent().children("."+ name);
    	var propertyName = $property.attr("name");
    	var propertyVal = $property.val();
		
		if(propertyVal.length == 0) {
			propertyVal = "*"
		} else if(type == "NUMBER") {
			propertyVal = '"' + propertyVal + '"';
		} else if(type == "DATE") {
			switch (name) {
			case "from":
				propertyVal += " 12:00:00 AM";
				break;
			case "to":
				propertyVal += " 11:59:59 PM"
				break;
			}
			dateProperties.push({name:$property.attr("id"), value:propertyVal});
		}
		
		return propertyVal;
    }
    
    // Bind an event to window.onhashchange that, when the history state changes,
    // restore table pagination
    
    $(window).bind( 'hashchange', function(e) {
    	if($datasetsTable != undefined) {
        	//restoreResultsTablePagination($datasetsTable);
    		var table = $datasetsTable;
    		var displayStart = e.getState("iDisplayStart", true);
        	var displayLength = e.getState("iDisplayLength", true);
        	
        	if(displayStart != undefined && displayLength != undefined) {
    	    	var settings = table.fnSettings();
    	    	if(settings._iDisplayStart != displayStart || settings._iDisplayLength != displayLength) {
	    	    	settings._iDisplayStart = displayStart;
	    	    	settings._iDisplayLength = displayLength;
	    	    	$("select[name=datasets_length]").val(displayLength);
	    	    	table.fnDraw(false);
    	    	}
        	}
        }
    });
    
    // Since the event is only triggered when the hash changes, we need to trigger
    // the event now, to handle the hash the page may have loaded with.
    $(window).trigger( 'hashchange' );

    
});
