<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Margins</title>

<style type="text/css">
table {
	width: 100%;
	height: 100%;
}

td,th {
	border-style: solid;
	border-width: 1px;
}
</style>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script>
	function clearData() {
		$('#pricesTable').empty();
	}

	function displayDataWithJSON(responseJSON) {
		$.each(responseJSON, function(index, item) {
			var $tr = $('<tr>').appendTo($('#pricesTable'));
			$('<td>').text(item['menuPath']).appendTo($tr);
			$('<td>').text(item['timestamp']).appendTo($tr);
			$('<td>').text(item['marginToBack']).appendTo($tr);
			$('<td>').text(item['marginToLay']).appendTo($tr);
		});
	}

	function updateDataWithMargin(margin) {

		$.get('MarginListServlet', {
			margin : margin
		}, function(responseJSON) {
			displayDataWithJSON(responseJSON);
		}).fail(function(xhr, err) {
			alert("error: " + xhr.status);
		});
	}
	
	function getParameterByName(name) {
	    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
	        results = regex.exec(location.search);
	    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
	}

	$(document).ready(function() {
		margin = getParameterByName("margin");
		updateDataWithMargin(margin);
		$('#marginValue').change(function(event) {
			clearData();
			var $margin = $("#marginValue").val();
			updateDataWithMargin($margin);
		});
	});
</script>
</head>
<body>

	<input type="number" id="marginValue" min="0" step="any"
		value="<%=request.getParameter("margin")%>" />

	<table>
		<thead>
			<tr bgcolor="#CCCCCC">
				<th>Menu Path</th>
				<th>Timestamp</th>
				<th>Margin to back</th>
				<th>Margin to lay</th>
			</tr>
		</thead>
		<tbody id="pricesTable">
		</tbody>
	</table>

</body>
</html>