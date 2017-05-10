package com.byd5.ats;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.byd5.ats.protocol.ats_ci.AppDataCIStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
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




public class JacksonAnnotationTest {

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
		
	}
	
	public class Province {
		public String name;
		public int population;
		public String[] city;
	}


	public class Country {
		//注意：被序列化的bean的private属性字段需要创建getter方法或者属性字段应该为public
		private String country_id;
		public Date birthDate;
		public List<String> nation = new ArrayList<String>();
		public String[] lakes;
		public List<Province> provinces = new ArrayList<Province>();
		public Map<String, Integer> traffic = new HashMap<String, Integer>();
		
		public Country() {
			
		}
		public Country(String countryId) {
			this.country_id = countryId;
		}
		
		public String getCountry_id() {
			return country_id;
		}
		public void setCountry_id(String country_id) {
			this.country_id = country_id;
		}
		
		@Override
		public String toString() {
			return "Country [country_id=" + country_id + 
					", birthDate=" + birthDate +
					", nation=" + nation +
					", lakes=" + Arrays.toString(lakes) +
					", provinces=" + provinces +
					", traffic=" + traffic + "]";
		}
	}
	*/
	public static void main(String[] args) throws IOException {
		AppDataCIStatus ciStatus;
		String ciStatusJsonStr = "{\"s_status\":[193,193,193,196,193,193,193,193,193,193,193,193,193,193],"
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
				+ "\"autotrig_status\":[1]}";
		
		ObjectMapper objMapper = new ObjectMapper();
		
		//反序列化
		//当反序列化json时，未知属性会引起发序列化被打断，这里禁用未知属性打断反序列化功能，
		//例如json里有10个属性，而我们bean中只定义了2个属性，其他8个属性将被忽略。
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		ciStatus = objMapper.readValue(ciStatusJsonStr, AppDataCIStatus.class);
		
		
		System.out.println("ciStatusJsonStr: " + ciStatusJsonStr);
		System.out.println("==============================================================");
		System.out.println("ciStatus Object: " + ciStatus);

		//序列化
		//为了使JSON可读，配置缩进输出；生产环境中不需要这样设置
		objMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		//配置mapper忽略空属性
		objMapper.setSerializationInclusion(Include.NON_EMPTY);
		//默认情况，Jackson使用Java属性字段名称作为Json的属性名称，也可以使用Jackson注解改变Json属性名称
		
		System.out.println(objMapper.writeValueAsString(ciStatus));
   	 
	}
}

