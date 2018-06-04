package org.fijoles.mapper.util;

import java.lang.reflect.Constructor;

public class ReflectUtils {

    @SuppressWarnings("unchecked")
    public static <T> Class<T> classFor(final String beanClassName) throws RuntimeException {
        try {
            return (Class<T>) Class.forName(beanClassName);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

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
