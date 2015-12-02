function getStates() {

		var states = [ "AZ", "CA", "FL", "TX", "GA", "NY" ];
		var dropdown = $("#stateSelect");
		for (var i = 0; i < states.length; i++) {
			dropdown.append(new Option(states[i], states[i]));
		}
}

$(document).ready(function() {
	getStates();
});

$(document).on('click', '#submitSearch', function() {
	if ($('input[name="use_case"]:checked').val() != "case_3") {
		if (!document.getElementById('proc_code').value.match(new RegExp(
				/^[A-Za-z0-9]{3,5}$/))) {
			alert("Procedure Code must be filled out properly.");
			return;
		}
	}

	if ($('input[name="use_case"]:checked').val() == "case_3") {
		if ($("#proc_keyword").val().length == 0) {
			alert("Procedure Keyword must be filled out.");
			return;
		}
	}
	$("#feedback").replaceWith('<span id="feedback">Searching...</span>');

	switch ($('input[name="use_case"]:checked').val()) {
	case ("case_1"):

		$(function() {
			$.get(
					"submit",
					{
						proc_code : $("#proc_code").val(),
						state : $("#state").val(),
						use_case : $('input[name="use_case"]:checked').val()
					},
					function(data) {
						$("#feedback").replaceWith(
								'<span id="feedback">' + displayDataCase1(data)
										+ '</span>');
					}).fail(function() {
				window.location = "../../error.html";
			});
		});
		break;
	case ("case_2"):
		$(function() {
			$.get(
					"submit",
					{
						proc_code : $("#proc_code").val(),
						state : $("#state").val(),
						use_case : $('input[name="use_case"]:checked').val()
					},
					function(data) {
						$("#feedback").replaceWith(
								'<span id="feedback">' + displayDataCase2(data)
										+ '</span>');
					}).fail(function() {
				window.location = "../../error.html";
			});
		});
		break;
	case ("case_3"):
		$(function() {
			$.get(
					"submit",
					{
						keyword : $("#proc_keyword").val(),
						state : $("#state").val(),
						use_case : $('input[name="use_case"]:checked').val()
					},
					function(data) {
						$("#feedback").replaceWith(
								'<span id="feedback">' + displayDataCase3(data)
										+ '</span>');
					}).fail(function() {
				window.location = "../../error.html";
			});
		});
		break;
	}
});

function displayDataCase1(data) {
	if (checkEmpty(data)) {
		return "No providers found for that criteria.";
	}

	var text = '<table style="width:100%"><tr><td>'
			+ '<b>Last Name</b></td><td><b>First Name</b></td>'
			+ '<td><b> Submitted Charge Amount </b></td></tr>';

	for (var i = 0; i < data.length; i++) {
		text += '<tr><td>'
				+ toNameCase(data[i].last_or_org_name)
				+ '</td><td>'
				+ toNameCase(data[i].first_name)
				+ '</td><td>\$'
				+ data[i].providerDetails.averageSubmittedChargeAmount
						.toFixed(2) + '</td></tr>';
	}
	text += '</table>';

	return text;
}

function displayDataCase2(data) {
	if (checkEmpty(data)) {
		return "No providers found for that criteria."
	}
	var text = '<table style="width:100%"><tr><td>'
			+ '<b>Last Name</b></td><td><b>First Name</b></td>'
			+ '<td><b> Day Service Count </b></td></tr>';

	for (var i = 0; i < data.length; i++) {
		text += '<tr><td>' + toNameCase(data[i].last_or_org_name) + '</td><td>'
				+ toNameCase(data[i].first_name) + '</td><td>'
				+ data[i].beneficiaries_day_service_count + '</td></tr>';
	}
	text += '</table>';
	return text;
}

function displayDataCase3(data) {
	if (checkEmpty(data)) {
		return "No procedures found for that keyword.";
	}
	var text = '<table style="width:100%"><tr>'
			+ '<td><b>Procedure Code</b></td>'
			+ '<td><b>Description</b></td><td><b> Average Cost </b></td>'
			+ '<td><b> State </b></td></tr>';

	for (var i = 0; i < data.length; i++) {
		text += '<tr><td>' + data[i].procCode + '</td><td>' + data[i].desc
				+ '</td><td>\$' + data[i].avgCost.toFixed(2) + '</td><td>'
				+ data[i].state + '</td></tr>';
	}
	text += '</table>';
	return text;
}

function checkEmpty(data) {
	if (data.length == 0) {
		return true;
	} else {
		return false;
	}
}

function caseCheck() {
	if (document.getElementById('case3btn').checked) {
		document.getElementById('proc_keyword_box').style.display = 'block';
		document.getElementById('proc_code').disabled = true;
	} else {
		document.getElementById('proc_keyword_box').style.display = 'none';
		document.getElementById('proc_code').disabled = false;
	}
}

function toNameCase(str) {
	//return str.replace(/\w\S*/g, function(txt) {
	//	return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
	//});
	return str;
}