package com.happycxz.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

public class Configuration {
	private static Configuration instance = new Configuration();
	private Properties propertie;

	public static Configuration getInstance() {
		return instance;
	}

	public synchronized void reload() {
		String filePath = Util.getRootPath() + "/res/config.properties";
		Util.d("new Configuration() filePath: " + filePath);
		try {
			FileInputStream inputFile = new FileInputStream(filePath);
			propertie = new Properties();
			propertie.load(new InputStreamReader(inputFile, "utf-8"));
			inputFile.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private Configuration() {
		Util.d("new Configuration() start");
		reload();
		Util.d("new Configuration() end");
	}

	public String getValue(String key, String defaultValue) {
		if (propertie.containsKey(key)) {
			String value = propertie.getProperty(key);
			return value.trim();
		} else
			return defaultValue;
	}

	public ArrayList<String> getKeys() {
		ArrayList<String> ret = new ArrayList<String>();
		for (Object key : propertie.keySet()) {
			ret.add((String) key);
		}
		return ret;
	}

	public Properties getPropertie() {
		return propertie;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String val = Configuration.getInstance().getValue("server.port", "hehe");
		Util.p(val);
	}
}
