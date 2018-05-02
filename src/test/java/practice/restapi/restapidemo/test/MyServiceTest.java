package practice.restapi.restapidemo.test;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import practice.restapi.restapidemo.tokenmgmt.TokenManagement;
import practice.restapi.restapidemo.v1.MyServices;
import practice.restapi.restapidemo.v1.Student;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(PowerMockRunner.class)
@PrepareForTest({
    TokenManagement.class,
    MyServices.class
})
public class MyServiceTest extends RestApiUnitTest {
    
    /** The version we are testing, for use in the service path. */
    private static final String VERSION = "v1.0";
    /** The path to the myservices service. */
    private static final String SERVICE = VERSION + "/myservices";
    /** The base url to the simulated http server. */
    private static final String BASE_URL = "http://localhost:9998/" + VERSION;
    
    @Override
    protected Class<?> getRestApiClass() {
        return MyServices.class;
    }
    
    @Test
    public void getStudent_succeeds() throws Exception {
        loginPrerequisite();
        String text = "{\"name\":\"abc\","
            + "\"rollno\":1,"
            + "\"totalMarks\":200}" ;

        Response response = target(SERVICE + "/1")
            .request()
            .header("Authorization", "Bearer VALID-TOKEN")
            .get();
        Assert.assertEquals("should have expected success code",
            200, response.getStatus());
        Assert.assertEquals("should have JSON content type",
            "application/json", response.getHeaderString("Content-Type"));
        Assert.assertEquals(text, readResponseBody(response));
    }
    
    @Test
    public void zetStudents_succeeds() throws Exception {
        loginPrerequisite();
        PowerMockito.mockStatic(MyServices.class);
        MyServices myServices = PowerMockito.mock(MyServices.class);
        List<Student> stuList = new ArrayList<Student>();
        Student s1 = new Student();
        s1.setName("abc");
        s1.setRollno(1);
        s1.setTotalMarks(200);
        stuList.add(s1);
        s1 = new Student();
        s1.setName("def");
        s1.setRollno(2);
        s1.setTotalMarks(400);
        stuList.add(s1);
        PowerMockito.when(MyServices.getStudentList()).thenReturn(stuList);
        String text = "[{\"name\":\"abc\","
            + "\"rollno\":1,"
            + "\"totalMarks\":200},"
            + "{\"name\":\"def\","
            + "\"rollno\":2,"
            + "\"totalMarks\":400}]" ;

        Response response = target(SERVICE)
            .request()
            .header("Authorization", "Bearer VALID-TOKEN")
            .get();
        Assert.assertEquals("should have expected success code",
            200, response.getStatus());
        Assert.assertEquals("should have JSON content type",
            "application/json", response.getHeaderString("Content-Type"));
        Assert.assertEquals(text, readResponseBody(response));
    }
    
    @Test
    public void createStudents_succeeds() throws Exception {
        loginPrerequisite();
        String text = "{\"name\":\"mno\","
            + "\"rollno\":4,"
            + "\"totalMarks\":800}";
        Response response = target("v1.0/myservices")
            .request()
            .header("Authorization", "Bearer VALID-TOKEN")
            .post(Entity.entity(text, MediaType.APPLICATION_JSON));
        Assert.assertEquals("should have expected success code",
            201, response.getStatus());
        Assert.assertEquals("should have JSON content type",
            "text/plain", response.getHeaderString("Content-Type"));
        Assert.assertEquals("should have expected URL in header",
                BASE_URL + "/myservices/4",
                response.getHeaderString("Location"));
    }
    
    @Test
    public void updateStudents_succeeds() throws Exception {
        loginPrerequisite();
        String text = "{\"name\":\"abc123\","
            + "\"rollno\":3}";
        Response response = target(SERVICE + "/3")
            .request()
            .header("Authorization", "Bearer VALID-TOKEN")
            .put(Entity.entity(text, MediaType.APPLICATION_JSON));
        Assert.assertEquals("should have expected success code",
            200, response.getStatus());
        Assert.assertEquals("should have JSON content type",
                "application/json", response.getHeaderString("Content-Type"));
        String output = "{\"name\":\"abc123\","
                + "\"rollno\":3,"
                + "\"totalMarks\":0}";
            Assert.assertEquals(output, readResponseBody(response));
    }
    
    @Test
    public void patchStudents_succeeds() throws Exception {
        loginPrerequisite();
        String text = "[{\"op\":\"replace\",\"path\":\"/name\","
            + "\"value\":\"modified-name\"},"
            + "{\"op\":\"replace\", \"path\":\"/totalMarks\","
            + "\"value\":800}]";
        Response response = target(SERVICE + "/3")
            .request()
            .header("Authorization", "Bearer VALID-TOKEN")
            .build("PATCH", Entity.entity(text, "application/json-patch+json"))
            .invoke();
        Assert.assertEquals("should have expected success code",
            200, response.getStatus());
        Assert.assertEquals("should have JSON content type",
                "application/json", response.getHeaderString("Content-Type"));
        String output = "{\"name\":\"modified-name\","
                + "\"rollno\":3,"
                + "\"totalMarks\":800}";
            Assert.assertEquals(output, readResponseBody(response));
    }
    
    @Test
    public void deleteStudents_succeeds() throws Exception {
        loginPrerequisite();
        Response response = target(SERVICE + "/2")
            .request()
            .header("Authorization", "Bearer VALID-TOKEN")
            .delete();
        Assert.assertEquals("should have expected success code",
                200, response.getStatus());
        Assert.assertEquals("Record Deleted succesfully",
                readResponseBody(response));
        Assert.assertEquals("should have text content type", "text/plain",
                response.getHeaderString("Content-Type"));
    }

}
