package il.ac.bgu.cs.bp.bpjs.debugger;

import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Kit;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.tools.debugger.Dim;
import org.mozilla.javascript.tools.debugger.ScopeProvider;
import org.mozilla.javascript.tools.shell.Global;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    /**
     * Reads the file with the given name and returns its contents as a String.
     */
    private static String readFile(String fileName) {
        String text;
        try {
            try (Reader r = new FileReader(fileName)) {
                text = Kit.readReader(r);
            }
        } catch (IOException ex) {
            System.out.println("Error reading " + fileName);
            text = null;
        }
        return text;
    }

    public static void main(String[] args)  {
        String filename = "BPJSDebuggerTest.js";
//        String filename = "try.js";
        Path p = null;
        try {
           p = Paths.get(ClassLoader.getSystemResource(filename).toURI());
            System.out.println(p);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ShellGui shellGui = new ShellGui();
        Dim dim = new Dim();
        dim.setGuiCallback(shellGui);
        shellGui.setDim(dim);
        ContextFactory factory = ContextFactory.getGlobal();
        dim.attachTo(factory);
//
//        final Global global = new Global();
//        global.init(factory);
//
//
//        dim.setScopeProvider(new ScopeProvider() {
//            @Override
//            public Scriptable getScope() {
//                return global;
//            }
//        });


      //  cx.compileString(readFile("./try.js"),"./try.js", 1, null );

        //dim.evalScript("./try.js", readFile("./try.js"));

        final BProgram bprog = new ResourceBProgram("BPJSDebuggerTest.js");

        BProgramRunner rnr = new BProgramRunner();
        rnr.addListener(new PrintBProgramRunnerListener());
        rnr.setBProgram(bprog);
        bprog.setup();
        Dim.SourceInfo sourceInfo = dim.sourceInfo(filename);
//        sourceInfo.breakpoint(2, true);
        sourceInfo.breakpoint(4, true);
        sourceInfo.breakpoint(10, true);
//        sourceInfo.breakpoint(10, true);
//        sourceInfo.breakpoint(11, true);
//        sourceInfo.breakpoint(12, true);
shellGui.setbProgramRunner(rnr);
        rnr.run();


//        dim.compileScript(filename, readFile(p.toString()));
//        Dim.SourceInfo sourceInfo = dim.sourceInfo(filename);
////        sourceInfo.breakpoint(2, true);
////        sourceInfo.breakpoint(3, true);
//        dim.evalScript(filename, readFile(p.toString()));



//
//        try {
//
//            Context cx = ContextFactory.getGlobal().enterContext();
//            Global global = new Global(cx);
//            cx.setLanguageVersion(Context.VERSION_ES6);
//            ImporterTopLevel importer = new ImporterTopLevel(cx);
//            Dim dim = new Dim();
//            dim.setBreak();
//            dim.attachTo(ContextFactory.getGlobal());
//            Scriptable programScope = cx.initStandardObjects(importer);
//
////            Object wrappedOut = Context.javaToJS(System.out, programScope);
////            ScriptableObject.putProperty(programScope, "out", wrappedOut);
//            String source= "var tal=5 \n tal=tal+6 \n tal=tal+3";
//
////            String source = "var tal=5\n" +
////                    "tal=tal+6\n" +
////                    "tal=tal+3\n" +
////                    "print(tal)\n" +
////                    "\n" +
////                    "function zzz (){\n" +
////                    "    var i = 100;\n" +
////                    "    i = i +1;\n" +
////                    "    return i;\n" +
////                    "}";
//
//            Script s = cx.compileString(source, "try.js", 1 ,null);
//            DebuggableScript ds = Context.getDebuggableView(s);
////            Dim.SourceInfo si = dim.sourceInfo("./try.js");
////            si.breakpoint(2, true);
////            Object o =  cx.evaluateString(programScope, source, "try.js", 1, null);
//            Object o =  dim.eval(source);
//            System.out.println(cx.toString(o));
//            Function fct = (Function) programScope.get("zzz", programScope); // call doImport()
//
//
//        } catch (EcmaError rerr) {
//            System.out.println(rerr);
//        }
    }
}
