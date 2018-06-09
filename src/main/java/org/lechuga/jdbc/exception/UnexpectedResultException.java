package org.lechuga.jdbc.exception;

public class UnexpectedResultException extends RuntimeException {

    private static final long serialVersionUID = 6193212451419056030L;

    public UnexpectedResultException(String message) {
        super(message);
    }

    public UnexpectedResultException(Exception e) {
        super(e);
    }

}
