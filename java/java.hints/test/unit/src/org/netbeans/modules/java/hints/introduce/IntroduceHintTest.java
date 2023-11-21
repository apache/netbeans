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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import java.awt.Dialog;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.lang.model.element.Modifier;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CodeStyle.InsertionPoint;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences.Provider;
import org.netbeans.modules.java.hints.introduce.IntroduceHint.InsertClassMember;
import org.netbeans.modules.java.hints.spiimpl.TestUtilities;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.ui.FmtOptions;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jan Lahoda
 */
public class IntroduceHintTest extends NbTestCase {

    public IntroduceHintTest(String testName) {
        super(testName);
    }

    private static Preferences codeStylePrefs;//XXX: does not allow parallel test execution
    private String sourceLevel;
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
        codeStylePrefs = NbPreferences.root().node("test/java/codestyle");
        IntroduceHint.INSERT_CLASS_MEMBER = new InsertClassMember() {
            @Override public ClassTree insertClassMember(WorkingCopy wc, ClassTree clazz, Tree member, int offset) throws IllegalStateException {
                return GeneratorUtilities.get(wc).insertClassMember(clazz, member);
            }
        };
    }

//    public static TestSuite suite() {
//        TestSuite s = new NbTestSuite();
//
//        s.addTest(new IntroduceHintTest("testIntroduceMethodReplaceDuplicatesNoRemap"));
//        s.addTest(new IntroduceHintTest("testIntroduceMethodReplaceDuplicatesSimpleRemap"));
//        s.addTest(new IntroduceHintTest("testIntroduceMethodReplaceDuplicatesRemapExpression"));
//
//        return s;
//    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        LifecycleManager.getDefault().saveAll();
        codeStylePrefs = null;
        info = null;
        doc = null;
    }

    public void testCorrectSelection1() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int i = 3;}}", 110 - 49, 111 - 49, true);
    }

    public void testCorrectSelection2() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test(int i) {|i = 3;|}}", false);
    }

    public void testCorrectSelection3() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int z = 0; int i = z + 2;}}", 121 - 49, 124 - 49, false);
    }

    public void testCorrectSelection4() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; System.err.println((\"x=\" + y).length());}}", 83, 102, true);
    }

    public void testCorrectSelection5() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; System.err.println((\"x=\" + y).length());}}", 64, 103, false);
    }

    public void testCorrectSelection6() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; System.err.println((\"x=\" + y).length());}}", 64, 104, false);
    }

    public void testCorrectSelection7() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; y = 2;}}", 64, 69, false);
    }

    public void testCorrectSelection8() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = (int)Math.round(1.2);}}", 111 - 49, 114 - 49, false);
    }

    public void testCorrectSelection9() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {long y = Math.round(1.2);}}", 111 - 49, 126 - 49, true);
    }

    public void testCorrectSelection10() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {String s = \"\"; int y = s.length();}}", 125 - 49, 135 - 49, true);
    }

    public void testCorrectSelection11() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {System.err.println();}}", 102 - 49, 120 - 49, false);
    }

    public void testCorrectSelection12() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test(|String|[] s) {}}", false);
    }

    public void testCorrectSelection13() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {new |Object|();}}", false);
    }

    public void test121420() throws Exception {
        performFixTest("package test; import java.util.ArrayList; public class Test {public void test() { |new ArrayList<String>()|; }}", "package test; import java.util.ArrayList; public class Test {public void test() { ArrayList<String> arrayList = new ArrayList<String>(); }}", new DialogDisplayerImpl(null, false, false, true), 5, 0);
    }

    public void test142424() throws Exception {
        performFixTest("package test; public class Test {private static void bar(int i) {} public void test() {new Runnable() {public void run() {String foo = \"foo\";bar(|foo.length()|);}}.run();}}",
                       "package test; public class Test {private static void bar(int i) {} public void test() {new Runnable() {public void run() {String foo = \"foo\";int length = foo.length(); bar(length);}}.run();}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       4, 0);
    }

    public void testFix1() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3; int name = y + 9; int x = name;}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       4, 0);
    }

    public void testFix2() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3; int nueName = y + 9; int x = nueName;}}",
                       new DialogDisplayerImpl("nueName", false, false, true),
                       4, 0);
    }

    public void testFix3() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3; int name = y + 9; int x = name; x = y + 9;}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       4, 0);
    }

    public void testFix4() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       72, 77,
                       "package test; public class Test {public void test() {int y = 3; int name = y + 9; int x = name; x = name;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       4, 0);
    }

    public void testFix5() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       108 - 25, 113 - 25,
                       "package test; public class Test {public void test() {int y = 3; int name = y + 9; int x = name; x = name;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       4, 0);
    }

    public void testFix6() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; int x = y + 9; x = y + 9;}}",
                       108 - 25, 113 - 25,
                       "package test; public class Test {public void test() {int y = 3; final int name = y + 9; int x = name; x = name;}}",
                       new DialogDisplayerImpl(null, true, true, true),
                       4, 0);
    }

    public void testFix7() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; if (true) y = y + 9; y = y + 9;}}",
                       103 - 25, 108 - 25,
                       "package test; public class Test {public void test() {int y = 3; int name = y + 9; if (true) y = name; y = y + 9;}}",
                       new DialogDisplayerImpl(null, false, false, true),
                       4, 0);
    }

    public void testFix8() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3; if (true) y = y + 9; y = y + 9;}}",
                       114 - 25, 119 - 25,
                       "package test; public class Test {public void test() {int y = 3; int name = y + 9; if (true) y = name; y = name;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       4, 0);
    }

    public void testFix9() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 8 + 9;} public void test2() { int y = 8 + 9;}}",
                       86 - 25, 91 - 25,
                       "package test; public class Test {public void test() {int name = 8 + 9; int y = name;} public void test2() { int y = 8 + 9;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       5, 0);
    }

    public void testFix10() throws Exception {
        performFixTest("package test; public class Test {public void test(int y) {while (y != 7) {y = 3 + 4;} y = 3 + 4;}}",
                       115 - 25, 120 - 25,
                       "package test; public class Test {public void test(int y) {int name = 3 + 4; while (y != 7) {y = name;} y = name;}}",
                       new DialogDisplayerImpl(null, true, false, true),
                       5, 0);
    }

    public void testFix11() throws Exception {
        performFixTest("package test; import java.util.List; public class Test {public void test1() {List<? extends CharSequence> l = |test()|;} public List<? extends CharSequence> test() {return null;}}",
                       "package test; import java.util.List; public class Test {public void test1() {List<? extends CharSequence> name = test(); List<? extends CharSequence> l = name;} public List<? extends CharSequence> test() {return null;}}",
                       new DialogDisplayerImpl("name", true, false, true),
                       4, 0);
    }

    public void testFix12() throws Exception {
        performFixTest("package test; import java.util.List; public class Test {public void test1() {List<? extends CharSequence> l = null; CharSequence c = |l.get(0)|;} }",
                       "package test; import java.util.List; public class Test {public void test1() {List<? extends CharSequence> l = null; CharSequence name = l.get(0); CharSequence c = name;} }",
                       new DialogDisplayerImpl("name", true, false, true),
                       4, 0);
    }

    public void testFix126460() throws Exception {
        performFixTest("package test; import java.util.List; public class Test {public void test1() {List<String> l = null; assert |l.get(0)| == null;} }",
                       "package test; import java.util.List; public class Test {public void test1() {List<String> l = null; String name = l.get(0); assert name == null;} }",
                       new DialogDisplayerImpl("name", true, false, true),
                       4, 0);
    }

    public void testFix126269() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        javax.swing.JTable table = null;\n" +
                       "        if (true) {\n" +
                       "            table.getColumnModel().getColumn(0);\n" +
                       "        } else {\n" +
                       "            |table.getColumnModel()|.getColumn(0);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       "package test;\n" +
                       "import javax.swing.table.TableColumnModel;\n" +
                       "public class Test {\n" +
                       "    public void test() {\n" +
                       "        javax.swing.JTable table = null;\n" +
                       "        TableColumnModel name = table.getColumnModel();\n" +
                       "        if (true) {\n" +
                       "            name.getColumn(0);\n" +
                       "        } else {\n" +
                       "            name.getColumn(0);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       new DialogDisplayerImpl("name", true, false, true),
                       4, 0);
    }

    public void testFix180164() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    private final Object o = new Runnable() {\n" +
                       "        public void run() {\n" +
                       "            String u = null;\n" +
                       "            String n = |u|;\n" +
                       "        }\n" +
                       "    };\n" +
                       "}\n",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    private final Object o = new Runnable() {\n" +
                       "        public void run() {\n" +
                       "            String u = null;\n" +
                       "            String name = u;\n" +
                       "            String n = name;\n" +
                       "        }\n" +
                       "    };\n" +
                       "}\n",
                       new DialogDisplayerImpl("name", true, false, true),
                       4, 0);
    }

    public void testFixNewClassTree179766() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        t(|new Runnable() {\n" +
                       "            public void run() {\n" +
                       "                throw new UnsupportedOperationException();\n" +
                       "            }\n" +
                       "        }|);\n" +
                       "    }\n" +
                       "    private static void t(Runnable r) {}\n" +
                       "}\n",
                       "package test; public class Test { public static void test() { Runnable name = new Runnable() { public void run() { throw new UnsupportedOperationException(); } }; t(name); } private static void t(Runnable r) {} } ",
                       new DialogDisplayerImpl("name", true, false, true),
                       5, 0);
    }

    public void testSwitchCase219714() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public String method(String... args) {\n" +
                       "        switch (args.length) {\n" +
                       "            case 1:\n" +
                       "                return |args[0]|;\n" +
                       "        }\n" +
                       "        return null;\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public String method(String... args) {\n" +
                        "        switch (args.length) {\n" +
                        "            case 1:\n" +
                        "                String name = args[0];\n" +
                        "                return name;\n" +
                        "        }\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("name", true, false, true),
                       4, 0);
    }

    //note the comment assignment done for this test in prepareTest
    public void testCommentVariable() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method(Object object) {\n" +
                       "        System.err.println(\"1\");\n" +
                       "        //comment 1\n" +
                       "        |object.toString()|; //comment 2\n" +
                       "        //comment 3\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                       "    public void method(Object object) {\n" +
                       "        System.err.println(\"1\");\n" +
                       "        //comment 1\n" +
                       "        String name = object.toString(); //comment 2\n" +
                       "        //comment 3\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("name", true, false, true),
                       4, 0);
    }
    
//    public void testFix121420() throws Exception {
//        performFixTest("package test; public class Test {public void test1() {|System.getProperty(\"\")|;} }",
//                       "package test; public class Test {public void test1() { String name = System.getProperty(\"\");} }",
//                       new DialogDisplayerImpl("name", true, null, true),
//                       2, 0);
//    }

    public void testSimple4() throws Exception {
        performSimpleSelectionVerificationTest("package test; import java.util.ArrayList; public class Test {public void test() {Object o = new ArrayList<String>();}}", 141 - 49, 164 - 49, true);
    }

    public void testConstant1() throws Exception {
        performConstantAccessTest("package test; public class Test {public void test() {int i = 1 + 2;}}", 97 - 36, 102 - 36, true);
    }

    public void testConstant2() throws Exception {
        performConstantAccessTest("package test; public class Test {private int i = 0; public void test() {int x = 1 + i;}}", 116 - 36, 121 - 36, false);
    }

    public void testConstant3() throws Exception {
        performConstantAccessTest("package test; public class Test {private static int i = 0; public void test() {int x = 1 + i;}}", 123 - 36, 128 - 36, false);
    }

    public void testConstant4() throws Exception {
        performConstantAccessTest("package test; public class Test {private final int i = 0; public void test() {int x = 1 + i;}}", 122 - 36, 127 - 36, false);
    }

    public void testConstant5() throws Exception {
        performConstantAccessTest("package test; public class Test {private static final int i = 0; public void test() {int x = 1 + i;}}", 129 - 36, 134 - 36, true);
    }

    public void testConstant187444a() throws Exception {
        performConstantAccessTest("package test; public class Test {private static final double i = |-2.4|;}", true);
    }

    public void testConstant187444b() throws Exception {
        performConstantAccessTest("package test; public class Test {private static final int i = |~(2 + 4)|;}", true);
    }

    public void testConstant187444c() throws Exception {
        performConstantAccessTest("package test; public class Test {int y = 1; private final int i = |~(2 + y)|; }", false);
    }

    public void testConstantFix1() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4;}}",
                       86 - 25, 91 - 25,
                       "package test; public class Test { private static final int NAME = 3 + 4; public void test() {int y = NAME;}}",
                       new DialogDisplayerImpl(null, false, null, true),
                       5, 1);
    }

    public void testConstantFixNoVariable() throws Exception {
        performFixTest("package test; public class Test { int y = 3 + 4;}",
                       67 - 25, 72 - 25,
                       "package test; public class Test { private static final int NAME = 3 + 4; int y = NAME;}",
                       new DialogDisplayerImpl(null, false, null, true),
                       1, 0);
    }

    public void testConstantFix2() throws Exception {
        performFixTest("package test; public class Test { int y = 3 + 4; int z = 3 + 4;}",
                       67 - 25, 72 - 25,
                       "package test; public class Test { private static final int NAME = 3 + 4; int y = NAME; int z = NAME;}",
                       new DialogDisplayerImpl(null, true, null, true),
                       1, 0);
    }

    public void testConstantFix106490a() throws Exception {
        performFixTest("package test; public class Test { int y = 3 + 4; int z = 3 + 4;}",
                       67 - 25, 72 - 25,
                       "package test; public class Test { public static final int NAME = 3 + 4; int y = NAME; int z = NAME;}",
                       new DialogDisplayerImpl(null, true, null, true, EnumSet
                .of(Modifier.PUBLIC)),
                       1, 0);
    }

    public void testConstantFix106490b() throws Exception {
        performFixTest("package test; public class Test { int y = 3 + 4; int z = 3 + 4;}",
                       67 - 25, 72 - 25,
                       "package test; public class Test { static final int NAME = 3 + 4; int y = NAME; int z = NAME;}",
                       new DialogDisplayerImpl(null, true, null, true, EnumSet
                .noneOf(Modifier.class)),
                       1, 0);
    }

    public void testConstantFix130938() throws Exception {
        performFixTest("package test;import java.util.logging.Level;import java.util.logging.Logger;public class Test {public void foo() { Logger.getLogger(Test.class.getName()).log(Level.FINEST, \"foo\");}}",
                       140 - 25,
                       178 - 25,
                       "package test;import java.util.logging.Level;import java.util.logging.Logger;public class Test { static final Logger LOGGER = Logger.getLogger(Test.class.getName()); public void foo() { LOGGER.log(Level.FINEST, \"foo\");}}",
                       new DialogDisplayerImpl(null, true, true, true, EnumSet
                .noneOf(Modifier.class)),
                       5, 1);
    }

    public void testIntroduceFieldFix1() throws Exception {
        performCheckFixesTest("package test; public class Test {int y = 3 + 4; int z = 3 + 4;}",
                              73 - 32, 78 - 32,
                              "[IntroduceFix:NAME:2:CREATE_CONSTANT]");
    }

    public void testIntroduceFieldFix2() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int y = 3 + 4; int z = 3 + 4;}}",
                              93 - 32, 98 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceFix:NAME:2:CREATE_CONSTANT]",
                              "[IntroduceField:name:2:false:false:[7, 7]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix3() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int y = 3 + 4; int z = 3 + 4;} public void test2() {int u = 3 + 4;}}",
                              93 - 32, 98 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceFix:NAME:3:CREATE_CONSTANT]",
                              "[IntroduceField:name:3:false:false:[7, 6]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix4() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int u = 0; int y = u + 4; int z = u + 4;} public void test2() {int u = 0; int a = u + 4;}}",
                              104 - 32, 109 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceField:name:2:false:false:[1, 1]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix5() throws Exception {
        performCheckFixesTest("package test; public class Test {int u = 0; public void test() {int y = u + 4; int z = u + 4;} public void test2() {int a = u + 4;}}",
                              104 - 32, 109 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceField:name:3:false:false:[7, 6]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix7() throws Exception {
        performCheckFixesTest("package test; public class Test {public void test() {int u = 0; int y = u + 4; int z = u + 4;}}",
                              104 - 32, 109 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceField:name:2:false:false:[1, 1]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix8() throws Exception {
        performCheckFixesTest("package test; public class Test {int u = 0; public void test() {int y = u + 4; int z = u + 4;}}",
                              104 - 32, 109 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceField:name:2:false:false:[7, 7]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix9() throws Exception {
        performCheckFixesTest("package test; public class Test {int u = 0; public void test() {int y = u + 4; int z = u + 4;} private int i = 4;}",
                              108 - 32, 109 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceFix:NAME:3:CREATE_CONSTANT]",
                              "[IntroduceField:name:3:false:false:[7, 6]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix10() throws Exception {
        performCheckFixesTest("package test; public class Test {static int u = 0; public static void test() {int y = u + 4; int z = u + 4;}}",
                              118 - 32, 123 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceField:name:2:true:false:[3, 3]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix11() throws Exception {
        performCheckFixesTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;}}",
                              88 - 32, 93 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceFix:NAME:2:CREATE_CONSTANT]",
                              "[IntroduceField:name:2:false:true:[7, 7]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix12() throws Exception {
        performCheckFixesTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                              88 - 32, 93 - 32,
                              "[IntroduceFix:name:2:CREATE_VARIABLE]",
                              "[IntroduceFix:NAME:2:CREATE_CONSTANT]",
                              "[IntroduceField:name:2:false:false:[7, 7]]",
                              "[IntroduceExpressionBasedMethodFix]",
                              "[Introduce Parameter Fix]");
    }

    public void testIntroduceFieldFix13() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int name = 3 + 4; public Test() {int y = name; int z = 3 + 4;} public Test(int i) {}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix14() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int name; public Test() {name = 3 + 4; int y = name; int z = 3 + 4;} public Test(int i) {}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_METHOD, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix15() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int name; public Test() {name = 3 + 4; int y = name; int z = 3 + 4;} public Test(int i) {name = 3 + 4; }}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix16() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int i; public Test() {i = 3 + 4; int y = i; int z = 3 + 4;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix17() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private int i; public Test() {i = 3 + 4; int y = i; int z = i;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, true, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix18() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { public int i; public Test() {i = 3 + 4; int y = i; int z = i;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, true, EnumSet
                .<Modifier>of(Modifier.PUBLIC), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix19() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { public final int i; public Test() {i = 3 + 4; int y = i; int z = i;} public Test(int i) {this.i = 3 + 4; }}",
                       new DialogDisplayerImpl2("i", IntroduceFieldPanel.INIT_CONSTRUCTORS, true, EnumSet
                .<Modifier>of(Modifier.PUBLIC), true, true),
                       5, 2);
    }

    public void testIntroduceFieldFix20() throws Exception {
        performFixTest("package test; public class Test {public void test() { int y = 3 + 4; int z = 3 + 4;}}",
                       87 - 25, 92 - 25,
                       "package test; public class Test { private int name; public Test() { name = 3 + 4; } public void test() { int y = name; int z = 3 + 4;}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testFix21() throws Exception {
        performFixTest("package test; import java.util.List; public class Test {public void test1() {List<? extends CharSequence> l = |test()|;} public List<? extends CharSequence> test() {return null;}}",
                       "package test; import java.util.List; public class Test { private List<? extends CharSequence> name; public void test1() {name = test(); List<? extends CharSequence> l = name;} public List<? extends CharSequence> test() {return null;}}",
                       new DialogDisplayerImpl2("name", IntroduceFieldPanel.INIT_METHOD, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       4, 1);
    }

    public void testFix22() throws Exception {
        performFixTest("package test; import java.util.List; public class Test {public void test1() {List<? extends CharSequence> l = null; CharSequence c = |l.get(0)|;} }",
                       "package test; import java.util.List; public class Test { private CharSequence name; public void test1() {List<? extends CharSequence> l = null; name = l.get(0); CharSequence c = name;} }",
                       new DialogDisplayerImpl2("name", IntroduceFieldPanel.INIT_METHOD, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       4, 1);
    }

    public void testIntroduceFieldFix114350() throws Exception {
        performFixTest("package test; public class Test {\n" +
                       "    public Test() {\n" +
                       "        super();\n" +
                       "        System.out.println(\"ctor\");\n" +
                       "    }\n" +
                       "    public void method() {\n" +
                       "        String s = |\"const\"|;\n" +
                       "    }\n" +
                       "}\n",
                       "package test; public class Test { private String aconst; public Test() { super(); aconst = \"const\"; System.out.println(\"ctor\"); } public void method() { String s = aconst; } } ",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix114360() throws Exception {
        performFixTest("package test; public enum Test {\n" +
                       "    A;\n" +
                       "    public void method() {\n" +
                       "        String s = |\"const\"|;\n" +
                       "    }\n" +
                       "}\n",
                       "package test; public enum Test { A; private String aconst; Test() { aconst = \"const\"; } public void method() { String s = aconst; } } ",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix120271() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    |@Deprecated|\n" +
                       "    private static void test() {}\n" +
                       "}",
                       null,
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       0, 0);
    }

    /**
     * Tests adding 'static' kw if some of replaced occurences has been in
     * static context
     *
     * @throws java.lang.Exception
     */
    public void testIntroduceFieldFix106495() throws Exception {
        performFixTest("package test; public class Test {public Test() {int y = 3 + 4; int z = 3 + 4;} public Test(int i) {} public static void a() {int y = 3 + 4;}}",
                       88 - 32, 93 - 32,
                       "package test; public class Test { private static int name = 3 + 4; public Test() {int y = name; int z = name;} public Test(int i) {} public static void a() {int y = name;}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, true, EnumSet
                .<Modifier>of(Modifier.PRIVATE, Modifier.STATIC), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix213972() throws Exception {
        performFixTest("package test; public class Test {public void test1() {|int i = 3;|} public void test2() {int i = 3;}}",
                       "package test; public class Test { private int i = 3; public void test1() {} public void test2() {int i = 3;}}",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, true, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       3, 1);
    }

    public void testIntroduceFieldFixNewClassTree179766() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        t(|new Runnable() {\n" +
                       "            public void run() {\n" +
                       "                throw new UnsupportedOperationException();\n" +
                       "            }\n" +
                       "        }|);\n" +
                       "    }\n" +
                       "    private static void t(Runnable r) {}\n" +
                       "}",
                       "package test; public class Test { private static Runnable runnable; public static void test() { t(runnable); } private static void t(Runnable r) {} public Test() { runnable = new Runnable() { public void run() { throw new UnsupportedOperationException(); } }; } }",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testCorrectMethodSelection1() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {int i = 3;}}", 105 - 52, 115 - 52, true, new int[]{0, 0});
    }

    public void testCorrectMethodSelection2() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {int i = 3; i += 2; i += 3;}}", 116 - 52, 123 - 52, true, new int[]{1, 1});
    }

    public void testCorrectMethodSelection3() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {int i = 3;  i += 2; i += 3;}}", 116 - 52, 125 - 52, true, new int[]{1, 1});
    }

    public void testCorrectMethodSelection4() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {Object o = null;}}", 108 - 52, 121 - 52, false, new int[]{0, 0});
    }

    public void testCorrectMethodSelection5() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {Object o = null;}}", 105 - 52, 105 - 52, false, new int[]{0, 0});
    }

    public void testCorrectMethodSelection6() throws Exception {
        performStatementSelectionVerificationTest("package test; public class Test {public void test() {       Object o = null;}}", 107 - 52, 107 - 52, false, new int[]{0, 0});
    }

    public void testIntroduceMethodFix1() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; int z = 3 + 4;}}",
                       78 - 25, 92 - 25,
                       "package test; public class Test {public void test() {name(); int z = 3 + 4;} private void name() { int y = 3 + 4; } }",
                       new DialogDisplayerImpl3("name", null, true),
                       3, 2);
    }

    public void testIntroduceMethodFix2() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; int z = y + 4;}}",
                       93 - 25, 107 - 25,
                       "package test; public class Test {public void test() {int y = 3 + 4; name(y); } private void name(int y) { int z = y + 4; } }",
                       new DialogDisplayerImpl3("name", null, true),
                       2, 1);
    }

    public void testIntroduceMethodFix3() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; y += 4; int z = y + 4;}}",
                       93 - 25, 100 - 25,
                       "package test; public class Test {public void test() {int y = 3 + 4; y = name(y); int z = y + 4;} private int name(int y) { y += 4; return y; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }

    public void testIntroduceMethodFix4() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; y += 4; int a = 4; int z = y + a;}}",
                       93 - 25, 111 - 25,
                       null,
                       new DialogDisplayerImpl3("name", null, true), 0, -1);
    }

    public void testIntroduceMethodFix5() throws Exception {
        performFixTest("package test; public class Test {public void test() {int y = 3 + 4; int a = y + 4; int z = y + a;}}",
                       93 - 25, 107 - 25,
                       "package test; public class Test {public void test() {int y = 3 + 4; int a = name(y); int z = y + a;} private int name(int y) { int a = y + 4; return a; } }",
                       new DialogDisplayerImpl3("name", null, true),
                       2, 1);
    }

    public void testIntroduceMethodFix6() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test() throws IOException {int y = 3 + 4; throw new IOException();}}",
                       140 - 25, 164 - 25,
                       "package test; import java.io.IOException; public class Test {public void test() throws IOException {int y = 3 + 4; name(); } private void name() throws IOException { throw new IOException(); } }",
                       new DialogDisplayerImpl3("name", null, true));
    }

    public void testIntroduceMethodFix7() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test() {while (true) {int y = 3 + 4;}}}",
                       120 - 25, 134 - 25,
                       "package test; import java.io.IOException; public class Test {public void test() {while (true) {name(); }} private void name() { int y = 3 + 4; } }",
                       new DialogDisplayerImpl3("name", null, true),
                       3, 2);
    }

    public void testIntroduceMethodFix8() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) break;}}}",
                       125 - 25, 145 - 25,
                       "package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (name(y)) break;}} private boolean name(int y) { if (--y <= 0) { return true; } return false; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }

    public void testIntroduceMethodFix9() throws Exception {
        performErrorMessageTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) {y = 3; break;}} int u = y;}}",
                                134 - 34, 163 - 34,
                                IntroduceKind.CREATE_METHOD,
                                "ERR_Too_Many_Return_Values");
    }

    public void testIntroduceMethodFix10() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) { y = 2; break; } else { y = 3; break; }} int u = y;}}",
                       125 - 25, 179 - 25,
                       "package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {y = name(y); break; } int u = y;} private int name(int y) { if (--y <= 0) { y = 2; return y; } else { y = 3; return y; } } }",
                       new DialogDisplayerImpl3("name", null, true));
    }

    public void testIntroduceMethodFix11() throws Exception {
        performFixTest("package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {if (--y <= 0) { break; } else { break; }}}}",
                       125 - 25, 165 - 25,
                       "package test; import java.io.IOException; public class Test {public void test(int y) {while (true) {name(y); break; }} private void name(int y) { if (--y <= 0) { return; } else { return; } } }",
                       new DialogDisplayerImpl3("name", null, true));
    }

    public void testIntroduceMethodFix12() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { return 1; } else { return 2; }}}}",
                       96 - 25, 142 - 25,
                       "package test; public class Test {public int test(int y) {while (true) {return name(y); }} private int name(int y) { if (--y <= 0) { return 1; } else { return 2; } } }",
                       new DialogDisplayerImpl3("name", null, true));
    }

    public void testIntroduceMethodPosition() throws Exception {
        performFixTest("package test; public class Test {public void foo() { int i = 1; } public void foo1() {}}", 78 - 25, 88 - 25,
                       "package test; public class Test {public void foo() { name(); } public void foo1() {} private void name() { int i = 1; } }",
                       new DialogDisplayerImpl3("name", null, true),
                       3, 2);
    }

    //not working because of code generator bug:
    public void XtestIntroduceMethodFix13() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { while (true) break; } else { return 2; } return 3;}}}",
                       96 - 25, 152 - 25,
                       "package test; public class Test {public int test(int y) {while (true) { if (name(y)) { return 2; } return 3;}} private boolean name(int y) { if (--y <= 0) { while (true) { break; } } else { return true; } return false; } }",
                       new DialogDisplayerImpl3("name", null, true));
    }

    //not working because of code generator bug:
    public void XtestIntroduceMethodFix14() throws Exception {
        performFixTest("package test; public class Test {public void test(int y) {if (3 != 4) return ;}}",
                       83 - 25, 103 - 25,
                       "package test; public class Test {public void test(int y) {if (3 != 4) return ;}}",
                       new DialogDisplayerImpl3("name", null, true));
    }

    public void testIntroduceMethodFixNeverEnds1() throws Exception {
        performFixTest("package test; public class Test {}    ",
                       60 - 25, 61 - 25,
                       null,
                       new DialogDisplayerImpl(null, null, null, false));
    }

    public void testIntroduceMethodFixNeverEnds2() throws Exception {
        performFixTest("     package test; public class Test {}",
                       26 - 25, 28 - 25,
                       null,
                       new DialogDisplayerImpl(null, null, null, false));
    }

    public void testIntroduceMethodFix106490a() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { return 1; } else { return 2; }}}}",
                       96 - 25, 142 - 25,
                       "package test; public class Test {public int test(int y) {while (true) {return name(y); }} public int name(int y) { if (--y <= 0) { return 1; } else { return 2; } } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PUBLIC), true));
    }

    public void testIntroduceMethodFix106490b() throws Exception {
        performFixTest("package test; public class Test {public int test(int y) {while (true) {if (--y <= 0) { return 1; } else { return 2; }}}}",
                       96 - 25, 142 - 25,
                       "package test; public class Test {public int test(int y) {while (true) {return name(y); }} int name(int y) { if (--y <= 0) { return 1; } else { return 2; } } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .noneOf(Modifier.class), true));
    }

    public void testIntroduceMethodFixStatic() throws Exception {
        performFixTest("package test; public class Test {public static int test(int y) {y += 5; return y;}}",
                       89 - 25, 96 - 25,
                       "package test; public class Test {public static int test(int y) {y = name(y); return y;} private static int name(int y) { y += 5; return y; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    /**
     * Return statement inside anonymous class should not be considered
     */
    public void testIntroduceMethodFix132434() throws Exception {
        performFixTest("package test;import java.awt.event.MouseAdapter;import java.awt.event.MouseEvent;import javax.swing.JPanel;public class Test {public static void main(String[] args) {JPanel p = new JPanel();|p.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { if (e.getX() > 100) { return; } else { System.out.println(e.getX()); } } });|}}",
                       "package test;import java.awt.event.MouseAdapter;import java.awt.event.MouseEvent;import javax.swing.JPanel;public class Test {public static void main(String[] args) {JPanel p = new JPanel();foo(p);} private static void foo(JPanel p) { p.addMouseListener(new MouseAdapter() { public void mousePressed(MouseEvent e) { if (e.getX() > 100) { return; } else { System.out.println(e.getX()); } } }); } }",
                       new DialogDisplayerImpl3("foo", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod109663a() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {while (y < 10) {if (y == 0) break; else y++; int u = y;}}}",
                                106 - 25, 134 - 25,
                                IntroduceKind.CREATE_METHOD,
                                "ERR_Too_Many_Return_Values");
    }

    public void testIntroduceMethod109663b() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {while (y < 10) {if (y == 0) break; else y++;}}}",
                                106 - 25, 134 - 25,
                                IntroduceKind.CREATE_METHOD,
                                "ERR_Too_Many_Return_Values");
    }

    public void testIntroduceMethod109663c() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {do {if (y == 0) break; else y++;} while (y < 10); }}",
                                103 - 34, 131 - 34,
                                IntroduceKind.CREATE_METHOD,
                                "ERR_Too_Many_Return_Values");
    }

    public void testIntroduceMethod109663d() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {for ( ; y < 10; ) {if (y == 0) break; else y++;}}}",
                                118 - 34, 146 - 34,
                                IntroduceKind.CREATE_METHOD,
                                "ERR_Too_Many_Return_Values");
    }

    public void testIntroduceMethod109663e() throws Exception {
        performErrorMessageTest("package test; public class Test {public static void test(int y) {for ( ; ; y++) {if (y == 0) break; else y++;}}}",
                                115 - 34, 143 - 34,
                                IntroduceKind.CREATE_METHOD,
                                "ERR_Too_Many_Return_Values");
    }

    public void testIntroduceMethod109663f() throws Exception {
        performFixTest("package test; public class Test {public static void test(int y) {for (int u = y ; ; ) {if (y == 0) break; else y++;}}}",
                       112 - 25, 140 - 25,
                       "package test; public class Test {public static void test(int y) {for (int u = y ; ; ) {if (name(y)) break;}} private static boolean name(int y) { if (y == 0) { return true; } else { y++; } return false; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod109663g() throws Exception {
        performFixTest("package test; public class Test {public static void test(int y) {for (Integer i : java.util.Arrays.asList(y)) {if (y == 0) break; else y++;}}}",
                       136 - 25, 164 - 25,
                       "package test; public class Test {public static void test(int y) {for (Integer i : java.util.Arrays.asList(y)) {if (name(y)) break;}} private static boolean name(int y) { if (y == 0) { return true; } else { y++; } return false; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void test107689() throws Exception {
        performSimpleSelectionVerificationTest("package test; import java.util.List; public class Test {}",
                                               53 - 32, 67 - 32, false);
    }

    public void testIntroduceMethod112552a() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; while (true) {if (first) {first = false;} else {break;}}}}",
                       130 - 25, 144 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; while (true) {if (first) {first = name();} else {break;}}} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod112552b() throws Exception {
        performFixTest("package test; public class Test {public static void t(int a) {boolean first = true; while (true) {if (first) {while (a != 1) {first = false;}} else {break;}}}}",
                       151 - 25, 165 - 25,
                       "package test; public class Test {public static void t(int a) {boolean first = true; while (true) {if (first) {while (a != 1) {first = name();}} else {break;}}} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod112552c() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; for (;;) {if (first) {first = false;} else {break;}}}}",
                       126 - 25, 140 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; for (;;) {if (first) {first = name();} else {break;}}} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod112552d() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; do {if (first) {first = false;} else {break;}} while (true);}}",
                       120 - 25, 134 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; do {if (first) {first = name();} else {break;}} while (true);} private static boolean name() { boolean first; first = false; return first; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod112552e() throws Exception {
        performFixTest("package test; public class Test {public static void t() {boolean first = true; while (true) {first = false; while (first) {System.err.println();}}}}",
                       148 - 25, 169 - 25,
                       "package test; public class Test {public static void t() {boolean first = true; while (true) {first = false; while (first) {name();}}} private static void name() { System.err.println(); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod111896a() throws Exception {
        performFixTest("package test; public class Test {public static void t() {new Runnable() { private  int i; public void run() { } };}}",
                       82 - 25, 139 - 25,
                       "package test; public class Test {public static void t() {name();} private static void name() { new Runnable() { private int i; public void run() { } }; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod111896b() throws Exception {
        performFixTest("package test; public class Test {public static void t() {final int a = 0; new Runnable() { private  int i; public void run() { i = a; } };}}",
                       99 - 25, 163 - 25,
                       "package test; public class Test {public static void t() {final int a = 0; name(a);} private static void name(final int a) { new Runnable() { private int i; public void run() { i = a; } }; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod111896c() throws Exception {
        performFixTest("package test; public class Test {public static void t() {final int a = 0; new Runnable() { private  int i; public void run() { int a = i; } }; int b = a;}}",
                       99 - 25, 167 - 25,
                       "package test; public class Test {public static void t() {final int a = 0; name(); int b = a;} private static void name() { new Runnable() { private int i; public void run() { int a = i; } }; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethodUselessLocalVariable() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        |int c = 0;\n" +
                       "        \n" +
                       "        c = 3;|\n" +
                       "        \n" +
                       "        System.err.println(c);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void main(String[] args) { int c = name(); System.err.println(c); } private static int name() { int c = 0; c = 3; return c; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod114371() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(boolean arg) {\n" +
                       "        int c = 0;\n" +
                       "        \n" +
                       "        |if (arg) c = 3;|\n" +
                       "        \n" +
                       "        System.err.println(c);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test(boolean arg) { int c = 0; c = name(arg, c); System.err.println(c); } private static int name(boolean arg, int c) { if (arg) c = 3; return c; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod179258() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        String test = null;\n" +
                       "        |test = \"foo\";\n" +
                       "        if (test == null) {\n" +
                       "            System.err.println(1);\n" +
                       "        } else {\n" +
                       "            System.err.println(2);\n" +
                       "        }|\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void test() {\n" +
                        "        String test = null;\n" +
                        "        name();\n" +
                        "    }\n" +
                        "    private static void name() {\n" +
                        "        String test;\n" +
                        "        test = \"foo\";\n" +
                        "        if (test == null) {\n" +
                        "            System.err.println(1);\n" +
                        "        } else {\n" +
                        "            System.err.println(2);\n" +
                        "        }\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethod116199() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(boolean arg) {\n" +
                       "        |String allianceString = new String(\"[]\");" +
                       "        allianceString += \"\";|" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test(boolean arg) { name(); } private static void name() { String allianceString = new String(\"[]\"); allianceString += \"\"; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethodComments170213() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(boolean arg) {\n" +
                       "        |//t1\n" +
                       "        String allianceString = new String(\"[]\");\n" +
                       "        //t2\n" +
                       "        allianceString += \"\";\n" +
                       "        //t3|\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test(boolean arg) { name(); } private static void name() { //t1\nString allianceString = new String(\"[]\"); //t2\nallianceString += \"\"; //t3\n} }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

//    public void testIntroduceMethod109489() throws Exception {
//        performErrorMessageTest("package test;\n" +
//                                "public class Test {\n" +
//                                "    public static void test(boolean arg) {\n" +
//                                "        |int i;|\n" +
//                                "        i = 0;\n" +
//                                "    }\n" +
//                                "}",
//                                IntroduceKind.CREATE_METHOD,
//                                "");
//    }

    public void testIntroduceMethodFromExpression1() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(int a) {\n" +
                       "        int b = 0;\n" +
                       "        int c = |a + b|;\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test(int a) { int b = 0; int c = name(a, b); } private static int name(int a, int b) { return a + b; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       4, 2);
    }

    public void testIntroduceMethodFromExpression2() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        java.util.List<? extends String> l = null;\n" +
                       "        System.err.println(|l.get(0)|);\n" +
                       "    }\n" +
                       "}",
                       "package test; import java.util.List; public class Test { public static void test() { java.util.List<? extends String> l = null; System.err.println(name(l)); } private static String name(List<? extends String> l) { return l.get(0); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       4, 2);
    }

    public void testIntroduceMethodFromExpressionNewClassTree179766() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test() {\n" +
                       "        t(|new Runnable() {\n" +
                       "            public void run() {\n" +
                       "                throw new UnsupportedOperationException();\n" +
                       "            }\n" +
                       "        }|);\n" +
                       "    }\n" +
                       "    private static void t(Runnable r) {}\n" +
                       "}\n",
                       "package test; public class Test { public static void test() { t(name()); } private static void t(Runnable r) {} private static Runnable name() { return new Runnable() { public void run() { throw new UnsupportedOperationException(); } }; } } ",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true),
                       5, 3);
    }

//    public void testIntroduceMethodTooManyExceptions() throws Exception {
//        performFixTest("package test;\n" +
//                       "public class Test {\n" +
//                       "    public static void test(int a) throws Exception {\n" +
//                       "        |if (a == 1) throw new java.io.IOException(\"\");\n" +
//                       "        if (a == 2) throw new java.io.FileNotFoundException(\"\");|\n" +
//                       "    }\n" +
//                       "}",
//                       "package test; import java.io.IOException; public class Test { public static void test(int a) throws Exception { name(a); } private static void name(int a) throws IOException { if (a == 1) { throw new java.io.IOException(\"\"); } if (a == 2) { throw new java.io.FileNotFoundException(\"\"); } } }",
//                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true));
//    }

    public void testIntroduceMethodArray162163() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(char[] test) {\n" +
                       "        |test[0] = 'a';|\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test(char[] test) { name(test); } private static void name(char[] test) { test[0] = 'a'; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testIntroduceMethodReplaceDuplicatesNoRemap() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    private static int i;\n" +
                       "    public static void test1() {\n" +
                       "        |i++;|\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        i++;\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { private static int i; public static void test1() { name(); } public static void test2() { name(); } private static void name() { i++; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodReplaceDuplicatesSimpleRemap() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        int i = 0;\n" +
                       "        |i++;|\n" +
                       "        System.err.println(i);\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        int a = 0;\n" +
                       "        a++;\n" +
                       "        System.err.println(a);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test1() { int i = 0; i = name(i); System.err.println(i); } public static void test2() { int a = 0; a = name(a); System.err.println(a); } private static int name(int i) { i++; return i; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodReplaceDuplicatesSimpleRemapNotUseAfterMethod() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        int i = 0;\n" +
                       "        |System.err.println(i);|\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        int a = 0;\n" +
                       "        System.err.println(a);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test1() { int i = 0; name(i); } public static void test2() { int a = 0; name(a); } private static void name(int i) { System.err.println(i); } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodReplaceDuplicates194622() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        |System.err.println(1);|\n" +
                       "        System.err.println(1);\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        System.err.println(1);\n" +
                       "        System.err.println(1);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test1() { name(); name(); } public static void test2() { name(); name(); } private static void name() { System.err.println(1); } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodReplaceDuplicatesRemapExpression() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        int i = 0;\n" +
                       "        |i++;|\n" +
                       "        System.err.println(i);\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        int[] a = {0};\n" +
                       "        a[0]++;\n" +
                       "        System.err.println(a[0]);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test1() { int i = 0; i = name(i); System.err.println(i); } public static void test2() { int[] a = {0}; a[0] = name(a[0]); System.err.println(a[0]); } private static int name(int i) { i++; return i; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodReplaceDuplicatesRemapExpression179515a() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        int i = 0;\n" +
                       "        |i++;|\n" +
                       "        System.err.println(i);\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        int[] a = {0};\n" +
                       "        if (true) a[0]++;\n" +
                       "        System.err.println(a[0]);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test1() { int i = 0; i = name(i); System.err.println(i); } public static void test2() { int[] a = {0}; if (true) a[0] = name(a[0]); System.err.println(a[0]); } private static int name(int i) { i++; return i; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodReplaceDuplicatesRemapExpression179515b() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        int i = 0;\n" +
                       "        |i++; i++;|\n" +
                       "        System.err.println(i);\n" +
                       "    }\n" +
                       "    public static void test2(int ii) {\n" +
                       "        int[] a = {0};\n" +
                       "        switch (ii) {\n" +
                       "            case 0: \n" +
                       "                a[0]++;\n" +
                       "                a[0]++;\n" +
                       "                break;\n" +
                       "        }\n" +
                       "        System.err.println(a[0]);\n" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static void test1() { int i = 0; i = name(i); System.err.println(i); } public static void test2(int ii) { int[] a = {0}; switch (ii) { case 0: a[0] = name(a[0]); break; } System.err.println(a[0]); } private static int name(int i) { i++; i++; return i; } }",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodFromExpressionDuplicatesAndRemap() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(java.util.List<? extends String> l, java.util.List<? extends String> ll) {\n" +
                       "        System.err.println(|l.get(0)|);\n" +
                       "        System.err.println(ll.get(0));\n" +
                       "    }\n" +
                       "}",
                       "package test; import java.util.List; public class Test { public static void test(java.util.List<? extends String> l, java.util.List<? extends String> ll) { System.err.println(name(l)); System.err.println(name(ll)); } private static String name(List<? extends String> l) { return l.get(0); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true, true),
                       4, 2);
    }

    public void testIntroduceMethodReplaceDuplicates206193() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        |int i1 = 0;\n" +
                       "        int i2 = 2;\n" +
                       "        System.err.println(i1 + i2);|\n" +
                       "        System.err.println(i2);\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        int i1 = 0;\n" +
                       "        int i2 = 2;\n" +
                       "        System.err.println(i1 + i2);\n" +
                       "        System.err.println(i2);\n" +
                       "    }\n" +
                       "}",
                       "package test;\n" +
                       "public class Test {\n" +
                       "    public static void test1() {\n" +
                       "        int i2 = name();\n" +
                       "        System.err.println(i2);\n" +
                       "    }\n" +
                       "    public static void test2() {\n" +
                       "        int i2 = name();\n" +
                       "        System.err.println(i2);\n" +
                       "    }\n" +
                       "    private static int name() {\n" +
                       "        int i1 = 0;\n" +
                       "        int i2 = 2;\n" +
                       "        System.err.println(i1 + i2);\n" +
                       "        return i2;\n" +
                       "    }\n" +
                       "}",
                       new DialogDisplayerImpl3("name", EnumSet.of(Modifier.PRIVATE), true, true),
                       1, 0);
    }

    public void testIntroduceMethodFromSingleStatement153399a() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(java.util.List<? extends String> l, java.util.List<? extends String> ll) {\n" +
                       "        if (true) |System.err.println(l.get(0));|\n" +
                       "    }\n" +
                       "}",
                       "package test; import java.util.List; public class Test { public static void test(java.util.List<? extends String> l, java.util.List<? extends String> ll) { if (true) name(l); } private static void name(List<? extends String> l) { System.err.println(l.get(0)); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testIntroduceMethodFromSingleStatement153399b() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void test(java.util.List<? extends String> l, java.util.List<? extends String> ll) {\n" +
                       "        switch (ll.size()) {\n" +
                       "           case 0:\n" +
                       "              |System.err.println(l.get(0));\n" +
                       "              System.err.println(l.get(0));|\n" +
                       "              break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}",
                       "package test; import java.util.List; public class Test { public static void test(java.util.List<? extends String> l, java.util.List<? extends String> ll) { switch (ll.size()) { case 0: name(l); break; } } private static void name(List<? extends String> l) { System.err.println(l.get(0)); System.err.println(l.get(0)); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testIntroduceMethodNPE() throws Exception {
        performErrorMessageTest("package test; public class Test { |private static class F { public static void test(int y) {for ( ; ; y++) {if (y == 0) break; else y++;}}}| }",
                                IntroduceKind.CREATE_METHOD,
                                "ERR_Invalid_Selection");
    }

    public void testIntroduceMethodTypeParam183435a() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static <T extends Number> void test(T t) {\n" +
                       "        String allianceString = new String(\"[]\");\n" +
                       "        |allianceString += t.toString();|" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static <T extends Number> void test(T t) { String allianceString = new String(\"[]\"); name(allianceString, t); } private static <T extends Number> void name(String allianceString, T t) { allianceString += t.toString(); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethodTypeParam183435b() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static <T extends Number> void test(T t) {\n" +
                       "        String allianceString = new String(\"[]\");\n" +
                       "        String s = t.toString();" +
                       "        |allianceString += s;|" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static <T extends Number> void test(T t) { String allianceString = new String(\"[]\"); String s = t.toString(); name(allianceString, s); } private static void name(String allianceString, String s) { allianceString += s; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true));
    }

    public void testIntroduceMethodTypeParam183435c() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static <T extends Number> void test(T t) {\n" +
                       "        String allianceString = new String(\"[]\");\n" +
                       "        allianceString += |t.toString()|;" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static <T extends Number> void test(T t) { String allianceString = new String(\"[]\"); allianceString += name(t); } private static <T extends Number> String name(T t) { return t.toString(); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       4, 2);
    }

    public void testIntroduceMethodTypeParam183435d() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static <T extends Number> void test(T t) {\n" +
                       "        String allianceString = new String(\"[]\");\n" +
                       "        String s = t.toString();" +
                       "        allianceString += |s|;" +
                       "    }\n" +
                       "}",
                       "package test; public class Test { public static <T extends Number> void test(T t) { String allianceString = new String(\"[]\"); String s = t.toString(); allianceString += name(s); } private static String name(String s) { return s; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       4, 2);
    }

    public void test152705() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        ArrayList a = new ArrayList();\n" +
                       "        for (int i = 0; i < 2; i++) {\n" +
                       "            for (int j = 0; j < 2; j++) {\n" +
                       "            }\n" +
                       "            |ArrayList b = new ArrayList();\n" +
                       "            a.add(b);|\n" +
                       "        }\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList a = new ArrayList();\n" +
                        "        for (int i = 0; i < 2; i++) {\n" +
                        "            for (int j = 0; j < 2; j++) {\n" +
                        "            }\n" +
                        "            name(a);\n" +
                        "        }\n" +
                        "    }\n" +
                        "    private static void name(ArrayList a) {\n" +
                        "        ArrayList b = new ArrayList();\n" +
                        "        a.add(b);\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testLocalVariableToField1() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        |int i = 0;|\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    private static int i = 0;\n" +
                        "    public static void main(String[] args) {\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, false, EnumSet
                .of(Modifier.PRIVATE), false, true),
                       3, 1);
    }

    public void testLocalVariableToField201759a() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        |int i = 0;|\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    private static int nue;\n" +
                        "    public static void main(String[] args) {\n" +
                        "        nue = 0;\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("nue", IntroduceFieldPanel.INIT_METHOD, false, EnumSet
                .of(Modifier.PRIVATE), false, true),
                       3, 1);
    }

    public void testLocalVariableToField201759b() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public Test() {\n" +
                       "        |int i = 0;|\n" +
                       "    }\n" +
                       "    public Test(int i) {\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    private int nue;\n" +
                        "    public Test() {\n" +
                        "        nue = 0;\n" +
                        "    }\n" +
                        "    public Test(int i) {\n" +
                        "        nue = 0;\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("nue", IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .of(Modifier.PRIVATE), false, true),
                       3, 1);
    }

    public void testLocalVariableToField201759c() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public void main(String[] args) {\n" +
                       "        |int i = 0;|\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    private int nue;\n" +
                        "    public Test() {\n" +
                        "        nue = 0;\n" +
                        "    }\n" +
                        "    public void main(String[] args) {\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("nue", IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .of(Modifier.PRIVATE), false, true),
                       3, 1);
    }

    public void testLocalVariableToFieldInitMethod270296() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(int i) {\n" +
                       "        switch (i) {\n" +
                       "            case 0:\n" +
                       "                |String str = \"test\";|\n" +
                       "        }\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                       "public class Test {\n" +
                       "    private String str;\n" +
                       "    public void test(int i) {\n" +
                       "        switch (i) {\n" +
                       "            case 0:\n" +
                       "                str = \"test\";\n" +
                       "        }\n" +
                       "    }\n" +
                       "}").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_METHOD, false,
                                                EnumSet.of(Modifier.PRIVATE), false, true),
                       3, 1);
    }

    public void testLocalVariableToConstant1() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        |int i = 0;|\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    private static final int i = 0;\n" +
                        "    public static void main(String[] args) {\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl(null, false, null, true, EnumSet
                .of(Modifier.PRIVATE)),
                       3, 0);
    }

    public void test196683() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public void loop(int a) {\n" +
                       "        String s= \"\";\n" +
                       "        while(--a>0) {\n" +
                       "            |//6\n" +
                       "            if (a%3 != 0) {\n" +
                       "                s = s+\"--, \";\n" +
                       "                return;\n" +
                       "            }\n" +
                       "            //7\n" +
                       "            s = s+a+\", \";\n" +
                       "            return;\n" +
                       "            //8|\n" +
                       "        }\n" +
                       "        System.err.println(s);\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    public void loop(int a) {\n" +
                        "        String s= \"\";\n" +
                        "        while(--a>0) {\n" +
                        "            foo(a, s);\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        System.err.println(s);\n" +
                        "    }\n" +
                        "    private void foo(int a, String s) {\n" +
                        "        //6\n" +
                        "        if (a%3 != 0) {\n" +
                        "            s = s+\"--, \";\n" +
                        "            return;\n" +
                        "        }\n" +
                        "        //7\n" +
                        "        s = s+a+\", \";\n" +
                        "        return;\n" +
                        "        //8\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("foo", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void test193775() throws Exception {
        performCheckFixesTest("package test; import java.util.Collection; import java.util.Map.Entry; public class Test { public void test(|Collection<Entry> e|) {} }");
    }

    public void test203478() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "    public void loop() {\n" +
                       "        String someExampleHere = |\"someExampleHere\"|;\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "import java.util.ArrayList;\n" +
                        "public class Test {\n" +
                        "    private static final String SOME_EXAMPLE_HERE = \"someExampleHere\";\n" +
                        "    public void loop() {\n" +
                        "        String someExampleHere = SOME_EXAMPLE_HERE;\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl(null, false, null, true, EnumSet
                .<Modifier>of(Modifier.PRIVATE)),
                       5, 1);
    }

    public void testIntroduceFieldFix203621a() throws Exception {
        performFixTest("package test; public class Test {\n" +
                       "    public void test() {\n" +
                       "        |String.valueOf(1)|;\n" +
                       "    }\n" +
                       "}\n",
                       "package test; public class Test { private String valueOf = String.valueOf(1); public void test() { }  } ",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix203621b() throws Exception {
        performFixTest("package test; public class Test {\n" +
                       "    public Test() {\n" +
                       "        System.err.println(1);\n" +
                       "        |String.valueOf(1)|;\n" +
                       "        System.err.println(2);\n" +
                       "    }\n" +
                       "}\n",
                       "package test; public class Test { private String valueOf; public Test() { System.err.println(1); valueOf = String.valueOf(1); System.err.println(2); } } ",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_CONSTRUCTORS, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceFieldFix203621c() throws Exception {
        performFixTest("package test; public class Test {\n" +
                       "    public void test() {\n" +
                       "        System.err.println(1);\n" +
                       "        |String.valueOf(1)|;\n" +
                       "        System.err.println(2);\n" +
                       "    }\n" +
                       "}\n",
                       "package test; public class Test { private String valueOf; public void test() { System.err.println(1); valueOf = String.valueOf(1); System.err.println(2); } } ",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_METHOD, false, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testFieldLocation233440() throws Exception {
        Preferences prefs = CodeStylePreferences.get((FileObject) null, JavacParser.MIME_TYPE).getPreferences();
        prefs.put("classMemberInsertionPoint", InsertionPoint.CARET_LOCATION.name());
        IntroduceHint.INSERT_CLASS_MEMBER = new InsertClassMember();
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    String s = \"text\";\n" +
                       "    public void method() {\n" +
                       "        String local = |\"text\"|;\n" +
                       "    }\n" +
                       "}\n",
                       "package test; public class Test { private String text = \"text\"; String s = text; public void method() { String local = text; } } ",
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, true, EnumSet
                .<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testIntroduceConstantFix203621() throws Exception {
        performFixTest("package test; public class Test {\n" +
                       "    public void test() {\n" +
                       "        |String.valueOf(1)|;\n" +
                       "    }\n" +
                       "}\n",
                       "package test; public class Test { static final String VALUE_OF = String.valueOf(1); public void test() { }  } ",
                       new DialogDisplayerImpl(null, true, true, true, EnumSet
                .noneOf(Modifier.class)),
                       5, 1);
    }

    public void testConstant203499() throws Exception {
        performConstantAccessTest("package test; public class Test { static String g(String s) { return s; } static String d(String s) { return |g(s)|; } }", false);
    }

    public void testIntroduceMethod203254() throws Exception {
        performFixTest("package test;\n" +
                       "class Test {\n" +
                       "    public void test() {\n" +
                       "        final String s;\n" +
                       "        |s = \"a\";|\n" +
                       "    }\n" +
                       "}",
                       "package test; class Test { public void test() { final String s; name(); } private void name() { String s; s = \"a\"; } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testIntroduceMethod228913() throws Exception {
        sourceLevel = "1.8";
        performFixTest("package test;\n" +
                       "import java.io.StringReader;\n" +
                       "class Test {\n" +
                       "  void test() {\n" +
                       "    try (StringReader y=null) {\n" +
                       "      |if (y !=null) test();|\n" +
                       "    }\n" +
                       "  }\n" +
                       "}",
                       "package test; import java.io.StringReader; class Test { void test() { try (StringReader y=null) { name(y); } } private void name(" + /*XXX*/"final StringReader y) { if (y !=null) test(); } }",
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testIntroduceMethod203002() throws Exception {
        performFixTest("package test;\n" +
                       "class Test {\n" +
                       "    public void test() {\n" +
                       "        |double leftH, rightH;\n" +
                       "        leftH = Math.max(1, 1);|\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "class Test {\n" +
                        "    public void test() {\n" +
                        "        name();\n" +
                        "    }\n" +
                        "    private void name() {\n" +
                        "        double leftH, rightH;\n" +
                        "        leftH = Math.max(1, 1);\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testMethodUnprefixedParam() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                        "    public int test() {\n" +
                        "        int param = 1;\n" +
                        "        |return 2 * param;|\n" +
                        "    }\n" +
                        "}"
                       ,
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public int test() {\n" +
                        "        int param = 1;\n" +
                        "        return m(param);\n" +
                        "    }\n" +
                        "\n" +
                        "    private int m(int param) {\n" +
                        "        return 2 * param;\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("m", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testMethodParamWithLetterPrefix() throws Exception {
        codeStylePrefs.put(FmtOptions.parameterNamePrefix, "p");
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                        "    public int test() {\n" +
                        "        int param = 1;\n" +
                        "        |return 2 * param;|\n" +
                        "    }\n" +
                        "}"
                       ,
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public int test() {\n" +
                        "        int param = 1;\n" +
                        "        return m(param);\n" +
                        "    }\n" +
                        "\n" +
                        "    private int m(int pParam) {\n" +
                        "        return 2 * pParam;\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("m", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    /**
     * Checks various prefixing cases.
     * pY is a parameter and is already prefixed - prefix should not be added. z is not prefixed
     * and should be prefixed in introduced method. pX is prefixed, but is local variable, so the
     * potential prefix should be ignored
     * 
     */
    public void testMethodPrefixedAndUnprefixed() throws Exception {
        codeStylePrefs.put(FmtOptions.parameterNamePrefix, "p");
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                        "    public int test(int pY, int z) {\n" +
                        "        int param = 1;\n" +
                        "        int pX = 2;\n" +
                        "        |return 2 * param + pX - pY * z;|\n" +
                        "    }\n" +
                        "}"
                       ,
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public int test(int pY, int z) {\n" +
                        "        int param = 1;\n" +
                        "        int pX = 2;\n" +
                        "        return m(param, pX, pY, z);\n" +
                        "    }\n" +
                        "\n" +
                        "    private int m(int pParam, int pPX, int pY, int pZ) {\n" +
                        "        return 2 * pParam + pPX - pY * pZ;\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("m", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void test224512() throws Exception {
        performFixTest("package test;\n" +
                       "class Test {\n" +
                       "    private static class Foo {}\n" +
                       "    private static String[] t(String[] strings) {\n" +
                       "        String[] result = null;\n" +
                       "        if (strings != null) |{\n" +
                       "            for (String str : strings) {\n" +
                       "                result = new String[]{\n" +
                       "                    str,\n" +
                       "                    null\n" +
                       "                };\n" +
                       "                break;\n" +
                       "            }\n" +
                       "        }|\n" +
                       "        return result;\n" +
                       "    }\n" +
                       "}",
                       ("package test;\n" +
                        "class Test {\n" +
                        "    private static String[] name(String[] strings, String[] result) {\n" +
                        "        for (String str : strings) {\n" +
                        "            result = new String[]{\n" +
                        "                str,\n" +
                        "                null\n" +
                        "            };\n" +
                        "            break;\n" +
                        "        }\n" +
                        "        return result;\n" +
                        "    }\n" +
                        "    private static class Foo {}\n" +
                        "    private static String[] t(String[] strings) {\n" +
                        "        String[] result = null;\n" +
                        "        if (strings != null) result = name(strings, result);\n" +
                        "        return result;\n" +
                        "    }\n" +
                        "}")
                .replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", EnumSet
                .of(Modifier.PRIVATE), true),
                       1, 0);
    }

    public void testConstantFix204373a() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     private Object LOGGER;\n" +
                       "     class C {\n" +
                       "         public void foo() {\n" +
                       "             |Logger.getLogger(Test.class.getName())|.log(Level.FINEST, \"foo\");\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                        "    static final Logger LOGGER1 = Logger.getLogger(Test.class.getName());\n" +
                        "    private Object LOGGER;\n" +
                        "    class C {\n" +
                        "        public void foo() {\n" +
                        "            LOGGER1.log(Level.FINEST, \"foo\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl(null, true, true, true, EnumSet.noneOf(Modifier.class)),
                       5, 1);
    }

    public void testConstantFix204373b() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     class C {\n" +
                       "         public void foo() {\n" +
                       "             int i = |1 + 2*3|;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                        "     class C {\n" +
                        "         static final int NAME = 1 + 2*3;\n" +
                        "         public void foo() {\n" +
                        "             int i = NAME;\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl(null, true, true, true, EnumSet.noneOf(Modifier.class)),
                       5, 1);
    }

    public void testConstantFix204373c() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     public static void foo() {\n" +
                       "         int i = 1 + 2*3;\n" +
                       "     }\n" +
                       "     class C {\n" +
                       "         public void foo() {\n" +
                       "             int i = |1 + 2*3|;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                        "     static final int NAME = 1 + 2*3;\n" +
                        "     public static void foo() {\n" +
                        "         int i = NAME;\n" +
                        "     }\n" +
                        "     class C {\n" +
                        "         public void foo() {\n" +
                        "             int i = NAME;\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl(null, true, true, true, EnumSet.noneOf(Modifier.class)),
                       5, 1);
    }

    public void testConstantFix204373d() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     public void foo() {\n" +
                       "         int i = 1 + 2*3;\n" +
                       "     }\n" +
                       "     class C {\n" +
                       "         public void foo() {\n" +
                       "             int i = |1 + 2*3|;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                        "     private int name = 1 + 2*3;\n" +
                        "     public void foo() {\n" +
                        "         int i = name;\n" +
                        "     }\n" +
                        "     class C {\n" +
                        "         public void foo() {\n" +
                        "             int i = name;\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, true, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testConstantFix204373e() throws Exception {
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     public static void foo() {\n" +
                       "         int i = 1 + 2*3;\n" +
                       "     }\n" +
                       "     class C {\n" +
                       "         public void foo() {\n" +
                       "             int i = |1 + 2*3|;\n" +
                       "         }\n" +
                       "     }\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                        "     private static int name = 1 + 2*3;\n" +
                        "     public static void foo() {\n" +
                        "         int i = name;\n" +
                        "     }\n" +
                        "     class C {\n" +
                        "         public void foo() {\n" +
                        "             int i = name;\n" +
                        "         }\n" +
                        "     }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2(null, IntroduceFieldPanel.INIT_FIELD, true, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testConstantFix208072a() throws Exception {
        Preferences prefs = CodeStylePreferences.get((FileObject) null, JavacParser.MIME_TYPE).getPreferences();
        prefs.put("classMemberInsertionPoint", "LAST_IN_CATEGORY");
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     private static final int II = |1 + 2 * 3|;\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                       "     private static final int ZZ = 1 + 2 * 3;\n" +
                       "     private static final int II = ZZ;\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("ZZ", true, true, true, EnumSet.of(Modifier.PRIVATE)),
                       1, 0);
    }

    public void testConstantFix208072b() throws Exception {
        Preferences prefs = CodeStylePreferences.get((FileObject) null, JavacParser.MIME_TYPE).getPreferences();
        prefs.put("classMembersOrder", "STATIC_INIT;STATIC METHOD;INSTANCE_INIT;CONSTRUCTOR;METHOD;STATIC CLASS;CLASS;STATIC FIELD;FIELD");
        prefs.put("classMemberInsertionPoint", "LAST_IN_CATEGORY");
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     static {\n" +
                       "         II = |1 + 2 * 3|;\n" +
                       "     }\n" +
                       "     private static final int II;\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                       "     private static final int ZZ = 1 + 2 * 3;\n" +
                       "     static {\n" +
                       "         II = ZZ;\n" +
                       "     }\n" +
                       "     private static final int II;\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("ZZ", true, true, true, EnumSet.of(Modifier.PRIVATE)),
                       5, 1);
    }

    public void testConstantFix208072c() throws Exception {
        Preferences prefs = CodeStylePreferences.get((FileObject) null, JavacParser.MIME_TYPE).getPreferences();
        prefs.put("classMembersOrder", "STATIC_INIT;STATIC METHOD;INSTANCE_INIT;CONSTRUCTOR;METHOD;STATIC CLASS;CLASS;STATIC FIELD;FIELD");
        prefs.put("classMemberInsertionPoint", "LAST_IN_CATEGORY");
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     {\n" +
                       "         int ii = |1 + 2 * 3|;\n" +
                       "     }\n" +
                       "     private static final int II = 0;\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                       "     {\n" +
                       "         int ii = ZZ;\n" +
                       "     }\n" +
                       "     private static final int II = 0;\n" +
                       "     private static final int ZZ = 1 + 2 * 3;\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("ZZ", true, true, true, EnumSet.of(Modifier.PRIVATE)),
                       5, 1);
    }

    public void testConstantFix219771a() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method() {\n" +
                       "        System.out.println(\"C0 = \" + |C1 * 5|);\n" +
                       "    }\n" +
                       "    public static final int C1 = 100;\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void method() {\n" +
                        "        System.out.println(\"C0 = \" + C0);\n" +
                        "    }\n" +
                        "    public static final int C1 = 100;\n" +
                        "    public static final int C0 = C1 * 5;\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("C0", true, true, true, EnumSet.of(Modifier.PUBLIC)),
                       5, 1);
    }

    public void testConstantFix219771b() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method() {\n" +
                       "        System.out.println(\"C0 = \" + |C1 * 5|);\n" +
                       "    }\n" +
                       "    public final int C1 = 100;\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void method() {\n" +
                        "        System.out.println(\"C0 = \" + C0);\n" +
                        "    }\n" +
                        "    public final int C1 = 100;\n" +
                        "    public final int C0 = C1 * 5;\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("C0", IntroduceFieldPanel.INIT_FIELD, true, EnumSet.of(Modifier.PUBLIC), true, true),
                       4, 1);
    }

    public void testConstantFix219771c() throws Exception {
        Preferences prefs = CodeStylePreferences.get((FileObject) null, JavacParser.MIME_TYPE).getPreferences();
        prefs.put("classMembersOrder", "STATIC_INIT;FIELD;STATIC METHOD;INSTANCE_INIT;CONSTRUCTOR;METHOD;STATIC CLASS;CLASS;STATIC FIELD");
        prefs.put("classMemberInsertionPoint", "LAST_IN_CATEGORY");
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method() {\n" +
                       "        System.out.println(\"C0 = \" + |C1 * 5|);\n" +
                       "    }\n" +
                       "    public static final int C1 = 100;\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public final int C0 = C1 * 5;\n" +
                        "    public void method() {\n" +
                        "        System.out.println(\"C0 = \" + C0);\n" +
                        "    }\n" +
                        "    public static final int C1 = 100;\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("C0", IntroduceFieldPanel.INIT_FIELD, true, EnumSet.of(Modifier.PUBLIC), true, true),
                       5, 2);
    }
    
    /**
     * Checks that expressions that instantiate member classes cannot form constant
     * @throws Exception 
     */
    public void testConstantFix236187() throws Exception {
        performConstantAccessTest("package test;\n" +
                        "public class Test {\n" +
                        "  public void method() {\n" +
                        "     InnerClass ic = |new InnerClass()|;\n" +
                        "  }\n" +
                        "  class InnerClass{\n" +
                        "  }\n" +
                        "}", false);
    }

    public void testConstantFix236187Static() throws Exception {
        performFixTest("package test;\n" +
                        "public class Test {\n" +
                        "  public void method() {\n" +
                        "     InnerClass ic = |new InnerClass()|;\n" +
                        "  }\n" +
                        "  static class InnerClass{\n" +
                        "  }\n" +
                        "}",
                        (
                        "package test;\n" +
                        "public class Test {\n" +
                        "  private static final InnerClass ZZ = new InnerClass();\n" +
                        "  public void method() {\n" +
                        "     InnerClass ic = ZZ;\n" +
                        "  }\n" +
                        "  static class InnerClass{\n" +
                        "  }\n" +
                        "}"
                        ).replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("ZZ", IntroduceFieldPanel.INIT_FIELD, true, EnumSet.<Modifier>of(Modifier.PRIVATE), true, true),
                       5, 1);
    }

    public void testFieldFix208072d() throws Exception {
        Preferences prefs = CodeStylePreferences.get((FileObject) null, JavacParser.MIME_TYPE).getPreferences();
        prefs.put("classMembersOrder", "STATIC_INIT;STATIC METHOD;INSTANCE_INIT;CONSTRUCTOR;METHOD;STATIC CLASS;CLASS;STATIC FIELD;FIELD");
        prefs.put("classMemberInsertionPoint", "LAST_IN_CATEGORY");
        performFixTest("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "     static {\n" +
                       "         int ii = |1 + 2 * 3|;\n" +
                       "     }\n" +
                       "     private static final int II = 0;\n" +
                       "}\n",
                       ("package test;\n" +
                        "import java.util.logging.Level;\n" +
                        "import java.util.logging.Logger;\n" +
                        "public class Test {\n" +
                       "     private static int ZZ = 1 + 2 * 3;\n" +
                       "     static {\n" +
                       "         int ii = ZZ;\n" +
                       "     }\n" +
                       "     private static final int II = 0;\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("ZZ", IntroduceFieldPanel.INIT_FIELD, false, EnumSet.<Modifier>of(Modifier.PRIVATE), false, true),
                       5, 2);
    }

    public void testWhitespace216402() throws Exception {
        performSimpleSelectionVerificationTest("package test; public class Test {public void test() {int y = 3; y =|  2   |; }}", true);
    }

    public void testVariableNullTypeVariable221440() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method(String... args) {\n" +
                       "        args = |null|;\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void method(String... args) {\n" +
                        "        String[] name = null;\n" +
                        "        args = name;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("name", true, false, true),
                       5, 0);
    }

    public void testVariableNullTypeConstant221440() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method(String... args) {\n" +
                       "        args = |null|;\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private static final String[] name = null;\n" +
                        "    public void method(String... args) {\n" +
                        "        args = name;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl("name", true, null, true),
                       5, 1);
    }

    public void testVariableNullTypeField221440() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method(String... args) {\n" +
                       "        args = |null|;\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private final String[] name = null;\n" +
                        "    public void method(String... args) {\n" +
                        "        args = name;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl2("name", IntroduceFieldPanel.INIT_FIELD, true, null, true, true),
                       5, 2);
    }

    public void testVariableNullTypeMethod221440() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method(String... args) {\n" +
                       "        args = |null|;\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    private static String[] name() {\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "    public void method(String... args) {\n" +
                        "        args = name();\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       5, 3);
    }

    public void test213023() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public void method(String... args) |{\n" +
                       "    }|\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public void method(String... args) {\n" +
                        "        name();\n" +
                        "    }\n" +
                        "    private void name() {\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       1, 0);
    }

    public void testNullIntroduceMethod231050a() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        foo(|null|);\n" +
                       "    }\n" +
                       "    private static void foo(Object object) {}\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        foo(name());\n" +
                        "    }\n" +
                        "    private static void foo(Object object) {}\n" +
                        "    private static Object name() {\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       5, 3);
    }

    public void testNullIntroduceMethod231050b() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        foo(|null|);\n" +
                       "    }\n" +
                       "    private static void foo(String str) {}\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        foo(name());\n" +
                        "    }\n" +
                        "    private static void foo(String str) {}\n" +
                        "    private static String name() {\n" +
                        "        return null;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       5, 3);
    }

    public void testIntroduceMethodLastStatement224168a() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        System.err.println(1);\n" +
                       "        |if (args.length == 0)\n" +
                       "             return;|\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.err.println(1);\n" +
                        "        name(args);\n" +
                        "    }\n" +
                        "    private static void name(String[] args) {\n" +
                        "        if (args.length == 0)\n" +
                        "             return;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       1, 0);
    }

    public void testIntroduceMethodLastStatement224168b() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        System.err.println(1);\n" +
                       "        if (args != null) {\n" +
                       "            |if (args.length == 0)\n" +
                       "                 return;|\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.err.println(1);\n" +
                        "        if (args != null) {\n" +
                        "            name(args);\n" +
                        "        }\n" +
                        "    }\n" +
                        "    private static void name(String[] args) {\n" +
                        "        if (args.length == 0)\n" +
                        "             return;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       1, 0);
    }

    public void testIntroduceMethodLastStatement224168c() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        System.err.println(1);\n" +
                       "        if (args != null) {\n" +
                       "            |if (args.length == 0)\n" +
                       "                 return;|\n" +
                       "        }\n" +
                       "        System.err.println(1);\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.err.println(1);\n" +
                        "        if (args != null) {\n" +
                        "            if (name(args))\n" +
                        "                return;\n" +
                        "        }\n" +
                        "        System.err.println(1);\n" +
                        "    }\n" +
                        "    private static boolean name(String[] args) {\n" +
                        "        if (args.length == 0) {\n" +
                        "             return true;\n" +
                        "        }\n" +
                        "        return false;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       1, 0);
    }

    public void testIntroduceMethodReturn233433() throws Exception {
        performFixTest("package test;\n" +
                       "public class Test {\n" +
                       "    public static int main(String[] args) {\n" +
                       "        |return 1;|\n" +
                       "    }\n" +
                       "}\n",
                       ("package test;\n" +
                        "public class Test {\n" +
                        "    public static int main(String[] args) {\n" +
                        "        return name();\n" +
                        "    }\n" +
                        "    private static int name() {\n" +
                        "        return 1;\n" +
                        "    }\n" +
                        "}\n").replaceAll("[ \t\n]+", " "),
                       new DialogDisplayerImpl3("name", null, true),
                       1, 0);
    }
    
    protected void prepareTest(String code) throws Exception {
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

        SourceUtilsTestUtil
                .prepareTest(sourceRoot, buildRoot, cache);
        
        if (sourceLevel != null)
            SourceUtilsTestUtil.setSourceLevel(data, sourceLevel);

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
        assertTrue(info
                .getDiagnostics()
                .toString(), info
                .getDiagnostics()
                .isEmpty());
        
        if (getName().equals("testCommentVariable")) {
            info.getTreeUtilities().getComments(info.getCompilationUnit(), true);
        }
    }
    private CompilationInfo info;
    private Document doc;

    private void performSimpleSelectionVerificationTest(String code, boolean awaited) throws Exception {
        int[] span = new int[2];

        code = TestUtilities
                .detectOffsets(code, span);

        performSimpleSelectionVerificationTest(code, span[0], span[1], awaited);
    }

    private void performSimpleSelectionVerificationTest(String code, int start, int end, boolean awaited) throws Exception {
        prepareTest(code);

        assertEquals(awaited, IntroduceHint
                .validateSelection(info, start, end) != null);
    }

    private void performStatementSelectionVerificationTest(String code, int start, int end, boolean awaited, int[] awaitedSpan) throws Exception {
        prepareTest(code);

        int[] actualSpan = new int[2];

        assertEquals(awaited, IntroduceMethodFix
                .validateSelectionForIntroduceMethod(info, start, end, actualSpan) != null);

        if (awaited) {
            assertTrue(Arrays
                    .toString(actualSpan), Arrays
                    .equals(awaitedSpan, actualSpan));
        }
    }

    private void performConstantAccessTest(String code, boolean awaited) throws Exception {
        int[] span = new int[2];

        code = TestUtilities
                .detectOffsets(code, span);

        performConstantAccessTest(code, span[0], span[1], awaited);
    }

    private void performConstantAccessTest(String code, int start, int end, boolean awaited) throws Exception {
        prepareTest(code);

        assertEquals(awaited, IntroduceConstantFix
                .checkConstantExpression(info, IntroduceHint
                .validateSelection(info, start, end)));
    }

    private void performFixTest(String code, String golden, DialogDisplayer dd) throws Exception {
        int[] span = new int[2];

        code = TestUtilities
                .detectOffsets(code, span);

        performFixTest(code, span[0], span[1], golden, dd);
    }

    private void performFixTest(String code, int start, int end, String golden, DialogDisplayer dd) throws Exception {
        performFixTest(code, start, end, golden, dd, 1, 0);
    }

    private void performFixTest(String code, String golden, DialogDisplayer dd, int numFixes, int useFix) throws Exception {
        int[] span = new int[2];

        code = TestUtilities
                .detectOffsets(code, span);

        performFixTest(code, span[0], span[1], golden, dd, numFixes, useFix);
    }

    private void performFixTest(String code, int start, int end, String golden, DialogDisplayer dd, int numFixes, int useFix) throws Exception {
        SourceUtilsTestUtil
                .prepareTest(new String[0], new Object[]{dd});

        prepareTest(code);

        Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);
        List<ErrorDescription> errors = IntroduceHint
                .computeError(info, start, end, null, errorMessages, new AtomicBoolean());

        if (golden == null) {
            assertEquals(errors
                    .toString(), 0, errors
                    .size());
            return;
        }

        assertEquals(errorMessages
                .toString(), 1, errors
                .size());

        List<Fix> fixes = errors
                .get(0)
                .getFixes()
                .getFixes();

        assertEquals(fixes
                .toString(), numFixes, fixes
                .size());

        fixes
                .get(useFix)
                .implement();

        String result = doc
                .getText(0, doc
                .getLength())
                .replaceAll("[ \t\n]+", " ");
        golden = golden
                .replaceAll("[ \t\n]+", " ");

        assertEquals(golden, result);
    }

    private void performErrorMessageTest(String code, IntroduceKind kind, String golden) throws Exception {
        int[] span = new int[2];

        code = TestUtilities
                .detectOffsets(code, span);

        performErrorMessageTest(code, span[0], span[1], kind, golden);
    }

    private void performErrorMessageTest(String code, int start, int end, IntroduceKind kind, String golden) throws Exception {
        SourceUtilsTestUtil
                .prepareTest(new String[0], new Object[0]);

        prepareTest(code);

        Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);
        List<ErrorDescription> errors = IntroduceHint
                .computeError(info, start, end, null, errorMessages, new AtomicBoolean());

        assertEquals(errors
                .toString(), 0, errors
                .size());
        assertEquals(golden, errorMessages
                .get(kind));
    }

    private void performCheckFixesTest(String code, String... goldenFixes) throws Exception {
        int[] span = new int[2];

        code = TestUtilities
                .detectOffsets(code, span);

        performCheckFixesTest(code, span[0], span[1], goldenFixes);
    }

    private void performCheckFixesTest(String code, int start, int end, String... goldenFixes) throws Exception {
        SourceUtilsTestUtil
                .prepareTest(new String[0], new Object[0]);

        prepareTest(code);

        List<ErrorDescription> errors = IntroduceHint
                .computeError(info, start, end, null, new EnumMap<IntroduceKind, String>(IntroduceKind.class), new AtomicBoolean());

        if (goldenFixes.length == 0 && errors
                .isEmpty()) {
            //OK:
            return;
        }

        assertEquals(errors
                .toString(), 1, errors
                .size());

        List<Fix> fixes = errors
                .get(0)
                .getFixes()
                .getFixes();
        List<String> fixNames = new LinkedList<String>();

        for (Fix f : fixes) {
            fixNames
                    .add(f
                    .toString());
        }

        assertEquals(Arrays
                .asList(goldenFixes), fixNames);
    }

    private static class DialogDisplayerImpl extends DialogDisplayer {

        private String name;
        private Boolean replaceAll;
        private Boolean declareFinal;
        private Set<Modifier> modifiers;
        private boolean ok;

        public DialogDisplayerImpl(String name, Boolean replaceAll, Boolean declareFinal, boolean ok) {
            this(name, replaceAll, declareFinal, ok, EnumSet
                    .of(Modifier.PRIVATE));
        }

        /**
         * Uses introduce dialog
         *
         * @param name name of newly created name/constant
         * @param replaceAll replace all occurences?
         * @param declareFinal should be declared as final?
         * @param ok confirm the dialog?
         * @param modifiers list of modifiers
         */
        public DialogDisplayerImpl(String name, Boolean replaceAll, Boolean declareFinal, boolean ok, Set<Modifier> modifiers) {
            this.name = name;
            this.replaceAll = replaceAll;
            this.declareFinal = declareFinal;
            this.ok = ok;
            this.modifiers = modifiers;
        }

        public Object notify(NotifyDescriptor descriptor) {
            IntroduceFieldPanel panel = (IntroduceFieldPanel) descriptor
                    .getMessage();

            if (name != null) {
                panel
                        .setFieldName(name);
            }

            if (replaceAll != null) {
                panel
                        .setReplaceAll(replaceAll);
            }

            if (declareFinal != null) {
                panel
                        .setDeclareFinal(declareFinal);
            }

            if (modifiers != null) {
                panel
                        .setAccess(modifiers);
            }

            return ok ? descriptor
                    .getOptions()[0] : descriptor
                    .getOptions()[1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class DialogDisplayerImpl2 extends DialogDisplayer {

        private String fieldName;
        private Integer initializeIn;
        private Boolean replaceAll;
        private Set<Modifier> access;
        private Boolean declareFinal;
        private boolean ok;

        public DialogDisplayerImpl2(String fieldName, Integer initializeIn, Boolean replaceAll, Set<Modifier> access, Boolean declareFinal, boolean ok) {
            this.fieldName = fieldName;
            this.initializeIn = initializeIn;
            this.replaceAll = replaceAll;
            this.access = access;
            this.declareFinal = declareFinal;
            this.ok = ok;
        }

        public Object notify(NotifyDescriptor descriptor) {
            IntroduceFieldPanel panel = (IntroduceFieldPanel) descriptor
                    .getMessage();

            if (fieldName != null) {
                panel
                        .setFieldName(fieldName);
            }

            if (initializeIn != null) {
                panel
                        .setInitializeIn(initializeIn);
            }

            if (replaceAll != null) {
                panel
                        .setReplaceAll(replaceAll);
            }

            if (access != null) {
                panel
                        .setAccess(access);
            }

            if (declareFinal != null) {
                panel
                        .setDeclareFinal(declareFinal);
            }

            return ok ? descriptor
                    .getOptions()[0] : descriptor
                    .getOptions()[1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class DialogDisplayerImpl3 extends DialogDisplayer {

        private String methodName;
        private Set<Modifier> access;
        private boolean ok;
        private final boolean replaceDuplicates;

        public DialogDisplayerImpl3(String methodName, Set<Modifier> access, boolean ok) {
            this(methodName, access, ok, false);
        }

        public DialogDisplayerImpl3(String methodName, Set<Modifier> access, boolean ok, boolean replaceDuplicates) {
            this.methodName = methodName;
            this.access = access;
            this.ok = ok;
            this.replaceDuplicates = replaceDuplicates;
        }

        public Object notify(NotifyDescriptor descriptor) {
            if (descriptor
                    .getMessage() instanceof String) {
                //check that this is the "do replace" dialog
                return NotifyDescriptor.YES_OPTION;
            }

            IntroduceMethodPanel panel = (IntroduceMethodPanel) descriptor
                    .getMessage();

            if (methodName != null) {
                panel
                        .setMethodName(methodName);
            }

            if (access != null) {
                panel
                        .setAccess(access);
            }

            panel
                    .setReplaceOther(replaceDuplicates);

            return ok ? descriptor
                    .getOptions()[0] : descriptor
                    .getOptions()[1];
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
    
    @ServiceProvider(service=Provider.class)
    public static final class PreferencesProvider implements Provider {
        @Override public Preferences forFile(FileObject file, String mimeType) {
            return codeStylePrefs;
        }
        @Override public Preferences forDocument(Document doc, String mimeType) {
            return codeStylePrefs;
        }
    }

}
