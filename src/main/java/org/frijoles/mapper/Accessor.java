package org.frijoles.mapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

public class Accessor {

    final Class<?> beanClass;
    // id.login
    final String propertyName;
    final String[] propertyNameParts;
    final List<PropertyDescriptor> propertyPath = new ArrayList<>();

    public Accessor(Class<?> beanClass, String propertyName) {
        super();
        this.propertyName = propertyName;
        this.beanClass = beanClass;
        this.propertyNameParts = propertyName.split("\\.");

        try {
            Class<?> c = beanClass;
            for (String part : propertyNameParts) {
                PropertyDescriptor pd = findProp(c, part);
                this.propertyPath.add(pd);
                c = pd.getPropertyType();
            }
        } catch (Exception e) {
            throw new RuntimeException("describing " + beanClass.getName() + "#" + propertyName, e);
        }
    }

    protected static PropertyDescriptor findProp(Class<?> beanClass, String propertyName) {
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
            if (pd.getName().equals(propertyName)) {
                return pd;
            }
        }
        throw new RuntimeException("property not found: '" + beanClass.getName() + "#" + propertyName + "'");
    }

    public Object get(Object bean) {
        return get(bean, 0);
    }

    public void set(Object bean, Object propertyValue) {
        set(bean, 0, propertyValue);
    }

    public Object get(Object bean, int startIndex) {
        try {
            Object o = bean;
            for (int i = startIndex; i < propertyPath.size(); i++) {
                o = propertyPath.get(i).getReadMethod().invoke(o);
                if (o == null) {
                    return null;
                }
            }
            return o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Object bean, int startIndex, Object propertyValue) {
        try {
            Object o = bean;
            for (int i = startIndex; i < propertyPath.size() - 1; i++) {
                PropertyDescriptor p = propertyPath.get(i);
                Object o2 = p.getReadMethod().invoke(o);
                if (o2 == null) {
                    o2 = p.getPropertyType().newInstance();
                    p.getWriteMethod().invoke(o, o2);
                }
                o = o2;
            }
            propertyPath.get(propertyPath.size() - 1).getWriteMethod().invoke(o, propertyValue);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Class<?> getPropertyFinalType() {
        return propertyPath.get(propertyPath.size() - 1).getPropertyType();
    }

    @Override
    public String toString() {
        return beanClass + "#" + propertyName;
    }

}