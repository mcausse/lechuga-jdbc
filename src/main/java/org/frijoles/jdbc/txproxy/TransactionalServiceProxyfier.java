package org.frijoles.jdbc.txproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.frijoles.jdbc.DataAccesFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionalServiceProxyfier implements InvocationHandler {

    static final Logger LOG = LoggerFactory.getLogger(TransactionalServiceProxyfier.class);

    final Object target;
    final DataAccesFacade facade;

    protected TransactionalServiceProxyfier(Object target, DataAccesFacade facade) {
        this.target = target;
        this.facade = facade;
    }

    @SuppressWarnings("unchecked")
    public static <T> T proxyfy(DataAccesFacade facade, T target, Class<? super T> serviceInterface) {
        return (T) Proxy.newProxyInstance(serviceInterface.getClassLoader(), new Class<?>[] { serviceInterface },
                new TransactionalServiceProxyfier(target, facade));
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method m;

        TransactionalMethod anno = method.getAnnotation(TransactionalMethod.class);
        if (anno == null) {
            m = target.getClass().getMethod(method.getName(), method.getParameterTypes());
            anno = m.getAnnotation(TransactionalMethod.class);
        }

        if (anno == null) {
            return method.invoke(target, args);
        } else if (anno.propagation() == EPropagation.NONE) {
            return method.invoke(target, args);
        } else if (anno.propagation() == EPropagation.CREATE_OR_REUSE) {
            if (facade.isValidTransaction()) {
                return method.invoke(target, args);
            } else {
                return executeInTransaction(method, args, anno.readOnly());
            }
        } else if (anno.propagation() == EPropagation.NEW) {
            return executeInTransaction(method, args, anno.readOnly());
        } else {
            throw new RuntimeException(anno.propagation().name());
        }

    }

    public Object executeInTransaction(Method method, Object[] args, boolean readOnly)
            throws IllegalAccessException, InvocationTargetException, Exception {
        if (readOnly) {
            facade.begin();
            try {
                return method.invoke(target, args);
            } finally {
                facade.rollback();
            }
        } else {
            facade.begin();
            try {
                Object r = method.invoke(target, args);
                facade.commit();
                return r;
            } catch (Exception e) {
                facade.rollback();
                throw e;
            }
        }
    }

}