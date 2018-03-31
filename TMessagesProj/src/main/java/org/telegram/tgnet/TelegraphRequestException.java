package org.telegram.tgnet;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Ruben Bermudez
 * @version 1.0
 */
public class TelegraphRequestException extends TelegraphException {
    private static final String ERRORFIELD = "error";

    private String error;

    public TelegraphRequestException(String message) {
        super(message);
    }

    public TelegraphRequestException(String message, JSONObject object) throws JSONException {
        super(message);
        error = object.getString(ERRORFIELD);
    }

    public TelegraphRequestException(String message, TelegraphResponse response) {
        super(message);
        error = response.getError();
    }

    public TelegraphRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String toString() {
        if (error == null) {
            return super.toString();
        } else {
            return super.toString() + ": [" + error + "] ";
        }
    }
}
