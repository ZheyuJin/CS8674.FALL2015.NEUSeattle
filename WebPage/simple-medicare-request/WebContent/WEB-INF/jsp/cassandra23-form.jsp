<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">


<script src="http://d3js.org/d3.v3.min.js"></script>


  <meta name="viewport" content="width=device-width, initial-scale=1">
  <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
  <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
<title>Provider payment gap</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">
<style>
.bar {
	fill: steelblue;
}

.bar:hover {
	fill: brown;
}

.axis {
	font: 10px sans-serif;
}

.axis path, .axis line {
	fill: none;
	stroke: #000;
	shape-rendering: crispEdges;
}

.x.axis path {
	display: none;
}

html, body {
	height: 100%;
	margin: 0px;
	padding: 0px;
}

#side-bar {
	height: 100%;
	margin: 0;
	padding: .25em 0em .25em 0em; //
	border: solid 1px #30c9e0;
	background: #fcac4c;
	width: 13.5em;
	float: left;
}

.result-hover {
	background-color: grey;
}

.link {
	color: blue;
	text-decoration: underline;
}

div.tooltip {   
  position: absolute;           
  text-align: center;           
  width: 75px;                  
  height: 40px;                 
  padding: 2px;             
  font: 12px sans-serif;        
  background: lightsteelblue;   
  border: 0px;      
  border-radius: 8px;           
  pointer-events: none;         
}
</style>
</head>
<body>
	<h3 id="hello">Treatment payment gap: Patient payment
		responsibility</h3>
<div class="container">
	Number of treatments:
	<input value="5" id="numRows" type="text" name="numRows" width="100%">
	<br> Starting at:
	<input value="0" id="startIndex" type="text" name="startIndex"
		class="box" width="100%">
	<br>
	<input type="checkbox" name="doHighestToLowest" value="setOrder"
		id="doHighestToLowest" checked />Sort ascending (= treatments with
	highest patient responsibility on top)
	<br>
	<input type="checkbox" name="percent" value="setAsPercent"
		id="percentBox" class="box" />Return gap as percentage of the overall
	charge?
	<br>
	<input id="request_button" type="submit" value="Search">
	<div id="graph_area"></div>
	<div id="result_area"></div>
	<br>

	<script>
		var getLargest = true;
		var isPercent = false;
		var numRows = 10;
		var startIndex = 0;

		// We might want some validation here (amount > 0 for example)

		$(document).ready(function() {

        $('body').append('<div id="ajaxBusy"><p><img src="http://chimplyimage.appspot.com/images/samples/misc-spinner/animatedSkull.gif"></p></div>');

        $('#ajaxBusy').css({
          display:"none",
          margin:"0px",
          paddingLeft:"0px",
          paddingRight:"0px",
          paddingTop:"0px",
          paddingBottom:"0px",
          position:"absolute",
          right:"3px",
          top:"3px",
           width:"auto"
        });
      });

      $(document).ajaxStart(function(){ 
        $('#ajaxBusy').show(); 
      }).ajaxStop(function(){ 
        $('#ajaxBusy').hide();
      });
		
		$(document).on('click', '#request_button', function() {
			numRows = +$('#numRows').val();
			startIndex = +$('#startIndex').val();
			isPercent = $('#percentBox').is(":checked");
			getLargest = $('#doHighestToLowest').is(":checked");
			searchRequest();
		});

		function searchRequest() {
			if (startIndex < 0 || numRows <= 0
					|| numRows > 100) {
				alert("Please enter valid search parameters");
				return;
			}

			$.ajax({
				url : "request",
				data : {
					numRows : numRows,
					start : startIndex,
					sortDesc : getLargest,
					isPercentage : isPercent
				}
			}).done(function(data) {
				responseHandler(data, isPercent);

				createGraph(data, isPercent);
			}).fail(function() {
				window.location = "../../error.html";
			});
		}

		function responseHandler(data, isPercent) {
			var output = "<hr />";

			if (data.length === 0) {
				output += "No results in the range specified";
			} else {
				output +=   '<div class="container"><div class="table-responsive">' 
				+ '<table class="table"><thead>';
				if(isPercent == true){
					output += '<thead><tr><th>Code</th><th>Description</th>'
					+ '<th>Percent</th></tr></thead>';
				} else {
					output += '<thead><tr><th>Code</th><th>Description</th>'
						+ '<th>Percent</th></tr></thead>';					
				}
			}

			for (var i = 0; i < data.length; i++) {

				// Patient responsibility is a percentage,
				// Pay gap is the amount diff
				var percentPayGapOrAmountDiff = data[i].payGap;
				var dollarSign = "$";
				var percentageSign = "";

				// There's a header value in the data (by accident)
				// Weed that one out.
				if (data[i].procCode === "hcpcs_code") {
					continue;
				}

				if (isPercent) {
					var percentPayGapOrAmountDiff = data[i].patientResponsibility;
					var dollarSign = "";
					var percentageSign = "%";
				}

				output += "<tr><td>" + data[i].procCode + "</td><td>" + data[i].desc + "</td><td>"
						+ dollarSign + "" + percentPayGapOrAmountDiff + "" 
						+ percentageSign + "</td></tr>"
				/*output += "<div>" + data[i].procCode + " " + data[i].desc + " "
						+ dollarSign + " " + percentPayGapOrAmountDiff + " "
						+ percentageSign + "</div>"*/
			}
			if (data.length != 0) {
				  output += '</table></div></div>'
				}
			$('#result_area').html(output);
		}


		function createGraph(data, isPercent) {
			
			
			var sign = "$";
			var amount = "Difference";
			if (isPercent == true) {
				sign = "%";
				amount = "Percent Paid";
			}

			$('#graph_area').replaceWith('<div id="graph_area"></div>');

			var formatTime = d3.time.format("%e %B");
			
			var div = d3.select("body").append("div")   
		    .attr("class", "tooltip")               
		    .style("opacity", 0);
			
			var margin = {
				top : 20,
				right : 20,
				bottom : 30,
				left : 40
			}, width = 960 - margin.left - margin.right, height = 500
					- margin.top - margin.bottom;

			var x = d3.scale.ordinal().rangeRoundBands([ 0, width ], .1)
					.domain(data.map(function(d) {
						return d.procCode;
					}));

			var y = d3.scale.linear().range([ height, 0 ]).domain(
					[ 0, d3.max(data, function(d) {
						return returnPayGapOrDiff(d, isPercent);
					}) ]);

			var xAxis = d3.svg.axis().scale(x).orient("bottom");

			var yAxis = d3.svg.axis().scale(y).orient("left").ticks(10, sign);

			var w = 500;
			var h = 100;
			var barPadding = 1;

			var yScale = d3.scale.linear().domain(
					[ 0, d3.max(data, function(d) {
						return returnPayGapOrDiff(d, isPercent);
					}) ]).range([ 0, h ]);

			var svg = d3.select("#graph_area")
			.append("svg")
			.attr("width", width + margin.left + margin.right)
			.attr("height",
					height + margin.top + margin.bottom)
				.append("g").attr(
					"transform",
					"translate(" + margin.left + "," + margin.top + ")");

			svg.append("g").attr("class", "x axis")
						.attr("transform",
					"translate(0," + height + ")")
					.call(xAxis);

			svg.append("g").attr("class", "y axis").call(yAxis).append("text")
					.attr("transform", "rotate(-90)").attr("y", 6).attr("dy",
							".71em").style("text-anchor", "end").text(amount);

			svg.selectAll(".bar").data(data)
								.enter()
								.append("rect")
								.attr(
					"class", "bar").attr("x", function(d) {
				return x(d.procCode);
			}).attr("width", x.rangeBand()).attr("y", function(d) {
				return y(returnPayGapOrDiff(d, isPercent));
			}).attr("height", function(d) {
				return height - y(returnPayGapOrDiff(d, isPercent));
			})
			        .on("mouseover", function(d) {      
            div.transition()        
                .duration(200)      
                .style("opacity", .9); 
            div .html("Procedure Code: " + d.procCode)  
                .style("left", (d3.event.pageX) + "px")     
                .style("top", (d3.event.pageY - 28) + "px");    
            })                  
        .on("mouseout", function(d) {       
            div.transition()        
                .duration(500)      
                .style("opacity", 0);   
        });
			
			
		}

		function returnPayGapOrDiff(data, isPercent) {
			if (isPercent == true) {
				return data.patientResponsibility;
			} else {
				return data.payGap;
			}

		}
		
	</script>

	<footer>
		<hr />
		<p>
			<a href="../../index.html">Home</a>
		</p>
	</footer>
</body>
</html>