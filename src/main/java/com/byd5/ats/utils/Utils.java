package com.byd5.ats.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Value;

public class Utils {

	public static boolean isWindows() {
		boolean isWin = false;
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			isWin = true;
		}
		return isWin;
	}
	
	public static String getLocalIP() {
		String ip = "";
		InetAddress addr = null;
		
		try {
			if (isWindows()) {
				addr = InetAddress.getLocalHost();
			}
			else {
				boolean find = false;
				Enumeration<NetworkInterface> enumNI = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
				int i = 0;
				int j = 0;
				while (enumNI.hasMoreElements()) {
					if (find) break;
					NetworkInterface ni = (NetworkInterface) enumNI.nextElement();
					Enumeration<InetAddress> enumAddr = ni.getInetAddresses();
					i ++;
					
					while (enumAddr.hasMoreElements()) {
						addr = (InetAddress) enumAddr.nextElement();
						ip = addr.getHostAddress();
						j ++;
						if (ip.contains("%")) continue;
						System.out.println("["+i+"-"+j+"] " + "ip: " + ip);
						if (addr.isSiteLocalAddress() && !(addr.isLoopbackAddress()) && !(addr.getHostAddress().indexOf(":") == -1)) {
							find = true;
							System.out.println("OK");
							break;
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		if (null != addr) {
			ip = addr.getHostAddress();
		}
		return ip;
	}
	
	public static String GetLocalIP2() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ip = addr.getHostAddress().toString();
		return ip;
	}
	
	public static String GetLocalHostname() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String hostname = addr.getHostName().toString();
		return hostname;
	}
}
