<%@page import="redis.clients.jedis.JedisSentinelPool"%>
<%@page import="redis.clients.util.Pool"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.Date"%>
<%@page import="com.lanhun.distributedLock.DateUtils"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Comparator"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.apache.log4j.LogManager"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Set"%>
<%@page import="redis.clients.jedis.Jedis"%>
<%@page import="com.ffzx.commerce.framework.utils.SpringContextHolder"%>
<%@page import="redis.clients.jedis.JedisPool"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Lock Manager</title>
<style type="text/css">
body {
	text-align: center;
	background: black;
	color: white;
}
table {
	border-collapse: collapse;
	border:solid 1px;
	margin: auto;
	border-color: blue; 
}
table td{
	border-collapse: collapse;
	border:solid 1px;
	background: white;
	color: black;
	border-color: blue;
}
table th{
	border-collapse: collapse;
	border:solid 1px;
	border-color: blue;
	background: navy;
}
</style>
</head>
<body>
	<%
		Date date = new Date();
		class ValueComparator implements Comparator<String> {

			Map<String, String> base;

			public ValueComparator(Map<String, String> base) {
				this.base = base;
			}

			// Note: this comparator imposes orderings that are inconsistent with equals.      
			public int compare(String a, String b) {
				if (base.get(a).compareTo(base.get(b)) > 0) {
					return -1;
				} else {
					return 1;
				} // returning 0 would merge keys  
			}
		}
		String prefix=request.getParameter("_prefix");
		if(prefix==null||prefix.trim().length()==0){
			prefix="_lock*";
		}

		Logger logger = LoggerFactory.getLogger("LockManager");
		Pool<Jedis> pool = SpringContextHolder.getBean(JedisSentinelPool.class);
		Jedis jedis = pool.getResource();
		Set<String> keys = jedis.keys(prefix);

		Map<String, String> kvs = new HashMap<String, String>();
		if (keys != null) {
			for (String key : keys) {
				try {
					kvs.put(key, jedis.get(key));
				} catch (Exception e) {
					logger.info("fetch key fail:" + key);
				}
			}
		}
		try {
			jedis.close();
		} catch (Exception e) {
			logger.info("", e);
		}
		ValueComparator vc = new ValueComparator(kvs);
		TreeMap<String, String> sortMap = new TreeMap<String, String>(vc);
		sortMap.putAll(kvs);
		out.println("total is :" + sortMap.size() + "<br/>");
		out.println("current time :" + DateUtils.dateTimeFormat(date) + "<br/>");
		out.println("current time :" + DateUtils.format("yyyyMMddHHmmss", date) + "<br/>");
		out.println("<a href='remove.jsp?key=_lock:*' target='_blank'>remove all lock</a><br/>");
		out.println("<a href='remove.jsp?key=_lock:stock:*' target='_blank'>remove all stock lock</a><br/>");
		out.println("<table>");
		out.println("<tr>");
		out.println("<th>key</th>");
		out.println("<th>value</th>");
		out.println("<th>locked time(ms)</th>");
		out.println("<th>locked times(s)</th>");
		out.println("<th>op</th>");
		out.println("</tr>");
		
		for (Entry<String, String> e : sortMap.entrySet()) {
			out.println("<tr>");
			out.println("<td>" + e.getKey() + "</td>");
			out.println("<td>" + e.getValue() + "</td>");
			String val = e.getValue();
			Long lockTime = null;
			String createTime=null;
			if (val != null) {

				try {
					String t = val.split("_")[0];
					String currentTimeStr = DateUtils.dateTimeFormat(date);
					Date d1 = DateUtils.parse("yyyy-MM-dd HH:mm:ss", currentTimeStr);
					Date d2 = DateUtils.parse("yyyyMMddHHmmss", t);
					createTime=DateUtils.dateTimeFormat(d2);
					lockTime = d1.getTime() - d2.getTime();
				} catch (Exception e2) {
					//ignore;
				}

			}

			if (lockTime != null) {
				out.println("<td>" + createTime + "</td>");
				out.println("<td>" + (lockTime / 1000) + "</td>");
			} else {
				out.println("<td></td>");
				out.println("<td></td>");
			}
			out.println("<td><a href='remove.jsp?key=" + e.getKey() + "' target='_blank'>remove this key</a></td>");
			out.println("</tr>");
		}
		out.println("</table>");
	%>
</body>
</html>