package com.byd5.ats;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.byd5.ats.utils.Utils;

public class test {

	public static void main(String arg[]){
		System.out.println(Utils.getLocalIP());
		
		final Timer timer1 = new Timer();
	    TimerTask task = new TimerTask() {
	        private int count;
	  
	        @Override
	        public void run() {
	            this.count++;
	            System.out.println(count);
	            if (count == 10) {
	                System.out.println("定时器停止了");
	                timer1.cancel();// 停止定时器
	            }
	        }
	    };
	    timer1.schedule(task, 0, 1000);// 1秒一次
		
		
		Timer timer = new Timer();     
        timer.schedule(new MyTask(), 1000, 2000);//在1秒后执行此任务,每次间隔2秒执行一次,如果传递一个Data参数,就可以在某个固定的时间执行这个任务.     
        while(true){//这个是用来停止此任务的,否则就一直循环执行此任务     
            try{     
                int in = System.in.read();    
                if(in == 's'){     
                    timer.cancel();//使用这个方法退出任务     
                    break;  
                }     
            } catch (IOException e){     
                // TODO Auto-generated catch block     
                e.printStackTrace();     
            }     
        }     
    }    
      
    static class MyTask extends java.util.TimerTask{      
        public void run(){     
            System.out.println("________");     
        }     
    }
	
}
