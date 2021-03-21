package il.ac.bgu.se.bp.mains;

import com.fasterxml.jackson.databind.ObjectMapper;
import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.socket.console.ConsoleMessage;
import il.ac.bgu.se.bp.socket.exit.ProgramExit;
import il.ac.bgu.se.bp.socket.state.BPDebuggerState;
import il.ac.bgu.se.bp.execution.BPJsDebuggerImpl;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;
import il.ac.bgu.se.bp.utils.observer.BPEvent;
import il.ac.bgu.se.bp.utils.observer.Subscriber;
import il.ac.bgu.se.bp.utils.visitor.PublisherVisitor;

import java.util.Collections;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.Callable;

public class BPJsDebuggerCliRunner implements Subscriber<BPEvent>, PublisherVisitor {

    private static final String debuggerId = UUID.randomUUID().toString();

    private boolean isTerminated = false;
    private Scanner sc = new Scanner(System.in);
    private boolean isSkipSyncPoints = false;
    private boolean isSkipBreakPoints = false;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private BPJsDebuggerImpl bpJsDebugger;

    public static void main(String[] args) {
        final String filename = "BPJSDebuggerForTesting.js";
//        final String filename = "BPJSDebuggerRecTest.js";

        System.out.println("Debugger id: " + debuggerId);
        BPJsDebuggerCliRunner bpJsDebuggerCliRunner = new BPJsDebuggerCliRunner(new BPJsDebuggerImpl(debuggerId, filename));
        bpJsDebuggerCliRunner.runBPJsDebuggerCliRunner();
    }

    BPJsDebuggerCliRunner(BPJsDebuggerImpl bpJsDebugger) {
        sc = new Scanner(System.in);
        this.bpJsDebugger = bpJsDebugger;
        bpJsDebugger.subscribe(this);
    }

    private void runBPJsDebuggerCliRunner() {
        while (!isTerminated) {
            boolean isStop = !userMenuLoop(sc, bpJsDebugger);
            isTerminated = isTerminated || isStop;
        }
        bpJsDebugger.stop();
        System.out.println("BPJsDebuggerCliRunner exiting..");
    }

    private boolean userMenuLoop(Scanner sc, BPJsDebugger<BooleanResponse> bpJsDebugger) {
        String[] splat = getUserInput(sc);
        String cmd = splat[0];
        switch (cmd) {
            case "b":
                if (!bpJsDebugger.isSetup()) {
                    sendRequest(() -> bpJsDebugger.setup(
                            Collections.singletonMap(Integer.parseInt(splat[1]), true),
                            isSkipBreakPoints,
                            isSkipSyncPoints));
                }
                else
                    sendRequest(() -> bpJsDebugger.setBreakpoint(
                            Integer.parseInt(splat[1]),
                            true));
                break;
            case "rb":
                if (!bpJsDebugger.isSetup() || !bpJsDebugger.isStarted()) {
                    sendRequest(() -> bpJsDebugger.setup(
                            Collections.singletonMap(Integer.parseInt(splat[1]), false),
                            isSkipBreakPoints,
                            isSkipSyncPoints));
                }
                else
                    sendRequest(() -> bpJsDebugger.setBreakpoint(Integer.parseInt(splat[1]), false));
                break;
            case "go":
                if (!bpJsDebugger.isStarted()) {
                    sendRequest(() -> bpJsDebugger.startSync(isSkipBreakPoints, isSkipSyncPoints));
                }
                else {
                    sendRequest(bpJsDebugger::continueRun);
                }
                break;
            case "si":
                sendRequest(bpJsDebugger::stepInto);
                break;
            case "sov":
                sendRequest(bpJsDebugger::stepOver);
                break;
            case "sou":
                sendRequest(bpJsDebugger::stepOut);
                break;
            case "tmb":
                isSkipBreakPoints = !isSkipBreakPoints;
                sendRequest(() -> bpJsDebugger.toggleMuteBreakpoints(isSkipBreakPoints));
                break;
            case "n":
                sendRequest(bpJsDebugger::nextSync);
                break;
            case "e": {
                if (splat.length != 2) {
                    System.out.println("must enter event");
                    break;
                }
                sendRequest(() -> bpJsDebugger.addExternalEvent(splat[1]));
                break;
            }
            case "re": {
                if (splat.length != 2) {
                    System.out.println("must enter event");
                    break;
                }
                sendRequest(() -> bpJsDebugger.removeExternalEvent(splat[1]));
                break;
            }
            case "we": {
                if (splat.length != 2) {
                    System.out.println("must enter event");
                    break;
                }
                boolean shouldWait = Integer.parseInt(splat[1]) > 0;
                sendRequest(() -> bpJsDebugger.setWaitForExternalEvents(shouldWait));
                break;
            }
            case "h": {
                System.out.println("go - start the program \n" +
                        "n - next sync state \n" +
                        "e <event name>- add external event+ " +
                        "re <event name> - remove external event" +
                        "we <0/1>- wait for external events " +
                        "");
            }
            case "tsp":
                isSkipSyncPoints = !isSkipSyncPoints;
                sendRequest(() -> bpJsDebugger.setIsSkipSyncPoints(isSkipSyncPoints));
                break;
            case "getss":
                sendGetSyncSnapshotsResponse(bpJsDebugger::getSyncSnapshotsHistory);
                break;
            case "sss":  // set syncsnapshot
                sendRequest(() -> bpJsDebugger.setSyncSnapshots(Long.parseLong(splat[1])));
                break;
            case "stop":
                sendRequest(bpJsDebugger::stop);
                break;
            case "gets":
                sendRequest(bpJsDebugger::getState);
                break;
        }
        return !cmd.equals("stop");
    }

    private void sendRequest(Callable<BooleanResponse> callable) {
        try {
            BooleanResponse booleanResponse = callable.call();
            if (!booleanResponse.isSuccess()) {
                System.out.println("booleanResponse.getErrorCode: " + booleanResponse.getErrorCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendGetSyncSnapshotsResponse(Callable<GetSyncSnapshotsResponse> callable) {
        try {
            GetSyncSnapshotsResponse getSyncSnapshotsResponse = callable.call();
            System.out.println(objectMapper.writeValueAsString(getSyncSnapshotsResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] getUserInput(Scanner sc) {
        try {
            System.out.println("Enter command: b / rb / go / si / sov / sou / getss / n / e / re / we / h / tmb / tsp / sss / stop");
            String cmd = sc.nextLine();
            return cmd.split(" ");
        } catch (Exception e) {
            return new String[]{"stop"};
        }
    }

    @Override
    public void update(BPEvent event) {
        event.accept(this);
    }

    @Override
    public void visit(String userId, BPDebuggerState debuggerState) {
        System.out.println("debuggerState event received, content: " + debuggerState.prettier());
    }

    @Override
    public void visit(String userId, ConsoleMessage consoleMessage) {
        System.out.println("consoleMessage event received, content: " + consoleMessage.toString());
    }

    @Override
    public void visit(String userId, ProgramExit programExit) {
        isTerminated = true;
        System.out.println("programExit event received");
    }
}
