<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.HashMap"%>
<%@ page import="pl.betwatcher.database.DataManager"%>
<%@ page import="pl.betwatcher.bet365.Bet365Market"%>
<%@ page import="pl.betwatcher.betfair.BetFairMarket"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%
	String bf_marketId = request.getParameter("id");

	BetFairMarket bfMarket = DataManager.sharedInstance()
	.getMarketWithId(bf_marketId);

	Bet365Market market = DataManager.sharedInstance()
	.getB365MarketWithBFMarketId(bf_marketId);
	
	HashMap<Integer, String> runners = new HashMap<Integer, String>();
	runners.put(bfMarket.runner1Id, bfMarket.runner1Name);
	runners.put(bfMarket.runner2Id, bfMarket.runner2Name);
%>

<title><%=market.name%></title>

<style type="text/css">
table {
	border-collapse: collapse;
}

.margin {
	background-color: #ddd;
}

.active {
	background-color: white;
}

.inactive {
	background-color: red;
}

td {
	border: solid 1px;
}
</style>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript">
	
	var runners = {
		<%=bfMarket.runner1Id%>:'<%=runners.get(bfMarket.runner1Id)%>', 
		<%=bfMarket.runner2Id%>:'<%=runners.get(bfMarket.runner2Id)%>'
	};
	

	function updateMarket(responseJson) {
		active = responseJson["status"]=="ACTIVE";
		$("body").attr('class', active?"active":"inactive");
		$("#active").text(active?"SET INACTIVE" : "SET ACTIVE");
		$("#useToPlay").text(responseJson["useToPlay"]?"STOP" : "PLAY");
	}

	function updateStructure(responseJson) {
		if (responseJson != "") {
			$("#prices").empty();
			$.each(responseJson, function(index, item) {
				var $tr = $('<tr>').appendTo($('#prices'));
				$('<td>').text(runners[item["runnerId"]]).appendTo($tr);
				$('<td>').text(item["timestamp"]).appendTo($tr);
				$('<td>').text(item["b365_odd"]).appendTo($tr);
				$('<td>').text(item["priceToBack"]).appendTo($tr);
				$('<td>').text(item["amountToBack"]).appendTo($tr);
				$('<td class="margin">').text(item["marginToBack"]).appendTo($tr);
				$('<td>').text(item["priceToLay"]).appendTo($tr);
				$('<td>').text(item["amountToLay"]).appendTo($tr);
				$('<td class="margin">').text(item["marginToLay"]).appendTo($tr);
			});
		}
	}

	function updatePrices(id) {
		var $margin = $("#marginValue").val();
		$margin = $margin==""?0.1:$margin;
		$.get('MarginListServlet', {
			bf_marketId : id,
			margin : $margin
		}, function(responseJson) {
			$('#status').text(new Date());
			updateStructure(responseJson);
			setTimeout(function() {
				updatePrices(id);
			}, 200);
		}).fail(function(xhr, status, error) {
			alert(xhr.responseText);
		});
		
		$.get('MarketsServlet', {
			bf_marketId : id,
		}, function(responseJson) {
			updateMarket(responseJson);
		}).fail(function(xhr, status, error) {
			alert(xhr.responseText);
		});
	}

	$(document).ready(function() {
		updatePrices(<%=bfMarket.id%>);
		
		$("#active").click(function(event) {
			status = $(event.target).text();
			activate = true;
			if (status=="SET ACTIVE") {
				$(event.target).text("...");
			} else {
				activate = false;
				$(event.target).text("...");
			}
			$.post('MarketsServlet', {
				action : "activate",
				id : "<%=market.id%>",
				activate : activate
			}, function(responseJson) {
			}).fail(function(xhr, status, error) {
				alert(xhr.responseText);
			});
		});

		$("#useToPlay").click(function(event) {
			useToPlayString = $(event.target).text();
			useToPlay = true;
			if (useToPlayString=="PLAY") {
				$(event.target).text("...");
			} else {
				useToPlay = false;
				$(event.target).text("...");
			}
			$.post('MarketsServlet', {
				action : "useToPlay",
				id : "<%=bfMarket.id%>",
				useToPlay : useToPlay
			}, function(responseJson) {
				$.post('MarketsServlet', {
					action : 'runProcess'
				}, function() {
				}).fail(function(xhr, status, error) {
					alert(xhr.responseText);
				});
			}).fail(function(xhr, status, error) {
				alert(xhr.responseText);
			});
		});
	});
</script>
</head>
<body>
	<p>
		<a><%=market.name%></a> <a id="status"></a>
	</p>
	<p>
		<button id="active"><%=market.status.equals("ACTIVE") ? "SET INACTIVE"
					: "SET ACTIVE"%></button>
		<button id="useToPlay"><%=market.useToPlay ? "STOP" : "PLAY"%></button>
		<input type="number" id="marginValue" min="0" step="any" value="0.3" />
	</p>
	<table>
		<thead>
			<tr>
				<th>Runner</th>
				<th>Timestamp</th>
				<th>Bet365 Odd</th>
				<th>Price to back</th>
				<th>Amount to back</th>
				<th>Margin to back</th>
				<th>Price to lay</th>
				<th>Amount to lay</th>
				<th>Margin to lay</th>
			</tr>
		</thead>
		<tbody id="prices">
		</tbody>
	</table>
</body>
</html>