package il.ac.bgu.se.bp.mains;

import il.ac.bgu.se.bp.debugger.BPJsDebuggerRunner;
import il.ac.bgu.se.bp.execution.BPJsDebuggerRunnerImpl;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


public class BPJsDebuggerCliRunner {

    public static void main(String[] args) {
        BPJsDebuggerRunner<FutureTask<String>> bpJsDebuggerRunner = new BPJsDebuggerRunnerImpl();
        bpJsDebuggerRunner.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String cmd = "";
        Scanner sc = new Scanner(System.in);
        System.out.println("BEFOREEEEEEEE  Thread count: " + Thread.activeCount());
        while (!cmd.equals("exit")) {
            System.out.println("Enter command: b / rb / go / si / so");
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
                case "so":
                    awaitForResponse(bpJsDebuggerRunner.stepOver());
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
