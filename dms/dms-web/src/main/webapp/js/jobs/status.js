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

// functions/events to handle the status update in index page
$(function(){
	
	function renderRow(jobData, trRow) {
		var $cellId = $('td:eq(0)', trRow);
		var $cellType = $('td:eq(1)', trRow);
		var $cellCreatedTime = $('td:eq(2)', trRow);
		var $cellStartTime = $('td:eq(3)', trRow);
		var $cellFrom = $('td:eq(4)', trRow);
		var $cellTo = $('td:eq(5)', trRow);
		var $cellPS = $('td:eq(6)', trRow);
		
		var job = $.parseJSON(jobData[0]);
		$(trRow).data('job', job);
		var copyImage = '../images/copyImage.png';
		var ingestImage = '../images/ingestImage.png';
		var openDetails = '../images/details_open.png';
		
		$cellId.html(job.jobId);
		
		if(job.type == "COPY"){
			$cellType.prepend('<img src="' + copyImage + '"alt="COPY" title="Copy" />');
		}else if(job.type == "INGEST"){
			$cellType.prepend('<img src="' + ingestImage + '"alt="INGEST" title="Ingest"/>');
		}

		$cellCreatedTime.html(job.createdTime);
		if(job.createdTime > job.copyStartedTime) {
			$cellStartTime.html('N/A');					//for jobs that have failed to copy.
		} else{
			$cellStartTime.html(job.copyStartedTime);
		}
		
		if(job.sourceDirs[1]){
			$cellFrom.html(unescape(job.source) + ' /....');
			$cellFrom.append('<img src="' + openDetails + '" id="openDetails" title="Click to see all files/folders selected during this job" style="float:right; position: center; cursor:pointer;"/>');
		} else {
			$cellFrom.html(unescape(job.source) + unescape(job.sourceDirs[0]));
		}
		$cellTo.html(unescape(job.destination) + unescape(job.destinationDir));
		
		$.renderProgress(job, $cellPS);
		
	}
	
    var $messageDialog = $("#messageDialog").createMessageDialog({title:"Error"});
    
    function fnFormatDetails(nTr){
    	var job = $(nTr).data('job');
    	var i =0;
    	var sOut = '<table cellpadding="5" cellspacing="0" border="0" >';
    	
    	for(i = 0; i < job.sourceDirs.length; i++){
    		sOut += '<tr><td style="text-align: right; min-width: 382px;">'+ (i+1) +':</td><td style="width:100%;">'+job.source + job.sourceDirs[i]+'</td></tr>';
    	}
    	sOut += '</table>';
    	
    	return sOut;
    }
    


    var oTable = $('#jobsDataTable').dataTable( {
	"bSort": false,
	"bFilter": false,
	"bJQueryUI": true,
	"bProcessing" : true,
	"sPaginationType": "full_numbers",
	"bServerSide": true,
	"sAjaxSource" : 'listRecentJobs',
        "aoColumns" : [null, null, null, null, null, null, null],
	"fnRowCallback" : function(trRow, data, index, filterIndex) {
    	renderRow(data, trRow);
    	return trRow;
        },
	"fnServerData" : function(url, data, callback) {
	    $.ajax( {
		"dataType" : 'json',
		"type" : "GET",
		"url" : url,
		"data" : data,
		"success" : function(response) {
		    if (response.data != null) {
		    	if (response.data.jobs == null || response.data.jobs.length == 0) {
		    		return callback({"iTotalRecords":0,"iTotalDisplayRecords":0,"aaData":[]});
		    	}
				var tableData = new Array(response.data.jobs.length);
				$.each(response.data.jobs, function(index, job) {
				    tableData[index] = [JSON.stringify(job), null, null, null, null, null, null];
				});
				var transformedResponse = {
					"iTotalRecords":response.data.totalRecords,
					"iTotalDisplayRecords":response.data.totalRecords,
					"aaData":tableData
				};
				return callback(transformedResponse);
		    } else {
				$("#messageDialog").showMessage(response.error, {
				    title : 'Could not retrive jobs.'
				});
	    		return callback({"iTotalRecords":0,"iTotalDisplayRecords":0,"aaData":[]});
		    }
		}
	    });
	}
    });
    
    $('#jobsDataTable tbody td #openDetails').live( 'click', function () { 
		var nTr = this.parentNode.parentNode;
		if ( this.src.match('details_close') )
		{
			/* This row is already open - close it */
			this.src = "../images/details_open.png";
			oTable.fnClose( nTr );
		}
		else
		{
			/* Open this row */
			this.src = "../images/details_close.png";
			oTable.fnOpen( nTr, fnFormatDetails(nTr), 'source_details' );
		}
	} );
    
});
