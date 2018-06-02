package org.moncheta.mapper;

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

    // public static <T> T newInstance(final Class<T> beanClass, final String[]
    // args) throws RuntimeException {
    //
    // final Class<?>[] argTypes = new Class<?>[args.length];
    // for (int i = 0; i < args.length; i++) {
    // argTypes[i] = String.class;
    // }
    //
    // try {
    // final Constructor<T> ctor = beanClass.getDeclaredConstructor(argTypes);
    // return ctor.newInstance((Object[]) args);
    // } catch (final Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    //
    // static String getGetterName(final String propName) {
    // if (Character.isUpperCase(propName.charAt(0)) || propName.length() > 1
    // && Character.isUpperCase(propName.charAt(1))) {
    // return "get" + propName;
    // }
    // return "get" + Character.toUpperCase(propName.charAt(0)) +
    // propName.substring(1);
    // }
    //
    // static String getIsGetterName(final String propName) {
    // if (Character.isUpperCase(propName.charAt(0)) || propName.length() > 1
    // && Character.isUpperCase(propName.charAt(1))) {
    // return "is" + propName;
    // }
    // return "is" + Character.toUpperCase(propName.charAt(0)) +
    // propName.substring(1);
    // }
    //
    // static String getSetterName(final String propName) {
    // if (Character.isUpperCase(propName.charAt(0)) || propName.length() > 1
    // && Character.isUpperCase(propName.charAt(1))) {
    // return "set" + propName;
    // }
    // return "set" + Character.toUpperCase(propName.charAt(0)) +
    // propName.substring(1);
    // }
    //
    // //
    // // ////////////////////////////////
    // //
    //
    // public static Method getGetterMethod(final Class<?> targetClass, final String
    // field)
    // throws RuntimeException {
    // Method m;
    // try {
    // m = targetClass.getMethod(getGetterName(field));
    // } catch (final Exception e) {
    // try {
    // m = targetClass.getMethod(getIsGetterName(field));
    // } catch (final Exception e1) {
    // throw new RuntimeException("no getter found for: " + targetClass.getName() +
    // "#" + field, e);
    // }
    // }
    // return m;
    // }
    //
    // public static Method getSetterMethod(final Class<?> targetClass, final String
    // field)
    // throws RuntimeException {
    // Method m = null;
    // try {
    // final String setterName = getSetterName(field);
    // for (final Method me : targetClass.getMethods()) {
    // if (me.getName().equals(setterName) && me.getReturnType().equals(void.class)
    // && me.getParameterTypes().length == 1) {
    // m = me;
    // break;
    // }
    // }
    // if (m == null) {
    // throw new RuntimeException();
    // }
    // } catch (final Exception e) {
    // throw new RuntimeException("no setter found for: " + targetClass.getName() +
    // "#" + field, e);
    // }
    // return m;
    // }

}
