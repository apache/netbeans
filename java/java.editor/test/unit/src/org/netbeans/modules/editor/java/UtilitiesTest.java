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

package org.netbeans.modules.editor.java;

import java.util.ArrayList;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class UtilitiesTest extends NbTestCase {
    
    public UtilitiesTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        super.setUp();
    }

    public void testFuzzyResolveMethod1() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       Object o = null;" +
                "       t(o);" +
                "   }" +
                "   private void t(String s) {}" +
                "}", "t(java.lang.String)");
    }
    
    public void testFuzzyResolveMethod2() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       Object o = null;" +
                "       t(o);" +
                "   }" +
                "   private static void t(String s) {}" +
                "}", "t(java.lang.String)");
    }
    
    public void testFuzzyResolveMethod3() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       test(\"\", x);\n" +
                "   }" +
                "   private static void test(String s, int i) {}\n" +
                "   private static void test(Object o, int i) {}\n" +
                "}", "test(java.lang.String,int)");
    }
    
    public void testFuzzyResolveMethod124901() throws Exception {
        performTest("package test;" +
                "public class Test<K> {" +
                "   private K read() {return null;}\n" +
                "   private void test() {" +
                "       read().read();\n" +
                "   }" +
                "}", "<not resolved>", "<not resolved>");
    }
    
    public void testFuzzyResolveConstructor1() throws Exception {
        performTest("package test;" +
                "public class Test {" +
                "   private void test() {" +
                "       Object o = null;" +
                "       new Test(o);" +
                "   }" +
                "   private Test(String s) {}" +
                "}", "Test(java.lang.String)");
    }
    
    public void testFuzzyResolveConstructor2() throws Exception {
        performTest("package test;" +
                "import java.awt.Font;\n" +
                "public class Test {" +
                "   private void test() {" +
                "       new Font(getSetting(\"fontName\"), Font.BOLD, 12);" +
                "   }\n" +
                "   public Object getSetting(String s) {return s;}\n" +
                "}", "Font(java.lang.String,int,int)", "<not resolved>");
    }
    
    public void testFuzzyResolve132627() throws Exception {
        performTest("package test;\n" +
                    "public class Main extends A {\n" +
                    "    public void test() {\n" +
                    "        getClass();\n" +
                    "    }\n" +
                    "}\n" +
                    "class A extends dddd implements Runnable {}\n",
                    "<not resolved>");
    }
    
    public void testFuzzyResolve203476a() throws Exception {
        performTest("package test;\n" +
                    "import static test.Aux.getName;\n" +
                    "public class Main {\n" +
                    "    public void test() {\n" +
                    "        getName(undef);\n" +
                    "    }\n" +
                    "}\n" +
                    "class Aux {\n" +
                    "    public static void getName(String str) { }\n" +
                    "}\n",
                    "getName(java.lang.String)");
    }
    
    public void testFuzzyResolve203476b() throws Exception {
        performTest("package test;\n" +
                    "import static test.Aux.*;\n" +
                    "public class Main {\n" +
                    "    public void test() {\n" +
                    "        getName(undef);\n" +
                    "    }\n" +
                    "}\n" +
                    "class Aux {\n" +
                    "    public static void getName(String str) { }\n" +
                    "}\n",
                    "getName(java.lang.String)");
    }
    
    public void testFuzzyResolve203476c() throws Exception {
        performTest("package test;\n" +
                    "import static test.Aux.other;\n" +
                    "public class Main {\n" +
                    "    public void test() {\n" +
                    "        getName(undef);\n" +
                    "    }\n" +
                    "}\n" +
                    "class Aux {\n" +
                    "    public static void getName(String str) { }\n" +
                    "    public static void other(String str) { }\n" +
                    "}\n",
                    "<not resolved>");
    }
    
    public void testFuzzyResolve203476d() throws Exception {
        performTest("package test;\n" +
                    "import static test.Aux.getName;\n" +
                    "public class Main {\n" +
                    "    public void test() {\n" +
                    "        getName(undef);\n" +
                    "    }\n" +
                    "}\n" +
                    "class Aux {\n" +
                    "    public void getName(String str) { }\n" +
                    "}\n",
                    "<not resolved>");
    }
    
    public void testFuzzyResolve203476e() throws Exception {
        performTest("package test;\n" +
                    "public class Main {\n" +
                "        public static void getName(String str) { }\n" +
                    "    class Aux {\n" +
                    "        public void test() {\n" +
                    "            getName(undef);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n",
                    "getName(java.lang.String)");
    }
    
    public void testNameGuessKeywordNoShortName2() throws Exception {
        assertEquals("aDo", Utilities.adjustName("do"));
    }

    private FileObject source;
    
    private void performTest(String sourceCode, String... golden) throws Exception {
        FileObject root = GoToSupportTest.makeScratchDir(this);
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        FileObject testDir  = sourceDir.createFolder("test");
        
        source = testDir.createData("Test.java");
        
        TestUtilities.copyStringToFile(source, sourceCode);
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        SourceUtilsTestUtil.compileRecursively(sourceDir);
        
        final List<String> result = new LinkedList<String>();
        
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {
            public void run(final CompilationController cc) throws Exception {
                cc.toPhase(Phase.RESOLVED);
                
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                        if (!cc.getTreeUtilities().isSynthetic(getCurrentPath())) {
                            List<ExecutableElement> elements = Utilities.fuzzyResolveMethodInvocation(cc, getCurrentPath(), new ArrayList<TypeMirror>(), new int[1]);
                            if (elements.isEmpty()) {
                                result.add("<not resolved>");
                            } else {
                                for (ExecutableElement ee : elements) {
                                    if (ee != null) {
                                        result.add(ee.toString()); //XXX
                                    }
                                }
                            }
                        }
                        
                        return super.visitMethodInvocation(node, p);
                    }

                    @Override
                    public Void visitNewClass(NewClassTree node, Void p) {
                        if (!cc.getTreeUtilities().isSynthetic(getCurrentPath())) {
                            List<ExecutableElement> elements = Utilities.fuzzyResolveMethodInvocation(cc, getCurrentPath(), new ArrayList<TypeMirror>(), new int[1]);
                            if (elements.isEmpty()) {
                                result.add("<not resolved>");
                            } else {
                                for (ExecutableElement ee : elements) {
                                    if (ee != null) {
                                        result.add(ee.toString()); //XXX
                                    }
                                }
                            }
                        }
                        
                        return super.visitNewClass(node, p);
                    }
                    
                }.scan(cc.getCompilationUnit(), null);
            }
        }, true);
        
        assertEquals(Arrays.asList(golden), result);
    }
}
