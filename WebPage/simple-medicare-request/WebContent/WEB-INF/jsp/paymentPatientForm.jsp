<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
	<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	
		<title>Provider payment gap</title>
		<script src="http://d3js.org/d3.v3.min.js"></script>
		<meta name="viewport" content="width=device-width, initial-scale=1">
		<link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
		<script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
		<link href="../../favicon.ico" rel="icon" type="image/x-icon">
		<script src="../../js/payment.js"></script>
		<script src="http://www.datatables.net/examples/ajax/objects.html"></script>
		<link rel="stylesheet" href="../../css/common.css" >
		<script src="../../js/common.js"></script>
		
	</head>
	<body class="container">
			
		<div class="container">
		<h2 id="hello">Treatment payment gap: Patient payment
			responsibility</h2>
			<div class="form-inline">
				<label forid="numRows">Number of treatments:</label>
				<input value="5" id="numRows" type="number" step="1" min = "0" name="numRows"  class = "form-control">
				<label forid="startIndex">Starting at:</label>
				<input value="0" id="startIndex" type="number" step="1" min = "0" name="startIndex"
					class="box form-control" />
				<br>
			</div>
			<input type="checkbox" name="doHighestToLowest" value="setOrder"
				id="doHighestToLowest" checked />
			Sort ascending (= treatments with highest patient responsibility on top)
			<br>
			<input type="checkbox" name="percent" value="setAsPercent"
				id="percentBox" class="box" />
			Return gap as percentage of the overall charge?
			<br>
            <br>

			<input id="request_button" type="submit" value="Submit" class="btn btn-success">
			<div id="graph_area"></div>
			<div id="result_area"></div>
			<br>
		</div>
		
	</body>
</html>