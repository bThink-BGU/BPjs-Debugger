package il.ac.bgu.se.bp.mocks.testService;

import il.ac.bgu.se.bp.rest.request.*;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.utils.Endpoints;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;

import java.security.Principal;

import static il.ac.bgu.se.bp.rest.utils.Endpoints.*;

public class RestServiceTestHelper implements TestService {

    private static final String BASE_URI = "http://localhost:8080" + Endpoints.BASE_URI;
    private static final String USER_ID = "userId";

    @Override
    public void subscribeUser(String sessionId, Principal principal) {
        // todo
    }

    @Override
    public BooleanResponse run(String userId, RunRequest code) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId)).body(code)
                .contentType(ContentType.JSON).when().post(BASE_URI + RUN);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse debug(String userId, DebugRequest code) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId)).body(code)
                .contentType(ContentType.JSON).when().post(BASE_URI + DEBUG);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse setBreakpoint(String userId, SetBreakpointRequest setBreakpointRequest) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId)).body(setBreakpointRequest)
                .contentType(ContentType.JSON).when().post(BASE_URI + BREAKPOINT);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse toggleMuteBreakpoints(String userId, ToggleBreakpointsRequest toggleBreakpointsRequest) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId)).body(toggleBreakpointsRequest)
                .contentType(ContentType.JSON).when().put(BASE_URI + BREAKPOINT);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse toggleMuteSyncPoints(String userId, ToggleSyncStatesRequest toggleMuteSyncPoints) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId)).body(toggleMuteSyncPoints)
                .contentType(ContentType.JSON).when().put(BASE_URI + SYNC_STATES);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse stop(String userId) {
        Response response = RestAssured.with()
                .contentType(ContentType.JSON).when().get(BASE_URI + STOP);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse stepOut(String userId) {
        Response response = RestAssured.with()
                .contentType(ContentType.JSON).when().get(BASE_URI + STEP_OUT);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse stepInto(String userId) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId))
                .contentType(ContentType.JSON).when().get(BASE_URI + STEP_INTO);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse stepOver(String userId) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId))
                .contentType(ContentType.JSON).when().get(BASE_URI + STEP_OVER);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse continueRun(String userId) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId))
                .contentType(ContentType.JSON).when().get(BASE_URI + CONTINUE);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse nextSync(String userId) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId))
                .contentType(ContentType.JSON).when().get(BASE_URI + NEXT_SYNC);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse externalEvent(String userId, ExternalEventRequest externalEventRequest) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId)).body(externalEventRequest)
                .contentType(ContentType.JSON).when().post(BASE_URI + EXTERNAL_EVENT);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }

    @Override
    public BooleanResponse setSyncSnapshot(String userId, SetSyncSnapshotRequest setSyncSnapshotRequest) {
        Response response = RestAssured.with().header(new Header(USER_ID, userId)).body(setSyncSnapshotRequest)
                .contentType(ContentType.JSON).when().post(BASE_URI + SYNC_SNAPSHOT);
        response.then().statusCode(200);
        return response.getBody().as(BooleanResponse.class);
    }
}
