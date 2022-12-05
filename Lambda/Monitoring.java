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
	public	String	handleRequest(Object	input,	Context	context) {
					context.getLogger().log("Input:	" +	input);
					String	json	= ""+input;
					JsonParser	parser	= new JsonParser();
					JsonElement	element	=	parser.parse(json);
					JsonElement	state	=	element.getAsJsonObject().get("state");
					JsonElement	reported	=	state.getAsJsonObject().get("reported");
					String	temperature	=	reported.getAsJsonObject().get("temperature").getAsString();
					double	temp	=	Double.valueOf(temperature);
					final	String	AccessKey="AKIA27W3KCRCGZ6PH7HF";
					final	String	SecretKey="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";
					final	String	topicArn="arn:aws:sns:ap-northeast-2:755300439108:temerature_warning_topic";
					BasicAWSCredentials	awsCreds	= new BasicAWSCredentials(AccessKey,	SecretKey);		
					AmazonSNS	sns	=	AmazonSNSClientBuilder.standard()
																	.withRegion(Regions.AP_NORTHEAST_2)
																	.withCredentials( new AWSStaticCredentialsProvider(awsCreds) )
																	.build();
					final	String	msg	= "*Temperature	Critical*\n" + "Your	device	temperature	is	" +	temp	+ "C";
					final	String	subject	= "Critical	Warning";
					if (temp	>= 26.0) {
									PublishRequest	publishRequest	= new PublishRequest(topicArn,	msg,	subject);
									PublishResult	publishResponse	=	sns.publish(publishRequest);
					}
					return	subject+ "temperature	=	" +	temperature	+ "!";
	}
}