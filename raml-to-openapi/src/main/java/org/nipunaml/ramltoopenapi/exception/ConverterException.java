package org.nipunaml.ramltoopenapi.exception;

/**
 * Custom exception for converter-related errors.
 */
public class ConverterException extends Exception {
    
    public ConverterException(String message) {
        super(message);
    }
    
    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConverterException(Throwable cause) {
        super(cause);
    }
}
