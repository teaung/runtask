package com.byd5.ats.service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.byd5.ats.message.AppDataStationTiming;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Demo1 {
	public static void main(String[] args) throws ParseException, JsonProcessingException  {
		AppDataStationTiming AppDataStationTiming = new AppDataStationTiming();
		AppDataStationTiming.setStation_id(9);
		Map<String, Object> map=new HashMap<String, Object>();
		System.out.println("---包含信息----"+map.size());
		
	    map.put("aa", AppDataStationTiming); 
	    ObjectMapper mapper = new ObjectMapper();
	    System.out.println(map.size()+"---"+mapper.writeValueAsString(map));
	    
	    List<AppDataStationTiming> list = new ArrayList<AppDataStationTiming>();
	    list.add(AppDataStationTiming);
	    if(list.contains(9)){
	    	System.out.println("---包含信息----");
	    }
	}
} 
		


