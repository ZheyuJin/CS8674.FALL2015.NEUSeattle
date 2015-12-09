<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">

<script src="http://d3js.org/d3.v3.min.js"></script>
<script src="http://d3js.org/queue.v1.min.js"></script>
<script src="http://d3js.org/topojson.v1.min.js"></script>

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">

<script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
<link rel="stylesheet" href="/simple-medicare-request/css/common.css">
<script src="/simple-medicare-request/js/common.js"></script>
<script src="/simple-medicare-request/js/original.js"></script>

<link href="/simple-medicare-request/favicon.ico" rel="icon" type="image/x-icon">

<style>

.counties {
  fill: none;
}

.states {
  fill: none;
    stroke: #CCC;
    stroke-width: .5px;}

.zip {
    stroke: #CCC;
    stroke-width: .5px;
}

.q0-9 { fill:rgb(247,251,255); }
.q1-9 { fill:rgb(222,235,247); }
.q2-9 { fill:rgb(198,219,239); }
.q3-9 { fill:rgb(158,202,225); }
.q4-9 { fill:rgb(107,174,214); }
.q5-9 { fill:rgb(66,146,198); }
.q6-9 { fill:rgb(33,113,181); }
.q7-9 { fill:rgb(8,81,156); }
.q8-9 { fill:rgb(8,48,107); }


</style>
</head>
<body class="container">
<h2> Urban and Rural Mapping </h2>
<div class="map"></div>
<h6></h6>
<script>

var width = 960,
    height = 500;

//Min:  Max: 
	//Min: -0.4123699698032442 Max: 2.0826857739844256

var color = d3.scale.threshold()
    .domain([-0.42, -.35, -.10, .11, 2.1])
    //.range(["#f2f0f7", "#54278f"]);
    .range(["#f2f0f7", "#dadaeb", "#bcbddc", "#9e9ac8", "#756bb1", "#54278f"]);
    //.range(["#f2f0f7", "#bcbddc", "#756bb1", "#54278f"]);
    //.range(["#f2f0f7", "#54278f"]);

var path = d3.geo.path();

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

queue()
    .defer(d3.json, "../../us.json")
    .defer(d3.tsv, "../../norm_dist.tsv")
    .await(ready);

var div = d3.select("body").append("div") 
    .style("opacity", 0);

function ready(error, us, urbanrural) {
  if (error) throw error;

  var rateById = {};

  urbanrural.forEach(function(d) { rateById[d.id] = +d.rate; });

  svg.append("g")
      .attr("class", "counties")
    .selectAll("path")
      .data(topojson.feature(us, us.objects.counties).features)
    .enter().append("path")
      .attr("class", "zip")
      .attr("d", path)
      .style("fill", function(d) { return color(rateById[d.id]); })
      .on("mouseover", function(d) {
				div.transition().duration(200).style("opacity", .9);
				div.html("County Code: " + d.id + " - Urban/Rural Value: " + rateById[d.id])
					.style("left", (d3.event.pageX) + "px")
					.style("top", (d3.event.pageY - 28) + "px");})
		.on("mouseout", function(d) {
				div.transition().duration(500).style("opacity", 0);});


  svg.append("path")
      .datum(topojson.mesh(us, us.objects.states, function(a, b) { return a.id !== b.id; }))
      .attr("class", "states")
      .attr("d", path);
}

</script>

  </body>
</html>