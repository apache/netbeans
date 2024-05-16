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
package org.netbeans.api.java.source;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TreeUtilitiesTest extends NbTestCase {

    private String sourceLevel;

    public TreeUtilitiesTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }
    
    private CompilationInfo info;
    
    private void prepareTest(String filename, String code) throws Exception {
        prepareTest(filename, code, Phase.RESOLVED);
    }

    private void prepareTest(String filename, String code, Phase resolutionPhase) throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.createFolder("test");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);
        
        FileObject testSource = packageRoot.createData(filename + ".java");
        
        assertNotNull(testSource);
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);
        
        SourceUtilsTestUtil.setSourceLevel(testSource, sourceLevel);

        JavaSource js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, resolutionPhase);
        
        assertNotNull(info);
    }

    public void testIsSynthetic1() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        BlockTree bt = (BlockTree) tp.getLeaf();
        
        tp = new TreePath(tp, bt.getStatements().get(0));
        
        assertTrue(info.getTreeUtilities().isSynthetic(tp));
    }
    
    public void testIsSynthetic2() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){super();}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        BlockTree bt = (BlockTree) tp.getLeaf();
        
        tp = new TreePath(tp, bt.getStatements().get(0));
        
        assertFalse(info.getTreeUtilities().isSynthetic(tp));
    }

    public void testIsSyntheticNewClassExtends() throws Exception {
        prepareTest("Test", "package test; import java.util.*; public class Test { void t() { new ArrayList<String>() { private int i; }; } }");

        TreePath tp = info.getTreeUtilities().pathFor(67);
        NewClassTree nct = (NewClassTree) tp.getLeaf();

        assertTrue(info.getTreeUtilities().isSynthetic(new TreePath(new TreePath(tp, nct.getClassBody()), nct.getClassBody().getExtendsClause())));
        assertFalse(info.getTreeUtilities().isSynthetic(new TreePath(new TreePath(tp, nct.getClassBody()), nct.getClassBody().getMembers().get(1))));
        assertFalse(info.getTreeUtilities().isSynthetic(new TreePath(tp, nct.getIdentifier())));
    }

    public void testIsSyntheticNewClassImplements() throws Exception {
        prepareTest("Test", "package test; import java.io.*; public class Test { void t() { new Serializable() { }; } }");

        TreePath tp = info.getTreeUtilities().pathFor(65);
        NewClassTree nct = (NewClassTree) tp.getLeaf();
        TreePath toTest = new TreePath(new TreePath(tp, nct.getClassBody()), nct.getClassBody().getImplementsClause().get(0));

        assertTrue(info.getTreeUtilities().isSynthetic(toTest));
    }

    public void testFindNameSpan1() throws Exception {
        prepareTest("Test", "package test; public class Test {}");
        
        TreePath tp = info.getTreeUtilities().pathFor(29);
        ClassTree ct = (ClassTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {27, 31}));
    }

    public void testFindNameSpanEnum() throws Exception {
        prepareTest("Test", "package test; public enum Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {56 - 30, 60 - 30}));
    }

    public void testFindNameSpanInterface() throws Exception {
        prepareTest("Test", "package test; public interface Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {61 - 30, 65 - 30}));
    }

    public void testFindNameSpanAnnotationType() throws Exception {
        prepareTest("Test", "package test; public @interface Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {62 - 30, 66 - 30}));
    }
    
    public void testFindNameSpan2() throws Exception {
        prepareTest("Test", "package test; public /*dsfasfd*/   class   /*laksdjflk*/   /**asdff*/ //    \n Test {}");
        
        TreePath tp = info.getTreeUtilities().pathFor(109 - 30);
        ClassTree ct = (ClassTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {108 - 30, 112 - 30}));
    }
    
    public void testFindNameSpan3() throws Exception {
        prepareTest("Test", "package test; public class   {}");
        
        TreePath tp = info.getTreeUtilities().pathFor(54 - 30);
        ClassTree ct = (ClassTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertEquals(null, span);
    }
    
    public void testFindNameSpan4() throws Exception {
        prepareTest("Test", "package test; public class Test {public void test() {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan5() throws Exception {
        prepareTest("Test", "package test; public class Test {private int test;}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        VariableTree ct = (VariableTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan6() throws Exception {
        prepareTest("Test", "package test; public class Test {public void test()[] {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan7() throws Exception {
        prepareTest("Test", "package test; public class Test {private int test[];}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        VariableTree ct = (VariableTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {75 - 30, 79 - 30}));
    }
    
    public void testFindNameSpan8() throws Exception {
        prepareTest("Test", "package test; public class Test {private test.Test t;}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MemberSelectTree ct = (MemberSelectTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {76 - 30, 80 - 30}));
    }
    
    public void testFindNameSpan9() throws Exception {
        prepareTest("Test", "package test; public class Test {private test. /*adsTestsldf*/ //\n /**aTestklajdf*/ Test t;}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MemberSelectTree ct = (MemberSelectTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {114 - 30, 118 - 30}));
    }
    
    public void testFindNameSpan10() throws Exception {
        prepareTest("Test", "package test; public class Test {public void /*test*/test()[] {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {83 - 30, 87 - 30}));
    }
    
    public void testFindNameSpan11() throws Exception {
        prepareTest("Test", "package test; public class Test {private int /*test*/test[];}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        VariableTree ct = (VariableTree) tp.getParentPath().getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {83 - 30, 87 - 30}));
    }
    
    public void testFindNameSpanUnicode() throws Exception {
        String code = "package test; public class Test {private int test\\u0061;}";
        prepareTest("Test", code);
        
        TreePath tp = info.getTreeUtilities().pathFor(71 - 24);
        VariableTree ct = (VariableTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {69 - 24, 79 - 24}));
    }
    
    public void testFindNameSpanConstructor() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(70 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findNameSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {70 - 30, 74 - 30}));
    }
    
    public void testFindNameSpanConstructor2() throws Exception {
        prepareTest("Test", "package test; public class Test {}");
        
        ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        MethodTree mt = (MethodTree) ct.getMembers().get(0); // synthetic constructor
        
        int[] span = info.getTreeUtilities().findNameSpan(mt);
        
        assertNull(span);
    }
    
    public void testFindClassBodySpan1() throws Exception {
        prepareTest("Test", "package test; public class Test {public void test() {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(59 - 30);
        ClassTree ct = (ClassTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findBodySpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {62 - 30, 85 - 30}));
    }
    
    public void testFindMethodParameterSpan1() throws Exception {
        prepareTest("Test", "package test; public class Test {public void test() {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findMethodParameterSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {79 - 30, 80 - 30}));
    }
    
    public void testFindMethodParameterSpan2() throws Exception {
        prepareTest("Test", "package test; public class Test {public void test(String name) {}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(77 - 30);
        MethodTree ct = (MethodTree) tp.getLeaf();
        
        int[] span = info.getTreeUtilities().findMethodParameterSpan(ct);
        
        assertTrue(Arrays.toString(span), Arrays.equals(span, new int[] {79 - 30, 91 - 30}));
    }
    
    public void testTreePath124760a() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(int iii[]){}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(50);
        
        assertEquals(Kind.VARIABLE, tp.getLeaf().getKind());
        //#125856:
        assertFalse(Kind.VARIABLE == tp.getParentPath().getLeaf().getKind());
    }
    
    public void testTreePath124760b() throws Exception {
        prepareTest("Test", "package test; public class Test {public int test()[]{}}");
        
        TreePath tp = info.getTreeUtilities().pathFor(47);
        
        assertEquals(Kind.METHOD, tp.getLeaf().getKind());
        //#125856:
        assertFalse(Kind.METHOD == tp.getParentPath().getLeaf().getKind());
    }
    
    public void testAnnotationSyntheticValue1() throws Exception {
        prepareTest("Test", "package test; @Meta(Test.VALUE) public class Test { public static final String VALUE = \"\"; } @interface Meta { public String value(); }");
        
        TreePath tp = info.getTreeUtilities().pathFor(58 - 30);
        
        assertEquals(Kind.MEMBER_SELECT, tp.getLeaf().getKind());
        assertEquals("Test.VALUE", tp.getLeaf().toString());
    }
    
    public void testAnnotationSyntheticValue2() throws Exception {
        prepareTest("Test", "package test; @Meta(Test.VALUE) public class Test { public static final String VALUE = \"\"; } @interface Meta { public String[] value(); }");
        
        TreePath tp = info.getTreeUtilities().pathFor(58 - 30);
        
        assertEquals(Kind.MEMBER_SELECT, tp.getLeaf().getKind());
        assertEquals("Test.VALUE", tp.getLeaf().toString());
    }
    
    public void testAnnotationSyntheticValue3() throws Exception {
        prepareTest("Test", "package test; @Meta({Test.VALUE}) public class Test { public static final String VALUE = \"\"; } @interface Meta { public String[] value(); }");
        
        TreePath tp = info.getTreeUtilities().pathFor(58 - 30);
        
        assertEquals(Kind.MEMBER_SELECT, tp.getLeaf().getKind());
        assertEquals("Test.VALUE", tp.getLeaf().toString());
    }

    public void testAnnotationSyntheticValue4() throws Exception {
        prepareTest("Test", "package test; @Meta(String.class) public class Test { } @interface Meta { public Class value(); }");

        TreePath tp = info.getTreeUtilities().pathFor(24);

        assertEquals(Kind.IDENTIFIER, tp.getLeaf().getKind());
        assertEquals("String", tp.getLeaf().toString());
    }
    
    public void testAutoMapComments1() throws Exception {
        prepareTest("Test", "package test;\n" +
                            "import java.io.File;\n" +
                            "\n" +
                            "/*test1*/\n" +
                            "public class Test {\n" +
                            "\n" +
                            "    //test2\n" +
                            "    void method() {\n" +
                            "        // Test\n" +
                            "        int a = 0;\n" +
                            "    }\n" +
                            "\n" +
                            "}\n");
        
        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        List<Comment> clazzComments = info.getTreeUtilities().getComments(clazz, true);
        
        assertEquals(1, clazzComments.size());
        
        assertEquals(Style.BLOCK, clazzComments.get(0).style());
        
        assertEquals("/*test1*/", clazzComments.get(0).getText());
        
        List<Comment> clazzComments2 = info.getTreeUtilities().getComments(clazz, true);
        
        assertEquals(1, clazzComments2.size());
        
        assertTrue(clazzComments.get(0) == clazzComments2.get(0));
    }
    
    public void testAutoMapComments2() throws Exception {
        prepareTest("Test", "package test;\n" +
                            "import java.io.File;\n" +
                            "\n" +
                            "/*test1*/\n" +
                            "public class Test {\n" +
                            "\n" +
                            "    //test2\n" +
                            "    void method() {\n" +
                            "        // Test\n" +
                            "        int a = 0;\n" +
                            "    }\n" +
                            "\n" +
                            "}\n");
        
        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        MethodTree method = (MethodTree) clazz.getMembers().get(1);
        List<Comment> methodComments = info.getTreeUtilities().getComments(method, true);
        
        assertEquals(1, methodComments.size());
        
        assertEquals(Style.LINE, methodComments.get(0).style());
        
        assertEquals("//test2\n", methodComments.get(0).getText());
        
        List<Comment> methodComments2 = info.getTreeUtilities().getComments(method, true);
        
        assertEquals(1, methodComments2.size());
        
        assertTrue(methodComments.get(0) == methodComments2.get(0));
    }

    public void testIsEnumConstant() throws Exception {
        prepareTest("Test", "package test; public enum Test {B; private static final int ii = 0;}");
        ClassTree clazz = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);
        Tree b = clazz.getMembers().get(1);
        assertTrue(info.getTreeUtilities().isEnumConstant((VariableTree) b));
        Tree ii = clazz.getMembers().get(2);
        assertFalse(info.getTreeUtilities().isEnumConstant((VariableTree) ii));
    }

    public void testUncaughtExceptionHandler() throws Exception {
        String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public static void test() {\n" +
                      "        t(ne|w Runnable() {\n" +
                      "            public void run() {\n" +
                      "                throw new UnsupportedOperationException();\n" +
                      "            }\n" +
                      "        });\n"+
                      "    }\n" +
                      "    private static void t(Runnable r) {}\n" +
                      "}\n";
        int pos = code.indexOf('|');

        prepareTest("Test", code.replaceAll(Pattern.quote("|"), ""));

        TreePath tp = info.getTreeUtilities().pathFor(pos);
        Set<TypeMirror> uncaughtExceptions = info.getTreeUtilities().getUncaughtExceptions(tp);
        Set<String> uncaughtExceptionStrings = new HashSet<String>();

        for (TypeMirror tm : uncaughtExceptions) {
            uncaughtExceptionStrings.add(tm.toString());
        }

        Set<String> golden = new HashSet<String>();

        assertEquals(golden, uncaughtExceptionStrings);
    }

    public void testExoticIdentifiers() throws Exception {
        performExoticIdentiferDecodeTest("#\"a\"", "a");
        for (char c : TreeUtilities.EXOTIC_ESCAPE) {
            performExoticIdentiferDecodeTest("#\"\\" + c + "\"", "\\" + c);
        }
        performExoticIdentiferDecodeTest("#\"\\n\"", "\n");

        performExoticIdentiferDecodeTest("a", "a");
        performExoticIdentiferEncodeTest("\n", "#\"\\n\"");
    }

    private void performExoticIdentiferDecodeTest(String exotic, String golden) throws Exception {
        assertEquals(golden, TreeUtilities.decodeIdentifierInternal(exotic).toString());
    }
    
    private void performExoticIdentiferEncodeTest(String exotic, String golden) throws Exception {
        assertEquals(golden, TreeUtilities.encodeIdentifierInternal(exotic).toString());
    }

    public void testNPEFromIsStatic() throws Exception {
        prepareTest("Test", "package test; public enum Test {A; private void m() { String s = \"a\"; } }");

        TreePath tp = info.getTreeUtilities().pathFor(97 - 30);

        assertEquals(Kind.STRING_LITERAL, tp.getLeaf().getKind());

        Scope s = info.getTrees().getScope(tp);

        assertFalse(info.getTreeUtilities().isStaticContext(s));
    }

    public void testIsCompileTimeConstant() throws Exception {
        prepareTest("Test", "package test; public class Test { private void m(String str) { int i; i = 1 + 1; i = str.length(); } }");

        final List<Boolean> result = new ArrayList<Boolean>();

        new ErrorAwareTreePathScanner<Void, Void>() {
            @Override public Void visitAssignment(AssignmentTree node, Void p) {
                result.add(info.getTreeUtilities().isCompileTimeConstantExpression(new TreePath(getCurrentPath(), node.getExpression())));
                return super.visitAssignment(node, p);
            }
        }.scan(info.getCompilationUnit(), null);

        assertEquals(Arrays.asList(true, false), result);
    }
    
    public void testJavacInitializationParse() throws Exception {
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY));
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.getTreeUtilities().parseExpression("1 + 1", new SourcePositions[1]);
            }
        }, true);
    }
    
    public void testStaticBlock() throws Exception {
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY));
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                final SourcePositions[] sp = new SourcePositions[1];
                BlockTree block = parameter.getTreeUtilities().parseStaticBlock("static { }", sp);
                assertNotNull(block);
                assertEquals(0, sp[0].getStartPosition(null, block));
                assertEquals(10, sp[0].getEndPosition(null, block));
                assertNull(parameter.getTreeUtilities().parseStaticBlock("static", new SourcePositions[1]));
            }
        }, true);
    }
    
    public void testJavacInitializationElements() throws Exception {
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY));
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                assertNotNull(parameter.getElements());
            }
        }, true);
    }
    
    public void testJavacInitializationTypes() throws Exception {
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY));
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                assertNotNull(parameter.getTypes());
            }
        }, true);
    }
    
    public void testJavacInitializationTrees() throws Exception {
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY));
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                assertNotNull(parameter.getTrees());
            }
        }, true);
    }

    public void testDisableAccessRightsCrash() throws Exception {
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        FileObject testFile = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Test.java");
        try (Writer w = new OutputStreamWriter(testFile.getOutputStream())) {
            w.append("public class Test {}");
        }
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY), testFile);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                TreePath clazzPath = new TreePath(new TreePath(parameter.getCompilationUnit()),
                                                  parameter.getCompilationUnit().getTypeDecls().get(0));
                Scope scope = parameter.getTrees().getScope(clazzPath);
                Scope disableScope = parameter.getTreeUtilities().toScopeWithDisabledAccessibilityChecks(scope);
                ExpressionTree et = parameter.getTreeUtilities().parseExpression("1 + 1", new SourcePositions[1]);
                parameter.getTreeUtilities().attributeTree(et, disableScope);
            }
        }, true);
    }

    public void testAttributingVar() throws Exception {
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        FileObject testFile = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Test.java");
        try (Writer w = new OutputStreamWriter(testFile.getOutputStream())) {
            w.append("public class Test { private static int I; }");
        }
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY), testFile);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                TreePath clazzPath = new TreePath(new TreePath(new TreePath(parameter.getCompilationUnit()),
                                                  parameter.getCompilationUnit().getTypeDecls().get(0)),
                        ((ClassTree) parameter.getCompilationUnit().getTypeDecls().get(0)).getMembers().get(1));
                Scope scope = parameter.getTrees().getScope(clazzPath);
                StatementTree st = parameter.getTreeUtilities().parseStatement("{ String s; }", new SourcePositions[1]);
                assertEquals(Kind.BLOCK, st.getKind());
                StatementTree var = st.getKind() == Kind.BLOCK ? ((BlockTree) st).getStatements().get(0) : st;
                parameter.getTreeUtilities().attributeTree(st, scope);
                checkType(parameter, clazzPath, var);
            }
        }, true);
    }

    private void checkType(CompilationInfo info, TreePath base, StatementTree st) {
        TreePath tp = new TreePath(base, st);
        Element el = info.getTrees().getElement(tp);

        assertNotNull(el);
        assertEquals("s", el.toString());
    }

    public void testNotUsingLiveScope() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_12");
        } catch (IllegalArgumentException ex) {
            //this test cannot pass on JDK <12, as javac there does not allow variables to own other variables
            return ;
        }
        ClassPath boot = ClassPathSupport.createClassPath(SourceUtilsTestUtil.getBootClassPath().toArray(new URL[0]));
        FileObject testFile = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Test.java");
        try (Writer w = new OutputStreamWriter(testFile.getOutputStream())) {
            w.append("import java.lang.String;\n" +
                     "public class Test {\n" +
                     "    void test(boolean b) {\n" +
                     "        int i1;\n" +
                     "        int i2;\n" +
                     "        int i3;\n" +
                     "        int i4;\n" +
                     "        int i5;\n" +
                     "        int i6;\n" +
                     "        int scopeHere = 0;\n" +
                     "    }\n" +
                     "}\n");
        }
        JavaSource js = JavaSource.create(ClasspathInfo.create(boot, ClassPath.EMPTY, ClassPath.EMPTY), testFile);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                TreePath[] path = new TreePath[1];
                new TreePathScanner<Void, Void>() {
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        if (node.getName().contentEquals("scopeHere")) {
                            path[0] = new TreePath(getCurrentPath(), node.getInitializer());
                        }
                        return super.visitVariable(node, p);
                    }
                }.scan(parameter.getCompilationUnit(), null);
                Scope scope = parameter.getTrees().getScope(path[0]);
                StatementTree st = parameter.getTreeUtilities().parseStatement("{ String t; }", new SourcePositions[1]);
                assertEquals(Kind.BLOCK, st.getKind());
                parameter.getTreeUtilities().attributeTree(st, scope);
            }
        }, true);
    }

    public void testIsEndOfCompoundVariableDeclaration() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){int i = 10, j = 11;}}");
        TreePath tp = info.getTreeUtilities().pathFor(47);
        BlockTree bt = (BlockTree) tp.getLeaf();
        assertFalse(info.getTreeUtilities().isEndOfCompoundVariableDeclaration(bt.getStatements().get(1)));
        assertTrue(info.getTreeUtilities().isEndOfCompoundVariableDeclaration(bt.getStatements().get(2)));
    }

    public void testIsPartOfCompoundVariableDeclaration() throws Exception {
        prepareTest("Test", "package test; public class Test {public Test(){int i = 10, j = 11; int k = 1;}}");

        //int i = 10
        VariableTree var1 = (VariableTree) info.getTreeUtilities().pathFor(55).getLeaf();
        assertTrue(info.getTreeUtilities().isPartOfCompoundVariableDeclaration(var1));

        //int j = 11
        VariableTree var2 = (VariableTree) info.getTreeUtilities().pathFor(60).getLeaf();
        assertTrue(info.getTreeUtilities().isPartOfCompoundVariableDeclaration(var2));

        //int k = 1
        VariableTree var3 = (VariableTree) info.getTreeUtilities().pathFor(71).getLeaf();
        assertFalse(info.getTreeUtilities().isPartOfCompoundVariableDeclaration(var3));

    }

    public void testForEachLoop() throws Exception {
        prepareTest("Test", "package test; public class Test { public Test(java.util.List<String> ll) { for (String s : ll.subList(0, ll.size())  ) { } } }");

        TreePath tp1 = info.getTreeUtilities().pathFor(122 - 30);
        assertEquals(Kind.IDENTIFIER, tp1.getLeaf().getKind());
        assertEquals("ll", ((IdentifierTree) tp1.getLeaf()).getName().toString());

        TreePath tp2 = info.getTreeUtilities().pathFor(127 - 30);
        assertEquals(Kind.MEMBER_SELECT, tp2.getLeaf().getKind());
        assertEquals("subList", ((MemberSelectTree) tp2.getLeaf()).getIdentifier().toString());

        TreePath tp3 = info.getTreeUtilities().pathFor(140 - 30);
        assertEquals(Kind.MEMBER_SELECT, tp3.getLeaf().getKind());
        assertEquals("size", ((MemberSelectTree) tp3.getLeaf()).getIdentifier().toString());

        TreePath tp4 = info.getTreeUtilities().pathFor(146 - 30);
        assertEquals(Kind.METHOD_INVOCATION, tp4.getLeaf().getKind());
        assertEquals("subList", ((MemberSelectTree) ((MethodInvocationTree) tp4.getLeaf()).getMethodSelect()).getIdentifier().toString());
    }

    public void testAttributeTreesInnerClasses() throws Exception {
        String code = "package test; public class Test { public void test1() { new Object() {}; new Object() {}; } public void test2() { new Object() {}; new Object() {}; } }";
        prepareTest("Test", code, Phase.ELEMENTS_RESOLVED);

        List<String> expectedNames = Arrays.asList("test.Test",
                                                   "test.Test$1",
                                                   "test.Test$2",
                                                   "test.Test$3",
                                                   "test.Test$4");

        for (int i = 0; i < 2; i++) {
            TreePath tp = info.getTreeUtilities().pathFor(code.indexOf("{}"));
            Scope scope = info.getTrees().getScope(tp);
            StatementTree parsed = info.getTreeUtilities().parseStatement("{ new Object() {}; new Object() {}; }", new SourcePositions[1]);
            info.getTreeUtilities().attributeTree(parsed, scope);
            info.impl.toPhase(Phase.RESOLVED);
            List<String> actualNames = new ArrayList<>();
            new TreePathScanner<Void, Void>() {
                @Override
                public Void visitClass(ClassTree node, Void p) {
                    actualNames.add(info.getElements().getBinaryName((TypeElement) info.getTrees().getElement(getCurrentPath())).toString());
                    return super.visitClass(node, p);
                }
            }.scan(info.getCompilationUnit(), null);
            assertEquals(expectedNames, actualNames);
        }
    }

    public void testPathForInUnnamedClass() throws Exception {
        this.sourceLevel = "21";

        String code = "void main() {\n" +
                      "    Sys|tem.err.println();\n" +
                      "}\n";

        prepareTest("Test", code.replace("|", ""));

        int pos = code.indexOf("|");
        TreePath tp = info.getTreeUtilities().pathFor(pos);
        IdentifierTree it = (IdentifierTree) tp.getLeaf();

        assertEquals("System", it.getName().toString());
    }
}
