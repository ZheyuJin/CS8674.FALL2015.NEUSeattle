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
	<script src="/simple-medicare-request/js/common.js"></script>
	

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
	<input type="checkbox" name="descr_box" value="foo foo bar bar" />This is HCPCS Code #1<br>
	<input type="checkbox" name="descr_box" value="bar buzz bar bar" />This is HCPCS Code #2<br>
	<input type="checkbox" name="descr_box" value="buzz buzz foo bar" />This is HCPCS Code #3<br>
	<input type="checkbox" name="descr_box" value="foo foo bar bar" />This is HCPCS Code #4<br>

	<input id="request_button" type="submit" value="Search">

	<div id="result_area"></div>


	<br>

	<script>
		var input_query = "";

		$(document).ready(function() {

			  $('body').append('<div id="ajaxBusy"><p><img src="http://mentalized.net/activity-indicators/indicators/pacific-assault/loader.gif"></p></div>');

			  $('#ajaxBusy').css({
			    display:"none",
			    margin:"0px",
			    paddingLeft:"0px",
			    paddingRight:"0px",
			    paddingTop:"0px",
			    paddingBottom:"0px",
			    position:"absolute",
			    right:"3px",
			    top:"3px",
			     width:"auto"
			  });
			});

			$(document).ajaxStart(function(){ 
			  $('#ajaxBusy').show(); 
			}).ajaxStop(function(){ 
			  $('#ajaxBusy').hide();
			});
		
		$(document).on('click', '#request_button', function() {
			input_query = $('#user_input').val();
			gatherAllInputs();
			if(input_query.length == 0){
				alert("Please enter input.")
				return;				
			}
			searchRequest();
		})

		function gatherAllInputs() {
			$("input:checkbox[name=descr_box]:checked").each(function() {
				input_query += '; ' + $(this).val();
			});
		}


		function searchRequest() {
			$.ajax({
				url : "request",
				data : {
					queries : input_query
				}
			}).done(function(data) {
				responseHandler(data);
			});
		}

		function responseHandler(data) {
			$("#result_area").replaceWith(
					'<div id="result_area">' + JSON.stringify(data) + '</div>');
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