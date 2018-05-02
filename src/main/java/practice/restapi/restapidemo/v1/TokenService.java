package practice.restapi.restapidemo.v1;

import java.io.Serializable;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import practice.restapi.restapidemo.service.BaseService;
import practice.restapi.restapidemo.tokenmgmt.TokenManagement;

//Call this API using URL http://localhost:8020/MyRESTAPIPractice/rest/token
@Path("/token")
@Api(tags = "tokenservice")
public class TokenService extends BaseService{

    /** The type of login that we accept (as a "grant_type"). */
    public static final String GRANT_TYPE_PASSWORD = "password";

    /** The type of token we accept in Authorization header. */
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    /** Request header that holds our token. */
    public static final String REQUEST_HEADER_AUTHORIZATION = "Authorization";
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Api to validate user and if user is valid return a token")
        @ApiResponses(
            value = {
                @ApiResponse(code = 500,
                    message = "Internal server error: Unexpected exception occurred")
            }
        )
    public TokenInformation getToken(@FormParam("grant_type") @ApiParam(value = "The type of login being performed",
            allowableValues = "password",
            required = true)
            String grantType,
            @FormParam("username") @ApiParam(value = "The username of who is logging in",
            required = true)
            String username, 
            @FormParam("password") @ApiParam(value = "The password of who is logging in",
            required = true) String password) {
        TokenInformation token = null;
        if (grantType.equals("password") && username.equals("admin") && password.equals("password")) {
           token = new TokenInformation();
           token.accessToken = TokenManagement.createToken();
        } else {
            throwAuthenticationError("Authentication failed");
        }
        return token;
    }
    
    
    
    class TokenInformation implements Serializable {
        // JSON property names conform to BMC standard
        /** The identifying user token. */
        @JsonProperty("access_token")
        private String accessToken;

        /** How long the token/session can sit idle, in milliseconds. */
        @JsonProperty("expires_in")
        private  long expiresIn = 2000;

        /** OAuth2 token type, which is always "Bearer" in our case. */
        @JsonProperty("token_type")
        private String tokenType = TOKEN_TYPE_BEARER;

        /** @return A stringified version of this SessionInfomation. */
        public String toString() {
            return "{ accessToken=" + accessToken
                    + ", expiresIn=" + expiresIn
                    + ", tokenType=" + tokenType
                    + " }";
        }
    }
}
