package il.ac.bgu.se.bp.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.debugger.Dim;

import java.io.IOException;

public class Common {
    public static final BEvent NO_MORE_WAIT_EXTERNAL = new BEvent("___bpjs-internal____NO_MORE_WAIT_EXTERNAL");

    public static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new FunctionModule())
            .registerModule(new StackFrameModule());

    public static <T> T cast(Object object) {
        return (T) object;
    }

    private static class StackFrameModule extends SimpleModule {

        private static final long serialVersionUID = -337535159557963163L;

        {
            addDeserializer(Dim.StackFrame.class, new StackFrameDeserializer());
        }

        private static class StackFrameDeserializer extends StdDeserializer<Dim.StackFrame> {
            private static final long serialVersionUID = -9076663982576848660L;

            StackFrameDeserializer() {
                super(Dim.StackFrame.class);
            }


            @Override
            public Dim.StackFrame deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return jsonParser.readValueAs(Dim.StackFrame.class);
            }
        }
    }


    private static class FunctionModule extends SimpleModule {

        private static final long serialVersionUID = -337535159557963163L;

        {
            addDeserializer(Function.class, new FunctionDeserializer<NativeContinuation>());
            addDeserializer(Function.class, new FunctionDeserializer<ArrowFunction>());
            addDeserializer(Function.class, new FunctionDeserializer<BaseFunction>());
            addDeserializer(Function.class, new FunctionDeserializer<BoundFunction>());
            addDeserializer(Function.class, new FunctionDeserializer<Delegator>());
            addDeserializer(Function.class, new FunctionDeserializer<FunctionObject>());
            addDeserializer(Function.class, new FunctionDeserializer<IdFunctionObject>());
            addDeserializer(Function.class, new FunctionDeserializer<IdFunctionObjectES6>());
            addDeserializer(Function.class, new FunctionDeserializer<NativeContinuation>());
            addDeserializer(Function.class, new FunctionDeserializer<NativeFunction>());
            addDeserializer(Function.class, new FunctionDeserializer<NativeJavaClass>());
            addDeserializer(Function.class, new FunctionDeserializer<NativeJavaConstructor>());
            addDeserializer(Function.class, new FunctionDeserializer<NativeJavaMethod>());
            addDeserializer(Function.class, new FunctionDeserializer<NativeJavaTopPackage>());
            addDeserializer(Function.class, new FunctionDeserializer<Synchronizer>());
        }

        private static class FunctionDeserializer<T extends Function> extends StdDeserializer<Function> {
            private T ignored;

            private static final long serialVersionUID = -9076663982576848660L;

            FunctionDeserializer() {
                super(Function.class);
            }

            @Override
            public Function deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                return jsonParser.readValueAs(ignored.getClass());
            }
        }
    }
}
