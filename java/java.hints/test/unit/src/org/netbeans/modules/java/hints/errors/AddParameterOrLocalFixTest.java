/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.util.TreePath;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.errors.ErrorFixesFakeHint.FixKind;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsTestBase;
import org.netbeans.modules.java.hints.spiimpl.JavaFixImpl;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class AddParameterOrLocalFixTest extends ErrorHintsTestBase {
    
    public AddParameterOrLocalFixTest(String testName) {
        super(testName, CreateElement.class);
    }

    public void testAddBeforeVararg() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(String... a) {bbb = 0;}}",
                       91 - 25,
                       "AddParameterOrLocalFix:bbb:int:PARAMETER",
                       "package test; public class Test {public void test(int bbb, String... a) {bbb = 0;}}");
    }

    public void testAddToTheEnd() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test(String[] a) {bbb = 0;}}",
                       90 - 25,
                       "AddParameterOrLocalFix:bbb:int:PARAMETER",
                       "package test; public class Test {public void test(String[] a, int bbb) {bbb = 0;}}");
    }

    public void testAddToTheEmptyParamsList() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {bbb = 0;}}",
                       80 - 25,
                       "AddParameterOrLocalFix:bbb:int:PARAMETER",
                       "package test; public class Test {public void test(int bbb) {bbb = 0;}}");
    }

    public void testAddLocalVariableWithComments() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {int a;\n //test\n |bbb = 0;\n int c; }}",
                       "AddParameterOrLocalFix:bbb:int:LOCAL_VARIABLE",
                       "package test; public class Test {public void test() {int a; //test int bbb = 0; int c; }}");
    }

    public void testAddLocalVariableNotInPlace() throws Exception {
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.CREATE_LOCAL_VARIABLE);
        boolean orig = ErrorFixesFakeHint.isCreateLocalVariableInPlace(prefs);

        try {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(prefs, false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public void test() {int a;\n |bbb = 0;\n int c; }}",
                    "AddParameterOrLocalFix:bbb:int:LOCAL_VARIABLE",
                    "package test; public class Test {public void test() {int bbb; int a; bbb = 0; int c; }}");
        } finally {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(prefs, orig);
        }
    }

    public void testAddLocalVariableNotInPlaceInConstr() throws Exception {
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.CREATE_LOCAL_VARIABLE);
        boolean orig = ErrorFixesFakeHint.isCreateLocalVariableInPlace(prefs);

        try {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(prefs, false);

            performFixTest("test/Test.java",
                    "package test; public class Test {public Test() {super();\n int a;\n |bbb = 0;\n int c; }}",
                    "AddParameterOrLocalFix:bbb:int:LOCAL_VARIABLE",
                    "package test; public class Test {public Test() {super(); int bbb; int a; bbb = 0; int c; }}");
        } finally {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(prefs, orig);
        }
    }

    public void testInsideBlock() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {if (true) {int aaa = 0; |bbb = aaa; }}}",
                       "AddParameterOrLocalFix:bbb:int:LOCAL_VARIABLE",
                       "package test; public class Test {public void test() {if (true) {int aaa = 0; int bbb = aaa; }}}");
    }

    public void testInsideBlockWithPreviousDeclaration() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {Object[] array = new Object[10];for (int i = 0; i < array.length; i++) {Object item = array[i + 1];item = array[i];}int j = 0;while (j < 10) {|item = array[j];j--;}}}",
                       "AddParameterOrLocalFix:item:java.lang.Object:LOCAL_VARIABLE",
                       "package test; public class Test {public void test() {Object[] array = new Object[10];for (int i = 0; i < array.length; i++) {Object item = array[i + 1];item = array[i];}int j = 0;while (j < 10) {Object item = array[j]; j--;}}}");
    }

    public void testInsideParentBlock() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void test() {{foo = \"bar\";}|foo = \"bar\";}}",
                       "AddParameterOrLocalFix:foo:java.lang.String:LOCAL_VARIABLE",
                       "package test; public class Test {public void test() {String foo; {foo = \"bar\";}foo = \"bar\";}}");
    }

    public void testEnhancedForLoopEmptyList() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (ttt : java.util.Collections.emptyList()) {}\n" +
                "     }\n" +
                "}\n",
                -1,
                "AddParameterOrLocalFix:ttt:java.lang.Object:LOCAL_VARIABLE",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (Object ttt : java.util.Collections.emptyList()) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopExtendedNumber() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         java.util.List<? extends Number> l = null;\n" +
                "         for (ttt : l) {}\n" +
                "     }\n" +
                "}\n",
                -1,
                "AddParameterOrLocalFix:ttt:java.lang.Number:LOCAL_VARIABLE",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         java.util.List<? extends Number> l = null;\n" +
                "         for (Number ttt : l) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopStringArray() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         String[] a = null;\n" +
                "         for (ttt : a) {}\n" +
                "     }\n" +
                "}\n",
                -1,
                "AddParameterOrLocalFix:ttt:java.lang.String:LOCAL_VARIABLE",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         String[] a = null;\n" +
                "         for (String ttt : a) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopPrimitiveArray() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         int[] a = null;\n" +
                "         for (ttt : a) {}\n" +
                "     }\n" +
                "}\n",
                -1,
                "AddParameterOrLocalFix:ttt:int:LOCAL_VARIABLE",
                ("package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         int[] a = null;\n" +
                "         for (int ttt : a) {}\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopNotImported() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (date : someMethod()) {\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n",
                -1,
                "AddParameterOrLocalFix:date:java.util.Date:LOCAL_VARIABLE",
                ("package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (Date date : someMethod()) {\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testEnhancedForLoopInsideItsBody() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (date : someMethod()) {\n" +
                "             Date local = |date;\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:date:java.util.Date:LOCAL_VARIABLE",
                ("package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public void test() {\n" +
                "         for (Date date : someMethod()) {\n" +
                "             Date local = date;\n" +
                "         }\n" +
                "     }\n" +
                "     private Iterable<java.util.Date> someMethod() {\n" +
                "         return null;\n" +
                "     }\n" +
                "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testAssignmentToValid181120() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.util.Date;\n" +
                "public class Test {\n" +
                "     public String test(int i) {\n" +
                "         String s;\n" +
                "         s = test(i|i);\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:ii:int:LOCAL_VARIABLE",
                ("package test;\n" +
                 "import java.util.Date;\n" +
                 "public class Test {\n" +
                 "     public String test(int i) {\n" +
                 "         String s;\n" +
                 "         int ii;\n" +
                 "         s = test(ii);\n" +
                 "     }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test189687a() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "     {\n" +
                "         i|i = 1;\n" +
                "     }\n" +
                "}\n",
                "AddParameterOrLocalFix:ii:int:LOCAL_VARIABLE",
                ("package test;\n" +
                 "public class Test {\n" +
                 "     {\n" +
                 "         int ii = 1;\n" +
                 "     }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test189687b() throws Exception {
        Preferences prefs = ErrorFixesFakeHint.getPreferences(null, FixKind.CREATE_LOCAL_VARIABLE);
        final boolean oldUse55 = ErrorFixesFakeHint.isCreateLocalVariableInPlace(prefs);

        try {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(prefs, false);
            performFixTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    "     {\n" +
                    "         i|i = 1;\n" +
                    "     }\n" +
                    "}\n",
                    "AddParameterOrLocalFix:ii:int:LOCAL_VARIABLE",
                    ("package test;\n" +
                     "public class Test {\n" +
                     "     {\n" +
                     "         int ii;\n" +
                     "         ii = 1;\n" +
                     "     }\n" +
                     "}\n").replaceAll("[ \t\n]+", " "));
        } finally {
            ErrorFixesFakeHint.setCreateLocalVariableInPlace(prefs, oldUse55);
        }
    }

    public void test204584a() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.util.*;\n" +
                "public class Test {\n" +
                "    public void method(List<String> l) {\n" +
                "        if (true) {\n" +
                "            for (Object o : l) {\n" +
                "                String aa = (String) o;\n" +
                "                System.err.println(aa);\n" +
                "            }\n" +
                "            Iterator<String> it = l.iterator();\n" +
                "            while (it.hasNext()) {\n" +
                "                a|a = it.next();\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}\n",
                "AddParameterOrLocalFix:aa:java.lang.String:LOCAL_VARIABLE",
                ("package test;\n" +
                 "import java.util.*;\n" +
                 "public class Test {\n" +
                 "    public void method(List<String> l) {\n" +
                 "        if (true) {\n" +
                 "            for (Object o : l) {\n" +
                 "                String aa = (String) o;\n" +
                 "                System.err.println(aa);\n" +
                 "            }\n" +
                 "            Iterator<String> it = l.iterator();\n" +
                 "            while (it.hasNext()) {\n" +
                 "                String aa = it.next();\n" +
                 "            }\n" +
                 "        }\n" +
                 "    }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test204584b() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.util.*;\n" +
                "public class Test {\n" +
                "    public void method(int i) {\n" +
                "        switch (i) {\n" +
                "            case 1:\n" +
                "                aa = 2;\n" +
                "                a|a = 2;\n" +
                "                break;   \n" +
                "        }\n" +
                "    }\n" +
                "}\n",
                "AddParameterOrLocalFix:aa:int:LOCAL_VARIABLE",
                ("package test;\n" +
                 "import java.util.*;\n" +
                 "public class Test {\n" +
                "    public void method(int i) {\n" +
                "        switch (i) {\n" +
                "            case 1:\n" +
                "                int aa = 2;\n" +
                "                aa = 2;\n" +
                "                break;\n" +
                "        }\n" +
                "    }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void test204584c() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.util.*;\n" +
                "public class Test {\n" +
                "    public void method(int i) {\n" +
                "        if (i == 0)\n" +
                "            v|ar = \"\";\n" +
                "    }\n" +
                "}\n",
                "AddParameterOrLocalFix:var:java.lang.String:LOCAL_VARIABLE",
                ("package test;\n" +
                 "import java.util.*;\n" +
                 "public class Test {\n" +
                "    public void method(int i) {\n" +
                "        String var;\n" +
                "        if (i == 0)\n" +
                "            var = \"\";\n" +
                "    }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }
    
    public void test206536() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "public class Test {\n" +
                "    interface Foo<T> {\n" +
                "        Foo<? extends ThreadLocal<? extends T>> foo();\n" +
                "    }\n" +
                "    private void t() {\n" +
                "        Foo<? extends Number> bar = null;\n" +
                "        fo|o = bar.foo();\n" +
                "    }\n" +
                "}\n",
                "AddParameterOrLocalFix:foo:test.Test.Foo<? extends java.lang.ThreadLocal<? extends java.lang.Number>>:LOCAL_VARIABLE",
                ("package test;\n" +
                 "public class Test {\n" +
                 "    interface Foo<T> {\n" +
                 "        Foo<? extends ThreadLocal<? extends T>> foo();\n" +
                 "    }\n" +
                 "    private void t() {\n" +
                 "        Foo<? extends Number> bar = null;\n" +
                 "        Foo<? extends ThreadLocal<? extends Number>> foo = bar.foo();\n" +
                 "    }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTWR1() throws Exception {
        performFixTest("test/Test.java",
                "package test;\n" +
                "import java.io.*;\n" +
                "public class Test {\n" +
                "    private void t(File f) {\n" +
                "        try (|in = new FileInputStream(f)) {\n" +
                "        }\n" +
                "    }\n" +
                "}\n",
                "AddParameterOrLocalFix:in:java.io.FileInputStream:RESOURCE_VARIABLE",
                ("package test;\n" +
                 "import java.io.*;\n" +
                 "public class Test {\n" +
                 "    private void t(File f) {\n" +
                 "        try (FileInputStream in = new FileInputStream(f)) {\n" +
                 "        }\n" +
                 "    }\n" +
                 "}\n").replaceAll("[ \t\n]+", " "));
    }

    public void testTWR2() throws Exception {
        performAnalysisTest("test/Test.java",
                "package test;\n" +
                "import java.io.*;\n" +
                "public class Test {\n" +
                "    private void t(File f) {\n" +
                "        try (|in = new FileInputStream(f)) {\n" +
                "        }\n" +
                "    }\n" +
                "}\n",
                "AddParameterOrLocalFix:in:java.io.FileInputStream:RESOURCE_VARIABLE");
    }

    public void testLocalInLambdaInField() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                public class Test {
                    Runnable r = () -> {
                        i|i = 0;
                    };
                }
                """,
                "AddParameterOrLocalFix:ii:int:LOCAL_VARIABLE",
                """
                package test;
                public class Test {
                    Runnable r = () -> {
                        int ii = 0;
                    };
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    public void testForLoopInit1() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                import java.util.*;
                public class Test {
                    private void t(Set<String> s) {
                        for (|it = s.iterator(); ;) {
                        }
                    }
                }
                """,
                "AddParameterOrLocalFix:it:java.util.Iterator<java.lang.String>:OTHER",
                """
                package test;
                import java.util.*;
                public class Test {
                    private void t(Set<String> s) {
                        for (Iterator<String> it = s.iterator(); ;) {
                        }
                    }
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    public void testForLoopInit2() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                import java.util.*;
                public class Test {
                    private void t(Set<String> s) {
                        for (|it = s.iterator()) {
                        }
                    }
                }
                """,
                "AddParameterOrLocalFix:it:java.util.Iterator<java.lang.String>:OTHER",
                """
                package test;
                import java.util.*;
                public class Test {
                    private void t(Set<String> s) {
                        for (Iterator<String> it = s.iterator()) {
                        }
                    }
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    public void testForLoopInit3() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                import java.util.*;
                public class Test {
                    private void t(Set<String> s) {
                        for (|it = s.iterator())
                    }
                }
                """,
                "AddParameterOrLocalFix:it:java.util.Iterator<java.lang.String>:OTHER",
                """
                package test;
                import java.util.*;
                public class Test {
                    private void t(Set<String> s) {
                        for (Iterator<String> it = s.iterator())
                    }
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    public void testTargetTypeFromYield() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                public class Test {
                    private String t(int i) {
                        return switch (i) {
                            default -> { yield |newVar; }
                        };
                    }
                }
                """,
                "AddParameterOrLocalFix:newVar:java.lang.String:LOCAL_VARIABLE",
                """
                package test;
                public class Test {
                    private String t(int i) {
                        return switch (i) {
                            default -> { String newVar; yield newVar; }
                        };
                    }
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    public void testTargetTypeFromCase() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                public class Test {
                    private String t(int i) {
                        return switch (i) {
                            default -> |newVar;
                        };
                    }
                }
                """,
                "AddParameterOrLocalFix:newVar:java.lang.String:LOCAL_VARIABLE",
                """
                package test;
                public class Test {
                    private String t(int i) {
                        String newVar;
                        return switch (i) {
                            default -> newVar;
                        };
                    }
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    public void testLambdaReturnType1() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                import java.util.*;
                import java.util.function.*;
                public class Test {
                    private Function<String,  List<Number>> t() {
                        return x -> |newVar;
                    }
                }
                """,
                "AddParameterOrLocalFix:newVar:java.util.List<java.lang.Number>:LOCAL_VARIABLE",
                """
                package test;
                import java.util.*;
                import java.util.function.*;
                public class Test {
                    private Function<String,  List<Number>> t() {
                        List<Number> newVar;
                        return x -> newVar;
                    }
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    public void testLambdaReturnType3() throws Exception {
        performFixTest("test/Test.java",
                """
                package test;
                import java.util.*;
                import java.util.function.*;
                public class Test {
                    private void t() {
                        tt(x -> |newVar);
                    }
                    private void tt(Function<String,  List<Number>> p) {}
                }
                """,
                "AddParameterOrLocalFix:newVar:java.util.List<java.lang.Number>:PARAMETER",
                """
                package test;
                import java.util.*;
                import java.util.function.*;
                public class Test {
                    private void t(List<Number> newVar) {
                        tt(x -> newVar);
                    }
                    private void tt(Function<String,  List<Number>> p) {}
                }
                """.replaceAll("[ \t\n]+", " "));
    }

    @Override
    protected List<Fix> computeFixes(CompilationInfo info, String diagnosticCode, int pos, TreePath path) throws Exception {
        List<Fix> fixes = super.computeFixes(info, diagnosticCode, pos, path);
        List<Fix> result=  new LinkedList<Fix>();

        for (Fix f : fixes) {
            if (f instanceof JavaFixImpl && ((JavaFixImpl) f).jf instanceof AddParameterOrLocalFix) {
                result.add(f);
            }
        }

        return result;
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return ((AddParameterOrLocalFix) ((JavaFixImpl) f).jf).toDebugString(info);
    }
}
