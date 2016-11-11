
<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>WebSearch</title>
	</head>

	<body>
		</br></br></br></br>
		<form method = "POST" action = "result.jsp">
		<img src="pic.png" width="180" height="150" >
		<%! Date date = new Date(); %> 
		<p align = "center"><font size = "12" color = "blue">WebSearch</font></p>
		<P align = "center"><font color = #FF5809><%=date.toString()%></font>
		<p align = "center">
			<font size = "12">	
			<input type = "text" name = "User" style = "width:400px;height:40px">&nbsp;
			<input type = "submit" value = "Search" style = "width:80px;height:40px">
			</font>
		</p>
		</form>
	</body>
</html>