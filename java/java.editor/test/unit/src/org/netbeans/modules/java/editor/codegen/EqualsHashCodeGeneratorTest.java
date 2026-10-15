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
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.BootClassPathUtil;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jaroslav Tulach
 */
public class EqualsHashCodeGeneratorTest extends NbTestCase {
    FileObject fo;
    private String sourceLevel;
    
    public EqualsHashCodeGeneratorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fo = SourceUtilsTestUtil.makeScratchDir(this);
        System.setProperty("netbeans.user", getWorkDirPath());
        SourceUtilsTestUtil.setLookup(new Object[] { new DD(), new SourceLevelQueryImplementation2() {
            @Override public Result getSourceLevel(FileObject javaFile) {
                return new Result() {
                    @Override
                    public String getSourceLevel() {
                        return sourceLevel != null ? sourceLevel : "1.5";
                    }
                    @Override
                    public void addChangeListener(ChangeListener listener) {}
                    @Override
                    public void removeChangeListener(ChangeListener listener) {}
                };
            }
        }, BootClassPathUtil.getBootClassPathProvider() }, getClass().getClassLoader());
    }
    
    

    public void testEnabledFieldsWhenHashCodeExists() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        String what1 = "class X {" +
            "  private int x;" +
            "  private int y;" +
            "  public int hashCode() {";
        
        String what2 = 
            "    return y;" +
            "  }" +
            "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);
        
        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);
        
        final JTextArea c = new JTextArea();
        c.getDocument().putProperty(JavaSource.class, new WeakReference<Object>(js));
        c.getDocument().insertString(0, what, null);
        c.setCaretPosition(what1.length());
        
        class Task implements org.netbeans.api.java.source.Task<CompilationController> {
            EqualsHashCodeGenerator generator;
            

            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element el = cc.getElements().getTypeElement("X");
                generator = EqualsHashCodeGenerator.createEqualsHashCodeGenerator(c, cc, el);
            }
            
            
            public void post() throws Exception {
                assertNotNull("panel", generator);
                
                assertEquals("Two fields", 2, generator.description.getSubs().size());
                assertEquals("y field selected", true, generator.description.getSubs().get(1).isSelected());
                assertEquals("x field not selected", false, generator.description.getSubs().get(0).isSelected());
                assertEquals("generate hashCode", false, generator.generateHashCode);
                assertEquals("generate equals", true, generator.generateEquals);
            }
        }
        final Task t = new Task();
        
        js.runUserActionTask(t, false);
        t.post();
        
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                t.generator.invoke();
            }
        });
        
        //String text = c.getDocument().getText(0, c.getDocument().getLength());
        String text = GeneratorUtilsTest.readFromFile(java);
        
        int first = text.indexOf("hashCode");
        if (first < 0) {
            fail("There should be one hashCode mehtod:\n" + text);
        }
        int snd = text.indexOf("hashCode", first + 5);
        if (snd >= 0) {
            fail("Only one hashCode:\n" + text);
        }
    }
    public void testEnabledFieldsWhenEqualsExists() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        String what1 = "class X {" +
            "  private int x;" +
            "  private int y;" +
            "  public Object equals(Object snd) {" +
            "    if (snd instanceof X) {" +
            "       X x2 = (X)snd;";
        
        String what2 = 
            "       return this.x == x2.x;" +
            "    }" +
            "    return false;" +
            "  }" +
            "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);
        
        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);
        
        class Task implements CancellableTask<CompilationController> {
            EqualsHashCodeGenerator generator;
            
            public void cancel() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Element el = cc.getElements().getTypeElement("X");
                generator = EqualsHashCodeGenerator.createEqualsHashCodeGenerator(null, cc, el);
            }
            
            
            public void post() throws Exception {
                assertNotNull("panel", generator);
                
                assertEquals("Two fields", 2, generator.description.getSubs().size());
                assertEquals("x field selected", true, generator.description.getSubs().get(0).isSelected());
                assertEquals("y field not selected", false, generator.description.getSubs().get(1).isSelected());
                assertEquals("generate hashCode", true, generator.generateHashCode);
                assertEquals("generate equals", false, generator.generateEquals);
            }
        }
        Task t = new Task();
        
        js.runUserActionTask(t, false);
        t.post();
    }
    
    public void test125114() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "class X {" +
            "  private int x;" +
            "  private int y;" +
            "  public void test() {" +
            "    new Object() {" +
            "       private int i;";
        
        String what2 = 
            "    }" +
            "  }" +
            "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);
        
        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);
        
        class TaskImpl implements Task<CompilationController> {
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                Element el = cc.getTrees().getElement(cc.getTreeUtilities().pathFor(what1.length()));
                assertNull(EqualsHashCodeGenerator.createEqualsHashCodeGenerator(null, cc, el));
            }
        }
        TaskImpl t = new TaskImpl();
        
        js.runUserActionTask(t, false);
    }

    public void test171265() throws Exception {
        EqualsHashCodeGenerator.randomNumber = 1;

        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "class X {\n" +
                             "  private String s;\n" +
                             "  private EE e;\n";

        String what2 =
            "}\n" +
            "enum EE { A, B;}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);

        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);

        class TaskImpl implements Task<WorkingCopy> {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree clazzTree = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                TreePath clazz = new TreePath(new TreePath(copy.getCompilationUnit()), clazzTree);
                List<VariableElement> vars = new LinkedList<VariableElement>();

                for (Tree m : clazzTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        vars.add((VariableElement) copy.getTrees().getElement(new TreePath(clazz, m)));
                    }
                }

                EqualsHashCodeGenerator.generateEqualsAndHashCode(copy, clazz, Collections.<VariableElement>emptyList(), vars, -1);
            }
        }

        TaskImpl t = new TaskImpl();

        js.runModificationTask(t).commit();
        
        DataObject dObj = DataObject.find(java);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        Document doc = ec != null ? ec.getDocument() : null;
        
        String result = doc != null ? doc.getText(0, doc.getLength()) : TestUtilities.copyFileToString(FileUtil.toFile(java));

        String golden = "" +
                        "class X {\n" +
                        "  private String s;\n" +
                        "  private EE e;\n" +
                        "    @Override\n" +
                        "    public int hashCode() {\n" +
                        "        int hash = 1;\n" +
                        "        hash = 1 * hash + (this.s != null ? this.s.hashCode() : 0);\n" +
                        "        hash = 1 * hash + (this.e != null ? this.e.hashCode() : 0);\n" +
                        "        return hash;\n" +
                        "    }\n" +
                        "    @Override\n" +
                        "    public boolean equals(Object obj) {\n" +
                        "        if (this == obj) {\n" +
                        "            return true;\n" +
                        "        }\n" +
                        "        if (obj == null) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (getClass() != obj.getClass()) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        final X other = (X) obj;\n" +
                        "        return true;\n" +
                        "    }\n" +
                        "}\n" +
                        "enum EE { A, B;}";

        result = result.replaceAll("[ \t\n]+", " ");
        golden = golden.replaceAll("[ \t\n]+", " ");

        assertEquals(golden, result);

    }

    
    public void test142212() throws Exception {
        EqualsHashCodeGenerator.randomNumber = 1;
        
        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "class X {\n" +
                             "  private byte b;\n" +
                             "  private int[] x;\n" +
                             "  private String[] y;\n" +
                             "  private String s;\n" +
                             "  private Object o;\n";

        String what2 =
            "}\n";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);

        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);

        class TaskImpl implements Task<WorkingCopy> {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree clazzTree = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                TreePath clazz = new TreePath(new TreePath(copy.getCompilationUnit()), clazzTree);
                List<VariableElement> vars = new LinkedList<VariableElement>();

                for (Tree m : clazzTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        vars.add((VariableElement) copy.getTrees().getElement(new TreePath(clazz, m)));
                    }
                }

                EqualsHashCodeGenerator.generateEqualsAndHashCode(copy, clazz, vars, vars, -1);
            }
        }

        TaskImpl t = new TaskImpl();

        js.runModificationTask(t).commit();

        DataObject dObj = DataObject.find(java);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        Document doc = ec != null ? ec.getDocument() : null;
        
        String result = doc != null ? doc.getText(0, doc.getLength()) : TestUtilities.copyFileToString(FileUtil.toFile(java));
        String golden = "\nimport java.util.Arrays;\n" +
                        "class X {\n" +
                        "  private byte b;\n" +
                        "  private int[] x;\n" +
                        "  private String[] y;\n" +
                        "  private String s;\n" +
                        "  private Object o;\n" +
                        "    @Override\n" +
                        "    public int hashCode() {\n" +
                        "        int hash = 1;\n" +
                        "        hash = 1 * hash + this.b;\n" +
                        "        hash = 1 * hash + Arrays.hashCode(this.x);\n" +
                        "        hash = 1 * hash + Arrays.deepHashCode(this.y);\n" +
                        "        hash = 1 * hash + (this.s != null ? this.s.hashCode() : 0);\n" +
                        "        hash = 1 * hash + (this.o != null ? this.o.hashCode() : 0);\n" +
                        "        return hash;\n" +
                        "    }\n" +
                        "    @Override\n" +
                        "    public boolean equals(Object obj) {\n" +
                        "        if (this == obj) {\n" +
                        "            return true;\n" +
                        "        }\n" +
                        "        if (obj == null) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (getClass() != obj.getClass()) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        final X other = (X) obj;\n" +
                        "        if (this.b != other.b) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if ((this.s == null) ? (other.s != null) : !this.s.equals(other.s)) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (!Arrays.equals(this.x, other.x)) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (!Arrays.deepEquals(this.y, other.y)) {\n" +
                        "            return false;\n" +
                        "        }" +
                        "        return this.o == other.o || (this.o != null && this.o.equals(other.o));\n" +
                        "    }\n" +
                        "}\n";

        result = result.replaceAll("[ \t\n]+", " ");
        golden = golden.replaceAll("[ \t\n]+", " ");
        
        assertEquals(golden, result);
    }
    
    public void testObjectEditing1() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "package java.lang; public class Object {\n" +
                             "  public int hashCode() {return 0;}" +
                             "  public static int hashCode(Object o) {return 0;}" +
                             "  public boolean equals(Object o) {return false;}" +
                             "  public boolean equals(Object o1, Object o2) {return false;}" +
                             "  \n";

        String what2 =
            "}\n";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);

        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);

        class TaskImpl implements Task<WorkingCopy> {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree clazzTree = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                TreePath clazz = new TreePath(new TreePath(copy.getCompilationUnit()), clazzTree);
                List<VariableElement> vars = new LinkedList<VariableElement>();

                for (Tree m : clazzTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        vars.add((VariableElement) copy.getTrees().getElement(new TreePath(clazz, m)));
                    }
                }

                EqualsHashCodeGenerator.overridesHashCodeAndEquals(copy, copy.getTrees().getElement(clazz), null);
            }
        }

        TaskImpl t = new TaskImpl();

        js.runModificationTask(t);
    }

    public void testObjectEditing2() throws Exception {
        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "package java.lang; public class Object {\n" +
                             "  public int hashCode() {return 0;}" +
                             "  public int hashCode() {return 0;}" +
                             "  public boolean equals(Object o) {return false;}" +
                             "  public boolean equals(Object o) {return false;}" +
                             "  \n";

        String what2 =
            "}\n";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);

        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);

        class TaskImpl implements Task<WorkingCopy> {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree clazzTree = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                TreePath clazz = new TreePath(new TreePath(copy.getCompilationUnit()), clazzTree);
                List<VariableElement> vars = new LinkedList<VariableElement>();

                for (Tree m : clazzTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        vars.add((VariableElement) copy.getTrees().getElement(new TreePath(clazz, m)));
                    }
                }

                EqualsHashCodeGenerator.overridesHashCodeAndEquals(copy, copy.getTrees().getElement(clazz), null);
            }
        }

        TaskImpl t = new TaskImpl();

        js.runModificationTask(t);
    }

    public void test156994() throws Exception {
        EqualsHashCodeGenerator.randomNumber = 1;

        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "class X {\n" +
                             "  private float f;\n" +
                             "  private double d;\n" +
                             "  private long l;\n" +
                             "  private boolean b;\n" +
                             "  private char c;\n" +
                             "  private E e;\n";

        String what2 =
            "  public enum E {A, B;}\n}\n";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);

        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);

        class TaskImpl implements Task<WorkingCopy> {
            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree clazzTree = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                TreePath clazz = new TreePath(new TreePath(copy.getCompilationUnit()), clazzTree);
                List<VariableElement> vars = new LinkedList<VariableElement>();

                for (Tree m : clazzTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        vars.add((VariableElement) copy.getTrees().getElement(new TreePath(clazz, m)));
                    }
                }

                EqualsHashCodeGenerator.generateEqualsAndHashCode(copy, clazz, vars, vars, -1);
            }
        }

        TaskImpl t = new TaskImpl();

        js.runModificationTask(t).commit();

        DataObject dObj = DataObject.find(java);
        EditorCookie ec = dObj != null ? dObj.getCookie(org.openide.cookies.EditorCookie.class) : null;
        Document doc = ec != null ? ec.getDocument() : null;
        
        String result = doc != null ? doc.getText(0, doc.getLength()) : TestUtilities.copyFileToString(FileUtil.toFile(java));
        String golden = "class X {\n" +
                        "  private float f;\n" +
                        "  private double d;\n" +
                        "  private long l;\n" +
                        "  private boolean b;\n" +
                        "  private char c;\n" +
                        "  private E e;\n" +
                        "    @Override\n" +
                        "    public int hashCode() {\n" +
                        "        int hash = 1;\n" +
                        "        hash = 1 * hash + Float.floatToIntBits(this.f);\n" +
                        "        hash = 1 * hash + (int) (Double.doubleToLongBits(this.d) ^ (Double.doubleToLongBits(this.d) >>> 32));\n" +
                        "        hash = 1 * hash + (int) (this.l ^ (this.l >>> 32));\n" +
                        "        hash = 1 * hash + (this.b ? 1 : 0);\n" +
                        "        hash = 1 * hash + this.c;\n" +
                        "        hash = 1 * hash + (this.e != null ? this.e.hashCode() : 0);\n" +
                        "        return hash;\n" +
                        "    }\n" +
                        "    @Override\n" +
                        "    public boolean equals(Object obj) {\n" +
                        "        if (this == obj) {\n" +
                        "            return true;\n" +
                        "        }\n" +
                        "        if (obj == null) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (getClass() != obj.getClass()) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        final X other = (X) obj;\n" +
                        "        if (Float.floatToIntBits(this.f) != Float.floatToIntBits(other.f)) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (Double.doubleToLongBits(this.d) != Double.doubleToLongBits(other.d)) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (this.l != other.l) {\n" +
                        "            return false;\n" +
                        "        }" +
                        "        if (this.b != other.b) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (this.c != other.c) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        return this.e == other.e;\n" +
                        "    }\n" +
                        "  public enum E {A, B;}\n" +
                        "}\n";

        result = result.replaceAll("[ \t\n]+", " ");
        golden = golden.replaceAll("[ \t\n]+", " ");

        assertEquals(golden, result);
    }

    public void test227171() throws Exception {
        sourceLevel = "1.7";
        
        EqualsHashCodeGenerator.randomNumber = 1;
        
        FileObject java = FileUtil.createData(fo, "X.java");
        final String what1 = "class X {\n" +
                             "  private final E e;\n" +
                             "  enum E {A}\n";
        String what2 =
            "}\n";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(java, what);

        JavaSource js = JavaSource.forFileObject(java);
        assertNotNull("Created", js);

        class TaskImpl implements Task<WorkingCopy> {
            @Override public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(JavaSource.Phase.RESOLVED);
                ClassTree clazzTree = (ClassTree) copy.getCompilationUnit().getTypeDecls().get(0);
                TreePath clazz = new TreePath(new TreePath(copy.getCompilationUnit()), clazzTree);
                List<VariableElement> vars = new LinkedList<>();

                for (Tree m : clazzTree.getMembers()) {
                    if (m.getKind() == Kind.VARIABLE) {
                        vars.add((VariableElement) copy.getTrees().getElement(new TreePath(clazz, m)));
                    }
                }

                EqualsHashCodeGenerator.generateEqualsAndHashCode(copy, clazz, vars, vars, -1);
            }
        }

        TaskImpl t = new TaskImpl();

        js.runModificationTask(t).commit();

        DataObject dObj = DataObject.find(java);
        EditorCookie ec = dObj != null ? dObj.getLookup().lookup(org.openide.cookies.EditorCookie.class) : null;
        Document doc = ec != null ? ec.getDocument() : null;
        
        String result = doc != null ? doc.getText(0, doc.getLength()) : TestUtilities.copyFileToString(FileUtil.toFile(java));
        String golden = "\nimport java.util.Objects;\n" +
                        "class X {\n" +
                        "  private final E e;\n" +
                        "    @Override\n" +
                        "    public int hashCode() {\n" +
                        "        int hash = 1;\n" +
                        "        hash = 1 * hash + Objects.hashCode(this.e);\n" +
                        "        return hash;\n" +
                        "    }\n" +
                        "    @Override\n" +
                        "    public boolean equals(Object obj) {\n" +
                        "        if (this == obj) {\n" +
                        "            return true;\n" +
                        "        }\n" +
                        "        if (obj == null) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        if (getClass() != obj.getClass()) {\n" +
                        "            return false;\n" +
                        "        }\n" +
                        "        final X other = (X) obj;\n" +
                        "        return this.e == other.e;\n" +
                        "    }\n" +
                        "  enum E {A}\n" +
                        "}\n";

        result = result.replaceAll("[ \t\n]+", " ");
        golden = golden.replaceAll("[ \t\n]+", " ");
        
        assertEquals(golden, result);
    }
    
    private static final class DD extends DialogDisplayer {

        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            descriptor.setValue(descriptor.getDefaultValue());
            
            return new JDialog() {
                public void setVisible(boolean b) {
                }
            };
        }
        
    }
}
