<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Provider results</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">
<link
	href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css"
	rel="stylesheet">
	<link href="/simple-medicare-request/favicon.ico" rel="icon"
	type="image/x-icon">
</head>
<body class="container main">
	<h2>Query results:</h2>

	<c:if test="${not empty providerlist}">

		<div class="list-group">
			<ul >
				<c:forEach var="listValue" items="${providerlist}">
					<li class="list-group-item">${listValue}</li>
				</c:forEach>
			</ul>
		</div>

	</c:if>

	<a href="/simple-medicare-request/index.html">Home</a>

</body>
</html>