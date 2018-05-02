package practice.restapi.restapidemo.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
 import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.inject.Singleton;


/**
 * Generic exception mapper for any uncaught exceptions that don't have
 * specific mappers assigned to them.
 */
@Singleton
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    /** Response status to use for this exception 500 (INTERNAL_SERVER_ERROR) */
    public static final Response.Status RESPONSE_STATUS
            = Response.Status.INTERNAL_SERVER_ERROR;
    
    /**
     * Override the mapping to the REST response. This will return a
     * 500 (INTERNAL_SERVER_ERROR) status code.
     *
     * @param exception Exception to report in the response.
     * @return Response REST response object.
     */
    public Response toResponse(Throwable ex) {
        if (ex instanceof WebApplicationException) {
            return ((WebApplicationException) ex).getResponse();
        }
        ErrorMessage em = new ErrorMessage(
                RESPONSE_STATUS, true, 
                "Unexpected error Occurred" + ex);
        return Response.status(RESPONSE_STATUS)
                       .entity(em)
                       .type(MediaType.APPLICATION_JSON)
                       .build();
    }
}
