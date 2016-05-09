package modkiwi.net.exceptions;

import java.io.IOException;

public class UnexpectedResponseCodeException extends IOException {
    private int code;

    public UnexpectedResponseCodeException(int responseCode) {
        super("Unexpected response code: " + responseCode);
        this.code = responseCode;
    }

    public int getResponseCode() {
        return code;
    }
}
