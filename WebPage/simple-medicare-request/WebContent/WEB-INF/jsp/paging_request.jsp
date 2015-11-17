<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<link rel="stylesheet"
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<script type="text/javascript"
	src="http://code.jquery.com/jquery-1.11.3.min.js"></script>

<title>Paging/Faceting Example</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">
<style>
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
	background: #E00000;
	width: 13.5em;
	float: left;
}

.result-hover {
	background-color: grey;
	width: "100%";
}

.link {
	color: blue;
	text-decoration: underline;
}
</style>

</head>
<body>
	<div id="side-bar" class="container">
		<!-- <input value="Search Request" id="state" type="text"
			name="state_request"> -->
			<input id="search_button" type="submit" value="Reset Search">
		<div id="facet-area"></div>
	</div>
	<h2>This is an example of paging</h2>
	<div id="result-area"></div>
	<br>

	<div id="next" class="link">Next</div>
	<script>
		var start_index = 0;
		var end_index = 15;
		var proc_code = "";
		var state = "";
		var zip_code = "";
		var facet_type = "State";
		var provider_type = "";
		var query = "";

		$(document).on('mouseenter', '.result', function() {
			$(this).addClass('result-hover');
		});

		$(document).on('mouseleave', '.result', function() {
			$(this).removeClass('result-hover');
		});

		$(document).on('mouseenter', '.facet', function() {
			$(this).addClass('link');
		});

		$(document).on('mouseleave', '.facet', function() {
			$(this).removeClass('link');
		});

		$(document).on('click', '#next', function() {
			searchRequest();
			start_index = end_index;
			end_index += 15;
		});

		$(document).on('click', '.facet', function() {
			switch (facet_type) {
			case "State":
				facet_type = "Zip";
				state = $(this).val();
				break;
			case "Zip":
				facet_type = "ProviderType";
				zip_code = $(this).val();
				break;
			case "ProviderType":
				provider_type = $(this).val();
				break;
			case "Query":
				query = $(this).val();
				break;

			}
			start_index = parseInt($(this).text());
			end_index = start_index + 15;
			searchRequest();
		});

		function resetVars() {
			start_index = start_index;
			end_index = end_index;
			proc_code = "";
			state = "";
			zip_code = "";
		}

		function searchRequest() {
			$.ajax({
				url : "request",
				data : {
					provider_type : provider_type,
					state : state,
					zip : zip_code,
					query : query,
					facet : facet_type,
					start : 0,
					end : 15
				}
			}).done(function(data) {
				responseHandler(data);
			});
		}

		function responseHandler(data) {
			handleResults(data.providers);
			handleFacets(data.facets);
		}

		function handleResults(list) {
			$("#result-area").replaceWith(
					'<div id="result-area">' + formatResults(list) + '</div>');
		}

		function formatResults(list) {
			var output = "";
			for ( var i in list) {
				output += '<div class="result">' + list[i].last_or_org_name
						+ '</div>';
			}
			return output;
		}

		function handleFacets(list) {
			$("#facet-area").replaceWith(
					'<div id="facet-area">' + FormatFacets(list) + '</div>');
		}

		$(document).on('click', '#search_button', function() {
			resetVars();
			//state = $('#state').val();
			//facet_type = "Zip";
			searchRequest();
		});

		function FormatFacets(list) {
			//alert(JSON.stringify(list));
			var output = "";
			for ( var i in list.facetedCount) {
				output += '<div class="facet">'
						+ list.facetedCount[i].propertyValue + '</div>';
			}
			return output;
		}
	</script>
	
    <a href="/simple-medicare-request/index.html">Home</a>
</body>
</html>