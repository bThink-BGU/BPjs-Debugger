package il.ac.bgu.se.bp.debugger;


import java.util.Objects;

public class DebuggerCommand {
    private final DebuggerOperations debuggerOperation;
    private final Object[] args;

    public DebuggerCommand(DebuggerOperations debuggerOperation, Object... args) {
        this.debuggerOperation = debuggerOperation;
        this.args = args;
    }

    public DebuggerOperations getDebuggerOperation() {
        return debuggerOperation;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebuggerCommand that = (DebuggerCommand) o;
        return debuggerOperation == that.debuggerOperation && Objects.equals(args, that.args);
    }
    //comment
    @Override
    public int hashCode() {
        return Objects.hash(debuggerOperation, args);
    }

    @Override
    public String toString() {
        return "DebuggerCommand{" +
                "debuggerOperation=" + debuggerOperation +
                ", args=" + args +
                '}';
    }
}