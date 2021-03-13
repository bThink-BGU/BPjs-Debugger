package il.ac.bgu.se.bp.execution;

import il.ac.bgu.se.bp.debugger.commands.Continue;
import il.ac.bgu.se.bp.debugger.commands.StepInto;
import il.ac.bgu.se.bp.debugger.commands.StepOut;
import il.ac.bgu.se.bp.debugger.commands.StepOver;
import il.ac.bgu.se.bp.debugger.engine.DebuggerEngine;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import il.ac.bgu.se.bp.error.ErrorCode;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class BPJsDebuggerImplTest {

    private final static String TEST_FILENAME = "BPJSDebuggerForTesting.js";
    private final static Callable onExitCallback = BPJsDebuggerImplTest::onExitTester;
    private final static Function<BPDebuggerState, Void> onStateChangedCallback = BPJsDebuggerImplTest::onStateChangedTester;

    private final static int[] BREAKPOINTS_LINES = new int[]{2, 4};
    private final static Map<Integer, Boolean> breakpoints = new HashMap<>();

    private final static BlockingQueue<BPDebuggerState> onStateChangedQueue = new ArrayBlockingQueue<>(5);

    @InjectMocks
    private BPJsDebuggerImpl bpJsDebugger = new BPJsDebuggerImpl(TEST_FILENAME, onExitCallback, onStateChangedCallback);

    @Mock
    private DebuggerEngine debuggerEngine;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        onStateChangedQueue.clear();
        Arrays.stream(BREAKPOINTS_LINES).forEach(lineNumber -> breakpoints.put(lineNumber, Boolean.TRUE));
        doAnswer(a -> onStateChangedTester(new BPDebuggerState())).when(debuggerEngine).onStateChanged();
    }

    private void setupDebugger() {
        assertFalse(bpJsDebugger.isSetup());
        BooleanResponse booleanResponse = bpJsDebugger.setup(breakpoints, false, false);
        assertSuccessResponse(booleanResponse);
        assertTrue(bpJsDebugger.isSetup());

        verify(debuggerEngine, times(1)).setupBreakpoints(breakpoints);
        verify(debuggerEngine, times(1)).setSyncSnapshot(any());
    }

    @Test
    public void addExternalEventTest() {
        setupDebugger();
        assertSuccessResponse(bpJsDebugger.addExternalEvent("event_1"));
        assertErrorResponse(bpJsDebugger.addExternalEvent(null), ErrorCode.INVALID_EVENT);
        assertErrorResponse(bpJsDebugger.addExternalEvent(""), ErrorCode.INVALID_EVENT);
    }

    @Test
    public void removeExternalEventTest() {
        setupDebugger();
        assertErrorResponse(bpJsDebugger.removeExternalEvent(null), ErrorCode.INVALID_EVENT);
        assertErrorResponse(bpJsDebugger.removeExternalEvent(""), ErrorCode.INVALID_EVENT);

        assertSuccessResponse(bpJsDebugger.addExternalEvent("event_1"));
        assertSuccessResponse(bpJsDebugger.removeExternalEvent("event_1"));
    }

    @Test
    public void setWaitForExternalEvents() {
        setupDebugger();
        assertSuccessResponse(bpJsDebugger.setWaitForExternalEvents(true));
        assertSuccessResponse(bpJsDebugger.setWaitForExternalEvents(false));
    }

    @Test
    public void setIsSkipSyncPointsTest() {
        setupDebugger();
        assertSuccessResponse(bpJsDebugger.setIsSkipSyncPoints(true));
        assertSuccessResponse(bpJsDebugger.setIsSkipSyncPoints(false));
    }

    @Test
    public void getSyncSnapshotsHistory_noSnapshotsAddedTest() {
        setupDebugger();
        GetSyncSnapshotsResponse getSyncSnapshotsResponse = bpJsDebugger.getSyncSnapshotsHistory();
        assertNotNull(getSyncSnapshotsResponse);
        SortedMap<Long, BPDebuggerState> syncSnapshotsHistory = getSyncSnapshotsResponse.getSyncSnapShotsHistory();
        assertNotNull(syncSnapshotsHistory);
        assertTrue(syncSnapshotsHistory.isEmpty());
    }

    @Test
    public void setSyncSnapshots_noSnapshotsAddedTest() {
        setupDebugger();
        assertErrorResponse(bpJsDebugger.setSyncSnapshots(123123L), ErrorCode.CANNOT_REPLACE_SNAPSHOT);
        assertErrorResponse(bpJsDebugger.setSyncSnapshots(-1L), ErrorCode.CANNOT_REPLACE_SNAPSHOT);
    }

    @Test
    public void startSyncTest() throws InterruptedException {
        setupDebugger();
        assertSuccessResponse(bpJsDebugger.startSync(false, false));

        sleepUntil(e -> bpJsDebugger.isStarted(), 3);
        assertTrue(bpJsDebugger.isStarted());

        assertSuccessResponse(bpJsDebugger.stop());
        sleepUntil(e -> !bpJsDebugger.isStarted(), 3);
        assertFalse(bpJsDebugger.isStarted());
    }

    @Test
    public void nextSyncTest() throws InterruptedException {
        setupDebugger();
        assertSuccessResponse(bpJsDebugger.startSync(false, false));

        sleepUntil(e -> bpJsDebugger.isStarted(), 3);
        assertTrue(bpJsDebugger.isStarted());

        assertSuccessResponse(bpJsDebugger.nextSync());

        onStateChangedQueue.take();

        GetSyncSnapshotsResponse getSyncSnapshotsResponse = bpJsDebugger.getSyncSnapshotsHistory();
        assertNotNull(getSyncSnapshotsResponse);
        SortedMap<Long, BPDebuggerState> syncSnapshotsHistory = getSyncSnapshotsResponse.getSyncSnapShotsHistory();
        assertNotNull(syncSnapshotsHistory);
        assertFalse(syncSnapshotsHistory.isEmpty());

        assertSuccessResponse(bpJsDebugger.stop());
        sleepUntil(e -> !bpJsDebugger.isStarted(), 3);
        assertFalse(bpJsDebugger.isStarted());
    }

    private void sleepUntil(Predicate sleepUntil, int maxToTest) throws InterruptedException {
        int counter = 0;
        while (!sleepUntil.test(null) && counter < maxToTest) {
            Thread.sleep(1000);
            counter++;
        }
    }

   // @Test
    public void debuggerCommands() {
        assertErrorResponse(bpJsDebugger.setBreakpoint(1, true), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.stop(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.stepOut(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.stepInto(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.stepOver(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.continueRun(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.getState(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.getState(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.getState(), ErrorCode.SETUP_REQUIRED);
        assertErrorResponse(bpJsDebugger.getState(), ErrorCode.SETUP_REQUIRED);

        setupDebugger();

        when(debuggerEngine.isBreakpointAllowed(anyInt())).thenReturn(false);
        assertErrorResponse(bpJsDebugger.setBreakpoint(1, true), ErrorCode.BREAKPOINT_NOT_ALLOWED);

        assertSuccessResponse(bpJsDebugger.toggleMuteBreakpoints(true));
        verify(debuggerEngine, times(1)).toggleMuteBreakpoints(eq(true));

        when(debuggerEngine.isBreakpointAllowed(anyInt())).thenReturn(true);
        assertSuccessResponse(bpJsDebugger.setBreakpoint(1, true));
        verify(debuggerEngine, times(1)).setBreakpoint(eq(1), eq(true));

        assertSuccessResponse(bpJsDebugger.stop());
        verify(debuggerEngine, times(1)).stop();

        /* todo Alex
        first option: fake move state to JS_DEBUG and expect success
         */
        assertSuccessResponse(bpJsDebugger.stepOut());
        verify(debuggerEngine, times(0)).stepOut();
        verify(debuggerEngine, times(1)).addCommand(isA(StepOut.class));
         /*
        second option: expect error
         */
        assertErrorResponse(bpJsDebugger.stepOut(), ErrorCode.NOT_IN_JS_DEBUG_STATE);
        verify(debuggerEngine, times(0)).stepOut();
        verify(debuggerEngine, times(1)).addCommand(isA(StepOut.class));

        assertErrorResponse(bpJsDebugger.stepInto(), ErrorCode.NOT_IN_JS_DEBUG_STATE);
        verify(debuggerEngine, times(0)).stepInto();
        verify(debuggerEngine, times(1)).addCommand(isA(StepInto.class));

        assertSuccessResponse(bpJsDebugger.stepOver());
        verify(debuggerEngine, times(0)).stepOver();
        verify(debuggerEngine, times(1)).addCommand(isA(StepOver.class));

        assertSuccessResponse(bpJsDebugger.continueRun());
        verify(debuggerEngine, times(0)).continueRun();
        verify(debuggerEngine, times(1)).addCommand(isA(Continue.class));

        assertSuccessResponse(bpJsDebugger.getState());
        verify(debuggerEngine, times(1)).getState();
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
        onStateChangedQueue.add(debuggerState);
        return null;
    }
}