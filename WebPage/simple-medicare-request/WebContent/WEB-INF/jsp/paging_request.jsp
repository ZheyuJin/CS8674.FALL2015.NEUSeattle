<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Find providers by navigational facets</title>

		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
		<link rel="stylesheet" href="/simple-medicare-request/css/common.css" >
		<script src="/simple-medicare-request/js/paging.js"></script>
		<script src="/simple-medicare-request/js/common.js"></script>

<link href="/simple-medicare-request/favicon.ico" rel="icon" type="image/x-icon">
<style>
</style>

</head>
<body>

	<h3>Explore providers and procedures via state, zip and specialty</h3>
	<div id="side-bar" class="container">
	
		<select id="stateSelect" class="selectpicker">
			<option label="Select the state" disabled>Select the state</option>
		</select> 
		<br />
		<br />
		
		<div>
			<b><span id="curFacet"></span></b>
		</div>
		<div id="facet-area"></div>
	</div>

	<div id="result-header" hidden=true>

		<div id="breadcrumb">
			<i> Query for providers and procedures: 
			<span id="currentState" class="link">
					state = <span id="stateVal"></span>
			</span> 
			&nbsp;<span id="currentZip" class="link"> + zip = <span id="zipVal"></span></span> 
			&nbsp;<span id="currentType" class="link"> + specialty = <span id="typeVal"></span></span>
			</i>
		</div>
        <h4>Results:</h4>

    </div>

    <div id="result-content">

		<table id="result-table">	
		</table>

        <br />
        <div id="result-nav">
            <span id="prev" class="link" hidden=true>Prev</span>

            <span id="next" class="link" hidden=true>Next</span>
        </div>
	</div>

	<br>

	<footer>
		<hr />
		<p>
			<a href="../../index.html">Home</a>
		</p>
		
	</footer>
</body>
</html>