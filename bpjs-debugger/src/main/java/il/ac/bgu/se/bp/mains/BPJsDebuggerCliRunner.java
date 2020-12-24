package il.ac.bgu.se.bp.mains;

import il.ac.bgu.se.bp.execution.BPJsDebuggerRunner;

import java.util.Scanner;


public class BPJsDebuggerCliRunner {

    public static void main(String[] args) {
        BPJsDebuggerRunner bpJsDebuggerRunner = new BPJsDebuggerRunner();
        bpJsDebuggerRunner.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String cmd = "";
        Scanner sc = new Scanner(System.in);
        while(!cmd.equals("exit")){
            System.out.println("Enter command: b / rb / go ");
            cmd = sc.nextLine();
            String[] splat= cmd.split(" ");
            switch (splat[0]) {
                case "b": {
                    System.out.println("breakpoint on line " + splat[1]);
                    bpJsDebuggerRunner.setBreakPoint(Integer.parseInt(splat[1]));
                    break;
                }
                case "rb": {
                    System.out.println("remove breakpoint on line " + splat[1]);
                    bpJsDebuggerRunner.removeBreakPoint(Integer.parseInt(splat[1]));
                    break;
                }
                case "go": {
                    System.out.println("Go! ");
                    bpJsDebuggerRunner.go();
                    break;
                }
            }
            break;
        }


    }
}
