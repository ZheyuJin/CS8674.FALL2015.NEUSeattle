<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!DOCTYPE html">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">

<script type="text/javascript"
	src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
<script type="text/javascript">
	var jq = jQuery.noConflict();
</script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js" charset="utf-8"></script>
<style type="text/css">
            div.bar {
                display: inline-block;
                width: 20px;
                height: 75px;
                background-color: teal;
</style>
<title>Medicare Data Query Form</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">

</head>
<body>
<!-- <div>
		<h2>Request Doctors:</h2>
		<div>
			<b> Procedure Code:</b> <br> <input id="proc_code" type="text"
				size="10">

		</div>
		<br>
		<div>
			<b> State: </b> <br> <select id="state">
				<option label="Select the state" disabled>Select the state</option>
				<option value="AZ">AZ</option>
				<option value="CA">CA</option>
				<option value="FL">FL</option>
				<option value="GA">GA</option>
				<option value="TX">TX</option>
				<option value="NY">NY</option>
			</select>
		</div>
		<form>
			<br> <input type="radio" name="use_case" value="case_1" checked
				id="case1btn" onClick="javascript:caseCheck()">Case 1 <br>
			<input type="radio" name="use_case" value="case_2" id="case2btn"
				onClick="javascript:caseCheck()">Case 2 <br> <input
				type="radio" name="use_case" value="case_3" id="case3btn"
				onClick="javascript:caseCheck()">Case 3 <br>
		</form>
	</div>
	<div id="proc_keyword_box" style="display: none">
		<b> Procedure Name:</b> <br> <input id="proc_keyword" type="text"
			size="10"> <br>
	</div>
	<input type="submit" value="Submit" onclick="submit()" />
	<input type="submit" value="Convert" onclick="displayData()" />
	<br />
	<span id="feedback"></span>
	<div id="proc_keyword_box" style="display: none">
		<b> Keyword:</b> <br> <input id="proc_keyword" type="text"
			size="10"> <br>
	</div>

	<footer>
		<p>
			<a href="../../index.html">Home</a>
	</footer>
 -->	
 
 
	<script type="text/javascript">
    forceSubmit();

	
    function forceSubmit(){
    	jq(function() {
			jq.get("submit", {
				proc_code : "99213",//jq("#proc_code").val(),
				state : "FL",//jq("#state").val(),
				use_case : "case_2"//jq('input[name="use_case"]:checked').val()
			}, function(data) {
				d3helper(data);
			});
			
		});
    
    }
    
    function d3helper(data){
    	var dataset = [];
    		
		for (var i = 0; i < data.length; i++) {
			dataset.push([data[i].beneficiaries_day_service_count),data[i].beneficiaries_unique_count]);
			//dataset.push(data[i].beneficiaries_day_service_count);//,data[i].beneficiaries_unique_count]);
			
		}
		alert(JSON.stringify(data));
		//alert(JSON.stringify(dataset));
	    /*d3.select("body").selectAll("div")
        .data(dataset)
        .enter()
        .append("div")
        .attr("class", "bar")
        .style("height", function(d) {
        	
        	//forceSubmit();
            var barHeight = d * 5;  //Scale up by factor of 5
            return barHeight + "px";
        });*/
	    
	    var w = 500;
		var h = 300;
		var padding = 30;
		
		//Dynamic, random dataset
		/*var dataset = [];					//Initialize empty array
		var numDataPoints = 50;				//Number of dummy data points to create
		var xRange = Math.random() * 1000;	//Max range of new x values
		var yRange = Math.random() * 1000;	//Max range of new y values
		for (var i = 0; i < numDataPoints; i++) {					//Loop numDataPoints times
			var newNumber1 = Math.round(Math.random() * xRange);	//New random integer
			var newNumber2 = Math.round(Math.random() * yRange);	//New random integer
			dataset.push([newNumber1, newNumber2]);					//Add new number to array
		}*/

		//Create scale functions
		var xScale = d3.scale.linear()
							 .domain([0, d3.max(dataset, function(d) { return d[0]; })])
							 .range([padding, w - padding * 2]);

		var yScale = d3.scale.linear()
							 .domain([0, d3.max(dataset, function(d) { return d[1]; })])
							 .range([h - padding, padding]);

		var rScale = d3.scale.linear()
							 .domain([0, d3.max(dataset, function(d) { return d[1]; })])
							 .range([2, 5]);

		var formatAsPercentage = d3.format(".1%");

		//Define X axis
		var xAxis = d3.svg.axis()
						  .scale(xScale)
						  .orient("bottom")
						  .ticks(5)
						  .tickFormat(formatAsPercentage);

		//Define Y axis
		var yAxis = d3.svg.axis()
						  .scale(yScale)
						  .orient("left")
						  .ticks(5)
						  .tickFormat(formatAsPercentage);

		//Create SVG element
		var svg = d3.select("body")
					.append("svg")
					.attr("width", w)
					.attr("height", h);

		//Create circles
		svg.selectAll("circle")
		   .data(dataset)
		   .enter()
		   .append("circle")
		   .attr("cx", function(d) {
		   		return xScale(d[0]);
		   })
		   .attr("cy", function(d) {
		   		return yScale(d[1]);
		   })
		   .attr("r", function(d) {
		   		return rScale(d[1]);
		   });

		
		//Create X axis
		svg.append("g")
			.attr("class", "axis")
			.attr("transform", "translate(0," + (h - padding) + ")")
			.call(xAxis);
		
		//Create Y axis
		svg.append("g")
			.attr("class", "axis")
			.attr("transform", "translate(" + padding + ",0)")
			.call(yAxis);
	    
	    
    }
    
	function submit() {

			if (jq('input[name="use_case"]:checked').val() != "case_3") {
				if (!document.getElementById('proc_code').value
						.match(new RegExp(/^[A-Za-z0-9]{3,5}$/))) {
					alert("Procedure Code must be filled out properly.");
					return;
				}
			}

			if (jq('input[name="use_case"]:checked').val() == "case_3") {
				if (jq("#proc_keyword").val().length == 0) {
					alert("Procedure Keyword must be filled out.");
					return;
				}
			}
			jq("#feedback").replaceWith(
					'<span id="feedback">Searching...</span>');

			switch (jq('input[name="use_case"]:checked').val()) {
			case ("case_1"):

				jq(function() {
					jq.get("submit", {
						proc_code : jq("#proc_code").val(),
						state : jq("#state").val(),
						use_case : jq('input[name="use_case"]:checked').val()
					}, function(data) {
						jq("#feedback").replaceWith(
								'<span id="feedback">' + dataset//displayDataCase1(data)
										+ '</span>');
					});
					
				});
				break;
			case ("case_2"):
				jq(function() {
					jq.get("submit", {
						proc_code : jq("#proc_code").val(),
						state : jq("#state").val(),
						use_case : jq('input[name="use_case"]:checked').val()
					}, function(data) {
						jq("#feedback").replaceWith(
								'<span id="feedback">' + displayDataCase2(data)
										+ '</span>');
					});
					
				});
			//handleClick();
				break;
			case ("case_3"):
				jq(function() {
					jq.get("submit", {
						keyword : jq("#proc_keyword").val(),
						state : jq("#state").val(),
						use_case : jq('input[name="use_case"]:checked').val()
					}, function(data) {
						jq("#feedback").replaceWith(
								'<span id="feedback">' + changeDataset()//displayDataCase3(data)
										+ '</span>');
					});
				});
				break;
			}
		}
		
		var dataset = [];
		
		var p = d3.select("body").selectAll("footer")
		.data(dataset);
		
		function handleClick(){
			//d3.select("body").append("p");
			//dataset = data;
			var p = d3.select("body").selectAll("div")
			.data(dataset)
			.enter()
			.attr("class","bar")
			.style("height", function(d){
				var barheight = d * 5;
				return barHeight + "px";
			});
			alert(dataset[0]);
			
		}

		function displayDataCase1(data) {
			if (checkEmpty(data)) {
				return "No providers found for that criteria.";
			}

			var text = '<table style="width:100%"><tr><td>'
			+ '<b>Last Name</b></td><td><b>First Name</b></td>'
			+ '<td><b> Submitted Charge Amount </b></td></tr>';

			for (var i = 0; i < data.length; i++) {
				text += '<tr><td>' + toNameCase(data[i].last_or_org_name)
						+ '</td><td>' + toNameCase(data[i].first_name)
						+ '</td><td>\$'
						+ data[i].providerDetails.averageSubmittedChargeAmount.toFixed(2)
						+ '</td></tr>';
			}
			text += '</table>';

			return text;
		}

		function displayDataCase2(data) {
			if (checkEmpty(data)) {
				return "No providers found for that criteria."
			}
			var text = '<table style="width:100%"><tr><td>'
			+ '<b>Last Name</b></td><td><b>First Name</b></td>'
			+ '<td><b> Day Service Count </b></td></tr>';

			for (var i = 0; i < data.length; i++) {
				text += '<tr><td>' + toNameCase(data[i].last_or_org_name)
						+ '</td><td>' + toNameCase(data[i].first_name)
						+ '</td><td>' + data[i].beneficiaries_day_service_count
						+ '</td></tr>';

				dataset.push(data[i].beneficiaries_day_service_count);
				//alert("got here");
			}
			text += '</table>';
			handleClick();
			return text;
		}
		
		function displayData(dataset){
			
		}

		function displayDataCase3(data) {
			if (checkEmpty(data)) {
				return "No procedures found for that keyword.";
			}
			var text = '<table style="width:100%"><tr>'
					+ '<td><b>Procedure Code</b></td>'
					+ '<td><b>Description</b></td><td><b> Average Cost </b></td>'
					+ '<td><b> State </b></td></tr>';

			for (var i = 0; i < data.length; i++) {
				text += '<tr><td>' + data[i].procCode + '</td><td>'
						+ data[i].desc + '</td><td>\$' + data[i].avgCost.toFixed(2)
						+ '</td><td>' + data[i].state + '</td></tr>';
			}
			text += '</table>';
			return text;
		}

		
		
		function checkEmpty(data) {
			if (data.length == 0) {
				return true;
			} else {
				return false;
			}
		}
		
		

		function caseCheck() {
			if (document.getElementById('case3btn').checked) {
				document.getElementById('proc_keyword_box').style.display = 'block';
				document.getElementById('proc_code').disabled = true;
			} else {
				document.getElementById('proc_keyword_box').style.display = 'none';
				document.getElementById('proc_code').disabled = false;
			}
		}

		function toNameCase(str) {
			return str.replace(/\w\S*/g, function(txt) {
				return txt.charAt(0).toUpperCase()
						+ txt.substr(1).toLowerCase();
			});
		};
	</script>

</body>
</html>