package org.frijoles.jdbc.exception;

public class EmptyResultException extends UnexpectedResultException {

    private static final long serialVersionUID = 8727129333282283655L;

    public EmptyResultException(final String message) {
        super(message);
    }

}
