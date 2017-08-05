package com.byd5.ats;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;
import com.byd5.ats.message.AppDataDwellTimeCommand;
import com.byd5.ats.message.TrainRunTask;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ApplicationTrainDepart.class)
public class ProtocolFrameTest2 {

	@Autowired
	private RestTemplate restTemplate;
	
	@Test
	public void TestFrameCIStatus() {
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		AppDataDwellTimeCommand AppDataDwellTimeCommand = new AppDataDwellTimeCommand();
		AppDataDwellTimeCommand.setRuntaskCmdType((short) 114);
		AppDataDwellTimeCommand.setPlatformId(1);
		AppDataDwellTimeCommand.setTime(10);
		AppDataDwellTimeCommand.setSetWay(0);

		/*try {
			String json = objMapper.writeValueAsString(AppDataDwellTimeCommand);
			String resultMsg = restTemplate.getForObject("http://serv39-trainruntask/client?json={json}", String.class, json);
			
			System.out.println("-------------------"+resultMsg);
			
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		String resultMsg = restTemplate.getForObject("http://serv31-trainrungraph/server/getRuntask?groupnum={carNum}&tablenum={tablenum}&trainnum={trainnum}", String.class, 103, 1, 101);
		System.out.println("---getRuntask---"+resultMsg);
	}
	
	
}