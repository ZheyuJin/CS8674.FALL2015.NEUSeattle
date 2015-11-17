<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">

<script type="text/javascript"
    src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
<script type="text/javascript">
    var jq = jQuery.noConflict();
</script>

<title>Medicare Data - Error</title>

<link href="/simple-medicare-request/favicon.ico" rel="icon"
    type="image/x-icon">

</head>
<body>

<!--  At the moment, this is the same as error.html
      but if the controllers wanted to dump in more error
      info, this jsp page is the place to do it. -->
<h3>Unfortunately....</h3>
    <div>An error occurred while processing your request.  
    Press the back button to retry the operation.</div>

    <footer>
    <hr />
        <p>
            <a href="../../index.html">Home</a>
            </p>
    </footer>
</body>
</html>