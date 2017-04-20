package com.byd5.ats;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.byd5.ats.protocol.ats_ci.AppDataCIStatus;
import com.byd5.ats.protocol.ats_ci.FrameCIStatus;
import com.byd5.ats.protocol.ats_vobc.FrameATOStatus;
import com.byd5.ats.protocol.ats_vobc.FrameTrainStatus;
import com.byd5.ats.protocol.ats_zc.FrameZCTSRStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/*
CI status data:
{"header_info":{"inface_type":36873,"send_vender":67175425,"receive_vender":50398209,"map_version":1,"map_crc":286331153,"msg_cnum":5948,"comm_cycle":4,"msg_snum_side":0,"msg_cnum_previous_msg":0,"protocol_version":1},"msg_header":{"msg_len":80,"msg_type":514},"ci_status":{"s_status":[193,193,193,196,193,193,193,193,193,193,193,193,193,193],"sw_status":[113],"t_status":[112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,114,112,114,112,114,112,112,112,112,112,112,112,112,98,112],"lt_status":[3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,1,3,3],"r_status":[0,0,4,0,0,0,0,0,0,0,0,0,0],"autopass_status":[0],"d_status":[2,2,2,2,2,2,2,2,2,2,2,2],"esp_status":[1],"keep_train":[7],"autoback_status":[1],"autoback_fully_status":[1],"spks_status":[1],"autotrig_status":[1]},"dev_id":{"s_id":[12289,12290,12291,12292,12293,12294,12295,12296,12297,12298,12299,12300,12301,12302],"sw_id":[20481],"t_id":[4097,4098,4099,4100,4101,4102,4103,4104,4105,4106,4107,4108,4109,4110,4111,4112,4113,4114,4115,4116,4117,4118,4119,4120,4121,4122,4123,4124,4125,4126,4127,4128],"lt_id":[40961,40962,40963,40964,40965,40966,40967,40968,40969,40970,40971,40972,40973,40974,40975,40976,40977,40978,40979,40980,40981,40982,40983,40984,40985,40986,40987,40988,40989,40990,40991,40992,40993,40994,40995,40996,40997,40998,40999,41000,41001,41002,41003,41004,41005,41006,41007,41008,41009,41010,41011,41012,41013,41014,41015,41016,41017,41018,41019,41020,41021,41022,41023,41024,41025,41026,41027,41028,41029,41030,41031,41032,41033,41034,41035,41036,41037,41038,41039,41040,41041,41042,41043,41044,41045,41046,41047,41048,41049,41050,41051,41052,41053],"r_id":[8193,8194,8195,8196,8197,8198,8199,8200,8201,8202,8203,8204,8205],"autopass_id":[0],"d_id":[32769,32770,32771,32772,32773,32774,32775,32776,32777,32778,32779,32780],"esp_id":[24577],"keep_train_id":[0],"autoback_id":[0],"autoback_fully_id":[0],"spks_id":[0],"autotrig_id":[0]}}

ZC status data:
{"zc_header_status":{"inface_type":36868,"send_vender":33620481,"receive_vender":50397953,"map_version":1,"map_crc":286331153,"msg_cnum":36,"comm_cycle":4,"msg_snum_side":0,"msg_cnum_previous_msg":0,"protocol_version":1},"zc_msg_header_sta":{"msg_len":187,"msg_type":518},"zc2ats_sta_tsr":{"tsr_electrify_confirm":85,"logic_track_num":92},"lgc_tsr_sta":[4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4]}

 */

/*
 * @JsonFormat(pattern="yyyy-MM-dd HH-mm-ss") 用于属性，把Date类型直接转化为想要的格式
 * @JsonProperty 用于属性上，把该属性的名称序列化为另外一个名称，如把trueName属性序列化为name， @JsonProperty("name") 
 * 

 * Read + Write Annotations
 * @JsonIgnore 用于属性上，在进行JSON操作时忽略该属性
 * @JsonIgnoreProperties({"firstName", "lastName"})
 * @JsonIgnoreType
 * 
 * 
 * Read Annotations
 * @JsonSetter("id")
 * @JsonAnySetter
 * @JsonCreator
 * @JacksonInject
 * @JsonDeserialize(using=OptimizedBooleanDeserializer.class)
 * 
 * Write Annotations
 * @JsonInclude(JsonInclude.Include.NON_EMPTY)
 * @JsonGetter("id")
 * @JsonAnyGetter
 * @JsonPropertyOrder("name", "personId")
 * @JsonRawValue
 * @JsonValue
 * @JsonSerialize(using=OptimizedBooleanSerializer.class)
 * 
 */


public class ProtocolFrameTest {

/*	public class BeanWithCreator {
		public int id;
		public String name;
		
		@JsonCreator
		public BeanWithCreator(@JsonProperty("id") int id, @JsonProperty("theName") String name) {
			this.id = id;
			this.name = name;
		}
	}
	
	@Test
	public void whenDeserializingUsingJsonCreator_thenCorrect() throws IOException {
		String json = "{\"id\":1, \"theName\":\"My bean\"}";
		
		BeanWithCreator bean = new ObjectMapper()
				.readerFor(BeanWithCreator.class)
				.readValue(json);
		
		assertEquals("My bean", bean.name);
		
	}*/
	
	@Test
	public void TestFrameCIStatus() {
		//{"header_info":{"inface_type":36873,"send_vender":67175425,"receive_vender":50398209,"map_version":1,"map_crc":286331153,"msg_cnum":5948,"comm_cycle":4,"msg_snum_side":0,"msg_cnum_previous_msg":0,"protocol_version":1},"msg_header":{"msg_len":80,"msg_type":514},"ci_status":{"s_status":[193,193,193,196,193,193,193,193,193,193,193,193,193,193],"sw_status":[113],"t_status":[112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,114,112,114,112,114,112,112,112,112,112,112,112,112,98,112],"lt_status":[3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,1,3,3],"r_status":[0,0,4,0,0,0,0,0,0,0,0,0,0],"autopass_status":[0],"d_status":[2,2,2,2,2,2,2,2,2,2,2,2],"esp_status":[1],"keep_train":[7],"autoback_status":[1],"autoback_fully_status":[1],"spks_status":[1],"autotrig_status":[1]},"dev_id":{"s_id":[12289,12290,12291,12292,12293,12294,12295,12296,12297,12298,12299,12300,12301,12302],"sw_id":[20481],"t_id":[4097,4098,4099,4100,4101,4102,4103,4104,4105,4106,4107,4108,4109,4110,4111,4112,4113,4114,4115,4116,4117,4118,4119,4120,4121,4122,4123,4124,4125,4126,4127,4128],"lt_id":[40961,40962,40963,40964,40965,40966,40967,40968,40969,40970,40971,40972,40973,40974,40975,40976,40977,40978,40979,40980,40981,40982,40983,40984,40985,40986,40987,40988,40989,40990,40991,40992,40993,40994,40995,40996,40997,40998,40999,41000,41001,41002,41003,41004,41005,41006,41007,41008,41009,41010,41011,41012,41013,41014,41015,41016,41017,41018,41019,41020,41021,41022,41023,41024,41025,41026,41027,41028,41029,41030,41031,41032,41033,41034,41035,41036,41037,41038,41039,41040,41041,41042,41043,41044,41045,41046,41047,41048,41049,41050,41051,41052,41053],"r_id":[8193,8194,8195,8196,8197,8198,8199,8200,8201,8202,8203,8204,8205],"autopass_id":[0],"d_id":[32769,32770,32771,32772,32773,32774,32775,32776,32777,32778,32779,32780],"esp_id":[24577],"keep_train_id":[0],"autoback_id":[0],"autoback_fully_id":[0],"spks_id":[0],"autotrig_id":[0]}}

		FrameCIStatus frameCIStatus = null;
		String frameCIStatusJson = "{\"header_info\":{\"inface_type\":36873,\"send_vender\":67175425,\"receive_vender\":50398209,\"map_version\":1,\"map_crc\":286331153,\"msg_cnum\":5948,\"comm_cycle\":4,\"msg_snum_side\":0,\"msg_cnum_previous_msg\":0,\"protocol_version\":1},"
						+ "\"msg_header\":{\"msg_len\":80,\"msg_type\":514},"
						+ "\"ci_status\":{"
						+ "\"s_status\":[193,193,193,196,193,193,193,193,193,193,193,193,193,193],"
						+ "\"sw_status\":[113],"
						+ "\"t_status\":[112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,112,114,112,114,112,114,112,112,112,112,112,112,112,112,98,112],"
						+ "\"lt_status\":[3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,1,3,3],"
						+ "\"r_status\":[0,0,4,0,0,0,0,0,0,0,0,0,0],"
						+ "\"autopass_status\":[0],"
						+ "\"d_status\":[2,2,2,2,2,2,2,2,2,2,2,2],"
						+ "\"esp_status\":[1],"
						+ "\"keep_train\":[7],"
						+ "\"autoback_status\":[1],"
						+ "\"autoback_fully_status\":[1],"
						+ "\"spks_status\":[1],"
						+ "\"autotrig_status\":[1]},"
						+ "\"dev_id\":{\"s_id\":[12289,12290,12291,12292,12293,12294,12295,12296,12297,12298,12299,12300,12301,12302],\"sw_id\":[20481],\"t_id\":[4097,4098,4099,4100,4101,4102,4103,4104,4105,4106,4107,4108,4109,4110,4111,4112,4113,4114,4115,4116,4117,4118,4119,4120,4121,4122,4123,4124,4125,4126,4127,4128],\"lt_id\":[40961,40962,40963,40964,40965,40966,40967,40968,40969,40970,40971,40972,40973,40974,40975,40976,40977,40978,40979,40980,40981,40982,40983,40984,40985,40986,40987,40988,40989,40990,40991,40992,40993,40994,40995,40996,40997,40998,40999,41000,41001,41002,41003,41004,41005,41006,41007,41008,41009,41010,41011,41012,41013,41014,41015,41016,41017,41018,41019,41020,41021,41022,41023,41024,41025,41026,41027,41028,41029,41030,41031,41032,41033,41034,41035,41036,41037,41038,41039,41040,41041,41042,41043,41044,41045,41046,41047,41048,41049,41050,41051,41052,41053],\"r_id\":[8193,8194,8195,8196,8197,8198,8199,8200,8201,8202,8203,8204,8205],\"autopass_id\":[0],\"d_id\":[32769,32770,32771,32772,32773,32774,32775,32776,32777,32778,32779,32780],\"esp_id\":[24577],\"keep_train_id\":[0],\"autoback_id\":[0],\"autoback_fully_id\":[0],\"spks_id\":[0],\"autotrig_id\":[0]}"
						+ "}";
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			frameCIStatus = objMapper.readValue(frameCIStatusJson, FrameCIStatus.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Short signal0 = 193;
		Byte switch0 = 113;
		assertEquals(signal0, frameCIStatus.getCiStatus().signalStatus.get(0));
		assertEquals(switch0, frameCIStatus.getCiStatus().switchStatus.get(0));
		
		int interfaceType = 0x9009;
		assertEquals(interfaceType, frameCIStatus.getFrameHeader().interfaceType);
		short msgType = 0x0202;
		assertEquals(msgType, frameCIStatus.getMsgHeader().type);
		
		
		System.out.println("JsonStr: " + frameCIStatusJson);
		System.out.println("==============================================================");
		System.out.println("java Object: " + frameCIStatus);
		
		
		//序列化
		//为了使JSON可读，配置缩进输出；生产环境中不需要这样设置
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//配置mapper忽略空属性
		//objMapper.setSerializationInclusion(Include.NON_EMPTY);
		//默认情况，Jackson使用Java属性字段名称作为Json的属性名称，也可以使用Jackson注解改变Json属性名称
		
		String js = null;
		try {
			//s = objMapper.writeValueAsString(ciStatus);
			js = objMapper.writeValueAsString(frameCIStatus);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(js);
	}
	
	@Test
	public void TestFrameZCTSRStatus() {
		//{"zc_header_status":{"inface_type":36868,"send_vender":33620481,"receive_vender":50397953,"map_version":1,"map_crc":286331153,"msg_cnum":36,"comm_cycle":4,"msg_snum_side":0,"msg_cnum_previous_msg":0,"protocol_version":1},
		//"zc_msg_header_sta":{"msg_len":187,"msg_type":518},
		//"zc2ats_sta_tsr":{"tsr_electrify_confirm":85,"logic_track_num":92},
		//"lgc_tsr_sta":[4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4]}

		FrameZCTSRStatus frame = null;
		String json = "{\"zc_header_status\":{\"inface_type\":36868,\"send_vender\":33620481,\"receive_vender\":50397953,\"map_version\":1,\"map_crc\":286331153,\"msg_cnum\":36,\"comm_cycle\":4,\"msg_snum_side\":0,\"msg_cnum_previous_msg\":0,\"protocol_version\":1},"
						+ "\"zc_msg_header_sta\":{\"msg_len\":187,\"msg_type\":518},"
						+ "\"zc2ats_sta_tsr\":{\"tsr_electrify_confirm\":85,\"logic_track_num\":92},"
						+ "\"lgc_tsr_sta\":[4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4]"
						+ "}";
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			//ciStatus = objMapper.readValue(ciStatusJsonStr, AppDataCIStatus.class);
			frame = objMapper.readValue(json, FrameZCTSRStatus.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int interfaceType = 0x9004;
		assertEquals(interfaceType, frame.getFrameHeader().interfaceType);
		short msgType = 0x0206;
		assertEquals(msgType, frame.getMsgHeader().type);

		short logicNum = 92;
		Short tsrValue0 = 4;
		
		assertEquals(logicNum, frame.getTsrStatus().logicNum);
		assertEquals(tsrValue0, frame.getLogicTSRValue().get(0));
		
		
		
		System.out.println("JsonStr: " + json);
		System.out.println("==============================================================");
		System.out.println("java Object: " + frame);
		
		
		//序列化
		//为了使JSON可读，配置缩进输出；生产环境中不需要这样设置
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//配置mapper忽略空属性
		//objMapper.setSerializationInclusion(Include.NON_EMPTY);
		//默认情况，Jackson使用Java属性字段名称作为Json的属性名称，也可以使用Jackson注解改变Json属性名称
		
		String js = null;
		try {
			js = objMapper.writeValueAsString(frame);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(js);
	}
	
	//Train Status
	//'{"header_info":{"inface_type":36869,"send_vender":16843010,"receive_vender":50397953,"map_version":1,"map_crc":286331153,"msg_cnum":4471,"comm_cycle":2,"msg_snum_side":1,"msg_cnum_previous_msg":1,"protocol_version":1},"msg_header":{"msg_len":58,"msg_type":516},"train_status":{"train_identify_id":1234,"line_num":64,"train_locate_status":85,"direction_train":85,"wheel_steer":0,"t_head_track_id":41003,"t_head_track_offset":492,"t_tail_track_id":40998,"t_tail_track_offset":6592,"excessive_error":0,"owe_error":0,"atp_mode":1,"train_run_mode":1,"train_comp":170,"train_emerg":170,"train_ar_status":170,"train_speed":0,"train_door":170,"park_stab_status":85,"park_ensure_status":255,"turnback_none_sta":0,"pre_choose_mode":8,"stop_emerg_reason":96,"stop_emerg_speed":700,"adv_speed":400,"track_id1":0,"track_offset1":0,"track_id2":0,"track_offset2":0,"reserv":0},"t_stamp":{"sec":1492616485,"usec":792839}}'
	//ATO Status
	//'{"header_info":{"inface_type":36869,"send_vender":16843010,"receive_vender":50397953,"map_version":1,"map_crc":286331153,"msg_cnum":4472,"comm_cycle":2,"msg_snum_side":1,"msg_cnum_previous_msg":1,"protocol_version":1},"msg_header":{"msg_len":33,"msg_type":514},"ato_status":{"service_num":1,"line_num":64,"train_line_num":2,"train_num":3,"origin_line_num":4,"train_order_num":505,"destin_line_num":5,"destin_num":6,"driver_num":0,"ato_mode":0,"running_adjust_command":65535,"cross_station_status":170,"detain_status":170,"next_station_id":28675,"reserv":0},"t_stamp":{"sec":1492616485,"usec":913142}}'
	@Test
	public void TestFrameTrainStatus() {
		//'{"header_info":{"inface_type":36869,"send_vender":16843010,"receive_vender":50397953,"map_version":1,"map_crc":286331153,"msg_cnum":4471,"comm_cycle":2,"msg_snum_side":1,"msg_cnum_previous_msg":1,"protocol_version":1},
		//"msg_header":{"msg_len":58,"msg_type":516},
		//"train_status":{"train_identify_id":1234,"line_num":64,"train_locate_status":85,"direction_train":85,"wheel_steer":0,"t_head_track_id":41003,"t_head_track_offset":492,"t_tail_track_id":40998,"t_tail_track_offset":6592,"excessive_error":0,"owe_error":0,"atp_mode":1,"train_run_mode":1,"train_comp":170,"train_emerg":170,"train_ar_status":170,"train_speed":0,"train_door":170,"park_stab_status":85,"park_ensure_status":255,"turnback_none_sta":0,"pre_choose_mode":8,"stop_emerg_reason":96,"stop_emerg_speed":700,"adv_speed":400,"track_id1":0,"track_offset1":0,"track_id2":0,"track_offset2":0,"reserv":0},
		//"t_stamp":{"sec":1492616485,"usec":792839}}'

		FrameTrainStatus frame = null;
		String json = "{\"header_info\":{\"inface_type\":36869,\"send_vender\":16843010,\"receive_vender\":50397953,\"map_version\":1,\"map_crc\":286331153,\"msg_cnum\":4471,\"comm_cycle\":2,\"msg_snum_side\":1,\"msg_cnum_previous_msg\":1,\"protocol_version\":1},"
						+ "\"msg_header\":{\"msg_len\":58,\"msg_type\":516},"
						+ "\"train_status\":{\"train_identify_id\":1234,\"line_num\":64,\"train_locate_status\":85,\"direction_train\":85,\"wheel_steer\":0,\"t_head_track_id\":41003,\"t_head_track_offset\":492,\"t_tail_track_id\":40998,\"t_tail_track_offset\":6592,\"excessive_error\":0,\"owe_error\":0,\"atp_mode\":1,\"train_run_mode\":1,\"train_comp\":170,\"train_emerg\":170,\"train_ar_status\":170,\"train_speed\":0,\"train_door\":170,\"park_stab_status\":85,\"park_ensure_status\":255,\"turnback_none_sta\":0,\"pre_choose_mode\":8,\"stop_emerg_reason\":96,\"stop_emerg_speed\":700,\"adv_speed\":400,\"track_id1\":0,\"track_offset1\":0,\"track_id2\":0,\"track_offset2\":0,\"reserv\":0},"
						+ "\"t_stamp\":{\"sec\":1492616485,\"usec\":792839}"
						+ "}";
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			//ciStatus = objMapper.readValue(ciStatusJsonStr, AppDataCIStatus.class);
			frame = objMapper.readValue(json, FrameTrainStatus.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int interfaceType = 0x9005;
		assertEquals(interfaceType, frame.getFrameHeader().interfaceType);
		short msgType = 0x0204;
		assertEquals(msgType, frame.getMsgHeader().type);

		short trainCode = 1234;
		long sec = 1492616485;
		long usec = 792839;
		
		assertEquals(trainCode, frame.getTrainStatus().trainCode);
		assertEquals(sec, frame.getTimestamp().sec);
		assertEquals(usec, frame.getTimestamp().usec);
		
		long ms = sec*1000 + usec/1000;
		Date d = new Date(ms);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("date(" + ms + "): " + df.format(d));
		
		Date today = null;
		String strToday = "2017-04-19 00:00:00";
		try {
			today = df.parse(strToday);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long msToday = today.getTime();
		System.out.println("msToday:" + msToday );
		long msTime = ms - msToday;
		System.out.println("msTime: " + msTime);
		Date dTime = new Date(msTime);
		System.out.println("date(" + msTime + "): " + df.format(dTime));
		
		Date currDate = new Date();
		long msCurr = currDate.getTime();
		System.out.println("msCurr: " + msCurr);
		System.out.println("date(" + msCurr + "): " + df.format(currDate));
		
		long msTime2 = msCurr - msToday;
		System.out.println("msTime2: " + msTime2);
		Date dTime2 = new Date(msTime2);
		System.out.println("date(" + msTime2 + "): " + df.format(dTime2)); 
		/*
		// df.format()输出日期时间时是加了时区的（+8小时）
		date(1492616485792): 2017-04-19 23:41:25
		msToday:1492531200000
		msTime: 85285792
		date(85285792): 1970-01-02 07:41:25
		msCurr: 1492590946927
		date(1492590946927): 2017-04-19 16:35:46
		msTime2: 59746927
		date(59746927): 1970-01-02 00:35:46*/
		
		System.out.println("JsonStr: " + json);
		System.out.println("==============================================================");
		System.out.println("java Object: " + frame);
		
		
		//序列化
		//为了使JSON可读，配置缩进输出；生产环境中不需要这样设置
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//配置mapper忽略空属性
		//objMapper.setSerializationInclusion(Include.NON_EMPTY);
		//默认情况，Jackson使用Java属性字段名称作为Json的属性名称，也可以使用Jackson注解改变Json属性名称
		
		String js = null;
		try {
			js = objMapper.writeValueAsString(frame);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(js);
	}
	
	@Test
	public void TestFrameATOStatus() {
		//'{"header_info":{"inface_type":36869,"send_vender":16843010,"receive_vender":50397953,"map_version":1,"map_crc":286331153,"msg_cnum":4472,"comm_cycle":2,"msg_snum_side":1,"msg_cnum_previous_msg":1,"protocol_version":1},
		//"msg_header":{"msg_len":33,"msg_type":514},
		//"ato_status":{"service_num":1,"line_num":64,"train_line_num":2,"train_num":3,"origin_line_num":4,"train_order_num":505,"destin_line_num":5,"destin_num":6,"driver_num":0,"ato_mode":0,"running_adjust_command":65535,"cross_station_status":170,"detain_status":170,"next_station_id":28675,"reserv":0},
		//"t_stamp":{"sec":1492616485,"usec":913142}}'

		FrameATOStatus frame = null;
		String json = "{\"header_info\":{\"inface_type\":36869,\"send_vender\":16843010,\"receive_vender\":50397953,\"map_version\":1,\"map_crc\":286331153,\"msg_cnum\":4472,\"comm_cycle\":2,\"msg_snum_side\":1,\"msg_cnum_previous_msg\":1,\"protocol_version\":1},"
						+ "\"msg_header\":{\"msg_len\":33,\"msg_type\":514},"
						+ "\"ato_status\":{\"service_num\":1,\"line_num\":64,\"train_line_num\":2,\"train_num\":3,\"origin_line_num\":4,\"train_order_num\":505,\"destin_line_num\":5,\"destin_num\":6,\"driver_num\":0,\"ato_mode\":0,\"running_adjust_command\":65535,\"cross_station_status\":170,\"detain_status\":170,\"next_station_id\":28675,\"reserv\":0},"
						+ "\"t_stamp\":{\"sec\":1492616485,\"usec\":913142}"
						+ "}";
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		try {
			//ciStatus = objMapper.readValue(ciStatusJsonStr, AppDataCIStatus.class);
			frame = objMapper.readValue(json, FrameATOStatus.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int interfaceType = 0x9005;
		assertEquals(interfaceType, frame.getFrameHeader().interfaceType);
		short msgType = 0x0202;
		assertEquals(msgType, frame.getMsgHeader().type);

		short trainNum = 505;
		long sec = 1492616485;
		long usec = 913142;
		
		assertEquals(trainNum, frame.getAtoStatus().trainNum);
		assertEquals(sec, frame.getTimestamp().sec);
		assertEquals(usec, frame.getTimestamp().usec);
		
		long ms = sec*1000 + usec/1000;
		Date d = new Date(ms);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("date(" + ms + "): " + df.format(d));
		
		Date today = null;
		String strToday = "2017-04-19 00:00:00";
		try {
			today = df.parse(strToday);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long msToday = today.getTime();
		System.out.println("msToday:" + msToday );
		long msTime = ms - msToday;
		System.out.println("msTime: " + msTime);
		Date dTime = new Date(msTime);
		System.out.println("date(" + msTime + "): " + df.format(dTime));
		
		Date currDate = new Date();
		long msCurr = currDate.getTime();
		System.out.println("msCurr: " + msCurr);
		System.out.println("date(" + msCurr + "): " + df.format(currDate));
		
		long msTime2 = msCurr - msToday;
		System.out.println("msTime2: " + msTime2);
		Date dTime2 = new Date(msTime2);
		System.out.println("date(" + msTime2 + "): " + df.format(dTime2)); 
		/*
		// df.format()输出日期时间时是加了时区的（+8小时）
		date(1492616485792): 2017-04-19 23:41:25
		msToday:1492531200000
		msTime: 85285792
		date(85285792): 1970-01-02 07:41:25
		msCurr: 1492590946927
		date(1492590946927): 2017-04-19 16:35:46
		msTime2: 59746927
		date(59746927): 1970-01-02 00:35:46*/
		
		System.out.println("JsonStr: " + json);
		System.out.println("==============================================================");
		System.out.println("java Object: " + frame);
		
		
		//序列化
		//为了使JSON可读，配置缩进输出；生产环境中不需要这样设置
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//配置mapper忽略空属性
		//objMapper.setSerializationInclusion(Include.NON_EMPTY);
		//默认情况，Jackson使用Java属性字段名称作为Json的属性名称，也可以使用Jackson注解改变Json属性名称
		
		String js = null;
		try {
			js = objMapper.writeValueAsString(frame);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(js);
	}
}