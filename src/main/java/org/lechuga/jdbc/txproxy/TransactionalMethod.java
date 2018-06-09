package org.lechuga.jdbc.txproxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TransactionalMethod {

    boolean readOnly() default false;

    EPropagation propagation() default EPropagation.CREATE_OR_REUSE;

}