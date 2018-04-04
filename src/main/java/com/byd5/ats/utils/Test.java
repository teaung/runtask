package com.byd5.ats.utils;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Test {

	public static void main(String[] args){
		Logger logger = Logger.getLogger("Test");
		char[] code = {' ', ' ', ' ', ' '};
		int len = 4;
		char[] dst = "1".toCharArray();
		if (dst.length < len) {
			len = dst.length;
		}
		for (int i = 0; i < len; i ++) {
			code[3-i] = dst[len-1-i];
		}
		ObjectMapper mapper = new ObjectMapper(); // 转换器
		try {
			String json = mapper.writeValueAsString(code);
			System.out.println(json);
			System.out.println("---"+Integer.parseInt(json));
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*try{
			Integer a = 0;
			if(a == 0){
				throw new Exception("MyException---未找到当日计划表号为:1对应的车次信息");
			}
			System.out.println("--------"+a);
			
		}catch (Exception e) {
			// TODO: handle exception
			String msg = MyExceptionUtil.printTrace2logger(e);
			//logger.error(msg);
		}*/
	}
}
