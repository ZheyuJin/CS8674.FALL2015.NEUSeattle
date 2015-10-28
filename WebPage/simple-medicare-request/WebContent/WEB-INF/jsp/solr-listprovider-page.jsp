<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Get Busiest Doctors in a state for a procedure</title>
<link href="/simple-medicare-request/favicon.ico" rel="icon" type="image/x-icon">

</head>
<body>
	<h2>Query results:</h2>

	<c:if test="${not empty providerlist}">

		<ul>
			<c:forEach var="listValue" items="${providerlist}">
				<li>${listValue}</li>
			</c:forEach>
		</ul>

	</c:if>

	<a href="/simple-medicare-request/index.html">Home</a>

</body>
</html>