<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

<style>
* {
	font-size: 100px;
}

html,body {
	height: 100%;
}

table {
	border-style: solid;
	border-width: 1px;
	width: 100%;
	height: 100%;
}

td {
	text-align: center;
	border-style: solid;
	border-width: 1px;
	border-style: solid;
}

.team {
	background-image: -webkit-gradient(linear, left top, right bottom, color-stop(0, #00FFFF),
		color-stop(1, #00A3EF));
	color: white;
	height: 10%
}

.score {
	height: 40%;
}

.score td {
	width: 33%;
}

.score td a {
	width: 100%;
	height: 100%;
	display: block;
	vertical-align: middle;
	text-align: center;
}
</style>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript">
	var lastDate;
	var diff = 0;

	$(document).ready(function() {
		$(".score td a").click(function(event) {
			id = $(event.target).attr("id");
			team1 = $('#team1').text();
			team2 = $('#team2').text();
			market = team1 + " vs. " + team2;
			lastDate = new Date();
			$.post('ScoresServlet', {
				score : id,
				market : market,
				diff : diff
			}, function(responseJson) {
				newDate = new Date();
				diff = newDate - lastDate;
			}).fail(function() {
			    alert("error");    
			});
		});
	});
</script>
</head>
<body>
	<table>
		<tbody>
			<tr class="team">
				<th colspan="3" id="team1">Polska</th>
			</tr>
			<tr class="score">
				<td><a href="#" id="1_1">+</a></td>
				<td><a href="#" id="1_2">++</a></td>
				<td><a href="#" id="1_3">+++</a></td>
			</tr>
			<tr class="team">
				<th colspan="3" id="team2">Australia</th>
			</tr>
			<tr class="score">
				<td><a href="#" id="2_1">+</a></td>
				<td><a href="#" id="2_2">++</a></td>
				<td><a href="#" id="2_3">+++</a></td>
			</tr>
		</tbody>
	</table>
</body>
</html>