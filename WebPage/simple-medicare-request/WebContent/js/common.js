$(document).ready(function() {
					$('body').append(
									'<div id="ajaxBusy"><p><img src="/simple-medicare-request/loading.gif"></p></div>');
					$('#ajaxBusy').hide();
				});

$(document).ajaxStart(function() {
	$('#ajaxBusy').show();
}).ajaxStop(function() {
	$('#ajaxBusy').hide();
});