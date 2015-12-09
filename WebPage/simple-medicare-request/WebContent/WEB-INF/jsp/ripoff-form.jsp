<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Is this price a Ripoff ?</title>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

<link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="../../css/common.css">
<script src="../../js/common.js"></script>

<link href="../../favicon.ico" rel="icon"
    type="image/x-icon">

	
	<script>
	$(init);
	
	 function init()
	  {
	    $("#button").click(function(){
	    	
	      	var proc_code = $("#proc_code").val();
	      	var price = $("#price").val();
	    	/* alert(""+ price + ":" + proc_code); */
	      	var content = " % of others paid more than you.";
	      	
    		if (!proc_code.match(new RegExp(/^[A-Za-z0-9]{3,5}$/))) {
    			alert("A valid Procedure Code must be input.");
    			return;
    		}

    		if (price.length == 0 || !price.match(new RegExp(/^[0-9]{0,10}$/))) {
	    		alert("Price must be filled out correctly.");
	    		return;
	    	}
	    	
	      //$("#text").html("loading...");
	      	
	      	/* send ajax  */
	      	$.ajax({
	      		
	            /* url: "http://localhost:8080/simple-medicare-request/assessment/ripoff/result-json?proc_code="+ proc_code+"&price="+price, */
	            url: "result-json?proc_code="+ proc_code+"&price="+price,
	            dataType: "json",
	            success: function(response)
	            {
	            	setText(response);
	            }
	          }).fail(function(){
	        	 alert("No valid response was found.\nPlease check your input and\ntry again."); 
	          });
	      	
	      	
	      	/* set text to the div.  */
	      	function setText(response){
				 /* alert("sucess!!:" + response.toFixed(1)); */
				 $("#text").html(""+ response.toFixed(1) + content); 
			 }
	      	
	    });
	    
		 
	  }
	 

	
	</script>
</head>
<body class="container main center">
	<h2>Is this price a ripoff?</h2>
	<p>By providing a procedure code and price in USD, you can see how many others paid less than this.</p>  
	<!-- <form action="result-json" method="get" class="form-inline"> -->
	<!-- <form method="get" class="form-inline">  -->
	<div class="form-inline">
		<label for="proc_code"> Procedure Code: </label> 		
		<input id="proc_code"  name="proc_code" type="text" class="form-control" />
		&nbsp;&nbsp;
		<label for="price"> Price: $ </label> 		
		<input id="price" type="number" min="0"  step="1" name="price" class="form-control" /> 
		 <br />
		<button type="submit" class="btn btn-success" id="button">Submit</button>
	</div>		
	<!-- </form> -->
	
	<h3 id="text" ></h3>
	
	<!--  
		<footer>
		<hr />
		<p>
			<a href="../../index.html">Home</a>
		</p>	
		
	</footer>
	   -->
	 
</body>
</html>