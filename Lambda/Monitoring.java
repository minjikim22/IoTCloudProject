package com.example.lambda.monitoring;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class Monitoring implements RequestHandler<Object, String> {

	@Override
	public String handleRequest(Object input, Context context) {
	    context.getLogger().log("Input: " + input);
	    String json = ""+input;
	    JsonParser parser = new JsonParser();
	    JsonElement element = parser.parse(json);
	    JsonElement state = element.getAsJsonObject().get("state");
	    JsonElement reported = state.getAsJsonObject().get("reported");
	    String waterlevel = reported.getAsJsonObject().get("waterlevel").getAsString();
	    double waterlv = Double.valueOf(waterlevel);

	    final String AccessKey="AKIA27W3KCRCGZ6PH7HF"; //엑세스 키 ID
	    final String SecretKey="/LUCBme0YOAZU4lbXEDetGonLCGRCxZUPQjE/yZA"; // 비밀 엑세스 키
	    final String topicArn="arn:aws:sns:ap-northeast-2:755300439108:gimal"; // 주제 ARN

	    BasicAWSCredentials awsCreds = new BasicAWSCredentials(AccessKey, SecretKey);  
	    AmazonSNS sns = AmazonSNSClientBuilder.standard()
	                .withRegion(Regions.AP_NORTHEAST_2)
	                .withCredentials( new AWSStaticCredentialsProvider(awsCreds) )
	                .build();

	    final String msg = "A waterlevel is " + waterlv + "! Close the GATE2"; // 내용
	    final String subject = "Warning"; // 제목
	    if (waterlv >= 600.0) {
	        PublishRequest publishRequest = new PublishRequest(topicArn, msg, subject); // 주제를 구독한 e-mail로 msg(내용)과 subject(제목)을 보낸다.
	        PublishResult publishResponse = sns.publish(publishRequest);
	    }

	    return subject+ "waterlevel = " + waterlevel + "!";
	}

}
