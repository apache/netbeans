package com.example.expression;

import jakarta.inject.Singleton;
import io.micronaut.context.annotation.AnnotationExpressionContext;

@Singleton
@CustomAnnotation(value = "#{}")
class Example {
}

@Singleton
class AnnotationContext {
    public String firstValue() {
        return "first value";
    }
    public String getUser() {
        return "Admin";
    }
}

@Singleton
class AnnotationMemberContext {
    public String secondValue() {
        return "second value";
    }
}

@AnnotationExpressionContext(AnnotationContext.class)
@interface CustomAnnotation {
    @AnnotationExpressionContext(AnnotationMemberContext.class)
    String value();
}
