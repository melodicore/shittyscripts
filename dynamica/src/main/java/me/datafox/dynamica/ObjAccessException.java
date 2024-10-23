package me.datafox.dynamica;

/**
 * Thrown if a field or a function of an object cannot be accessed.
 *
 * @author datafox
 */
public class ObjAccessException extends RuntimeException {
    public ObjAccessException() {
        super();
    }

    public ObjAccessException(String message) {
        super(message);
    }

    public ObjAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ObjAccessException(Throwable cause) {
        super(cause);
    }

    protected ObjAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
