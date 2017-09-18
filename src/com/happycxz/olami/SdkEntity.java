package com.happycxz.olami;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.google.gson.Gson;
import com.happycxz.utils.Util;

import ai.olami.cloudService.APIConfiguration;
import ai.olami.cloudService.APIResponse;
import ai.olami.cloudService.CookieSet;
import ai.olami.cloudService.SpeechRecognizer;
import ai.olami.cloudService.SpeechResult;
import ai.olami.nli.NLIResult;
import ai.olami.util.GsonFactory;

public class SdkEntity {
	
	//indicate simplified input
	private static int localizeOption = APIConfiguration.LOCALIZE_OPTION_SIMPLIFIED_CHINESE;
	// * Replace the audio type you want to analyze with this variable.
	
	private static int audioType = SpeechRecognizer.AUDIO_TYPE_PCM_WAVE;
	//private static int audioType = SpeechRecognizer.AUDIO_TYPE_PCM_RAW;

	// * Replace FALSE with this variable if your test file is not final audio. 
	private static boolean isTheLastAudio = true;
	
	private APIConfiguration config = null;
	
	//configure text recognizer
	SpeechRecognizer recoginzer = null;	
	// * Prepare to send audio by a new task identifier.
	//CookieSet cookie = new CookieSet();
	
	// json string for print pretty
	private static Gson jsonDump = GsonFactory.getDebugGson(false);
	// normal json string
	private static Gson mGson = GsonFactory.getNormalGson();

	public SdkEntity(String appKey, String appSecret, String userId) {
		Util.d("new SdkEntity() start.  appKey:" + appKey + ", appSecret: " + appSecret + ", userId: " + userId);
		try {
			config = new APIConfiguration(appKey, appSecret, localizeOption);
			recoginzer = new SpeechRecognizer(config);
	    	recoginzer.setEndUserIdentifier(userId);
	    	recoginzer.setTimeout(10000);
	    	recoginzer.setAudioType(audioType);
		} catch (Exception e) {
			Util.w("new SdkEntity() exception", e);
		}
		Util.d("new SdkEntity() done");
	}
	
	public String getSpeechResult(String inputFilePath) throws NoSuchAlgorithmException, IOException, InterruptedException {
		String lastResult = "";
		
		Util.d("SdkEntity.getSpeechResult() inputFilePath:" + inputFilePath);
		
		CookieSet cookie = new CookieSet();
		
		// * Start sending audio.
		APIResponse response = recoginzer.uploadAudio(cookie, inputFilePath, audioType, isTheLastAudio);
		//
		// You can also send audio data from a buffer (in bytes).
		//
		// For Example :
		// ===================================================================
		// byte[] audioBuffer = Files.readAllBytes(Paths.get(inputFilePath));
		// APIResponse response = recoginzer.uploadAudio(cookie, audioBuffer, audioType, isTheLastAudio);
		// ===================================================================
		//
		Util.d("\nOriginal Response : " + response.toString());
		Util.d("\n---------- dump ----------\n");
		Util.d(jsonDump.toJson(response));
		Util.d("\n--------------------------\n");

		//四种结果，full最完整，seg, nli, asr只包括那一部分
		String full = "", seg = "", nli = "", asr = "";
		// Check request status.
		if (response.ok()) {
			// Now we can try to get recognition result.
			Util.d("\n[Get Speech Result] =====================");
			while (true) {
				Thread.sleep(500);
				// * Get result by the task identifier you used for audio upload.
				Util.d("\nRequest CookieSet[" + cookie.getUniqueID() + "] speech result...");
				response = recoginzer.requestRecognitionWithAll(cookie);
				Util.d("\nOriginal Response : " + response.toString());
				Util.d("\n---------- dump ----------\n");
				Util.d(jsonDump.toJson(response));
				Util.d("\n--------------------------\n");
				// Check request status.
				if (response.ok() && response.hasData()) {
					full = mGson.toJson(response.getData());
					// * Check to see if the recognition has been completed.
					SpeechResult sttResult = response.getData().getSpeechResult();
					if (sttResult.complete()) {
						// * Get speech-to-text result
						Util.p("* STT Result : " + sttResult.getResult());
						asr = mGson.toJson(sttResult);
						// * Check to see if the recognition has be
						// Because we used requestRecognitionWithAll()
						// So we should be able to get more results.
						// --- Like the Word Segmentation.
						if (response.getData().hasWordSegmentation()) {
							String[] ws = response.getData().getWordSegmentation();
							for (int i = 0; i < ws.length; i++) {
								Util.d("* Word[" + i + "] " + ws[i]);
							}
							seg = response.getData().getWordSegmentationSingleString();
						}
						// --- Or the NLI results.
						if (response.getData().hasNLIResults()) {
							NLIResult[] nliResults = response.getData().getNLIResults();
							nli = mGson.toJson(nliResults);
						}
						// * Done.
						break;
					} else {
						// The recognition is still in progress.
						// But we can still get immediate recognition results.
						Util.d("* STT Result [Not yet completed] ");
						Util.d(" --> " + sttResult.getResult());
					}
				}
			}
		} else {
			// Error
			Util.w("* Error! Code : " + response.getErrorCode(), null);
			Util.w(response.getErrorMessage(), null);
		}
		
		lastResult = full;
		
		Util.d("\n===========================================\n");
		return lastResult;
	}
	
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InterruptedException {
		Util.p("SdkEntity.main() start...");
    	int argLen = args.length;
    	
    	Util.d("SdkEntity.main() args.length[" + argLen + "]:");
    	for (String arg : args) {
    		Util.d("SpeexPcm.main() arg[" + arg + "]");
    	}

		new SdkEntity("b4118cd178064b45b7c8f1242bcde31f", "7908028332a64e47b8336d71ad3ce9ab", "abdd").getSpeechResult(args[0]);
    	Util.p("SdkEntity.main() end...");
	}
}
