var start_index = 0;
var page_size = 10;
var end_index = page_size - 1;

var state = "";
var zip_code = "";
var facet_type = "Zip";
var provider_type = "";
var query = "";
$("#result-header").hide();
$("#prev").hide();

$("#next").hide();
$("#breadcrumb").hide();
$("#currentState").hide();
$("#currentZip").hide();
$("#currentType").hide();

getStates();

$(document).on('mouseenter', '.result', function() {
	$(this).addClass('result-hover');
});

$(document).on('mouseleave', '.result', function() {
	$(this).removeClass('result-hover');
});

$(document).on('mouseenter', '.facet', function() {
	$(this).addClass('link');
});

$(document).on('mouseleave', '.facet', function() {
	$(this).removeClass('link');
});

$(document).on('click', '#next', function() {
	start_index = end_index + 1;
	end_index = start_index + page_size - 1;

	searchRequest();
});

$(document).on('click', '#prev', function() {
	if (start_index >= page_size) {
		start_index = start_index - page_size;

	} else {
		start_index = 0;
	}

	end_index = start_index + page_size - 1;

	searchRequest();
});

$(document).on('click', '#currentState', function() {
	var curState = $("#stateVal").text();
	resetVars();
	facet_type = "Zip";
	state = curState;
	searchRequest();
});

$(document).on('click', '#currentZip', function() {
	var curState = $("#stateVal").text();
	var curZip = $("#zipVal").text();
	resetVars();
	facet_type = "ProviderType";
	state = curState;
	zip_code = curZip;
	searchRequest();
});

$(document).on('click', '#currentType', function() {
	var curState = $("#stateVal").text();
	var curZip = $("#zipVal").text();
	var curType = $("#typeVal").text();
	resetVars();
	facet_type = "Query";
	state = curState;
	zip_code = curZip;
	provider_type = curType;

	searchRequest();
});

$('#stateSelect').change(function() {
	var val = $("#stateSelect option:selected").val();
	resetVars();
	state = val.toUpperCase();
	facet_type = "Zip";
	searchRequest();
});

$(document).on('click', '.facet', function() {

	switch (facet_type) {
	case "State":
		facet_type = "Zip";
		state = $(this).text();
		break;
	case "Zip":
		facet_type = "ProviderType";
		zip_code = $(this).text();
		break;
	case "ProviderType":
		facet_type = "";
		provider_type = $(this).text();
		break;
	case "Query":
		facet_type = "";
		query = $(this).text();
		break;
	}

	// Reset our row counters for this new query/filter
	start_index = 0;
	end_index = start_index + page_size - 1;
	searchRequest();
});

function resetVars() {

	start_index = 0;
	end_index = page_size - 1;

	state = "";
	zip_code = "";
	facet_type = "Zip";
	provider_type = "";
	query = "";
	$("#next").hide();
	$("#prev").hide();

	$("#result-header").hide();
	$("#breadcrumb").hide();

	$("#curFacet").text("");

	$("#currentState").hide();
	$("#currentZip").hide();
	$("#currentType").hide();

}

function getStates() {

	$.ajax({
		url : "../provider/count/states",
	}).done(function(data) {
		setDropdown(data.facetedCount);
	}).fail(function() {

		// Fill in some defaults.
		var states = [ "AZ", "CA", "FL", "TX", "GA", "NY" ];
		var dropdown = $("#stateSelect");
		for (var i = 0; i < states.length; i++) {
			dropdown.append(new Option(states[i], states[i]));
		};
	});
}

function setDropdown(facetList) {

	var dropdown = $("#stateSelect");

	for (var i = 0; i < facetList.length; i++) {
		var curState = facetList[i].propertyValue;
		var curCount = facetList[i].propertyCount;
		if (curCount === 0) {
		} else {
			dropdown.append(new Option(curState.toUpperCase() + " ( "
					+ curCount + " )", curState));
		}
	}
}

function searchRequest() {

	$.ajax({
		url : "query",
		data : {
			provider_type : provider_type,
			state : state,
			zip : zip_code,
			query : query,
			facet : facet_type,
			start : start_index,
			end : end_index
		}
	}).done(function(data) {
		responseHandler(data);
	}).fail(function() {
		window.location = "../../error.html";
	});
}

function responseHandler(data) {

	$("#result-header").show();

	if (start_index == 0) {
		$("#prev").hide();
	} else {
		$("#prev").show();
	}
	if (data.numProvidersTotal > end_index + 1) {
		$("#next").show();
	} else {
		$("#next").hide();
	}

	$("#breadcrumb").show();
	if (state != "") {
		$("#stateVal").text(state);
		$("#currentState").show();
	}
	if (zip_code != "") {
		$("#zipVal").text(zip_code);
		$("#currentZip").show();
	}
	if (provider_type != "") {
		$("#typeVal").text(provider_type);
		$("#currentType").show();
	}

	handleResults(data.providers);
	handleFacets(data.facets);
}

function handleResults(list) {

	$('#result-table').html(formatResults(list));
	$('#result-table').show();

}

function formatResults(list) {

	var output = "<thead>"
			+ "<th width='15%'>Name</th><th width='12%'>Specialty</th><th width='8%'>At</th><th width='15%'>Location</th><th width='55%'>Procedure</th>"
			+ "</thead><tbody>";

	for ( var i in list) {
		var officeOrFacility = "Facility";

		if (list[i].place_of_service.indexOf("O") != -1) {
			officeOrFacility = "Office";
		}

		var formattedZip = list[i].zip;
		if (formattedZip.length === 9) {
			formattedZip = formattedZip.substr(0, 5) + "-"
					+ formattedZip.substr(5, 4);
		}

		output += '<tr class="result">' + '<td>'
				+ toNameCase(list[i].first_name) + " "
				+ toNameCase(list[i].last_or_org_name) + '</td>' + '<td>'
				+ list[i].provider_type + '</td>' + '<td>' + officeOrFacility
				+ '</td>' + '<td>' + toNameCase(list[i].city) + ", "
				+ list[i].state + "  " + formattedZip + '</td>' + '<td>'
				+ list[i].hcpcs_description + '</td>' + '</tr>';
	}

	output += "</tbody>";
	return output;
}

function handleFacets(list) {

	if (list.facetType == "ProviderType") {
		$("#curFacet").text("Specialty:");
	} else if (list.facetType == "Zip" || list.facetType == "State") {
		$("#curFacet").text(list.facetType + ":");
	} else {
		// alert("No facet type?");
		$("#curFacet").text(" ");
	}

	$("#facet-area").replaceWith(
			'<div id="facet-area">' + formatFacets(list) + '</div>');
}

function formatFacets(list) {
	var output = "";
	for ( var i in list.facetedCount) {

		if (list.facetedCount[i].propertyCount === 0) {
			alert("Facet has zero facet count?");
		} else {
			output += '<div><span class="facet">'
					+ list.facetedCount[i].propertyValue + '</span><span> ( '
					+ list.facetedCount[i].propertyCount + ' )</span></div>';
		}
	}
	return output;
}
