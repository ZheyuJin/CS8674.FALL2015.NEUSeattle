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

        <script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>

<title>Simple Visualization</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">

</head>
<body>
			<input id="search_button" type="submit" value="Search">

	<script>
		function searchRequest() {
			$.ajax({
				url : "request",
				data : {
					state : "FL"
					}
			}).done(function(data) {
				alert(JSON.stringify(data));
			});
		};

		$(document).on('click', '#search_button', function() {
			alert('here');
			searchRequest();
		});

	</script>
</body>
</html>