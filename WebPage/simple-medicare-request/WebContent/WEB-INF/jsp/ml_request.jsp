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

<title>Machine Learning Use Case 1 Example</title>
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
	<h2>This Page Is For Testing ML Case #1</h2>
	<h6>The user input delimits based on semi-colons</h6>
	<input value="" id="user_input" type="text" name="state_request"
		width="100%"> <br>
	<input type="checkbox" name="descr_box" value="This is HCPSI Code #1" />This is HCPSI Code #1<br>
	<input type="checkbox" name="descr_box" value="This is HCPSI Code #2" />This is HCPSI Code #2<br>
	<input type="checkbox" name="descr_box" value="This is HCPSI Code #3" />This is HCPSI Code #3<br>
	<input type="checkbox" name="descr_box" value="This is HCPSI Code #4" />This is HCPSI Code #4<br>

	<input id="request_button" type="submit" value="Search">

	<div id="result_area"></div>


	<br>

	<script>
		var input_query = [];
		var fixed_input = [];

		$(document).on('click', '#request_button', function() {
			//user_input = [];
			gatherAllInputs();
			updateSearchQuery();
			searchRequest();
		})

		function gatherAllInputs() {
			fixed_input = [];
			$("input:checkbox[name=descr_box]:checked").each(function() {
				fixed_input.push($(this).val());
			});
		}

		function updateSearchQuery() {
			if($('#user_input').val().length == 0)
				return;
			input_query = $('#user_input').val().split(";");
			for(var s in input_query)
				fixed_input.push(s);
		}

		function searchRequest() {
			alert(JSON.stringify(fixed_input));
			$.ajax({
				url : "request",
				data : {
					fixed_queries : fixed_input
				}
			}).done(function(data) {
				responseHandler(data);
			});
		}

		function responseHandler(data) {
			alert(JSON.stringify(data));
			$("#result_area").replaceWith(
					'<div id="result_area">' + FormatResults(data) + '</div>');
		}

		function FormatResults(list) {
			var output = "";
			
			for ( var i in list) {
				output += '<div class="result">' + list[i] + '</div>';
			}
			return output;
		}
	</script>
</body>
</html>