package practice.restapi.restapidemo.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import practice.restapi.restapidemo.error.ErrorMessage;
import practice.restapi.restapidemo.tokenmgmt.TokenManagement;
import practice.restapi.restapidemo.v1.TokenService;

public class BaseService {
    
    /** HTTP headers, used to get the session token for authentication. */
    @Context
    private HttpHeaders httpHeaders;

    /** Incoming HTTP request, for error logging purposes. */
    @Context
    protected HttpServletRequest request;
    
    public boolean validateToken(){
        boolean authenticated = false;
        String token  = getToken();
        if(TokenManagement.getUserTokens().contains(token)){
            authenticated = true;
        } else {
            throwAuthenticationError("Not a Valid Token");
        }
        return authenticated;
    }

    /**
     * @param message Error message indicating specific error condition.
     * @throws NotAuthorizedException Generates 401 error response.
     */
    protected void throwAuthenticationError(String message)
            throws NotAuthorizedException {
        Response.Status status = Response.Status.UNAUTHORIZED;
        // not transient; will fail again if identical request made again
        // without either first logging in or putting right token into header
        ErrorMessage em = new ErrorMessage(status, false, message);
        Response response = Response.status(status)
                                    .entity(em)
                                    .type(MediaType.APPLICATION_JSON)
                                    .language("en")
                                    .build();
        throw new NotAuthorizedException(response);
    }
    
    /**
     * @param message Error message indicating specific error condition.
     * @param e Some exception we want to treat as a bad request, for
     *        logging purposes only.
     * @throws BadRequestException Generates 400 error response.
     */
    protected void throwBadRequestError(String message, Exception e)
            throws BadRequestException {
        Response.Status status = Response.Status.BAD_REQUEST;
        // if user tries same request again (without correcting some input
        // in the request), it would fail again
        ErrorMessage em = new ErrorMessage(status, false, message);
        Response response = Response.status(status)
                                    .entity(em)
                                    .type(MediaType.APPLICATION_JSON)
                                    .language("en")
                                    .build();
        throw new BadRequestException(response);
    }
    
    /**
     * @param e The unexpected exception that occurred.
     * @throws InternalServerErrorException Generates 500 error response.
     */
    protected void throwUnexpectedError(Exception e)
            throws InternalServerErrorException {
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        // it's likely to be transient, since it is unexpected
        ErrorMessage em = new ErrorMessage(status, true,
            "UnExpected Error occured" + e.getMessage());
        Response response = Response.status(status)
                                    .entity(em)
                                    .type(MediaType.APPLICATION_JSON)
                                    .language("en")
                                    .build();
        throw new InternalServerErrorException(response, e);
    }
    
    /**
     * @throws NotFoundException Generates 404 error response.
     */
    protected void throwNotFoundError() throws NotFoundException {
        Response.Status status = Response.Status.NOT_FOUND;
        // if user tries same data again, it would fail again, so it's not a
        // transient server condition
        ErrorMessage em = new ErrorMessage(status, false, null);
        Response response = Response.status(status)
                                    .entity(em)
                                    .type(MediaType.APPLICATION_JSON)
                                    .language("en")
                                    .build();
        throw new NotFoundException(response);
    }
    
    
    /**
     * @return Bearer session token from the HTTP request header. Will be null
     *         if request is missing an Authorization header or if the value
     *         doesn't start with "Bearer ".
     */
    protected String getToken() {
        List<String> tokens = httpHeaders.getRequestHeader(
                TokenService.REQUEST_HEADER_AUTHORIZATION);
        if (tokens == null || tokens.isEmpty() || tokens.get(0) == null) {
            return null;
        }
        String token = tokens.get(0).trim();
        if (token.startsWith(TokenService.TOKEN_TYPE_BEARER + " ")) {
            token = token.substring(TokenService.TOKEN_TYPE_BEARER.length())
                         .trim();
            if (token.length() == 0) {
                return null;
            }
            return token;
        }
        return null;
    }
}
