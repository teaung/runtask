package com.byd5.ats.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.util.StopWatch;

/**
 * 接收运行图的消息
 * 
 */
public class ReceiverDepart {
	private final static Logger LOG = LoggerFactory.getLogger(ReceiverDepart.class);
	
	@RabbitListener(queues = "#{queueDepart.name}")
	public void receiveDepart(String in) throws InterruptedException {
		LOG.info("[departR] '" + in + "'");
	}

	public void receive(String in, int receiver) throws InterruptedException {
		StopWatch watch = new StopWatch();
		watch.start();
		System.out.println("instance " + receiver + " [x] Received '" + in + "'");
		doWork(in);
		watch.stop();
		System.out.println("instance " + receiver + " [x] Done in " + watch.getTotalTimeSeconds() + "s");
	}

	private void doWork(String in) throws InterruptedException {
		for (char ch : in.toCharArray()) {
			if (ch == '.') {
				Thread.sleep(1000);
			}
		}
	}

}
