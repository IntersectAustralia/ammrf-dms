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
	$("#filterBuilder").accordion().data("counter", 0);
});

function addProperty(displayName, name, type) {
	var $form = $("#searchForm");
	var $submitButton = $form.children("#proceed");
	var $inputDiv = $('<div class="advancedSearchInputField"></div>');
	
	var counter = $("#filterBuilder").data("counter");
	counter += 1;
	$inputDiv.attr("id", counter);
	$("#filterBuilder").data("counter", counter);
	
	$inputDiv.append('<img src="../images/details_close.png" class="advanced_search_remove_property" alt="Remove" title="Remove ' + displayName +' property"/>');
	var $label = $('<label for="' + name + '">' + displayName + ':</label>').css('width', 'auto');
	$inputDiv.append($label);
	switch (type) {
	case "TEXT":
		$('<input type="text"/>').attr('name', name).attr('id', $inputDiv.attr("id")).addClass("solrInput").appendTo($inputDiv);
		break;
	case "NUMBER":
		var inputFields = createRangeProperty(name, type, $inputDiv);
		$.each(inputFields, function() {
			$(this).addClass("scientificNumber");
		});
		break;
	case "DATE":
		var inputFields = createRangeProperty(name, type, $inputDiv);
		$.each(inputFields, function() {
			$(this).datepicker({changeMonth: true, changeYear: true});
		});
		break;
	}
	
	$inputDiv.children("img.advanced_search_remove_property").click(function() {
		$inputDiv.remove();
		if($form.children("div.advancedSearchInputField").size() == 0) {
			$submitButton.hide();
		}
	});
	$inputDiv.insertBefore($submitButton);
	$submitButton.show();
	
	return $inputDiv;
}

function createRangeProperty(name, type, inputDiv) {
	var uniqueId = $(inputDiv).attr("id");
	var $select = $('<select><option value="gt" selected="selected">greater</option><option value="lt">less</option><option value="range">between</option></select>')
	.attr("name", name).attr('id', uniqueId).data("type", type);
	$select.appendTo(inputDiv);
	
	var inputFields = new Array(2);
	var $from=$('<input type="text"/>');
	var $to=$('<input type="text"/>');
	$from.attr('name', name + "From").attr('id', name + "From-" + uniqueId).addClass("range").addClass("from").appendTo(inputDiv);
	$to.attr('name', name + "To").attr('id', name + "To-" + uniqueId).addClass("range").addClass("to").appendTo(inputDiv).hide();
	inputFields[0] = $from;
	inputFields[1] = $to;
	
	$('<input type="hidden"/>').attr('name', name).attr('id', uniqueId).addClass("solrInput").appendTo(inputDiv);
	$select.change(function() {
		$from = $(this).parent().children(".from").val("");
		$to = $(this).parent().children(".to").val("");
		switch ($(this).val()) {
		case "gt":
			$from.show();
			$to.hide();
			break;
		case "lt":
			$to.show();
			$from.hide();
			break;
		case "range":
			$from.show();
			$to.show();
			break;
		}
	});
	
	return inputFields;
}
