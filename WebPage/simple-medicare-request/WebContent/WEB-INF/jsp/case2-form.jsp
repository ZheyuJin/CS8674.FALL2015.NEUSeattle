<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Case2: Top providers by number of visits
	(unique_bene_day_cnt) for a given procedure and state?</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon" type="image/x-icon">

</head>
<body>
	<h2>Case2: Top providers by number of visits (unique_bene_day_cnt)
		for a given procedure and state?</h2>
	<form action="case2-result-jsp" method="GET">
		<div>
			State: <input type="text" name="state" size="2">
		</div>
		<div>
			Procedure: <input type="text" name="proc_code" size="2">
		</div>
		<input type="submit" value="JSP result">
	</form>
	<form action="case2-result-json" method="GET">
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