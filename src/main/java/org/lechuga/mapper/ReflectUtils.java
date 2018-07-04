package org.lechuga.mapper;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import org.lechuga.jdbc.exception.LechugaException;

public class ReflectUtils {

    public static <T> T newInstance(final Class<T> beanClass) throws RuntimeException {
        T entity;
        try {
            entity = beanClass.newInstance();
        } catch (final Exception e) {
            throw new LechugaException("instancing: " + beanClass.getName(), e);
        }
        return entity;
    }

    public static <T> T newInstance(final Class<T> beanClass, final String[] args) throws RuntimeException {

        final Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = String.class;
        }

        try {
            final Constructor<T> ctor = beanClass.getDeclaredConstructor(argTypes);
            return ctor.newInstance((Object[]) args);
        } catch (final Exception e) {
            throw new LechugaException("instancing: " + beanClass.getName() + "[" + Arrays.toString(argTypes) + "]", e);
        }
    }

}
