package com.securitydemo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static ConfigurableApplicationContext applicationContext;
    private static SpringApiGatewayRequestHandler requestHandler;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            // Initialize Spring context if not already done
            if (applicationContext == null) {
                applicationContext = SpringApplication.run(SecurityDemoApplication.class);
                requestHandler = new SpringApiGatewayRequestHandler(applicationContext);
            }

            // Process the request through Spring
            return requestHandler.handle(input);

        } catch (Exception e) {
            e.printStackTrace();
            APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();
            errorResponse.setStatusCode(500);
            errorResponse.setBody("{\"error\":\"" + e.getMessage() + "\"}");

            // Add only Content-Type header without CORS
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            errorResponse.setHeaders(headers);

            return errorResponse;
        }
    }
}