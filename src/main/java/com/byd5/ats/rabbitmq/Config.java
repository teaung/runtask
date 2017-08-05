/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.byd5.ats.rabbitmq;

import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.byd5.ats.protocol.AppProtocolConstant;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Gary Russell
 * @author Scott Deeg
 */
//@Profile({"tut5","topics"})
@Configuration
public class Config {

	@Bean
	public TopicExchange topicATS2CU() {
		return new TopicExchange(AppProtocolConstant.EXCHANGE_ATS2CU); //"topic.ats2cu"
	}
	@Bean
	public TopicExchange topicCU2ATS() {
		return new TopicExchange(AppProtocolConstant.EXCHANGE_CU2ATS); //"topic.cu2ats"
	}

	@Bean
	public TopicExchange topicServ2Cli() {
		return new TopicExchange("topic.serv2cli");
	}
	
	@Bean
	public TopicExchange exchangeRungraph() {
		return new TopicExchange("topic.ats.trainrungraph");
	}
	@Bean
	public TopicExchange exchangeTrace() {
		return new TopicExchange("topic.ats.traintrace");
	}

	@Bean
	public TopicExchange exchangeDepart() {
		//return new TopicExchange("topic.ats2aod");
		return new TopicExchange("topic.ats.traindepart");
	}
	
	@Bean
	public TopicExchange exchangeAdjust() {
		return new TopicExchange("topic.ats.trainadjust");
	}
	
	//@Profile("receiver")
	private static class ReceiverConfig {

		@Bean
		public ReceiverAdjust receiverAdjust() {
			return new ReceiverAdjust();
		}
		
		@Bean
		public ReceiverRungraph receiverRungraph() {
			return new ReceiverRungraph();
		}
		
		@Bean
		public ReceiverDepart receiverDepart() {
			return new ReceiverDepart();
		}

		@Bean
		public Queue queueRungraph() {
			return new AnonymousQueue();
		}
		
		@Bean
		public Queue queueAdjust() {
			return new AnonymousQueue();
		}
		
		@Bean
		public Binding bindingAdjust(@Qualifier("exchangeAdjust") TopicExchange ex, Queue queueAdjust) {
			return BindingBuilder.bind(queueAdjust).to(ex).with("ats.trainadjust.runtime");
		}
		
		@Bean
		public Binding bindingRungraph(@Qualifier("exchangeRungraph") TopicExchange ex, Queue queueRungraph) {
			return BindingBuilder.bind(queueRungraph).to(ex).with("ats.trainrungraph.task");
		}
		
		@Bean
		public Queue queueTraceStationArrive() {
			return new AnonymousQueue();
		}
		@Bean
		public Queue queueTraceStationEnter() {
			return new AnonymousQueue();
		}
		//车辆到站停稳消息
		@Bean
		public Binding bindingTraceStationArrive(@Qualifier("exchangeTrace") TopicExchange ex, Queue queueTraceStationArrive) {
			return BindingBuilder.bind(queueTraceStationArrive).to(ex).with("ats.traintrace.station.arrive");
		}
		//到站（不管是否停稳）
		@Bean
		public Binding bindingTraceStationEnter(@Qualifier("exchangeTrace") TopicExchange ex, Queue queueTraceStationEnter) {
			return BindingBuilder.bind(queueTraceStationEnter).to(ex).with("ats.traintrace.station.enter");
		}
		@Bean
		public ReceiverTrace receiverTrace() {
			return new ReceiverTrace();
		}
		
		
		@Bean
		public Queue queueDepart() {
			return new AnonymousQueue();
		}
		@Bean
		public Binding bindingDepart(@Qualifier("exchangeDepart") TopicExchange ex, Queue queueDepart) {
			return BindingBuilder.bind(queueDepart).to(ex).with("ats.traindepart.*");
		}
		
		@Bean
		public Queue queueATOStatus() {
			return new AnonymousQueue();
		}
		@Bean
		public Binding bindingATOStatus(@Qualifier("topicCU2ATS") TopicExchange ex, Queue queueATOStatus) {
			return BindingBuilder.bind(queueATOStatus).to(ex).with(AppProtocolConstant.ROUTINGKEY_VOBC_ATO_STATUS); //"cu2ats.vobc.ato.status"
		}
	}

	//@Profile("sender")
	@Bean
	public SenderDepart senderDepart() {
		return new SenderDepart();
	}
	
}
