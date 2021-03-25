package il.ac.bgu.se.bp;

import il.ac.bgu.se.bp.config.IDECommonTestConfiguration;
import il.ac.bgu.se.bp.mocks.MockSessionHandler;
import il.ac.bgu.se.bp.mocks.testService.TestService;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.socket.StompPrincipal;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Map;

import static il.ac.bgu.se.bp.code.CodeFilesHelper.getCodeByFileName;
import static il.ac.bgu.se.bp.common.Utils.*;
import static org.junit.Assert.*;

@ContextConfiguration(classes = IDECommonTestConfiguration.class)
public class IDESteps {

    private String userId;
    private BooleanResponse lastResponse;

    @Autowired
    private TestService testService;

    @Autowired
    private MockSessionHandler sessionHandler;

    @Given("I have connected to websocket with (.*) and (.*)")
    public void iHaveConnectedToWebsocketWithSessionIdAndUserId(String sessionId, String userId) {
        this.userId = userId;
        testService.subscribeUser(sessionId, new StompPrincipal(userId));
    }

    @When("I ask to debug with filename (.*) and toggleMuteBreakpoints (.*) and toggleMuteSyncPoints (.*) and breakpoints (.*)")
    public void iAskToDebugWithFilenameAndToggleMuteBreakpointsAndToggleMuteSyncPointsAndBreakpoints(String filename, String toggleMuteBreakpoints, String toggleMuteSyncPoints, String breakpoints) {
        boolean toggleMuteBreakpointsBoolean = strToBoolean(toggleMuteBreakpoints);
        boolean toggleMuteSyncPointsBoolean = strToBoolean(toggleMuteSyncPoints);

        DebugRequest debugRequest = new DebugRequest(getCodeByFileName(filename), strToIntList(breakpoints));
        debugRequest.setSkipBreakpointsToggle(toggleMuteBreakpointsBoolean);
        debugRequest.setSkipSyncStateToggle(toggleMuteSyncPointsBoolean);

        lastResponse = testService.debug(userId, debugRequest);
    }

    @Then("wait until breakpoint reached")
    public void waitUntilBreakpointReached() {
        waitUntilPredicateSatisfied(() -> sessionHandler.getLastDebuggerStates() != null, 1000, 3);
    }

    @Then("The response should be (.*) with errorCode (.*)")
    public void theResponseShouldBe(String result, String errorCode) {
        assertEquals(strToBoolean(result), lastResponse.isSuccess());

        if (isNull(errorCode)) {
            assertNull(lastResponse.getErrorCode());
        }
        else {
            assertNotNull(lastResponse.getErrorCode());
            assertEquals(errorCode, lastResponse.getErrorCode().name());
        }
    }

    @Then("I should get notification with doubles (.*) and strings (.*)")
    public void iShouldGetNotificationWithDoubleVariablesAndStringVariables(String doubleVars, String stringVars) {
        BPDebuggerState lastDebuggerState = sessionHandler.getLastDebuggerStates();
        Map<String, String> env = lastDebuggerState.getbThreadInfoList().get(0).getEnv().get(0);

        strToStringVarsList(stringVars).forEach(var -> assertEquals(var.getRight(), env.get(var.getLeft())));
        strToDoubleVarsList(doubleVars).forEach(var -> assertEquals(var.getRight(), strToDouble(env.get(var.getLeft())), 0));
    }
}
