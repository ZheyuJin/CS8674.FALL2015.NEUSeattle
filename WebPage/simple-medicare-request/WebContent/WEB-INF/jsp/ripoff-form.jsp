<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Is this price a Ripoff ?</title>
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet" >
</head>
<body class="container main center">
	<h1>Is this price a ripoff?</h1>
	<form action="result-json" method="get" class="form-inline"> 
		
		<label for="proc_code"> Procedure Code </label> 		
		<input id="proc_code"  name="proc_code" type="text" class="form-control" />
		
		<label for="percentage"> Price </label> 		
		<input id="percentage" type="number" min="0"  step="1" name="price" class="form-control" /> 
		 
		<button type="submit" class="btn btn-success">Submit</button>
			
	</form>

</body>
</html>