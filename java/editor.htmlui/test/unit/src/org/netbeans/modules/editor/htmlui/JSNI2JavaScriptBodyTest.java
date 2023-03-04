/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.editor.htmlui;

import net.java.html.js.JavaScriptBody;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;
import org.testng.annotations.Test;

public class JSNI2JavaScriptBodyTest {
    @Test
    public void testFixWorking() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public native void run(int a) /*-{ this.a = a; }-*/;\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(JSNI2JavaScriptBody.class)
                .findWarning("2:23-2:26:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import net.java.html.js.JavaScriptBody;\n" +
                              "public class Test {\n" +
                              "    @JavaScriptBody(args = {\"a\"}, body = \" this.a = a; \")\n" +
                              "    public native void run(int a);\n" +
                              "}\n");
    }

    @Test public void testUseQuote() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public native void alert() /*-{ alert(\"Pozor!\"); }-*/;\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(JSNI2JavaScriptBody.class)
                .findWarning("2:23-2:28:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import net.java.html.js.JavaScriptBody;\n" +
                              "public class Test {\n" +
                              "    @JavaScriptBody(args = {}, body = \" alert(\\\"Pozor!\\\"); \")\n" +
                              "    public native void alert();\n" +
                              "}\n");
    }

    @Test
    public void test1() throws Exception {
        String s = "class Test {\n"
            + "    /** javadoc */\n"
            + "    public native void test() /*-{\n"
            + "        // body\n"
            + "    }-*/;\n"
            + "}\n";

        String expected = " import net.java.html.js.JavaScriptBody;\n"
            + "class Test {\n"
            + "\n"
            + "    /** javadoc */\n"
            + "    @JavaScriptBody(args = {}, body = \"\\n\" + \"        // body\\n\" + \" \")\n"
            + "    public native void test();\n"
            + "}\n";

        HintTest.create()
            .input(s)
            .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
            .run(JSNI2JavaScriptBody.class)
            .findWarning("2:23-2:27:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
            .applyFix()
            .assertCompilable()
            .assertOutput(expected);
    }

    @Test
    public void test2() throws Exception {
        String s = "class Test {\n"
            + "    /** javadoc */\n"
            + "    @SuppressWarnings(\"unused\")\n"
            + "    // comment\n"
            + "    public native void test() /*-{\n"
            + "        // body\n"
            + "    }-*/;\n"
            + "}\n";

        String expected = " import net.java.html.js.JavaScriptBody;\n"
            + "class Test {\n"
            + "\n"
            + "    /** javadoc */\n"
            + "    @SuppressWarnings(\"unused\")\n"
            + "    // comment\n"
            + "    @JavaScriptBody(args = {}, body = \"\\n\" + \"        // body\\n\" + \"  \")\n"
            + "    public native void test();\n"
            + "}\n";
        HintTest.create()
            .input(s)
            .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
            .run(JSNI2JavaScriptBody.class)
            .findWarning("4:23-4:27:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
            .applyFix()
            .assertCompilable()
            .assertOutput(expected);
    }

    static String append(StringBuilder sb, String x) {
        sb.append(x);
        return sb.toString();
    }

    @Test
    public void testWithStaticMethodCall() throws Exception {
        String s = "class Test {\n"
            + "    /** javadoc */\n"
            + "    @SuppressWarnings(\"unused\")\n"
            + "    // comment\n"
            + "    public native void test(String builder) /*-{\n"
            + "        @org.netbeans.modules.project.jsjava.JSNI2JavaScriptBodyTest::append(Ljava/lang/StringBuilder;Ljava/lang/String;)(builder, 'Ahoj');\n"
            + "    }-*/;\n"
            + "}\n";

        String expected = " import net.java.html.js.JavaScriptBody;\n"
            + "class Test {\n"
            + "\n"
            + "    /** javadoc */\n"
            + "    @SuppressWarnings(\"unused\")\n"
            + "    // comment\n"
            + "    @JavaScriptBody(args = {\"builder\"}, javacall = true, body = \"\\n\" + "
            + "    \"    @org.netbeans.modules.project.jsjava.JSNI2JavaScriptBodyTest::append(Ljava/lang/StringBuilder;Ljava/lang/String;)(builder, 'Ahoj');\\n\" + \"  \")\n"
            + "    public native void test(String builder);\n"
            + "}\n";
        HintTest.create()
            .input(s)
            .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
            .run(JSNI2JavaScriptBody.class)
            .findWarning("4:23-4:27:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
            .applyFix()
            .assertCompilable()
            .assertOutput(expected);
    }

    @Test
    public void testWithInstanceMethodCall() throws Exception {
        String s = "class Test {\n"
            + "    /** javadoc */\n"
            + "    @SuppressWarnings(\"unused\")\n"
            + "    // comment\n"
            + "    public native void test(String builder) /*-{\n"
            + "        builder.@java.lang.StringBuilder::append(Ljava/lang/String;)('Ahoj');\n"
            + "    }-*/;\n"
            + "}\n";

        String expected = " import net.java.html.js.JavaScriptBody;\n"
            + "class Test {\n"
            + "\n"
            + "    /** javadoc */\n"
            + "    @SuppressWarnings(\"unused\")\n"
            + "    // comment\n"
            + "    @JavaScriptBody(args = {\"builder\"}, javacall = true, body = \"\\n\" + \""
            + "        builder.@java.lang.StringBuilder::append(Ljava/lang/String;)('Ahoj');\\n\" + \"    \")"
            + "    public native void test(String builder);\n"
            + "}\n";
        HintTest.create()
            .input(s)
            .classpath(FileUtil.getArchiveRoot(JavaScriptBody.class.getProtectionDomain().getCodeSource().getLocation()))
            .run(JSNI2JavaScriptBody.class)
            .findWarning("4:23-4:27:verifier:" + Bundle.ERR_JSNI2JavaScriptBody())
            .applyFix()
            .assertCompilable()
            .assertOutput(expected);
    }

}
