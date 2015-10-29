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
		<h2> Request Doctors: </h2>
		<div>
			<b> Procedure Code:</b>
			<br>
			<input id="proc_code" type="text" size="10">
			
		</div>
		<br>
		<div>
			<b> State: </b>
			<br>
			<select id="state">
				<option label="Select the state" disabled>Select the state</option>
				<option value="AL">AL</option>
				<option value="FL">FL</option>
				<option value="WA">WA</option>
				<option value="NY">NY</option>
			</select>
		</div>
		<form>
		<br>
		<input type="radio" name="use_case" value="case_1" checked id="case1btn" onClick="javascript:caseCheck()">Case 1
		<br>
		<input type="radio" name="use_case" value="case_2" id="case2btn" onClick="javascript:caseCheck()">Case 2
		<br>
		<input type="radio" name="use_case" value="case_3" id="case3btn" onClick="javascript:caseCheck()">Case 3
		<br>
		</form>
			</div>
			<div id="proc_keyword_box" style="display:none">
				<b> Procedure Name:</b>
				<br>
				<input id="proc_keyword" type="text" size="10">
				<br>
			</div>
		<input type="submit" value="Submit" onclick="submit()" /> <br /> <span
			id="feedback"></span>
	</div>
			<div id="proc_keyword_box" style="display:none">
				<b> Keyword:</b>
				<br>
				<input id="proc_keyword" type="text" size="10">
				<br>
			</div>

	<footer>
	<p><a href="/simple-medicare-request/index.html">Home</a>
	</footer>

	<script type="text/javascript"> 

function submit() {

    if(jq('input[name="use_case"]:checked').val() != "case_3"){
    	if (jq("#proc_code").val().length == 0 || jq("#proc_code").val().length > 5) {
        	alert("Procedure Code must be filled out");
        	return;
    	}
    }
    
    if(jq('input[name="use_case"]:checked').val() == "case_3"){
    	if (jq("#proc_keyword").val().length == 0) {
        	alert("Procedure Keyword must be filled out");
        	return;
    	}
    }
    
	switch(jq('input[name="use_case"]:checked').val()){
	case("case_1"):
	
	jq(function() {
		jq.get("submit",
					{ 	proc_code:  jq("#proc_code").val(),
				  		state:  jq("#state").val(),
				  		use_case:  jq('input[name="use_case"]:checked').val()},
				  		function(data){
				  			jq("#feedback").replaceWith('<span id="feedback">' + JSON.stringify(data) + '</span>');
				  		});
	});
	break;
	case("case_2"):
		jq(function() {
			jq.get("submit",
						{ 	proc_code:  jq("#proc_code").val(),
					  		state:  jq("#state").val(),
					  		use_case:  jq('input[name="use_case"]:checked').val()},
							function(data){
					  			jq("#feedback").replaceWith('<span id="feedback">' + displayDataCase2(data) + '</span>');
					  		});
		});
		break;
	case("case_3"):
		jq(function() {
			jq.get("submit",
						{ 	keyword:  jq("#proc_keyword").val(),
					  		state:  jq("#state").val(),
					  		use_case:  jq('input[name="use_case"]:checked').val()},
							function(data){
					  			jq("#feedback").replaceWith('<span id="feedback">'+ JSON.stringify(data) + '</span>');
					  		});
		});
		break;
}
}

function displayDataCase1(data){
	var text = "";
	var i;
	for(i = 0; i < data.length; i++){
		text += data[i].last_or_org_name + ", " + data[i].first_name + " - \t" + data[i].providerDetails.averageSubmittedChargeAmount + "<br>";
	};
	
	
	return text;
}

function displayDataCase2(data){
	 var text = '<table style="width:100%">'
	
	 for(var i = 0; i < data.length; i++){
			text +=  '<tr><td>' + data[i].last_or_org_name + '</td><td>' + data[i].first_name + '</td><td>'  + data[i].beneficiaries_day_service_count + '</td></tr>';
		};
		text += '</table>';
		return text;
}

function displayEntry(data){
	
	
}

function displayCorp(data){
	
}

function displayPerson(data){
	
}

function caseCheck(){
    if (document.getElementById('case3btn').checked) {
        document.getElementById('proc_keyword_box').style.display = 'block';
        document.getElementById('proc_code').disabled = true;
    } else {
    	document.getElementById('proc_keyword_box').style.display = 'none';
        document.getElementById('proc_code').disabled = false;
    }
	
}

</script>

</body>
</html>