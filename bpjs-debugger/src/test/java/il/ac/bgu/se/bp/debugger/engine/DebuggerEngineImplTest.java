package il.ac.bgu.se.bp.debugger.engine;

import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import il.ac.bgu.se.bp.debugger.state.EventsStatus;
import il.ac.bgu.se.bp.execution.RunnerState;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class DebuggerEngineImplTest {

    private final static Function<BPDebuggerState, Void> onStateChangedCallback = DebuggerEngineImplTest::onStateChangedTester;
    private final static String TEST_FILENAME = "BPJSDebuggerTest.js";

    private final static int[] BREAKPOINTS_LINES = new int[]{2, 4};
    private final static Map<Integer, Boolean> breakpoints = new HashMap<>();

    private final ExecutorService execSvc = ExecutorServiceMaker.makeWithName("TEST_");

    private BPDebuggerState expectedState;

    @InjectMocks
    private DebuggerEngineImpl debuggerEngine = new DebuggerEngineImpl(TEST_FILENAME, new RunnerState(), onStateChangedCallback);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Arrays.stream(BREAKPOINTS_LINES).forEach(lineNumber -> breakpoints.put(lineNumber, true));
    }


    @Test
    public void test1() throws InterruptedException {
        expectedState = new BPDebuggerState(new LinkedList<>(), null);
        BProgram bProg = new ResourceBProgram(TEST_FILENAME);
        BProgramSyncSnapshot bProgramSyncSnapshot = bProg.setup();
        debuggerEngine.setSyncSnapshot(bProgramSyncSnapshot);


        bProgramSyncSnapshot.start(execSvc);


    }


    private static Void onStateChangedTester(BPDebuggerState debuggerState) {
        System.out.println("MOCK...");
        return null;


    }

}