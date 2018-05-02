package practice.restapi.restapidemo.error;

import javax.ws.rs.core.Response;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean class that holds the data that will be returned to the client if there
 * is an error during a REST call.
 */
public class ErrorMessage {

    /** User-facing localized error message. */
    private String message;

    /** Standard HTTP status code (e.g. 400 or 500). */
    private int code;

    /**
     * Whether or not this error condition is temporary.
     */
    @JsonProperty("transient")
    private boolean transientFlag;

    /**
     * A GUID that appears with the log message containing a stack trace
     * in the server log file, when logging is enabled for the error; user
     * would be able to locate any full stack trace using this GUID.
     */
    private String serverLogTag;

    /**
     * @param status The response code being reported to the client.
     * @param transientFlag Value to set the transientFlag.
     * @param message Value to set the error message description.
     */
    public ErrorMessage(Response.Status status, boolean transientFlag,
            String message) {
        this.transientFlag = transientFlag;
        this.code = status.getStatusCode();
        this.message = this.code + " " + status.getReasonPhrase();
        if (message != null) {
            this.message += ": " + message;
        }
    }

    /** @return The current value of the message attribute. */
    public String getMessage() {
        return message;
    }

    /** @return The current value of the code attribute. */
    public int getCode() {
        return code;
    }

    /** @return The current value of the transientFlag attribute. */
    public boolean getTransientFlag() {
        return transientFlag;
    }

    /** @return The current value of the serverLogTag attribute. */
    public String getServerLogTag() {
        return serverLogTag;
    }

    /** @return A stringified version of this ErrorMessage. */
    public String toString() {
        return "{ code=" + code
                + ", message=" + message
                + ", serverLogTag=" + serverLogTag
                + ", transientFlag=" + transientFlag
                + " }";
    }
}
