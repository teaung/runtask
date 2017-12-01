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
import com.byd.ats.protocol.RabbConstant;
import com.byd5.ats.utils.RuntaskConstant;

import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Gary Russell
 * @author Scott Deeg
 */
//@Profile({"tut5","topics"})
@Configuration
public class Config{

	@Bean
	public TopicExchange topicATS2CU() {
		return new TopicExchange(RabbConstant.RABB_EX_ATS2CU); //"topic.ats2cu"
	}
	@Bean
	public TopicExchange topicCU2ATS() {
		return new TopicExchange(RabbConstant.RABB_EX_CU2ATS); //"topic.cu2ats"
	}

	@Bean
	public TopicExchange topicServ2Cli() {
		return new TopicExchange(RuntaskConstant.RABB_EX_SER2CLI);
	}
	
	@Bean
	public TopicExchange exchangeRungraph() {
		return new TopicExchange(RuntaskConstant.RABB_EX_RUNGRAPH);
	}
	@Bean
	public TopicExchange exchangeTrace() {
		return new TopicExchange(RuntaskConstant.RABB_EX_TRACE);
	}

	@Bean
	public TopicExchange exchangeDepart() {
		//return new TopicExchange("topic.ats2aod");
		return new TopicExchange(RuntaskConstant.RABB_EX_DEPART);
	}
	
	@Bean
	public TopicExchange exchangeAdjust() {
		return new TopicExchange(RuntaskConstant.RABB_EX_DJUST);
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
		public Queue queueRungraphRunInfo() {
			return new AnonymousQueue();
		}
		
		@Bean
		public Queue queueAdjust() {
			return new AnonymousQueue();
		}
		
		@Bean
		public Queue queueRungraphChangeTask(){
			return new AnonymousQueue();
		}
		
		@Bean
		public Binding bindingAdjust(@Qualifier("exchangeAdjust") TopicExchange ex, Queue queueAdjust) {
			return BindingBuilder.bind(queueAdjust).to(ex).with(RuntaskConstant.RABB_RK_ADJUST_RUNTIME);
		}
		
		@Bean
		public Binding bindingRungraph(@Qualifier("exchangeRungraph") TopicExchange ex, Queue queueRungraph) {
			return BindingBuilder.bind(queueRungraph).to(ex).with(RuntaskConstant.RABB_RK_RUNGRAPH_TASK);
		}
		
		@Bean
		public Binding bindingRungraphRunInfo(@Qualifier("exchangeRungraph") TopicExchange ex, Queue queueRungraphRunInfo) {
			return BindingBuilder.bind(queueRungraphRunInfo).to(ex).with(RuntaskConstant.RABB_RK_RUNGRAPH_OUTGARAGE);
		}
		
		@Bean
		public Binding bindingRungraphChangeTask(@Qualifier("exchangeRungraph") TopicExchange ex, Queue queueRungraphChangeTask) {
			return BindingBuilder.bind(queueRungraphChangeTask).to(ex).with("ats.trainrungraph.changeTask");
		}
		
		@Bean
		public Queue queueTraceStationArrive() {
			return new AnonymousQueue();
		}
		@Bean
		public Queue queueTraceStationEnter() {
			return new AnonymousQueue();
		}
		@Bean
		public Queue queueTraceReturnLeave() {
			return new AnonymousQueue();
		}
		@Bean
		public Queue queueTraceReturnArrive() {
			return new AnonymousQueue();
		}
		@Bean
		public Queue queueTraceTransformArrive() {
			return new AnonymousQueue();
		}
		@Bean
		public Queue queueTraceJudgeATO() {
			return new AnonymousQueue();
		}
		@Bean
		public Queue queueTraceStationLeave() {
			return new AnonymousQueue();
		}
		//车辆到站停稳消息
		@Bean
		public Binding bindingTraceStationArrive(@Qualifier("exchangeTrace") TopicExchange ex, Queue queueTraceStationArrive) {
			return BindingBuilder.bind(queueTraceStationArrive).to(ex).with(RuntaskConstant.RABB_RK_TRACE_ARRIVE_STATION);
		}
		//到站（不管是否停稳）
		@Bean
		public Binding bindingTraceStationEnter(@Qualifier("exchangeTrace") TopicExchange ex, Queue queueTraceStationEnter) {
			return BindingBuilder.bind(queueTraceStationEnter).to(ex).with(RuntaskConstant.RABB_RK_TRACE_ENTER_STATION);
		}
		//离站
		@Bean
		public Binding bindingTraceStationLeave(@Qualifier("exchangeTrace") TopicExchange ex, Queue queueTraceStationLeave) {
			return BindingBuilder.bind(queueTraceStationLeave).to(ex).with(RuntaskConstant.RABB_RK_TRACE_LEAVE_STATION);
		}		
		//离开折返轨
		@Bean
		public Binding bindingTraceReturnLeave(@Qualifier("exchangeTrace") TopicExchange ex, Queue queueTraceReturnLeave) {
			return BindingBuilder.bind(queueTraceReturnLeave).to(ex).with(RuntaskConstant.RABB_RK_TRACE_LEAVE_RETURN);
		}
		//到达折返轨
		@Bean
		public Binding bindingTraceReturnArrive(@Qualifier("exchangeTrace") TopicExchange ex, Queue queueTraceReturnArrive) {
			return BindingBuilder.bind(queueTraceReturnArrive).to(ex).with(RuntaskConstant.RABB_RK_TRACE_ARRIVE_RETURN);
		}		
		// 到达转换轨
		@Bean
		public Binding bindingTraceTransformArrive(@Qualifier("exchangeTrace") TopicExchange ex,Queue queueTraceTransformArrive) {
			return BindingBuilder.bind(queueTraceTransformArrive).to(ex).with(RuntaskConstant.RABB_RK_TRACE_ARRIVE_TRANSFORM);
		}	
		// 离开折返轨
		@Bean
		public Binding bindingTraceJudgeATO(@Qualifier("exchangeTrace") TopicExchange ex,Queue queueTraceJudgeATO) {
			return BindingBuilder.bind(queueTraceJudgeATO).to(ex).with(RuntaskConstant.RABB_RK_TRACE_JUDGEATO);
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
			return BindingBuilder.bind(queueATOStatus).to(ex).with(RabbConstant.RABB_RK_VA_ATO_STATUS); //"cu2ats.vobc.ato.status"
		}
	}

	//@Profile("sender")
	@Bean
	public SenderDepart senderDepart() {
		return new SenderDepart();
	}
	
}
