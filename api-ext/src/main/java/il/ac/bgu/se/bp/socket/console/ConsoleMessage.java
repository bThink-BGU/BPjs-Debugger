package il.ac.bgu.se.bp.socket.console;

import java.io.Serializable;
import java.util.Objects;

public class ConsoleMessage implements Serializable {
    private static final long serialVersionUID = -5291444590847219727L;

    private String message;

    public ConsoleMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsoleMessage that = (ConsoleMessage) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    @Override
    public String toString() {
        return "ConsoleMessage{" +
                "message='" + message + '\'' +
                '}';
    }
}
