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
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.lexer.Language;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.introduce.Flow.FlowResult;
import org.netbeans.modules.java.hints.spiimpl.TestUtilities;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 * TODO: mostly tested indirectly through IntroduceHintTest, should be rather
 * tested here
 *
 * @author lahvac
 */
public class FlowTest extends NbTestCase {

    public FlowTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        SourceUtilsTestUtil
                .prepareTest(new String[0], new Object[0]);
        super
                .setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = false;
        super.tearDown(); 
    }
    
    

    public void testSimple() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        if (i == 0) ii = 3;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary1() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = i == 0 && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary2() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = true && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testBinary3() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = false && (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2");
    }

    public void testBinary4() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = i == 0 || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2",
                    "3");
    }

    public void testBinary5() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = false || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testBinary6() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii = 2;\n" +
                    "        boolean b = true || (ii = 3) != 0;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "2");
    }

    public void test197666() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int i) {\n" +
                    "        int ii = 1;\n" +
                    "        boolean b = i == 1 && true;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1");
    }

    public void test198233() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        boolean b = i == 1 && true;\n" +
                    "        System.err.println(i`i);\n" +
                    "        ===\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "1");
    }

    public void testIncorrectDeadBranch() throws Exception {
        performDeadBranchTest("package test;\n" +
                              "public class Test {\n" +
                              "    public void i() {\n" +
                              "        if (!i.getAndSet(true)) {\n" +
                              "            System.err.println(\"\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "    private final java.util.concurrent.atomic.AtomicBoolean i = new java.util.concurrent.atomic.AtomicBoolean();\n" +
                              "}\n");
    }

    public void testTryCatch() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } catch (Exception e) {\n" +
                    "            ii = 2;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "2");
    }

    public void testTryCatchFinally() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } catch (Exception e) {\n" +
                    "            ii = 2;\n" +
                    "        } finally {\n" +
                    "            ii = 3;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testTryFinally() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } finally {\n" +
                    "            ii = 3;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "3");
    }

    public void testTryFinally2() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 0;\n" +
                    "        try {\n" +
                    "            ii = 1;\n" +
                    "        } catch (Exception e) {\n" +
                    "            ii = 2;\n" +
                    "        } finally {\n" +
                    "            System.err.println(i`i);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1",
                    "2");
    }

    public void testSwitch1() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p, int r) {\n" +
                    "        int ii;\n" +
                    "        switch (p) {\n" +
                    "            case 0: ii = 1; break;\n" +
                    "            case 1: if (r > 5) {\n" +
                    "                         ii = 5;\n" +
                    "                         break;\n" +
                    "                    }\n" +
                    "                    ii = 2;\n" +
                    "            case 2: ii = 3; break;\n" +
                    "            default: ii = 4; break;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "5",
                    "3",
                    "4");
    }

    public void testSwitch2() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p) {\n" +
                    "        int ii;\n" +
                    "        switch (p) {\n" +
                    "            case 0: ii = 1; break;\n" +
                    "            case 1: ii = 2;\n" +
                    "            case 2: ii = 3; return;\n" +
                    "            default: ii = 4; break;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "4");
    }

    public void testSwitch3() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p) {\n" +
                    "        int ii = 0;\n" +
                    "        switch (p) {\n" +
                    "            case 0: ii = 1; break;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void testSwitch4() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(String str) {\n" +
                    "        final int mm = 1;\n" +
                    "        int b = 0;\n" +
                    "        switch (str.length()) {\n" +
                    "            case 0: break;\n" +
                    "            case 1: b |= m`m; break;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "1");
    }

    public void testForUpdate() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        for (int ii = 0; ii < 100; ii = ii + 1) {\n" +
                    "            System.err.println(i`i);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "ii + 1");
    }

    public void testForEach() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(String... args) {\n" +
                    "        boolean ff = true;\n" +
                    "        for (String a : args) {\n" +
                    "            if (!f`f) System.err.println(1);\n" +
                    "            ff = false;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "true",
                    "false");
    }

    public void testAnonymous() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        final String model = \"\";\n" +
                    "        java.util.Collections.sort(java.util.Collections.emptyList(), new java.util.Comparator<Object>() {\n" +
                    "            public int compare(Object o1, Object o2) {\n" +
                    "                return 0;\n" +
                    "            }\n" +
                    "        });\n" +
                    "        System.err.println(mod`el);\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"");
    }

    public void test198975() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        ii + +=;\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "1");
    }

    public void test199335() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        List<Object> ll = null;\n" +
                    "        for (Object str : ll) {\n" +
                    "            if (str instanceof String) {\n" +
                    "                System.err.println(st`r);\n" +
                    "            }\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "<null>");
    }

    public void testAssert() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        boolean bb = false;\n" +
                    "        assert bb = true;\n" +
                    "        System.err.println(b`b);\n" +
                    "    }\n" +
                    "}\n",
                    false,
                    "false",
                    "true");
    }

    public void testTryFinallyAndReturn() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        boolean bb = false;\n" +
                    "        try {\n" +
                    "            bb = true;\n" +
                    "            return ;\n" +
                    "        } finally {\n" +
                    "            System.err.println(b`b);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }

    public void testWhileWriteInCondition() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        while (i`i-- > 0);\n" +
                    "        System.err.println(ii);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "ii--");
    }

    public void testForWriteInCondition() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        int ii = 1;\n" +
                    "        for (;i`i-- > 0;);\n" +
                    "        System.err.println(ii);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "ii--");
    }

    public void testWhileLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        while (t`p != null) {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testForLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        for (String i = null; i != t`p; ) {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testEnhancedForLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(String[] args) {\n" +
                    "        String tp = \"\";\n" +
                    "        for (String i : args) {\n" +
                    "            if (t`p != null) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testDoWhileLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        do {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        } while (t`p != null);\n" +
                    "    }\n" +
                    "}\n",
                    "null");
    }

    public void testLabeledLoop() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        LOOP: while (t`p != null) {\n" +
                    "            if (tp.length() == 0) {\n" +
                    "                tp = null;\n" +
                    "                continue LOOP;\n" +
                    "            }\n" +
                    "            return ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "\"\"",
                    "null");
    }

    public void testContinue204845() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t() {\n" +
                    "        String tp = \"\";\n" +
                    "        if (t`p.length() == 0) {\n" +
                    "            continue ;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "\"\"");
    }

    public void test205347a() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void reallyUsed() {\n" +
                    "        boolean again = true;\n" +
                    "        for (;;) {\n" +
                    "            if (ag`ain) {\n" +
                    "                again = false;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            break;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "true",
                    "false");
    }

    public void test205347b() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void reallyUsed() {\n" +
                    "        int ii = 0;\n" +
                    "        for (;; ii++) {\n" +
                    "            if (i`i < 100) {\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            break;\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "ii++");
    }

    public void test210520a() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            if (i == 0) throw new Exception();\n" +
                    "            rr = 1;\n" +
                    "            if (i == 1) throw new Exception();\n" +
                    "            rr = 2;\n" +
                    "            if (i == 1) throw new java.io.IOException();\n" +
                    "        } catch (Exception e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1",
                    "2");
    }

    public void test210520b() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            t1();\n" +
                    "            rr = 1;\n" +
                    "            t1();\n" +
                    "        } catch (java.io.IOException e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() throws java.io.FileNotFoundException {\n" +
                    "         throw new java.io.FileNotFoundException();\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void test210520c() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            t1();\n" +
                    "            rr = 1;\n" +
                    "            t1();\n" +
                    "        } catch (Exception e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() {\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void test210520d() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            new java.io.FileInputStream(\"\");\n" +
                    "            rr = 1;\n" +
                    "            new java.io.FileInputStream(\"\");\n" +
                    "        } catch (Exception e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() {\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }

    public void test210520e() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(int i) {\n" +
                    "        int rr = 0;\n" +
                    "        try {\n" +
                    "            assert i == 2;\n" +
                    "            rr = 1;\n" +
                    "            assert i == 3;\n" +
                    "        } catch (Throwable e) {\n" +
                    "            System.out.println(r`r);\n" +
                    "        }\n" +
                    "    }\n" +
                    "    public void t1() {\n" +
                    "    }\n" +
                    "}\n",
                    "0",
                    "1");
    }
    
    public void test211926a() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void f(String... args) {\n" +
                    "        boolean empty = false;\n" +
                    "        for (String a : args) {\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }
    
    public void test211926b() throws Exception {
        performTest("package test;\n" +
                    "import java.util.Collection;\n" +
                    "import java.util.Iterator;\n" +
                    "public class Test {\n" +
                    "    public void f(Collection<String> args) {\n" +
                    "        boolean empty = false;\n" +
                    "        for (Iterator<String> it = args.iterator(); it.hasNext(); ) {\n" +
                    "            String a = it.next();\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }
    
    public void test211926c() throws Exception {
        performTest("package test;\n" +
                    "import java.util.Collection;\n" +
                    "import java.util.Iterator;\n" +
                    "public class Test {\n" +
                    "    public void f(Collection<String> args) {\n" +
                    "        boolean empty = false;\n" +
                    "        Iterator<String> it = args.iterator();\n" +
                    "        while (it.hasNext()) {\n" +
                    "            String a = it.next();\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        }\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }
    
    public void test211926d() throws Exception {
        performTest("package test;\n" +
                    "import java.util.Collection;\n" +
                    "import java.util.Iterator;\n" +
                    "public class Test {\n" +
                    "    public void f(Collection<String> args) {\n" +
                    "        boolean empty = false;\n" +
                    "        Iterator<String> it = args.iterator();\n" +
                    "        do {\n" +
                    "            String a = it.next();\n" +
                    "            if (\"\".equals(a)) {\n" +
                    "                empty = true;\n" +
                    "                continue;\n" +
                    "            }\n" +
                    "            return;\n" +
                    "        } while (it.hasNext());\n" +
                    "        \n" +
                    "        System.err.println(emp`ty);\n" +
                    "    }\n" +
                    "}\n",
                    "false",
                    "true");
    }
    
    public void testStayContinue219270() throws Exception {
        performTest("package test;\n" +
                    "import java.util.Collection;\n" +
                    "import java.util.Iterator;\n" +
                    "public class Test {\n" +
                    "    public void f() {\n" +
                    "        boolean empty = false;\n" +
                    "        System.err.println(emp`ty);\n" +
                    "        T: { continue T; }\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "false");
    }

    public void testLoopExponentialExplosion() throws Exception {
        String sourceCode = "package test;\n" +
                            "import java.util.*;\n" +
                            "class Test {\n" +
                            "    private void t(List<String> args) {\n";
        
        for (int i = 0; i < 20; i++) {
            sourceCode += "for (Iterator<String> it" + i + " = args.iterator(); it" + i + ".hasNext(); )";
        }
        
        sourceCode += "if (ar`gs.size() == 0) System.err.println('a');\n" +
                      "    }\n" +
                      "}\n";
        performTest(sourceCode,
                    true,
                    "List<String> args");
    }
    
    public void testLoopExponentialExplosionDoWhile() throws Exception {
        String sourceCode = "package test;\n" +
                            "import java.util.*;\n" +
                            "class Test {\n" +
                            "    private void t(List<Boolean> args, boolean b) {\n" +
                            "        |\n" +
                            "    }\n" +
                            "}\n";
        
        for (int i = 0; i < 20; i++) {
            sourceCode = sourceCode.replace("|", "do { args.set(i, !args.get(i)); | } while (args.get(i)); ");
        }
        
        sourceCode = sourceCode.replace("|", "if (ar`gs.size() == 0) System.err.println('a');\n");
        
        performTest(sourceCode,
                    true,
                    "List<Boolean> args");
    }
    
    public void testSystemExit() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p) {\n" +
                    "        int ii = 0;\n" +
                    "        switch (p) {\n" +
                    "            case 0: ii = 1; System.exit(0); break;\n" +
                    "        }\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "0");
    }
    
    public void test224028() throws Exception {
        performTest("package test;\n" +
                    "import java.io.*;\n" +
                    "import java.util.*;\n" +
                    "public class Test {\n" +
                    "    private void doSomething(Properties props) {\n" +
                    "        Properties props = new Properties();\n" +
                    "        try {\n" +
                    "            canThrow();\n" +
                    "        } catch (EmptyStackException | IOException ex) {\n" +
                    "            props = null;\n" +
                    "        }\n" +
                    "        pro`ps.clear();\n" +
                    "    }\n" +
                    "    private void canThrow() throws EmptyStackException, IOException {\n" +
                    "    }\n" +
                    "}\n",
                    true,
                    "new Properties()",
                    "null");
    }
    
    public void test241221() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void usage() {\n" +
                    "        int temp;\n" +
                    "        int[] a = new int[]{3, 5, 1, 4, 2};\n" +
                    "        int[] b = new int[5];\n" +
                    "        for (int i = 0; i < b.length; i++) {\n" +
                    "            temp = a[i];\n" +
                    "            b[temp`] = i;\n" +
                    "        }\n" +
                    "    }" +
                    "}\n",
                    true,
                    "a[i]");
    }
    /**
     * Checks that return inside lambda expr does not affect state of enclosing method's variables
     */
    public void test241034ReturnFromLambda() throws Exception {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
        sourceLevel = "1.8";
        performTest("package test;\n" +
                    "import java.io.*;\n" +
                    "import java.util.*;\n" +
                    "public class Test {\n" +
                    "    public int foo() {\n" +
                    "        final int r = 123; \n" +
                    "        BooleanSupplier x = () -> { \n" +
                    "            return true;\n" +
                    "        };\n" +
                    "        return r`;\n" +
                    "    }\n" +
                    "public interface BooleanSupplier {\n" +
                    "\n" +
                    "    /**\n" +
                    "     * Gets a result.\n" +
                    "     *\n" +
                    "     * @return a result\n" +
                    "     */\n" +
                    "    boolean getAsBoolean();\n" +
                    "}\n"+ 
                    "}\n",
                    false,
                    "123");
    }
    
    public void testLoops() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "     public Test getParent() {\n" +
                    "          return this;\n" +
                    "     }\n" +
                    "     public static void t(Iterable<? extends Test> tps) {\n" +
                    "          for (Test tp : tps) {\n" +
                    "               Test toResume = tp;\n" +
                    "               while (toR`esume != null) {\n" +
                    "                    toResume = toResume.getParent();\n" +
                    "               }\n" +
                    "          }\n" +
                    "     }\n" +
                    "}\n",
                    "tp",
                    "toResume.getParent()");
    }
    
    public void testDoNotGeneralizeIfsWithElse() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "     public static void t(int yy) {\n" +
                    "          if (yy > 0) return;\n" +
                    "          else yy++;" +
                    "          System.err.println(y`y);\n" +
                    "     }\n" +
                    "}\n",
                    "yy++");
    }
    
    public void testUseInAnonymousInnerclass228416() throws Exception {
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "     public static void t() {\n" +
                    "          final int yy = 42;\n" +
                    "          new Runnable() {\n" +
                    "              public void run() {\n" +
                    "                  System.err.println(y`y);\n" +
                    "              }\n" +
                    "          };\n" +
                    "     }\n" +
                    "}\n",
                    "42");
    }
    
    public void testDeadBranch207514() throws Exception {
        performDeadBranchTest("package test;\n" +
                              "public class Test {\n" +
                              "    public void i() {\n" +
                              "        if (false) |{\n" +
                              "            System.err.println(\"\");\n" +
                              "        }|\n" +
                              "    }\n" +
                              "    private final java.util.concurrent.atomic.AtomicBoolean i = new java.util.concurrent.atomic.AtomicBoolean();\n" +
                              "}\n");
    }
    
    private String sourceLevel = null;

    private void prepareTest(String code, boolean allowErrors) throws Exception {
        clearWorkDir();

        FileObject workFO = FileUtil
                .toFileObject(getWorkDir());

        assertNotNull(workFO);

        FileObject sourceRoot = workFO
                .createFolder("src");
        FileObject buildRoot = workFO
                .createFolder("build");
        FileObject cache = workFO
                .createFolder("cache");

        FileObject data = FileUtil
                .createData(sourceRoot, "test/Test.java");

        org.netbeans.api.java.source.TestUtilities
                .copyStringToFile(FileUtil
                .toFile(data), code);

        data
                .refresh();

        if (sourceLevel != null) {
            SourceUtilsTestUtil.setSourceLevel(data, sourceLevel);
        }
        SourceUtilsTestUtil
                .prepareTest(sourceRoot, buildRoot, cache);

        DataObject od = DataObject
                .find(data);
        EditorCookie ec = od
                .getCookie(EditorCookie.class);

        assertNotNull(ec);

        doc = ec
                .openDocument();

        doc
                .putProperty(Language.class, JavaTokenId
                .language());
        doc
                .putProperty("mimeType", "text/x-java");

        JavaSource js = JavaSource
                .forFileObject(data);

        assertNotNull(js);

        info = SourceUtilsTestUtil
                .getCompilationInfo(js, Phase.RESOLVED);

        assertNotNull(info);

        if (!allowErrors) {
            assertTrue(info
                    .getDiagnostics()
                    .toString(), info
                    .getDiagnostics()
                    .isEmpty());
        }
    }
    private CompilationInfo info;
    private Document doc;

    private void performTest(String code, String... assignments) throws Exception {
        performTest(code, false, assignments);
    }

    private void performTest(String code, boolean allowErrors, String... assignments) throws Exception {
        int[] span = new int[1];

        code = TestUtilities
                .detectOffsets(code, span, "`");

        prepareTest(code, allowErrors);

        FlowResult flow = Flow
                .assignmentsForUse(info, new AtomicBoolean());
        TreePath sel = info
                .getTreeUtilities()
                .pathFor(span[0]);

        Set<String> actual = new HashSet<String>();
        Iterable<? extends TreePath> c = flow
                .getAssignmentsForUse()
                .get(sel
                .getLeaf());
        if (c != null) {
            for (TreePath tp : c) {
                if (tp == null) {
                    actual
                            .add("<null>");
                } else {
                    actual
                            .add(tp
                            .getLeaf()
                            .toString());
                }
            }
        }

        assertEquals(new HashSet<String>(Arrays
                .asList(assignments)), actual);
    }

    private void performDeadBranchTest(String code) throws Exception {
        List<String> splitted = new LinkedList<String>(Arrays
                .asList(code
                .split(Pattern
                .quote("|"))));
        List<Integer> goldenSpans = new ArrayList<Integer>(splitted
                .size() - 1);
        StringBuilder realCode = new StringBuilder();

        realCode
                .append(splitted
                .remove(0));

        for (String s : splitted) {
            goldenSpans
                    .add(realCode
                    .length());
            realCode
                    .append(s);
        }

        prepareTest(realCode
                .toString(), false);

        FlowResult flow = Flow
                .assignmentsForUse(info, new AtomicBoolean());

        List<Integer> actual = new ArrayList<Integer>(2 * flow
                .getDeadBranches()
                .size());

        for (Tree dead : flow
                .getDeadBranches()) {
            actual
                    .add((int) info
                    .getTrees()
                    .getSourcePositions()
                    .getStartPosition(info
                    .getCompilationUnit(), dead));
            actual
                    .add((int) info
                    .getTrees()
                    .getSourcePositions()
                    .getEndPosition(info
                    .getCompilationUnit(), dead));
        }

        assertEquals(goldenSpans, actual);
    }

    public void testMustPerformResumeAfter206739() throws Exception {
        performDefinitellyAssignmentTest("package test;\n" +
                                         "public class Test {\n" +
                                         "    static void t(java.lang.annotation.RetentionPolicy pol) {\n" +
                                         "        if (true) {\n" +
                                         "            int i`i = 0;\n" +
                                         "            |switch (pol) {\n" +
                                         "                case CLASS: ii = 0; break;\n" +
                                         "                default: break;\n" +
                                         "            }|\n" +
                                         "            System.err.println(ii);\n" +
                                         "        }\n" +
                                         "    }\n" +
                                         "}\n",
                                         false,
                                         false);
    }
    
    private void performDefinitellyAssignmentTest(String code, boolean allowErrors, boolean definitellyAssigned) throws Exception {
        int varPos = code.indexOf('`');
        
        code = code.replace("`", "");
        
        assertTrue(varPos >= 0);
        
        int[] span = new int[2];

        code = TestUtilities.detectOffsets(code, span, "\\|");

        prepareTest(code, allowErrors);

        TreePath tp = info.getTreeUtilities().pathFor((span[0] + span[1]) / 2);
        
        while (tp != null) {
            long s = info.getTrees().getSourcePositions().getStartPosition(tp.getCompilationUnit(), tp.getLeaf());
            long e = info.getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), tp.getLeaf());
            
            if (span[0] == s && span[1] == e) break;
            
            tp = tp.getParentPath();
        }
        
        assertNotNull(tp);
        
        TreePath var = info.getTreeUtilities().pathFor(varPos);
        Element el = info.getTrees().getElement(var);
        
        assertNotNull(el);
        assertEquals(ElementKind.LOCAL_VARIABLE, el.getKind());
        
        boolean actual = Flow.definitellyAssigned(info, (VariableElement) el, Collections.singletonList(tp), new AtomicBoolean());
        
        assertEquals(definitellyAssigned, actual);
    }
    
    public void testFinalCandidatesConstr1() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test() {\n" +
                                   "        i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidatesConstr2() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test() {\n" +
                                   "    }\n" +
                                   "    public Test(int i) {\n" +
                                   "        this.i = i;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesInit1() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidatesInit2() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "    {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesInit3() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "    public Test() {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesMethod1() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "    private void test() {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesMethod2() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    private void test() {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "    public Test() {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesVarInit1() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i = 0;\n" +
                                   "    public Test() {\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidatesVarInit2() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i = 0;\n" +
                                   "    {\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidatesVarInit3() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i = 0;\n" +
                                   "    {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesVarInit4() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i = 0;\n" +
                                   "    public Test() {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesUseBeforeDefining1() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test() {\n" +
                                   "        this.i = i;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesUseBeforeDefining2() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test() {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "    public void test() {\n" +
                                   "        int ii = this.i;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidatesUseBeforeDefining3() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test() {\n" +
                                   "        int ii = this.i;\n" +
                                   "    }\n" +
                                   "    {\n" +
                                   "        this.i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidatesStatic1() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private static int i;\n" +
                                   "    public Test() {\n" +
                                   "        i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesStatic2() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private static int i;\n" +
                                   "    {\n" +
                                   "        i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesStatic3() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private static int i;\n" +
                                   "    static {\n" +
                                   "        i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidatesReturnInConstructor1() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test(boolean b) {\n" +
                                   "        if (b) return ;\n" +
                                   "        i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidatesReturnInConstructor2() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test(boolean b) {\n" +
                                   "        if (b) {" +
                                   "            i = 0;\n" +
                                   "            return ;\n" +
                                   "        }" +
                                   "        i = 0;\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void testFinalCandidates228416a() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public void test() {\n" +
                                   "        i = 0;\n" +
                                   "        new Runnable() {\n" +
                                   "            public void run() {}\n" +
                                   "        };\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testFinalCandidates228416b() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private int i;\n" +
                                   "    public Test() {\n" +
                                   "        i = 0;\n" +
                                   "        new Runnable() {\n" +
                                   "            public void run() {}\n" +
                                   "        };\n" +
                                   "    }\n" +
                                   "}\n",
                                   false,
                                   "i");
    }
    
    public void test229306() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private static int countCreated = 0;\n" +
                                   "    private final int ID = ++countCreated;\n" +
                                   "}\n",
                                   false,
                                   "ID");//TODO: ID actually already is final - should or should not be on the list?
    }
    
    public void test229585a() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private boolean value;\n" +
                                   "    public Runnable runner = null;\n" +
                                   "    Test() {\n" +
                                   "        value = false;\n" +
                                   "        runner = new Runnable() {\n" +
                                   "            public void run() { value = true; }\n" +
                                   "        };\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void test229585b() throws Exception {
        performFinalCandidatesTest("package test;\n" +
                                   "public class Test {\n" +
                                   "    private boolean value;\n" +
                                   "    Test() {\n" +
                                   "        value = false;\n" +
                                   "        class RunnableImpl {\n" +
                                   "            public RunnableImpl() { value = true; }\n" +
                                   "        };\n" +
                                   "    }\n" +
                                   "}\n",
                                   false);
    }
    
    public void testDoWhileUsedNextIteration241808() throws Exception {
        performTest("package test;\n" +
                "public class Test {\n" +
                "    public void test() {\n" +
                "        int[] arr = new int[] {1, 2, 3, 4};\n" +
                "        int di = 0;\n" +
                "        do {\n" +
                "            int i = di;\n" +
                "            do {\n" +
                "                int ii = arr[i`];                \n" +
                "                if (ii == 4) {\n" +
                "                    break;                    \n" +
                "                } else {\n" +
                "                    i = ii;\n" +
                "                }\n" +
                "            } while(true);\n" +
                "        } while(++di < 4);\n" +
                "    }\n" +
                "}",
                "ii",
                "di");
    }
    
    public void testRuleCases() throws Exception {
        sourceLevel = "14";
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    public void test(int i) {\n" +
                    "        int val;\n" +
                    "        switch (i) {\n" +
                    "            case 0 -> val = 0;\n" +
                    "            case 1 -> val = 1;\n" +
                    "            case 2 -> { val = 2; }\n" +
                    "            case 3 -> { val = 3; }\n" +
                    "            default -> { val = -1; }\n" +
                    "        }\n" +
                    "        System.err.println(val`);\n" +
                    "    }\n" +
                    "}",
                    "0",
                    "1",
                    "2",
                    "3",
                    "-1");
    }

    public void testSwitchExpression1() throws Exception {
        sourceLevel = "14";
        performTest("package test;\n" +
                    "public class Test {\n" +
                    "    static void t(int p) {\n" +
                    "        int ii;\n" +
                    "        int x = switch (p) {\n" +
                    "            case 0: ii = 1; yield 0;\n" +
                    "            case 1: ii = 2;\n" +
                    "            case 2: ii = 3; yield 0;\n" +
                    "            default: ii = 4; yield 0;\n" +
                    "        };\n" +
                    "        System.err.println(i`i);\n" +
                    "    }\n" +
                    "}\n",
                    "1",
                    "3",
                    "4");
    }

    public void testParameters() throws Exception {
        performTest("""
                    package test;
                    public class Test {
                        static void t(int ii) {
                            System.err.println(i`i);
                        }
                    }
                    """,
                    "int ii");
    }

    public void testLambdaParameters() throws Exception {
        performTest("""
                    package test;
                    public class Test {
                        static void t() {
                            I i = ii -> System.err.println(i`i);
                        }
                        interface I {
                            public void consume(int ii);
                        }
                    }
                    """,
                    "int ii");
    }

    public void testCatchParameters() throws Exception {
        performTest("""
                    package test;
                    public class Test {
                        static void t() {
                            try {
                            } catch (Throwable tt) {
                                System.err.println(t`t);
                            }
                        }
                    }
                    """,
                    "Throwable tt");
    }

    public void testBindingVariables() throws Exception {
        sourceLevel = "21";
        performTest("""
                    package test;
                    public class Test {
                        static void t(Object o) {
                            boolean b = o instanceof Integer ii && i`i == 0;
                        }
                    }
                    """,
                    "Integer ii");
    }

    private void performFinalCandidatesTest(String code, boolean allowErrors, String... finalCandidates) throws Exception {
        prepareTest(code, allowErrors);

        FlowResult actual = Flow.assignmentsForUse(info, new AtomicBoolean());
        List<String> computedCandidates = new ArrayList<String>();
        
        for (VariableElement ve : actual.getFinalCandidates()) {
            computedCandidates.add(ve.getSimpleName().toString());
        }
        
        Collections.sort(computedCandidates);
        
        assertEquals(Arrays.asList(finalCandidates), computedCandidates);
    }

    static {
        Main.initializeURLFactory();
    }
}
