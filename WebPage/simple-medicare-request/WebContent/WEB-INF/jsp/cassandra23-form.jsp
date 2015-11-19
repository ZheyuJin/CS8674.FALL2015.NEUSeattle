<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script type="text/javascript"
	src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

<title>Use case 2 and 3</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">
<style>
html, body {
	height: 100%;
	margin: 0px;
	padding: 0px;
}

#side-bar {
	height: 100%;
	margin: 0;
	padding: .25em 0em .25em 0em; //
	border: solid 1px #30c9e0;
	background: #fcac4c;
	width: 13.5em;
	float: left;
}

.result-hover {
	background-color: grey;
}

.link {
	color: blue;
	text-decoration: underline;
}


</style>

</head>
<body>
	<h2>This Page Is For Testing Use Cases 2 and 3</h2>	
	Amount ('N'): <input value="" id="Number" type="text" name="amount"
		width="100%"> <br>
	<input type="checkbox" name="doHighestToLowest" value="setOrder" id="doHighestToLowest" checked />Check this box to return highest to lowest<br>
	<input type="checkbox" name="percent" value="setAsPercent" id="percentBox" />Check this box to return values as a percentage<br>
	<input id="request_button" type="submit" value="Search">
	<div id="result_area"></div>	
	<div id="graph_area"></div>
	<br>

	<script>
		var getLargest = true;
		var isPercent = false;
		var amount = 10;
		
		// We might want some validation here (amount > 0 for example)

		$(document).on('click', '#request_button', function() {
			amount = $('#Number').val();
			isPercent = $('#percentBox').is(":checked");
			getLargest = $('#doHighestToLowest').is(":checked");
			searchRequest();
		});

		function searchRequest() {
			//alert("here");
			
			$.ajax({
				url : "request",
				data : {
					amount: amount,
					sortDesc: getLargest,
					isPercentage: isPercent
				}
			}).done(function(data) {
				//alert(JSON.stringify(data));
				responseHandler(data);
			}).fail(function() {
		        window.location = "../../error.html";
		    });
		}

		function responseHandler(data) {
			//alert(JSON.stringify(data));
			$("#result_area").replaceWith(
					'<div id="result_area">' + JSON.stringify(data) + '</div>');
		}
		</script>
</body>
</html>