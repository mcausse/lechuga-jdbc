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

    protected static Field getField(Class<?> beanClass, String name) {
        Class<?> o = beanClass;
        while (o != null) {
            for (Field f : o.getDeclaredFields()) {
                if (name.equals(f.getName())) {
                    return f;
                }
            }
            o = o.getSuperclass();
        }
        throw new RuntimeException("field not readable? " + beanClass.getName() + "#" + name);
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

            final String prefix2;
            if (prefix.isEmpty()) {
                prefix2 = pd.getName();
            } else {
                prefix2 = prefix + "." + pd.getName();
            }

            Class<?> t = pd.getPropertyType();
            if (t.isPrimitive() || t.isEnum() || t.getPackage().getName().startsWith("java.lang")
                    || t.getPackage().getName().startsWith("java.util")) {
                r.put(prefix2, getField(beanClass, pd.getName()));
            } else {
                r.putAll(getFields(prefix2, t));
            }
        }

        return r;
    }

}
