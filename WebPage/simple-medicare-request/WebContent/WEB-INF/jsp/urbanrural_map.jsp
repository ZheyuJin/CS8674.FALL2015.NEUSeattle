<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">

    <link rel="stylesheet" href="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
  <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
  <script src="http://d3js.org/d3.v3.min.js"></script>
  <script src="http://d3js.org/queue.v1.min.js"></script>
  <script src="http://d3js.org/topojson.v1.min.js"></script>
  
<script type="text/javascript"
  src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
<style>
  .zip {
    stroke: #CCC;
    stroke-width: .5px;
  }

.blank {
  fill: none;
    stroke: #CCC;
    stroke-width: .5px;}
}
.q0-9 { fill:rgb(247,251,255); 
    stroke: #CCC;
    stroke-width: .5px;}
.q1-9 { fill:rgb(222,235,247); 
    stroke: #CCC;
    stroke-width: .5px;}
.q2-9 { fill:rgb(198,219,239); 
    stroke: #CCC;
    stroke-width: .5px;}
.q3-9 { fill:rgb(158,202,225); 
    stroke: #CCC;
    stroke-width: .5px;}
.q4-9 { fill:rgb(107,174,214); 
    stroke: #CCC;
    stroke-width: .5px;}
.q5-9 { fill:rgb(198,219,239);
  /*fill:rgb(66,146,198); */
    stroke: #CCC;
    stroke-width: .5px;}
.q6-9 { fill:rgb(33,113,181); 
    stroke: #CCC;
    stroke-width: .5px;}
.q7-9 { fill:rgb(8,81,156); 
    stroke: #CCC;
    stroke-width: .5px;}
.q8-9 { 
    fill:rgb(8,48,107);
    /*fill:rgb(8,48,107);*/ 
    stroke: #CCC;
    stroke-width: .5px;}


.test { fill:rgb(8,48,107);}


</style>
</head>
<body>
<h2> Urban vs Rural</h2>
  <script type="text/javascript">



  
    var width = 960,
    height = 500;

var color = d3.scale.threshold()
    .domain([1, 3, 5, 7, 9])
    .range(["q0-9", "q1-9", "q3-9", "q5-9", "q7-9", "q8-9"]);

var quantize = d3.scale.quantize()
    .domain([1, 12])
    .range(d3.range(9).map(function(i) { return "q" + i + "-9"; }));

    var path = d3.geo.path();


    var rateById = d3.map();

    var svg = d3.select("body").append("svg")
    .attr("width", width)
    .attr("height", height);

    queue()
    .defer(d3.json, "../../us_2.json")
    .defer(d3.tsv, "../../normal_dist.tsv", function(d) { rateById.set(d.id, +d.rate); })
    .await(ready);

    function findFill(data){
      if(rateById.get(data.properties.zip) == null){
        return "blank";
      }
      if(rateById.get(data.properties.zip) <= 0){
        return "q5-9";        
      }
      return "q8-9";
    }


    function ready(error, us, urbanrural) {


            svg.append("g")
      .attr("class", "counties")
      .attr("class", "tipsy")
      .selectAll("path")
      .data(topojson.feature(us, us.objects.zip_codes_for_the_usa).features)
      .enter().append("path")
      //.attr("original-title", "HELLO WORLD")
      .attr("class", "zip")
      .attr("class", "tipsy")
      .attr("class", function(d){return findFill(d);})//findFillColor(d);})
      .attr("d", path);

      svg.append("path")
      .attr("class", "tipsy")
      .attr("data-state", function(d) {return d.properties.state; })
      .attr("data-name", function(d) {return d.properties.name; })
      .attr("d", path);

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