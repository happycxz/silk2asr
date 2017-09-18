package com.happycxz.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Map;  
  
import javax.servlet.ServletException;  
import javax.servlet.http.HttpServletRequest;  
import javax.servlet.http.HttpServletResponse;  
import javax.servlet.http.Part;

import org.springframework.stereotype.Controller;  
import org.springframework.util.StringUtils;  
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RequestParam;  
import org.springframework.web.bind.annotation.ResponseBody;

import com.happycxz.olami.AsrAdditionInfo;
import com.happycxz.olami.OlamiEntityFactory;
import com.happycxz.olami.SdkEntity;
import com.happycxz.utils.Configuration;
import com.happycxz.utils.Util;
import com.sun.org.apache.xml.internal.security.utils.Base64;  

/** 
 * olami与微信小程序 接口相关对接
 * @author Jod
 */
@Controller  
@RequestMapping("/olami")  
public class OlamiController {
	
	//保存linux shell命令字符串
	private static final String SHELL_CMD = Configuration.getInstance().getValue("local.shell.cmd", "sh /YOUR_PATH/silk-v3-decoder/converter_cxz.sh %s wav");

    //保存silk和wav文件的目录，放在web目录、或一个指定的绝对目录下 
    private static final String localFilePath = Configuration.getInstance().getValue("local.file.path", "/YOUR/LOCAL/VOICE/PATH/");;  
    
    static {
    	Util.p("OlamiController base SHELL_CMD:" + SHELL_CMD);
    	Util.p("OlamiController base localFilePath:" + localFilePath);
    }

    @RequestMapping(value="/asr", produces="plain/text; charset=UTF-8")  
    public @ResponseBody String asrUploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> p)  
            throws ServletException, IOException {  

    	AsrAdditionInfo additionInfo = new AsrAdditionInfo(p);
    	if (additionInfo.getErrCode() != 0) {
    		//参数不合法，或者appKey没有在支持列表中备录
    		return Util.JsonResult(String.valueOf(additionInfo.getErrCode()), additionInfo.getErrMsg());  
    	}
    	
    	String localPathToday = localFilePath + Util.getDateStr() + File.separator;
        // 如果文件存放路径不存在，则mkdir一个  
        File fileSaveDir = new File(localPathToday);  
        if (!fileSaveDir.exists()) {  
            fileSaveDir.mkdirs();  
        }
  
        int count = 1;
        String asrResult = "";
        for (Part part : request.getParts()) {  
            String fileName_origin = extractFileName(part);
            //这里必须要用原始文件名是否为空来判断，因为part列表是所有数据，前三个被formdata占了，对应文件名其实是空
            if(!StringUtils.isEmpty(fileName_origin)) {
            	String fileName = additionInfo.getVoiceFileName();
            	String silkFile = localPathToday + fileName;
            	Util.p("silkFile[" + count + "]:" + silkFile);

            	part.write(silkFile);
            	
            	if (webmBase64Decoder2Wav(silkFile)) {
            		// support webm/base64 in webmBase64Decoder2Wav();
            		// is webm base64 format, and xxxx.webm file is temporary created, xxxx.wav was last be converted.
            	} else {
            		// run script to convert silk(v3) to wav
                    Util.RunShell2Wav(SHELL_CMD, silkFile);
            	}
            	
                // get wave file path and name, prepare for olami asr
                String waveFile = DotSilk2DotOther(silkFile, "wav");
                Util.p("OlamiController.asrUploadFile() waveFile:" + waveFile);
                
                if (new File(waveFile).exists() == false) {
                	Util.w("OlamiController.asrUploadFile() wav file[" + waveFile + "] not exist!", null);
					return Util.JsonResult("80", "convert silk to wav failed, NOW NOT SUPPORT WXAPP DEVELOP RECORD because it is not silk_v3 format. anyother reason please tell QQ:404499164."); 
                }
                
                try {
                	SdkEntity entity = OlamiEntityFactory.createEntity(additionInfo.getAppKey(), additionInfo.getAppSecret(), additionInfo.getUserId());
					asrResult = entity.getSpeechResult(waveFile);
					Util.p("OlamiController.asrUploadFile() asrResult:" + asrResult);
				} catch (NoSuchAlgorithmException | InterruptedException e) {
					Util.w("OlamiController.asrUploadFile() asr NoSuchAlgorithmException or InterruptedException", e);
				} catch (FileNotFoundException e) {
					Util.w("OlamiController.asrUploadFile() asr FileNotFoundException", e);
					return Util.JsonResult("80", "convert silk to wav failed, NOW NOT SUPPORT WXAPP DEVELOP RECORD because it is not silk_v3 format. anyother reason please tell QQ:404499164."); 
				} catch (Exception e) {
					Util.w("OlamiController.asrUploadFile() asr Exception", e);
				}
            }
            count++;
        }
        
        //防止数据传递乱码
        //response.setContentType("application/json;charset=UTF-8");

        return Util.JsonResult("0", "olami asr success!", asrResult);  
    }  
   
    /**
     * 将  xxxxx.silk 文件名转 xxxx.wav
     * @param silkName
     * @param otherSubFix
     * @return
     */
    private static String DotSilk2DotOther(String silkName, String otherSubFix) {
    	int removeByte = 4;
    	if (silkName.endsWith("silk")) {
    		removeByte = 4;
    	} else if (silkName.endsWith("slk")) {
    		removeByte = 3;
    	}
    	return silkName.substring(0, silkName.length()-removeByte) + otherSubFix;
    }
    
    /** 
     * 从content-disposition头中获取源文件名 
     *  
     * content-disposition头的格式如下： 
     * form-data; name="dataFile"; filename="PHOTO.JPG" 
     *  
     * @param part 
     * @return 
     */  
    @SuppressWarnings("unused")
	private String extractFileName(Part part) {  
        String contentDisp = part.getHeader("content-disposition");  
        String[] items = contentDisp.split(";");  
        for (String s : items) {  
            if (s.trim().startsWith("filename")) {  
                return s.substring(s.indexOf("=") + 2, s.length()-1);  
            }  
        }  
        return "";  
    }


    /**
     * 通过filePath内容判断是否是webm/base64格式，如果是，先decode base64后，再直接ffmpeg转wav，
     * 如果不是，返回false丢给外层继续当作silk v3去解
     * @param filePath
     * @return
     */
	public static boolean webmBase64Decoder2Wav(String filePath) {
		boolean isWebm = false;
		try {
			String encoding = "utf-8";
			File file = new File(filePath);
			// 判断文件是否存在
			if ((file.isFile() == false) || (file.exists() == false)) {
				Util.w("webmBase64Decoder2Wav() no file[" + filePath + "] exist.", null);
			}
			
			StringBuilder lineTxt = new StringBuilder();
			String line = null;
			try (
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);) {
				while ((line = bufferedReader.readLine()) != null) {
					lineTxt.append(line);
				}
				read.close();
			} catch (Exception e) {
				Util.w("webmBase64Decoder2Wav() exception0:", e);
				return isWebm;
			}
			
			String oldData = lineTxt.toString();
			if (oldData.startsWith("data:audio/webm;base64,") == false) {
				Util.d("webmBase64Decoder2Wav() file[" + filePath + "] is not webm, or already decoded." );
				return isWebm;
			}
			
			isWebm = true;
			oldData = oldData.replace("data:audio/webm;base64,", "");
			String webmFileName = DotSilk2DotOther(filePath, "webm");
			try {

				File webmFile = new File(webmFileName);
				byte[] bt = Base64.decode(oldData);
				FileOutputStream in = new FileOutputStream(webmFile);
				try {
					in.write(bt, 0, bt.length);
					in.close();
				} catch (IOException e) {
					Util.w("webmBase64Decoder2Wav() exception1:", e);
					return isWebm;
				}
			} catch (FileNotFoundException e) {
				Util.w("webmBase64Decoder2Wav() exception2:", e);
				return isWebm;
			}
			
			// run cmd to convert webm to wav
    		Util.RunShell2Wav(SHELL_CMD, webmFileName);
		} catch (Exception e) {
			Util.w("webmBase64Decoder2Wav() exception3:", e);
			return isWebm;
		}
		
		return isWebm;
	}
	
	public static void main(String[] args) {
		webmBase64Decoder2Wav("D:\\secureCRT_RZSZ\\1505716415538_f7d98081-4d21-3b40-a7df-e56c046a784d_b4118cd178064b45b7c8f1242bcde31f.silk");
	}
} 