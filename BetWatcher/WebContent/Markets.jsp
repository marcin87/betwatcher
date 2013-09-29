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
	function manageSelection($selected) {
		if ($selected.text() == 'PLAYING') {
			$selected.text('PLAY');
			return false;
		} else {
			$selected.text("PLAYING");
			return true;
		}
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
								$('<td>')
										.text(
												item["category"] + " > "
														+ item["name"])
										.appendTo($tr);
								$('<td>').text(item["createDate"])
										.appendTo($tr);
								if (statusClass == "active") {
									$td = $('<td><a>ACTIVE</a>').appendTo($tr);
								} else {
									$td = $('<td>').appendTo($tr);
									$(
											'<button class="activate" id="' + item["bf_marketId"] + '">ACTIVATE</button>')
											.appendTo($td);
								}
								if (item["useToPlay"] == "YES") {
									$td = $('<td>').appendTo($tr);
									$(
											'<button class="play" id="' + item["bf_marketId"] + '">PLAYING</button>')
											.appendTo($td);
								} else {
									$(
											'<td><button class="play" id="' + item["bf_marketId"] + '">PLAY</button>')
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
			}, 5000);
		}).fail(function(xhr, status, error) {
			alert(xhr.responseText);
		});
	}

	function handlePrices(id) {
		$
				.get(
						'MarginListServlet',
						{
							bf_marketId : id,
							margin : 0.01
						},
						function(responseJson) {
							if ($('#' + id).text() == 'PLAYING') {
								$('#status')
										.text('handlePrices: ' + new Date());
								string = '';
								$
										.each(
												responseJson,
												function(index, item) {
													string = string
															+ '<' + item["timestamp"] + ' | ' + item["marginToLay"] + ' | ' + item["marginToLay"] + '>\n';
												});
								$('#response').text('handlePrices: ' + string);

								setTimeout(function() {
									handlePrices(id);
								}, 3000);
							}
						}).fail(function(xhr, status, error) {
					alert(xhr.responseText);
				});
	}

	$(document).ready(function() {

		updateMarkets();

		$("#runProcess").click(function(event) {
			$.post('MarketsServlet', {
				action : 'runProcess'
			}, function() {
			}).fail(function(xhr, status, error) {
				alert(xhr.responseText);
			});
		});

		$('body').on('click', 'button.play', function() {
			useToPlay = manageSelection($(this));
			id = event.target.id;
			$.post('MarketsServlet', {
				action : "useToPlay",
				id : id,
				useToPlay : useToPlay
			}, function(responseJson) {
			}).fail(function(xhr, status, error) {
				alert(xhr.responseText);
			});

			if (useToPlay) {
				handlePrices(id);
			}
		});
		

		$('body').on('click', 'button.activate', function() {
			$.post('MarketsServlet', {
				action : "activate",
				id : id
			}, function(responseJson) {
			}).fail(function(xhr, status, error) {
				alert(xhr.responseText);
			});
		});
	});
</script>
</head>
<body>
	<div style="width: 100%">
		<button id="runProcess">RUN PROCESS</button>
		<a id="status"></a>
		<table width="100%">
			<tbody id="markets">
			</tbody>
		</table>
		<a id="response"></a>
	</div>
</body>
</html>