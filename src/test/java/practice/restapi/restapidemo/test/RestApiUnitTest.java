
package practice.restapi.restapidemo.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.grizzly2.servlet.GrizzlyWebContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Assert;
import org.powermock.api.mockito.PowerMockito;

import practice.restapi.restapidemo.error.GenericExceptionMapper;
import practice.restapi.restapidemo.tokenmgmt.TokenManagement;


/** Base class for REST API test classes, with various utilities. */
public abstract class RestApiUnitTest extends JerseyTest  {

    /** @return The REST API class under test. */
    protected abstract Class<?> getRestApiClass();

    protected void loginPrerequisite() {
        PowerMockito.mockStatic(TokenManagement.class);
        TokenManagement tokenMgmt = PowerMockito.mock(TokenManagement.class);
        List<String> tokenList = new ArrayList<String>();
        tokenList.add("VALID-TOKEN");
        PowerMockito.when(tokenMgmt.getUserTokens()).thenReturn(tokenList);
    }
    
    /**
     * @param response Response from the service.
     * @return Text from the response body.
     * @throws IOException When unabble to read response body as stream.
     */
    protected String readResponseBody(Response response) throws IOException {
        InputStream input = (InputStream) response.getEntity();
        int len = input.available();
        if (len == 0) {
            return null;
        }
        byte[] bytes = new byte[len];
        input.read(bytes, 0, len);
        String responseStr = new String(bytes);
        return responseStr;
    }

    /**
     * Veriries response holds our JSON-formatted ErrorMessage object.
     * @param response Response from the service.
     * @param code Expected http error status code.
     * @param transientFlag Expected transient flag value.
     * @param message Expected error message.
     * @throws IOException When unabble to read response body as stream.
     */
    protected void assertErrorMessage(Response response, int code,
            boolean transientFlag, String message) throws IOException {
        Assert.assertEquals("should have expected error code",
             code, response.getStatus());
        String result = readResponseBody(response);
        Assert.assertTrue(
            "should get expected JSON error structure, but got: " + result,
            result.matches("\\{"
            + "\"message\":\"\\Q" + message + "\\E\","
            + "\"code\":" + code + ","
            + "\"serverLogTag\":\"[A-Z0-9\\-]{36}\","    // a GUID
            + "\"transient\":" + transientFlag + "\\}"));
    }

    /**
     * Sets up JerseyTest to dispatch to the methods under test and to pass
     * unhandled exceptions through my exception handler.
      * @return Application ready to exercide the test class.
     */
    @Override
    protected Application configure() {
        return new ResourceConfig(getRestApiClass())
                .register(new GenericExceptionMapper());
    }

    /** Hack to make PATCH methods get called. */
    @Override
    protected void configureClient(final ClientConfig config) {
        config.property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true);
    }

    /**
     * Code found here (tweaked a little):
     * http://stackoverflow.com/questions/17973277/problems-running-jerseytest-when-dealing-with-httpservletresponse
     */
    @Override
    public TestContainerFactory getTestContainerFactory()
            throws TestContainerException {
        return new TestContainerFactory() {
            public TestContainer create(final URI baseUri,
                    DeploymentContext deploymentContext) {
                return new TestContainer() {
                    private HttpServer server;
                    public ClientConfig getClientConfig() {
                        return null;
                    }
                    public URI getBaseUri() {
                        return baseUri;
                    }
                    public void start() {
                        HashMap<String, String> initParams
                                = new HashMap<String, String>();
                        // classes with @Path or @Provider annotations
                        initParams.put(
                                ServerProperties.PROVIDER_CLASSNAMES,
                                GenericExceptionMapper.class.getName() + ";"
                                + getRestApiClass().getName());
                        for (int i = 0; i < 10; i++) {
                            try {
                                this.server = GrizzlyWebContainerFactory.create(
                                        baseUri, initParams);
                                break;
                            } catch (ProcessingException e) {
                                if (i == 9) {    // last attempt
                                    throw new TestContainerException(e);
                                }
                                if (e.getMessage().contains(
                                        "Address already in use")) {
                                    // sometimes there is some weird random race
                                    // condition, so just retry in a loop
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ie) { }
                                } else {
                                    throw new TestContainerException(e);
                                }
                            } catch (IOException e) {
                                throw new TestContainerException(e);
                            }
                        }
                    }
                    public void stop() {
                        this.server.shutdownNow();
                    }
                };
            }
        };
    }
}
