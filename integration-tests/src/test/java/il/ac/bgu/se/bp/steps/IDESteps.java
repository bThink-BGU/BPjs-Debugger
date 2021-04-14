package il.ac.bgu.se.bp.steps;

import il.ac.bgu.se.bp.config.IDECommonTestConfiguration;
import il.ac.bgu.se.bp.mocks.SessionHandlerMock;
import il.ac.bgu.se.bp.mocks.testService.TestService;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.request.ToggleBreakpointsRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.DebugResponse;
import il.ac.bgu.se.bp.rest.socket.StompPrincipal;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.utils.Pair;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static il.ac.bgu.se.bp.code.CodeFilesHelper.getCodeByFileName;
import static il.ac.bgu.se.bp.common.Utils.*;
import static org.junit.Assert.*;

@ContextConfiguration(classes = IDECommonTestConfiguration.class)
public class IDESteps {

    //    private String activeUserId;
    private Map<String, String> userIdsByNames;
    private BooleanResponse lastResponse;
    private DebugResponse lastDebugResponse;
    private Map<String, Map<Integer, Boolean>> breakpointsVerifierPerUser;

    @Autowired
    private TestService testService;

    @Autowired
    private SessionHandlerMock sessionHandler;

    private final static String END_OF_EXECUTION_INDICATOR_MESSAGE = "Ended";

//    @Given("the current user is (.*)")
//    public void theCurrentUserIsUserId(String userId) {
//        activeUserId = userId;
//    }

    @Given("user (.*) has connected with userId (.*)")
    public void userUserHasConnectedWithUserIdUserId(String userName, String userId) {
        if (userIdsByNames == null) {
            userIdsByNames = new HashMap<>();
        }
        userIdsByNames.put(userName, userId);
    }

    @Given("(.*) has connected to websocket with (.*)")
    public void userHasConnectedToWebsocketWithSessionIdAndUserId(String userName, String sessionId) {
        testService.subscribeUser(sessionId, new StompPrincipal(getUserIdByName(userName)));
    }


    @When("(.*) asks to debug with filename (.*) and toggleMuteBreakpoints (.*) and toggleMuteSyncPoints (.*) and breakpoints (.*)")
    public void userAsksToDebugWithFilenameAndToggleMuteBreakpointsAndToggleMuteSyncPointsAndBreakpoints(String username, String filename, String toggleMuteBreakpoints, String toggleMuteSyncPoints, String breakpoints) {
        sessionHandler.cleanUserMockData(getUserIdByName(username));

        DebugRequest debugRequest = new DebugRequest(getCodeByFileName(filename), strToIntList(breakpoints));
        debugRequest.setSkipBreakpointsToggle(strToBoolean(toggleMuteBreakpoints));
        debugRequest.setSkipSyncStateToggle(strToBoolean(toggleMuteSyncPoints));

        lastDebugResponse = testService.debug(getUserIdByName(username), debugRequest);
    }

    @When("(.*) clicks on continue")
    public void userClicksOnContinue(String username) {
        sessionHandler.cleanUserMockData(getUserIdByName(username));
        lastResponse = testService.continueRun(getUserIdByName(username));
    }

    @When("(.*) toggles mute breakpoints to (.*)")
    public void userTogglesMuteBreakpointsToOff(String username, String skipBreakpoints) {
        lastResponse = testService.toggleMuteBreakpoints(getUserIdByName(username), new ToggleBreakpointsRequest(strToBoolean(skipBreakpoints)));
    }

    @Then("wait until program of user (.*) is over")
    public void waitUntilTheProgramIsOver(String username) {
        waitUntilPredicateSatisfied(() -> sessionHandler.getUsersLastConsoleMessage(getUserIdByName(username)) != null &&
                sessionHandler.getUsersLastConsoleMessage(getUserIdByName(username)).getMessage().contains(END_OF_EXECUTION_INDICATOR_MESSAGE), 1000, 3);
    }

    @Then("The response should be (.*) with errorCode (.*)")
    public void theResponseShouldBe(String result, String errorCode) {
        assertBooleanResponse(lastResponse, strToBoolean(result), errorCode);
    }

    @Then("The debug response should be (.*) with errorCode (.*) and breakpoints (.*) for user (.*)")
    public void theDebugResponseShouldBeTrueWithErrorCodeNull(String result, String errorCode, String breakpoints, String username) {
        assertBooleanResponse(lastDebugResponse, strToBoolean(result), errorCode);
        List<Integer> expectedBreakpoints = strToIntList(breakpoints);
        boolean[] actualBreakpoints = lastDebugResponse.getBreakpoints();
        for (int i = 0; i < actualBreakpoints.length; i++) {
            assertEquals(expectedBreakpoints.contains(i), actualBreakpoints[i]);
        }
        expectedBreakpoints.forEach(breakpoint -> assertTrue(actualBreakpoints[breakpoint]));
        initBreakpointsVerifier(username, expectedBreakpoints);
    }

    private void initBreakpointsVerifier(String username, List<Integer> breakpointsList) {
        if (breakpointsVerifierPerUser == null) {
            breakpointsVerifierPerUser = new HashMap<>();
        }

        HashMap<Integer, Boolean> breakpointsVerifier = new HashMap<>();
        breakpointsList.forEach(breakpoint -> breakpointsVerifier.put(breakpoint, false));
        breakpointsVerifierPerUser.put(username, breakpointsVerifier);
    }

    private void assertBooleanResponse(BooleanResponse booleanResponse, boolean expectedResult, String errorCode) {
        assertEquals(expectedResult, booleanResponse.isSuccess());

        if (isNull(errorCode)) {
            assertNull(booleanResponse.getErrorCode());
        }
        else {
            assertNotNull(booleanResponse.getErrorCode());
            assertEquals(errorCode, booleanResponse.getErrorCode().name());
        }
    }

    @And("verify all breakpoints of user (.*) were reached")
    public void verifyAllBreakpointsWereReached(String username) {
        breakpointsVerifierPerUser.get(username)
                .forEach((breakpoint, isReached) -> assertTrue("did not get to breakpoint " + breakpoint, isReached));
    }

    @And("verify user (.*) has reached only (.*) breakpoints")
    public void verifyOnlyPartialBreakpointsWereReached(String username, String numOfBreakpointsReachedStr) {
        AtomicInteger actualNumOfBreakpointsReached = new AtomicInteger(0);
        breakpointsVerifierPerUser.get(username).values().forEach(isReached -> {
            if (isReached) {
                actualNumOfBreakpointsReached.incrementAndGet();
            }
        });

        assertEquals(strToInt(numOfBreakpointsReachedStr), actualNumOfBreakpointsReached.get());
    }

    @Then("wait until user (.*) has reached breakpoint")
    public void waitUntilBreakpointReached(String username) {
        waitUntilPredicateSatisfied(() -> sessionHandler.getUsersLastDebuggerState(getUserIdByName(username)) != null &&
                sessionHandler.getUsersLastDebuggerState(getUserIdByName(username)).getCurrentLineNumber() != null, 2000, 3);
    }

    @Then("(.*) should get notification with BThread (.*), doubles (.*), strings (.*) and breakpoint lines (.*)")
    public void userShouldGetNotificationWithDoubleVariablesAndStringVariables(String username, String bThreads, String doubleVars, String stringVars, String breakpointsStr) {
        BPDebuggerState lastDebuggerState = sessionHandler.getUsersLastDebuggerState(getUserIdByName(username));
        assertNotNull("BPDebuggerState was not received for user: " + username, lastDebuggerState);

        List<Integer> breakpoints = strToIntList(breakpointsStr);
        assertBreakpoints(username, breakpoints, lastDebuggerState);

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

    private void assertBreakpoints(String username, List<Integer> breakpoints, BPDebuggerState bpDebuggerState) {
        Integer currentBreakpoint = bpDebuggerState.getCurrentLineNumber();
        assertNotNull("current line is null", currentBreakpoint);
        assertTrue(breakpoints.contains(currentBreakpoint));
        breakpointsVerifierPerUser.get(username).put(currentBreakpoint, true);

        Boolean[] actualBreakpointsLines = bpDebuggerState.getBreakpoints();
        breakpoints.forEach(breakpointLine -> assertTrue("breakpoint not set on line: " + breakpointLine, actualBreakpointsLines[breakpointLine]));
        int actualNumOfBreakpoints = Arrays.stream(actualBreakpointsLines).reduce(0, (acc, curr) -> acc += curr ? 1 : 0, Integer::sum);
        assertEquals(breakpoints.size(), actualNumOfBreakpoints);
    }

    private String getUserIdByName(String userName) {
        return userIdsByNames.get(userName);
    }
}
