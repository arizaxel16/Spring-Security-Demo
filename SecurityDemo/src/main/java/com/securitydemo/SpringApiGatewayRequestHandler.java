package com.securitydemo;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class SpringApiGatewayRequestHandler {
    private final DispatcherServlet dispatcherServlet;

    public SpringApiGatewayRequestHandler(ConfigurableApplicationContext applicationContext) {
        this.dispatcherServlet = applicationContext.getBean(DispatcherServlet.class);
    }

    public APIGatewayProxyResponseEvent handle(APIGatewayProxyRequestEvent requestEvent) {
        try {
            // Convert API Gateway request to Spring request
            MockHttpServletRequest request = createSpringRequest(requestEvent);
            MockHttpServletResponse response = new MockHttpServletResponse();

            // Process the request
            dispatcherServlet.service(request, response);

            // Convert Spring response to API Gateway response
            return createApiGatewayResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse(e);
        }
    }

    private MockHttpServletRequest createSpringRequest(APIGatewayProxyRequestEvent requestEvent) {
        String path = requestEvent.getPath();
        String httpMethod = requestEvent.getHttpMethod();

        // Create a new Spring mock request
        MockHttpServletRequest request = new MockHttpServletRequest(httpMethod, path);

        // Handle query parameters
        Map<String, String> queryParams = requestEvent.getQueryStringParameters();
        if (queryParams != null) {
            queryParams.forEach(request::addParameter);
        }

        // Set headers
        Map<String, String> headers = requestEvent.getHeaders();
        if (headers != null) {
            headers.forEach(request::addHeader);
        }

        // Set body
        String body = requestEvent.getBody();
        if (body != null) {
            request.setContent(body.getBytes());
        }

        // Set context path for proper Spring MVC URL handling
        request.setContextPath("");
        request.setServletPath(path);

        return request;
    }

    private APIGatewayProxyResponseEvent createApiGatewayResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
        APIGatewayProxyResponseEvent apiGatewayResponse = new APIGatewayProxyResponseEvent();

        // Set status code
        apiGatewayResponse.setStatusCode(response.getStatus());

        // Set response body
        String body = response.getContentAsString();
        apiGatewayResponse.setBody(body);

        // Set headers without CORS
        Map<String, String> responseHeaders = new HashMap<>();

        // Add content type
        responseHeaders.put("Content-Type", response.getContentType());

        // Copy other headers
        for (String headerName : response.getHeaderNames()) {
            responseHeaders.put(headerName, response.getHeader(headerName));
        }

        apiGatewayResponse.setHeaders(responseHeaders);

        return apiGatewayResponse;
    }

    private APIGatewayProxyResponseEvent createErrorResponse(Exception e) {
        APIGatewayProxyResponseEvent errorResponse = new APIGatewayProxyResponseEvent();
        errorResponse.setStatusCode(500);
        errorResponse.setBody("{\"error\":\"" + e.getMessage() + "\"}");

        // Add only Content-Type header
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        errorResponse.setHeaders(headers);

        return errorResponse;
    }
}