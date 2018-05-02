package practice.restapi.restapidemo.test;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import practice.restapi.restapidemo.tokenmgmt.TokenManagement;
import practice.restapi.restapidemo.v1.TokenService;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
    TokenManagement.class
})
public class TokenServiceTest extends RestApiUnitTest {

    @Override
    protected Class<?> getRestApiClass() {
        return TokenService.class;
    }
    
    @Test
    public void create_token_succeded() throws IOException{
        PowerMockito.mockStatic(TokenManagement.class);
        TokenManagement tokenManagement = PowerMockito.mock(TokenManagement.class);
        PowerMockito.when(tokenManagement.createToken()).thenReturn("VALID_TOKEN");
        
        Response response = target("token").request().post(
                Entity.entity(
                "grant_type=password&username=admin&password=password",
                "application/x-www-form-urlencoded"));
        
        Assert.assertEquals("should succeed", 200, response.getStatus());
        Assert.assertEquals("{\"access_token\":\"VALID_TOKEN\","
            + "\"expires_in\":2000,"
            + "\"token_type\":\"Bearer\"}",
            readResponseBody(response));
    }
    
    

}
