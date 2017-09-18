package com.happycxz.olami;

import java.util.ArrayList;

import com.happycxz.utils.Configuration;
import com.happycxz.utils.Util;

public class OlamiKeyManager {
	private static ArrayList<String> keys = new ArrayList<String>();
	
	static {
		init();
	}
	
	public static String updateKeys() {
		init();
		return keys.toString();
	}
	
	private static void init() {
		ArrayList<String> keysTmp = new ArrayList<String>();
		try {
			String keyListConfig = Configuration.getInstance().getValue("olami.asr.keys", "").trim();
			String keyArray[] = keyListConfig.split(",");
			if ((keyArray.length == 0) || ((keyArray.length == 1) && (keyArray[0].trim().isEmpty()))) {
				Util.p("OlamiKeyManager.init() olami keys config IS EMPTY."); 
			}
			
			for (String key : keyArray) {
				if (key.trim().isEmpty()) {
					continue;
				}
				
				keysTmp.add(key.trim());
			}
		} catch (Exception e) {
			Util.w("OlamiKeyManager.init() exception!", e);
		}
		
		keys = keysTmp;
		Util.p("OlamiKeyManager.init() olami keys supported:" + keys.toString());
	}
	
	public static boolean isValidKey(String appKey) {
		return keys.contains(appKey);
	}
	
	public static void main(String[] args) {
		
	}
}
