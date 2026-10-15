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
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import org.netbeans.api.java.source.JavaSourceTest.SourceLevelQueryImpl;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 *
 * @author Jan Lahoda
 */
public class ElementUtilitiesTest extends NbTestCase {

    private static final FileObject[] EMPTY_PATH = new FileObject[0];

    public ElementUtilitiesTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.setLookup(new Object[0], ElementUtilities.class.getClassLoader());
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }
    
    private FileObject[] modulePathElements = EMPTY_PATH;
    private FileObject sourceRoot;
    private FileObject testFO;
        
    private void prepareTest(FileDescription... fileNameAndContent) throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);
        
        assertNotNull(workFO);
        
        sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache, EMPTY_PATH, modulePathElements);
        
        if (fileNameAndContent.length > 0) {
            testFO = writeFiles(sourceRoot, fileNameAndContent);
        } else {
            testFO = sourceRoot.createData("Test.java");
        }
    }

    private FileObject writeFiles(FileObject src,
                                  FileDescription... fileNameAndContent) throws Exception {
        FileObject firstFile = null;

        for (FileDescription fileDescription : fileNameAndContent) {
            FileObject f = writeFile(src,
                                     fileDescription.path(),
                                     fileDescription.content());

            if (firstFile == null) {
                firstFile = f;
            }
        }

        return firstFile;
    }

    private FileObject writeFile(FileObject root,
                                 String path,
                                 String content) throws Exception {
        FileObject file = FileUtil.createData(root, path);

        TestUtilities.copyStringToFile(FileUtil.toFile(file), content);

        return file;
    }

    public void testGetImplementationOfAndOverriden() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "8");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "import java.util.AbstractList;\n"
                + "import java.util.Iterator;\n"
                + "import java.util.ListIterator;\n"
                + "\n"
                + "public abstract class Test1 extends AbstractList {\n"
                + "    public static void method() {}\n"
                + "\n"
                + "    public abstract class Test2 extends Test1 { \n"
                + "         public static void method() {}\n"
                + "    } \n"
                + "     \n"
                + "    public class Test3 extends Test2 {\n"
                + "         public static void method() {}\n"
                + "    } \n"
                + "}\n"
                + "abstract class TestImpl implements NRI, NRI2 {\n"
                + "    public static void method() {}\n"
                + "    public static Object next() { return null; }\n"
                + "    public static String previous() { return null; }\n"
                + "\n"
                + "}\n"
                + "interface NRI extends ListIterator<String> {\n"
                + "    private static void someMethod() {\n"
                + "    }\n"
                + "}\n"
                + "interface NRI2 extends ListIterator<String> {\n"
                + "    public default void remove()  {\n"
                + "    }\n"
                + "}");
        SourceLevelQueryImpl.sourceLevel = "8";
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassTree test1Tree = (ClassTree)controller.getCompilationUnit().getTypeDecls().get(0);
                ClassTree testImplTree = (ClassTree)controller.getCompilationUnit().getTypeDecls().get(1);
                ClassTree nriTree = (ClassTree)controller.getCompilationUnit().getTypeDecls().get(2);
                TreePath cuPath = new TreePath(controller.getCompilationUnit());
                
                ClassTree test2Tree = (ClassTree)test1Tree.getMembers().get(2);
                ClassTree test3Tree = (ClassTree)test1Tree.getMembers().get(3);
                
                TreePath test1Path = new TreePath(cuPath, test1Tree);
                TreePath testImplPath = new TreePath(cuPath, testImplTree);
                TreePath nriPath = new TreePath(cuPath, test1Tree);
                TreePath test2Path = new TreePath(test1Path, test1Tree.getMembers().get(2));
                TreePath test3Path = new TreePath(test1Path, test1Tree.getMembers().get(3));
                
                Element test1Class = controller.getTrees().getElement(test1Path);
                Element test2Class = controller.getTrees().getElement(test2Path);
                TypeElement test3Class = (TypeElement)controller.getTrees().getElement(test3Path);
                TypeElement testImplClass = (TypeElement)controller.getTrees().getElement(testImplPath);
                
                Element mMethod = controller.getTrees().getElement(new TreePath(test1Path, test1Tree.getMembers().get(1)));
                
                // get implementation of Test1.method in Test3; should be still Test1.method
                Element check = controller.getElementUtilities().getImplementationOf((ExecutableElement)mMethod, test3Class);
                assertSame(mMethod, check);
                
                // check that overriden static method is null, although matching signature exists in the superclass.
                Element test3Method = controller.getTrees().getElement(new TreePath(test3Path, test3Tree.getMembers().get(1)));
                check = controller.getElementUtilities().getOverriddenMethod((ExecutableElement)test3Method);
                assertNull(check);
                
                Element nextMethod = controller.getTrees().getElement(new TreePath(test1Path, testImplTree.getMembers().get(2)));
                Element prevMethod = controller.getTrees().getElement(new TreePath(test1Path, testImplTree.getMembers().get(3)));
                
                ExecutableElement iteratorRemoveMethod = (ExecutableElement)controller.getElementUtilities().findElement("java.util.Iterator.remove()");
                ExecutableElement iteratorNextMethod = (ExecutableElement)controller.getElementUtilities().findElement("java.util.Iterator.next()");
                ExecutableElement iteratorPrevMethod = (ExecutableElement)controller.getElementUtilities().findElement("java.util.Iterator.previous()");
                ExecutableElement listIteratorPrevMethod = (ExecutableElement)controller.getElementUtilities().findElement("java.util.ListIterator.previous()");
                ExecutableElement listIteratorNextMethod = (ExecutableElement)controller.getElementUtilities().findElement("java.util.ListIterator.next()");

                check = controller.getElementUtilities().getImplementationOf(iteratorNextMethod, testImplClass);
                assertSame(null, check);
                check = controller.getElementUtilities().getImplementationOf(listIteratorPrevMethod, testImplClass);
                assertSame(check, prevMethod);
                
                // try to find remove implementation, should find NRI2.remove()
                check = controller.getElementUtilities().getImplementationOf(iteratorRemoveMethod, testImplClass);
                assertNotNull(check);
                TypeElement owner = (TypeElement)check.getEnclosingElement();
                assertEquals("NRI2", owner.getSimpleName().toString());
                
                
                // but findUnimplementedMethods does:
                List<? extends ExecutableElement> unimplemented = controller.getElementUtilities().findUnimplementedMethods(testImplClass);
                int cnt = 0;
                ExecutableElement foundNext = null;
                ExecutableElement foundPrev = null;
                ExecutableElement foundRemove = null;
                for (ExecutableElement ee : unimplemented) {
                    String s = ee.getSimpleName().toString();
                    if ("next".equals(s)) {
                        foundNext = ee;
                        cnt++;
                    }
                    if ("previous".equals(s)) {
                        foundPrev = ee;
                        cnt++;
                    }
                    if ("remove".equals(s)) {
                        foundRemove = ee;
                        cnt++;
                    }
                }
                assertEquals("Only one of next/previous/remove is actually unimplemented", 1, cnt);
                // next from listIterator (not iterator) is not implemented
                assertSame(listIteratorNextMethod, foundNext);
                // remove from the is implemented by the default method
                assertNull(foundRemove);
                
                unimplemented = controller.getElementUtilities().findUnimplementedMethods(testImplClass, true);
                cnt = 0;
                for (ExecutableElement ee : unimplemented) {
                    if ("remove".equals(ee.getSimpleName().toString())) {
                        cnt++;
                    }
                }
                assertEquals("Should return even remove default method", 1, cnt);
            }
        }, true);
    }

    public void testInheritanceOfDefault() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "8");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "import java.util.Iterator;\n" +
                "public class Test {\n" +
                "    public class C1 implements Iterator, I1 {\n" +
                "    }\n" +
                "    \n" +
                "    public class C2 implements A1, B {\n" +
                "    }\n" +
                "    \n" +
                "    public class C3 implements A2 {\n" +
                "    }\n" +
                "    \n" +
                "    public class C4 implements A1, A2 {\n" +
                "    }\n" +
                "    \n" +
                "    public class C5 implements A1, AB {\n" +
                "    }\n" +
                "    public class C6 implements A1, AX {\n" +
                "    }\n" +
                "    \n" +
                "    public interface I1 extends Iterator {\n" +
                "        public default boolean hasNext() { return false; }\n" +
                "    }\n" +
                "    \n" +
                "    public interface A {\n" +
                "        public void a();\n" +
                "    }\n" +
                "    \n" +
                "    public interface A1 extends A {\n" +
                "        public default void a() {}\n" +
                "    }\n" +
                "    \n" +
                "    public interface B {\n" +
                "        public void b();\n" +
                "    }\n" +
                "    \n" +
                "    public interface AX  {\n" +
                "        public void a();\n" +
                "    }\n" +
                "    \n" +
                "    public interface A2 extends A {\n" +
                "        public void a();\n" +
                "    }\n" +
                "    \n" +
                "    public interface AB extends A, B {\n" +
                "        public default void b() {}\n" +
                "    }\n" +
                "    \n" +
                "    public interface AB2 extends A1, B {\n" +
                "    }\n" +
                "    \n" +
                "}");
        SourceLevelQueryImpl.sourceLevel = "8";
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ClassTree outerTree = (ClassTree)controller.getCompilationUnit().getTypeDecls().get(0);
                TreePath outerPath = new TreePath(
                        new TreePath(controller.getCompilationUnit()),
                        outerTree);
                ClassTree c1Tree = (ClassTree)outerTree.getMembers().get(1);
                ClassTree c2Tree = (ClassTree)outerTree.getMembers().get(2);
                ClassTree c3Tree = (ClassTree)outerTree.getMembers().get(3);
                ClassTree c4Tree = (ClassTree)outerTree.getMembers().get(4);
                ClassTree c5Tree = (ClassTree)outerTree.getMembers().get(5);
                ClassTree c6Tree = (ClassTree)outerTree.getMembers().get(6);
                
                TypeElement c1Elem = (TypeElement)controller.getTrees().getElement(new TreePath(outerPath, c1Tree));
                TypeElement c2Elem = (TypeElement)controller.getTrees().getElement(new TreePath(outerPath, c2Tree));
                TypeElement c3Elem = (TypeElement)controller.getTrees().getElement(new TreePath(outerPath, c3Tree));
                TypeElement c4Elem = (TypeElement)controller.getTrees().getElement(new TreePath(outerPath, c4Tree));
                TypeElement c5Elem = (TypeElement)controller.getTrees().getElement(new TreePath(outerPath, c5Tree));
                TypeElement c6Elem = (TypeElement)controller.getTrees().getElement(new TreePath(outerPath, c6Tree));
              
                // C1 gets defaulted 'remove' and 'hasNext'.
                List<? extends ExecutableElement> missing = controller.getElementUtilities().findUnimplementedMethods(c1Elem);
                ExecutableElement found = find("remove", missing);
                assertNull("remove is defined by default", found);
                found = find("hasNext", missing);
                assertNull("hasNext is defined by default", found);
                
                // ensure that we can find implementation of those methods:
                ExecutableElement iteratorRemove = (ExecutableElement)controller.getElementUtilities().findElement("java.util.Iterator.remove()");
                ExecutableElement iteratorHasNext = (ExecutableElement)controller.getElementUtilities().findElement("java.util.Iterator.hasNext()");
                ExecutableElement check = (ExecutableElement)controller.getElementUtilities().getImplementationOf(iteratorRemove, c1Elem);
                ExecutableElement check2 = (ExecutableElement)controller.getElementUtilities().getImplementationOf(iteratorHasNext, c1Elem);
                
                assertNotNull(check);
                assertNotNull(check2);
                
                TypeElement enclosing = (TypeElement)check.getEnclosingElement();
                assertEquals("java.util.Iterator", enclosing.getQualifiedName().toString());
                enclosing = (TypeElement)check2.getEnclosingElement();
                assertEquals("Test.I1", enclosing.getQualifiedName().toString());
                
                // now get everything incl. the defaults:
                missing = controller.getElementUtilities().findUnimplementedMethods(c1Elem, true);
                found = find("remove", missing);
                assertNotNull("default remove should be present", found);
                found = find("hasNext", missing);
                assertNotNull("default hasNext should be present", found);
                
                // C2 gets one default and one abstract method
                missing = controller.getElementUtilities().findUnimplementedMethods(c1Elem);
                assertEquals(1, missing.size());
                assertNull("a is provided from default", find("a", missing));
                ExecutableElement method = (ExecutableElement)controller.getElementUtilities().findElement("Test.A.a()");
                check = (ExecutableElement)controller.getElementUtilities().getImplementationOf(method, c2Elem);
                assertNotNull(check);
                enclosing = (TypeElement)check.getEnclosingElement();
                assertEquals("Test.A1", enclosing.getQualifiedName().toString());
                
                missing = controller.getElementUtilities().findUnimplementedMethods(c2Elem, true);
                assertEquals(2, missing.size());
                
                // C3 gets a from A2, not A
                missing = controller.getElementUtilities().findUnimplementedMethods(c3Elem);
                assertEquals(1, missing.size());
                found = (ExecutableElement)controller.getElementUtilities().findElement("Test.A2.a()");
                check = missing.get(0);
                assertSame(found, check);
                
                // C4 gets default from A2, but that is discarded by sibling A2
                missing = controller.getElementUtilities().findUnimplementedMethods(c4Elem);
                assertEquals(1, missing.size());
                check = missing.get(0);
                assertSame(found, check);
                method = (ExecutableElement)controller.getElementUtilities().findElement("Test.A.a()");
                check = (ExecutableElement)controller.getElementUtilities().getImplementationOf(method, c4Elem);
                // no real implementation
                assertNull(check);
                
                // C5 gets defaulted b() and a()
                missing = controller.getElementUtilities().findUnimplementedMethods(c5Elem);
                assertEquals("All methods are defined by defaults", 0, missing.size());
                
                // C6 gets an unrelated a() which discards the A1.a() default
                missing = controller.getElementUtilities().findUnimplementedMethods(c6Elem);
                assertEquals("Default method should conflict", 1, missing.size());
                method = (ExecutableElement)controller.getElementUtilities().findElement("Test.AX.a()");
                assertSame(method, missing.get(0));
            }}, true);
    }
    
    private ExecutableElement find(String s, Collection<? extends ExecutableElement> elems) {
        for (ExecutableElement e : elems) {
            if (e.getSimpleName().contentEquals(s)) {
                return e;
            }
        }
        return null;
    }

    public void testI18N() throws Exception {
        prepareTest();
        
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "public class Test {" +
                "}");
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                
                {
                    Element wait = controller.getElementUtilities().findElement("java.lang.Object.wait(long)");
                    assertNotNull(wait);
                    assertEquals(ElementKind.METHOD, wait.getKind());
                    ExecutableElement waitMethod = (ExecutableElement) wait;
                    assertEquals("wait", waitMethod.getSimpleName().toString());
                    assertEquals(1, waitMethod.getParameters().size());
                    assertEquals(TypeKind.LONG, waitMethod.getParameters().get(0).asType().getKind());
                    assertEquals(controller.getElements().getTypeElement("java.lang.Object"), waitMethod.getEnclosingElement());
                }
                
                {
                    Element arrayListInit = controller.getElementUtilities().findElement("java.util.ArrayList.ArrayList(java.util.Collection)");
                    assertNotNull(arrayListInit);
                    assertEquals(ElementKind.CONSTRUCTOR, arrayListInit.getKind());
                    ExecutableElement arrayListInitMethod = (ExecutableElement) arrayListInit;
                    assertEquals("<init>", arrayListInitMethod.getSimpleName().toString());
                    assertEquals(1, arrayListInitMethod.getParameters().size());
                    assertEquals("java.util.Collection", controller.getTypes().erasure(arrayListInitMethod.getParameters().get(0).asType()).toString());
                    assertEquals(controller.getElements().getTypeElement("java.util.ArrayList"), arrayListInitMethod.getEnclosingElement());
                }
                
                {
                    Element arrayListAdd = controller.getElementUtilities().findElement("java.util.ArrayList.add(int, Object)");
                    assertNotNull(arrayListAdd);
                    assertEquals(ElementKind.METHOD, arrayListAdd.getKind());
                    ExecutableElement arrayListAddMethod = (ExecutableElement) arrayListAdd;
                    assertEquals("add", arrayListAddMethod.getSimpleName().toString());
                    assertEquals(2, arrayListAddMethod.getParameters().size());
                    assertEquals(TypeKind.INT, arrayListAddMethod.getParameters().get(0).asType().getKind());
                    assertEquals("java.lang.Object", controller.getTypes().erasure(arrayListAddMethod.getParameters().get(1).asType()).toString());
                    assertEquals(controller.getElements().getTypeElement("java.util.ArrayList"), arrayListAddMethod.getEnclosingElement());
                }
                
                {
                    Element arraysAsList = controller.getElementUtilities().findElement("java.util.Arrays.asList(Object...)");
                    assertNotNull(arraysAsList);
                    assertEquals(ElementKind.METHOD, arraysAsList.getKind());
                    ExecutableElement arraysAsListMethod = (ExecutableElement) arraysAsList;
                    assertEquals("asList", arraysAsListMethod.getSimpleName().toString());
                    assertEquals(1, arraysAsListMethod.getParameters().size());
                    assertEquals(TypeKind.ARRAY, arraysAsListMethod.getParameters().get(0).asType().getKind());
                    assertEquals(controller.getElements().getTypeElement("java.util.Arrays"), arraysAsListMethod.getEnclosingElement());
                }
                
                {
                    Element hashCode = controller.getElementUtilities().findElement("java.lang.Object.hashCode()");
                    assertNotNull(hashCode);
                    assertEquals(ElementKind.METHOD, hashCode.getKind());
                    ExecutableElement hashCodeMethod = (ExecutableElement) hashCode;
                    assertEquals("hashCode", hashCodeMethod.getSimpleName().toString());
                    assertEquals(0, hashCodeMethod.getParameters().size());
                    assertEquals(controller.getElements().getTypeElement("java.lang.Object"), hashCodeMethod.getEnclosingElement());
                }
                
                {
                    Element bigIntegerOne = controller.getElementUtilities().findElement("java.math.BigInteger.ONE");
                    assertNotNull(bigIntegerOne);
                    assertEquals(ElementKind.FIELD, bigIntegerOne.getKind());
                    assertEquals("ONE", bigIntegerOne.getSimpleName().toString());
                    assertEquals(controller.getElements().getTypeElement("java.math.BigInteger"), bigIntegerOne.getEnclosingElement());
                }
                
                {
                    Element bigInteger = controller.getElementUtilities().findElement("java.math.BigInteger");
                    assertEquals(controller.getElements().getTypeElement("java.math.BigInteger"), bigInteger);
                }
            }
        }, true);
    }

    public void testIsLocal() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "8");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "import java.util.Iterator;\n" +
                "public class Test {\n" +
                "    public class Nested {\n" +
                "        {\n" +
                "            int i;\n" +
                "            class C { class CN {} int i; { int i;} private void test() { int i; } }\n" +
                "        }\n" +
                "        private void test() {\n" +
                "            int i;\n" +
                "            class C { class CN {} int i; { int i;} private void test() { int i; } }\n" +
                "        }\n" +
                "        private Object o = new Object() {\n" +
                "            {\n" +
                "                int i;\n" +
                "                class C { class CN {} int i; { int i;} private void test() { int i; } }\n" +
                "            }\n" +
                "            private void test() {\n" +
                "                int i;\n" +
                "                class C { class CN {} int i; { int i;} private void test() { int i; } }\n" +
                "            }\n" +
                "            class ON {\n" +
                "                {\n" +
                "                    int i;\n" +
                "                    class C { class CN {} int i; { int i;} private void test() { int i; } }\n" +
                "                }\n" +
                "                private void test() {\n" +
                "                    int i;\n" +
                "                    class C { class CN {} int i; { int i;} private void test() { int i; } }\n" +
                "                }\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "}");
        SourceLevelQueryImpl.sourceLevel = "8";
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.RESOLVED);
                new TreePathScanner<Void, Void>() {
                    boolean local;
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        handleDecl();
                        return super.visitClass(node, p);
                    }
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        handleDecl();
                        boolean oldLocal = local;
                        try {
                            local = true;
                            return super.visitMethod(node, p);
                        } finally {
                            local = oldLocal;
                        }
                    }
                    @Override
                    public Void visitVariable(VariableTree node, Void p) {
                        handleDecl();
                        boolean oldLocal = local;
                        try {
                            local = true;
                            return super.visitVariable(node, p);
                        } finally {
                            local = oldLocal;
                        }
                    }
                    @Override
                    public Void visitBlock(BlockTree node, Void p) {
                        boolean oldLocal = local;
                        try {
                            local |= TreeUtilities.CLASS_TREE_KINDS.contains(getCurrentPath().getParentPath().getLeaf().getKind());
                            return super.visitBlock(node, p);
                        } finally {
                            local = oldLocal;
                        }
                    }
                    private void handleDecl() {
                        Element current = controller.getTrees().getElement(getCurrentPath());
                        assertEquals(local, controller.getElementUtilities().isLocal(current));
                    }
                }.scan(controller.getCompilationUnit(), null);
            }}, true);
    }


    public void testGetMembers() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "8");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "package test;" +
                "public class A implements Runnable {" +
                "    private final int FIELD1 = 0;" +
                "    private final int FIELD2 = 1;" +
                "    public void run() {}" +
                "}");
        SourceLevelQueryImpl.sourceLevel = "8";

        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            ClassTree outerTree = (ClassTree)controller.getCompilationUnit().getTypeDecls().get(0);
            TreePath outerPath = new TreePath(new TreePath(controller.getCompilationUnit()), outerTree);
            TypeMirror mirror = controller.getTrees().getTypeMirror(outerPath);
            assertNotNull(mirror);

            ElementUtilities utils = controller.getElementUtilities();
            Set<String> members = new HashSet<>();
            utils.getMembers(mirror, null).forEach((e) -> members.add(e.toString()));

            List<String> good = Arrays.asList("getClass()",
                    "hashCode()", "equals(java.lang.Object)", "clone()", "toString()", "notify()",
                    "notifyAll()", "wait(long)", "wait(long,int)", "wait()", "finalize()", "A()",
                    "FIELD1", "FIELD2", "run()", "this", "super", "class");
            assertEquals(good.size(), members.size());
            assertTrue(members.containsAll(good));

        }, true);
    }

    public void testGetGlobalTypes() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "8");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                "package test;" +
                "import java.util.List;" +
                "public class A { public static class B {} }"
        );
        SourceLevelQueryImpl.sourceLevel = "8";

        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            ElementUtilities utils = controller.getElementUtilities();
            Set<String> globals = new HashSet<>();
            utils.getGlobalTypes(null).forEach((e) -> globals.add(e.toString()));

            assertFalse(globals.isEmpty());
            assertTrue(globals.contains("test.A"));
            assertFalse(globals.contains("test.A.B"));
            assertTrue(globals.contains("java.util.List"));
            assertTrue(globals.contains("java.lang.System"));
        }, true);
    }

    public void testGetLinkedRecordElements1() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "17");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                """
                package test;
                public record R(String component) {}
                """
        );
        SourceLevelQueryImpl.sourceLevel = "17";

        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            TypeElement record = controller.getTopLevelElements().get(0);
            ElementUtilities utils = controller.getElementUtilities();
            Collection<? extends Element> linked = utils.getLinkedRecordElements(record.getRecordComponents().get(0));
            Set<String> linkedEncoded = linked.stream()
                                              .map(Element::getKind)
                                              .map(ElementKind::name)
                                              .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(new TreeSet<>(Arrays.asList("FIELD", "METHOD", "PARAMETER", "RECORD_COMPONENT")),
                         linkedEncoded);

            for (Element linkedElement : linked) {
                if (!linked.equals(utils.getLinkedRecordElements(linkedElement))) {
                    utils.getLinkedRecordElements(linkedElement);
                }
                assertEquals(linked, utils.getLinkedRecordElements(linkedElement));
            }
        }, true);
    }

    public void testGetLinkedRecordElements2() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "17");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                """
                package test;
                public record R(String component) {
                    public R {
                        this.component = component;
                    }
                    public String component() {
                        return component;
                    }
                }
                """
        );
        SourceLevelQueryImpl.sourceLevel = "17";

        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            TypeElement record = controller.getTopLevelElements().get(0);
            ElementUtilities utils = controller.getElementUtilities();
            Collection<? extends Element> linked = utils.getLinkedRecordElements(record.getRecordComponents().get(0));
            Set<String> linkedEncoded = linked.stream()
                                              .map(Element::getKind)
                                              .map(ElementKind::name)
                                              .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(new TreeSet<>(Arrays.asList("FIELD", "METHOD", "PARAMETER", "RECORD_COMPONENT")),
                         linkedEncoded);

            for (Element linkedElement : linked) {
                if (!linked.equals(utils.getLinkedRecordElements(linkedElement))) {
                    utils.getLinkedRecordElements(linkedElement);
                }
                assertEquals(linked, utils.getLinkedRecordElements(linkedElement));
            }
        }, true);
    }

    public void testGetLinkedRecordElements3() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "17");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                """
                package test;
                public record R(String component) {
                    public R(String component) {
                        this.component = component;
                    }
                    public String component() {
                        return component;
                    }
                }
                """
        );
        SourceLevelQueryImpl.sourceLevel = "17";

        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            TypeElement record = controller.getTopLevelElements().get(0);
            ElementUtilities utils = controller.getElementUtilities();
            Collection<? extends Element> linked = utils.getLinkedRecordElements(record.getRecordComponents().get(0));
            Set<String> linkedEncoded = linked.stream()
                                              .map(Element::getKind)
                                              .map(ElementKind::name)
                                              .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(new TreeSet<>(Arrays.asList("FIELD", "METHOD", "PARAMETER", "RECORD_COMPONENT")),
                         linkedEncoded);

            for (Element linkedElement : linked) {
                if (!linked.equals(utils.getLinkedRecordElements(linkedElement))) {
                    utils.getLinkedRecordElements(linkedElement);
                }
                assertEquals(linked, utils.getLinkedRecordElements(linkedElement));
            }
        }, true);
    }

    public void testGetLinkedRecordElements4() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(testFO, "17");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                """
                package test;
                public record R(String component) {
                    public R(String anotherName) { //error
                        this.component = anotherName;
                    }
                    public String component() {
                        return component;
                    }
                }
                """
        );
        SourceLevelQueryImpl.sourceLevel = "17";

        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            TypeElement record = controller.getTopLevelElements().get(0);
            Element brokenParameter = ElementFilter.constructorsIn(record.getEnclosedElements()).get(0).getParameters().get(0);
            ElementUtilities utils = controller.getElementUtilities();
            Collection<? extends Element> linked = utils.getLinkedRecordElements(brokenParameter);
            Set<String> linkedEncoded = linked.stream()
                                              .map(Element::getKind)
                                              .map(ElementKind::name)
                                              .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(new TreeSet<>(Arrays.asList("PARAMETER")),
                         linkedEncoded);
        }, true);
    }

    public void testGetLinkedRecordElements5() throws Exception {
        prepareTest();
        SourceUtilsTestUtil.setSourceLevel(sourceRoot, "17");
        TestUtilities.copyStringToFile(FileUtil.toFile(testFO),
                """
                package test;
                public record R(String component) {
                    public R {
                        this.component = component;
                    }
                    public String component() {
                        return component;
                    }
                }
                """
        );
        FileObject useFO = FileUtil.createData(sourceRoot, "test/Use.java");
        TestUtilities.copyStringToFile(FileUtil.toFile(useFO),
                """
                package test;
                public class Use {}
                """
        );
        SourceLevelQueryImpl.sourceLevel = "17";

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        JavaSource javaSource = JavaSource.forFileObject(useFO);
        javaSource.runUserActionTask((CompilationController controller) -> {
            controller.toPhase(JavaSource.Phase.RESOLVED);

            TypeElement record = controller.getElements().getTypeElement("test.R");
            Element component = record.getRecordComponents().get(0);
            ElementUtilities utils = controller.getElementUtilities();
            Collection<? extends Element> linked = utils.getLinkedRecordElements(component);
            Set<String> linkedEncoded = linked.stream()
                                              .map(Element::getKind)
                                              .map(ElementKind::name)
                                              .collect(Collectors.toCollection(TreeSet::new));
            assertEquals(new TreeSet<>(Arrays.asList("FIELD", "METHOD", "PARAMETER", "RECORD_COMPONENT")),
                         linkedEncoded);

            for (Element linkedElement : linked) {
                if (!linked.equals(utils.getLinkedRecordElements(linkedElement))) {
                    utils.getLinkedRecordElements(linkedElement);
                }
                assertEquals(linked, utils.getLinkedRecordElements(linkedElement));
            }
        }, true);
    }

    public void testTransitivelyExportedPackages() throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject module1 = workFO.createFolder("module1");
        FileObject module1Src = module1.createFolder("src");
        FileObject module1Classes = module1.createFolder("classes");

        writeFiles(module1Src,
                   new FileDescription("module-info.java",
                                       """
                                       module module1 {
                                           exports api1a;
                                           exports api1b to test;
                                           exports api1c to another;
                                       }
                                       """),
                   new FileDescription("api1a/Api1a.java",
                                       """
                                       package api1a;
                                       public class Api1a {
                                       }
                                       """),
                   new FileDescription("api1b/Api1b.java",
                                       """
                                       package api1b;
                                       public class Api1b {
                                       }
                                       """),
                   new FileDescription("api1c/Api1c.java",
                                       """
                                       package api1c;
                                       public class Api1c {
                                       }
                                       """),
                   new FileDescription("impl1/Impl1.java",
                                       """
                                       package impl1;
                                       public class Impl1 {
                                       }
                                       """));
        compile(module1Src, module1Classes, "24");

        FileObject module2 = workFO.createFolder("module2");
        FileObject module2Src = module2.createFolder("src");
        FileObject module2Classes = module2.createFolder("classes");

        writeFiles(module2Src,
                   new FileDescription("module-info.java",
                                       """
                                       module module2 {
                                           requires transitive module1;
                                           exports api2a;
                                           exports api2b to test;
                                           exports api2c to another;
                                       }
                                       """),
                   new FileDescription("api2a/Api2a.java",
                                       """
                                       package api2a;
                                       public class Api2a {
                                       }
                                       """),
                   new FileDescription("api2b/Api2b.java",
                                       """
                                       package api2b;
                                       public class Api2b {
                                       }
                                       """),
                   new FileDescription("api2c/Api2c.java",
                                       """
                                       package api2c;
                                       public class Api2c {
                                       }
                                       """),
                   new FileDescription("impl2/Impl2.java",
                                       """
                                       package impl2;
                                       public class Impl2 {
                                       }
                                       """));
        compile(module2Src, module2Classes, "24", "--module-path", FileUtil.toFile(module1Classes).getAbsolutePath());

        modulePathElements = new FileObject[] {
            module1Classes,
            module2Classes
        };

        prepareTest(new FileDescription("module-info.java",
                                        """
                                        module test {
                                            requires module2;
                                        }
                                        """));

        SourceUtilsTestUtil.setSourceLevel(testFO, "24");
        SourceLevelQueryImpl.sourceLevel = "24";
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                ModuleElement m2 = controller.getElements()
                                             .getModuleElement("module2");
                Set<String> packages =
                        controller.getElementUtilities()
                                  .transitivelyExportedPackages(m2)
                                  .stream()
                                  .map(pack -> pack.getQualifiedName().toString())
                                  .collect(Collectors.toSet());
                assertEquals(Set.of("api1b", "api1a", "api2b", "api2a"),
                             packages);
            }
        }, true);
    }

    private void compile(FileObject src, FileObject classes, String sourceLevel, String... extraOpts) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null)) {
            List<File> sources = new ArrayList<>();

            for (Enumeration<? extends FileObject> en = src.getChildren(true); en.hasMoreElements(); ) {
                FileObject c = en.nextElement();

                if (c.isData() && "text/x-java".equals(c.getMIMEType())) {
                    sources.add(FileUtil.toFile(c));
                }
            }

            Iterable<? extends JavaFileObject> sourceFileObjects = fm.getJavaFileObjectsFromFiles(sources);
            List<String> options = new ArrayList<>();

            options.addAll(List.of("--release", sourceLevel, "-d"));
            options.addAll(List.of(FileUtil.toFile(classes).getAbsolutePath()));
            options.addAll(List.of(extraOpts));

            assertTrue(compiler.getTask(null, fm, null, options, null, sourceFileObjects).call());
        }
    }

    private record FileDescription(String path, String content) {}
}
