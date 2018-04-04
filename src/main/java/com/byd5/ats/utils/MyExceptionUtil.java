package com.byd5.ats.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

public class MyExceptionUtil {


	/**
	 * 将捕获的异常详细信息输出到日志文件中
	 * @param e 捕获到的异常
	 * @return 异常信息字符串
	 */
	public static String printTrace2logger(Exception e) {
		Logger logger = Logger.getLogger("MyExceptionUtil");
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		e.printStackTrace(writer);
		StringBuffer buffer = stringWriter.getBuffer();
		logger.error(buffer.toString());
		return buffer.toString();
	}
	
}
