<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<script type="text/javascript"
	src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
<script type="text/javascript">
	    var jq = jQuery.noConflict();
	</script>

<title>Top Medicare Providers</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon" type="image/x-icon">

</head>
<body>

	<!-- 
<div class="container" style="min-height: 500px">

	<div class="basic-input">
		<h1>Search Form</h1>
		<br />

		<div id="feedback"></div>

		<form class="form-horizontal" id="search-form">
			<div class="form-group form-group-lg">
				<label class="col-sm-2 control-label">State</label>
				<div class="col-sm-10">
					<input type=text class="form-control" id="state">
				</div>
			</div>
			<div class="form-group form-group-lg">
				<label class="col-sm-2 control-label">Provider ID</label>
				<div class="col-sm-10">
					<input type="text" class="form-control" id="providerID">
				</div>
			</div>

			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<button type="submit" id="bth-search"
						class="btn btn-primary btn-lg" onclick="submit()">Search</button>
				</div>
			</div>
		</form>

	</div>

</div>
-->

	<div>
		Request Doctors: <br />
		<div>
			Procedure code:<input id="proc_code" type="text" size="10">
		</div>
		<br>
		<div>
			State: <select id="state">
				<option label="Select the state">Select the state</option>
				<option value="AZ">AZ</option>
				<option value="CA">CA</option>
				<option value="FL">FL</option>
				<option value="GA">GA</option>
				<option value="TX">TX</option>
				<option value="NY">NY</option>
			</select>
		</div>
		<input type="submit" value="Submit" onclick="submit()" /> <br /> <span
			id="feedback"></span>
	</div>

	<script type="text/javascript"> 

function submit() {
	jq(function() {
		if(jq("#state").val() != "AL"){
		jq.post("submit",
					{ 	proc_code:  jq("#proc_code").val(),
				  		state:  jq("#state").val() },
						function(data){
							jq("#feedback").replaceWith('<span id="feedback">'+ JSON.stringify(data) + '</span>');
				  		});}
	});
}

function displayData(data){
	
	var text = "";
	var i;
	
	for(i = 0; i < data.result.length; i++){
		text += data.result[i].last_or_org_name + "<br>";
	}
	text += " " + data.msg + " " + data.code + "<br>";
	
	return text;
}

</script>

	<a href="/simple-medicare-request/index.html">Home</a>
</body>
</html>