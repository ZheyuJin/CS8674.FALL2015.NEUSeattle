<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Case1: Who are the top 10 most expensive providers for a
	given state and procedure?</title>
</head>
<body>
	<h2>Case1: Who are the top 10 most expensive providers for a given
		state and procedure?</h2>
	<!-- change actoin to case1-result-json you get json response -->
	<form action="case1-result-jsp" method="GET">
		<div>
			State: <input type="text" name="state" size="2">
		</div>
		<div>
			Procedure: <input type="text" name="proc_code" size="2">
		</div>
		<input type="submit" value="JSP result">
	</form>
	<form action="case1-result-json" method="GET">
		<div>
			State: <input type="text" name="state" size="2">
		</div>
		<div>
			Procedure: <input type="text" name="proc_code" size="2">
		</div>
		<input type="submit" value="JSON result">
	</form>
</body>
</html>