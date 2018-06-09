package org.lechuga.jdbc.exception;

public class LechugaException extends RuntimeException {

    private static final long serialVersionUID = 8727129333282283655L;

    public LechugaException() {
        super();
    }

    public LechugaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public LechugaException(final String message) {
        super(message);
    }

    public LechugaException(final Throwable cause) {
        super(cause);
    }

}
