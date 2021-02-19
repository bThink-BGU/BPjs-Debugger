package il.ac.bgu.se.bp.mains;
import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.execution.BPJsDebuggerRunnerImpl;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class BPJsDebuggerCliRunner {

    public static void main(String[] args) {
//        final String filename = "BPJSDebuggerTest.js";
        final String filename = "BPJSDebuggerRecTest.js";
        BPJsDebuggerRunner<FutureTask<String>> bpJsDebuggerRunner = new BPJsDebuggerRunnerImpl(filename);
        String cmd = "";
        Scanner sc = new Scanner(System.in);
        while (!cmd.equals("exit")) {
            System.out.println("Enter command: b / rb / go / si / sov / sou / get / n / e / re / we /h / stop");
            cmd = sc.nextLine();
            String[] splat = cmd.split(" ");
            switch (splat[0]) {
                case "b":
                    if (!bpJsDebuggerRunner.isSetup() || !bpJsDebuggerRunner.isStarted()) {
                        bpJsDebuggerRunner.setup(Collections.singletonMap(Integer.parseInt(splat[1]), true));
                    }
                    else
                        awaitForResponse(bpJsDebuggerRunner.setBreakpoint(Integer.parseInt(splat[1]), true));
                    break;
                case "rb":
                    if (!bpJsDebuggerRunner.isSetup() || !bpJsDebuggerRunner.isStarted()) {
                        bpJsDebuggerRunner.setup(Collections.singletonMap(Integer.parseInt(splat[1]), false));
                    }
                    else
                        awaitForResponse(bpJsDebuggerRunner.setBreakpoint(Integer.parseInt(splat[1]), false));
                    break;
                case "go":
                    if (!bpJsDebuggerRunner.isStarted()) {
                        awaitForResponse(bpJsDebuggerRunner.startSync());
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
                case "get":
                    awaitForResponse(bpJsDebuggerRunner.getVars());
                    break;
                case "exit":
                    awaitForResponse(bpJsDebuggerRunner.exit());
                    break;
                case "n":
                    awaitForResponse(bpJsDebuggerRunner.nextSync());
                    break;
                case "e": {
                    if(splat.length != 2) {
                        System.out.println("must enter event");
                        break;
                    }
                    awaitForResponse(bpJsDebuggerRunner.addExternalEvent(splat[1]));
                    break;
                }
                case "re": {
                    if(splat.length != 2) {
                        System.out.println("must enter event");
                        break;
                    }
                    awaitForResponse(bpJsDebuggerRunner.removeExternalEvent(splat[1]));
                    break;
                }
                case "stop":
                    bpJsDebuggerRunner.stop();
                    break;
                case "we": {
                    if(splat.length != 2) {
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

            }
        }
    }

    private static void awaitForResponse(FutureTask<String> future) {
        try {
            System.out.println(future.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
