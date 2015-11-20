package endpointstest;

/**
 * Created by marco on 11/20/15.
 */
public class AdWordsException extends Exception {
    public AdWordsException() {
    }

    public AdWordsException(String message) {
        super(message);
    }

    public AdWordsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AdWordsException(Throwable cause) {
        super(cause);
    }

    public AdWordsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
