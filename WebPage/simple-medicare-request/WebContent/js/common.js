$(document).ready(function() {
					$('body').append(
									'<div id="ajaxBusy"><p><img src="/simple-medicare-request/loading.gif"></p></div>');
				});

$(document).ajaxStart(function() {
	$('#ajaxBusy').show();
}).ajaxStop(function() {
	$('#ajaxBusy').hide();
});