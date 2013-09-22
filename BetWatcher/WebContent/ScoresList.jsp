<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<style>
table {
	border-style: solid;
	border-width: 1px;
	width: 100%;
	height: 100%;
}

th {
	color: white;
	background-color: #00923F;
}

td,th {
	font-size: 25px;
	height: 30px;
}

tbody {
	color: black;
	border-width: 1px;
}

.I_I {
	background-color: #FFC5C0;
}

.I_II {
	background-color: #FB6F5D;
}

.I_III {
	background-color: #D71500;
}

.II_I {
	color: white;
	background-color: #86E0FB;
}

.II_II {
	color: white;
	background-color: #34AED3;
}

.II_III {
	color: white;
	background-color: #253370;
}
</style>

<script src="http://code.jquery.com/jquery-latest.min.js"></script>
<script type="text/javascript">

	var maxId = 0;

	function updateStructure(responseJson) {
		if (responseJson != "") {
			responseJson = responseJson.slice(1, -1);
			var scores = responseJson.split(", ");
			$.each(scores, function(index, item) {
				var fields = item.split(" | ");
				maxId = fields[0];
				if (fields[2] == "Polska") {
					trclass = "I";
				} else {
					trclass = "II";
				}
				if (fields[3] == "1") {
					trclass = trclass + "_I";
				} else if (fields[3] == "2") {
					trclass = trclass + "_II";
				} else {
					trclass = trclass + "_III";
				}
				var $tr = $('<tr class="' + trclass + '">').prependTo(
						$('#scores'));
				$('<td>').text(fields[1]).appendTo($tr);
				$('<td>').text(fields[2]).appendTo($tr);
				$('<td>').text(fields[3]).appendTo($tr);
			});
		}
	}

	function updateScores(id) {
		$.get('ScoresServlet', {
			id : id,
			market : "Polska vs. Australia"
		}, function(responseJson) {
			updateStructure(responseJson);
			setTimeout(function() {
				updateScores(maxId);
			}, 100);
		}).fail(function() {
			alert("error");
		});
	}

	$(document).ready(function() {
		updateScores(maxId);
	});
</script>


<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<div id="console"></div>
	<table>
		<thead>
			<tr>
				<th>Date</th>
				<th>Team</th>
				<th>Score</th>
			</tr>
		</thead>
		<tbody id="scores">
		</tbody>
	</table>
</body>
</html>