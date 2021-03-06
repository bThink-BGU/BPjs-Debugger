package il.ac.bgu.se.bp.execution;

import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


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
        setupDebugger();
    }

    private void setupDebugger() {
        assertFalse(bpJsDebugger.isSetup());
        BooleanResponse booleanResponse = bpJsDebugger.setup(breakpoints, false);
        assertSuccessResponse(booleanResponse);
        assertTrue(bpJsDebugger.isSetup());

        verify(debuggerEngine, times(1)).setupBreakpoints(breakpoints);
        verify(debuggerEngine, times(1)).setSyncSnapshot(any());
    }

    @Test
    public void addExternalEventTest() {
        assertSuccessResponse(bpJsDebugger.addExternalEvent("event_1"));
        assertErrorResponse(bpJsDebugger.addExternalEvent(null), ErrorCode.INVALID_EVENT);
        assertErrorResponse(bpJsDebugger.addExternalEvent(""), ErrorCode.INVALID_EVENT);
    }

    @Test
    public void removeExternalEventTest() {
        assertErrorResponse(bpJsDebugger.removeExternalEvent(null), ErrorCode.INVALID_EVENT);
        assertErrorResponse(bpJsDebugger.removeExternalEvent(""), ErrorCode.INVALID_EVENT);

        assertSuccessResponse(bpJsDebugger.addExternalEvent("event_1"));
        assertSuccessResponse(bpJsDebugger.removeExternalEvent("event_1"));
    }

    @Test
    public void setWaitForExternalEvents() {
        assertSuccessResponse(bpJsDebugger.setWaitForExternalEvents(true));
        assertSuccessResponse(bpJsDebugger.setWaitForExternalEvents(false));
    }

    @Test
    public void setIsSkipSyncPointsTest() {
        assertSuccessResponse(bpJsDebugger.setIsSkipSyncPoints(true));
        assertSuccessResponse(bpJsDebugger.setIsSkipSyncPoints(false));
    }

    @Test
    public void getSyncSnapshotsHistoryTest() {
        GetSyncSnapshotsResponse getSyncSnapshotsResponse = bpJsDebugger.getSyncSnapshotsHistory();
        assertNotNull(getSyncSnapshotsResponse);
        SortedMap<Long, BPDebuggerState> syncSnapshotsHistory = getSyncSnapshotsResponse.getSyncSnapShotsHistory();
        assertNotNull(syncSnapshotsHistory);
        assertTrue(syncSnapshotsHistory.isEmpty());
    }

    @Test
    public void setSyncSnapshotsTest() {

    }

    @Test
    public void startSyncTest() {
//        bpJsDebugger.startSync(false);
    }

    @Test
    public void nextSyncTest() {

    }

    private void assertSuccessResponse(BooleanResponse booleanResponse) {
        assertTrue(booleanResponse.isSuccess());
        assertNull(booleanResponse.getErrorCode());
    }

    private void assertErrorResponse(BooleanResponse booleanResponse, ErrorCode expectedErrorCode) {
        assertFalse(booleanResponse.isSuccess());
        assertEquals(expectedErrorCode, booleanResponse.getErrorCode());
    }

    private static boolean onExitTester() {
        return true;
    }

    private static Void onStateChangedTester(BPDebuggerState debuggerState) {
        System.out.println("SHOULD NOT HAPPEN");
        return null;
    }
}