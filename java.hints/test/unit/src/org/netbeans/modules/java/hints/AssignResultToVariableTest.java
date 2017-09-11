/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.lang.reflect.Method;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Lahoda
 */
public class AssignResultToVariableTest extends TreeRuleTestBase {
    
    public AssignResultToVariableTest(String testName) {
        super(testName);
    }
    
    public void testDoNothingForVoidReturnType() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void t() {get();} public void get() {}}", 51);
    }

    public void testProposeHint() throws Exception {
        performAnalysisTest("test/Test.java", "package test; public class Test {public void t() {get();} public int get() {}}", 51, "0:51-0:51:hint:Assign Return Value To New Variable");
    }

    public void testApplyHintGenericType() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {java.util.List<String> l = null; l.get(0);}}",
                       111 - 25,
                       "0:86-0:86:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {java.util.List<String> l = null; String get = l.get(0); }}");
    }

    public void testApplyHintGenericType2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {java.util.List<? extends String> l = null; l.get(0);}}",
                       121 - 25,
                       "0:96-0:96:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {java.util.List<? extends String> l = null; String get = l.get(0); }}");
    }

    public void testApplyHintGenericType3() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test<T> {public void t() {get();} T get() {return null;}}",
                       79 - 25,
                       "0:54-0:54:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test<T> {public void t() {T get = get(); } T get() {return null;}}");
    }

    public void testApplyHintGenericType4() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {test();} private Iterable<? extends CharSequence> test() {return null;}}",
                       77 - 25,
                       "0:52-0:52:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {Iterable<? extends CharSequence> test = test(); } private Iterable<? extends CharSequence> test() {return null;}}");
    }

    public void testApplyHintGenericType5() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {test();} private Iterable<? super CharSequence> test() {return null;}}",
                       77 - 25,
                       "0:52-0:52:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {Iterable<? super CharSequence> test = test(); } private Iterable<? super CharSequence> test() {return null;}}");
    }

    public void testApplyHintGenericType6() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;l.get(0); } }",
                       117 - 25,
                       "0:92-0:92:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;Object get = l.get(0); } }");
    }

    public void testCommentsCopied() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {\n/*t*/get();\n} String get() {return null;}}",
                       82 - 25,
                       "1:6-1:6:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() { /*t*/ String get = get(); } String get() {return null;}}");
    }

    public void testNewClass1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new Te|st(); } private static class Test {} }",
                       "0:57-0:57:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() { Test test = new Test(); } private static class Test {} }");
    }

    public void testNewClass2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new te|st(); } private static class test {} }",
                       "0:57-0:57:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() { test test = new test(); } private static class test {} }");
    }

    public void testNewClass133825a() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new Te|st<String>(); } private static class Test<T> {}}",
                       "0:57-0:57:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() { Test<String> test = new Test<String>(); } private static class Test<T> {}}");
    }

    public void testNewClass133825b() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new Test.In|ner(); } private static class Inner {} }",
                       "0:62-0:62:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() { Inner inner = new Test.Inner(); } private static class Inner {} }");
    }

    public void testAnonymousClass138223() throws Exception {
        performFixTest("test/Test.java",
                "package test; public class Test {public void t() { new Run|nable() { public void run() { } }; } }",
                "0:58-0:58:hint:Assign Return Value To New Variable",
                "FixImpl",
                "package test; public class Test {public void t() { Runnable runnable = new Runnable() { public void run() { } }; } }");
    }

    public void testForgiving1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n  l.get(0);|\n } }",
                       "1:11-1:11:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null; Object get = l.get(0); } }");
    }

    public void testForgiving2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n  l.get(0)|;\n } }",
                       "1:10-1:10:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null; Object get = l.get(0); } }");
    }

    public void testForgiving3() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n  |l.get(0);\n } }",
                       "1:2-1:2:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null; Object get = l.get(0); } }");
    }

    public void testForgiving4() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n  l.get(0);    |\n } }",
                       "1:15-1:15:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null; Object get = l.get(0); } }");
    }

    public void testForgiving5() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n|  l.get(0);\n } }",
                       "1:0-1:0:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null; Object get = l.get(0); } }");
    }

    public void testForgiving6() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n  l.get(0);    //tttt|\n } }",
                       "1:21-1:21:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null; Object get = l.get(0); //tttt } }");
    }

    public void testForgiving7() throws Exception {
        performFixTest("test/Test.java",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;/*\n|*/  l.get(0);\n } }",
                       "1:0-1:0:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; import java.util.List; public class Test {public Test() {List<?> l = null;/* */ Object get = l.get(0); } }");
    }

    public void testForgivingNegative1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import java.util.List; public class Test {public Test() {int i = 0;i++;| } }");
    }

    public void testForgivingNegative2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n  l.get(0);|l.get(0);\n } }");
    }

    public void testForgivingNegative3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import java.util.List; public class Test {public Test() {List<?> l = null;\n  l.get(0); | l.get(0);\n } }");
    }

    public void testForgivingNegative188326() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "import java.util.List;\n" +
                            "public class Test {\n" +
                            "    {\n" +
                            "        new Runnable()\n" +
                            "        {|\n" +
                            "            public void run()\n" +
                            "            {\n" +
                            "            }\n" +
                            "        };\n" +
                            "    }\n" +
                            "}");
    }

    public void testAddSemicolon1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() { new Run|nable() { public void run() { } } } }",
                       "0:58-0:58:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() { Runnable runnable = new Runnable() { public void run() { } }; } }");
    }

    public void testAddSemicolon2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test {public void t() {java.util.List<String> l = null; l.ge|t(0) }}",
                       "0:87-0:87:hint:Assign Return Value To New Variable",
                       "FixImpl",
                       "package test; public class Test {public void t() {java.util.List<String> l = null; String get = l.get(0); }}");
    }

    public void test197050() throws Exception {
        performFixTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    static void f() {\n" +
                            "        class Test { }\n" +
                            "     |  new Test();\n" +
                            "    }\n" +
                            "}",
                            "4:5-4:5:hint:Assign Return Value To New Variable",
                            "FixImpl",
                            ("package test;\n" +
                             "class Test {\n" +
                             "    static void f() {\n" +
                             "        class Test { }\n" +
                             "        Test test = new Test();\n" +
                             "    }\n" +
                             "}").replaceAll("\\s+", " "));
    }
    
    public void test235716NewVariable() throws Exception {
        performFixTest("test/Test.java",
            "package test;\n" +
            "import java.util.Arrays;\n" +
            "class Test {\n" +
            "    static void f() {\n" +
            "       Arrays.as|List(Integer.class,String.class);\n" +
            "    }\n" +
            "}",
            "4:16-4:16:hint:Assign Return Value To New Variable",
            "FixImpl",
            ("package test;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
             "class Test {\n" +
             "    static void f() {\n" +
             "       List asList = Arrays.asList(Integer.class,String.class);\n" +  
             "    }\n" +
             "}").replaceAll("\\s+", " "));
    }
    
    public void testInferBounds258167() throws Exception {
        performFixTest("test/Test.java",
            "package test;\n" +
            "import java.util.Map;\n" +
            "public class Test {\n" +
            "    void test(Map<? extends String, ? extends Number> map) {\n"
                + "        map.entr|ySet();//assign return value to a new variable here\n"
                + "    }\n"
                + "}",                       
            "4:16-4:16:hint:Assign Return Value To New Variable",
            "FixImpl",
            ("package test;\n" +
            "import java.util.Map;\n" +
            "import java.util.Set;\n" +
            "public class Test {\n" +
            "    void test(Map<? extends String, ? extends Number> map) {\n"
                + "   Set<? extends Map.Entry<? extends String, ? extends Number>> entrySet = map.entrySet(); //assign return value to a new variable here\n"
                + "    }\n"
                + "}").replaceAll("\\s+", " "));
    }

    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path, int offset) {
        while (path != null && !new AssignResultToVariable().getTreeKinds().contains(path.getLeaf().getKind()))
            path = path.getParentPath();
        
        if (path == null)
            return null;
        
        try {
            Method m = CaretAwareJavaSourceTaskFactory.class.getDeclaredMethod("setLastPosition", FileObject.class, int.class);

            assertNotNull(m);

            m.setAccessible(true);

            m.invoke(null, new Object[]{info.getFileObject(), offset});
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        
        return new AssignResultToVariable().run(info, path);
    }

    @Override
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        if (f instanceof AssignResultToVariable.FixImpl) {
            return "FixImpl";
        } else {
            return super.toDebugString(info, f);
        }
    }
    
}
