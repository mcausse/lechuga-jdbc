package org.fijoles.annotated.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.fijoles.mapper.handler.Handler;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CustomHandler {

    Class<? extends Handler> value();

    String[] args() default {};
}