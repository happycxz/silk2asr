package com.happycxz.olami;

import java.util.Map;

import com.happycxz.utils.Util;

public class AsrAdditionInfo {
	private String appKey = "";
	private String appSecret = "";
	private String userId = "";
	private String voiceFileName = "";
	
	private static final String otherInfo = "\n"
			+ "NOTICE: "
			+ "\n"
			+ "Users identified by 'userId'; 'appKey' and 'appSecret' comes from cn.olami.ai, pls request it yourself and then tell me."
			+ "\n"
			+ "Or pls ask QQ:404499164 or QQ_Group:656580961 @csdn.happycxz, attach comments 'silk2asr appKey' when u contact me"
			+ "\n";
	
	private int errCode = 0;
	public String getErrMsg() {
		switch (this.errCode) {
		case 0:
			return "none error";
		case 1:
			return "ERROR: parameter illegal. appKey, appSecret, userId was requried." + otherInfo;
		case 2:
			return "ERROR: appKey was not put on record on my silk2asr server, contact QQ:404499164 to request it for free." + otherInfo;
		case 99:
		default:
			return "other error, code[" + this.errCode + "]." + otherInfo;
		}
	}

	public AsrAdditionInfo(Map<String, Object> paras) {
		printParameters(paras);

		if (paras.containsKey("appKey") == false) {
			this.errCode = 1;
		} else {
			this.appKey = (String) paras.get("appKey");
		}
		
		if (paras.containsKey("appSecret") == false) {
			this.errCode = 1;
		} else {
			this.appSecret = (String) paras.get("appSecret");
		}
		
		if (paras.containsKey("userId") == false) {
			this.errCode = 1;
		} else {
			this.userId = (String) paras.get("userId");
		}

		if (this.appKey.isEmpty() || this.appSecret.isEmpty() || this.userId.isEmpty()) {
			this.errCode = 1;
		}
		
		if (OlamiKeyManager.isValidKey(this.appKey) == false) {
			//检查appKey是否在我的支持列表中
			this.errCode = 2;
		}
		
		if (this.errCode == 0) {
			this.voiceFileName = Util.getCurrentMilliSecond() + "_" + this.userId + "_" + this.appKey + ".silk";
		}
	}

	private static void printParameters(Map<String, Object> paras) {
		if (paras.size() == 0) {
			Util.p("para list IS EMPTY!");
			return;
		}

		StringBuffer paraList = new StringBuffer();
		for (String key : paras.keySet()) {
			if (paraList.length() != 0) {
				paraList.append(", ");
			}
			paraList.append(key + "|" + paras.get(key));
		}
		Util.p("para list IS: " + paraList.toString());
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getVoiceFileName() {
		return voiceFileName;
	}

	public void setVoiceFileName(String voiceFileName) {
		this.voiceFileName = voiceFileName;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

}
