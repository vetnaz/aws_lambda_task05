package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.task05.dto.Event;
import com.task05.dto.LambdaRequest;
import com.task05.dto.LambdaResponse;
import org.joda.time.DateTime;

import java.util.UUID;

@LambdaHandler(lambdaName = "api_handler",
        roleName = "api_handler-role"
)
public class ApiHandler implements RequestHandler<LambdaRequest, LambdaResponse> {
    private AmazonDynamoDB amazonDynamoDB;
    private static final String REGION = "eu-central-1";
    private static final String DYNAMODB_TABLE_NAME = "cmtr-985d4752-Events-test";

    public LambdaResponse handleRequest(LambdaRequest request, Context context) {
        this.initDynamoDbClient();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        LambdaLogger logger = context.getLogger();

        logger.log("EVENT: " + gson.toJson(request));

        LambdaResponse lambdaResponse = new LambdaResponse();
        lambdaResponse.setStatusCode(201);
        lambdaResponse.setEvent(persistData(request, gson));
        return lambdaResponse;
    }

    private Event persistData(LambdaRequest request, Gson gson) {
        String generatedId = UUID.randomUUID().toString();
        String createAt = DateTime.now().toString();
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);

        Item item = new Item()
                .withString("id", generatedId)
                .withInt("principalId", request.getPrincipalId())
                .withString("createdAt", createAt)
                .withString("body", gson.toJson(request.getContent()));

        Table table = dynamoDB.getTable(DYNAMODB_TABLE_NAME);
        table.putItem(item);

        Event event = new Event();
        event.setBody(gson.toJson(request.getContent()));
        event.setId(generatedId);
        event.setCreatedAt(createAt);
        event.setPrincipalId(request.getPrincipalId());
        return event;
    }

    private void initDynamoDbClient() {
        this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(REGION)
                .build();
    }
}
