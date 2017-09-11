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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.JEditorPane;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.JavaDataLoader;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.SharedClassObject;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class GeneratorUtilitiesTest extends NbTestCase {
    
    public GeneratorUtilitiesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        SharedClassObject loader = JavaDataLoader.findObject(JavaDataLoader.class, true);
        SourceUtilsTestUtil.prepareTest(new String[]{"org/netbeans/modules/java/source/resources/layer.xml", "META-INF/generated-layer.xml"}, new Object[]{loader/*, cpp*/});
        JEditorPane.registerEditorKitForContentType("text/x-java", "org.netbeans.modules.editor.java.JavaKit");
        Main.initializeURLFactory();
    }

    private void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    public void testInsertFields() throws Exception {
        performTest("package test;\npublic class Test {\npublic static int i;\nprivate String s;\npublic Test(){\n}\npublic void op(){\n}\nprivate class Nested{\n}\n}\n",
                new InsertMembersTask(34, EnumSet.of(Modifier.PUBLIC), ElementKind.FIELD), new InsertMemberValidator(2));
    }

    public void testInsertStaticFields() throws Exception {
        performTest("package test;\npublic class Test {\npublic static int i;\nprivate String s;\npublic Test(){\n}\npublic void op(){\n}\nprivate class Nested{\n}\n}\n",
                new InsertMembersTask(34, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), ElementKind.FIELD), new InsertMemberValidator(1));
    }

    public void testInsertConstructors() throws Exception {
        performTest("package test;\npublic class Test {\npublic static int i;\nprivate String s;\npublic Test(){\n}\npublic void op(){\n}\nprivate class Nested{\n}\n}\n",
                new InsertMembersTask(34, EnumSet.of(Modifier.PUBLIC), ElementKind.CONSTRUCTOR), new InsertMemberValidator(3));
    }

    public void testInsertMethods() throws Exception {
        performTest("package test;\npublic class Test {\npublic static int i;\nprivate String s;\npublic Test(){\n}\npublic void op(){\n}\nprivate class Nested{\n}\n}\n",
                new InsertMembersTask(34, EnumSet.of(Modifier.PUBLIC), ElementKind.METHOD), new InsertMemberValidator(4));
    }

    public void testInsertStaticMethods() throws Exception {
        performTest("package test;\npublic class Test {\npublic static int i;\nprivate String s;\npublic Test(){\n}\npublic void op(){\n}\nprivate class Nested{\n}\n}\n",
                new InsertMembersTask(34, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), ElementKind.METHOD), new InsertMemberValidator(1));
    }

    public void testInsertNestedClasses() throws Exception {
        performTest("package test;\npublic class Test {\npublic static int i;\nprivate String s;\npublic Test(){\n}\npublic void op(){\n}\nprivate class Nested{\n}\n}\n",
                new InsertMembersTask(34, EnumSet.of(Modifier.PUBLIC), ElementKind.CLASS), new InsertMemberValidator(5));
    }

    public void testInsertStaticNestedClasses() throws Exception {
        performTest("package test;\npublic class Test {\npublic static int i;\nprivate String s;\npublic Test(){\n}\npublic void op(){\n}\nprivate class Nested{\n}\n}\n",
                new InsertMembersTask(34, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), ElementKind.CLASS), new InsertMemberValidator(4));
    }

    public void testImplementAllAbstractMethods1() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\npublic Test(){\n}\n }\n", new AddAllAbstractMethodsTask(54), new RunnableValidator());
    }

    public void testImplementAllAbstractMethods2() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\n }\n", new AddAllAbstractMethodsTask(54), new RunnableValidator());
    }

    public void testImplementAllAbstractMethods3() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\npublic void testMethod() {\n} }\n", new AddAllAbstractMethodsTask(54), new RunnableValidator());
    }

    public void testImplementAllAbstractMethods4() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\npublic Test(){\n}\npublic void testMethod() {\n} }\n", new AddAllAbstractMethodsTask(54), new RunnableValidator());
    }

    public void testImplementAllAbstractMethods5() throws Exception {
        performTest("package test;import java.util.concurrent.*;\npublic class Test implements Future<String>{\npublic Test(){\n} }\n", new AddAllAbstractMethodsTask(89), new SimpleFutureValidator("java.lang.String"));
    }

    public void testImplementAllAbstractMethods6() throws Exception {
        performTest("package test;import java.util.concurrent.*;\npublic class Test implements Future<java.util.List<? extends java.util.List>>{\npublic Test(){\n} }\n", new AddAllAbstractMethodsTask(123), new FutureValidator() {
            protected TypeMirror returnType(CompilationInfo info) {
                return info.getTreeUtilities().parseType("java.util.List<? extends java.util.List>", info.getElements().getTypeElement("test.Test"));
            }
        });
    }

    public void testImplementAllAbstractMethods7() throws Exception {
        performTest("package test;\npublic class Test extends java.util.AbstractList{\npublic Test(){\n} }\n", new AddAllAbstractMethodsTask(64), null);
    }

    /** issue #85966
     */
    public void testImplementAllAbstractMethods8() throws Exception {
        performTest("package test;\npublic class Test implements XX {\npublic Test(){\n} }\ninterface XX {\npublic void test(String ... a);}", new AddAllAbstractMethodsTask(42), new Validator() {
            public void validate(CompilationInfo info) {
                TypeElement clazz = info.getElements().getTypeElement("test.Test");
                ExecutableElement method = ElementFilter.methodsIn(clazz.getEnclosedElements()).get(0);
                assertTrue(method.isVarArgs());
            }
        });
    }

    public void testImplementAllAbstractMethods9() throws Exception {
        performTest("package test;\npublic class Test implements java.util.concurrent.ExecutorService {\npublic Test(){\n} }\n", new AddAllAbstractMethodsTask(30), null);
    }

    public void testImplementAllAbstractMethodsa() throws Exception {
        performTest("package test;\npublic class Test implements XX {\npublic Test(){\n} }\ninterface XX {public <T extends java.util.List> void test(T t);}", new AddAllAbstractMethodsTask(30), null);
    }

    public void testImplementAllAbstractMethodsb() throws Exception {
        performTest("package test;\npublic class Test implements java.util.List{\npublic Test(){\n} }\n", new AddAllAbstractMethodsTask(30), null);
    }

    public void testImplementAllAbstractMethodsc() throws Exception {
        performTest("package test;\npublic class Test implements java.util.List<String>{\npublic Test(){\n} }\n", new AddAllAbstractMethodsTask(30), null);
    }

    public void testImplementAllAbstractMethodsd() throws Exception {
        performTest("package test;\npublic class Test implements B {\npublic Test(){\n} }\ninterface A {\npublic Number f();}\ninterface B extends A {\npublic Integer f();}", new AddAllAbstractMethodsTask(30), null);
    }

    public void testOverrideAnnotation1() throws Exception {
        performTest("package test;\npublic class Test extends C implements B { }\ninterface A {public void test1(); public void test4();}\ninterface B {public void test2();}\nabstract class C implements A {public abstract void test3(); public abstract void test4();}\n", "1.5", new AddAllAbstractMethodsTask(30), new Validator() {
            public void validate(CompilationInfo info) {
                ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);

                for (Tree member : ct.getMembers()) {
                    MethodTree m = (MethodTree) member;

                    if ("test1".equals(m.getName().toString())) {
                        assertTrue(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test2".equals(m.getName().toString())) {
                        assertTrue(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test3".equals(m.getName().toString())) {
                        assertFalse(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test4".equals(m.getName().toString())) {
                        assertFalse(m.getModifiers().getAnnotations().isEmpty());
                    }
                }
            }
        });
    }

    public void testOverrideAnnotation2() throws Exception {
        performTest("package test;\npublic class Test extends C implements B { }\ninterface A {public void test1(); public void test4();}\ninterface B {public void test2();}\nabstract class C implements A {public abstract void test3(); public abstract void test4();}\n", "1.6", new AddAllAbstractMethodsTask(30), new Validator() {
            public void validate(CompilationInfo info) {
                ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);

                for (Tree member : ct.getMembers()) {
                    MethodTree m = (MethodTree) member;

                    if ("test1".equals(m.getName().toString())) {
                        assertFalse(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test2".equals(m.getName().toString())) {
                        assertFalse(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test3".equals(m.getName().toString())) {
                        assertFalse(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test4".equals(m.getName().toString())) {
                        assertFalse(m.getModifiers().getAnnotations().isEmpty());
                    }
                }
            }
        });
    }

    public void testOverrideAnnotation3() throws Exception {
        performTest("package test;\npublic class Test extends C implements B { }\ninterface A {public void test1(); public void test4();}\ninterface B {public void test2();}\nabstract class C implements A {public abstract void test3(); public abstract void test4();}\n", "1.4", new AddAllAbstractMethodsTask(30), new Validator() {
            public void validate(CompilationInfo info) {
                ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);

                for (Tree member : ct.getMembers()) {
                    MethodTree m = (MethodTree) member;

                    if ("test1".equals(m.getName().toString())) {
                        assertTrue(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test2".equals(m.getName().toString())) {
                        assertTrue(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test3".equals(m.getName().toString())) {
                        assertTrue(m.getModifiers().getAnnotations().isEmpty());
                    }

                    if ("test4".equals(m.getName().toString())) {
                        assertTrue(m.getModifiers().getAnnotations().isEmpty());
                    }
                }
            }
        });
    }

    public void testOverrideMethods1() throws Exception {
        performTest("package test;\npublic class Test {\npublic Test(){\n}\n }\n", new SimpleOverrideMethodsTask(34), new CloneAndToStringValidator());
    }

    public void testOverrideMethods2() throws Exception {
        performTest("package test;\npublic class Test {\n }\n", new SimpleOverrideMethodsTask(34), new CloneAndToStringValidator());
    }

    public void testOverrideMethods3() throws Exception {
        performTest("package test;\npublic class Test {\npublic void testMethod() {\n} }\n", new SimpleOverrideMethodsTask(34), new CloneAndToStringValidator());
    }

    public void testOverrideMethods4() throws Exception {
        performTest("package test;\npublic class Test {\npublic Test(){\n}\npublic void testMethod() {\n} }\n", new SimpleOverrideMethodsTask(34), new CloneAndToStringValidator());
    }

    public void testOverrideMethods5() throws Exception {
        performTest("package test;\npublic class Test extends XX<Number> {\npublic Test(){\n} }\nclass XX<T> {\npublic void test(T ... a) {}}", new OverrideMethodsTask(30), new Validator() {
            public void validate(CompilationInfo info) {
                TypeElement clazz = info.getElements().getTypeElement("test.Test");
                ExecutableElement method = ElementFilter.methodsIn(clazz.getEnclosedElements()).get(0);
                assertTrue(method.getSimpleName().contentEquals("test"));
                TypeElement te = info.getElements().getTypeElement("java.lang.Number");
                assertEquals(1, method.getParameters().size());
                TypeMirror paramType = method.getParameters().get(0).asType();
                assertNotNull(paramType);
                assertTrue(paramType.getKind() == TypeKind.ARRAY);
                assertTrue(info.getTypes().isSameType(te.asType(), ((ArrayType)paramType).getComponentType()));
                assertTrue(method.isVarArgs());
            }
        });
    }

    public void testConstructor1() throws Exception {
        performTest("package test;\npublic class Test {\nprivate int test;\n}\n", new ConstructorTask(34), new ConstructorValidator());
    }

    public void testConstructor2() throws Exception {
        performTest("package test;\npublic class Test extends XX {\nprivate int test;\n}\nclass XX {\npublic XX(boolean b){\n}\n}\n", new ConstructorTask(30), new ConstructorValidator());
    }

    public void testConstructor197192() throws Exception {
        performTest("package test;\npublic class Test extends XX {\nprivate int test;\n}\nclass XX {\npublic XX(String... args){\n}\n}\n", new ConstructorTask(30), new ConstructorValidator());
    }

    public void testConstructor100341() throws Exception {
        performTest("package test;\npublic class Test extends java.util.ArrayList<String> {\n}\n", new ALConstructorTask(30), null);
    }

    public void testConstructor134673a() throws Exception {
        performTest("package test;\npublic class Test extends java.io.RandomAccessFile {\n}\n", new ConstructorTask(30, 2, 0), null);
    }

    public void testConstructor134673b() throws Exception {
        performTest("package test;\npublic class Test extends G<java.io.IOException> {\n} class G<T extends Throwable> {public G() throws T {}}\n", new ConstructorTask(30), null);
    }

    public void testGetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate int test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, true), new GetterSetterValidator(true));
    }

    public void testBooleanGetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate boolean test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, true), new GetterSetterValidator(true));
    }

    public void testStaticGetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate static int test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, true), new GetterSetterValidator(true));
    }

    public void testStaticBooleanGetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate static boolean test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, true), new GetterSetterValidator(true));
    }

    public void testSetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate int test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, false), new GetterSetterValidator(false));
    }

    public void testBooleanSetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate boolean test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, false), new GetterSetterValidator(false));
    }

    public void testStaticSetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate static int test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, false), new GetterSetterValidator(false));
    }

    public void testStaticBooleanSetter() throws Exception {
        performTest("package test;\npublic class Test {\nprivate static boolean test;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, false), new GetterSetterValidator(false));
    }

    public void testCreateMethod() throws Exception {
        performTest("package test;\npublic class Test { }\n", "1.5", new CreateMethodTask(34), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(info.getDiagnostics().toString(), 1, info.getDiagnostics().size());

                for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getTypeElement("test.Test").getEnclosedElements())) {
                    assertEquals("toArray", ee.getSimpleName().toString());
                    assertEquals(1, ee.getTypeParameters().size());
                    return ;
                }

                fail("toArray method not found");
            }
        }, false);
    }

    public void testAddImports1() throws Exception {
        performTest("package test;\nimport java.util.List;\nimport javax.swing.JLabel;\npublic class Test { }\n", "1.5", new AddImportsTask("javax.swing.JTable", "java.util.ArrayList"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(4, imports.size());
                assertEquals("java.util.ArrayList", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.List", imports.get(1).getQualifiedIdentifier().toString());
                assertEquals("javax.swing.JLabel", imports.get(2).getQualifiedIdentifier().toString());
                assertEquals("javax.swing.JTable", imports.get(3).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports2() throws Exception {
        performTest("package test;\nimport java.util.List;\nimport javax.swing.JLabel;\npublic class Test { }\n", "1.5", new AddImportsTask("javax.swing.*", "java.util.ArrayList", "java.util.*"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(2, imports.size());
                assertEquals("java.util.*", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("javax.swing.*", imports.get(1).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports3() throws Exception {
        performTest("package test;\nimport java.util.List;\nimport javax.swing.*;\npublic class Test { }\n", "1.5", new AddImportsTask("java.util.ArrayList", "javax.swing.JTable"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(3, imports.size());
                assertEquals("java.util.ArrayList", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.List", imports.get(1).getQualifiedIdentifier().toString());
                assertEquals("javax.swing.*", imports.get(2).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports4() throws Exception {
        performTest("package test;\nimport java.util.List;\npublic class Test { }\n", "1.5", new AddImportsTask("java.lang.Math.max"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(2, imports.size());
                assertEquals("java.lang.Math.max", imports.get(0).getQualifiedIdentifier().toString());
                assertTrue(imports.get(0).isStatic());
                assertEquals("java.util.List", imports.get(1).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports5() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("allowConvertToStarImport", true);
        performTest("package test;\nimport java.util.AbstractList;\nimport java.util.ArrayList;\nimport java.util.List;\nimport java.util.Vector;\npublic class Test { }\n", "1.5", new AddImportsTask("java.util.Collection"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(1, imports.size());
                assertEquals("java.util.*", imports.get(0).getQualifiedIdentifier().toString());
            }
        }, false);
        preferences.putBoolean("allowConvertToStarImport", false);
    }

    public void testAddImports6() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("allowConvertToStarImport", true);
        performTest("package test;\nimport java.util.AbstractList;\nimport java.util.ArrayList;\nimport java.util.List;\nimport java.util.Vector;\npublic class Test { }\n", "1.5", new AddImportsTask("java.util.Collection"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(1, imports.size());
                assertEquals("java.util.*", imports.get(0).getQualifiedIdentifier().toString());
            }
        }, false);
        preferences.putBoolean("allowConvertToStarImport", false);
    }

    public void testAddImports7() throws Exception {
        performTest("package test;\nimport java.util.*;\npublic class Test {\n}\n", "1.5", new AddImportsTask("java.awt.*"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(2, imports.size());
                assertEquals("java.awt.*", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.*", imports.get(1).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports8() throws Exception {
        performTest("package test;\nimport java.util.*;\npublic class Test {\nprivate void op(List list) {\nint size = list.size();\n}\n}\n", "1.5", new AddImportsTask("java.awt.*"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(3, imports.size());
                assertEquals("java.awt.*", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.*", imports.get(1).getQualifiedIdentifier().toString());
                assertEquals("java.util.List", imports.get(2).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports9() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("allowConvertToStarImport", true);
        performTest("package test;\nimport java.awt.*;\nimport java.util.List;\npublic class Test {\nprivate void op(List list) {\nint size = list.size();\n}\n}\n", "1.5", new AddImportsTask("java.util.AbstractList", "java.util.ArrayList", "java.util.Collection", "java.util.Collections"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(3, imports.size());
                assertEquals("java.awt.*", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.*", imports.get(1).getQualifiedIdentifier().toString());
                assertEquals("java.util.List", imports.get(2).getQualifiedIdentifier().toString());
            }
        }, false);
        preferences.putBoolean("allowConvertToStarImport", false);
    }

    public void testAddImports9a() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("allowConvertToStarImport", true);
        performTest("package test;\npublic class Test {\nprivate void op(List list) {\nint size = list.size();\n}\n}\n", "1.5", new AddImportsTask("java.util.AbstractList", "java.util.ArrayList", "java.util.Collection", "java.util.Collections", "java.util.List", "java.awt.Component", "java.awt.Container", "java.awt.Dialog", "java.awt.Graphics", "java.awt.Menu"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(3, imports.size());
                assertEquals("java.awt.*", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.*", imports.get(1).getQualifiedIdentifier().toString());
                assertEquals("java.util.List", imports.get(2).getQualifiedIdentifier().toString());
            }
        }, false);
        preferences.putBoolean("allowConvertToStarImport", false);
    }

    public void testAddImports10() throws Exception {
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("allowConvertToStaticStarImport", true);
        performTest("package test;\nimport static java.lang.Math.max;\npublic class Test { }\n", "1.5", new AddImportsTask("java.lang.Math.abs", "java.lang.Math.min"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(1, imports.size());
                assertEquals("java.lang.Math.*", imports.get(0).getQualifiedIdentifier().toString());
                assertTrue(imports.get(0).isStatic());
            }
        }, false);
        preferences.putBoolean("allowConvertToStaticStarImport", false);
    }

    public void testAddImports11() throws Exception {
        performTest("package test;\nimport java.util.AbstractList;\nimport java.util.ArrayList;\nimport java.util.List;\nimport java.util.Vector;\npublic class Test { }\n", "1.5", new AddImportsTask("java.util.Map.Entry"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(5, imports.size());
                assertEquals("java.util.AbstractList", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.ArrayList", imports.get(1).getQualifiedIdentifier().toString());
                assertEquals("java.util.List", imports.get(2).getQualifiedIdentifier().toString());
                assertEquals("java.util.Map.Entry", imports.get(3).getQualifiedIdentifier().toString());
                assertEquals("java.util.Vector", imports.get(4).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports12() throws Exception {
        performTest("package test;\nimport Bar;\nimport org.me.Foo;\npublic class Test { }\n", "1.5", new AddImportsTask("java.util.List"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(2, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(3, imports.size());
                assertEquals("Bar.<error>", imports.get(0).getQualifiedIdentifier().toString());
                assertEquals("java.util.List", imports.get(1).getQualifiedIdentifier().toString());
                assertEquals("org.me.Foo", imports.get(2).getQualifiedIdentifier().toString());
            }
        }, false);
    }

    public void testAddImports13() throws Exception {
        performTest("package test;\npublic class Test { }\n", "1.5", new AddImportsTask("test.Test"), new Validator() {
            public void validate(CompilationInfo info) {
                assertEquals(0, info.getDiagnostics().size());
                List<? extends ImportTree> imports = info.getCompilationUnit().getImports();
                assertEquals(0, imports.size());
            }
        }, false);
    }

    public void testGetterNamingConvention0() throws Exception {//#165241
        performTest("package test;\npublic class Test {\nprivate int eMai;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, false), new Validator() {

            public void validate(CompilationInfo info) {
                ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);

                for (Tree member : ct.getMembers()) {
                    if (member.getKind() == Kind.METHOD) {
                        String name = ((MethodTree) member).getName().toString();
                        if (!name.equals("<init>")) {
                            assertEquals(name, "seteMai");
                        }
                    }
                }
            }
        });
    }

    public void testGetterNamingConvention1() throws Exception {//#165241
        performTest("package test;\npublic class Test {\nprivate int emai;\npublic Test(){\n}\n }\n", new GetterSetterTask(34, false), new Validator() {

            public void validate(CompilationInfo info) {
                ClassTree ct = (ClassTree) info.getCompilationUnit().getTypeDecls().get(0);

                for (Tree member : ct.getMembers()) {
                    if (member.getKind() == Kind.METHOD) {
                        String name = ((MethodTree) member).getName().toString();
                        if (!name.equals("<init>")) {
                            assertEquals(name, "setEmai");
                        }
                    }
                }
            }
        });
    }

    public void testCommentsCopyingNull() throws Exception {//#165241
        performTest("package test;\npublic abstract class Test {\npublic abstract void test();\n }\n", new Task<WorkingCopy>() {
            @Override public void run(WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                ClassTree clazz = (ClassTree) parameter.getCompilationUnit().getTypeDecls().get(0);
                MethodTree mt = (MethodTree) clazz.getMembers().get(1);
                BlockTree nue = parameter.getTreeMaker().Block(Collections.<StatementTree>emptyList(), false);
                GeneratorUtilities.get(parameter).copyComments(mt.getBody(), nue, true);
            }
        }, new Validator() {
            public void validate(CompilationInfo info) {}
        });
    }

    public static interface Validator {

        public void validate(CompilationInfo info);

    }

    private static class InsertMembersTask implements CancellableTask<WorkingCopy> {

        private int offset;
        private Set<Modifier> modifiers;
        private ElementKind kind;

        public InsertMembersTask(int offset, Set<Modifier> modifiers, ElementKind kind) {
            this.offset = offset;
            this.modifiers = modifiers;
            this.kind = kind;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            TreeMaker maker = copy.getTreeMaker();
            ArrayList<Tree> members = new ArrayList<Tree>(1);
            switch(kind) {
            case FIELD:
                members.add(maker.Variable(maker.Modifiers(modifiers), "test", maker.PrimitiveType(TypeKind.INT), null));
                break;
            case METHOD:
                members.add(maker.Method(maker.Modifiers(modifiers), "test", maker.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(), maker.Block(Collections.<StatementTree>emptyList(), false),
                        null));
                break;
            case CONSTRUCTOR:
                members.add(maker.Method(maker.Modifiers(modifiers), "<init>", null,
                        Collections.<TypeParameterTree>emptyList(), Collections.singletonList(maker.Variable(
                        maker.Modifiers(EnumSet.noneOf(Modifier.class)), "s", maker.Identifier("String"), null)),
                        Collections.<ExpressionTree>emptyList(), maker.Block(Collections.<StatementTree>emptyList(), false),
                        null));
                break;
            case CLASS:
                members.add(maker.Class(maker.Modifiers(modifiers), "test", Collections.<TypeParameterTree>emptyList(),
                        null, Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList()));
                break;
            }
            ClassTree newCt = utilities.insertClassMembers(ct, members);
            copy.rewrite(ct, newCt);
        }
    }

    private final class InsertMemberValidator implements Validator {

        private int testIdx;

        public InsertMemberValidator(int testIdx) {
            this.testIdx = testIdx;
        }

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            ClassTree ct = info.getTrees().getTree(test);
            assertNotNull(ct);

            int foundTestIdx = -1;

            int idx = 0;
            for (Tree t : ct.getMembers()) {
                Name name = null;
                switch(t.getKind()) {
                case VARIABLE:
                    name = ((VariableTree)t).getName();
                    break;
                case METHOD:
                    name = ((MethodTree)t).getName();
                    break;
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    name = ((ClassTree)t).getSimpleName();
                    break;
                }
                if (name != null) {
                    if (name.contentEquals("test")) {
                        assertEquals(-1, foundTestIdx);
                        foundTestIdx = idx;
                    } else if (name.contentEquals("<init>") && ((MethodTree)t).getParameters().size() > 0) {
                        assertEquals(-1, foundTestIdx);
                        foundTestIdx = idx;
                    }
                }
                idx++;
            }

            assertEquals(testIdx, foundTestIdx);
        }

    }

    private static class AddAllAbstractMethodsTask implements CancellableTask<WorkingCopy> {

        private int offset;

        public AddAllAbstractMethodsTask(int offset) {
            this.offset = offset;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
            assertNotNull(te);
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            ClassTree newCt = utilities.insertClassMembers(ct, utilities.createAllAbstractMethodImplementations(te));
            copy.rewrite(ct, newCt);
        }
    }

    private final class RunnableValidator implements Validator {

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");

            boolean foundRunMethod = false;

            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("run".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(foundRunMethod);
                        foundRunMethod = true;
                    }
                }
            }

            assertTrue(foundRunMethod);
        }

    }

    private final class SimpleFutureValidator extends FutureValidator {

        private String returnTypeName;

        public SimpleFutureValidator(String returnTypeName) {
            this.returnTypeName = returnTypeName;
        }

        protected TypeMirror returnType(CompilationInfo info) {
            TypeElement returnTypeElement = info.getElements().getTypeElement(returnTypeName);

            return returnTypeElement.asType();
        }
    }

    private abstract class FutureValidator implements Validator {

        protected abstract TypeMirror returnType(CompilationInfo info);

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            TypeMirror returnType = returnType(info);

            boolean hasShortGet = false;
            boolean hasLongGet = false;

            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("get".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(hasShortGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasShortGet = true;
                    }
                    if (ee.getParameters().size() == 2) {
                        assertFalse(hasLongGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasLongGet = true;
                    }
                }
            }

            assertTrue(hasShortGet);
            assertTrue(hasLongGet);
        }

    }

    private static class SimpleOverrideMethodsTask implements CancellableTask<WorkingCopy> {

        private int offset;

        public SimpleOverrideMethodsTask(int offset) {
            this.offset = offset;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
            assertNotNull(te);
            ArrayList<ExecutableElement> methods = new ArrayList<ExecutableElement>(2);
            TypeElement object = copy.getElements().getTypeElement("java.lang.Object");
            assertNotNull(object);
            for (ExecutableElement method : ElementFilter.methodsIn(object.getEnclosedElements())) {
                if (method.getSimpleName().contentEquals("clone"))
                    methods.add(method);
                else if (method.getSimpleName().contentEquals("toString"))
                    methods.add(method);
            }
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            ClassTree newCt = utilities.insertClassMembers(ct, utilities.createOverridingMethods(te, methods));
            copy.rewrite(ct, newCt);
        }
    }

    private static class OverrideMethodsTask implements CancellableTask<WorkingCopy> {

        private int offset;

        public OverrideMethodsTask(int offset) {
            this.offset = offset;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
            assertNotNull(te);
            ArrayList<ExecutableElement> methods = new ArrayList<ExecutableElement>(1);
            TypeElement sup = (TypeElement)((DeclaredType)te.getSuperclass()).asElement();
            assertNotNull(sup);
            for (ExecutableElement method : ElementFilter.methodsIn(sup.getEnclosedElements())) {
                if (method.getSimpleName().contentEquals("test"))
                    methods.add(method);
            }
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            ClassTree newCt = utilities.insertClassMembers(ct, utilities.createOverridingMethods(te, methods));
            copy.rewrite(ct, newCt);
        }
    }

    private final class CloneAndToStringValidator implements Validator {

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");

            boolean foundCloneMethod = false;
            boolean foundToStringMethod = false;

            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if (ee.getSimpleName().contentEquals("clone")) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(foundCloneMethod);
                        foundCloneMethod = true;
                    }
                } else if (ee.getSimpleName().contentEquals("toString")) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(foundToStringMethod);
                        foundToStringMethod = true;
                    }
                }
            }

            assertTrue(foundCloneMethod);
            assertTrue(foundToStringMethod);
        }

    }

    private static class ConstructorTask implements CancellableTask<WorkingCopy> {

        private final int numCtors;
        private final int ctorToUse;
        private final int offset;

        public ConstructorTask(int offset) {
            this(offset, 1, 0);
        }

        public ConstructorTask(int offset, int numCtors, int ctorToUse) {
            this.offset = offset;
            this.numCtors = numCtors;
            this.ctorToUse = ctorToUse;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
            assertNotNull(te);
            List<? extends VariableElement> vars = ElementFilter.fieldsIn(te.getEnclosedElements());
            TypeElement sup = (TypeElement)((DeclaredType)te.getSuperclass()).asElement();
            assertNotNull(sup);
            List<? extends ExecutableElement> ctors = sup.getQualifiedName().contentEquals("java.lang.Object")
                    ? null : ElementFilter.constructorsIn(sup.getEnclosedElements());
            if (ctors != null)
                assertEquals(numCtors, ctors.size());
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            ClassTree newCt = utilities.insertClassMember(ct, utilities.createConstructor(te, vars, ctors != null ? ctors.get(ctorToUse) : null));
            copy.rewrite(ct, newCt);
        }
    }

    private static class ALConstructorTask implements CancellableTask<WorkingCopy> {

        private int offset;

        public ALConstructorTask(int offset) {
            this.offset = offset;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
            assertNotNull(te);
            List<? extends VariableElement> vars = ElementFilter.fieldsIn(te.getEnclosedElements());
            TypeElement sup = (TypeElement)((DeclaredType)te.getSuperclass()).asElement();
            assertNotNull(sup);
            List<? extends ExecutableElement> ctors = ElementFilter.constructorsIn(sup.getEnclosedElements());
            ExecutableElement found = null;
            for (ExecutableElement ee : ctors) {
                if (ee.getParameters().size() != 1) {
                    continue;
                }

                if (ee.getParameters().get(0).asType().getKind() == TypeKind.DECLARED) {
                    found = ee;
                    break;
                }
            }
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            ClassTree newCt = utilities.insertClassMember(ct, utilities.createConstructor(te, vars, found));
            copy.rewrite(ct, newCt);
        }
    }

    private final class ConstructorValidator implements Validator {

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            VariableElement var = ElementFilter.fieldsIn(test.getEnclosedElements()).get(0);
            TypeElement sup = (TypeElement)((DeclaredType)test.getSuperclass()).asElement();
            ExecutableElement supCtor = sup.getQualifiedName().contentEquals("java.lang.Object")
                    ? null : ElementFilter.constructorsIn(sup.getEnclosedElements()).get(0);

            List<? extends ExecutableElement> ctors = ElementFilter.constructorsIn(test.getEnclosedElements());
            assertEquals(1, ctors.size());
            ExecutableElement ctor = ctors.get(0);

            assertEquals(supCtor == null ? 1 : 2, ctor.getParameters().size());
            if (supCtor != null)
                assertEquals(supCtor.isVarArgs(), ctor.isVarArgs());
        }

    }

    private static class GetterSetterTask implements CancellableTask<WorkingCopy> {

        private int offset;
        private boolean getter;

        public GetterSetterTask(int offset, boolean getter) {
            this.offset = offset;
            this.getter = getter;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
            assertNotNull(te);
            List<? extends VariableElement> vars = ElementFilter.fieldsIn(te.getEnclosedElements());
            assertEquals(1, vars.size());
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            ClassTree newCt = utilities.insertClassMember(ct, getter
                    ? utilities.createGetter(te, vars.get(0))
                    : utilities.createSetter(te, vars.get(0)));
            copy.rewrite(ct, newCt);
        }
    }

    private final class GetterSetterValidator implements Validator {

        private boolean getter;

        public GetterSetterValidator(boolean getter) {
            this.getter = getter;
        }

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            VariableElement var = ElementFilter.fieldsIn(test.getEnclosedElements()).get(0);

            List<? extends ExecutableElement> methods = ElementFilter.methodsIn(test.getEnclosedElements());
            assertEquals(1, methods.size());
            ExecutableElement method = methods.get(0);

            TypeMirror type = info.getTypes().asMemberOf((DeclaredType)test.asType(), var);
            if (getter) {
                assertTrue(info.getTypes().isSameType(type, method.getReturnType()));
                assertEquals(type.getKind() == TypeKind.BOOLEAN ? "isTest" : "getTest", method.getSimpleName().toString());
                assertEquals(0, method.getParameters().size());
            } else {
                assertTrue(info.getTypes().isSameType(info.getTypes().getNoType(TypeKind.VOID), method.getReturnType()));
                assertEquals("setTest", method.getSimpleName().toString());
                assertEquals(1, method.getParameters().size());
                assertTrue(info.getTypes().isSameType(type, method.getParameters().get(0).asType()));
            }
        }

    }

    private static class CreateMethodTask implements CancellableTask<WorkingCopy> {

        private int offset;

        public CreateMethodTask(int offset) {
            this.offset = offset;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            TreePath tp = copy.getTreeUtilities().pathFor(offset);
            assertTrue(TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind()));
            ClassTree ct = (ClassTree)tp.getLeaf();
            TypeElement te = (TypeElement)copy.getTrees().getElement(tp);
            DeclaredType dt = (DeclaredType) copy.getTreeUtilities().parseType("java.util.List<java.lang.String>", te);
            TypeElement list = copy.getElements().getTypeElement("java.util.List");
            ExecutableElement ee = null;
            for (ExecutableElement m : ElementFilter.methodsIn(list.getEnclosedElements())) {
                if (m.getSimpleName().contentEquals("toArray") && !m.getTypeParameters().isEmpty()) {
                    ee = m;
                }
            }
            assertNotNull(ee);
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            ClassTree newCt = utilities.insertClassMembers(ct, Collections.singletonList(utilities.createMethod(dt, ee)));
            copy.rewrite(ct, newCt);
        }
    }

    private static class AddImportsTask implements CancellableTask<WorkingCopy> {

        private String[] toImport;
        
        public AddImportsTask(String... toImport) {
            this.toImport = toImport;
        }

        public void cancel() {
        }

        public void run(WorkingCopy copy) throws Exception {
            copy.toPhase(JavaSource.Phase.RESOLVED);
            CompilationUnitTree cut = copy.getCompilationUnit();
            Elements elements = copy.getElements();
            Set<Element> imports = new HashSet<Element>();
            for (String imp : toImport) {
                if (imp.endsWith(".*"))
                    imp = imp.substring(0, imp.length() - 2);
                Element el = elements.getTypeElement(imp);
                if (el == null)
                    el = elements.getPackageElement(imp);
                if (el == null) {
                    int idx = imp.lastIndexOf('.');
                    if (idx >= 0) {
                        el = elements.getTypeElement(imp.substring(0, idx));
                        if (el != null) {
                            String name = imp.substring(idx + 1);
                            for (Element ee : el.getEnclosedElements()) {
                                if (name.contentEquals(ee.getSimpleName())) {                                
                                    el = ee;
                                    break;
                                }
                            }
                        }
                    }
                }
                assertNotNull(el);
                imports.add(el);
            }
            GeneratorUtilities utilities = GeneratorUtilities.get(copy);
            assertNotNull(utilities);
            CompilationUnitTree newCut = utilities.addImports(cut, imports);
            copy.rewrite(cut, newCut);
        }
    }

    private void performTest(String sourceCode, final Task<WorkingCopy> task, final Validator validator) throws Exception {
        performTest(sourceCode, "1.5", task, validator);
    }

    private void performTest(String sourceCode, String sourceLevel, final Task<WorkingCopy> task, final Validator validator) throws Exception {
        performTest(sourceCode, sourceLevel, task, validator, true);
    }

    private void performTest(String sourceCode, String sourceLevel, final Task<WorkingCopy> task, final Validator validator, final boolean requireNoErrors) throws Exception {
        performTest("test/Test.java", sourceCode, sourceLevel, task, validator, requireNoErrors);
    }

    private void performTest(String filePath, String sourceCode, String sourceLevel, final Task<WorkingCopy> task, final Validator validator, final boolean requireNoErrors) throws Exception {
        FileObject root = makeScratchDir(this);

        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");

        FileObject source = FileUtil.createData(sourceDir, filePath);

        writeIntoFile(source, sourceCode);

        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        SourceUtilsTestUtil.setSourceLevel(source, sourceLevel);
        
        JavaSource js = JavaSource.forFileObject(source);

        ModificationResult result = js.runModificationTask(task);

        result.commit();

        js.runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);

                if (requireNoErrors) {
                    assertEquals(controller.getDiagnostics().toString(), 0, controller.getDiagnostics().size());
                }

                if (validator != null)
                    validator.validate(controller);
            }
        }, true);
    }

    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }

    public void testimportFQNs126796() throws Exception {
        String sourceCode = "package test;\n" +
                            "public class Test{\n" +
                            "     public void test(Class<? extends CharSequence> c) {}\n" +
                            "}\n";
        FileObject root = makeScratchDir(this);

        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");

        FileObject source = sourceDir.createFolder("test").createData("Test.java");

        writeIntoFile(source, sourceCode);

        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);

        JavaSource js = JavaSource.forFileObject(source);

        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                GeneratorUtilities.get(copy).importFQNs(copy.getCompilationUnit());
            }
        });
    }

    public void testImportFQNs114623() throws Exception {
        String sourceCode = "package test;\n" +
                            "public class Test{\n" +
                            "     public void test() {\n" +
                            "          java.util.List l = java.util.Collections.emptyList();\n" +
                            "     }\n" +
                            "}\n";
        FileObject root = makeScratchDir(this);

        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");

        FileObject source = sourceDir.createFolder("test").createData("Test.java");

        writeIntoFile(source, sourceCode);

        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);

        JavaSource js = JavaSource.forFileObject(source);

        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).importFQNs(copy.getCompilationUnit()));
            }
        }).commit();

        String actual = TestUtilities.copyFileToString(FileUtil.toFile(source));
        String golden = "package test;\n\n" +
                        "import java.util.Collections;\n" +
                        "import java.util.List;\n\n" +
                        "public class Test{\n" +
                        "     public void test() {\n" +
                        "          List l = Collections.emptyList();\n" +
                        "     }\n" +
                        "}\n";
        assertEquals(actual, golden);
    }

    public void testImportFQNs175735() throws Exception {
        String sourceCode = "package test;\n" +
                            "public class Test{\n" +
                            "    public void test() {\n" +
                            "        E e = null;\n" +
                            "        switch (e) {\n" +
                            "            case A: break;\n" +
                            "        }\n" +
                            "    }\n" +
                            "}\n";
        FileObject root = makeScratchDir(this);

        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");

        FileObject source = FileUtil.createFolder(sourceDir, "test").createData("Test.java");

        writeIntoFile(source, sourceCode);

        FileObject e = FileUtil.createFolder(sourceDir, "test").createData("E.java");

        writeIntoFile(e, "package test;\n" +
                         "enum E {\n" +
                         "    A;" +
                         "}\n");

        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        SourceUtilsTestUtil.compileRecursively(sourceDir);

        JavaSource js = JavaSource.forFileObject(source);

        js.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);

                copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).importFQNs(copy.getCompilationUnit()));
            }
        }).commit();

        String actual = TestUtilities.copyFileToString(FileUtil.toFile(source));
        String golden = "package test;\n" +
                        "public class Test{\n" +
                        "    public void test() {\n" +
                        "        E e = null;\n" +
                        "        switch (e) {\n" +
                        "            case A: break;\n" +
                        "        }\n" +
                        "    }\n" +
                        "}\n";
        assertEquals(golden, actual);
    }

    public void testAddAnnotationAttributeValue1() throws Exception {
        performTest("package test;\npublic class Test { }\n",
                    new AddAnnotationAttributeValue("java.lang.SuppressWarnings", "value", "\"foobar\""),
                    new ContentValidator("package test;\n@SuppressWarnings(\"foobar\")\npublic class Test { }\n"));
    }

    public void testAddAnnotationAttributeValue2() throws Exception {
        performTest("package test;\n@SuppressWarnings(\"w\")\npublic class Test { }\n",
                    new AddAnnotationAttributeValue("java.lang.SuppressWarnings", "value", "\"foobar\""),
                    new ContentValidator("package test;\n@SuppressWarnings({\"w\", \"foobar\"})\npublic class Test { }\n"));
    }

    public void testAddAnnotationAttributeValue3() throws Exception {
        performTest("package test;\n@SuppressWarnings({\"w1\", \"w2\"})\npublic class Test { }\n",
                    new AddAnnotationAttributeValue("java.lang.SuppressWarnings", "value", "\"foobar\""),
                    new ContentValidator("package test;\n@SuppressWarnings({\"w1\", \"w2\", \"foobar\"})\npublic class Test { }\n"));
    }

    public void testAddAnnotationAttributeValue4() throws Exception {
        performTest("package test;\npublic class Test { }\n",
                    new AddAnnotationAttributeValue("java.lang.SuppressWarnings", "value", "\"foo\"", "\"bar\""),
                    new ContentValidator("package test;\n@SuppressWarnings({\"foo\", \"bar\"})\npublic class Test { }\n"));
    }

    public void testAddAnnotationAttributeValue5() throws Exception {
        performTest("package test;\n@SuppressWarnings({\"w1\", \"w2\"})\npublic class Test { }\n",
                    new AddAnnotationAttributeValue("java.lang.SuppressWarnings", "value", "\"foo\"", "\"bar\""),
                    new ContentValidator("package test;\n@SuppressWarnings({\"w1\", \"w2\", \"foo\", \"bar\"})\npublic class Test { }\n"));
    }

    public void testAddAnnotationAttributeValueCompilationUnit() throws Exception {
        performTest("test/package-info.java",
                    "package test;\n",
                    "1.5",
                    new AddAnnotationAttributeValue("java.lang.SuppressWarnings", "value", "\"foo\"", "\"bar\""),
                    new ContentValidator("@SuppressWarnings({\"foo\", \"bar\"})\npackage test;\n"),
                    false);//@SuppressWarning is not application to package-info.
    }

    public void testAddAnnotationToInterface213360() throws Exception {
        performTest("package test;\npublic interface Test { }\n",
                    new AddAnnotationAttributeValue("java.lang.SuppressWarnings", "value", "\"foo\"", "\"bar\""),
                    new ContentValidator("package test;\n@SuppressWarnings({\"foo\", \"bar\"})\npublic interface Test { }\n"));
    }
    
    private static final class AddAnnotationAttributeValue implements Task<WorkingCopy> {

        private final String annotationType;
        private final String attributeName;
        private final String[] values;

        public AddAnnotationAttributeValue(String annotationType, String attributeName, String... values) {
            this.annotationType = annotationType;
            this.attributeName = attributeName;
            this.values = values;
        }

        @Override
        public void run(WorkingCopy parameter) throws Exception {
            parameter.toPhase(Phase.RESOLVED);

            if ("package-info.java".equals(parameter.getFileObject().getNameExt())) {
                CompilationUnitTree newCUT = parameter.getCompilationUnit();
                TypeElement annotation = parameter.getElements().getTypeElement(annotationType);

                assertNotNull(annotation);

                for (int i = 0; i < values.length; i++) {
                    ExpressionTree value = parameter.getTreeUtilities().parseExpression(values[i], new SourcePositions[1]);
                    newCUT = GeneratorUtilities.get(parameter).appendToAnnotationValue(newCUT, annotation, attributeName, value);
                }

                parameter.rewrite(parameter.getCompilationUnit(), newCUT);
            } else {
                ClassTree ct = (ClassTree) parameter.getCompilationUnit().getTypeDecls().get(0);
                ModifiersTree newMods = ct.getModifiers();
                TypeElement annotation = parameter.getElements().getTypeElement(annotationType);

                assertNotNull(annotation);

                for (int i = 0; i < values.length; i++) {
                    ExpressionTree value = parameter.getTreeUtilities().parseExpression(values[i], new SourcePositions[1]);
                    newMods = GeneratorUtilities.get(parameter).appendToAnnotationValue(newMods, annotation, attributeName, value);
                }

                parameter.rewrite(ct.getModifiers(), newMods);
            }
        }
    }

    public void test232199a() throws Exception {
        performTest("test/Test.java",
                    "package test; public class Test implements I { } interface I { public default void t() { } } \n",
                    "1.8",
                    new T2(),
                    new ContentValidator("package test; public class Test implements I { \n\n    @Override\n    public void t() {\n        I.super.t(); //To change body of generated methods, choose Tools | Templates.\n    }\n } interface I { public default void t() { } } \n"),
                    false);
    }
    
    public void test232199b() throws Exception {
        performTest("test/Test.java",
                    "package test; public interface Test extends I { } interface I { public void t(); } \n",
                    "1.8",
                    new T2(),
                    new ContentValidator("package test; public interface Test extends I { \n\n    @Override\n    public default void t() {\n        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n    }\n } interface I { public void t(); } \n"),
                    false);
    }
    
    public void test232199c() throws Exception {
        performTest("test/Test.java",
                    "package test; public interface Test extends I { } interface I { public default void t() { } } \n",
                    "1.8",
                    new T2(),
                    new ContentValidator("package test; public interface Test extends I { \n\n    @Override\n    public default void t() {\n        I.super.t(); //To change body of generated methods, choose Tools | Templates.\n    }\n } interface I { public default void t() { } } \n"),
                    false);
    }
    
    public void test232199d() throws Exception {
        performTest("test/Test.java",
                    "package test; public class Test implements I { } interface I { public void t(); } \n",
                    "1.8",
                    new T2(),
                    new ContentValidator("package test; public class Test implements I { \n\n    @Override\n    public void t() {\n        throw new UnsupportedOperationException(\"Not supported yet.\"); //To change body of generated methods, choose Tools | Templates.\n    }\n } interface I { public void t(); } \n"),
                    false);
    }
    
    private static final class T2 implements Task<WorkingCopy> {
        @Override
        public void run(WorkingCopy parameter) throws Exception {
            parameter.toPhase(Phase.RESOLVED);

            TypeElement i = parameter.getElements().getTypeElement("test.I");
            TypeElement test = parameter.getElements().getTypeElement("test.Test");
            
            assertNotNull(i);
            assertNotNull(test);
            
            ExecutableElement t = (ExecutableElement) i.getEnclosedElements().get(0);
            
            ClassTree testTree = parameter.getTrees().getTree(test);
            MethodTree newMethod = GeneratorUtilities.get(parameter).createAbstractMethodImplementation(test, t);
            
            ClassTree nueTestTree = parameter.getTreeMaker().addClassMember(testTree, newMethod);
            
            parameter.rewrite(testTree, nueTestTree);
        }
    }
    
    private static final class ContentValidator implements Validator {

        private final String expectedCode;

        public ContentValidator(String expectedCode) {
            this.expectedCode = expectedCode;
        }

        @Override
        public void validate(CompilationInfo info) {
            assertEquals(expectedCode, info.getText());
        }

    }

}
