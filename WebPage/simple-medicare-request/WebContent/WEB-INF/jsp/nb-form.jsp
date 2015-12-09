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
	<script src="../../js/common.js"></script>
	<link rel="stylesheet" href="../../css/common.css">
<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>

<title>Specialty predictor</title>
<link href="../../favicon.ico" rel="icon"
	type="image/x-icon">

</head>
<body class="container main center">
	<h2>Predict a provider specialty</h2>
	<h6></h6>
	<p>
	Submit a procedure keyword to see the most likely provider specialty.
	</p>
	<p></p>
	<input value="" id="user_input" type="text" name="state_request"
		width="100%" required/> <br>

<!-- 
	<input type="checkbox" name="descr_box" value="Anesthesia for lens surgery" />Anesthesia for lens surgery<br>
	<input type="checkbox" name="descr_box" value="Anesthesia surgery" />Anesthesia surgery<br>
	<input type="checkbox" name="descr_box" value="Surgery, Xray of hip minimum 2 views" />Surgery, Xray of hip minimum 2 views<br>
	<input type="checkbox" name="descr_box" value="Xray of hip" />Xray of hip<br>
 -->
    <br />
	<input id="request_button" type="submit" value="Submit" class="btn btn-success">

	<div id="result_area"></div>


	<br>

	<script>
		var input_query = "";
		
		$(document).on('click', '#request_button', function() {
			clearResultArea();
			input_query = "";
			if($('#user_input').val().length > 0){
				input_query = $('#user_input').val() + "; ";
			}
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
			}).fail(function() {
		        window.location = "../../error.html";
		    });
		}

		function responseHandler(data) {
			$("#result_area").replaceWith(
					'<h3 id="result_area">' + JSON.stringify(data) + '</h3>');
		}

		function formatResponse(data){
			var response = "The most likely provider to match this input is " + JSON.stringify(data);
			
		}
		
		function FormatResults(list) {
			var output = "";
			
			for ( var i in list) {
				output += '<div class="result">' + list[i] + '</div>';
			}
			return output;
		}
		
		function clearResultArea(){
			$("#result_area").replaceWith('<div id="result_area"></div>');
		}
	</script>
</body>
</html>