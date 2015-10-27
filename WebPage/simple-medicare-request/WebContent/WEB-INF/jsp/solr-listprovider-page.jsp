<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
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