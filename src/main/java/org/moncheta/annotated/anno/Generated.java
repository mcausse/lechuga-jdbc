package org.moncheta.annotated.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.moncheta.mapper.autogen.Generator;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Generated {

    Class<? extends Generator> value();

    String[] args() default {};
}