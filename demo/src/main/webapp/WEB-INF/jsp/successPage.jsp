<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sql" uri="http://java.sun.com/jsp/jstl/sql"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Health Food Store</title>
</head>
<link href=static/css/journal/bootstrap.min.css rel="stylesheet">
<body>

	<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
		<a class="navbar-brand" href="/successPage">Health Food Store</a>
		<button class="navbar-toggler" type="button" data-toggle="collapse"
			data-target="#navbarColor01" aria-controls="navbarColor01"
			aria-expanded="false" aria-label="Toggle navigation">
			<span class="navbar-toggler-icon"></span>
		</button>

		<div class="collapse navbar-collapse" id="navbarColor01">
			<ul class="navbar-nav mr-auto">
				<li class="nav-item"><a class="nav-link" href="searchProducts">Search
						Products</a></li>
				<li class="nav-item"><a class="nav-link" href="myCart">My
						Cart</a></li>
			</ul>
			<ul class="navbar-nav ml-auto">
				<li class="nav-item"><a class="nav-link" href="/logout">Logout</a></li>
			</ul>
		</div>
	</nav>

	<div class="container text-center">
		<br>
		<h3>Welcome ${sessionScope.customer.firstName }</h3>
		<hr>
	</div>

	<div class="container text-center">
		<h2>Full list of Products</h2>
	</div>


	<sql:setDataSource var="con" driver="com.mysql.jdbc.Driver"
		url="jdbc:mysql://localhost:3306/storeDb" user="root" password="root" />
	<sql:query dataSource="${con }" sql="select * from stock_item"
		var="stock" />

	<table>
		<c:forEach var="stockItem" items="${stock.rows}" varStatus="status">
			<c:if test="${not status.first and status.index % 4 == 0}">
				<tr>
			</c:if>
			<td width="100">&nbsp</td>
			<td width="200"><img src="images/${stockItem.image}"
				height="200" width="180" /><br> <c:out
					value="${stockItem.title}" /><br> Manufacturer: <c:out
					value="${stockItem.manufacturer}" /><br> Category: <c:out
					value="${stockItem.category}" /><br> Price: €<c:out
					value="${stockItem.price}" /><br> <c:choose>
					<c:when test="${!stockItem.state}">Out of Stock</c:when>
					<c:when test="${stockItem.state}">
						<form class="form-horizontal" method="post" action="addToCart">
							<div class="form-group">
								<input type="hidden" name="itemId" value="${stockItem.item_id}" />
								<input type="submit" class="btn btn-primary btn-sm"
									value="Add To Cart" />
							</div>
						</form>
					</c:when>
				</c:choose> <br> Add a Review:
				<form class="form-horizontal" method="post" action="addReview">
						<input type="hidden" name="itemId" value="${stockItem.item_id}" />
						<input type="text" class="form-control col-md-11" name="comment" value="${Reviews.comment }"
							placeholder="Comment" /> 
						<input type="text" class="form-control col-md-11" name="rating" autocomplete="off"
							placeholder="Rating out of 5" list="rate" />
						<datalist id="rate">
							<option value="1">
							<option value="2">
							<option value="3">
							<option value="4">
							<option value="5">
						</datalist>
						<input type="submit" class="btn btn-primary btn-sm"
							value="Add Review" />
					</div>
				</form> <br></td>
			<td width="100">&nbsp</td>
			<c:if test="${status.first and status.index % 4 == 4 or status.last}">
				</tr>
			</c:if>
		</c:forEach>
	</table>

	<script src="static/js/jquery.min.js"></script>
	<script src="static/js/bootstrap.js"></script>
	<script src="static/js/npm.js"></script>
</body>
</html>
