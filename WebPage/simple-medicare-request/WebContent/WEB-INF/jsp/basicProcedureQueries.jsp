<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Medicare Data Query Form</title>

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
<link rel="stylesheet" href="../../css/common.css">
<script src="../../js/common.js"></script>
<script src="../../js/original.js"></script>

<link href="../../favicon.ico" rel="icon"
	type="image/x-icon">

<link rel="stylesheet" type="text/css" href="//cdn.datatables.net/1.10.10/css/jquery.dataTables.css">
  
<script type="text/javascript" charset="utf8" src="//cdn.datatables.net/1.10.10/js/jquery.dataTables.js"></script>
</head>
<body class="container main center">
	<div>
		<h2>Explore Providers and Procedures</h2>
		<form role="form">
		<div class="form-group">
		<label for="proc_code"> Procedure Code </label>
		<input id="proc_code" name="proc_code" type="proc_code" class="form-control" placeholder="Enter Procedure Code"></div>
		<div class="form-group">
		<label for="proc_keyword" class="keyword_class">Procedure Keyword</label>
		<input id="proc_keyword" name="proc_keyword" type="proc_keyword" class="form-control keyword_class" placeholder="Enter Procedure Keyword">
		</div>
		</form>	
		
			<select id="stateSelect" class="selectpicker form-control">
			<option label="Select the state" disabled>Select the state</option>
		</select>
		<form>
			<br> <input type="radio" name="request-type" value="mostExpensiveProc" checked
				id="mostExpensiveProc" class="request-select">Show most
			expensive provider(s) in a state for a procedure
			<br> 
			<input
				type="radio" value="busiestProvider" name="request-type" id="busiestProvider" 
				class="request-select">Show busiest provider(s) in
			a state for a procedure
			<br> 
			<input type="radio" value="avgProcedureCost" name="request-type" 
			id="avgProcedureCost" class="request-select">Find
			procedures using a keyword, show average cost in a state<br>
		</form>
			<input id="submitSearch" type="submit" value="Submit" class="btn btn-default" />
			
	</div> 
	
	<br />

	<br />
	<span id="feedback"></span>

	<table id="busiestTable" class="table" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Service Charge Amount</th>
            </tr>
        </thead>
        <tfoot>
            <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Service Charge Amount</th>
            </tr>
        </tfoot>
    </table>

	<table id="expensiveTable" class="table" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Charged Amount</th>
            </tr>
        </thead>
        <tfoot>
            <tr>
                <th>First Name</th>
                <th>Last Name</th>
                <th>Charged Amount</th>
            </tr>
        </tfoot>
    </table>
    
	<table id="avgCostTable" class="table" cellspacing="0" width="100%">
        <thead>
            <tr>
                <th>Procedure Code</th>
                <th>Description</th>
                <th>Average Cost</th>
                <th>State</th>
            </tr>
        </thead>
        <tfoot>
            <tr>
                <th>Procedure Code</th>
                <th>Description</th>
                <th>Average Cost</th>
                <th>State</th>
            </tr>
        </tfoot>
    </table>
        


</body>
</html>