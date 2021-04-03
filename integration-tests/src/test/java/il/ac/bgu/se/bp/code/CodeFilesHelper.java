package il.ac.bgu.se.bp.code;

public class CodeFilesHelper {

    public static String getCodeByFileName(String filename) {
        switch (filename) {
            case "testFile1":
                return filename1();
            case "testFile2":
                return filename2();
            case "testFile3":
                return filename3();
            case "testFile4":
                return filename4();
        }
        return null;
    }

    private static String filename1() {
        return
                "bp.registerBThread('bt-world', function () {\n" +
                        "    var myvar1 = 10;\n" +
                        "    var myvar2 = 20;\n" +
                        "    bp.registerBThread('bt-world-son', function () {\n" +
                        "        bp.sync({ request: bp.Event('son-e') });\n" +
                        "        var x = 5;\n" +
                        "        var y = 16.7;\n" +
                        "        foo(3)\n" +
                        "        var z = \"alex\"\n" +
                        "        var tt = \"hello\"\n" +
                        "        bp.sync({ request: bp.Event('world12121') });\n" +
                        "    })\n" +
                        "    foo(1)\n" +
                        "    var z = myvar1 + 5\n" +
                        "})\n" +
                        "\n" +
                        "function foo(bt) {\n" +
                        "    var m = 50;\n" +
                        "    var n = 100;\n" +
                        "    var p = m + n;\n" +
                        "    goo()\n" +
                        "    const t = 200\n" +
                        "}\n" +
                        "\n" +
                        "function goo() {\n" +
                        "    var g1 = 50;\n" +
                        "    var g2 = 100;\n" +
                        "    const g3 = 200\n" +
                        "}\n" +
                        "\n";
    }

    private static String filename2() {
        return
                "bp.registerBThread(\"Thread1\",function(){\n" +
                        "    var t = 6;\n" +
                        "    bp.sync({request:bp.Event(\"Thread1-EVENT\")});\n" +
                        "    var innerVarT1After = 2;\n" +
                        "})\n" +
                        "\n" +
                        "bp.registerBThread(\"Thread2\", function(){\n" +
                        "    var innerVarT2Before = 1;\n" +
                        "    bp.sync({request:bp.Event(\"Thread2-EVENT\")});\n" +
                        "    var innerVarT2After = 2;\n" +
                        "})";
    }

    private static String filename3() {
        return null;
    }

    private static String filename4() {
        return null;
    }
}
