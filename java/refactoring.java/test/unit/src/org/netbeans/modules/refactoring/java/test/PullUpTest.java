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
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.RefactoringUtils;
import org.netbeans.modules.refactoring.java.api.MemberInfo;
import org.netbeans.modules.refactoring.java.api.PullUpRefactoring;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Ruijs
 */
public class PullUpTest extends RefactoringTestBase {

    public PullUpTest(String name) {
        super(name, "1.8");
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
    
    public void test241514a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public interface A { void x(); }"),
                new File("pullup/B.java", "package pullup; public interface B extends A { default void y() { } }"));
        performPullUpIface(src.getFileObject("pullup/B.java"), 0, 0, true);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public interface A { void x(); void y(); }"),
                new File("pullup/B.java", "package pullup; public interface B extends A { default void y() { } }"));
    }
    
    public void test241514b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public interface A { void x(); }"),
                new File("pullup/B.java", "package pullup; public interface B extends A { default void y() { } }"));
        performPullUpIface(src.getFileObject("pullup/B.java"), 0, 0, false);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public interface A { void x(); default void y() { } }"),
                new File("pullup/B.java", "package pullup; public interface B extends A {}"));
    }
    
    public void testPullUpOverridingMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B implements C { @Override public void i() { } }"),
                new File("pullup/B.java", "package pullup; public class B { }"),
                new File("pullup/C.java", "package pullup; public interface C { void i(); }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B implements C {}"),
                new File("pullup/B.java", "package pullup; public class B { public void i() { } }"),
                new File("pullup/C.java", "package pullup; public interface C { void i(); }"));
        
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B  { @Override public void i() { } }"),
                new File("pullup/B.java", "package pullup; public class B extends C { }"),
                new File("pullup/C.java", "package pullup; public class C { public void i() { } }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B {}"),
                new File("pullup/B.java", "package pullup; public class B extends C { @Override public void i() { } }"),
                new File("pullup/C.java", "package pullup; public class C { public void i() { } }"));
    }
    
    public void test230719() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public interface A {\n"
                + "    int cal(int a, int b);\n"
                + "}"),
                new File("pullup/B.java", "package pullup; public class B implements A {\n"
                + "    public int cal(int a, int b,int c) {\n"
                + "        return a+b+c;\n"
                + "    }\n"
                + "    public int cal(int a, int b) {\n"
                + "        return a+b;\n"
                + "    }\n"
                + "}"));
        performPullUpIface(src.getFileObject("pullup/B.java"), 1, 0, true);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public interface A {\n"
                + "    int cal(int a, int b);\n"
                + "    int cal(int a, int b, int c);\n"
                + "}"),
                new File("pullup/B.java", "package pullup; public class B implements A {\n"
                + "    public int cal(int a, int b,int c) {\n"
                + "        return a+b+c;\n"
                + "    }\n"
                + "    public int cal(int a, int b) {\n"
                + "        return a+b;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test230930() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public interface A { }"),
                new File("pullup/B.java", "package pullup; public class B implements A { static void y(); }"));
        performPullUpIface(src.getFileObject("pullup/B.java"), 0, 0, true);
    }
    
    public void test229061() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public interface A { void x(); }"),
                new File("pullup/B.java", "package pullup; public interface B extends A { void y(); }"));
        performPullUpIface(src.getFileObject("pullup/B.java"), 0, 0, true);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public interface A { void x(); void y(); }"),
                new File("pullup/B.java", "package pullup; public interface B extends A {}"));
    }

    public void test134034() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A { void x() { } }"),
                new File("pullup/B.java", "package pullup; public class B extends A { void y() { super.x(); } }"));
        performPullUp(src.getFileObject("pullup/B.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A { void x() { } void y() { x(); } }"),
                new File("pullup/B.java", "package pullup; public class B extends A {}"));
    }
    
    public void test212934() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A { }"),
                new File("pullup/B.java", "package pullup; import java.io.Serializable; public class B extends A implements Serializable { } class T implements Serializable { }"));
        performPullUpImplements(src.getFileObject("pullup/B.java"), 0, -1);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; import java.io.Serializable; public class A implements Serializable { }"),
                new File("pullup/B.java", "package pullup; import java.io.Serializable; public class B extends A { } class T implements Serializable { }"));
    }

    public void test206683() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public int i; }"),
                new File("pullup/B.java", "package pullup; public class B { private int i; }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE, new Problem(true, "ERR_PullUp_MemberAlreadyExists"));
    }

    public void testPullUpField() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public int i; }"),
                new File("pullup/B.java", "package pullup; public class B { }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B {}"),
                new File("pullup/B.java", "package pullup; public class B { public int i; }"));
    }
    
    public void testPullUpGenMethoda() throws Exception { // #147508 - [Pull Up][Push down] Remap generic names
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass<J> extends PullUpSuperClass<String, J> implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method(J j) {\n"
                + "        //method body\n"
                + "        System.out.println(j.toString());\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass<B, A> {\n"
                + "    \n"
                + "    public void m2(A a) {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass<J> extends PullUpSuperClass<String, J> implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass<B, A> {\n"
                + "    \n"
                + "    public void m2(A a) {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    public void method(A j) {\n"
                + "        //method body\n"
                + "        System.out.println(j.toString());\n"
                + "    }\n"
                + "    \n"
                + "\n"
                + "}"));
    }

    public void testPullUpGenMethodb() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> { void method(X x) { } }"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<Z> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> {}"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<Z> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { void method(Y x) { } }"));

        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> { void method(X x) { } }"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<T> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> {}"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<T> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { void method(String x) { } }"));
    }
    
    public void testPullUpGenMethodc() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> { X method() { } }"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<Z> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> {}"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<Z> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { Y method() { } }"));

        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> { void method(X x) { } }"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<T> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> {}"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<T> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { void method(String x) { } }"));
    }
    
    public void testPullUpGenField() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> { X x; }"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<Z> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> {}"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<Z> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { Y x; }"));

        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> { X x; }"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<T> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A<X extends String> extends B<String, X> {}"),
                new File("pullup/B.java", "package pullup; public class B<T, Z> extends C<T> { }"),
                new File("pullup/C.java", "package pullup; public class C<Y> { String x; }"));
    }

    public void testPullUpMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "\n"
                + "}"));
    }
    
    public void testPullUpMethodUndo() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "\n"
                + "}"));
        UndoManager undoManager = UndoManager.getDefault();
        undoManager.setAutoConfirm(true);
        undoManager.undo(null);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
    }

    public void testPullUpAllComments() throws Exception { // #210915 - Refactoring Pull Up Leaves Single Comment in Sub Class
        writeFilesAndWaitForScan(src, new File("t/SuperClass.java",
                "/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package t;\n"
                + "\n"
                + "import java.util.EventListener;\n"
                + "import u.SuperDuperClass;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Mark\n"
                + " */\n"
                + "public class SuperClass extends SuperDuperClass {\n"
                + "    \n"
                + "    // fields\n"
                + "    Integer a;\n"
                + "    /* Comment */\n"
                + "    Integer b;\n"
                + "    /**\n"
                + "     * Comment 3\n"
                + "     */\n"
                + "    String s;\n"
                + "    // Comment 2\n"
                + "    Boolean t;\n"
                + "\n"
                + "    // Comment doSomething(int x)\n"
                + "    public void doSomething(int x) {\n"
                + "        // Do Something\n"
                + "    }\n"
                + "\n"
                + "    // Comment doSomethingElse()\n"
                + "    public String doSomethingElse() {\n"
                + "        return \"hello world\";\n"
                + "    }\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * Comment doStuff(String s)\n"
                + "     */\n"
                + "    public void doStuff(String s) {\n"
                + "        System.out.println(\"do stuff\");\n"
                + "        System.out.println(new EventListener() {\n"
                + "        });\n"
                + "    }\n"
                + "}\n"
                + ""),
                new File("u/SuperDuperClass.java",
                "package u;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Mark\n"
                + " */\n"
                + "public class SuperDuperClass {\n"
                + "    \n"
                + "}"));
        performPullUp(src.getFileObject("t/SuperClass.java"), -1, Boolean.FALSE);
        verifyContent(src, new File("t/SuperClass.java",
                "/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package t;\n"
                + "\n"
                + "import java.util.EventListener;\n"
                + "import u.SuperDuperClass;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Mark\n"
                + " */\n"
                + "public class SuperClass extends SuperDuperClass {\n"
                + "    \n"
                + "}\n"
                + ""),
                new File("u/SuperDuperClass.java",
                "package u;\n"
                + "\n"
                + "import java.util.EventListener;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Mark\n"
                + " */\n"
                + "public class SuperDuperClass {\n"
                + "    \n"
                + "    // fields\n"
                + "    Integer a;\n"
                + "    /* Comment */\n"
                + "    Integer b;\n"
                + "    /**\n"
                + "     * Comment 3\n"
                + "     */\n"
                + "    String s;\n"
                + "    // Comment 2\n"
                + "    Boolean t;\n"
                + "\n"
                + "    // Comment doSomething(int x)\n"
                + "    public void doSomething(int x) {\n"
                + "        // Do Something\n"
                + "    }\n"
                + "\n"
                + "    // Comment doSomethingElse()\n"
                + "    public String doSomethingElse() {\n"
                + "        return \"hello world\";\n"
                + "    }\n"
                + "\n"
                + "    /**\n"
                + "     *\n"
                + "     * Comment doStuff(String s)\n"
                + "     */\n"
                + "    public void doStuff(String s) {\n"
                + "        System.out.println(\"do stuff\");\n"
                + "        System.out.println(new EventListener() {\n"
                + "        });\n"
                + "    }\n"
                + "}"));
    }

    public void testPullUpClass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "    \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 3, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "    \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}"));
    }

    public void testPullUp2Iface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + "}"));
        performPullUpIface(src.getFileObject("pullup/PullUpBaseClass.java"), 2, 0, true);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + " void method();\n"
                + "}"));
    }

    public void testPullUpMakeAbs() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.TRUE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public void method() {\n"
                + "        //method body\n"
                + "        System.out.println(\"Hello\");\n"
                + "    }\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "    public abstract void method();\n"
                + "}"));
    }

    public void testPullUpAbsMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public abstract class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public abstract void method();\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "}"));
        performPullUp(src.getFileObject("pullup/PullUpBaseClass.java"), 2, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "\n"
                + "public abstract class PullUpBaseClass extends PullUpSuperClass implements Serializable {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpSuperClass {\n"
                + "    \n"
                + "    public void m2() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    final String field2 = \"const\";\n"
                + "\n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "\n"
                + "    public abstract void method();\n"
                + "}"));
    }

    public void testPullUpAbsMethod2Iface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public abstract void method();\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + "}"));
        performPullUpIface(src.getFileObject("pullup/PullUpBaseClass.java"), 2, 0, true);
        verifyContent(src,
                new File("pullup/PullUpBaseClass.java", "package pullup;\n"
                + "\n"
                + "public abstract class PullUpBaseClass implements PullUpSuperIface {\n"
                + "     \n"
                + "    public String field;\n"
                + "    \n"
                + "    public class InnerClass {\n"
                + "        //class body\n"
                + "        public void method() {\n"
                + "            \n"
                + "        }\n"
                + "    }\n"
                + "   \n"
                + "    public void existing() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void localyReferenced() {\n"
                + "        \n"
                + "    }\n"
                + "    \n"
                + "    private void reference() {\n"
                + "        localyReferenced();\n"
                + "    }\n"
                + "}"),
                new File("pullup/PullUpSuperIface.java", "package pullup;\n"
                + "\n"
                + "public interface PullUpSuperIface {\n"
                + " void method();\n"
                + "}"));
    }

    public void testPullUpInterface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B implements Runnable { public void run() { } }"),
                new File("pullup/B.java", "package pullup; public class B { }"));
        performPullUpImplements(src.getFileObject("pullup/A.java"), 0, -1);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public void run() { } }"),
                new File("pullup/B.java", "package pullup; public class B implements Runnable { }"));
    }
    
    public void testPullUpInterface2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A implements B { }"),
                new File("pullup/B.java", "package pullup; public interface B { }"));
        performPullUpImplements(src.getFileObject("pullup/A.java"), 0, 0, new Problem(true, "ERR_PullUp_MemberTargetType"));
    }

    public void testPullUpTwoClassesUp() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public int i; }"),
                new File("pullup/B.java", "package pullup; public class B extends C { }"),
                new File("pullup/C.java", "package pullup; public class C { }"));
        performPullUpSuper(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B {}"),
                new File("pullup/B.java", "package pullup; public class B extends C { }"),
                new File("pullup/C.java", "package pullup; public class C { public int i; }"));
    }

    public void testPullUpExisting() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public void foo() { } }"),
                new File("pullup/B.java", "package pullup; public class B { public void foo() { } }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE, new Problem(true, "ERR_PullUp_MemberAlreadyExists"));
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B { public void foo() { } }"),
                new File("pullup/B.java", "package pullup; public class B { public void foo() { } }"));
    }

    public void testPullUpLocalyReferenced() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("pullup/A.java", "package pullup; public class A extends B { private void foo() { } private void method() { foo() } }"),
                new File("pullup/B.java", "package pullup; public class B { }"));
        performPullUp(src.getFileObject("pullup/A.java"), 1, Boolean.FALSE);
        verifyContent(src,
                new File("pullup/A.java", "package pullup; public class A extends B {private void method() { foo() } }"),
                new File("pullup/B.java", "package pullup; public class B { protected void foo() { } }"));
    }

    private void performPullUpImplements(FileObject source, final int position, final int supertype, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();

                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);

                TypeMirror superclass;
                if(supertype < 0) {
                    superclass = classEl.getSuperclass();
                } else {
                    superclass = classEl.getInterfaces().get(supertype);
                }
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);

                MemberInfo[] members = new MemberInfo[1];
                TypeMirror implementedInterface = classEl.getInterfaces().get(position);
                members[0] = MemberInfo.create(RefactoringUtils.typeToElement(implementedInterface, info), info, MemberInfo.Group.IMPLEMENTS);

                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(superEl));
                r[0].setMembers(members);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

    private void performPullUpIface(FileObject source, final int position, final int iface, final boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();

                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);

                TypeMirror superclass = classEl.getInterfaces().get(iface);
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);

                MemberInfo[] members = new MemberInfo[1];
                Tree member = classTree.getMembers().get(position);
                Element el = info.getTrees().getElement(new TreePath(classPath, member));
                members[0] = MemberInfo.create(el, info);
                members[0].setMakeAbstract(makeAbstract);

                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(superEl));
                r[0].setMembers(members);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

    private void performPullUp(FileObject source, final int position, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();

                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);

                TypeMirror superclass = classEl.getSuperclass();
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);

                MemberInfo[] members;
                if(position < 0) {
                    List<? extends Tree> classMembers = classTree.getMembers();
                    List<MemberInfo> selectedMembers = new LinkedList<MemberInfo>();
                    for (int i = 0; i < classMembers.size(); i++) {
                        Tree tree = classMembers.get(i);
                        if(!info.getTreeUtilities().isSynthetic(new TreePath(classPath, tree)) ) {
                            Element el = info.getTrees().getElement(new TreePath(classPath, tree));
                            MemberInfo<ElementHandle<Element>> memberInfo = MemberInfo.create(el, info);
                            memberInfo.setMakeAbstract(makeAbstract);
                            selectedMembers.add(memberInfo);
                        }
                    }
                    members = selectedMembers.toArray(new MemberInfo[0]);
                } else {
                    members = new MemberInfo[1];
                    Tree member = classTree.getMembers().get(position);
                    Element el = info.getTrees().getElement(new TreePath(classPath, member));
                    members[0] = MemberInfo.create(el, info);
                    members[0].setMakeAbstract(makeAbstract);
                }
                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(superEl));
                r[0].setMembers(members);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

    private void performPullUpSuper(FileObject source, final int position, final Boolean makeAbstract, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final PullUpRefactoring[] r = new PullUpRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();

                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);

                TypeMirror superclass = classEl.getSuperclass();
                TypeElement superEl = (TypeElement) info.getTypes().asElement(superclass);
                TypeMirror supersuperclass = superEl.getSuperclass();
                TypeElement supersuperEl = (TypeElement) info.getTypes().asElement(supersuperclass);

                MemberInfo[] members = new MemberInfo[1];
                Tree member = classTree.getMembers().get(position);
                Element el = info.getTrees().getElement(new TreePath(classPath, member));
                members[0] = MemberInfo.create(el, info);
                members[0].setMakeAbstract(makeAbstract);

                r[0] = new PullUpRefactoring(TreePathHandle.create(classEl, info));
                r[0].setTargetType(ElementHandle.create(supersuperEl));
                r[0].setMembers(members);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
}
