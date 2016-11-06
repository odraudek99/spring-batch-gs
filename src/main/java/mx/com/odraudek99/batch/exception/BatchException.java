package mx.com.odraudek99.batch.exception;

public class BatchException extends Exception {

    private static final long serialVersionUID = 1L;

    private Exception exception;

    public BatchException() {
        super();
    }

    public BatchException(final String message) {
        super(message);
    }

    public BatchException(final String message, Exception exception) {
        super(message);
        this.exception = exception;
    }

    public BatchException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public BatchException(final Throwable cause) {
        super(cause);
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

}