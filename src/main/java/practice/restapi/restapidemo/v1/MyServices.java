package practice.restapi.restapidemo.v1;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import practice.restapi.restapidemo.service.BaseService;

// Call this API using URL http://localhost:8020/MyRESTAPIPractice/rest/v1.0/myservices
@Path("/v1.0/myservices")
@Api(tags = "myservices")
public class MyServices extends BaseService{
    
private static List<Student> stuList = null;

    static {
        stuList = getStudentList();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch list of students present in the system.")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name="Authorization", value="Authorization token in format Bearer [token value]",
                    required=true,dataType="String", paramType="header")
    })
    @ApiResponses(value = {
            @ApiResponse(code=401, message = "Unauthorized: User is not authorized to access the application"),
            @ApiResponse(code=500, message="Internal server error: Unexpected exception occurred")
    })
    public Response getStudents(){
        stuList = getStudentList();
        validateToken();
        return Response.ok(stuList)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("{rollNo}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch one student based on the param value.")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name="Authorization", value="Authorization token in format Bearer [token value]",
                    required=true,dataType="String", paramType="header")
    })
    @ApiResponses(value = {
            @ApiResponse(code=401, message = "Unauthorized: User is not authorized to access the application"),
            @ApiResponse(code=404, message="Not Found: Requested resource does not exist in the system"),
            @ApiResponse(code=500, message="Internal server error: Unexpected exception occurred")
    })
    public Student getStudents(@PathParam("rollNo") @ApiParam(value="rollno for student you want to fetch", required=true)
             String rollNo) {
        stuList = getStudentList();
        validateToken();
        Student matchedRecord = getStudentByRollNo(rollNo);
        if(matchedRecord == null){
            throwNotFoundError();
        }
        return matchedRecord;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @ApiOperation(value = "create student in a system.")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name="Authorization", value="Authorization token in format Bearer [token value]",
                    required=true,dataType="String", paramType="header")
    })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created: New student added successfully"),
            @ApiResponse(code=401, message = "Unauthorized: User is not authorized to access the application"),
            @ApiResponse(code=500, message="Internal server error: Unexpected exception occurred")
    })
    public Response createStudent(@ApiParam(value="object for creating student", required=true) Student stu) throws URISyntaxException{
        validateToken();
        stuList.add(stu);
        return Response.created(new URI(request.getRequestURI() + "/" + stu.getRollno()))
                .type(MediaType.TEXT_PLAIN)
                .build();
        
    }
    
    @PUT
    @Path("{rollNo}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "update student based on the param value.")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name="Authorization", value="Authorization token in format Bearer [token value]",
                    required=true,dataType="String", paramType="header")
    })
    @ApiResponses(value = {
            @ApiResponse(code=401, message = "Unauthorized: User is not authorized to access the application"),
            @ApiResponse(code=404, message="Not Found: Requested resource does not exist in the system"),
            @ApiResponse(code=500, message="Internal server error: Unexpected exception occurred")
    })
    public Student updateStudent(@PathParam("rollNo") @ApiParam(value="rollno for student you want to fetch", required=true)
                                 String rollNo, Student stu) {
        stuList = getStudentList();
        validateToken();
        Student matchedRecord = getStudentByRollNo(rollNo);
        if(matchedRecord == null){
            throwNotFoundError();
        }
        Student updatedRecord = matchedRecord;
        updatedRecord.setName(stu.getName());
        updatedRecord.setTotalMarks(stu.getTotalMarks());
        
        return updatedRecord;
    }
    
    
    /**
     * Patch should be in a format.
     * 
     * [{"op":"replace","path":"/name",
            "value":"modified-name"},
            {"op":"replace", "path":"/totalMarks",
            "value":800}]
     * 
     * and Content-Type must be application/json-patch+json
     * 
     */
    @PATCH
    @Path("{rollNo}")
    @Consumes("application/json-patch+json") // per RFC 6902
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "patch student based on the param value.")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name="Authorization", value="Authorization token in format Bearer [token value]",
                    required=true,dataType="String", paramType="header")
    })
    @ApiResponses(value = {
            @ApiResponse(code=401, message = "Unauthorized: User is not authorized to access the application"),
            @ApiResponse(code=404, message="Not Found: Requested resource does not exist in the system"),
            @ApiResponse(code=500, message="Internal server error: Unexpected exception occurred")
    })
    public Student patchStudent(@PathParam("rollNo") @ApiParam(value="rollno for student you want to fetch", required=true) 
                   String rollNo,
                   @ApiParam(value = "student value in JSON Patch format",
                           required = true)
                   JsonPatch jsonPatch) {
        stuList = getStudentList();
        validateToken();
        Student updatedStudent = null;
        try {
            if (jsonPatch == null) {
                throwBadRequestError("Missing patch", null);
            }
            Student matchedRecord = getStudentByRollNo(rollNo);
            if (matchedRecord == null) {
                throwNotFoundError();
            }
            ObjectMapper mapper = new ObjectMapper();
            String studentAsJson = mapper.writeValueAsString(matchedRecord);
            JsonNode result = jsonPatch.apply(mapper.readTree(studentAsJson));
            updatedStudent = mapper.convertValue(result, Student.class);
        } catch (JsonProcessingException e) {
            throwBadRequestError("Invalid JSON PATCH", null);
        } catch (JsonPatchException e) {
            throwBadRequestError("Invalid JSON PATCH", null);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
        return updatedStudent;
    }
    
    @DELETE
    @Path("{rollNo}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @ApiOperation(value = "delete student based on the param value.")
    @ApiImplicitParams(value= {
            @ApiImplicitParam(name="Authorization", value="Authorization token in format Bearer [token value]",
                    required=true,dataType="String", paramType="header")
    })
    @ApiResponses(value = {
            @ApiResponse(code=401, message = "Unauthorized: User is not authorized to access the application"),
            @ApiResponse(code=404, message="Not Found: Requested resource does not exist in the system"),
            @ApiResponse(code=500, message="Internal server error: Unexpected exception occurred")
    })
    public Response deleteStudent(@PathParam("rollNo") @ApiParam(value="rollno for student you want to fetch", required=true)
                                 String rollNo){
        stuList = getStudentList();
        validateToken();
        Student matchedRecord = getStudentByRollNo(rollNo);
        if(matchedRecord == null){
            throwNotFoundError();
        }
        stuList.remove(matchedRecord);
        return Response.ok("Record Deleted succesfully").type(MediaType.TEXT_PLAIN).build();
    }
    
    /**
     * @param rollNo
     * @param matchedRecord
     * @return
     */
    private Student getStudentByRollNo(String rollNo) {
        for (Student student : stuList) {
            if (student.getRollno() == Integer.parseInt(rollNo)) {
               return student;
            }
        }
        return null;
    }
    
    /**
     * This method is populating dummy data
     */
    public static List<Student> getStudentList() {
        stuList = new ArrayList<Student>();
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
        s1 = new Student();
        s1.setName("xyz");
        s1.setRollno(3);
        s1.setTotalMarks(260);
        stuList.add(s1);
        return stuList;
    }
}
