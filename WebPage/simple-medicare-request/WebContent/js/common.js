$(document).ready(function() {
					$('body').append(
							'<div id="ajaxBusy"><p><img src="../../loading.gif">'
							+ '</p></div>');
					$('#ajaxBusy').hide();

					$('body').append(
					  
							
'<nav class="navbar navbar-default navbar-fixed-bottom">'
  + '<div class="container-fluid">'
  + '<div class="navbar-header">'
  + '<a class="navbar-brand">'
  + '<img alt="Simple Medicare Request" src="../../gorty.gif" width="30" height="30" >'
  + '</a>'
  + '</div>'
  + '<div>'
  + '<ul class="nav navbar-nav">'
  + '<li><a href="../../index.html">Home</a></li>'
  + '<li><a href="../provider/form">Providers by State</a></li>'
  + '<li><a href="../payment/form">Patient Responsbility</a></li>'
  + '<li><a href="../outlier/form">Outlier</a></li>'
  + '<li><a href="../map/usa">Urban/Rural</a></li>'
  + '<li><a href="../procedure/form">Procedures</a></li>'
  + '<li><a href="../ripoff/form">Ripoff</a></li>'
  + '<li><a href="../nb/form">Specialty Predictor</a></li>'
  + '</ul>'
  + '</div>'
  + '</div>'
  + '</nav><p class="bottom">_</p><p class="bottom">_</p><p class="bottom">_</p>');
					
				});

$(document).ajaxStart(function() {
	$('#ajaxBusy').show();
}).ajaxStop(function() {
	$('#ajaxBusy').hide();
});

function isEmpty(data) {
	return (data.length == 0);
}

function toNameCase(str) {
  return str.replace(/\w\S*/g, function(txt) {
      return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
  });
}

function upperFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}