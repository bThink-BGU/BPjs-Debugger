package il.ac.bgu.cs.bp.bpjs.debugger;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.jsproxy.BpLog;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import org.junit.Test;

import static org.junit.Assert.fail;

public class Debugger {

    public static void main(String[] args) throws InterruptedException {
        // testing the dining philosophers for a large number of philosophers
        final ResourceBProgram bprog = new ResourceBProgram("BPJSDebuggerTest.js");
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.setBProgram(bprog);
        bprog.setup();

        rnr.run();
//        printCounterExample(verifyPhilosophers(4));
    }

    @Test
    public void testCounterexampleFound() throws InterruptedException {
        VerificationResult res = verifyPhilosophers(5);
        if ( res.isViolationFound()) {
            printCounterExample(res);

        } else {
            System.out.println("No counterexample found.");
            fail("No counterexample found for dinning philosophers.");
        }
    }

    private static void printCounterExample(VerificationResult res) {
        System.out.println("Found a counterexample");
        final ExecutionTrace trace = res.getViolation().get().getCounterExampleTrace();
        trace.getNodes().forEach(nd -> System.out.println(" " + nd.getEvent()));

        BProgramSyncSnapshot last = trace.getLastState();
        System.out.println("selectableEvents: " + trace.getBProgram().getEventSelectionStrategy()
                .selectableEvents(last));
        last.getBThreadSnapshots().stream()
                .sorted((s1, s2) -> s1.getName().compareTo(s2.getName()))
                .forEach(s -> {
                    System.out.println(s.getName());
                    System.out.println(s.getSyncStatement());
                    System.out.println();
                });
    }

    private static VerificationResult verifyPhilosophers(int philosopherCount) throws InterruptedException {
        // Create a program
        final ResourceBProgram bprog = new ResourceBProgram("BPJSDebuggerTest.js");
        bprog.setLogLevel(BpLog.LogLevel.Info);
        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.setBProgram(bprog);
        try {
            DfsBProgramVerifier vfr = new DfsBProgramVerifier();
            vfr.setMaxTraceLength(50);
            final VerificationResult res = vfr.verify(bprog);

            System.out.printf("Scanned %,d states\n", res.getScannedStatesCount());
            System.out.printf("Time: %,d milliseconds\n", res.getTimeMillies());

            return res;

        } catch (Exception ex) {
            ex.printStackTrace(System.out);
        }
        return null;
    }

}
