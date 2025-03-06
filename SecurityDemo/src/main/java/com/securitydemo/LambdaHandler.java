package com.securitydemo;

import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;
import org.springframework.stereotype.Component;

@Component
public class LambdaHandler extends SpringBootRequestHandler<Object, Object> {
}
