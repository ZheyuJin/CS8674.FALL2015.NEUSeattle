<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	
	<title>Get Busiest Doctors in a state for a procedure</title>
</head>
<body>
  <form action="ui/list" method="GET">
  <div>
     Enter a state:  <input type="text" name="state" size="2">
     </div>
     <div> 
     Enter a procedure code: <input type="text" name="proc" size="5">
     </div> 
     <br>
     <div>
     For example, enter FL and 99213 to see the busiest doctors in Florida for 15 minute existing patient office visits.
     </div>
    <br>
    <input type="submit" value="Submit">
    </form>

</body>
</html>