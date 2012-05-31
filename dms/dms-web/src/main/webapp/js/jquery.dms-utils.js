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

/*!
 * DMS jQuery utility functions:
   $.formatDate(date,pattern)
	Formats the passed date according to the supplied pattern. The tokens that are substituted in the
	pattern are as follows:
	yyyy: the 4-digit year
	yy: the 2-digit year
	MMMM: the full name of the month
	MMM: the abbreviated name of the month
	MM: the month number as a 0-filled, 2-character field
	M: the month number
	dd: the day of the month as a 0-filled, 2-character field
	d: the day of the month
	EEEE: the full name of the day of the week
	EEE: the abbreviated name of the day of the week
	a: the meridian (AM or PM)
	HH: the 24-hour clock hour in the day as a 2-character, 0-filled field
	H: the 24-hour clock hour in the day
	hh: the 12-hour clock hour in the day as a 2-character, 0-filled field
	h: the 12-hour clock hour in the day
	mm: the minutes in the hour as a 2-character, 0-filled field
	m: the minutes in the hour
	ss: the seconds in the minute as a 2-character, 0-filled field
	s: the seconds in the minute
	S: the milliseconds in the second as a 3-character, 0-filled field
 
	Parameters
	date (Date) The date to be formatted.
	pattern (String) The pattern to format the date into. Any characters not matching pattern
	tokens are copied as-is to the result.

	Returns
	The formatted date.

   $.toFixedWidth(value,length,fill)
	Formats the passed value as a fixed-width field of the specified length. An optional fill character
	can be supplied. If the numeric value exceeds the specified length, its higher order digits will be
	truncated to fit the length.

	Parameters
	value (Number) The value to be formatted.
	length (Number) The length of the resulting field.
	fill (String) The fill character used when front-padding the value. If omitted, 0 is used.

	Returns
	The fixed-width field.
 */
(function($){
	
  $.formatDate = function(date) {
	return $.formatDate(date, "dd/MM/yyyy h:mm:ss a");  
  };
  
  $.formatDate = function(date,pattern) {
    var result = [];
    while (pattern.length>0) {
      $.formatDate.patternParts.lastIndex = 0;
      var matched = $.formatDate.patternParts.exec(pattern);
      if (matched) {
        result.push($.formatDate.patternValue[matched[0]].call(this,date));
        pattern = pattern.slice(matched[0].length);
      }
      else {
        result.push(pattern.charAt(0));
        pattern = pattern.slice(1);
      }
    }
    return result.join('');
  };

  $.formatDate.patternParts =
    /^(yy(yy)?|M(M(M(M)?)?)?|d(d)?|EEE(E)?|a|H(H)?|h(h)?|m(m)?|s(s)?|S)/;

  $.formatDate.monthNames = [
    'January','February','March','April','May','June','July',
    'August','September','October','November','December'
  ];

  $.formatDate.dayNames = [
    'Sunday','Monday','Tuesday','Wednesday','Thursday','Friday',
    'Saturday'
  ];

  $.formatDate.patternValue = {
    yy: function(date) {
      return $.toFixedWidth(date.getFullYear(),2);
    },
    yyyy: function(date) {
      return date.getFullYear().toString();
    },
    MMMM: function(date) {
      return $.formatDate.monthNames[date.getMonth()];
    },
    MMM: function(date) {
      return $.formatDate.monthNames[date.getMonth()].substr(0,3);
    },
    MM: function(date) {
      return $.toFixedWidth(date.getMonth()+1,2);
    },
    M: function(date) {
      return date.getMonth()+1;
    },
    dd: function(date) {
      return $.toFixedWidth(date.getDate(),2);
    },
    d: function(date) {
      return date.getDate();
    },
    EEEE: function(date) {
      return $.formatDate.dayNames[date.getDay()];
    },
    EEE: function(date) {
      return $.formatDate.dayNames[date.getDay()].substr(0,3);
    },
    HH: function(date) {
      return $.toFixedWidth(date.getHours(),2);
    },
    H: function(date) {
      return date.getHours();
    },
    hh: function(date) {
      var hours = date.getHours();
      return $.toFixedWidth(hours>12 ? hours - 12 : hours,2);
    },
    h: function(date) {
      return date.getHours()%12;
    },
    mm: function(date) {
      return $.toFixedWidth(date.getMinutes(),2);
    },
    m: function(date) {
      return date.getMinutes();
    },
    ss: function(date) {
      return $.toFixedWidth(date.getSeconds(),2);
    },
    s: function(date) {
      return date.getSeconds();
    },
    S: function(date) {
      return $.toFixedWidth(date.getMilliseconds(),3);
    },
    a: function(date) {
      return date.getHours() < 12 ? 'AM' : 'PM';
    }
  };
  
  /**
   * Returns bound (beginning or end) date of the month as string dd/mm/yyyy.
   * 
   *  Parameters:
   *  date(Date) - any date of the month we are searching bounds in
   *  bound(String) - which bound date we are searching (first|last) 
   */
  $.getMonthBoundDate = function(date, bound) {
      var m = date.getMonth()+1;
      var d = date.getDate();
      var y = date.getFullYear();
      switch (bound) {
        case "first":
    	    return 1 + "/" + m + "/" + y; // First day in month
        case "last":
            // Find last day in month
            var lastDayRaw = new Date(y,m,0);
            var dLast = lastDayRaw.getDate();
            return dLast + "/" + m + "/" + y; // Last day in month
        default:
            return null;
    }
  };
  
  $.findApplet = function (appletId) {
		var $applet = $('object#' + appletId + ', embed#' + appletId);
		return $applet.size() == 0 ? null : (("isActive" in $applet[0]) ? $applet[0] : $applet[1]);
  };
  
  $.insertApplet = function(appletId, $container, url, code, props, params) {
	  var propsStr = "";
	  if (props != null) {
		  for(key in props) {
			  propsStr += key + '="' + props[key] + '" ';
		  }
	  }
	  
	  var paramsStr = "";
	  var paramsForEmbed = "";
	  if(params != null) {
		for(key in params) {
			paramsStr += '<param name="' + key + '" value="' + params[key] + '">';
			paramsForEmbed += key + '="' + params[key] + '" ';
		}  
	  }
	  
	  $('<object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" id="' + appletId + '"'
				+ propsStr + ' name="myPC" codebase="http://java.sun.com/products/plugin/autodl/jinstall-1_6-windows-i586.cab#Version=1,6,0,0">'
				+'<param name="type" value="application/x-java-applet;version=1.6"><param name="code" value="'+code+'">'
				+'<param name="name" value="myPC"> <param name="archive" value="' + url + '">'
				+'<param name="cache_archive" value="dms-applet.jar">'
				+'<param name="scriptable" value="true">'
				+ paramsStr
				+' <comment> <embed type="application/x-java-applet;version=1.6" name="myPC" code="' + code + '" '
				+' pluginspage="http://www.oracle.com/technetwork/java/javase/downloads/index.html" archive="' + url + '" '
				+'" cache_archive="dms-applet.jar" id="' + appletId + '" ' + propsStr +  paramsForEmbed + ' mayscript="mayscript">'					
				+'<noembed>Your Browser Does Not Have Java 1.6 Support, Which Is Needed To Run This Applet!</noembed></embed>'
				+'</comment></object>').appendTo($container);	  
  };


  $.toFixedWidth = function(value,length,fill) {
    var result = (value || '').toString();
    fill = fill || '0';
    var padding = length - result.length;
    if (padding < 0) {
      result = result.substr(-padding);
    }
    else {
      for (var n = 0; n < padding; n++) result = fill + result;
    }
    return result;
  };
  
  $.fn.createMessageDialog = function(options) {
      var settings = $.extend({
          modal: true,
          autoOpen: false,
          width: 400, 
          height: 250,
          title: '',
          buttons: {
              Ok: function() {
                  $(this).dialog('close');
              }
          }
      }, options || {});
      return $dialog = this.dialog(settings);
  };
  
  $.fn.showMessage = function(message, options) {
      var settings = this.dialog('option');
      return this.dialog('option', $.extend(settings, options || {})).html(message).dialog('open');  
  };
  
  function createAjaxTimer(job, $cell) {
		var getUpdate = function() {
			var numErrors = 0;
			$.ajax({
			    url: '../jobs/jobStatus',
			    dataType: 'json',
			    data: {jobId: job.jobId},
			    success:function(newData) {
			    	if (newData.data != null) {
			    		var newJobData = newData.data;
			    		$.renderProgress(newJobData, $cell);
			    	} else {
			    		$cell.html('Server error');
			    	}
			    },
			    error:function() {
			    	numErrors = numErrors + 1;
			    	if (numErrors > 5) {
			    		$cell.html('Error!');
			    	} else {
			    		$cell.html('Error, retrying...');
			    		setTimeout(getUpdate, 5000);
			    	}
			    }
			});
		};
		setTimeout(getUpdate, 1000);
	}
  
  function calculateTimeRemainingUnits(estimatedTimeRemaining){
	  
	  var hourString;
	  var minuteString;
	  var secondString;
	  var totalTime = '';
	  
	  var remainder = estimatedTimeRemaining%3600;
	  hourString = (estimatedTimeRemaining - remainder)/3600;
	  var r2 = remainder%60;
	  minuteString = (remainder-r2)/60;
	  secondString = r2;
	  if(hourString < 10){
		  hourString = '0'+ hourString;
	  }
	  if(minuteString < 10){
		  minuteString = '0'+ minuteString;
	  }
	  if(secondString < 10){
		  secondString = '0'+ secondString;
	  }
	  
	  //converting variables into strings by adding a character.
	  hourString += 'h: ';
	  minuteString += 'm: ';
	  secondString += 's.';
	  
	  totalTime = hourString.concat(minuteString,secondString);
	  
	  return totalTime;
  }
  
  $.formatBytes = function(bytes) {
	  var result;
	  
	  // we use 1000 instead of 1024 to be consistent with disk utilites
	  var blockSize = 1000; 
	  
	  if(bytes >= (blockSize*blockSize*blockSize)){
		  result = bytes/(blockSize*blockSize*blockSize);
		  result = Math.round(result*100)/100;
		  result += ' GB';
	  }
	  else if(bytes >= (blockSize*blockSize)){
		  result = bytes/(blockSize*blockSize);
		  result = Math.round(result*100)/100;
		  result += ' MB';
	  }
	  else {
		  result = bytes/blockSize;
		  result = Math.round(result*100)/100;
		  result += ' KB';
	  }
	  
	  return result; 
  }
  
  function calculateByteUnits(displayedAverageSpeed){
	  
	  var unit = 0;

	  if(displayedAverageSpeed >= (1024*1024*1024)){
		  unit = displayedAverageSpeed/(1024*1024*1024);
		  unit = Math.round(unit*100)/100;
		  unit += ' GBps';
		  return unit;
	  }
	  else if(displayedAverageSpeed >= (1024*1024)){
		  unit = displayedAverageSpeed/(1024*1024);
		  unit = Math.round(unit*100)/100;
		  unit += ' MBps';
		  return unit;
	  }
	  else if(displayedAverageSpeed >= 1024){
		  unit = displayedAverageSpeed/1024;
		  unit = Math.round(unit*100)/100;
		  unit += ' KBps';
		  return unit;
	  }
	  else{
		  unit = displayedAverageSpeed;
		  unit = Math.round(unit*100)/100;
		  unit += ' Bps';
		  return unit;
	  }
	  return unit;
  }
	
  function getPercentageString(job) {
	    return job.currentNumberOfFiles + " of " + job.totalNumberOfFiles + " [" + job.percentage + "%]";
  }
  
	
  $.renderProgress = function(job, $cell) {
		if (job.status in {'FINISHED':1, 'CANCELLED':1, 'ABORTED':1}) {
			$cell.html(getPercentageString(job) + " " + 
                    (job.status == 'FINISHED' ? 'Finished' : (job.status == 'ABORTED' ? 'Error' : 'Cancelled')));
		} else {
			$cell.html('<div class="pb"><span style="position:absolute; margin-left:10px; margin-top:2px"></span></div>').append('<div class="status"  style="float:left"></div>').append('<button id="jobCancel" style="float:right">Cancel</button>');
			$cell.find('div.pb span').html(getPercentageString(job));
			$cell.find('div.pb').progressbar({value:job.percentage});
			
			var message = '';
			switch (job.status) {
			case 'CREATED':
				message = 'Scoping job'; break;
			case 'MONITORING':
				message = 'Waiting for data'; break;
			case 'SCOPING':
				message = 'Scoping job'; break;
			case 'COPYING':
				var totalTime = calculateTimeRemainingUnits(job.estimatedTimeRemaining);
				var displayedSpeed = calculateByteUnits(job.displayedAverageSpeed);
				message = 'Copying Speed: ' + displayedSpeed + '<br>' + totalTime + ' Remaining.' + '<br>'; break;
			}
			$cell.find('div.status').html(message);
			$cell.find('#jobCancel').click(function() {
				$.ajax({
					url:'../jobs/jobCancel',
				    dataType: 'json',
				    data: {jobId: job.jobId},
				    success:function(newData) {
				    	if (newData.data == null || newData.data != true) {
				    		$cell.find('div.status').html('Couldn\'t cancel job');
				    	}
				    }
				});
        	});
			// creates a timer to refresh this
			createAjaxTimer(job, $cell);
		}
  };
  
  $.isNumber = function(string) {
	  var numberRegex = /^[+-]?\d+(\.\d+)?([eE][+-]?\d+)?$/;
	  return numberRegex.test(string);
  };

})(jQuery);
