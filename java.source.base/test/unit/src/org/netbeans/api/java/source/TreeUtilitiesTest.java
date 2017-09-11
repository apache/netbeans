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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.api.java.source;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.Comment.Style;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class TreeUtilitiesTest extends NbTestCase {
    
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
        
        JavaSource js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);
        
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

        new TreePathScanner<Void, Void>() {
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
}
