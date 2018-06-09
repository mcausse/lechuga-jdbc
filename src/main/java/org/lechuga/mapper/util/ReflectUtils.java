package org.lechuga.mapper.util;

import java.lang.reflect.Constructor;

public class ReflectUtils {

    public static <T> T newInstance(final Class<T> beanClass) throws RuntimeException {
        T entity;
        try {
            entity = beanClass.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

}
