<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Outliers</title>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

<link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="../../css/common.css">
<script src="../../js/common.js"></script>

<link href="../../favicon.ico" rel="icon"
    type="image/x-icon">

</head>

<body class="container main center">
	<h1>Outlier Detection</h1>
	<p>Outliers are providers or institutions whose charge is in the top percentage for the given procedure code.</p>   
    <p>Enter a procedure code and a percentage cutoff to see providers who charged at the top of that range for the procedure.</p>   
	<form action="result-jsp" method="get" class="form-inline"> 
		<label for="proc_code" class="form-group"> Procedure Code: </label> 		
		<input id="proc_code"  name="proc_code" type="text" class="form-control" required/>
		&nbsp;&nbsp;
		<label for="percentage" class="form-group"> Percentage: </label> 		
		<input id="percentage" type="number" min="0" max="100" step="0.01" name="percentage" class="form-control" required/> 
		 <br />
		<button type="submit" class="btn btn-success">Submit</button>
			
	</form>

</body>
</html>