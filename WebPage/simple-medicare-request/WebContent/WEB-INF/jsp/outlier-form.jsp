<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="Outlier">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Outliers</title>
<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

<link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>

<link rel="stylesheet"
    href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
    
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.4.5/angular.min.js"></script>
<link rel="stylesheet" href="../../css/common.css">
<script src="../../js/common.js"></script>
<script src="../../js/outlier.js"></script>

<link href="../../favicon.ico" rel="icon"
    type="image/x-icon">

</head>

<body class="container main center" ng-controller="OutlierController as con">
	<h2>Outlier Detection</h2>
	<p>Outliers are providers whose charge is within the top <i>n</i> % for the given procedure.</p>   
    <p>Enter a procedure code and percentage cutoff <i>n</i> to see providers whose charge is among top <i>n</i> %.</p>   
	
	<form class="form-inline" role="form">
	 
		<label for="proc_code" class="form-group"> Procedure Code: </label> 		
		<input id="proc_code"  name="proc_code" type="text" class="form-control"  ng-model="con.proc_code" required/>
		&nbsp;&nbsp;
		<label for="percentage" class="form-group" > Percentage: </label> 		
		<input id="percentage" type="number" min="0" max="20" step="0.01" name="percentage" class="form-control" ng-model="con.percentage" required/>
		<br/> 
		<button type="submit" class="btn btn-success" ng-click="con.loadContent()">Submit</button>
			
	
	</form> 
	
	<table class="table table-striped">
	<thead ng-show="con.rows.length">
		<th>NPI_CODE</th>
		<th>$ Avg Charged</th>
		<th>First Name</th>
		<th>Last or Organization Name</th>
		<th>Zip</th>		
		<th>State</th>
	</thead>
	
	<tbody>
		<tr ng-repeat="row in con.rows">
			<td>{{row.npi}}</td>
			<td>{{row.providerDetails.averageSubmittedChargeAmount.toFixed(0)}}</td>
			<td>{{row.first_name}}</td>
			<td>{{row.last_or_org_name}}</td>
			<td>{{row.zip}}</td>
			<td>{{row.state}}</td>
		</tr>
	</tbody>
	</table>
</body>
</html>