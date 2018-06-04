package org.fijoles.jdbc.exception;

public class TooManyResultsException extends UnexpectedResultException {

    private static final long serialVersionUID = 677361547286326224L;

    public TooManyResultsException(final String message) {
        super(message);
    }

}
