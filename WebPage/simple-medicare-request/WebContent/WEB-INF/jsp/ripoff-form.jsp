<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Is this price a Ripoff ?</title>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet" >
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">
	
	<script>
	$(init);
	
	function init(){
		
	}
	
	 function init()
	  {
	    $("#button").click(function(){
	    	
	      	var proc_code = $("#proc_code").val();
	      	var price = $("#price").val();
	    	/* alert(""+ price + ":" + proc_code); */
	      	var content = " % of other people paied less than this price.";
	      	
	      	$("#text").html("loading...");
	      	
	      	/* send ajax  */
	      	$.ajax({
	      		
	            url: "http://localhost:8080/simple-medicare-request/assessment/ripoff/result-json?proc_code="+ proc_code+"&price="+price,
	            dataType: "json",
	            success: function(response)
	            {
	            	setText(response);
	            }
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
	<h1>Is this price a ripoff?</h1>
	<!-- <form action="result-json" method="get" class="form-inline"> -->
	<!-- <form method="get" class="form-inline">  -->
	<div class="form-inline">
		<label for="proc_code"> Procedure Code </label> 		
		<input id="proc_code"  name="proc_code" type="text" class="form-control" />
		
		<label for="price"> Price </label> 		
		<input id="price" type="number" min="0"  step="1" name="price" class="form-control" /> 
		 
		<button type="submit" class="btn btn-success" id="button">Submit</button>
	</div>		
	<!-- </form> -->
	
	<h3 id="text" ></h3>
	 
</body>
</html>