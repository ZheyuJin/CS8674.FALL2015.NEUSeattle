<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
	<script type="text/javascript">
	    var jq = jQuery.noConflict();
	</script>
	
	<title>Top Doctors - Ajax Style</title>

</head>
<body>

Demo Request:
<div style="border: 1px solid #ccc; width: 250px;">
	Request Doctors: <br/>
	<input id="gender" type="text" size="5"> +
	<input id="state" type="text" size="5">
	<input type="submit" value="Add" onclick="add()" /> <br/>
	Output: <span id="output">(Result will be shown here)</span>
</div>


<script type="text/javascript"> 

function add() {
	jq(function() {
		// Call a URL and pass two arguments
		// Also pass a call back function
		// See http://api.jquery.com/jQuery.post/
		// See http://api.jquery.com/jQuery.ajax/
		// You might find a warning in Firefox: Warning: Unexpected token in attribute selector: '!' 
		// See http://bugs.jquery.com/ticket/7535
		jq.post("add",
					{ 	gender:  jq("#gender").val(),
				  		state:  jq("#state").val() },
						function(data){
							// data contains the result
							// Assign result to the sum id
							jq("#output").replaceWith('<span id="output">'+ data + '</span>');
					});
	});
}

</script>

	<a href="/simple-medicare-request/index.html">Home</a> 
</body>
</html>