package com.amazonaws.lambda.demo;

//파티션키 DeviceID(문자형), 정렬키 timestamp(숫자형)

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class InfoToDynamoDB implements RequestHandler<Document , String> {

	private DynamoDB dynamoDb;
    private String DYNAMODB_TABLE_NAME = "FinalDB"; //생성한 dynamoDB 테이블이름을 적는다.

    @Override
    public String handleRequest(Document input, Context context) {
        this.initDynamoDbClient();
        context.getLogger().log("Input: "+ input);
        persistData(input);
        
        return "Success in storing to DB!";
    }

    private PutItemOutcome persistData(Document document) throws ConditionalCheckFailedException {

        // Epoch Conversion Code: https://www.epochconverter.com/
        SimpleDateFormat sdf = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
        String timeString = sdf.format(new java.util.Date (document.timestamp*1000));

        return this.dynamoDb.getTable(DYNAMODB_TABLE_NAME)
                .putItem(new PutItemSpec().withItem(new Item().withPrimaryKey("DeviceID",document.device)
                		.withLong("timestamp", document.timestamp) 
                        .withString("waterlevel", document.current.state.reported.waterlevel)
                        .withString("Gate", document.current.state.reported.BAR)
                        .withString("Realtime",timeString)));
        				
        				
    }

    private void initDynamoDbClient() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-northeast-2").build();

        this.dynamoDb = new DynamoDB(client);
    }


}

class Document {
          
    public Thing current;
    public long timestamp;
    public String device;       // AWS IoT에 등록된 사물 이름 
}

class Thing {
    public State state = new State();
    public long timestamp;
    public String clientToken;
    
    public class State {
        public Tag reported = new Tag();
        public Tag desired = new Tag();
        
        public class Tag {
        	
            public String waterlevel;
            public String BAR;
        }
    }
}