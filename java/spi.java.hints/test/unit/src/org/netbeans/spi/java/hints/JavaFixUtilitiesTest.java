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

package org.netbeans.spi.java.hints;

import org.netbeans.modules.java.hints.providers.spi.HintDescriptionFactory;
import org.netbeans.modules.java.hints.providers.spi.HintDescription;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.java.hints.spiimpl.TestBase;
import org.netbeans.modules.java.hints.spiimpl.hints.HintsInvoker;
import org.netbeans.modules.java.hints.providers.spi.Trigger.PatternDescription;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.modules.java.hints.spiimpl.pm.PatternCompilerUtilities;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.LifecycleManager;
import org.openide.modules.SpecificationVersion;
import org.openide.util.MapFormat;

/**
 *
 * @author Jan Lahoda
 */
public class JavaFixUtilitiesTest extends TestBase {

    public JavaFixUtilitiesTest(String name) {
        super(name);
    }

    public void testSimple() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testSimpleDate() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5 (16 May 2005)\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testLongText() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.123.2.1 - branch propsheet_issue_29447\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.123.2.1")));
    }

    public void testModuleName() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since org.openide.filesystems 7.15\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("7.15")));
    }

    public void testModuleNameMajor() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since org.openide/1 4.42\n" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("4.42")));
    }

    public void testEnd() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since 1.5 */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("1.5")));
    }

    public void testOpenAPI() throws Exception {
        SpecificationVersion v = computeSpecVersion("/**\n" +
                                                    " * @since OpenAPI version 2.12" +
                                                    " */\n");

        assertEquals(0, v.compareTo(new SpecificationVersion("2.12")));

    }

    private SpecificationVersion computeSpecVersion(String javadoc) throws Exception {
        prepareTest("test/Test.java",
                    "package test;\n" +
                    "public class Test {\n" +
                    javadoc +
                    "     public void test() {\n" +
                    "     }\n" +
                    "}\n");

        TypeElement te = info.getElements().getTypeElement("test.Test");
        ExecutableElement method = ElementFilter.methodsIn(te.getEnclosedElements()).iterator().next();

        return JavaFixUtilities.computeSpecVersion(info, method);
    }

    public void testArithmetic1() throws Exception {
        performArithmeticTest("1 + 2", "3");
        performArithmeticTest("1f + 2", "3.0F");
        performArithmeticTest("1 + 2f", "3.0F");
        performArithmeticTest("1.0 + 2f", "3.0");
        performArithmeticTest("1 + 2.0", "3.0");
        performArithmeticTest("1L + 2", "3L");
    }

    public void testArithmetic2() throws Exception {
        performArithmeticTest("1 * 2", "2");
        performArithmeticTest("1f * 2", "2.0F");
        performArithmeticTest("1 * 2f", "2.0F");
        performArithmeticTest("1.0 * 2f", "2.0");
        performArithmeticTest("1 * 2.0", "2.0");
        performArithmeticTest("1L * 2", "2L");
    }

    public void testArithmetic3() throws Exception {
        performArithmeticTest("4 / 2", "2");
        performArithmeticTest("4f / 2", "2.0F");
        performArithmeticTest("4 / 2f", "2.0F");
        performArithmeticTest("4.0 / 2f", "2.0");
        performArithmeticTest("4 / 2.0", "2.0");
        performArithmeticTest("4L / 2", "2L");
    }

    public void testArithmetic4() throws Exception {
        performArithmeticTest("5 % 2", "1");
        performArithmeticTest("5f % 2", "1.0F");
        performArithmeticTest("5 % 2f", "1.0F");
        performArithmeticTest("5.0 % 2f", "1.0");
        performArithmeticTest("5 % 2.0", "1.0");
        performArithmeticTest("5L % 2", "1L");
    }

    public void testArithmetic5() throws Exception {
        performArithmeticTest("5 - 2", "3");
        performArithmeticTest("5f - 2", "3.0F");
        performArithmeticTest("5 - 2f", "3.0F");
        performArithmeticTest("5.0 - 2f", "3.0");
        performArithmeticTest("5 - 2.0", "3.0");
        performArithmeticTest("5L - 2", "3L");
    }

    public void testArithmetic6() throws Exception {
        performArithmeticTest("5 | 2", "7");
        performArithmeticTest("5L | 2", "7L");
        performArithmeticTest("5 | 2L", "7L");
    }

    public void testArithmetic7() throws Exception {
        performArithmeticTest("5 & 4", "4");
        performArithmeticTest("5L & 4", "4L");
        performArithmeticTest("5 & 4L", "4L");
    }

    public void testArithmetic8() throws Exception {
        performArithmeticTest("5 ^ 4", "1");
        performArithmeticTest("5L ^ 4", "1L");
        performArithmeticTest("5 ^ 4L", "1L");
    }

    public void testArithmetic9() throws Exception {
        performArithmeticTest("5 << 2", "20");
        performArithmeticTest("5L << 2", "20L");
        performArithmeticTest("5 << 2L", "20L");
    }

    public void testArithmeticA() throws Exception {
        performArithmeticTest("-20 >> 2", "-5");
        performArithmeticTest("-20L >> 2", "-5L");
        performArithmeticTest("-20 >> 2L", "-5L");
    }

    public void testArithmeticB() throws Exception {
        performArithmeticTest("-20 >>> 2", "1073741819");
    }

    public void testArithmeticC() throws Exception {
        performArithmeticTest("0 + -20", "-20");
        performArithmeticTest("0 + +20", "20");
    }

    public void testArithmeticComplex() throws Exception {
        performArithmeticTest("1 + 2 * 4 - 5", "4");
        performArithmeticTest("1f + 2 * 4.0 - 5", "4.0");
        performArithmeticTest("1L + 2 * 4 - 5", "4L");
    }

    private static final String ARITHMETIC = "public class Test { private Object o = __VAL__; }";
    private void performArithmeticTest(String orig, String nue) throws Exception {
        String code = replace("0");

        prepareTest("Test.java", code);
        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        VariableTree variable = (VariableTree) clazz.getMembers().get(1);
        ExpressionTree init = variable.getInitializer();
        TreePath tp = new TreePath(new TreePath(new TreePath(new TreePath(info.getCompilationUnit()), clazz), variable), init);
        Fix fix = JavaFixUtilities.rewriteFix(info, "A", tp, orig, Collections.<String, TreePath>emptyMap(), Collections.<String, Collection<? extends TreePath>>emptyMap(), Collections.<String, String>emptyMap(), Collections.<String, TypeMirror>emptyMap(), Collections.<String, String>emptyMap());
        fix.implement();

        String golden = replace(nue);
        String out = doc.getText(0, doc.getLength());

        assertEquals(golden, out);

        LifecycleManager.getDefault().saveAll();
    }

    private static String replace(String val) {
        MapFormat f = new MapFormat(Collections.singletonMap("VAL", val));

        f.setLeftBrace("__");
        f.setRightBrace("__");

        return f.format(ARITHMETIC);
    }

    public void testRewriteWithParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = new String(\"a\" + \"b\").length();\n" +
                           "}\n",
                           "new String($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = (\"a\" + \"b\").length();\n" +
		           "}\n");
    }

    public void testRewriteWithParenthesis2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = Integer.valueOf(1 + 2) * 3;\n" +
                           "}\n",
                           "Integer.valueOf($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = (1 + 2) * 3;\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = new String(\"a\" + \"b\").length();\n" +
                           "}\n",
                           "new String($1)=>java.lang.String.format(\"%s%s\", $1, \"\")",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = String.format(\"%s%s\", \"a\" + \"b\", \"\").length();\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    String s = (\"a\" + \"b\").intern();\n" +
                           "}\n",
                           "($1).intern()=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    String s = \"a\" + \"b\";\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis3() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = Integer.valueOf(1 + 2) + 3;\n" +
                           "}\n",
                           "Integer.valueOf($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = 1 + 2 + 3;\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis4() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = Integer.valueOf(1 * 2) + 3;\n" +
                           "}\n",
                           "Integer.valueOf($1)=>$1",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = 1 * 2 + 3;\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis5() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = new Integer(1 * 2).hashCode();\n" +
                           "}\n",
                           "$1.hashCode()=>$1.hashCode()",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = new Integer(1 * 2).hashCode();\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis6() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        System.err.println(\"a\" + 1);\n" +
                           "    }\n" +
                           "}\n",
                           "System.err.println($t)=>D.println($t)",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        D.println(\"a\" + 1);\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testRewriteWithoutParenthesis7() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        new String(\"a\" + 1);\n" +
                           "    }\n" +
                           "}\n",
                           "new String($t)=>new D($t)",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        new D(\"a\" + 1);\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testTopLevelRewriteWithoutParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = (1 + 2) * 2;\n" +
                           "}\n",
                           "$1 + $2=>3",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = 3 * 2;\n" +
		           "}\n");
    }

    public void testTopLevelRewriteKeepParenthesis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    int i = (1 * 2) + 2;\n" +
                           "}\n",
                           "$1 * $2=>2",
                           "package test;\n" +
                           "public class Test {\n" +
		           "    int i = (2) + 2;\n" +
		           "}\n");
    }

    public void testTopLevelRewriteKeepParenthesis2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    { if (1 > 2) ; }\n" +
                           "}\n",
                           "$1 > $2=>false",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    { if (false) ; }\n" +
		           "}\n");
    }
    
    public void testRewriteCatchMultiVariable() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        try {\n" +
                           "        } catch (NullPointerException ex) { }\n" +
                           "    }\n" +
                           "}\n",
                           "try { } catch $catches$ => try { new Object(); } catch $catches$",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        try {\n" +
                           "            new Object();\n" +
                           "        } catch (NullPointerException ex) { }\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testRewriteCaseMultiVariable() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    { int i = 0; switch (i) {case 0: System.err.println(1); break; case 1: System.err.println(2); break; case 2: System.err.println(3); break; }\n" +
                           "}\n",
                           "switch ($v) { case $p$ case 2: $stmts$; } => switch ($v) { case $p$ case 3: $stmts$; }",
                           "package test;\n" +
                           "public class Test {\n" +
                           //XXX: whitespaces:
                           "    { int i = 0; switch (i) {case 0: System.err.println(1); break; case 1: System.err.println(2); break; case 3: System.err.println(3); break; }\n" +
//                           "    { int i = 0; switch (i) {case 0: System.err.println(1); break; case 1: System.err.println(2); break; case   3: System.err.println(3); break; }\n" +
		           "}\n");
    }

    public void testRewriteMemberSelectVariable() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    { java.io.File f = null; boolean b = f.isDirectory(); }\n" +
                           "}\n",
                           "$file.$m() => foo.Bar.$m($file)",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    { java.io.File f = null; boolean b = foo.Bar.isDirectory(f); }\n" +
		           "}\n");
    }
    
    public void testRewriteIdent2IdentMemberSelectPattern() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private boolean b; private void t() { boolean c = b; }\n" +
                           "}\n",
                           "$0{test.Test}.b => !$0.b",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private boolean b; private void t() { boolean c = !b; }\n" +
		           "}\n");
    }

    public void testCarefulRewriteInImports() throws Exception {
        performRewriteTest("package test;\n" +
                           "import javax.swing.text.AbstractDocument;\n" +
                           "public class Test {\n" +
                           "}\n",
                           "javax.swing.text.AbstractDocument => javax.swing.text.Document",
                           "package test;\n" +
                           "import javax.swing.text.Document;\n" +
                           "public class Test {\n" +
		           "}\n");
    }

    public void testRemoveFromParent1() throws Exception {
        performRemoveFromParentTest("package test;\n" +
                                    "public class Test {\n" +
                                    "    private int I;" +
                                    "}\n",
                                    "$mods$ int $f;",
                                    "package test;\n" +
                                    "public class Test {\n" +
                                    "}\n");
    }

    public void testRemoveFromParent2() throws Exception {
        performRemoveFromParentTest("package test;\n" +
                                    "public class Test extends java.util.ArrayList {\n" +
                                    "}\n",
                                    "java.util.ArrayList",
                                    "package test;\n" +
                                    "public class Test {\n" +
                                    "}\n");
    }
    
    public void testRemoveFromParentExpressionStatement206116() throws Exception {
        performRemoveFromParentTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        System.err.println();\n" +
                           "        System.err.println(\"a\");\n" +
                           "    }\n" +
                           "}\n",
                           "System.err.println()",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        System.err.println(\"a\");\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testUnresolvableTarget() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test extends java.util.ArrayList {\n" +
                           "}\n",
                           "java.util.ArrayList => Test",
                           "package test;\n" +
                           "public class Test extends Test {\n" +
                           "}\n");
    }

    public void testTryWithResourceTarget() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        InputStream in = null;\n" +
                           "        try {\n" +
                           "        } finally {\n" +
                           "            in.close()\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "$type $var = $init; try {} finally {$var.close();} => try ($type $var = $init) {} finally {$var.close();}",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        try (InputStream in = null) {\n" +
                           "        } finally {\n" +
                           "            in.close()\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testSingle2MultipleStatements() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        int i = 0;\n" +
                           "    }\n" +
                           "}\n",
                           "$type $var = $init; => $type $var; $var = $init;",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        int i;\n" +
                           "        i = 0;\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testSingle2MultipleStatements2() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        while (true)\n" +
                           "            if (true) {\n" +
                           "                System.err.println();\n" +
                           "            }\n" +
                           "    }\n" +
                           "}\n",
                           "if (true) $then; => if (true) $then; System.err.println();",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        while (true) {\n" +
                           "            if (true) {\n" +
                           "                System.err.println();\n" +
                           "            }\n" +
                           "            System.err.println();\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testMultipleStatementsWrapComments1() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        if (1 == 1) {\n" +
                           "            System.err.println();\n" +
                           "            System.err.println(\"a\");\n" +
                           "            \n" +
                           "            \n" +
                           "            //C\n" +
                           "            System.err.println(\"b\");\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "if ($cond) { System.err.println(); $stmts$;} => while ($cond) { $stmts$;}",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        while (1 == 1) {\n" +
                           "            System.err.println(\"a\");\n" +
                           "            \n" +
                           "            \n" +
                           "            //C\n" +
                           "            System.err.println(\"b\");\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testMultipleStatementsWrapComments2() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        if (1 == 1) {\n" +
                           "            System.err.println();\n" +
                           "            System.err.println(\"a\");\n" +
                           "            \n" +
                           "            \n" +
                           "            //C\n" +
                           "            System.err.println(\"b\");\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "if ($cond) { $stmts$;} => while ($cond) { $stmts$;}",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        while (1 == 1) {\n" +
                           "            System.err.println();\n" +
                           "            System.err.println(\"a\");\n" +
                           "            \n" +
                           "            \n" +
                           "            //C\n" +
                           "            System.err.println(\"b\");\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testReplaceTypeParameters1() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private <A, B> void t() {\n" +
                           "    }\n" +
                           "}\n",
                           "$mods$ <$O, $T> $ret $name() { $body$; } => $mods$ <$T, $O> $ret $name() { $body$; }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private <B, A> void t() {\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testReplaceTypeParameters2() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private <A, B> void t() {\n" +
                           "    }\n" +
                           "}\n",
                           "$mods$ <$T$> $ret $name() { $body$; } => $mods$ <C, $T$> $ret $name() { $body$; }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private <C, A, B> void t() {\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testAdd2Modifiers() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    void t() {\n" +
                           "    }\n" +
                           "}\n",
                           "$mods$ $ret $name() { $body$; } => $mods$ @java.lang.Deprecated private $ret $name() { $body$; }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    @Deprecated\n" +
                           "    private void t() {\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testReplaceInModifiers() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    public @Override void t() {\n" +
                           "    }\n" +
                           "}\n",
                           "$mods$ public @Override $ret $name() { $body$; } => $mods$ private @Deprecated $ret $name() { $body$; }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private @Deprecated void t() {\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testKeepInModifiers() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    public @Override void t() {\n" +
                           "    }\n" +
                           "}\n",
                           "$mods$ public @Override $ret $name() { $body$; } => $mods$ public @Override $ret $name() { $body$; }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    public @Override void t() {\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testRemoveInModifiers() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    public static @Deprecated @Override void t() {\n" +
                           "    }\n" +
                           "}\n",
                           "$mods$ public @Override $ret $name() { $body$; } => $mods$ $ret $name() { $body$; }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    static @Deprecated void t() {\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testRewriteMethodParametersWildcard() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    public static void t() {\n" +
                           "    }\n" +
                           "}\n",
                           "$mods$ void $name($args$) { $body$; } => $mods$ int $name($args$) { $body$; }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    public static int t() {\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testRewriteClass() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "}\n",
                           "$mods$ class $name<$tp$> extends $e$ implements $i$ { $members$; } => $mods$ @java.lang.Deprecated class $name<$tp$> extends $e$ implements $i$ { $members$; }",
                           "package test;\n" +
                           "@Deprecated\n" +
                           "public class Test {\n" +
		           "}\n");
    }
    
    public void testOptionalVariableInitializer1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private int I;\n" +
                           "}\n",
                           "$mods$ int $name = $init$; => $mods$ long $name = $init$;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private long I;\n" +
		           "}\n");
    }
    
    public void testOptionalVariableInitializer2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private int I = 1;\n" +
                           "}\n",
                           "$mods$ int $name = $init$; => $mods$ long $name = $init$;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private long I = 1;\n" +
		           "}\n");
    }
    
    public void testOptionalElse1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        if (true) System.err.println(\"a\");\n" +
                           "    }\n" +
                           "}\n",
                           "if (true) $then else $else$; => if (false) $then else $else$;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        if (false) System.err.println(\"a\");\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testOptionalElse2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        if (true) System.err.println(\"a\");\n" +
                           "        else System.err.println(\"b\");\n" +
                           "    }\n" +
                           "}\n",
                           "if (true) $then else $else$; => if (false) $then else $else$;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    {\n" +
                           "        if (false) System.err.println(\"a\");\n" +
                           "        else System.err.println(\"b\");\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testMultiNewArray() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(Object... obj) {\n" +
                           "        Test.t(1);\n" +
                           "    }\n" +
                           "}\n",
                           "test.Test.t($args$) => test.Test.t(new Object[] {$args$})",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(Object... obj) {\n" +
                           "        Test.t(new Object[]{1});\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testFakeBlock2FakeBlock191283() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        System.err.println(1);\n" +
                           "        lock();\n" +
                           "        System.err.println(2);\n" +
                           "        unlock();\n" +
                           "        System.err.println(3);\n" +
                           "    }\n" +
                           "    private static void lock() {}\n" +
                           "    private static void unlock() {}\n" +
                           "}\n",
                           "test.Test.lock(); $i$; test.Test.unlock(); => lock(); try { $i$; } finally { unlock(); }",
                           "package test;\n" +
                           "import java.io.InputStream;\n" +
                           "public class Test {\n" +
                           "    private void t() throws Exception {\n" +
                           "        System.err.println(1);\n" +
                           "        lock();\n" +
                           "        try {\n" +
                           "            System.err.println(2);\n" +
                           "        } finally {\n" +
                           "            unlock();\n" +
                           "        }\n" +
                           "        System.err.println(3);\n" +
                           "    }\n" +
                           "    private static void lock() {}\n" +
                           "    private static void unlock() {}\n" +
		           "}\n");
    }
    
    public void testOptimizeNegExpression() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(int i) {\n" +
                           "        if (i == 0) {\n" +
                           "            System.err.println(1);\n" +
                           "        } else {\n" +
                           "            System.err.println(2);\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "if ($cond) $then; else $else; => if (!$cond) $else; else $then;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(int i) {\n" +
                           "        if (i != 0) {\n" +
                           "            System.err.println(2);\n" +
                           "        } else {\n" +
                           "            System.err.println(1);\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testDontOptimizeNegExpression() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(int i) {\n" +
                           "        if (!(i == 0)) {\n" +
                           "            System.err.println(1);\n" +
                           "        } else {\n" +
                           "            System.err.println(2);\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "if (!$cond) $then; else $else; => if (!$cond) $else; else $then;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(int i) {\n" +
                           "        if (!(i == 0)) {\n" +
                           "            System.err.println(2);\n" +
                           "        } else {\n" +
                           "            System.err.println(1);\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testCannotOptimizeNegExpression() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(String str) {\n" +
                           "        if (str.isEmpty()) {\n" +
                           "            System.err.println(1);\n" +
                           "        } else {\n" +
                           "            System.err.println(2);\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "if ($cond) $then; else $else; => if (!$cond) $else; else $then;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(String str) {\n" +
                           "        if (!str.isEmpty()) {\n" +
                           "            System.err.println(2);\n" +
                           "        } else {\n" +
                           "            System.err.println(1);\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testExpression2ExpressionStatement() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static String t(CharSequence seq) {\n" +
                           "        return seq.toString();\n" +
                           "    }\n" +
                           "}\n",
                           "return $var; => $var;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static String t(CharSequence seq) {\n" +
                           "        seq.toString();\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testOptimizeNegExpressionNeg() throws Exception {
        performOptimizeNegExpressionTest("!s.isEmpty()", "s.isEmpty()");
    }
    
    public void testOptimizeNegExpressionParens() throws Exception {
        performOptimizeNegExpressionTest("!(a.length == 0)", "a.length == 0");
    }
    
    public void testOptimizeNegExpressionEquals() throws Exception {
        performOptimizeNegExpressionTest("i == 0", "i != 0");
    }
    
    public void testOptimizeNegExpressionNotEquals() throws Exception {
        performOptimizeNegExpressionTest("i != 0", "i == 0");
    }
    
    public void testOptimizeNegExpressionTrue() throws Exception {
        performOptimizeNegExpressionTest("true", "false");
    }
    
    public void testOptimizeNegExpressionFalse() throws Exception {
        performOptimizeNegExpressionTest("false", "true");
    }
    
    public void testOptimizeNegExpressionDeMorganAnd() throws Exception {
        performOptimizeNegExpressionTest("a.length != 0 && true", "a.length == 0 || false");
    }
    
    public void testOptimizeNegExpressionDeMorganOr() throws Exception {
        performOptimizeNegExpressionTest("args.length != 0 || false", "args.length == 0 && true");
    }
    
    public void testOptimizeNegExpressionLess() throws Exception {
        performOptimizeNegExpressionTest("i < 0", "i >= 0");
    }
    
    public void testOptimizeNegExpressionLessEq() throws Exception {
        performOptimizeNegExpressionTest("i <= 0", "i > 0");
    }
    
    public void testOptimizeNegExpressionGreater() throws Exception {
        performOptimizeNegExpressionTest("i > 0", "i <= 0");
    }
    
    public void testOptimizeNegExpressionGreaterEq() throws Exception {
        performOptimizeNegExpressionTest("i >= 0", "i < 0");
    }
    
    public void testOptimizeNegExpressionAnd() throws Exception {
        performOptimizeNegExpressionTest("b1 && b2", "!b1 || !b2");
    }
    
    public void test229785a() throws Exception {
        performOptimizeNegExpressionTest("(a[0] == null && a[1] != null) || (a[0] != null && !a[0].equals(a[1]))", "(a[0] != null || a[1] == null) && (a[0] == null || a[0].equals(a[1]))");
    }
    
    public void test229785b() throws Exception {
        performOptimizeNegExpressionTest("a[0] == null && a[1] != null || a[0] != null && !a[0].equals(a[1])", "(a[0] != null || a[1] == null) && (a[0] == null || a[0].equals(a[1]))");
    }
    
    private void performOptimizeNegExpressionTest(String origExpr, String newExpr) throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(String s, int i, boolean b1, boolean b2, String... a) {\n" +
                           "        if (" + origExpr + ") {\n" +
                           "            System.err.println(1);\n" +
                           "        } else {\n" +
                           "            System.err.println(2);\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "if ($cond) $then; else $else; => if (!$cond) $else; else $then;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(String s, int i, boolean b1, boolean b2, String... a) {\n" +
                           "        if (" + newExpr + ") {\n" +
                           "            System.err.println(2);\n" +
                           "        } else {\n" +
                           "            System.err.println(1);\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testExpression2ExpressionStatementTolerance227429() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static void t() {\n" +
                           "        System.err.println(1);\n" +
                           "    }\n" +
                           "}\n",
                           "java.lang.System.err.println($args$) => java.lang.System.out.println($args$);",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static void t() {\n" +
                           "        System.out.println(1);\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testSplitIfOr() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(int i) {\n" +
                           "        if (i == 0 || i == 1) {\n" +
                           "            System.err.println();\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "if ($cond1 || $cond2) $then; => if ($cond1) $then; else if ($cond2) $then;",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    private static void t(int i) {\n" +
                           "        if (i == 0) {\n" +
                           "            System.err.println();\n" +
                           "        } else if (i == 1) {\n" +
                           "            System.err.println();\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testLambdaExpr2Block() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.util.*;\n" +
                           "public class Test {\n" +
                           "    public void main(List<String> list) {\n" +
                           "        Collections.sort(list, (l, r) -> l.compareTo(r));\n" +
                           "    }\n" +
                           "}\n",
                           "($args$) -> $expr => ($args$) -> { return $expr; }",
                           "package test;\n" +
                           "import java.util.*;\n" +
                           "public class Test {\n" +
                           "    public void main(List<String> list) {\n" +
                           "        Collections.sort(list, (l, r) -> {\n" +
                           "            return l.compareTo(r);\n" +
                           "        });\n" +
                           "    }\n" +
		           "}\n", "1.8");
    }

    public void testMemberRef2Null() throws Exception {
        performRewriteTest("package test;\n" +
                           "import java.util.*;\n" +
                           "public class Test {\n" +
                           "    public void main(List<String> list) {\n" +
                           "        Collections.sort(list, String::compareTo);\n" +
                           "    }\n" +
                           "}\n",
                           "$expr::$name => (String l, String r) -> l.$name(r)",
                           "package test;\n" +
                           "import java.util.*;\n" +
                           "public class Test {\n" +
                           "    public void main(List<String> list) {\n" +
                           "        Collections.sort(list, (String l, String r) -> l.compareTo(r));\n" +
                           "    }\n" +
		           "}\n", "1.8");
    }

    public void testChangeMemberRefs() throws Exception {
        performRewriteTest("package test;\n" +
                           "\n" +
                           "import java.util.Objects;\n" +
                           "import java.util.stream.Stream;\n" +
                           "\n" +
                           "public class Test {\n" +
                           "\n" +
                           "    public static <T> T identity(T t) {\n" +
                           "        return t;\n" +
                           "    }\n" +
                           "\n" +
                           "    public static String toString(Object o) {\n" +
                           "        return Objects.toString(o);\n" +
                           "    }\n" +
                           "\n" +
                           "    public <T> Stream<?> test(Stream<T> stream) {\n" +
                           "        return stream.map(Test::identity);\n" +
                           "    }\n" +
                           "}",
                           "$expr::identity => $expr::toString",
                           "package test;\n" +
                           "\n" +
                           "import java.util.Objects;\n" +
                           "import java.util.stream.Stream;\n" +
                           "\n" +
                           "public class Test {\n" +
                           "\n" +
                           "    public static <T> T identity(T t) {\n" +
                           "        return t;\n" +
                           "    }\n" +
                           "\n" +
                           "    public static String toString(Object o) {\n" +
                           "        return Objects.toString(o);\n" +
                           "    }\n" +
                           "\n" +
                           "    public <T> Stream<?> test(Stream<T> stream) {\n" +
                           "        return stream.map(Test::toString);\n" +
                           "    }\n" +
                           "}",
                           "1.8");
    }

    public void testComments232298() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {\n" +
                           "        while (true) {\n" +
                           "            int i = 0;\n" +
                           "            \n" +
                           "            //a\n" +
                           "            System.err.println(1); //b\n" +
                           "            //c\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "while (true) { int i = 0; $statements$; } => for (; ;) { $statements$; }",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {\n" +
                           "        for (;;) {\n" +
                           "            //a\n" +
                           "            System.err.println(1); //b\n" +
                           "            //c\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }
    
    public void testImplicitThis1() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void z(Test t) {\n" +
                           "        this.z(new Test());\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.z($1) => $1.z($0)",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void z(Test t) {\n" +
                           "        new Test().z(this);\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testImplicitThis2() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void z(Test t) {\n" +
                           "        z(new Test());\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.z($1) => $1.z($0)",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void z(Test t) {\n" +
                           "        new Test().z(this);\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testImplicitThis3() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void z(Test t) {\n" +
                           "    }\n" +
                           "    private class T {\n" +
                           "        void t() {\n" +
                           "            z(new Test());\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.z($1) => $1.z($0)",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void z(Test t) {\n" +
                           "    }\n" +
                           "    private class T {\n" +
                           "        void t() {\n" +
                           "            new Test().z(Test.this);\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testImplicitThis4() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {}\n" +
                           "    public void a() {}\n" +
                           "    private class T {\n" +
                           "        void t() {\n" +
                           "            z();\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.z() => $0.a()",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {}\n" +
                           "    public void a() {}\n" +
                           "    private class T {\n" +
                           "        void t() {\n" +
                           "            a();\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testImplicitThis5() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {}\n" +
                           "    public void a() {}\n" +
                           "    private class T {\n" +
                           "        public void a() {}\n" +
                           "        void t() {\n" +
                           "            z();\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.z() => $0.a()",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {}\n" +
                           "    public void a() {}\n" +
                           "    private class T {\n" +
                           "        public void a() {}\n" +
                           "        void t() {\n" +
                           "            Test.this.a();\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testImplicitThis6() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {}\n" +
                           "    private class T {\n" +
                           "        void t() {\n" +
                           "            z();\n" +
                           "        }\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.z() => $0.toString()",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void z() {}\n" +
                           "    private class T {\n" +
                           "        void t() {\n" +
                           "            Test.this.toString();\n" +
                           "        }\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testRewriteUndeclaredLambda() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void m() {\n" +
                           "        m();\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.m()=>$0.m(t -> System.err.println(t))",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void m() {\n" +
                           "        m(t -> System.err.println(t));\n" +
                           "    }\n" +
		           "}\n");
    }

    public void testRewriteToExplicitLambda() throws Exception {
        performRewriteTest("package test;\n" +
                           "public class Test {\n" +
                           "    public void m() {\n" +
                           "        m();\n" +
                           "    }\n" +
                           "}\n",
                           "$0{test.Test}.m()=>$0.m((String t) -> System.err.println(t))",
                           "package test;\n" +
                           "public class Test {\n" +
                           "    public void m() {\n" +
                           "        m((String t) -> System.err.println(t));\n" +
                           "    }\n" +
		           "}\n");
    }

    public void performRewriteTest(String code, String rule, String golden) throws Exception {
        performRewriteTest(code, rule, golden, null);
    }
    
    public void performRewriteTest(String code, String rule, String golden, String sourceLevel) throws Exception {
	prepareTest("test/Test.java", code, sourceLevel);

        final String[] split = rule.split("=>");
        assertEquals(2, split.length);
        Map<String, TypeMirror> variablesToTypesTM = new HashMap<String, TypeMirror>();
        String plainRule = PatternCompilerUtilities.parseOutTypesFromPattern(info, split[0], variablesToTypesTM);
        Map<String, String> variablesToTypes = new HashMap<String, String>();
        for (Entry<String, TypeMirror> e : variablesToTypesTM.entrySet()) {
            if (e.getValue() == null) continue;
            variablesToTypes.put(e.getKey(), e.getValue().toString());
        }
        HintDescription hd = HintDescriptionFactory.create()
                                                   .setTrigger(PatternDescription.create(plainRule, variablesToTypes))
                                                   .setWorker(new HintDescription.Worker() {
            @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "", JavaFixUtilities.rewriteFix(ctx, "", ctx.getPath(), split[1])));
            }
        }).produce();

        List<ErrorDescription> computeHints = new HintsInvoker(HintsSettings.getGlobalSettings(), new AtomicBoolean()).computeHints(info, Collections.singleton(hd));

        assertEquals(computeHints.toString(), 1, computeHints.size());

        Fix fix = computeHints.get(0).getFixes().getFixes().get(0);

	fix.implement();

        assertEquals(golden, doc.getText(0, doc.getLength()));
    }

    public void performRemoveFromParentTest(String code, String rule, String golden) throws Exception {
	prepareTest("test/Test.java", code);

        HintDescription hd = HintDescriptionFactory.create()
                                                   .setTrigger(PatternDescription.create(rule, Collections.<String, String>emptyMap()))
                                                   .setWorker(new HintDescription.Worker() {
            @Override public Collection<? extends ErrorDescription> createErrors(HintContext ctx) {
                return Collections.singletonList(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), "", JavaFixUtilities.removeFromParent(ctx, "", ctx.getPath())));
            }
        }).produce();

        List<ErrorDescription> computeHints = new HintsInvoker(HintsSettings.getGlobalSettings(), new AtomicBoolean()).computeHints(info, Collections.singleton(hd));

        assertEquals(computeHints.toString(), 1, computeHints.size());

        Fix fix = computeHints.get(0).getFixes().getFixes().get(0);

	fix.implement();

        assertEquals(golden, doc.getText(0, doc.getLength()));
    }
 
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
