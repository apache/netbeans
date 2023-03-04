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

package org.netbeans.modules.java.hints.spiimpl.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import org.netbeans.modules.java.hints.providers.spi.HintDescription.Worker;
import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata.Options;
import org.netbeans.modules.java.hints.providers.spi.Trigger.Kinds;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFixUtilities;
import org.openide.LifecycleManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author user
 */
public class HintsInvokerTest extends TestBase {

    public HintsInvokerTest(String name) {
        super(name);
    }

//    public static TestSuite suite() {
//        NbTestSuite r = new NbTestSuite();
//        r.addTest(new HintsInvokerTest("testPatternVariable1"));
//        return r;
//    }

    public void testPattern1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "import java.io.File;\n" +
                            "public class Test {\n" +
                            "     private void test(File f) {\n" +
                            "         f.toURL();\n" +
                            "     }\n" +
                            "}\n",
                            "4:11-4:16:verifier:HINT");
    }

    public void testPattern2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private void test(java.io.File f) {\n" +
                            "         f.toURL();\n" +
                            "     }\n" +
                            "}\n",
                            "4:11-4:16:verifier:HINT");
    }

    public void testKind1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private void test(java.io.File f) {\n" +
                            "         f.toURL();\n" +
                            "     }\n" +
                            "}\n",
                            "4:11-4:16:verifier:HINT");
    }

    public void testPatternVariable1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         {\n" +
                       "             int y;\n" +
                       "             y = 1;\n" +
                       "         }\n" +
                       "         int z;\n" +
                       "         {\n" +
                       "             int y;\n" +
                       "             z = 1;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:10:verifier:HINT",
                       "FixImpl",
                       "package test; public class Test { private void test() { { int y = 1; } int z; { int y; z = 1; } } } ");
    }

    public void testPatternAssert1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private void test() {\n" +
                            "         assert true : \"\";\n" +
                            "     }\n" +
                            "}\n",
                            "4:9-4:15:verifier:HINT");
    }

    public void testPatternStatementAndSingleStatementBlockAreSame() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private int test() {\n" +
                            "         if (true) {\n" +
                            "             return 0;\n" +
                            "         }\n" +
                            "     }\n" +
                            "}\n",
                            "4:9-4:11:verifier:HINT");
    }

    public void testPatternFalseOccurrence() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "\n" +
                            "public class Test {\n" +
                            "     private int test(java.io.File f) {\n" +
                            "         f.toURI().toURL();\n" +
                            "     }\n" +
                            "}\n");
    }

    public void testStatementVariables1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (true)\n" +
                       "             System.err.println(1);\n" +
                       "         else\n" +
                       "             System.err.println(2);\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:11:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (false)\n" +
                       "             System.err.println(2);\n" +
                       "         else\n" +
                       "             System.err.println(1);\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testStatementVariables2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (true)\n" +
                       "             return 1;\n" +
                       "         else\n" +
                       "             return 2;\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:11:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(java.io.File f) {\n" +
                       "         if (false)\n" +
                       "             return 2;\n" +
                       "         else\n" +
                       "             return 1;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariables1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         int i = 3;\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n",
                       "3:29-3:30:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         float i = 3;\n" +
                       "         j++;\n" +
                       "         j++;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariables2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         int i = 3;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n",
                       "3:29-3:30:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test(int j) {\n" +
                       "         float i = 3;\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariables3() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         System.err.println();\n" +
                       "         System.err.println();\n" +
                       "         int i = 3;\n" +
                       "         System.err.println(i);\n" +
                       "         System.err.println(i);\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n",
                       "3:24-3:25:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         System.err.println();\n" +
                       "         System.err.println();\n" +
                       "         float i = 3;\n" +
                       "         System.err.println(i);\n" +
                       "         System.err.println(i);\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiStatementVariablesAndBlocks() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {" +
                       "         if (true)\n" +
                       "             System.err.println();\n" +
                       "     }\n" +
                       "}\n",
                       "3:35-3:37:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {" +
                       "         if (false) {\n" +
                       "             System.err.println();\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testOneStatement2MultipleBlock() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:32:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testOneStatement2MultipleStatement() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         if (true)\n" +
                       "             System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "5:13-5:36:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         if (true) {\n" +
                       "             System.err.println(\"\");\n" +
                       "             System.err.println(\"\");\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiple2OneStatement1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n",
                       "4:9-4:32:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         System.err.println(\"\");\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiple2OneStatement2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         int i = 0;\n" +
                       "         System.err.println(\"\");\n" +
                       "         System.err.println(\"\");\n" +
                       "         i++;\n" +
                       "     }\n" +
                       "}\n",
                       "5:9-5:32:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         int i = 0;\n" +
                       "         System.err.println(\"\");\n" +
                       "         i++;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMemberSelectInsideMemberSelect() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     public Test test;\n" +
                       "     public String name;\n" +
                       "     private void test() {\n" +
                       "         Test t = null;\n" +
                       "         String s = t.test.toString();\n" +
                       "     }\n" +
                       "}\n",
                       "7:22-7:26:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     public Test test;\n" +
                       "     public String name;\n" +
                       "     private void test() {\n" +
                       "         Test t = null;\n" +
                       "         String s = t.getTest().toString();\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testPackageInfo() throws Exception {
        performAnalysisTest("test/package-info.java",
                            "|package test;\n");
    }

    public void testSuppressWarnings() throws Exception {
        performAnalysisTest("test/Test.java",
                            "|package test;\n" +
                            "@SuppressWarnings(\"test\")\n" +
                            "public class Test {\n" +
                            "     public Test test;\n" +
                            "     public String name;\n" +
                            "     private void test() {\n" +
                            "         Test t = null;\n" +
                            "         String s = t.test.toString();\n" +
                            "     }\n" +
                            "}\n");
    }

    public void testRewriteOneToMultipleClassMembers() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int i;\n" +
                       "}\n",
                       "3:17-3:18:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private int i;\n" +
                       "     public int getI() {\n" +
                       "         return i;\n" +
                       "     }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testImports1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         new java.util.LinkedList();\n" +
                       "     }" +
                       "}\n",
                       "4:9-4:35:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         new ArrayList();\n" +
                       "     }" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testImports2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         LinkedList l;\n" +
                       "     }" +
                       "}\n",
                       "4:20-4:21:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         ArrayList l;\n" +
                       "     }" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testMultiParameters() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.asList(\"a\", \"b\", \"c\"); }\n" +
                       "}\n",
                       "3:14-3:20:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.asList(\"d\", \"a\", \"b\", \"c\"); }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTypeParametersMethod() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.<String>asList(\"a\", \"b\", \"c\"); }\n" +
                       "}\n",
                       "3:22-3:28:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.Arrays;\n" +
                       "public class Test {\n" +
                       "     { Arrays.<String>asList(\"d\", \"a\", \"b\", \"c\"); }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTypeParametersNewClass() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "import java.util.Arrays;\n" +
                       "import java.util.HashSet;\n" +
                       "public class Test {\n" +
                       "     { new HashSet<String>(Arrays.<String>asList(\"a\", \"b\", \"c\")); }\n" +
                       "}\n",
                       "4:7-4:64:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "import java.util.Arrays;\n" +
                       "import java.util.HashSet;\n" +
                       "public class Test {\n" +
                       "     { new HashSet<String>(Arrays.<String>asList(\"d\", \"a\", \"b\", \"c\")); }\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testChangeFieldType1() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     private String name = null;\n" +
                       "}\n",
                       "2:20-2:24:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     private CharSequence name = null;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testChangeFieldType2() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     String name = null;\n" +
                       "}\n",
                       "2:12-2:16:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     CharSequence name = null;\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testChangeFieldType3() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     private static final String name = \"test\".substring(0, 4);\n" +
                       "}\n",
                       "2:33-2:37:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     private static final CharSequence name = \"test\".substring(0, 4);\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testIdentifier() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     private int l;" +
                       "     {System.err.println(l);}\n" +
                       "}\n",
                       "2:44-2:45:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     private int l;" +
                       "     {System.err.println(2);}\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testLambda() throws Exception {
        performAnalysisTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     { new java.io.FileFilter() {public boolean accept(java.io.File f) { return true; } } }\n" +
                       "}\n",
                       "2:7-2:89:verifier:HINT");
    }

    public void testAddCasesToSwitch() throws Exception {
        performFixTest("test/Test.java",
                       "|package test;\n" +
                       "public class Test {\n" +
                       "     {\n" +
                       "         E e = null;\n" +
                       "         switch (e) {\n" +
                       "             case A: System.err.println(1); break;\n" +
                       "             case D: System.err.println(2); break;\n" +
                       "             case E: System.err.println(3); break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "     public enum E {A, B, C, D, E, F;}\n" +
                       "}\n",
                       "4:9-4:15:verifier:HINT",
                       "FixImpl",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "     {\n" +
                       "         E e = null;\n" +
                       "         switch (e) {\n" +
                       "             case A: System.err.println(1); break;\n" +
                       "             case B: case C:\n" +
//                       "             case C:\n" +
                       "             case D: System.err.println(2); break;\n" +
                       "             case E: System.err.println(3); break;\n" +
                       "         }\n" +
                       "     }\n" +
                       "     public enum E {A, B, C, D, E, F;}\n" +
                       "}\n").replaceAll("[ \t\n]+", " "));
    }

    private static final Map<String, HintDescription> test2Hint;

    static {
        test2Hint = new HashMap<String, HintDescription>();
        test2Hint.put("testPattern1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("$1.toURL()", Collections.singletonMap("$1", "java.io.File"))).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPattern2", test2Hint.get("testPattern1"));
        test2Hint.put("testKind1", HintDescriptionFactory.create().setTrigger(new Kinds(EnumSet.of(Kind.METHOD_INVOCATION))).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPatternVariable1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("{ $1 $2; $2 = $3; }", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("{ $1 $2 = $3; }")).produce());
        Map<String, String> constraints = new HashMap<String, String>();

        constraints.put("$1", "boolean");
        constraints.put("$2", "java.lang.Object");

        test2Hint.put("testPatternAssert1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("assert $1 : $2;", constraints)).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPatternStatementAndSingleStatementBlockAreSame", HintDescriptionFactory.create().setTrigger(PatternDescription.create("if ($1) return $2;", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testPatternFalseOccurrence", HintDescriptionFactory.create().setTrigger(PatternDescription.create("$1.toURL()", Collections.singletonMap("$1", "java.io.File"))).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testStatementVariables1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("if ($1) $2; else $3;", constraints)).setWorker(new WorkerImpl("if (!$1) $3; else $2;")).produce());
        test2Hint.put("testStatementVariables2", test2Hint.get("testStatementVariables1"));
        test2Hint.put("testMultiStatementVariables1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("{ $pref$; int $i = 3; $inf$; return $i; }", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("{ $pref$; float $i = 3; $inf$; return $i; }")).produce());
        test2Hint.put("testMultiStatementVariables2", test2Hint.get("testMultiStatementVariables1"));
        test2Hint.put("testMultiStatementVariables3", test2Hint.get("testMultiStatementVariables1"));
        test2Hint.put("testMultiStatementVariablesAndBlocks", HintDescriptionFactory.create().setTrigger(PatternDescription.create("if ($c) {$s1$; System.err.println(); $s2$; }", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("if (!$c) {$s1$; System.err.println(); $s2$; }")).produce());
        test2Hint.put("testOneStatement2MultipleBlock", HintDescriptionFactory.create().setTrigger(PatternDescription.create("System.err.println($1);", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("System.err.println($1); System.err.println($1);")).produce());
        test2Hint.put("testOneStatement2MultipleStatement", test2Hint.get("testOneStatement2MultipleBlock"));
        test2Hint.put("testMultiple2OneStatement1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("System.err.println($1); System.err.println($2);", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("System.err.println($1);")).produce());
        test2Hint.put("testMultiple2OneStatement2", test2Hint.get("testMultiple2OneStatement1"));
        test2Hint.put("testMemberSelectInsideMemberSelect", HintDescriptionFactory.create().setTrigger(PatternDescription.create("$Test.test", Collections.<String, String>singletonMap("$Test", "test.Test"))).setWorker(new WorkerImpl("$Test.getTest()")).produce());
        test2Hint.put("testPackageInfo", HintDescriptionFactory.create().setTrigger(PatternDescription.create("$Test.test", Collections.<String, String>singletonMap("$Test", "test.Test"))).setWorker(new WorkerImpl("$Test.getTest()")).produce());
        HintMetadata metadata = HintMetadata.Builder.create("no-id").addOptions(Options.NON_GUI).addSuppressWarnings("test").build();
        test2Hint.put("testSuppressWarnings", HintDescriptionFactory.create().setTrigger(PatternDescription.create("$Test.test", Collections.<String, String>singletonMap("$Test", "test.Test"))).setWorker(new WorkerImpl("$Test.getTest()")).setMetadata(metadata).produce());
        test2Hint.put("testRewriteOneToMultipleClassMembers", HintDescriptionFactory.create().setTrigger(PatternDescription.create("private int i;", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("private int i; public int getI() { return i; }")).produce());
//        test2Hint.put("testImports1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("new LinkedList()", Collections.<String, String>emptyMap(), "import java.util.LinkedList;")).setWorker(new WorkerImpl("new ArrayList()", "import java.util.ArrayList;\n")).produce());
//        test2Hint.put("testImports2", HintDescriptionFactory.create().setTrigger(PatternDescription.create("LinkedList $0;", Collections.<String, String>emptyMap(), "import java.util.LinkedList;")).setWorker(new WorkerImpl("ArrayList $0;", "import java.util.ArrayList;\n")).produce());
        test2Hint.put("testImports1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("new LinkedList()", Collections.<String, String>emptyMap(), "import java.util.LinkedList;")).setWorker(new WorkerImpl("new java.util.ArrayList()")).produce());
        test2Hint.put("testImports2", HintDescriptionFactory.create().setTrigger(PatternDescription.create("LinkedList $0;", Collections.<String, String>emptyMap(), "import java.util.LinkedList;")).setWorker(new WorkerImpl("java.util.ArrayList $0;")).produce());
        test2Hint.put("testMultiParameters", HintDescriptionFactory.create().setTrigger(PatternDescription.create("java.util.Arrays.asList($1$)", Collections.<String,String>emptyMap())).setWorker(new WorkerImpl("java.util.Arrays.asList(\"d\", $1$)")).produce());
        test2Hint.put("testTypeParametersMethod", HintDescriptionFactory.create().setTrigger(PatternDescription.create("java.util.Arrays.<$T>asList($1$)", Collections.<String,String>emptyMap())).setWorker(new WorkerImpl("java.util.Arrays.<$T>asList(\"d\", $1$)")).produce());
        test2Hint.put("testTypeParametersNewClass", HintDescriptionFactory.create().setTrigger(PatternDescription.create("new java.util.HashSet<$T1$>(java.util.Arrays.<$T$>asList($1$))", Collections.<String,String>emptyMap())).setWorker(new WorkerImpl("new java.util.HashSet<$T1$>(java.util.Arrays.<$T$>asList(\"d\", $1$))")).produce());
        test2Hint.put("testChangeFieldType1", HintDescriptionFactory.create().setTrigger(PatternDescription.create("$modifiers$ java.lang.String $name = $initializer;", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl("$modifiers$ java.lang.CharSequence $name = $initializer;")).produce());
        test2Hint.put("testChangeFieldType2", test2Hint.get("testChangeFieldType1"));
        test2Hint.put("testChangeFieldType3", test2Hint.get("testChangeFieldType1"));
        test2Hint.put("testIdentifier", HintDescriptionFactory.create().setTrigger(PatternDescription.create("$i", Collections.<String, String>singletonMap("$i", "int"))).setWorker(new WorkerImpl("2")).produce());
        test2Hint.put("testLambda", HintDescriptionFactory.create().setTrigger(PatternDescription.create("new $type() { $mods$ $retType $name($params$) { $body$; } }", Collections.<String, String>emptyMap())).setWorker(new WorkerImpl()).produce());
        test2Hint.put("testAddCasesToSwitch", HintDescriptionFactory.create().setTrigger(PatternDescription.create("switch ($var) { case $c1$; case D: $stmts$; case $c2$; }", Collections.<String,String>singletonMap("$var", "test.Test.E"))).setWorker(new WorkerImpl("switch ($var) { case $c1$ case B: case C: case D: $stmts$; case $c2$ }")).produce());
    }

//    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path, int pos) {
        HintDescription hd = test2Hint.get(getName());

        assertNotNull(hd);

        return new HintsInvoker(HintsSettings.getGlobalSettings(), new AtomicBoolean()).computeHints(info, Collections.singletonList(hd));
    }

//    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return "FixImpl";
    }

//    @Override
//    public void testIssue105979() throws Exception {}
//
//    @Override
//    public void testIssue108246() throws Exception {}
//
//    @Override
//    public void testIssue113933() throws Exception {}
//
//    @Override
//    public void testNoHintsForSimpleInitialize() throws Exception {}

    private static final class WorkerImpl implements Worker {

        private final String fix;

        public WorkerImpl() {
            this(null);
        }

        public WorkerImpl(String fix) {
            this.fix = fix;
        }

        @Override
        public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
            if (ctx.getInfo().getTreeUtilities().isSynthetic(ctx.getPath())) {
                return null;
            }

            List<Fix> fixes = new LinkedList<Fix>();

            if (fix != null) {
                fixes.add(JavaFixUtilities.rewriteFix(ctx, "Rewrite", ctx.getPath(), fix));
            }
            
            return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "HINT", fixes.toArray(new Fix[0])));
        }
    }


    //XXX:copied from TreeRuleTestBase:

    protected void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        int[] offset = new int[1];

        code = org.netbeans.modules.java.hints.spiimpl.TestUtilities.detectOffsets(code, offset);

        performAnalysisTest(fileName, code, offset[0], golden);
    }

    protected void performAnalysisTest(String fileName, String code, int pos, String... golden) throws Exception {
        prepareTest(fileName, code);

        TreePath path = info.getTreeUtilities().pathFor(pos);

        List<ErrorDescription> errors = computeErrors(info, path, pos);
        List<String> errorsNames = new LinkedList<String>();

        errors = errors != null ? errors : Collections.<ErrorDescription>emptyList();

        for (ErrorDescription e : errors) {
            errorsNames.add(e.toString());
        }

        assertTrue("The warnings provided by the hint do not match expected warnings. Provided warnings: " + errorsNames.toString(), Arrays.equals(golden, errorsNames.toArray(new String[0])));
    }

    protected String performFixTest(String fileName, String code, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        int[] offset = new int[1];

        code = org.netbeans.modules.java.hints.spiimpl.TestUtilities.detectOffsets(code, offset);

        return performFixTest(fileName, code, offset[0], errorDescriptionToString, fixDebugString, golden);
    }

    protected String performFixTest(String fileName, String code, int pos, String errorDescriptionToString, String fixDebugString, String golden) throws Exception {
        return performFixTest(fileName, code, pos, errorDescriptionToString, fixDebugString, fileName, golden);
    }

    protected String performFixTest(String fileName, String code, String errorDescriptionToString, String fixDebugString, String goldenFileName, String golden) throws Exception {
        int[] offset = new int[1];

        code = org.netbeans.modules.java.hints.spiimpl.TestUtilities.detectOffsets(code, offset);

        return performFixTest(fileName, code, offset[0], errorDescriptionToString, fixDebugString, goldenFileName, golden);
    }

    protected String performFixTest(String fileName, String code, int pos, String errorDescriptionToString, String fixDebugString, String goldenFileName, String golden) throws Exception {
        prepareTest(fileName, code);

        TreePath path = info.getTreeUtilities().pathFor(pos);

        List<ErrorDescription> errors = computeErrors(info, path, pos);

        ErrorDescription toFix = null;

        for (ErrorDescription d : errors) {
            if (errorDescriptionToString.equals(d.toString())) {
                toFix = d;
                break;
            }
        }

        assertNotNull("Error: \"" + errorDescriptionToString + "\" not found. All ErrorDescriptions: " + errors.toString(), toFix);

        assertTrue("Must be computed", toFix.getFixes().isComputed());

        List<Fix> fixes = toFix.getFixes().getFixes();
        List<String> fixNames = new LinkedList<String>();
        Fix toApply = null;

        for (Fix f : fixes) {
            if (fixDebugString.equals(toDebugString(info, f))) {
                toApply = f;
            }

            fixNames.add(toDebugString(info, f));
        }

        assertNotNull("Cannot find fix to invoke: " + fixNames.toString(), toApply);

        toApply.implement();

        FileObject toCheck = sourceRoot.getFileObject(goldenFileName);

        assertNotNull(toCheck);

        DataObject toCheckDO = DataObject.find(toCheck);
        EditorCookie ec = toCheckDO.getLookup().lookup(EditorCookie.class);
        Document toCheckDocument = ec.openDocument();

        String realCode = toCheckDocument.getText(0, toCheckDocument.getLength());

        //ignore whitespaces:
        realCode = realCode.replaceAll("[ \t\n]+", " ");

        if (golden != null) {
            assertEquals("The output code does not match the expected code.", golden, realCode);
        }

        LifecycleManager.getDefault().saveAll();

        return realCode;
    }
}