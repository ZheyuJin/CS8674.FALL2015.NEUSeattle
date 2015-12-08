<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">

<script src="http://d3js.org/d3.v3.min.js"></script>
<script src="http://d3js.org/queue.v1.min.js"></script>
<script src="http://d3js.org/topojson.v1.min.js"></script>

<script type="text/javascript"
  src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

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
<body>


<script>



var width = 960,
    height = 500;

var color = d3.scale.threshold()
    .domain([1, 9])
    .range(["#f2f0f7", "#54278f"]);

var path = d3.geo.path();

var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

queue()
    .defer(d3.json, "../../us.json")
    .defer(d3.tsv, "../../norm_dist.tsv")
    .await(ready);


var formatTime = d3.time.format("%e %B");

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

      .append("svg:title")
      //.text(function(d) { return "Hello World"; });
      //;

  svg.append("path")
      .datum(topojson.mesh(us, us.objects.states, function(a, b) { return a.id !== b.id; }))
      .attr("class", "states")
      .attr("d", path);

}



</script>

  </body>
</html>