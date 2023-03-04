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
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldsRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Ruijs
 */
public class EncapsulateFieldsTest extends RefactoringTestBase {

    public EncapsulateFieldsTest(String name) {
        super(name);
    }
    
    public void test253363() throws Exception { // #219140 - Encapsulate Field setter with PCS does not throw property changes
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public int i;\n"
                + "\n"
                + "    public void foo() {\n"
                + "        (new A().i) = 5;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public void foo() {\n"
                + "        (new A()).setI(5);\n"
                + "        System.out.println(getI());\n"
                + "    }\n"
                + "\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(int i) {\n"
                + "        this.i = i;\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test219140() throws Exception { // #219140 - Encapsulate Field setter with PCS does not throw property changes
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public int i;\n"
                + "\n"
                + "    public void foo() {\n"
                + "        i = 5;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), true);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "import java.beans.PropertyChangeSupport;\n"
                + "public class A {\n"
                + "    public static final String PROP_I = \"i\";\n"
                + "    private int i;\n"
                + "    private final transient PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);\n"
                + "\n"
                + "    public void foo() {\n"
                + "        setI(5);\n"
                + "        System.out.println(getI());\n"
                + "    }\n"
                + "\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(int i) {\n"
                + "        int oldI = this.i;\n"
                + "        this.i = i;\n"
                + "        propertyChangeSupport.firePropertyChange(PROP_I, oldI, i);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testEncapsulateFieldsGroup() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap; public class A {\n"
                        + "    private String j = \"three\";\n"
                        + "\n"
                        + "    public String getSequence() {\n"
                        + "        String[] s = new String[]{\n"
                        + "            \"one\",   // comment one\n"
                        + "            \"three\", // comment two\n"
                        + "            j          // comment three\n"
                        + "        };\n"
                        + "        return Arrays.toString(s);\n"
                        + "    }\n"
                        + "}"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap; public class A {\n"
                + "    private String j = \"three\";\n"
                + "\n"
                + "    public String getSequence() {\n"
                + "        String[] s = new String[]{\n"
                + "            \"one\",   // comment one\n"
                + "            \"three\", // comment two\n"
                + "            getJ()     // comment three\n"
                + "        };\n"
                + "        return Arrays.toString(s);\n"
                + "    }\n"
                + "    public String getJ() { return j; }\n"
                + "    public void setJ(String j) { this.j = j; } }"));
    }

    public void testEncapsulateFields() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap; public class A { public int i; public int j; }"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0, 1}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap; public class A { private int i; private int j;\n"
                + "public int getI() { return i; }\n"
                + "public void setI(int i) { this.i = i; }\n"
                + "public int getJ() { return j; }\n"
                + "public void setJ(int j) { this.j = j; } }"));
    }
    
    public void testEncapsulateStaticFields() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap; public class A { public Object i; public static Object j; }"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0, 1}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap; public class A {\n"
                + "public static Object getJ() { return j; }\n"
                + "public static void setJ(Object aJ) { j = aJ; }\n"
                + "private Object i;\n"
                + "private static Object j;\n"
                + "public Object getI() { return i; }\n"
                + "public void setI(Object i) { this.i = i; }\n"
                + "}"));
    }

    public void testSelfEncapsulateFields() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public int i;\n"
                + "\n"
                + "    public void foo() {\n"
                + "        i = 5;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public void foo() {\n"
                + "        setI(5);\n"
                + "        System.out.println(getI());\n"
                + "    }\n"
                + "\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(int i) {\n"
                + "        this.i = i;\n"
                + "    }\n"
                + "}\n"));
    }

    public void testEncapsulateFieldsReferences() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public int i;\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B {\n"
                + "    private A a;\n"
                + "    B() {\n"
                + "        a = new A();\n"
                + "    }\n"
                + "\n"
                + "    public void foo() {\n"
                + "        a.i = 5;\n"
                + "        System.out.println(a.i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(int i) {\n"
                + "        this.i = i;\n"
                + "    }\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B {\n"
                + "    private A a;\n"
                + "    B() {\n"
                + "        a = new A();\n"
                + "    }\n"
                + "\n"
                + "    public void foo() {\n"
                + "        a.setI(5);\n"
                + "        System.out.println(a.getI());\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testEncapsulateFieldsSubclass() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public int i;\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        i = 5;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(int i) {\n"
                + "        this.i = i;\n"
                + "    }\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        setI(5);\n"
                + "        System.out.println(getI());\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testEncapsulateFieldsCompound() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public int i;\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        (i)++;\n"
                + "        i += 5;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(int i) {\n"
                + "        this.i = i;\n"
                + "    }\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        setI(getI() + 1);\n"
                + "        setI(getI() + 5);\n"
                + "        System.out.println(getI());\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testEncapsulateFieldsCompoundByte() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public byte i;\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        (i)++;\n"
                + "        i += 5;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private byte i;\n"
                + "\n"
                + "    public byte getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(byte i) {\n"
                + "        this.i = i;\n"
                + "    }\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        setI((byte) (getI() + 1));\n"
                + "        setI((byte) (getI() + 5));\n"
                + "        System.out.println(getI());\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testEncapsulateFieldsThisSuper() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    public int i;\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        super.i++;\n"
                + "        this.i += 5;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "\n"
                + "    public void setI(int i) {\n"
                + "        this.i = i;\n"
                + "    }\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "public class B extends A {\n"
                + "\n"
                + "    public void foo() {\n"
                + "        super.setI(super.getI() + 1);\n"
                + "        this.setI(this.getI() + 5);\n"
                + "        System.out.println(getI());\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test108473a() throws Exception { // #108473
        writeFilesAndWaitForScan(src, new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private String theField;\n"
                + "    public static void main(String[] args) {\n"
                + "        B b = new A().new B();\n"
                + "        System.out.println(b.getTheField());\n"
                + "    }\n"
                + "\n"
                + "    private class B extends A {\n"
                + "        private String getTheField() {\n"
                + "            return theField;\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false, new Problem(false, "ERR_EncapsulateAccessOverGetter"));
        verifyContent(src, new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private String theField;\n"
                + "    public static void main(String[] args) {\n"
                + "        B b = new A().new B();\n"
                + "        System.out.println(b.getTheField());\n"
                + "    }\n"
                + "\n"
                + "    public String getTheField() {\n"
                + "        return theField;\n"
                + "    }\n"
                + "    public void setTheField(String theField) {\n"
                + "        this.theField = theField;\n"
                + "    }\n"
                + "    private class B extends A {\n"
                + "        private String getTheField() {\n"
                + "            return getTheField();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test108473b() throws Exception { // #108473
        writeFilesAndWaitForScan(src, new File("encap/A.java", "package encap;\n"
                + "public class A extends B {\n"
                + "    private String theField;\n"
                + "    public static void main(String[] args) {\n"
                + "        B b = new A().new B();\n"
                + "        System.out.println(b.getTheField());\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public String getTheField() {\n"
                + "        return \"\";\n"
                + "    }\n"
                + "}\n"
                + "\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PROTECTED), false, new Problem(false, "ERR_EncapsulateAccessGetter"));
        verifyContent(src, new File("encap/A.java", "package encap;\n"
                + "public class A extends B {\n"
                + "    private String theField;\n"
                + "    public static void main(String[] args) {\n"
                + "        B b = new A().new B();\n"
                + "        System.out.println(b.getTheField());\n"
                + "    }\n"
                + "    protected String getTheField() {\n"
                + "        return theField;\n"
                + "    }\n"
                + "    protected void setTheField(String theField) {\n"
                + "        this.theField = theField;\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public String getTheField() {\n"
                + "        return \"\";\n"
                + "    }\n"
                + "}\n"
                + "\n"));
    }
    
    public void test217262() throws Exception { // #217262
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap; import java.util.List; public class A { public List<String> i; public List<Double> j; public void setI(List<String> i) { this.i = i; } }"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0, 1}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap; import java.util.List; public class A { private List<String> i; private List<Double> j;\n"
                + "public void setI(List<String> i) { this.i = i; }\n"
                + "public List<String> getI() { return i; }\n"
                + "public List<Double> getJ() { return j; }\n"
                + "public void setJ(List<Double> j) { this.j = j; } }"));
    }

    /**
     * TODO: Test for issue #108489. The issue was closed, but the case still fails.
     */
    public void FAILtest108489() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    String f;\n"
                + "    public static void main(String[] args) {\n"
                + "        B b=new B();\n"
                + "        b.setF(\"abcd\");\n"
                + "    }\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "class B extends A {\n"
                + "    public int setF(  String theField){\n"
                + "        this.f=theField;\n"
                + "        return 2;\n"
                + "    }\n"
                + "}\n"));
        performEncapsulate(src.getFileObject("encap/A.java"), new int[]{0}, EnumSet.of(Modifier.PUBLIC), false);
        verifyContent(src,
                new File("encap/A.java", "package encap;\n"
                + "public class A {\n"
                + "    private String f;\n"
                + "    public static void main(String[] args) {\n"
                + "        B b=new B();\n"
                + "        b.setF(\"abcd\");\n"
                + "    }\n"
                + "    public String getF() {\n"
                + "        return f;\n"
                + "    }\n"
                + "    public void setF(String f) {\n"
                + "        this.f = f;\n"
                + "    }\n"
                + "}\n"),
                new File("encap/B.java", "package encap;\n"
                + "class B extends A {\n"
                + "    public int setF(String theField){\n"
                + "        super.setF(theField);\n"
                + "        return 2;\n"
                + "    }\n"
                + "}\n"));
    }

    private void performEncapsulate(FileObject source, final int[] position, final EnumSet<Modifier> methodModifiers, final boolean isGeneratePropertyChangeSupport, Problem... expectedProblems) throws IOException, IllegalArgumentException, InterruptedException {
        final EncapsulateFieldsRefactoring[] r = new EncapsulateFieldsRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController info) throws Exception {
                info.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = info.getCompilationUnit();
                CodeStyle cs = DiffContext.getCodeStyle(info);
                final ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                final TreePath classPath = info.getTrees().getPath(cut, classTree);
                TypeElement classEl = (TypeElement) info.getTrees().getElement(classPath);
                List<? extends Element> allMembers = info.getElements().getAllMembers(classEl);
                List<VariableElement> fieldsIn = ElementFilter.fieldsIn(allMembers);
                LinkedList<EncapsulateFieldsRefactoring.EncapsulateFieldInfo> fields = new LinkedList<EncapsulateFieldsRefactoring.EncapsulateFieldInfo>();
                for (int p : position) {
                    VariableElement field = fieldsIn.get(p);
                    boolean staticMod = field.getModifiers().contains(Modifier.STATIC);
                    String getName = CodeStyleUtils.computeGetterName(field.getSimpleName(), field.asType().getKind() == TypeKind.BOOLEAN, staticMod, cs);
                    String setName = CodeStyleUtils.computeSetterName(field.getSimpleName(), staticMod, cs);
                    EncapsulateFieldsRefactoring.EncapsulateFieldInfo encInfo = new EncapsulateFieldsRefactoring.EncapsulateFieldInfo(TreePathHandle.create(field, info), getName, setName);
                    fields.add(encInfo);
                }
                r[0] = new EncapsulateFieldsRefactoring(TreePathHandle.create(classEl, info));
                r[0].setAlwaysUseAccessors(true);
                r[0].setRefactorFields(fields);
                r[0].setFieldModifiers(EnumSet.of(Modifier.PRIVATE));
                r[0].setMethodModifiers(methodModifiers);
                r[0].setGeneratePropertyChangeSupport(isGeneratePropertyChangeSupport);
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