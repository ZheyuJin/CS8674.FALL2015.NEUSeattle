$(document).ready(function() {
					$('body').append(
							'<div id="ajaxBusy"><p><img src="/simple-medicare-request/loading.gif">'
							+ '</p></div>');
					$('#ajaxBusy').hide();

					$('body').append(
//'<nav class="navbar navbar-default navbar-fixed-bottom">'
//+ '<div class="container-fluid"><div class="navbar-header">'
//+ '<a class="navbar-brand">Simple Medicare Request</a></div></div></nav>');
					  
							
'<nav class="navbar navbar-default navbar-fixed-bottom">'
  + '<div class="container-fluid">'
  + '<div class="navbar-header">'
  + '<a class="navbar-brand">'
  + '<img alt="Simple Medicare Request" src="/simple-medicare-request/gorty.gif" width="30" height="30" >'
  //+ 'Simple Medicare Request'
  + '</a>'
  + '</div>'
  + '<div>'
  + '<ul class="nav navbar-nav">'
  + '<li><a href="../../index.html">Home</a></li>'
  + '<li><a href="../paging/request">Paging</a></li>'
  + '<li><a href="../payment/patient">Patient Responsbility</a></li>'
  + '<li><a href="../outlier/form">Outlier</a></li>'
  + '<li><a href="../map/usa">Urban/Rural</a></li>'
  + '<li><a href="../assessment/submit">Basic</a></li>'
  + '<li><a href="../ripoff/form">Ripoff</a></li>'

  + '</ul>'
  + '</div>'
  + '</div>'
  + '</nav>');
					
				});

$(document).ajaxStart(function() {
	$('#ajaxBusy').show();
}).ajaxStop(function() {
	$('#ajaxBusy').hide();
});