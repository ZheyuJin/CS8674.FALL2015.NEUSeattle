[1mdiff --git a/WebPage/simple-medicare-request/WebContent/WEB-INF/jsp/urbanrural_map.jsp b/WebPage/simple-medicare-request/WebContent/WEB-INF/jsp/urbanrural_map.jsp[m
[1mindex 3f4e469..43a3bb6 100644[m
[1m--- a/WebPage/simple-medicare-request/WebContent/WEB-INF/jsp/urbanrural_map.jsp[m
[1m+++ b/WebPage/simple-medicare-request/WebContent/WEB-INF/jsp/urbanrural_map.jsp[m
[36m@@ -44,14 +44,13 @@[m
 [m
 <script>[m
 [m
[31m-[m
[31m-[m
 var width = 960,[m
     height = 500;[m
 [m
 var color = d3.scale.threshold()[m
[31m-    .domain([1, 9])[m
[31m-    .range(["#f2f0f7", "#54278f"]);[m
[32m+[m[32m    .domain([-0.6, 1.6])[m[41m[m
[32m+[m[32m    .range(["#f2f0f7", "#bcbddc", "#756bb1", "#54278f"]);[m[41m[m
[32m+[m[32m    //.range(["#f2f0f7", "#54278f"]);[m[41m[m
 [m
 var path = d3.geo.path();[m
 [m
[36m@@ -64,9 +63,6 @@[m [mqueue()[m
     .defer(d3.tsv, "../../norm_dist.tsv")[m
     .await(ready);[m
 [m
[31m-[m
[31m-var formatTime = d3.time.format("%e %B");[m
[31m-[m
 var div = d3.select("body").append("div") [m
     .style("opacity", 0);[m
 [m
[36m@@ -86,7 +82,7 @@[m [mfunction ready(error, us, urbanrural) {[m
       .attr("d", path)[m
       .style("fill", function(d) { return color(rateById[d.id]); })[m
 [m
[31m-      .append("svg:title")[m
[32m+[m[32m      //.append("svg:title")[m[41m[m
       //.text(function(d) { return "Hello World"; });[m
       //;[m
 [m
[36m@@ -94,11 +90,8 @@[m [mfunction ready(error, us, urbanrural) {[m
       .datum(topojson.mesh(us, us.objects.states, function(a, b) { return a.id !== b.id; }))[m
       .attr("class", "states")[m
       .attr("d", path);[m
[31m-[m
 }[m
 [m
[31m-[m
[31m-[m
 </script>[m
 [m
   </body>[m
[1mdiff --git a/WebPage/simple-medicare-request/WebContent/js/original.js b/WebPage/simple-medicare-request/WebContent/js/original.js[m
[1mindex 1e732f7..cf12268 100644[m
[1m--- a/WebPage/simple-medicare-request/WebContent/js/original.js[m
[1m+++ b/WebPage/simple-medicare-request/WebContent/js/original.js[m
[36m@@ -59,7 +59,8 @@[m [m$(document).on('click', '#submitSearch', function() {[m
 				}).fail(function() {[m
 			window.location = "../../error.html";[m
 		});[m
[31m-	});*/[m
[32m+[m	[32m});[m[41m[m
[32m+[m	[32m*/[m[41m[m
 });[m
 [m
 function responseHandler(URL, data){[m
[1mdiff --git a/WebPage/simple-medicare-request/src/org/hunter/medicare/controller/BasicProcedureController.java b/WebPage/simple-medicare-request/src/org/hunter/medicare/controller/BasicProcedureController.java[m
[1mindex 7fc5a47..1996eb1 100644[m
[1m--- a/WebPage/simple-medicare-request/src/org/hunter/medicare/controller/BasicProcedureController.java[m
[1m+++ b/WebPage/simple-medicare-request/src/org/hunter/medicare/controller/BasicProcedureController.java[m
[36m@@ -127,6 +127,7 @@[m [mpublic class BasicProcedureController {[m
         throw new Exception("This is an error");[m
     }[m
 [m
[32m+[m[32m    /*[m
     @ExceptionHandler({ Exception.class })[m
     public String genericError() {[m
         // Returns the logical view name of an error page, passed to[m
[36m@@ -136,6 +137,7 @@[m [mpublic class BasicProcedureController {[m
         // for more options.[m
         return "genericError";[m
     }[m
[32m+[m[32m    */[m
 }[m
 [m
 /**[m
