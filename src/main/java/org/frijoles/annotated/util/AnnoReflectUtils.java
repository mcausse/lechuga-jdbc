package org.frijoles.annotated.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnoReflectUtils {

    public static Map<String, Field> getFields(Class<?> beanClass) {
        return getFields("", beanClass);
    }

    protected static Field findField(Class<?> beanClass, String propertyName) {

        Class<?> o = beanClass;
        while (o != null) {

            try {
                Field f = beanClass.getDeclaredField(propertyName);
                return f;
            } catch (NoSuchFieldException e) {
            }

            o = o.getSuperclass();
        }

        throw new RuntimeException("field not found: " + beanClass.getName() + "#" + propertyName);
    }

    protected static Map<String, Field> getFields(String prefix, Class<?> beanClass) {
        Map<String, Field> r = new LinkedHashMap<>();

        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(beanClass);
        } catch (IntrospectionException e) {
            throw new RuntimeException("describing " + beanClass.getName(), e);
        }
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            if (pd.getName().equals("class") || pd.getName().contains("$")) {
                continue;
            }

            Field field = findField(beanClass, pd.getName());

            final String prefix2;
            if (prefix.isEmpty()) {
                prefix2 = field.getName();
            } else {
                prefix2 = prefix + "." + field.getName();
            }

            Class<?> t = field.getType();
            if (t.isPrimitive() || t.isEnum() || t.getPackage().getName().startsWith("java.lang")
                    || t.getPackage().getName().startsWith("java.util")) {
                r.put(prefix2, field);
            } else {
                r.putAll(getFields(prefix2, t));
            }
        }

        return r;
    }

}
