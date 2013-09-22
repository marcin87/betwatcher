<%@page import="org.apache.jasper.tagplugins.jstl.core.ForEach"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="pl.betwatcher.database.DataManager"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Markets</title>

<style type="text/css">
td {
	border: solid 1px;
}

.active {
	background-color: green;
}

.inactive {
	background-color: red;
}
</style>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript">
	var $lastSelected = null;

	function manageSelection($selected) {
		if ($lastSelected != null) {
			$lastSelected.text("PLAY");
			if ($lastSelected.attr("id") == $selected.attr("id")) {
				$lastSelected = null;
				return false;
			}
		}
		$selected.text("PLAYING");
		$lastSelected = $selected;
		return true;
	}

	function updateStructure(responseJson) {
		if (responseJson != "") {
			$("#markets").empty();
			$
					.each(
							responseJson,
							function(index, item) {
								statusClass = (item["status"] == "ACTIVE") ? "active"
										: "inactive";
								var $tr = $('<tr class="' + statusClass + '">')
										.appendTo($('#markets'));
								$('<td>').text(item["name"]).appendTo($tr);
								$('<td>').text(item["createDate"])
										.appendTo($tr);
								$('<td>').text(item["algorithm"]).appendTo($tr);
								if (statusClass == "active") {
									if (item["useToPlay"] == "YES") {
										$td = $('<td>').appendTo($tr);
										$lastSelected = $(
												'<button class="play" id="' + item["bf_marketId"] + '">PLAYING</button>')
												.appendTo($td);
									} else {
										$(
												'<td><button class="play" id="' + item["bf_marketId"] + '">PLAY</button>')
												.appendTo($tr);
									}
								} else {
									$('<td>').appendTo($tr);
								}
							});
		}
	}

	function updateMarkets() {
		$.get('MarketsServlet', function(responseJson) {
			updateStructure(responseJson);
			setTimeout(function() {
				updateMarkets();
			}, 5000);
		}).fail(function() {
			alert("error");
		});
	}

	$(document).ready(function() {

		updateMarkets();

		$('body').on('click', 'button.play', function() {
			/* if ($lastSelected != null) {
				id = $lastSelected.attr("id");
				$.post('MarketsServlet', {
					id : id,
					useToPlay : false
				}, function(responseJson) {
				}).fail(function() {
					alert("error");
				});
			} */
			useToPlay = manageSelection($(this));
			id = event.target.id;
			$.post('MarketsServlet', {
				id : id,
				useToPlay : useToPlay
			}, function(responseJson) {
			}).fail(function() {
				alert("error");
			});
		});
	});
</script>
</head>
<body>
	<table>
		<thead>
			<tr>
				<th>Name</th>
				<th>Create date</th>
				<th>Algorithm</th>
				<th>Action</th>
			</tr>
		</thead>
		<tbody id="markets">
		</tbody>
	</table>

</body>
</html>