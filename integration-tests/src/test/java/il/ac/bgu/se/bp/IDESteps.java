package il.ac.bgu.se.bp;

import il.ac.bgu.se.bp.config.IDECommonTestConfiguration;
import il.ac.bgu.se.bp.mocks.SessionHandlerMock;
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

import java.util.List;
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
    private SessionHandlerMock sessionHandler;

    @Given("I have connected to websocket with (.*) and (.*)")
    public void iHaveConnectedToWebsocketWithSessionIdAndUserId(String sessionId, String userId) {
        this.userId = userId;
        testService.subscribeUser(sessionId, new StompPrincipal(userId));
    }

    @When("I ask to debug with filename (.*) and toggleMuteBreakpoints (.*) and toggleMuteSyncPoints (.*) and breakpoints (.*)")
    public void iAskToDebugWithFilenameAndToggleMuteBreakpointsAndToggleMuteSyncPointsAndBreakpoints(String filename, String toggleMuteBreakpoints, String toggleMuteSyncPoints, String breakpoints) {
        sessionHandler.cleanMock();
        boolean toggleMuteBreakpointsBoolean = strToBoolean(toggleMuteBreakpoints);
        boolean toggleMuteSyncPointsBoolean = strToBoolean(toggleMuteSyncPoints);

        DebugRequest debugRequest = new DebugRequest(getCodeByFileName(filename), strToIntList(breakpoints));
        debugRequest.setSkipBreakpointsToggle(toggleMuteBreakpointsBoolean);
        debugRequest.setSkipSyncStateToggle(toggleMuteSyncPointsBoolean);

        lastResponse = testService.debug(userId, debugRequest);
    }

    @Then("wait until breakpoint reached")
    public void waitUntilBreakpointReached() {
        waitUntilPredicateSatisfied(() -> sessionHandler.getLastDebuggerStates() != null &&
                sessionHandler.getLastDebuggerStates().getCurrentLineNumber() != null, 1000, 3);
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

    @Then("I should get notification with doubles (.*) and strings (.*) and breakpoint lines (.*)")
    public void iShouldGetNotificationWithDoubleVariablesAndStringVariables(String doubleVars, String stringVars, String breakpointsStr) {
        BPDebuggerState lastDebuggerState = sessionHandler.getLastDebuggerStates();
        assertNotNull("BPDebuggerState was not received", lastDebuggerState);
        Map<String, String> env = lastDebuggerState.getbThreadInfoList().get(0).getEnv().get(0);

        List<Integer> breakpoints = strToIntList(breakpointsStr);
        assertCurrentLineMatches(breakpoints, lastDebuggerState.getCurrentLineNumber());
        strToStringVarsList(stringVars).forEach(var -> assertEquals(var.getRight(), strToString(env.get(var.getLeft()))));
        strToDoubleVarsList(doubleVars).forEach(var -> assertEquals(var.getRight(), strToDouble(env.get(var.getLeft())), 0));
    }

    private void assertCurrentLineMatches(List<Integer> breakpoints, Integer currentLineNumber) {
        assertNotNull("current line is null", currentLineNumber);
        assertTrue(breakpoints.stream().reduce(false, (matches, breakpointLine) -> matches || breakpointLine.equals(currentLineNumber), Boolean::logicalOr));
    }

}
