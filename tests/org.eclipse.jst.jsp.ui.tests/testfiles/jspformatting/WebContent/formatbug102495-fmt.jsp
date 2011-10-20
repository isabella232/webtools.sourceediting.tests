<%-- jsp declaration for String pal should stay on same line. All others should move to new line --%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%><%!String pal = "friend";%>
<%!String message = "friend";%>
<%!String buddy = "friend";%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<%-- jsp expression must be followed directly by ! because no whitespace exists between --%>
	Welcome to WTP,
	<%=message%>!
	<p>
		Welcome to WTP,
		<%=pal%>!
	</p>
	<%-- jsp expression should be treated like any other attribute --%>
	<table <%="align='left'"%>>
		<tr>
			<%-- no whitespace exists before/after jsp tag so keep it that way --%>
			<th>Column<jsp:setProperty name="myname" property="myproperty" />Heading</th>
			<%-- whitespace exists before/after jsp tag so newlines are okay --%>
			<th>Column <jsp:setProperty name="myname" property="myproperty" />
				Heading</th>
		</tr>
		<tr>
			<td>Row 1: Col 1</td>
			<td>Row 1: Col 2</td>
		</tr>
	</table>
</body>
</html>