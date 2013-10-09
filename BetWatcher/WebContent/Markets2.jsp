<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>All markets for today</title>


<style type="text/css">
table {
	border-collapse: collapse;
}

td {
	border: solid 1px;
}

.active {
	background-color: #7f7;
}

.inactive {
	background-color: #f77;
}
</style>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript">
	function updateStructure(responseJson) {
		if (responseJson != "") {
			$("#markets").empty();
			$.each(responseJson, function(index, item) {
				category = item["category"];
				if (category == "Tennis") {
					statusClass = (item["status"] == "ACTIVE") ? "active"
							: "inactive";
					var $tr = $('<tr class="' + statusClass + '">').appendTo(
							$('#markets'));
					$('<td>').text(category).appendTo($tr);
					$('<td>').text(item["name"]).appendTo($tr);
					$('<td>').text(item["createDate"]).appendTo($tr);
					$(
							'<td><a href="Market?id=' + item["bf_marketId"]
									+ '">' + item["bf_marketId"] + '</a>')
							.appendTo($tr);
				}
			});
		}
	}

	function updateMarkets() {
		$.get('MarketsServlet', function(responseJson) {
			$('#status').text("updateMarkets: " + new Date());
			updateStructure(responseJson);
			setTimeout(function() {
				updateMarkets();
			}, 10000);
		}).fail(function(xhr, status, error) {
			alert(xhr.responseText);
		});
	}

	$(document).ready(function() {
		updateMarkets();
	});
</script>

</head>
<body>
	<a id="status"></a>
	<table width="100%">
		<thead>
			<tr>
				<th>Category</th>
				<th>Market</th>
				<th>Date start</th>
				<th>Go</th>
			</tr>
		</thead>
		<tbody id="markets">
		</tbody>
	</table>
</body>
</html>