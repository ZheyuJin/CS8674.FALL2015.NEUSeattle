<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Medicare Data Query Form</title>

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script type="text/javascript"
	src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="/simple-medicare-request/css/common.css">

<script src="/simple-medicare-request/js/original.js"></script>
<script src="/simple-medicare-request/js/common.js"></script>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">

</head>
<body class="container main center">
<!-- 
	<h1>Outlier detection by percentage.</h1>
	<form action="result-jsp" method="get" class="form-inline"> 
		
		<label for="proc_code"> Procedure Code </label> 		
		<input id="proc_code"  name="proc_code" type="text" class="form-control" />
		
		<label for="percentage"> Percentage </label> 		
		<input id="percentage" type="number" min="0" max="100" step="0.01" name="percentage" class="form-control" /> 
		 
		<button type="submit" class="btn btn-default">Submit</button>
			
	</form>
-->
	<div>
		<h2>Explore Providers and Procedures</h2>
		<form role="form">
		<div class="form-group">
		<label for="proc_code"> Procedure Code </label>
		<input id="proc_code" name="proc_code" type="proc_code" class="form-control" placeholder="Enter Procedure Code"></div>
		<div class="form-group">
		<label for="proc_keyword">Procedure Keyword</label>
		<input id="proc_keyword" name="proc_keyword" type="proc_keyword" class="form-control" placeholder="Enter Procedure Keyword">
		</div>
		</form>
		

			<br> <br> 
			<select id="stateSelect" class="selectpicker form-control">
			<option label="Select the state" disabled>Select the state</option>
		</select>

		<form>
			<br> <input type="radio" name="use_case" value="case_1" checked
				id="case1btn" onClick="javascript:caseCheck()" class="case-select">Show most
			expensive provider(s) in a state for a procedure<br> 
			<input
				type="radio" name="use_case" value="case_2" id="case2btn" class="case-select"
				onClick="javascript:caseCheck()">Show busiest provider(s) in
			a state for a procedure<br> <input type="radio" name="use_case"
				value="case_3" id="case3btn" onClick="javascript:caseCheck()">Find
			procedures using a keyword, show average cost in a state<br>
		</form>
			<input id="submitSearch" type="submit" value="Submit" class="btn btn-default" />
			
	</div>
	<br />

	<br />
	<span id="feedback"></span>

	<footer>
		<hr />
		<p>
			<a href="../../index.html">Home</a>
		</p>
	</footer>
</body>
</html>