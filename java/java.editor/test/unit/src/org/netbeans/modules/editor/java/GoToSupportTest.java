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
package org.netbeans.modules.editor.java;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;

import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.java.GoToSupport.UiUtilsCaller;
import org.netbeans.modules.java.source.TreeShims;
//import org.netbeans.modules.java.source.TreeLoader;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Lahoda
 */
public class GoToSupportTest extends NbTestCase {
    
    /** Creates a new instance of GoToSupportTest */
    public GoToSupportTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);
        org.netbeans.api.project.ui.OpenProjects.getDefault().getOpenProjects();
        SourceUtilsTestUtil2.disableArtificalParameterNames();
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = false;
    }
    
    public void testGoToMethod() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test { public void test() {} public static void main(String[] args) {test();}}", 97, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(34, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToClass() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test { public static void main(String[] args) {TT tt} } class TT { }", 75, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(83, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test { public Test() {} public static void main(String[] args) {new Test();}}", 97, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(34, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToGenerifiedConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test<T> { public Test() {} public static void main(String[] args) {new Test<String>();}}", 100, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(37, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToSuperConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test extends Base { public Test() {super(1);} } class Base {public Base() {} public Base(int i) {}}", 64, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(104, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToThisConstructor() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test { public Test() {this(1);} public Test(int i) {}}", 50, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(59, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToSuperMethod1() throws Exception {
        //try to go to "super" in super.methodInParent():
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test extends Base { public void test() {super.methodInParent();} } class Base {public void methodInParent() {}}", 75, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(106, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToSuperMethod2() throws Exception {
        //try to go to "super" in super.methodInParent():
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test extends Base { public void test() {super.methodInParent();} } class Base {public void methodInParent() {}}", 70, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToGarbage() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {ddddddddd public void test() {super.methodInParent();} }", 36, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testTooltipForGarbage() throws Exception {
        String tooltip = performTest("package test; public class Test {ddddddddd public void test() {super.methodInParent();} }", 36, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, true);
    }

    public void testTooltipForInnerClasses() throws Exception {
        String code = "package test; public class Test {enum EE {A} class CC {} EE a; CC c;}";
        int[] offset = new int[] {82, 88};
        String[] golden = new String[] {
            "<html><body><base href=\"file:" + getWorkDirPath() + "/src/test/Test.java\"></base><font size='+0'><b><a href='*0'>test.&#x200B;Test</a></b></font><pre>static enum <b>EE</b><br>extends <a href='*1'>Enum</a>&lt;<a href='*2'>EE</a>&gt;</pre>",
            "<html><body><base href=\"file:" + getWorkDirPath() + "/src/test/Test.java\"></base><font size='+0'><b><a href='*0'>test.&#x200B;Test</a></b></font><pre>class <b>CC</b><br>extends <a href='*1'>Object</a></pre>",
        };
        assertEquals(offset.length, golden.length);
        for (int cntr = 0; cntr < offset.length; cntr++) {
            String tooltip = performTest(code, offset[cntr] - 24, new OrigUiUtilsCaller() {
                public void open(FileObject fo, int pos) {
                    fail("Should not be called.");
                }
                public void beep() {
                    fail("Should not be called.");
                }
                public void open(ClasspathInfo info, Element el) {
                    fail("Should not be called.");
                }
            }, true);

            assertEquals(golden[cntr], tooltip);
        }
    }

    public void testGoToIntoAnnonymous() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {new Runnable() {int var; public void run() {var = 0;}};} }", 99, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                wasCalled[0] = true;
                assertTrue(source == fo);
                assertEquals(69, pos);
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToString() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {String s = null;} }", 56, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.lang.String", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToAnnonymousInnerClass() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {new Runnable() {public void run(){}};} }", 61, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.INTERFACE, el.getKind());
                assertEquals("java.lang.Runnable", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToAnnonymousInnerClass2() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {new java.util.ArrayList(c) {public void run(){}};} java.util.Collection c;}", 70, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CONSTRUCTOR, el.getKind());
                assertEquals("java.util.ArrayList", ((TypeElement) el.getEnclosingElement()).getQualifiedName().toString());

                ExecutableElement ee = (ExecutableElement) el;

                assertEquals(1, ee.getParameters().size());

                TypeMirror paramType = ee.getParameters().get(0).asType();

                assertEquals(TypeKind.DECLARED, paramType.getKind());
                assertEquals("java.util.Collection", ((TypeElement) ((DeclaredType) paramType).asElement()).getQualifiedName().toString());

                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToAnnonymousInnerClass3() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {java.util.List l = new java.util.ArrayList() {public void run(){}};}", 70, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CONSTRUCTOR, el.getKind());
                assertEquals("java.util.ArrayList", ((TypeElement) el.getEnclosingElement()).getQualifiedName().toString());

                ExecutableElement ee = (ExecutableElement) el;

                assertEquals(0, ee.getParameters().size());

                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToParameter() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test(int xx) {xx = 0;}}", 60, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(50, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToLocalVariable() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {int xx;xx = 0;}}", 61, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(53, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToTypeVariable() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test<TTT> {public void test() {TTT t;}}", 60, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(32, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToSynteticConstructorInDifferentClass() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {new Auxiliary();}}", 62, new UiUtilsCaller() {
            public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }
            public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            public boolean open(ClasspathInfo info, final ElementHandle<?> el) {
                try {
                    JavaSource.create(info).runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                            Element e = el.resolve(parameter);

                            //jlahoda: originally, GtS jumped directly to the owning class for synthetic constructors
                            //due to a performance improvement (not parsing the target class from source if the target class
                            //is not the current class), the jump is always performed to the specific element (constructor):
                            assertEquals(ElementKind.CONSTRUCTOR, e.getKind());
                            assertEquals("test.Auxiliary", ((TypeElement) e.getEnclosingElement()).getQualifiedName().toString());
                            wasCalled[0] = true;
                        }
                    }, true);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                return true;
            }
            public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, true);

        assertTrue(wasCalled[0]);
    }

    public void testGoToCArray90875() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {int ar[][] = null; System.err.println(ar);}}", 92, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(53, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testNewClass91637() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public Test(int x){} public void test() {int ii = 0; new Test(ii);}}", 96, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(74, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);

        wasCalled[0] = false;

        performTest("package test; public class Test<T> {public Test(int x){} public void test() {int ii = 0; new Test<Object>(ii);}}", 107, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(77, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);

        wasCalled[0] = false;

        performTest("package test; public class Test<T> {public Test(int x){} public void test() {int ii = 0; new Test<Object>(ii);}}", 100, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.lang.Object", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testNewClass91769() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test {public void test() {new AB(name);} private static class AB {public AB(String n){}}}", 58, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(93, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);

        wasCalled[0] = false;

        performTest("package test; public class Test {public void test() {new AB<Object>(name);} private static class AB<T> {public AB(String n){}}}", 58, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(104, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);

        wasCalled[0] = false;

        performTest("package test; public class Test {public void test() {new AB(name);} private static class AB {public AB(String n){}}}", 62, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);

        wasCalled[0] = false;

        performTest("package test; public class Test {public void test() {new AB<Object>(name);} private static class AB<T> {public AB(String n){}}}", 63, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.lang.Object", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testBeepOnDeclarations() throws Exception {
        String code = "package test; public class Test {public void test(String s) {} public String test2(String s) {} public void test3() {} private static class AB {} private String FIELD; private void test4(String name1, String name2) {}}";
        final boolean[] wasCalled = new boolean[1];

        for (final int pos : new int[] {53, 71, 103, 134, 165, 187, 220, 234}) {
            performTest(code, pos - 24, new OrigUiUtilsCaller() {
                public void open(FileObject fo, int pos) {
                    fail("Should not be called, position= " + pos + ".");
                }
                public void beep() {
                    wasCalled[0] = true;
                }
                public void open(ClasspathInfo info, Element el) {
                    fail("Should not be called, position= " + pos + ".");
                }
            }, false);

            assertTrue(wasCalled[0]);

            wasCalled[0] = false;
        }

        for (final int pos : new int[] {77, 97, 109, 181, 214, 228}) {
            performTest(code, pos - 24, new OrigUiUtilsCaller() {
                public void open(FileObject fo, int pos) {
                    fail("Should not be called, position= " + pos + ".");
                }
                public void beep() {
                    fail("Should not be called, position= " + pos + ".");
                }
                public void open(ClasspathInfo info, Element el) {
                    assertEquals(ElementKind.CLASS, el.getKind());
                    assertEquals("java.lang.String", ((TypeElement) el).getQualifiedName().toString());
                    wasCalled[0] = true;
                }
            }, false);

            assertTrue(wasCalled[0]);

            wasCalled[0] = false;
        }
    }

    public void test113474() throws Exception {
        String code = "package test; public class Test {}whatever";
        final boolean[] wasCalled = new boolean[1];

        performTest(code, 63 - 24, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called, position= " + pos + ".");
            }
            public void beep() {
                wasCalled[0] = true;
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called, element= " + el + ".");
            }
        }, false);

        wasCalled[0] = false;
    }

    public void testGoToImport() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        String code = "package test; import java.awt.Color; public class Test {}";

        performTest(code, code.indexOf("Color") + 1, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.awt.Color", ((TypeElement) el).getQualifiedName().toString());
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToStaticImport() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        String code = "package test; import static java.awt.Color.BLACK; public class Test {}";

        performTest(code, code.indexOf("BLACK") + 1, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                assertEquals(ElementKind.FIELD, el.getKind());
                assertEquals("java.awt.Color.BLACK",
                        ((TypeElement) el.getEnclosingElement()).getQualifiedName().toString() + '.' + el);
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void test110185() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        String code = "package test; public class Test { private Test t;}";

        performTest(code, code.lastIndexOf("Test") + 1, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(14, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void test228438() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        String code = "package test; public enum Test { ONE(\"one\"); private String str; private Test(String str) {this.str = str}}";

        performTest(code, code.indexOf("ONE") + 1, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(65, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testNearlyMatchingMethod1() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        String code = "package test; public class Test { public void x() {Object o = null; test(o);} public void test(int i, float f) {} public void test(Integer i) {} }";

        performTest(code, code.indexOf("test(") + 1, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(114, pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void test122637() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private void x() {\n" +
                      "        RequestProcessor.post(new Runnable() {\n" +
                      "            public void run() {\n" +
                      "                String test = null;\n" +
                      "                test.length();\n" +
                      "            }\n" +
                      "        });\n" +
                      "    }\n" +
                      "}\n";

        performTest(code, code.indexOf("test.length") + 1, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(code.indexOf("String"), pos);
                wasCalled[0] = true;
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testMethodReference() throws Exception {
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "1.8";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private static void method() {\n" +
                      "        javax.swing.SwingUtilities.invokeLater(Test::m|ethod);\n" +
                      "    }\n" +
                      "}\n";

        performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(code.indexOf("private static "), pos);
                wasCalled[0] = true;
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToCannotOpen1() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test { public static void main(String[] args) {TT tt} } class TT { }", 75, new UiUtilsCaller() {
            public boolean open(FileObject fo, int pos) {
                return false;
            }
            public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            public void warnCannotOpen(String displayName) {
                assertEquals("TT", displayName);
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testGoToCannotOpen2() throws Exception {
        final boolean[] wasCalled = new boolean[1];

        performTest("package test; public class Test { public static void main(String[] args) {TT tt} } class TT { }", 62, new UiUtilsCaller() {
            public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }
            public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                return false;
            }
            public void warnCannotOpen(String displayName) {
                assertEquals("String", displayName);
                wasCalled[0] = true;
            }
        }, false);

        assertTrue(wasCalled[0]);
    }

    public void testDeadlock135736() throws Exception {
        final CountDownLatch l1 = new CountDownLatch(1);
        final CountDownLatch l2 = new CountDownLatch(1);
        final boolean[] wasCalled = new boolean[1];

        new Thread() {
            @Override
            public void run() {
                try {
                    FileObject f = FileUtil.createMemoryFileSystem().getRoot().createData("Test.java");

                    try {
                        l1.await();
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }

                    JavaSource.forFileObject(f).runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                            l2.countDown();
                        }
                    }, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }.start();

        performTest("package test; public class Test { public static void main(String[] args) {} }", 62, new UiUtilsCaller() {
            public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }
            public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                assertEquals(ElementKind.CLASS, el.getKind());
                assertEquals("java.lang.String", el.getQualifiedName());
                wasCalled[0] = true;
                l1.countDown();
                try {
                    l2.await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
                return true;
            }

            public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false);

        assertTrue(wasCalled[0]);
    }
    
    public void testTooltipForConciseConstructorCall1() throws Exception {
        String code = "package test; public class Test {java.util.List<String> l = new java.util.Arr|ayList<>();}";
        int offset = code.indexOf('|');
        code = code.replaceAll(Pattern.quote("|"), "");
        assertNotSame(-1, offset);
        String golden = "<html><body><font size='+0'><b><a href='*0'>java.&#x200B;util.&#x200B;ArrayList</a></b></font><pre>public <b>ArrayList</b>()</pre>";

        String tooltip = performTest(code, offset, new OrigUiUtilsCaller() {
            public void open(FileObject fo, int pos) {
                fail("Should not be called.");
            }
            public void beep() {
                fail("Should not be called.");
            }
            public void open(ClasspathInfo info, Element el) {
                fail("Should not be called.");
            }
        }, true);

        assertEquals(golden, tooltip);
    }

    public void testVar() throws Exception {
        try {
            SourceVersion.valueOf("RELEASE_10");
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_10, skip test:
            return ;
        }
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "1.10";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private static void method() {\n" +
                      "        var var = 0;\n" +
                      "        int i = va|r;\n" +
                      "    }\n" +
                      "}\n";

        performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(code.indexOf("var var = 0;"), pos);
                wasCalled[0] = true;
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, false);

        assertTrue(wasCalled[0]);
    }

    public void testBindingVar() throws Exception {
        if (!hasPatterns()) return ;
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private static void method(Object o) {\n" +
                      "        if (o instanceof String str) {\n" +
                      "            System.err.println(s|tr);\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";

        performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(code.indexOf("o instanceof String str"), pos);
                wasCalled[0] = true;
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, false);

        assertTrue(wasCalled[0]);
    }

    public void testBindingVarInName() throws Exception {
        if (!hasPatterns()) return ;
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private static void method(Object o) {\n" +
                      "        if (o instanceof String s|tr) {\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";

        performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                wasCalled[0] = true;
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, false);

        assertTrue(wasCalled[0]);
    }

    public void testBindingVarToolTip() throws Exception {
        if (!hasPatterns()) return ;
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private static void method(Object o) {\n" +
                      "        if (o instanceof String s|tr) {\n" +
                      "        }\n" +
                      "    }\n" +
                      "}\n";

        String tooltip = performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                wasCalled[0] = true;
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, true, false);

        assertEquals("<html><body>final java.lang.String <b>str</b>", tooltip);
    }

    private String sourceLevel = "1.5";
    private FileObject source;
    
    private String performTest(String sourceCode, final int offset, final OrigUiUtilsCaller validator, boolean tooltip) throws Exception {
        return performTest(sourceCode, offset, new UiUtilsCaller() {
            public boolean open(FileObject fo, int pos) {
                validator.open(fo, pos);
                return true;
            }
            public void beep(boolean goToSource, boolean goToJavadoc) {
                validator.beep();
            }
            public boolean open(final ClasspathInfo info, final ElementHandle<?> el) {
                try {
                    JavaSource.create(info).runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                            Element e = el.resolve(parameter);

                            validator.open(info, e);
                        }
                    }, true);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                return true;
            }
            public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, tooltip);
    }
    
    private String performTest(String sourceCode, final int offset, final UiUtilsCaller validator, boolean tooltip) throws Exception {
        return performTest(sourceCode, offset, validator, tooltip, false);
    }

    private String performTest(String sourceCode, final UiUtilsCaller validator, boolean tooltip, boolean doCompileRecursively) throws Exception {
        int offset = sourceCode.indexOf('|');

        assertNotSame(-1, offset);

        sourceCode = sourceCode.replace("|", "");

        return performTest(sourceCode, offset, validator, tooltip, doCompileRecursively);
    }

    private String performTest(String sourceCode, final int offset, final UiUtilsCaller validator, boolean tooltip, boolean doCompileRecursively) throws Exception {
        String auxiliary = "package test; public class Auxiliary {}"; //test go to "syntetic" constructor
        return performTest(sourceCode, auxiliary, offset, validator, tooltip, doCompileRecursively);
    }

    private String performTest(String sourceCode, String auxiliaryCode, int offset, final UiUtilsCaller validator, boolean tooltip, boolean doCompileRecursively) throws Exception {

        GoToSupport.CALLER = validator;
        
        if (offset == (-1)) {
            offset = sourceCode.indexOf('|');

            assertNotSame(-1, offset);

            sourceCode = sourceCode.replace("|", "");
        }

        clearWorkDir();
        FileUtil.refreshFor(getWorkDir());

        FileObject wd = FileUtil.toFileObject(getWorkDir());
        FileObject sourceDir = FileUtil.createFolder(wd, "src");
        FileObject buildDir = FileUtil.createFolder(wd, "build");
        FileObject cacheDir = FileUtil.createFolder(wd, "cache");
        
        source = FileUtil.createData(sourceDir, "test/Test.java");
        
        FileObject auxiliarySource = FileUtil.createData(sourceDir, "test/Auxiliary.java");

        TestUtilities.copyStringToFile(source, sourceCode);
        TestUtilities.copyStringToFile(auxiliarySource, auxiliaryCode);

        SourceUtilsTestUtil.setSourceLevel(source, sourceLevel);
        SourceUtilsTestUtil.setSourceLevel(auxiliarySource, sourceLevel);
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);

        if (doCompileRecursively) {
            SourceUtilsTestUtil.compileRecursively(sourceDir);
        }
        
        DataObject od = DataObject.find(source);
        EditorCookie ec = od.getCookie(EditorCookie.class);
        Document doc = ec.openDocument();

        doc.putProperty(Language.class, JavaTokenId.language());
        doc.putProperty("mimeType", "text/x-java");
        
        if (tooltip)
            return GoToSupport.getGoToElementTooltip(doc, offset, false, null);
        else
            GoToSupport.goTo(doc, offset, false);
        
        return null;
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
    
    interface OrigUiUtilsCaller {
        
        public void open(FileObject fo, int pos);
        public void beep();
        public void open(ClasspathInfo info, Element el);
        
    }
    
    public void testRecords1() throws Exception {
        if (!hasRecords()) return ;
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "1.14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public record R(int ff) {}\n" +
                      "    private static void method(R r) {\n" +
                      "        int i = r.f|f();\n" +
                      "    }\n" +
                      "}\n";

        performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                assertTrue(source == fo);
                assertEquals(code.indexOf("int ff"), pos);
                wasCalled[0] = true;
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, false);

        assertTrue(wasCalled[0]);
    }

    public void testRecords2() throws Exception {
        if (!hasRecords()) return ;
        this.sourceLevel = "1.14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public record R(int ff) {}\n" +
                      "    private static void method(R r) {\n" +
                      "        int i = r.f|f();\n" +
                      "    }\n" +
                      "}\n";

        String toolTip = performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, true, false);

        toolTip = toolTip.replace(source.toURI().toString(), "FILE");
        assertEquals("<html><body><base href=\"FILE\"></base><font size='+0'><b><a href='*0'>test.&#x200B;Test.&#x200B;R</a></b></font>", toolTip);
    }

    public void testRecords3() throws Exception {
        if (!hasRecords()) return ;
        this.sourceLevel = "1.14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    public record RR(int ff) {}\n" +
                      "    private static void method(R|R r) {\n" +
                      "        int i = r.ff();\n" +
                      "    }\n" +
                      "}\n";

        String toolTip = performTest(code, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                fail("Should not be called.");
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, true, false);

        toolTip = toolTip.replace(source.toURI().toString(), "FILE");
        assertEquals("<html><body><base href=\"FILE\"></base><font size='+0'><b><a href='*0'>test.&#x200B;Test</a></b></font>", toolTip);
    }

    public void testRecords4() throws Exception {
        if (!hasRecords()) return ;
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "1.14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private static void method(Auxiliary r) {\n" +
                      "        int i = r.f|f();\n" +
                      "    }\n" +
                      "}\n";
        final String auxiliary = "package test;\n" +
                                 "public record Auxiliary(int ff) {}";

        performTest(code, auxiliary, -1, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                try {
                    JavaSource.create(info).runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element e = el.resolve(parameter);

                            assertTrue(TreeShims.isRecordComponent(e));
                            assertEquals("ff", e.getSimpleName().toString());
                            wasCalled[0] = true;
                        }
                    }, true);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, true);

        assertTrue(wasCalled[0]);
    }

    public void testRecords5() throws Exception {
        if (!hasRecords()) return ;
        final boolean[] wasCalled = new boolean[1];
        this.sourceLevel = "1.14";
        final String code = "package test;\n" +
                      "public class Test {\n" +
                      "    private static void method(Auxi|liary r) {\n" +
                      "    }\n" +
                      "}\n";
        final String auxiliary = "package test;\n" +
                                 "public record Auxiliary(int ff) {}";

        performTest(code, auxiliary, -1, new UiUtilsCaller() {
            @Override public boolean open(FileObject fo, int pos) {
                fail("Should not be called.");
                return true;
            }

            @Override public void beep(boolean goToSource, boolean goToJavadoc) {
                fail("Should not be called.");
            }
            @Override public boolean open(ClasspathInfo info, ElementHandle<?> el) {
                try {
                    JavaSource.create(info).runUserActionTask(new Task<CompilationController>() {
                        public void run(CompilationController parameter) throws Exception {
                            parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element e = el.resolve(parameter);

                            assertTrue(TreeShims.isRecord(e));
                            assertEquals("test.Auxiliary", ((TypeElement) e).getQualifiedName().toString());
                            wasCalled[0] = true;
                        }
                    }, true);
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                return true;
            }
            @Override public void warnCannotOpen(String displayName) {
                fail("Should not be called.");
            }
        }, false, true);

        assertTrue(wasCalled[0]);
    }

    private static boolean hasPatterns() {
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
            return true;
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip tests
            return false;
        }
    }

    private static boolean hasRecords() {
        try {
            SourceVersion.valueOf("RELEASE_14"); //NOI18N
            return true;
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_14, skip tests
            return false;
        }
    }

}
