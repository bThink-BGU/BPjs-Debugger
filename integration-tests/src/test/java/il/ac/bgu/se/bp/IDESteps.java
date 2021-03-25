package il.ac.bgu.se.bp;

import il.ac.bgu.se.bp.config.IDECommonTestConfiguration;
import il.ac.bgu.se.bp.mocks.MockSessionHandler;
import il.ac.bgu.se.bp.mocks.testService.TestService;
import il.ac.bgu.se.bp.rest.request.DebugRequest;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.socket.StompPrincipal;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.utils.Pair;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static il.ac.bgu.se.bp.code.CodeFilesHelper.getCodeByFileName;
import static org.junit.Assert.*;

@ContextConfiguration(classes = IDECommonTestConfiguration.class)
public class IDESteps {

    private String userId;

    @Autowired
    TestService testService;

    @Autowired
    MockSessionHandler sessionHandler;

    private BooleanResponse lastResponse;

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

    private void waitUntilPredicateSatisfied(Callable<Boolean> predicate, int timeToSleep, int maxToTry) {
        try {
            for (int i = 0; i < maxToTry; i++) {
                if (predicate.call()) {
                    return;
                }
                sleep(timeToSleep);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sleep(int timeToSleep) {
        try {
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    private boolean isNull(String str) {
        return str.equals("null");
    }

    private List<Pair<String, String>> strToStringVarsList(String stringVars) {
        return StringUtils.isEmpty(stringVars) ? new LinkedList<>() :
                Arrays.stream(stringVars.split(",")).map(strVar -> Pair.of(getVar(strVar), getStrVal(strVar))).collect(Collectors.toList());
    }

    private List<Pair<String, Double>> strToDoubleVarsList(String doubleVars) {
        return StringUtils.isEmpty(doubleVars) ? new LinkedList<>() :
                Arrays.stream(doubleVars.split(",")).map(strVar -> Pair.of(getVar(strVar), getDoubleVal(strVar))).collect(Collectors.toList());
    }

    private String getVar(String str) {
        return splitAndGetBy(str, "=", 0);
    }

    private String getStrVal(String str) {
        return splitAndGetBy(str, "=", 1);
    }

    private Double getDoubleVal(String str) {
        return strToDouble(splitAndGetBy(str, "=", 1));
    }

    private String splitAndGetBy(String str, String regex, int i) {
        return str.split(regex)[i];
    }

    private boolean strToBoolean(String str) {
        return Boolean.parseBoolean(str);
    }

    private Double strToDouble(String str) {
        return Double.parseDouble(str);
    }

    private List<String> strToStringList(String strings) {
        return Arrays.stream(strings.split(",")).collect(Collectors.toList());
    }

    private List<Double> strToDoubleList(String doubles) {
        return Arrays.stream(doubles.split(",")).map(Double::parseDouble).collect(Collectors.toList());
    }

    private List<Integer> strToIntList(String breakpoints) {
        return Arrays.stream(breakpoints.split(",")).map(Integer::parseInt).collect(Collectors.toList());
    }
}
