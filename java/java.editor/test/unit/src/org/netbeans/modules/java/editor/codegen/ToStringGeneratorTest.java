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

import java.awt.Dialog;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
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
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author junichi11
 */
public class ToStringGeneratorTest extends NbTestCase {

    private FileObject fo;
    private String sourceLevel;

    public ToStringGeneratorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fo = SourceUtilsTestUtil.makeScratchDir(this);
        System.setProperty("netbeans.user", getWorkDirPath());
        SourceUtilsTestUtil.setLookup(new Object[]{new ToStringGeneratorTest.DD(), new SourceLevelQueryImplementation2() {
            @Override
            public SourceLevelQueryImplementation2.Result getSourceLevel(FileObject javaFile) {
                return new Result() {
                    @Override
                    public String getSourceLevel() {
                        return sourceLevel != null ? sourceLevel : "1.5";
                    }

                    @Override
                    public void addChangeListener(ChangeListener listener) {
                    }

                    @Override
                    public void removeChangeListener(ChangeListener listener) {
                    }
                };
            }
        }, BootClassPathUtil.getBootClassPathProvider()}, getClass().getClassLoader());
    }

    public void testToStringWithPlusOperator() throws Exception {
        FileObject javaFile = FileUtil.createData(fo, "NewClass.java");
        String what1 = ""
                + "public class NewClass {\n"
                + "    private final String test1 = \"test\";\n"
                + "    private final String test2 = \"test\";\n"
                + "    private final String test3 = \"test\";\n";

        String what2 = ""
                + "\n"
                + "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(javaFile, what);

        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        assertNotNull("Created", javaSource);

        Document doc = getDocuemnt(javaFile);

        final JTextArea component = new JTextArea(doc);
        component.setCaretPosition(what1.length());

        class Task implements org.netbeans.api.java.source.Task<CompilationController> {

            private ToStringGenerator generator;

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("NewClass");
                generator = ToStringGenerator.createToStringGenerator(component, controller, typeElement, false);
            }

            public void post() throws Exception {
                assertNotNull("Created", generator);

                assertEquals("Three fields", 3, generator.getDescription().getSubs().size());
                assertEquals("test1 field selected", true, generator.getDescription().getSubs().get(0).isSelected());
                assertEquals("test2 field selected", true, generator.getDescription().getSubs().get(1).isSelected());
                assertEquals("test3 field selected", true, generator.getDescription().getSubs().get(2).isSelected());
                assertEquals("Don't use StringBuilder", false, generator.useStringBuilder());
            }
        }
        final Task task = new Task();

        javaSource.runUserActionTask(task, false);
        task.post();

        SwingUtilities.invokeAndWait(() -> task.generator.invoke());

        Document document = component.getDocument();
        String text = document.getText(0, document.getLength());
        String expected = ""
                + "public class NewClass {\n"
                + "    private final String test1 = \"test\";\n"
                + "    private final String test2 = \"test\";\n"
                + "    private final String test3 = \"test\";\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"NewClass{\" + \"test1=\" + test1 + \", test2=\" + test2 + \", test3=\" + test3 + '}';\n"
                + "    }\n"
                + "\n"
                + "}";
        assertEquals(expected, text);
    }

    public void testToStringWithStringBuilder() throws Exception {
        FileObject javaFile = FileUtil.createData(fo, "NewClass.java");
        String what1 = ""
                + "public class NewClass {\n"
                + "    private final String test1 = \"test\";\n"
                + "    private final String test2 = \"test\";\n"
                + "    private final String test3 = \"test\";\n";

        String what2 = ""
                + "\n"
                + "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(javaFile, what);

        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        assertNotNull("Created", javaSource);

        Document doc = getDocuemnt(javaFile);

        final JTextArea component = new JTextArea(doc);
        component.setCaretPosition(what1.length());

        class Task implements org.netbeans.api.java.source.Task<CompilationController> {

            private ToStringGenerator generator;

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("NewClass");
                generator = ToStringGenerator.createToStringGenerator(component, controller, typeElement, true);
            }

            public void post() throws Exception {
                assertNotNull("Created", generator);

                assertEquals("Three fields", 3, generator.getDescription().getSubs().size());
                assertEquals("test1 field selected", true, generator.getDescription().getSubs().get(0).isSelected());
                assertEquals("test2 field selected", true, generator.getDescription().getSubs().get(1).isSelected());
                assertEquals("test3 field selected", true, generator.getDescription().getSubs().get(2).isSelected());
                assertEquals("Use StringBuilder", true, generator.useStringBuilder());
            }
        }
        final Task task = new Task();

        javaSource.runUserActionTask(task, false);
        task.post();

        SwingUtilities.invokeAndWait(() -> task.generator.invoke());

        Document document = component.getDocument();
        String text = document.getText(0, document.getLength());
        String expected = ""
                + "public class NewClass {\n"
                + "    private final String test1 = \"test\";\n"
                + "    private final String test2 = \"test\";\n"
                + "    private final String test3 = \"test\";\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        StringBuilder sb = new StringBuilder();\n"
                + "        sb.append(\"NewClass{\");\n"
                + "        sb.append(\"test1=\").append(test1);\n"
                + "        sb.append(\", test2=\").append(test2);\n"
                + "        sb.append(\", test3=\").append(test3);\n"
                + "        sb.append('}');\n"
                + "        return sb.toString();\n"
                + "    }\n"
                + "\n"
                + "}";
        assertEquals(expected, text);
    }

    public void testEnumToString() throws Exception {
        String name = "NewEnum";
        String code = ""
                + "public enum NewEnum {\n"
                + "    A, B;\n"
                + "    private final String test1 = \"test\";\n"
                + "|\n"
                + "}";
        String expected = ""
                + "public enum NewEnum {\n"
                + "    A, B;\n"
                + "    private final String test1 = \"test\";\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"NewEnum{\" + \"ordinal=\" + ordinal() + \", name=\" + name() + \", test1=\" + test1 + '}';\n"
                + "    }\n"
                + "\n"
                + "}";

        runTest(name, code, expected);
    }

    public void testRecordToString() throws Exception {
        String name = "NewRecord";
        String code = ""
                + "public record NewRecord(int x, int y) {\n"
                + "|\n"
                + "}";
        String expected = ""
                + "public record NewRecord(int x, int y) {\n"
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        return \"NewRecord{\" + \"x=\" + x + \", y=\" + y + '}';\n"
                + "    }\n"
                + "\n"
                + "}";

        runTest(name, code, expected);
    }

    public void testToStringExists() throws Exception {
        FileObject javaFile = FileUtil.createData(fo, "NewClass.java");
        String what1 = ""
                + "public class NewClass {\n"
                + "    private final String test1 = \"test\";\n"
                + "    private final String test2 = \"test\";\n"
                + "    private final String test3 = \"test\";\n";

        String what2 = ""
                + "\n"
                + "    @Override\n"
                + "    public String toString() {\n"
                + "        StringBuilder sb = new StringBuilder();\n"
                + "        sb.append(\"NewClass{\");\n"
                + "        sb.append(\"test1=\").append(test1);\n"
                + "        sb.append(\", \");\n"
                + "        sb.append(\"test2=\").append(test2);\n"
                + "        sb.append(\", \");\n"
                + "        sb.append(\"test3=\").append(test3);\n"
                + "        sb.append('}');\n"
                + "        return sb.toString();\n"
                + "    }\n"
                + "\n"
                + "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(javaFile, what);

        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        assertNotNull("Created", javaSource);

        Document doc = getDocuemnt(javaFile);

        final JTextArea component = new JTextArea(doc);
        component.setCaretPosition(what1.length());

        class Task implements org.netbeans.api.java.source.Task<CompilationController> {

            private ToStringGenerator generator;

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("NewClass");
                generator = ToStringGenerator.createToStringGenerator(component, controller, typeElement, true);
            }

            public void post() throws Exception {
                assertNull("Not created", generator);
            }
        }
        final Task task = new Task();

        javaSource.runUserActionTask(task, false);
        task.post();
    }

    public void testToStringCanNotUseStringBuilder() throws Exception {
        sourceLevel = "1.4";
        FileObject javaFile = FileUtil.createData(fo, "NewClass.java");
        String what1 = ""
                + "public class NewClass {\n"
                + "    private final String test1 = \"test\";\n"
                + "    private final String test2 = \"test\";\n"
                + "    private final String test3 = \"test\";\n";

        String what2 = ""
                + "\n"
                + "}";
        String what = what1 + what2;
        GeneratorUtilsTest.writeIntoFile(javaFile, what);

        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        assertNotNull("Created", javaSource);

        Document doc = getDocuemnt(javaFile);

        final JTextArea component = new JTextArea(doc);
        component.setCaretPosition(what1.length());

        class Task implements org.netbeans.api.java.source.Task<CompilationController> {

            private ToStringGenerator generator;

            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement("NewClass");
                generator = ToStringGenerator.createToStringGenerator(component, controller, typeElement, true);
            }

            public void post() throws Exception {
                assertNotNull("Created", generator);

                assertEquals("Three fields", 3, generator.getDescription().getSubs().size());
                assertEquals("test1 field selected", true, generator.getDescription().getSubs().get(0).isSelected());
                assertEquals("test2 field selected", true, generator.getDescription().getSubs().get(1).isSelected());
                assertEquals("test3 field selected", true, generator.getDescription().getSubs().get(2).isSelected());
                assertEquals("Don't use StringBuilder", true, generator.useStringBuilder());
            }
        }
        final Task task = new Task();

        javaSource.runUserActionTask(task, false);
        task.post();

        SwingUtilities.invokeAndWait(() -> task.generator.invoke());

        Document document = component.getDocument();
        String text = document.getText(0, document.getLength());
        String expected = ""
                + "public class NewClass {\n"
                + "    private final String test1 = \"test\";\n"
                + "    private final String test2 = \"test\";\n"
                + "    private final String test3 = \"test\";\n"
                + "\n"
                + "    public String toString() {\n"
                + "        return \"NewClass{\" + \"test1=\" + test1 + \", test2=\" + test2 + \", test3=\" + test3 + '}';\n"
                + "    }\n"
                + "\n"
                + "}";
        assertEquals(expected, text);
    }

    private void runTest(String name, String code, String expected) throws Exception {
        int caret = code.indexOf("|");
        FileObject javaFile = FileUtil.createData(fo, name+".java");
        GeneratorUtilsTest.writeIntoFile(javaFile, code.replace("|", ""));

        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        assertNotNull("Created", javaSource);

        Document doc = getDocuemnt(javaFile);

        JTextArea component = new JTextArea(doc);
        component.setCaretPosition(caret);

        class Task implements org.netbeans.api.java.source.Task<CompilationController> {
            private ToStringGenerator generator;
            @Override
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(name);
                generator = ToStringGenerator.createToStringGenerator(component, controller, typeElement, false);
            }

        }
        Task task = new Task();
        javaSource.runUserActionTask(task, false);
        SwingUtilities.invokeAndWait(() -> task.generator.invoke());

        Document document = component.getDocument();
        String text = document.getText(0, document.getLength());
        assertEquals(expected, text);
    }

    private static Document getDocuemnt(FileObject fileObject) throws DataObjectNotFoundException, IOException {
        DataObject dataObject = DataObject.find(fileObject);
        EditorCookie ec = dataObject.getLookup().lookup(org.openide.cookies.EditorCookie.class);
        return ec.openDocument();
    }

    private static final class DD extends DialogDisplayer {

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            descriptor.setValue(descriptor.getDefaultValue());

            return new JDialog() {
                @Override
                public void setVisible(boolean b) {
                }
            };
        }

    }

}
