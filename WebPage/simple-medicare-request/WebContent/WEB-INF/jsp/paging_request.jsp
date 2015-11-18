<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap-theme.min.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>

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

	<h3>Explore providers and procedures via state, zip and specialty</h3>
	<div id="side-bar" class="container">

		<select id="stateSelect" class="selectpicker">
			<option label="Select the state" disabled>Select the state</option>
		</select> <input id="search_button" type="submit" value="Search"> <br />
		<div>
			<b><span id="curFacet"></span></b>
		</div>
		<div id="facet-area"></div>
	</div>

	<div id="result-header" hidden=true>

		<div id="breadcrumb">
			<i> Query for providers and procedures: 
			<span id="currentState">
					state = <span id="stateVal"></span>
			</span> 
			<span id="currentZip">+ zip = <span id="zipVal"></span></span> 
			<span id="currentType">+ specialty = <span id="typeVal"></span></span>
			</i>
		</div>
        <h4>Results:</h4>

    </div>

    <div id="result-content">

		<table id="result-table">	
		</table>

        <div id="result-area"></div>

	</div>

	<br>

	<div id="next" class="link" hidden=true>Next</div>

	<script>
    var start_index = 0;
    var page_size = 10;
    var end_index = page_size - 1;

    var proc_code = "";
    var state = "";
    var zip_code = "";
    var facet_type = "Zip";
    var provider_type = "";
    var query = "";
    $("#result-header").hide();
    $("#next").hide();
    $("#breadcrumb").hide();
    $("#currentState").hide();
    $("#currentZip").hide();
    $("#currentType").hide();

    
    var states = [ "AZ", "CA", "FL", "TX", "GA", "NY" ];
    var dropdown = $("#stateSelect");
    for (var i = 0; i < states.length; i++) {
      dropdown.append(new Option(states[i], states[i]));
    };
    // TODO: get states from facet query and populate dropdown
    //getStates();

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
    });

    //$(document).on('click', '#stateSelect', function() {
    //alert("click!");
    //});

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
      start_index = 0;
      end_index = start_index + page_size - 1;
      searchRequest();
    });

    function resetVars() {

      start_index = 0;
      page_size = 10;
      end_index = page_size - 1;

      proc_code = "";
      state = "";
      zip_code = "";
      facet_type = "Zip";
      provider_type = "";
      query = "";
      $("#next").hide();
      $("#result-header").hide();
      $("#breadcrumb").hide();

      $("#curFacet").text("");

      $("#currentState").hide();
      $("#currentZip").hide();
      $("#currentType").hide();

    }

    // ToDo: get this working for dropdown population
    function getStates() {

      $.ajax({
        url : "request",
        data : {
          facet : "State"
        }
      }).done(function(data) {
        alert(data.numProvidersTotal);
        setDropdown(data.facets);
      }).fail(function() {
        window.location = "../../error.html";
      });
    }

    function setDropdown(facetData) {

      var dropdown = $("#stateSelect");
      alert("here");
      alert(facetData.facetType);
      alert(facetData.facetedCount.length);

      for (var i = 0; i < facetData.facetedCount.length; i++) {
        var curState = facetData.facetedCount[i].propertyValue;
        var curCount = facetData.facetedCount[i].propertyCount;
        dropdown.append(new Option(curState, curState + " ( " + curCount + " )"));
      }
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
        window.location = "../../error.html";
      });
    }

    function responseHandler(data) {

      $("#result-header").show();

      if (data.numProvidersTotal > end_index + 1) {
        $("#next").show();
      } else {
        $("#next").hide();
      }

      $("#breadcrumb").show();
      if (state != "") {
        $("#stateVal").text(state);
        $("#currentState").show();
      }
      if (zip_code != "") {
        $("#zipVal").text(zip_code);
        $("#currentZip").show();
      }
      if (provider_type != "") {
        $("#typeVal").text(provider_type);
        $("#currentType").show();
      }

      handleResults(data.providers);
      handleFacets(data.facets);
    }

    function handleResults(list) {

      //$("#result-area").replaceWith('<div id="result-area">' + formatResults(list) + '</div>');

           //var table = $('#result-table');
      
      $('#result-table').html(formatResults(list));
      $('#result-table').show();
      
      if (list.length == page_size) {

        start_index = end_index + 1;
        end_index = start_index + page_size - 1;
      }   
    }

    function formatResults(list) {
      
      var output = "<thead>" 
      + "<th width='20%'>Name</th><th width='10%'>At</th><th width='15%'>Location</th><th width='60%'>Procedure</th>" 
      + "</thead><tbody>";         

      for ( var i in list) {
        var officeOrFacility = "Facility";
        
        //alert(list[i].place_of_service);
        if (list[i].place_of_service.indexOf("O") != -1)
        {
          officeOrFacility = "Office";
        }
        
        output += '<tr class="result">' 
            + '<td>' 
            + toNameCase(list[i].first_name) + " "
            + toNameCase(list[i].last_or_org_name) 
            + '</td>'
            + '<td>'            
            + officeOrFacility 
            + '</td>'
            + '<td>'   
            + toNameCase(list[i].city) + ", " + list[i].state + "  " + list[i].zip 
            + '</td>'
            + '<td>'   
            + list[i].hcpcs_description + 
            + '</td>'
            + '</tr>';
      }
      
      output += "</tbody>";     
      return output;
    }

    function handleFacets(list) {

      if (list.facetType == "ProviderType") {
        $("#curFacet").text("Specialty:");
      } else if (list.facetType == "Zip" || list.facetType == "State") {
        $("#curFacet").text(list.facetType + ":");
      } else {
        $("#curFacet").text(" ");
      }

      $("#facet-area").replaceWith('<div id="facet-area">' + formatFacets(list) + '</div>');
    }

    $(document).on('click', '#search_button', function() {
      resetVars();
      state = $('#stateSelect').val();
      facet_type = "Zip";
      searchRequest();
    });

    function formatFacets(list) {
      //alert(JSON.stringify(list));
      var output = "";
      for ( var i in list.facetedCount) {
        output += '<div><span class="facet">' + list.facetedCount[i].propertyValue
            + '</span><span> ( ' + list.facetedCount[i].propertyCount + ' )</span></div>';
      }
      return output;
    }

    function toNameCase(str) {
      return str.replace(/\w\S*/g, function(txt) {
        return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
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