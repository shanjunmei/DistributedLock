<%@page import="java.util.Set"%>
<%@page import="redis.clients.jedis.JedisSentinelPool"%>
<%@page import="redis.clients.util.Pool"%>
<%@page import="org.apache.commons.lang3.StringUtils"%>
<%@page import="redis.clients.jedis.Jedis"%>
<%@page import="com.ffzx.commerce.framework.utils.SpringContextHolder"%>
<%@page import="redis.clients.jedis.JedisPool"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<!DOCTYPE html >
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>key remove</title>
<style type="text/css">
body {
	text-align: center;
	background: black;
	color: white;
}
</style>
</head>
<body>
	<%
		Logger logger = LoggerFactory.getLogger("LockManager");
		String key = request.getParameter("key");
		if (StringUtils.isNotBlank(key)) {
			Pool<Jedis> pool = SpringContextHolder.getBean(JedisSentinelPool.class);
			Jedis jedis = pool.getResource();
			try{
				Set<String> keys = jedis.keys(key);
				
				if (keys != null) {
					long t=System.currentTimeMillis();
					for (String i : keys) {
						jedis.del(i);
					}
					t=System.currentTimeMillis()-t;
					logger.info("remove "+keys.size()+" key take "+ t+" ms");
					out.println("remove "+keys.size()+" key take "+ t+" ms");
					
				}
			}catch(Exception e){
				logger.info(key +"remove fail");
				out.println(key +"remove fail");
			}
			jedis.close();
			logger.info(key + " remove success");
			out.println(key + " remove success");
		}
	%>
</body>
</html>