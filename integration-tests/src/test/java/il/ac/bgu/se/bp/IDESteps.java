package il.ac.bgu.se.bp;

import il.ac.bgu.se.bp.config.IDECommonTestConfiguration;
import il.ac.bgu.se.bp.mocks.SessionHandlerMock;
import il.ac.bgu.se.bp.mocks.testService.TestService;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.socket.StompPrincipal;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.utils.Pair;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
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

    private Map<Integer, Boolean> breakpointsVerifier;

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

        List<Integer> breakpointsList = strToIntList(breakpoints);
        initBreakpointsVerifier(breakpointsList);
        DebugRequest debugRequest = new DebugRequest(getCodeByFileName(filename), breakpointsList);
        debugRequest.setSkipBreakpointsToggle(toggleMuteBreakpointsBoolean);
        debugRequest.setSkipSyncStateToggle(toggleMuteSyncPointsBoolean);

        lastResponse = testService.debug(userId, debugRequest);
    }

    private void initBreakpointsVerifier(List<Integer> breakpointsList) {
        breakpointsVerifier = new HashMap<>();
        breakpointsList.forEach(breakpoint -> breakpointsVerifier.put(breakpoint, false));
    }

    @When("I click on continue")
    public void iClickOnContinue() {
        sessionHandler.cleanMock();
        lastResponse = testService.continueRun(userId);
    }

    @And("verify all breakpoints were reached")
    public void verifyAllBreakpointsWereReached() {
        breakpointsVerifier.forEach((breakpoint, isReached) -> assertTrue("did not get to breakpoint " + breakpoint, isReached));
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

    @Then("I should get notification with BThread (.*), doubles (.*), strings (.*) and breakpoint lines (.*)")
    public void iShouldGetNotificationWithDoubleVariablesAndStringVariables(String bThreads, String doubleVars, String stringVars, String breakpointsStr) {
        BPDebuggerState lastDebuggerState = sessionHandler.getLastDebuggerStates();
        assertNotNull("BPDebuggerState was not received", lastDebuggerState);

        List<Integer> breakpoints = strToIntList(breakpointsStr);
        assertCurrentLineMatches(breakpoints, lastDebuggerState.getCurrentLineNumber());

        List<String> bThreadsOfCurrentBreakpoint = getBThreadNamesByBreakpoint(bThreads, lastDebuggerState.getCurrentLineNumber());
        Map<String, String> actualEnv = getLastEnvOfMatchingBThread(lastDebuggerState.getbThreadInfoList(), bThreadsOfCurrentBreakpoint);
        assertEnvVariables(actualEnv, lastDebuggerState.getCurrentLineNumber(), doubleVars, stringVars);
    }

    private void assertEnvVariables(Map<String, String> actualEnv, int currentBreakpoint, String doubleVars, String stringVars) {
        List<Pair<String, String>> expectedStringVars = createStringEnvByBreakpoints(stringVars).get(currentBreakpoint);
        if (expectedStringVars != null) {   // test file might not include string vars at this breakpoint
            expectedStringVars.forEach(var -> assertEquals(var.getRight(), strToString(actualEnv.get(var.getLeft()))));
        }
        List<Pair<String, Double>> expectedDoubleVars = createDoubleEnvByBreakpoints(doubleVars).get(currentBreakpoint);
        if (expectedDoubleVars != null) {   // test file might not include double vars at this breakpoint
            expectedDoubleVars.forEach(var -> assertEquals(var.getRight(), strToDouble(actualEnv.get(var.getLeft())), 0));
        }
    }

    private void assertCurrentLineMatches(List<Integer> breakpoints, Integer currentLineNumber) {
        assertNotNull("current line is null", currentLineNumber);
        assertTrue(breakpoints.contains(currentLineNumber));
        breakpointsVerifier.put(currentLineNumber, true);
    }
}
