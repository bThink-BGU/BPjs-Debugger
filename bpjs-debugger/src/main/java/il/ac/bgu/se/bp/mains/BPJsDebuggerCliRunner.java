package il.ac.bgu.se.bp.mains;

import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.execution.BPJsDebuggerRunnerImpl;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class BPJsDebuggerCliRunner {

    public static void main(String[] args) {
        final String filename = "BPJSDebuggerTest.js";
        int[] breakpoints = {5, 13, 20};
        BPJsDebuggerRunner<FutureTask<String>> bpJsDebuggerRunner = new BPJsDebuggerRunnerImpl(filename,breakpoints);
        bpJsDebuggerRunner.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String cmd = "";
        Scanner sc = new Scanner(System.in);
        while (!cmd.equals("exit")) {
            System.out.println("Enter command: b / rb / go / si / sov / sou / get / n");
            cmd = sc.nextLine();
            String[] splat = cmd.split(" ");
            switch (splat[0]) {
                case "b":
                    awaitForResponse(bpJsDebuggerRunner.setBreakpoint(Integer.parseInt(splat[1])));
                    break;
                case "rb":
                    awaitForResponse(bpJsDebuggerRunner.removeBreakpoint(Integer.parseInt(splat[1])));
                    break;
                case "go":
                    awaitForResponse(bpJsDebuggerRunner.continueRun());
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
                    bpJsDebuggerRunner.nextSync();
                    break;

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
