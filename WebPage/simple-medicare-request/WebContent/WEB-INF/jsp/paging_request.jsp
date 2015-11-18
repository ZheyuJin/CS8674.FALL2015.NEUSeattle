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

<title>Find providers by navigational facets</title>
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
	padding: .25em 0em .25em 0em; 
	border: solid 1px #30c9e0;
	background: #cc0000;
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

    <h2>Explore providers via state, zip and type</h2>
	<div id="side-bar" class="container">
		<!-- <select id="state" class="selectpicker" data-style="btn-info">
				<option label="Select the state" disabled>Select the state</option>
				<option value="AZ">AZ</option>
				<option value="CA">CA</option>
				<option value="FL">FL</option>
				<option value="GA">GA</option>
				<option value="TX">TX</option>
				<option value="NY">NY</option>
			</select>-->
		<input value="Enter a state (ie: TX)" id="state" type="text"
			name="state_request"> <input id="search_button" type="submit"
			value="Search">
			
		<div id="facet-area"></div>
	</div>

	<div id="result-area"></div>

	<br>

	<div id="next" class="link">Next</div>
	
	<script> 
       var start_index = 0;
       var page_size = 15;
       var end_index = page_size-1;
    
       var proc_code = "";
       var state = "";
       var zip_code = "";
       var facet_type = "Zip";
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
			start_index = end_index+1;
			end_index += start_index+page_size-1;
		});

		$(document).on('click', '.facet', function() {

		    switch (facet_type) {
			case "State":
                facet_type = "Zip";
				state = $(this).text();
				break;
			case "Zip":
				facet_type = "ProviderType";
				zip_code = $(this).text();
			    break;
			case "ProviderType":
                facet_type = "";
				provider_type = $(this).text();
				break;
			case "Query":
				facet_type = "";
				query = $(this).text();
				break;
			}
			start_index = start_index;
			end_index = start_index + page_size - 1;
			searchRequest();
		});

		function resetVars() {
	        start_index = 0;
	        page_size = 15;
	        end_index = page_size-1;
	        
	        proc_code = "";
	        state = "";
	        zip_code = "";
	        facet_type = "Zip";
	        provider_type = "";
	        query = "";
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
					start : start_index,
					end : end_index
				}
			}).done(function(data) {
				responseHandler(data);
			}).fail(function() { 
				window.location ="../../error.html";
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
				output += '<div class="result">' + 
				toNameCase(list[i].first_name) + " " 
				+ toNameCase(list[i].last_or_org_name) + "  "
				+ list[i].place_of_service + "  "
                + toNameCase(list[i].city) + "  "
                + list[i].state + "  "
                + list[i].zip + "  "	
                + list[i].hcpcs_description
						+ '</div>';
			}
			return output;
		}

		function handleFacets(list) {
			$("#facet-area").replaceWith(
					'<div id="facet-area">' + formatFacets(list) + '</div>');
		}

		$(document).on('click', '#search_button', function() {
			resetVars();
			state = $('#state').val();
			facet_type = "Zip";
			searchRequest();
		});

		function formatFacets(list) {
			//alert(JSON.stringify(list));
			var output = "";
			for ( var i in list.facetedCount) {
				output += '<div class="facet">'
						+ list.facetedCount[i].propertyValue + '</div>';
			}
			return output;
		}
		
	    function toNameCase(str) {
	        return str.replace(/\w\S*/g, function(txt) {
	            return txt.charAt(0).toUpperCase()
	                + txt.substr(1).toLowerCase();
	        });
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