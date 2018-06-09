package org.frijoles.jdbc.exception;

public class FrijolesException extends RuntimeException {

    private static final long serialVersionUID = 8727129333282283655L;

    public FrijolesException() {
        super();
    }

    public FrijolesException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FrijolesException(final String message) {
        super(message);
    }

    public FrijolesException(final Throwable cause) {
        super(cause);
    }

}
