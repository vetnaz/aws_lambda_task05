package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task05.dto.Event;
import com.task05.dto.LambdaRequest;
import com.task05.dto.LambdaResponse;
import com.task05.model.ItemRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role"
)
public class ApiHandler implements RequestHandler<LambdaRequest, LambdaResponse> {
    private AmazonDynamoDB amazonDynamoDB;
    private static final String REGION = "eu-central-1";
    public LambdaResponse handleRequest(LambdaRequest request, Context context) {
        this.initDynamoDbClient();
        Gson gson = new GsonBuilder().create();
        LambdaLogger logger = context.getLogger();

        logger.log("EVENT: " + gson.toJson(request));

        LambdaResponse lambdaResponse = new LambdaResponse();
        lambdaResponse.setStatusCode(201);
        lambdaResponse.setEvent(persistData(request, gson));
        return lambdaResponse;
    }

    private Event persistData(LambdaRequest request, Gson gson) {

        String generatedId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // Format the date and time using a specific pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        String formattedDateTime = now.format(formatter);

        ItemRecord item = new ItemRecord();
        item.setId(generatedId);
        item.setPrincipalId(request.getPrincipalId());
        item.setCreatedAt(formattedDateTime);
        item.setBody(gson.toJson(request.getContent()));

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);
        mapper.save(item);

        Event event = new Event();
        event.setBody(gson.toJson(request.getContent()));
        event.setId(generatedId);
        event.setCreatedAt(formattedDateTime);
        event.setPrincipalId(request.getPrincipalId());
        return event;
    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .build();
    }
}
