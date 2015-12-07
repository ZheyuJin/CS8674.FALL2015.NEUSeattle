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
	<link rel="stylesheet" href="/simple-medicare-request/css/common.css">

<title>This is a Temporary Page for Brian</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">

</head>
<body>
	<h2>Brian's Test Page</h2>
	<h6></h6>
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
				url : "query",
				data : {
					request : input_query
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