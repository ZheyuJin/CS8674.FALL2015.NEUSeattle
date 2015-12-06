$(document).ready(function() {
					$('body').append(
							'<div id="ajaxBusy"><p><img src="/simple-medicare-request/loading.gif">'
							+ '</p></div>');
					$('#ajaxBusy').hide();

					$('body').append('<nav class="navbar navbar-default navbar-fixed-top">'
							+ '<div class="container-fluid"><div class="navbar-header">'
					      + '<a class="navbar-brand" href="#">Medicare Simple Request</a></div></div></nav>');
					  
							
/*<nav class="navbar navbar-default navbar-fixed-top">
  <div class="container-fluid">
    <div class="navbar-header">
      <a class="navbar-brand" href="#">WebSiteName</a>
    </div>
    <div>
      <ul class="nav navbar-nav">
        <li><a href="../../index.html">Home</a></li>
        <li><a href="#">Page 1</a></li>
        <li><a href="#">Page 2</a></li>
        <li><a href="#">Page 3</a></li>
      </ul>
    </div>
  </div>
</nav>'*/
					
				});

$(document).ajaxStart(function() {
	$('#ajaxBusy').show();
}).ajaxStop(function() {
	$('#ajaxBusy').hide();
});