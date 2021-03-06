package il.ac.bgu.se.bp.execution;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class BPJsDebuggerImplTest {

    private final static String TEST_FILENAME = "BPJSDebuggerTest.js";
    private final static Callable onExitCallback = BPJsDebuggerImplTest::onExitTester;
    private final static Function<BPDebuggerState, Void> onStateChangedCallback = BPJsDebuggerImplTest::onStateChangedTester;

    private final static int[] BREAKPOINTS_LINES = new int[]{2, 4};
    private final static Map<Integer, Boolean> breakpoints = new HashMap<>();

    @InjectMocks
    private BPJsDebuggerImpl bpJsDebugger = new BPJsDebuggerImpl(TEST_FILENAME, onExitCallback, onStateChangedCallback);

    @Mock
    private DebuggerEngine debuggerEngine;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Arrays.stream(BREAKPOINTS_LINES).forEach(lineNumber -> breakpoints.put(lineNumber, true));
    }

    private BooleanResponse setupDebugger() {
        return bpJsDebugger.setup(breakpoints, false);
    }

    @Test
    public void setupTest() {
        assertFalse(bpJsDebugger.isSetup());
        BooleanResponse booleanResponse = setupDebugger();
        assertSuccessResponse(booleanResponse);
        assertTrue(bpJsDebugger.isSetup());

        verify(debuggerEngine, times(1)).setupBreakpoints(breakpoints);
        verify(debuggerEngine, times(1)).setSyncSnapshot(any());
    }

    @Test
    public void startTest() {
        setupDebugger();

    }

    @Test
    public void addExternalEventTest() {

    }

    @Test
    public void removeExternalEventTest() {


    }

    @Test
    public void setWaitForExternalEvents() {

    }

    @Test
    public void startSyncTest() {

    }

    @Test
    public void nextSyncTest() {

    }

    @Test
    public void setIsSkipSyncPointsTest() {

    }

    @Test
    public void getSyncSnapshotsHistoryTest() {

    }

    @Test
    public void setSyncSnapshotsTest() {

    }

    private void assertSuccessResponse(BooleanResponse booleanResponse) {
        assertTrue(booleanResponse.isSuccess());
        assertNull(booleanResponse.getErrorCode());
    }

    private static boolean onExitTester() {
        return true;
    }

    private static Void onStateChangedTester(BPDebuggerState debuggerState) {
        System.out.println("SHOULD NOT HAPPEN");
        return null;
    }
}