package com.happycxz.olami;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OlamiEntityFactory {
	private static Map<String, SdkEntity> cache = new ConcurrentHashMap<String, SdkEntity>();
	
	public static SdkEntity createEntity(String appKey, String appSecret, String userId) {
		String key = appKey + "_" + appSecret + "_" + userId;
		if (cache.containsKey(key)) {
			return cache.get(key);
		}
		
		SdkEntity entity = new SdkEntity(appKey, appSecret, userId);
		//暂时关闭缓存，内存吃不消
		//cache.put(key, entity);
		return entity;
	}
	
	public static String cacheStatus() {
		StringBuffer ret = new StringBuffer("online cache number:" + cache.size() + "\n");
		for (String key : cache.keySet()) {
			ret.append(key + "\n");
		}
		
		return ret.toString();
	}
}
