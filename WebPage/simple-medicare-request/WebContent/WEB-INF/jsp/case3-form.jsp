<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Case1: Who are the top 10 most expensive providers for a
	given state and procedure?</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">

</head>
<body>
	<h2>Case3: Avg cost for procedures given keyword and states?</h2>
	<!-- change actoin to case1-result-json you get json response -->
	<form action="case3-result-jsp" method="GET">
		<div>
			State: <input type="text" name="state" size="2">
		</div>
		<div>
			Procedure Description: <input type="text" name="proc_desc" size="50">
		</div>
		<input type="submit" value="JSP result">
	</form>
	<form action="case3-result-json" method="GET">
		<div>
			State: <input type="text" name="state" size="2">
		</div>
		<div>
			Procedure Description: <input type="text" name="proc_desc" size="50">
		</div>
		<input type="submit" value="JSON result">
	</form>
</body>
</html>