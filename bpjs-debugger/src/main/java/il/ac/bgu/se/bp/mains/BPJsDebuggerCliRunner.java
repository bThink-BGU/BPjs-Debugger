package il.ac.bgu.se.bp.mains;

import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.execution.BPJsDebuggerRunnerImpl;

import java.util.Collections;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BPJsDebuggerCliRunner {

    private static boolean isTerminated = false;
    private static BPJsDebuggerRunner<FutureTask<String>> bpJsDebuggerRunner;
    private static Scanner sc = new Scanner(System.in);
    private static boolean isSkipSyncPoints = false;
    private static boolean isSkipBreakPoints = false;

    private static void runBPJsDebuggerCliRunner() {
        System.out.println("RUNNING!");
        while (!isTerminated) {
            boolean isStop = !userMenuLoop(sc, bpJsDebuggerRunner);
            isTerminated = isTerminated || isStop;
        }
        bpJsDebuggerRunner.stop();
    }

    public static void main(String[] args) {
        final String filename = "BPJSDebuggerTest.js";
//        final String filename = "BPJSDebuggerRecTest.js";

        sc = new Scanner(System.in);
        bpJsDebuggerRunner = new BPJsDebuggerRunnerImpl(filename, createOnExistCallback(sc));
        runBPJsDebuggerCliRunner();

        System.out.println("BPJsDebuggerCliRunner exiting..");
    }

    private static boolean userMenuLoop(Scanner sc, BPJsDebuggerRunner<FutureTask<String>> bpJsDebuggerRunner) {
        String[] splat = getUserInput(sc);
        String cmd = splat[0];
        switch (cmd) {
            case "b":
                if (!bpJsDebuggerRunner.isSetup() || !bpJsDebuggerRunner.isStarted()) {
                    bpJsDebuggerRunner.setup(Collections.singletonMap(Integer.parseInt(splat[1]), true), isSkipSyncPoints);
                }
                else
                    awaitForResponse(bpJsDebuggerRunner.setBreakpoint(Integer.parseInt(splat[1]), true));
                break;
            case "rb":
                if (!bpJsDebuggerRunner.isSetup() || !bpJsDebuggerRunner.isStarted()) {
                    bpJsDebuggerRunner.setup(Collections.singletonMap(Integer.parseInt(splat[1]), false), isSkipSyncPoints);
                }
                else
                    awaitForResponse(bpJsDebuggerRunner.setBreakpoint(Integer.parseInt(splat[1]), false));
                break;
            case "go":
                if (!bpJsDebuggerRunner.isStarted()) {
                    awaitForResponse(bpJsDebuggerRunner.startSync(isSkipSyncPoints));
                }
                else {
                    awaitForResponse(bpJsDebuggerRunner.continueRun());
                }
                break;
            case "si":
                awaitForResponse(bpJsDebuggerRunner.stepInto());
                break;
            case "sov":
                awaitForResponse(bpJsDebuggerRunner.stepOver());
                break;
            case "sou":
                awaitForResponse(bpJsDebuggerRunner.stepOut());
                break;
            case "getv":
                awaitForResponse(bpJsDebuggerRunner.getVars());
                break;
            case "tmb":
                isSkipBreakPoints = !isSkipBreakPoints;
                awaitForResponse(bpJsDebuggerRunner.toggleMuteBreakpoints(isSkipBreakPoints));
                break;
            case "n":
                bpJsDebuggerRunner.nextSync();
                break;
            case "e": {
                if (splat.length != 2) {
                    System.out.println("must enter event");
                    break;
                }
                awaitForResponse(bpJsDebuggerRunner.addExternalEvent(splat[1]));
                break;
            }
            case "re": {
                if (splat.length != 2) {
                    System.out.println("must enter event");
                    break;
                }
                awaitForResponse(bpJsDebuggerRunner.removeExternalEvent(splat[1]));
                break;
            }
            case "we": {
                if (splat.length != 2) {
                    System.out.println("must enter event");
                    break;
                }
                boolean shouldWait = Integer.parseInt(splat[1]) > 0;
                awaitForResponse(bpJsDebuggerRunner.setWaitForExternalEvents(shouldWait));
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
                awaitForResponse(bpJsDebuggerRunner.setIsSkipSyncPoints(isSkipSyncPoints));
                break;
            case "getss":
                awaitForResponse(bpJsDebuggerRunner.getSyncSnapshotsHistory());
                break;
            case "sss":  // set syncsnapshot
                awaitForResponse(bpJsDebuggerRunner.setSyncSnapshots(Long.parseLong(splat[1])));
                break;
            case "stop":
                awaitForResponse(bpJsDebuggerRunner.stop());
                break;
            case "gets":
                awaitForResponse(bpJsDebuggerRunner.getState());
                break;
        }
        return !cmd.equals("stop");
    }

    private static Callable<Boolean> createOnExistCallback(Scanner sc) {
        return () -> {
            isTerminated = true;
            return true;
        };
    }

    private static String[] getUserInput(Scanner sc) {
        try {
            System.out.println("Enter command: b / rb / go / si / sov / sou / getv / getss / n / e / re / we / h / tmb / tsp / sss / stop");
            String cmd = sc.nextLine();
            return cmd.split(" ");
        } catch (Exception e) {
            return new String[]{"stop"};
        }
    }

    public static void printActiveThreads() {
        Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
        System.out.println("===================");
        System.out.println("threads count: " + Thread.activeCount());
        final Predicate<String> isNameOfJavaThreads = name -> !(name.equals("Finalizer") || name.equals("main") || name.equals("Attach Listener")
                || name.equals("Reference Handler") || name.equals("Monitor Ctrl-Break") || name.equals("Signal Dispatcher"));
        System.out.println(stackTraces.keySet().stream().map(Thread::getName).filter(isNameOfJavaThreads).collect(Collectors.joining(", ")));
        System.out.println("===================");
    }

    private static void awaitForResponse(FutureTask<String> future) {
        try {
            System.out.println(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
