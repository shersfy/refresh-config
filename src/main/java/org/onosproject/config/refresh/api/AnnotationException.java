package org.onosproject.config.refresh.api;

public class AnnotationException extends RuntimeException{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public AnnotationException() {
        super();
    }

    public AnnotationException(String message, Throwable cause, boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AnnotationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnnotationException(String message) {
        super(message);
    }

    public AnnotationException(Throwable cause) {
        super(cause);
    }
    

}
