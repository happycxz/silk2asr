package com.happycxz.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.FastDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {
	static ObjectMapper mapper = new ObjectMapper();
	
	// log level :
	// 0 : error
	// 1 : warning
	// 2 : info
	// 3 : debug
	private static int logLevel;
	
	static {
		updateLogLevel();
	}
	
	public static void setLogLevel(int newLevel) {
		Util.logLevel = newLevel;
	}
	
	public static int updateLogLevel() {
		int ret = 3;
		try {
			ret = Integer.valueOf(Configuration.getInstance().getValue("log.level", "3"));
		} catch (Exception e) {
			Util.w("Util.updateLogLevel() exception!", e);
		}
		
		return Util.logLevel = ret;
	}
	
	//////////////////////////////////////////////////////////////////
	// logger
	//////////////////////////////////////////////////////////////////
	public static void d(Object dst) {
		if (logLevel < 3) {
			return;
		}
		log("debug", dst, null);
	}
	public static void debugFormat(String format, Object ... args) {
		if (logLevel < 3) {
			return;
		}
		System.out.format(logHead("debug") + format, args);
	}
	public static void p(Object dst) {
		if (logLevel < 2) {
			return;
		}
		log("info", dst, null);
	}
	public static void w(String dst, Throwable e) {
		if (logLevel < 1) {
			return;
		}
		log("warning", dst, e);
	}
	public static void e(String dst, Throwable e) {
		if (logLevel < 0) {
			return;
		}
		log("error", dst, e);
	}
	private static void log(String level, Object dst, Throwable e) {
		System.out.println(logHead(level) + dst);
		if (e != null) {
			System.out.println(logHead(level) + "exception message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	private static String logHead(String level) {
		return "[" + getFullTimeStr() +"][" + Thread.currentThread().getName() + "][" + level + "]: ";
	}
	
	
	/**
	 * 
	 * @param cmd 带一个%s的字符串，被替换完后即可执行cmd中的脚本或命令
	 * @param sourceFile 替换cmd中的%s部分
	 * @return
	 */
	public static String RunShell2Wav(String cmd, String sourceFile) {
		String realCmd = String.format(cmd, sourceFile);
		Util.p("RunShell2Wav() run shell:" + realCmd); 
		return execShell(realCmd);
	}

	
	/**
	 * exec linux shell command
	 * @param cmd
	 * @return
	 */
	private static String execShell(String cmd) {
		String result = "";
		try {
			Process ps = Runtime.getRuntime().exec(cmd);
			ps.waitFor();

			BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line).append("\n");
			}
			result = sb.toString();
			Util.d(result);
		} catch (Exception e) {
			Util.w("Util.execShell exception", e);
		}
		return result;
	}
	
	public static String JsonResult(String status, String msg) {
		return JsonResult(status, msg, "{}");
	}
	
	public static String JsonResult(String status, String msg, String json) {
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("status", status);
		ret.put("msg", msg);
		ret.put("result", json);
		
		String retStr = "";
		try {
			retStr = mapper.writeValueAsString(ret);
		} catch (JsonProcessingException e) {
			p("JsonResult() JsonProcessingException on parsing:" + ret);
			e.printStackTrace();
		}
		
		p("JsonResult() result:" + retStr);
		
		return retStr;
	}
	
	/**
	 * 根据当前时间，获取毫秒数
	 * @return
	 */
	public static long getCurrentMilliSecond() {
		Calendar c1 = Calendar.getInstance();
        return c1.getTimeInMillis();
	}
	
	/**
	 * 根据当前日期，获取yyyy.MM格式字符串
	 * @return
	 */
	public static String getDateStr() {
		Calendar c1 = Calendar.getInstance();
        String logdate = FastDateFormat.getInstance("yyyy.MM.dd").format(c1.getTime());
        return logdate;
	}
	
	/**
	 * 根据当前日期，获取yyyy-MM-dd HH:mm:ss.SSS格式字符串
	 * @return
	 */
	public static String getFullTimeStr() {
		Calendar c1 = Calendar.getInstance();
        String logdate = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS").format(c1.getTime());
        return logdate;
	}
	
	
	/**
	 * 获取工程根目录路径
	 * @return
	 */
	public static String getRootPath() {
		String path = Util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (path.startsWith("/home")) {
			
		} else if (path.startsWith("file:") || path.startsWith("/")) {
			if (path.startsWith("file:"))
				path = path.substring(5);
			else if (path.startsWith("/"))
				path = path.substring(1);
		}
		
		return path;
	}
	
    public static void main(String[] args) {
    	
    }
}