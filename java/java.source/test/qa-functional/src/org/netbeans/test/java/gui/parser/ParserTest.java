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
package org.netbeans.test.java.gui.parser;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.event.KeyEvent;
import org.netbeans.junit.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.actions.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import junit.framework.Test;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.test.java.JavaTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests Java Parser.
 *
 * @author Roman Strobl, Jiri Prox
 */
public class ParserTest extends JavaTestCase {

    /**
     * Switch to generate golden files
     */
    private boolean generateGoledenFiles = false;

    // name of sample project
    private static final String TEST_PROJECT_NAME = "default";

    // name of sample package
    private static String testPackageName;

    // name of sample class
    private static String testClassName;

    // workdir, default /tmp, changed to NBJUnit workdir during test
    private String workDir = "/tmp";

    private static String projectDir;

    private File classPathWorkDir;

    private FileObject artefact;

    private FileObject testFileObject;

    /**
     * Main method for standalone execution.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Sets up logging facilities.
     */
    public void setUp() {
        openDefaultProject();
        System.out.println("########  " + getName() + "  #######");
        try {
            File wd = getWorkDir();
            workDir = wd.toString();
        } catch (IOException e) {
        }
        classPathWorkDir = new File(getDataDir(), ("projects." + TEST_PROJECT_NAME + ".src").replace('.', File.separatorChar));
        artefact = FileUtil.toFileObject(classPathWorkDir);
        testPackageName = getClass().getPackage().getName() + "." + getClass().getSimpleName();
        testClassName = getName();
        getLog().println("testPackage=" + testPackageName + " testClass=" + testClassName);
        testFileObject = artefact.getFileObject((testPackageName + "." + testClassName).replace(".", "/") + ".java");
    }

    public void tearDown() {
        System.out.println("########  " + getName() + "  #######");
        if (generateGoledenFiles) {
            fail("Passive mode");
        }
    }

    /**
     * Creates a new instance of ParserTest
     *
     * @param testName name of test
     */
    public ParserTest(String testName) {
        super(testName);
    }

    private void dumpStructure(JavaSource js, final PrintStream ps, final String text) throws IOException {
        CancellableTask task = new CancellableTask<CompilationController>() {
            ElementVisitor scanner;

            public void cancel() {
                scanner.cancel();
            }

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(Phase.RESOLVED);
                CompilationUnitTree cuTree = parameter.getCompilationUnit();
                List<? extends Tree> typeDecls = cuTree.getTypeDecls();
                TreePath cuPath = new TreePath(cuTree);
                List<Element> elements = new ArrayList<Element>(typeDecls.size());
                for (Tree t : typeDecls) {
                    TreePath p = new TreePath(cuPath, t);
                    Element e = parameter.getTrees().getElement(p);
                    if (e != null) {
                        elements.add(e);
                    }
                }
                scanner = new ElementVisitor(parameter, text);
                for (Element element : elements) {
                    scanner.scan(element, ps);
                }
            }

        };
        js.runModificationTask(task).commit();
    }

    public File getRefFile(int n) throws IOException {
        File f = new File(getWorkDir(), getName() + n + ".ref");
        return f;
    }

    public File getGoldenFile(int n) {
        return super.getGoldenFile(getName() + n + ".pass");
    }

    public File getNewGoldenFile(int n) {
        String fileName = "data/goldenfiles/" + testPackageName.replace('.', '/') + "/" + testClassName + n + ".pass";
        File f = new File(getDataDir().getParentFile().getParentFile().getParentFile(), fileName);
        f.getParentFile().mkdirs();
        return f;
    }

    public void compareGoldenFile(int n) {
        if (generateGoledenFiles) {
            try {
                File nGolden = getNewGoldenFile(n);
                File ref = getRefFile(n);
                BufferedReader br = new BufferedReader(new FileReader(ref));
                FileWriter fw = new FileWriter(nGolden);
                String line;
                while ((line = br.readLine()) != null) {
                    fw.write(line + "\n");
                }
                fw.close();
                br.close();
            } catch (IOException ioe) {
                //fail(ioe.getMessage());
            }
        } else {
            try {
                assertFile(getRefFile(n), getGoldenFile(n), new File(getWorkDir(), getName() + n + ".diff"));
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }

    private EditorOperator getDefaultEditor() {
        Node pn = new ProjectsTabOperator().getProjectRootNode(
                TEST_PROJECT_NAME);
        pn.select();

        Node n = new Node(pn, org.netbeans.jellytools.Bundle.getString(
                "org.netbeans.modules.java.j2seproject.Bundle",
                "NAME_src.dir") + "|" + testPackageName + "|"
                + testClassName);

        n.select();
        new OpenAction().perform();

        EditorOperator editor = new EditorOperator(testClassName);
        return editor;
    }

    /**
     * Tests parsing of inner class.
     */
    public void testCreateInnerClass() throws IOException {
        EditorOperator editor = getDefaultEditor();
        try {
            editor.insert("    public class InnerClass {\n        int a;\n    }\n", 48, 1);
            JavaSource js = JavaSource.forFileObject(testFileObject);
            FileWriter fw = new FileWriter(getRefFile(1));
            FileOutputStream fos = new FileOutputStream(getRefFile(1));
            PrintStream ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(1);
        } finally {
            editor.closeDiscard();
        }
    }

    /**
     * Tests parsing of inner interface.
     */
    public void testCreateInnerInterface() throws IOException {
        EditorOperator editor = getDefaultEditor();
        try {
            editor.insert("public interface Iface {\n        String method();\n }\n", 48, 1);
            JavaSource js = JavaSource.forFileObject(testFileObject);
            FileWriter fw = new FileWriter(getRefFile(1));
            FileOutputStream fos = new FileOutputStream(getRefFile(1));
            PrintStream ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(1);
        } finally {
            editor.closeDiscard();
        }
    }

    /**
     * Tests parsing of constructor.
     */
    public void testCreateConstructor() throws IOException {
        EditorOperator editor = getDefaultEditor();
        try {
            editor.insert("public testCreateConstructor(String param) { }\n", 48, 1);
            JavaSource js = JavaSource.forFileObject(testFileObject);
            FileWriter fw = new FileWriter(getRefFile(1));
            FileOutputStream fos = new FileOutputStream(getRefFile(1));
            PrintStream ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(1);
            editor.deleteLine(48);
            fos = new FileOutputStream(getRefFile(2));
            ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(2);
        } finally {
            editor.closeDiscard();
        }
    }

    /**
     * Tests parsing of a field.
     */
    public void testCreateField() throws IOException {
        EditorOperator editor = getDefaultEditor();
        try {
            editor.insert("String field;\n", 48, 1);
            JavaSource js = JavaSource.forFileObject(testFileObject);
            FileWriter fw = new FileWriter(getRefFile(1));
            FileOutputStream fos = new FileOutputStream(getRefFile(1));
            PrintStream ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(1);
            editor.deleteLine(48);
            fos = new FileOutputStream(getRefFile(2));
            ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(2);
        } finally {
            editor.closeDiscard();
        }

    }

    /**
     * Tests parsing of a method.
     */
    public void testCreateMethod() throws IOException {
        EditorOperator editor = getDefaultEditor();
        try {
            editor.insert("private int method(Object obj) { return 1; }\n", 48, 1);
            JavaSource js = JavaSource.forFileObject(testFileObject);
            FileWriter fw = new FileWriter(getRefFile(1));
            FileOutputStream fos = new FileOutputStream(getRefFile(1));
            PrintStream ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(1);
            editor.deleteLine(48);
            fos = new FileOutputStream(getRefFile(2));
            ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(2);
        } finally {
            editor.closeDiscard();
        }
    }

    /**
     * Tests parsing of outer class.
     */
    public void testCreateOuterClass() throws IOException {
        EditorOperator editor = getDefaultEditor();
        try {
            editor.insert("class Outer { }\n", 53, 1);
            JavaSource js = JavaSource.forFileObject(testFileObject);
            FileWriter fw = new FileWriter(getRefFile(1));
            FileOutputStream fos = new FileOutputStream(getRefFile(1));
            PrintStream ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(1);
            editor.deleteLine(53);
            fos = new FileOutputStream(getRefFile(2));
            ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(2);
        } finally {
            editor.closeDiscard();
        }

    }

    /**
     * Tests parsing of outer interface.
     */
    public void testCreateOuterInterface() throws IOException {
        EditorOperator editor = getDefaultEditor();
        try {
            editor.insert("interface Iface{ }\n", 53, 1);
            JavaSource js = JavaSource.forFileObject(testFileObject);
            FileOutputStream fos = new FileOutputStream(getRefFile(1));
            PrintStream ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(1);
            editor.deleteLine(53);
            fos = new FileOutputStream(getRefFile(2));
            ps = new PrintStream(fos);
            dumpStructure(js, ps, editor.getText());
            compareGoldenFile(2);
        } finally {
            editor.closeDiscard();
        }

    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ParserTest.class).enableModules(".*").clusters(".*"));
    }
}
