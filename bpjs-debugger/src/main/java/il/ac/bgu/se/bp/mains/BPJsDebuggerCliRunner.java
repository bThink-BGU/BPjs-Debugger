package il.ac.bgu.se.bp.mains;

import il.ac.bgu.se.bp.debugger.BPJsDebugger;
import il.ac.bgu.se.bp.debugger.state.BPDebuggerState;
import il.ac.bgu.se.bp.execution.BPJsDebuggerImpl;
import il.ac.bgu.se.bp.rest.response.BooleanResponse;
import il.ac.bgu.se.bp.rest.response.GetSyncSnapshotsResponse;

import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class BPJsDebuggerCliRunner {

    private static boolean isTerminated = false;
    private static BPJsDebugger<BooleanResponse> bpJsDebugger;
    private static Scanner sc = new Scanner(System.in);
    private static boolean isSkipSyncPoints = false;
    private static boolean isSkipBreakPoints = false;

    public static void main(String[] args) {
        final String filename = "BPJSDebuggerTest.js";
//        final String filename = "BPJSDebuggerRecTest.js";

        Function<BPDebuggerState, Void> onStateChangedEvent = BPJsDebuggerCliRunner::onStateChanged;
        sc = new Scanner(System.in);
        bpJsDebugger = new BPJsDebuggerImpl(filename, createOnExitCallback(sc), onStateChangedEvent);
        runBPJsDebuggerCliRunner();

        System.out.println("BPJsDebuggerCliRunner exiting..");
    }

    private static Void onStateChanged(BPDebuggerState state) {
        System.out.println(state.toString());
        return null;
    }

    private static void runBPJsDebuggerCliRunner() {
        while (!isTerminated) {
            boolean isStop = !userMenuLoop(sc, bpJsDebugger);
            isTerminated = isTerminated || isStop;
        }
        bpJsDebugger.stop();
    }

    private static boolean userMenuLoop(Scanner sc, BPJsDebugger<BooleanResponse> bpJsDebugger) {
        String[] splat = getUserInput(sc);
        String cmd = splat[0];
        switch (cmd) {
            case "b":
                if (!bpJsDebugger.isSetup() || !bpJsDebugger.isStarted()) {
                    sendRequest(() -> bpJsDebugger.setup(
                            Collections.singletonMap(Integer.parseInt(splat[1]), true),
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
                            isSkipSyncPoints));
                }
                else
                    sendRequest(() -> bpJsDebugger.setBreakpoint(Integer.parseInt(splat[1]), false));
                break;
            case "go":
                if (!bpJsDebugger.isStarted()) {
                    sendRequest(() -> bpJsDebugger.startSync(isSkipSyncPoints));
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

    private static void sendRequest(Callable<BooleanResponse> callable) {
        try {
            BooleanResponse booleanResponse = callable.call();
            if (!booleanResponse.isSuccess()) {
                System.out.println("booleanResponse.getErrorCode: " + booleanResponse.getErrorCode());
            }
            else {
                System.out.println("request sent successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendGetSyncSnapshotsResponse(Callable<GetSyncSnapshotsResponse> callable) {
        try {
            GetSyncSnapshotsResponse getSyncSnapshotsResponse = callable.call();
            System.out.println(getSyncSnapshotsResponse.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Callable<Boolean> createOnExitCallback(Scanner sc) {
        return () -> {
            isTerminated = true;
            return true;
        };
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
}
