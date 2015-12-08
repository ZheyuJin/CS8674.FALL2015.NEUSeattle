function getStates() {

		var states = [ "AZ", "CA", "FL", "TX", "GA", "NY" ];
		var dropdown = $("#stateSelect");
		for (var i = 0; i < states.length; i++) {
			dropdown.append(new Option(states[i], states[i]));
		}
}

$(document).ready(function() {
	getStates();
	$('.keyword_class').hide();
	$('.table').hide();
});



$(document).on('click', '#submitSearch', function() {
	$('.table').hide();
	$('.table').DataTable().destroy();
	
	var query;
	if ($('input[name="request-type"]:checked').val() != "avgProcedureCost") {
		if (!document.getElementById('proc_code').value.match(new RegExp(
				/^[A-Za-z0-9]{3,5}$/))) {
			alert("Procedure Code must be filled out properly.");
			return;
		}
		query = $("#proc_code").val();
	} else{
		if ($("#proc_keyword").val().length == 0) {
			alert("Procedure Keyword must be filled out.");
			return;
		}
		query = $("#proc_keyword").val();
	}

	var URL = "query" + upperFirstLetter($('input[name="request-type"]:checked').val());
	$(function() {
		$.get(
				URL,
				{
					query : query,
					state : $("#stateSelect option:selected").val()
				},
				function(data) {
					responseHandler(URL, data);
				}).fail(function() {
			window.location = "../../error.html";
		});
	});
});

function responseHandler(URL, data){
	
	switch(URL){
	case "queryAvgProcedureCost":
		fillAvgCostTable(data);
		$('#avgCostTable').show();
		break;
	case "queryBusiestProvider":
		fillBusiestTable(data);
		$('#busiestTable').show();
		break;
	case "queryMostExpensiveProc":
		fillExpensiveTable(data);
		$('#expensiveTable').show();
		break;
	}	
}


function fillAvgCostTable(data){
	$('#avgCostTable').DataTable( {
	    data: data,
	    columns: [
	        { data: 'procCode' },
	        { data: 'desc' },
	        { data: 'avgCost' },
	        { data: 'state' }
	    ]
	} );
}

function fillBusiestTable(data){
	$('#busiestTable').DataTable( {
	    data: data,
	    columns: [
	        { data: 'first_name' },
	        { data: 'last_or_org_name' },
	        { data: 'beneficiaries_day_service_count' }
	    ]
	} );
}

function fillExpensiveTable(data){
	$('#expensiveTable').DataTable( {
	    data: data,
	    columns: [
	        { data: 'first_name' },
	        { data: 'last_or_org_name' },
	        { data: 'providerDetails.averageSubmittedChargeAmount' }
	    ]
	} );
}

	/*
	 * 	********* CASE 1 - EXPENSIVE *********** 
				+ toNameCase(data[i].last_or_org_name)
				+ toNameCase(data[i].first_name)
				+ data[i].providerDetails.averageSubmittedChargeAmount
						.toFixed(2) + '</td></tr>';

	********* CASE 2 - BUSIEST ***********
	for (var i = 0; i < data.length; i++) {
		text += '<tr><td>' + toNameCase(data[i].last_or_org_name) + '</td><td>'
				+ toNameCase(data[i].first_name) + '</td><td>'
				+ data[i].beneficiaries_day_service_count + '</td></tr>';

		****** CASE 3 - AVERAGE COST ******
		text += '<tr><td>' + data[i].procCode + '</td><td>' + data[i].desc
				+ '</td><td>\$' + data[i].avgCost.toFixed(2) + '</td><td>'
				+ data[i].state + '</td></tr>';

*/
function checkEmpty(data) {
	if (data.length == 0) {
		return true;
	} else {
		return false;
	}
}

$(document).on('click', '.case-select', function(){
//function caseCheck() {
	if ($(this).is('#avgProcedureCost')) {
		$('.keyword_class').show();
		$('#proc_code').attr('disabled',true);
		//document.getElementById('proc_code').disabled = true;
	} else {
		$('.keyword_class').hide();
		$('#proc_code').attr('disabled', false);
		//document.getElementById('proc_code').disabled = false;
	}
});
