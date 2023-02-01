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
package org.netbeans.api.java.source.gen;

import org.netbeans.api.editor.settings.SimpleValueNames;
import java.util.prefs.Preferences;
import com.sun.source.tree.Tree;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.GeneratorUtilities;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.StyledDocument;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.guards.JavaGuardedSectionsFactory;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.editor.guards.GuardedEditorSupport;
import org.netbeans.spi.editor.guards.GuardedSectionsFactory;
import org.netbeans.spi.editor.guards.GuardedSectionsProvider;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.LifecycleManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.text.CloneableEditor;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.DataEditorSupport;
import org.openide.windows.CloneableOpenSupport;
import static org.netbeans.api.java.source.JavaSource.Phase.*;
import org.netbeans.modules.java.source.BootClassPathUtil;

/**
 * Regression tests for guarded exceptions.
 * 
 * @author Pavel Flaska
 */
public class GuardedBlockTest extends GeneratorTestMDRCompat {

    public GuardedBlockTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(GuardedBlockTest.class);
//        suite.addTest(new GuardedBlockTest("testInsertMethodBeforeVariablesBug177824"));
//        suite.addTest(new GuardedBlockTest("testAddMethodAfterVariables"));
//        suite.addTest(new GuardedBlockTest("test119048"));
//        suite.addTest(new GuardedBlockTest("test119962"));
//        suite.addTest(new GuardedBlockTest("testRenameTypeParameter125385"));
//        suite.addTest(new GuardedBlockTest("test119345"));
//        suite.addTest(new GuardedBlockTest("testComplex186754"));
        return suite;
    }
    
    /**
     * We need our own data loader to use guarded blocks.
     */
    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]); //to initialize the correct lookup instance
        FileUtil.setMIMEType("java", "text/x-java");
        MockMimeLookup.setInstances(
                MimePath.parse("text/x-java"),
                new JavaGuardedSectionsFactory(),
                new org.netbeans.modules.editor.NbEditorKit());
        MockMimeLookup mml = new MockMimeLookup();
        ClassPathProvider cpp = new ClassPathProvider() {
            public ClassPath findClassPath(FileObject file, String type) {
                if (type.equals(ClassPath.SOURCE))
                    return ClassPathSupport.createClassPath(new FileObject[] {FileUtil.toFileObject(getDataDir())});
                    if (type.equals(ClassPath.COMPILE))
                        return ClassPathSupport.createClassPath(new FileObject[0]);
                    if (type.equals(ClassPath.BOOT))
                        return BootClassPathUtil.getBootClassPath();
                    return null;
            }
        };
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[] {mml, new GuardedDataLoader(), cpp});
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
    /**
     * #177824: Guarded Exception
     */
    public void testInsertMethodBeforeVariablesBug177824() throws Exception {

        String source =
            "package test;\n" +
            "\n" +
            "import java.awt.event.ActionEvent;\n" +
            "import java.awt.event.ActionListener;\n" +
            "\n" +
            "public class Guarded1 implements ActionListener {\n" +
            "    \n" +
            "    private void fooActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fooActionPerformed\n" +
            "    }//GEN-LAST:event_fooActionPerformed\n" +
            "\n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private javax.swing.JButton jButton1;\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "}\n";

        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, source);
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        Document doc = editorCookie.openDocument();
        String golden =
            "package test;\n" +
            "\n" +
            "import java.awt.event.ActionEvent;\n" +
            "import java.awt.event.ActionListener;\n" +
            "\n" +
            "public class Guarded1 implements ActionListener {\n" +
            "    \n" +
            "    private void fooActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fooActionPerformed\n" +
            "    }//GEN-LAST:event_fooActionPerformed\n" +
            "\n" +
            "    public String toString() {\n" +
            "    }\n" +
            "\n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private javax.swing.JButton jButton1;\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree newMethod = make.Method(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "toString",
                        make.Type("java.lang.String"),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                );
                ClassTree copy = make.insertClassMember(clazz, 2, newMethod);
                workingCopy.rewrite(clazz, copy);
            }
        };
        src.runModificationTask(task).commit();
        editorCookie.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }

    /**
     * #90424: Guarded Exception
     */
    public void testAddMethodAfterVariables() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication5;\n" +
            "\n" +
            "import java.awt.event.ActionEvent;\n" +
            "import java.awt.event.ActionListener;\n" +
            "\n" +
            "public class Guarded1 implements ActionListener {\n" +
            "    \n" +
            "    public Guarded1() {\n" +
            "    }\n" +
            "    \n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private javax.swing.JButton jButton1;\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "}\n"
        );
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        Document doc = editorCookie.openDocument();
        String golden = 
            "package javaapplication5;\n" +
            "\n" +
            "import java.awt.event.ActionEvent;\n" +
            "import java.awt.event.ActionListener;\n" +
            "\n" +
            "public class Guarded1 implements ActionListener {\n" +
            "    \n" +
            "    public Guarded1() {\n" +
            "    }\n" +
            "    \n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private javax.swing.JButton jButton1;\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree newMethod = make.Method(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "actionPerformed",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>singletonList(
                            make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "e", 
                                make.Identifier("ActionEvent"), 
                            null)
                        ),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                ); 
                ClassTree copy = make.addClassMember(clazz, newMethod);
                workingCopy.rewrite(clazz, copy);
            }
        };
        src.runModificationTask(task).commit();
        editorCookie.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    /**
     * #119048: Guarded Exception
     */
    public void test119048() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package javaapplication5;\n" +
            "\n" +
            "public class NewJFrame extends javax.swing.JFrame {\n" +
            "\n" +
            "    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed\n" +
            "        // TODO add your handling code here:\n" +
            "        a = 3;\n" +
            "    }//GEN-LAST:event_jButton1ActionPerformed\n" +
            "\n" +
            "}"
        );
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        Document doc = editorCookie.openDocument();
        String golden = 
            "package javaapplication5;\n" +
            "\n" +
            "public class NewJFrame extends javax.swing.JFrame {\n" +
            "\n" +
            "    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed\n" +
            "        int a;\n" +
            "        // TODO add your handling code here:\n" +
            "        a = 3;\n" +
            "    }//GEN-LAST:event_jButton1ActionPerformed\n" +
            "\n" +
            "}";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                VariableTree var = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "a",
                        make.PrimitiveType(TypeKind.INT),
                        null
                    );
                BlockTree copy = make.insertBlockStatement(method.getBody(), 0, var);
                workingCopy.rewrite(method.getBody(), copy);
            }
        };
        src.runModificationTask(task).commit();
        editorCookie.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #119962: Guarded Exception
     */
    public void test119962() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package test;\n" +
            "\n" +
            "public class NewJFrame extends javax.swing.JFrame {\n" +
            "    private String str;\n" +
            "    \n" +
            "    // <editor-fold defaultstate=\"collapsed\" desc=\"Generated Code\">//GEN-BEGIN:initComponents\n" +
            "    private void initComponents() {\n" +
            "\n" +
            "        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);\n" +
            "\n" +
            "        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());\n" +
            "        getContentPane().setLayout(layout);\n" +
            "        layout.setHorizontalGroup(\n" +
            "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
            "            .addGap(0, 400, Short.MAX_VALUE)\n" +
            "        );\n" +
            "        layout.setVerticalGroup(\n" +
            "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
            "            .addGap(0, 300, Short.MAX_VALUE)\n" +
            "        );\n" +
            "\n" +
            "        pack();\n" +
            "    }// </editor-fold>//GEN-END:initComponents\n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "}"
        );
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        Document doc = editorCookie.openDocument();
        String golden = 
            "package test;\n" +
            "\n" +
            "public class NewJFrame extends javax.swing.JFrame {\n" +
            "    private String str;\n" +
            "    \n" +
            "    // <editor-fold defaultstate=\"collapsed\" desc=\"Generated Code\">//GEN-BEGIN:initComponents\n" +
            "    private void initComponents() {\n" +
            "\n" +
            "        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);\n" +
            "\n" +
            "        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());\n" +
            "        getContentPane().setLayout(layout);\n" +
            "        layout.setHorizontalGroup(\n" +
            "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
            "            .addGap(0, 400, Short.MAX_VALUE)\n" +
            "        );\n" +
            "        layout.setVerticalGroup(\n" +
            "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
            "            .addGap(0, 300, Short.MAX_VALUE)\n" +
            "        );\n" +
            "\n" +
            "        pack();\n" +
            "    }// </editor-fold>//GEN-END:initComponents\n" +
            "\n" +
            "    public void actionPerformed(ActionEvent e) {\n" +
            "    }\n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "}";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree newMethod = make.Method(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "actionPerformed",
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>singletonList(
                            make.Variable(
                                make.Modifiers(Collections.<Modifier>emptySet()),
                                "e", 
                                make.Identifier("ActionEvent"), 
                            null)
                        ),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                ); 
                workingCopy.rewrite(clazz, make.addClassMember(clazz, newMethod));
            }
        };
        src.runModificationTask(task).commit();
        editorCookie.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #119345: Duplicated initComponents() when trying to rename in
     * the guarded.
     */
    public void Dtest119345() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
                "package desktopapplication3;\n" +
                "\n" +
                "import org.jdesktop.application.Action;\n" +
                "\n" +
                "public class DesktopApplication3AboutBox extends javax.swing.JDialog {\n" +
                "\n" +
                "    public DesktopApplication3AboutBox(java.awt.Frame parent) {\n" +
                "        super(parent);\n" +
                "        initComponents();\n" +
                "        getRootPane().setDefaultButton(closeButton);\n" +
                "    }\n" +
                "\n" +
                "    @Action public void closeAboutBox() {\n" +
                "        setVisible(false);\n" +
                "    }\n" +
                "\n" +
                "    /** This method is called from within the constructor to\n" +
                "     * initialize the form.\n" +
                "     * WARNING: Do NOT modify this code. The content of this method is\n" +
                "     * always regenerated by the Form Editor.\n" +
                "     */\n" +
                "    // <editor-fold defaultstate=\"collapsed\" desc=\"Generated Code\">//GEN-BEGIN:initComponents\n" +
                "    private void initComponents() {\n" +
                "\n" +
                "        closeButton = new javax.swing.JButton();\n" +
                "        javax.swing.JLabel appTitleLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel versionLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appVersionLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel vendorLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appVendorLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel homepageLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appHomepageLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appDescLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel imageLabel = new javax.swing.JLabel();\n" +
                "\n" +
                "        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);\n" +
                "        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(desktopapplication3.DesktopApplication3.class).getContext().getResourceMap(DesktopApplication3AboutBox.class);\n" +
                "        setTitle(resourceMap.getString(\"title\")); // NOI18N\n" +
                "        setModal(true);\n" +
                "        setName(\"aboutBox\"); // NOI18N\n" +
                "        setResizable(false);\n" +
                "\n" +
                "        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(desktopapplication3.DesktopApplication3.class).getContext().getActionMap(DesktopApplication3AboutBox.class, this);\n" +
                "        closeButton.setAction(actionMap.get(\"closeAboutBox\")); // NOI18N\n" +
                "        closeButton.setName(\"closeButton\"); // NOI18N\n" +
                "\n" +
                "        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize()+4));\n" +
                "        appTitleLabel.setText(resourceMap.getString(\"Application.title\")); // NOI18N\n" +
                "        appTitleLabel.setName(\"appTitleLabel\"); // NOI18N\n" +
                "\n" +
                "        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));\n" +
                "        versionLabel.setText(resourceMap.getString(\"versionLabel.text\")); // NOI18N\n" +
                "        versionLabel.setName(\"versionLabel\"); // NOI18N\n" +
                "\n" +
                "        appVersionLabel.setText(resourceMap.getString(\"Application.version\")); // NOI18N\n" +
                "        appVersionLabel.setName(\"appVersionLabel\"); // NOI18N\n" +
                "\n" +
                "        vendorLabel.setFont(vendorLabel.getFont().deriveFont(vendorLabel.getFont().getStyle() | java.awt.Font.BOLD));\n" +
                "        vendorLabel.setText(resourceMap.getString(\"vendorLabel.text\")); // NOI18N\n" +
                "        vendorLabel.setName(\"vendorLabel\"); // NOI18N\n" +
                "\n" +
                "        appVendorLabel.setText(resourceMap.getString(\"Application.vendor\")); // NOI18N\n" +
                "        appVendorLabel.setName(\"appVendorLabel\"); // NOI18N\n" +
                "\n" +
                "        homepageLabel.setFont(homepageLabel.getFont().deriveFont(homepageLabel.getFont().getStyle() | java.awt.Font.BOLD));\n" +
                "        homepageLabel.setText(resourceMap.getString(\"homepageLabel.text\")); // NOI18N\n" +
                "        homepageLabel.setName(\"homepageLabel\"); // NOI18N\n" +
                "\n" +
                "        appHomepageLabel.setText(resourceMap.getString(\"Application.homepage\")); // NOI18N\n" +
                "        appHomepageLabel.setName(\"appHomepageLabel\"); // NOI18N\n" +
                "\n" +
                "        appDescLabel.setText(resourceMap.getString(\"appDescLabel.text\")); // NOI18N\n" +
                "        appDescLabel.setName(\"appDescLabel\"); // NOI18N\n" +
                "\n" +
                "        imageLabel.setIcon(resourceMap.getIcon(\"imageLabel.icon\")); // NOI18N\n" +
                "        imageLabel.setName(\"imageLabel\"); // NOI18N\n" +
                "\n" +
                "        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());\n" +
                "        getContentPane().setLayout(layout);\n" +
                "        layout.setHorizontalGroup(\n" +
                "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "            .addGroup(layout.createSequentialGroup()\n" +
                "                .addComponent(imageLabel)\n" +
                "                .addGap(18, 18, 18)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)\n" +
                "                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()\n" +
                "                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "                            .addComponent(versionLabel)\n" +
                "                            .addComponent(vendorLabel)\n" +
                "                            .addComponent(homepageLabel))\n" +
                "                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "                            .addComponent(appVersionLabel)\n" +
                "                            .addComponent(appVendorLabel)\n" +
                "                            .addComponent(appHomepageLabel)))\n" +
                "                    .addComponent(appTitleLabel, javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "                    .addComponent(appDescLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)\n" +
                "                    .addComponent(closeButton))\n" +
                "                .addContainerGap())\n" +
                "        );\n" +
                "        layout.setVerticalGroup(\n" +
                "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "            .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)\n" +
                "            .addGroup(layout.createSequentialGroup()\n" +
                "                .addContainerGap()\n" +
                "                .addComponent(appTitleLabel)\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addComponent(appDescLabel)\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)\n" +
                "                    .addComponent(versionLabel)\n" +
                "                    .addComponent(appVersionLabel))\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)\n" +
                "                    .addComponent(vendorLabel)\n" +
                "                    .addComponent(appVendorLabel))\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)\n" +
                "                    .addComponent(homepageLabel)\n" +
                "                    .addComponent(appHomepageLabel))\n" +
                "                .addGap(19, 19, Short.MAX_VALUE)\n" +
                "                .addComponent(closeButton)\n" +
                "                .addContainerGap())\n" +
                "        );\n" +
                "\n" +
                "        pack();\n" +
                "    }// </editor-fold>//GEN-END:initComponents\n" +
                "    \n" +
                "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
                "    private javax.swing.JButton closeButton;\n" +
                "    // End of variables declaration//GEN-END:variables\n" +
                "    \n" +
                "}\n"
        );
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        Document doc = editorCookie.openDocument();
        String golden = 
                "package crystalball;\n" +
                "\n" +
                "import org.jdesktop.application.Action;\n" +
                "\n" +
                "public class DesktopApplication3AboutBox extends javax.swing.JDialog {\n" +
                "\n" +
                "    public DesktopApplication3AboutBox(java.awt.Frame parent) {\n" +
                "        super(parent);\n" +
                "        initComponents();\n" +
                "        getRootPane().setDefaultButton(closeButton);\n" +
                "    }\n" +
                "\n" +
                "    @Action public void closeAboutBox() {\n" +
                "        setVisible(false);\n" +
                "    }\n" +
                "\n" +
                "    /** This method is called from within the constructor to\n" +
                "     * initialize the form.\n" +
                "     * WARNING: Do NOT modify this code. The content of this method is\n" +
                "     * always regenerated by the Form Editor.\n" +
                "     */\n" +
                "    // <editor-fold defaultstate=\"collapsed\" desc=\"Generated Code\">//GEN-BEGIN:initComponents\n" +
                "    private void initComponents() {\n" +
                "\n" +
                "        closeButton = new javax.swing.JButton();\n" +
                "        javax.swing.JLabel appTitleLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel versionLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appVersionLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel vendorLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appVendorLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel homepageLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appHomepageLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel appDescLabel = new javax.swing.JLabel();\n" +
                "        javax.swing.JLabel imageLabel = new javax.swing.JLabel();\n" +
                "\n" +
                "        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);\n" +
                "        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(crystalball.DesktopApplication3.class).getContext().getResourceMap(DesktopApplication3AboutBox.class);\n" +
                "        setTitle(resourceMap.getString(\"title\")); // NOI18N\n" +
                "        setModal(true);\n" +
                "        setName(\"aboutBox\"); // NOI18N\n" +
                "        setResizable(false);\n" +
                "\n" +
                "        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(crystalball.DesktopApplication3.class).getContext().getActionMap(DesktopApplication3AboutBox.class, this);\n" +
                "        closeButton.setAction(actionMap.get(\"closeAboutBox\")); // NOI18N\n" +
                "        closeButton.setName(\"closeButton\"); // NOI18N\n" +
                "\n" +
                "        appTitleLabel.setFont(appTitleLabel.getFont().deriveFont(appTitleLabel.getFont().getStyle() | java.awt.Font.BOLD, appTitleLabel.getFont().getSize()+4));\n" +
                "        appTitleLabel.setText(resourceMap.getString(\"Application.title\")); // NOI18N\n" +
                "        appTitleLabel.setName(\"appTitleLabel\"); // NOI18N\n" +
                "\n" +
                "        versionLabel.setFont(versionLabel.getFont().deriveFont(versionLabel.getFont().getStyle() | java.awt.Font.BOLD));\n" +
                "        versionLabel.setText(resourceMap.getString(\"versionLabel.text\")); // NOI18N\n" +
                "        versionLabel.setName(\"versionLabel\"); // NOI18N\n" +
                "\n" +
                "        appVersionLabel.setText(resourceMap.getString(\"Application.version\")); // NOI18N\n" +
                "        appVersionLabel.setName(\"appVersionLabel\"); // NOI18N\n" +
                "\n" +
                "        vendorLabel.setFont(vendorLabel.getFont().deriveFont(vendorLabel.getFont().getStyle() | java.awt.Font.BOLD));\n" +
                "        vendorLabel.setText(resourceMap.getString(\"vendorLabel.text\")); // NOI18N\n" +
                "        vendorLabel.setName(\"vendorLabel\"); // NOI18N\n" +
                "\n" +
                "        appVendorLabel.setText(resourceMap.getString(\"Application.vendor\")); // NOI18N\n" +
                "        appVendorLabel.setName(\"appVendorLabel\"); // NOI18N\n" +
                "\n" +
                "        homepageLabel.setFont(homepageLabel.getFont().deriveFont(homepageLabel.getFont().getStyle() | java.awt.Font.BOLD));\n" +
                "        homepageLabel.setText(resourceMap.getString(\"homepageLabel.text\")); // NOI18N\n" +
                "        homepageLabel.setName(\"homepageLabel\"); // NOI18N\n" +
                "\n" +
                "        appHomepageLabel.setText(resourceMap.getString(\"Application.homepage\")); // NOI18N\n" +
                "        appHomepageLabel.setName(\"appHomepageLabel\"); // NOI18N\n" +
                "\n" +
                "        appDescLabel.setText(resourceMap.getString(\"appDescLabel.text\")); // NOI18N\n" +
                "        appDescLabel.setName(\"appDescLabel\"); // NOI18N\n" +
                "\n" +
                "        imageLabel.setIcon(resourceMap.getIcon(\"imageLabel.icon\")); // NOI18N\n" +
                "        imageLabel.setName(\"imageLabel\"); // NOI18N\n" +
                "\n" +
                "        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());\n" +
                "        getContentPane().setLayout(layout);\n" +
                "        layout.setHorizontalGroup(\n" +
                "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "            .addGroup(layout.createSequentialGroup()\n" +
                "                .addComponent(imageLabel)\n" +
                "                .addGap(18, 18, 18)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)\n" +
                "                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()\n" +
                "                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "                            .addComponent(versionLabel)\n" +
                "                            .addComponent(vendorLabel)\n" +
                "                            .addComponent(homepageLabel))\n" +
                "                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "                            .addComponent(appVersionLabel)\n" +
                "                            .addComponent(appVendorLabel)\n" +
                "                            .addComponent(appHomepageLabel)))\n" +
                "                    .addComponent(appTitleLabel, javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "                    .addComponent(appDescLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)\n" +
                "                    .addComponent(closeButton))\n" +
                "                .addContainerGap())\n" +
                "        );\n" +
                "        layout.setVerticalGroup(\n" +
                "            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)\n" +
                "            .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)\n" +
                "            .addGroup(layout.createSequentialGroup()\n" +
                "                .addContainerGap()\n" +
                "                .addComponent(appTitleLabel)\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addComponent(appDescLabel)\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)\n" +
                "                    .addComponent(versionLabel)\n" +
                "                    .addComponent(appVersionLabel))\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)\n" +
                "                    .addComponent(vendorLabel)\n" +
                "                    .addComponent(appVendorLabel))\n" +
                "                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)\n" +
                "                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)\n" +
                "                    .addComponent(homepageLabel)\n" +
                "                    .addComponent(appHomepageLabel))\n" +
                "                .addGap(19, 19, Short.MAX_VALUE)\n" +
                "                .addComponent(closeButton)\n" +
                "                .addContainerGap())\n" +
                "        );\n" +
                "\n" +
                "        pack();\n" +
                "    }// </editor-fold>//GEN-END:initComponents\n" +
                "    \n" +
                "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
                "    private javax.swing.JButton closeButton;\n" +
                "    // End of variables declaration//GEN-END:variables\n" +
                "    \n" +
                "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                workingCopy.rewrite(cut.getPackageName(), make.Identifier("crystalball"));
                MethodTree initComponents = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(2);
                List<? extends StatementTree> stmts = initComponents.getBody().getStatements();
                VariableTree var = (VariableTree) stmts.get(11);
                MethodInvocationTree invocation = (MethodInvocationTree) var.getInitializer();
                MemberSelectTree mst = (MemberSelectTree) invocation.getArguments().get(0);
                mst = (MemberSelectTree) invocation.getMethodSelect();
                invocation = (MethodInvocationTree) mst.getExpression();
                mst = (MemberSelectTree) invocation.getMethodSelect();
                invocation = (MethodInvocationTree) mst.getExpression();
                mst = (MemberSelectTree) invocation.getArguments().get(0);
                mst = (MemberSelectTree) mst.getExpression();
                workingCopy.rewrite(mst.getExpression(), make.Identifier("crystalball"));
                
                var = (VariableTree) stmts.get(16);
                invocation = (MethodInvocationTree) var.getInitializer();
                mst = (MemberSelectTree) invocation.getArguments().get(0);
                mst = (MemberSelectTree) invocation.getMethodSelect();
                invocation = (MethodInvocationTree) mst.getExpression();
                mst = (MemberSelectTree) invocation.getMethodSelect();
                invocation = (MethodInvocationTree) mst.getExpression();
                mst = (MemberSelectTree) invocation.getArguments().get(0);
                mst = (MemberSelectTree) mst.getExpression();
                workingCopy.rewrite(mst.getExpression(), make.Identifier("crystalball"));
            }
        };
        src.runModificationTask(task).commit();
        editorCookie.saveDocument();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameTypeParameter125385() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class MyList {\n" +
            "    public <X, Y extends Class<X>> X makeObject() {\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class MyList {\n" +
            "    public <A, Y extends Class<A>> A makeObject() {\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n";
        
        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        Document doc = editorCookie.openDocument();
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override
                    public Void visitIdentifier(IdentifierTree node, Void p) {
                        if ("X".equals(node.getName().toString())) {
                            workingCopy.rewrite(node, make.setLabel(node, "A"));
                        }
                        return super.visitIdentifier(node, p);
                    }
                    @Override
                    public Void visitTypeParameter(TypeParameterTree node, Void p) {
                        if ("X".equals(node.getName().toString())) {
                            workingCopy.rewrite(node, make.setLabel(node, "A"));
                        }
                        return super.visitTypeParameter(node, p);
                    }
                }.scan(cut, null);
            }

        };
        src.runModificationTask(task).commit();
        String res = doc.getText(0, doc.getLength());
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testComplex186754() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Test() {\n" +
            "   }\n" +
            "    // <editor-fold defaultstate=\"collapsed\" desc=\"Generated Code\">//GEN-BEGIN:initComponents\n" +
            "    private void initComponents() {\n" +
            "    }// </editor-fold>//GEN-END:initComponents\n" +
            "   /**\n" +
            "    * @Action\n" +
            "    */\n" +
            "   public void method1() {\n" +
            "   }\n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private Object jButton1;\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "   /**\n" +
            "    * @Action\n" +
            "    */\n" +
            "   public void method2() {\n" +
            "   }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "   public Test() {\n" +
            "   }\n" +
            "    // <editor-fold defaultstate=\"collapsed\" desc=\"Generated Code\">//GEN-BEGIN:initComponents\n" +
            "    private void initComponents() {\n" +
            "    }// </editor-fold>//GEN-END:initComponents\n" +
            "\n" + //XXX 186759
            "   public void method1() {\n" +
            "   }\n" +
            "    // Variables declaration - do not modify//GEN-BEGIN:variables\n" +
            "    private Object jButton1;\n" +
            "    // End of variables declaration//GEN-END:variables\n" +
            "\n" + //XXX 186759
            "   public void method2() {\n" +
            "   }\n" +
            "}\n";

        DataObject dataObject = DataObject.find(FileUtil.toFileObject(testFile));
        EditorCookie editorCookie = ((GuardedDataObject) dataObject).getCookie(EditorCookie.class);
        Document doc = editorCookie.openDocument();

        //XXX: strip whitespaces on modified line on save, so that the actual output matches the "golden" output:
        Preferences prefs = MimeLookup.getLookup("text/x-java").lookup(Preferences.class);
        prefs.put(SimpleValueNames.ON_SAVE_REMOVE_TRAILING_WHITESPACE, "modified-lines"); //NOI18N

        JavaSource src = getJavaSource(testFile);

        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                final TreeMaker make = workingCopy.getTreeMaker();
                final TreeUtilities tu = workingCopy.getTreeUtilities();
                new ErrorAwareTreeScanner<Void, Void>() {
                  @Override
                  public Void visitMethod(MethodTree mt, Void p) {
                     Tree nt = make.setLabel(mt, mt.getName());
                     final List<Comment> comments = tu.getComments(nt, true);
                     int size = comments.size();

                     if (size == 0) {
                        return super.visitMethod(mt, p);
                     }

                     for (int i = size - 1; i >= 0; i--) {
                        if (comments.get(i).isDocComment()) {
                           make.removeComment(nt, i, true);
                        }
                     }

                     workingCopy.rewrite(mt, nt);

                     return super.visitMethod(mt, p);
                  }
                }.scan(cut, null);
            }

        };
        src.runModificationTask(task).commit();
        LifecycleManager.getDefault().saveAll();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    @Override
    String getGoldenPckg() {
        return "";
    }

    @Override
    String getSourcePckg() {
        return "";
    }

    ////////////////////////////////////////////////////////////////////////////
    // classes used for getting guarded section initialized
    public static class GuardedDataLoader extends MultiFileLoader {

        private final String JAVA_EXTENSION = "java";

        public GuardedDataLoader() {
            super("org.netbeans.api.java.source.gen.GuardedBlockTest$GuardedDataObject"); // NOI18N
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.getExt().equals(JAVA_EXTENSION)) {
                return fo;
            }
            return null;
        }

        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            if (primaryFile.getExt().equals(JAVA_EXTENSION)) {
                return new GuardedDataObject(primaryFile, this);
            }
            return null;
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }

        @Override
        protected Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return null;
        }
    } // GuardedDataLoader


    public static class GuardedDataObject extends MultiDataObject {

        private MyGuardedEditorSupport fes = null;

        public GuardedDataObject(FileObject primaryFile, GuardedDataLoader loader) throws DataObjectExistsException {
            super(primaryFile, loader);
            getCookieSet().add(createMyGuardedEditorSupport());
            getCookieSet().assign(SaveAsCapable.class, new SaveAsCapable() {

                        public void saveAs(FileObject folder, String fileName) throws IOException {
                            createMyGuardedEditorSupport().saveAs(folder, fileName);
                        }
                    });
        }

        private synchronized MyGuardedEditorSupport createMyGuardedEditorSupport() {
            if (fes == null) {
                fes = new MyGuardedEditorSupport(this);
            }
            return fes;
        }

        private static class MyGuardedEditorSupport extends DataEditorSupport
                implements OpenCookie, EditCookie, EditorCookie, PrintCookie, EditorCookie.Observable {

            private static final class Environment extends DataEditorSupport.Env {

                private static final long serialVersionUID = -1;
                private transient SaveSupport saveCookie = null;

                private final class SaveSupport implements SaveCookie {

                    public void save() throws IOException {
                        ((MyGuardedEditorSupport) findCloneableOpenSupport()).saveDocument();
                        getDataObject().setModified(false);
                    }
                }

                public Environment(GuardedDataObject obj) {
                    super(obj);
                }

                protected FileObject getFile() {
                    return this.getDataObject().getPrimaryFile();
                }

                protected FileLock takeLock() throws java.io.IOException {
                    return ((MultiDataObject) this.getDataObject()).getPrimaryEntry().takeLock();
                }

                public @Override
                CloneableOpenSupport findCloneableOpenSupport() {
                    return (CloneableEditorSupport) ((GuardedDataObject) this.getDataObject()).getCookie(EditorCookie.class);
                }

                public void addSaveCookie() {
                    GuardedDataObject javaData = (GuardedDataObject) this.getDataObject();
                    if (javaData.getCookie(SaveCookie.class) == null) {
                        if (this.saveCookie == null) {
                            this.saveCookie = new SaveSupport();
                        }
                        javaData.getCookieSet().add(this.saveCookie);
                        javaData.setModified(true);
                    }
                }

                public void removeSaveCookie() {
                    GuardedDataObject javaData = (GuardedDataObject) this.getDataObject();
                    if (javaData.getCookie(SaveCookie.class) != null) {
                        javaData.getCookieSet().remove(this.saveCookie);
                        javaData.setModified(false);
                    }
                }
            }

            public MyGuardedEditorSupport(GuardedDataObject dataObject) {
                super(dataObject, new Environment(dataObject));
                setMIMEType("text/x-java"); // NOI18N
            }

            @Override
            protected boolean notifyModified() {
                if (!super.notifyModified()) {
                    return false;
                }
                ((Environment) this.env).addSaveCookie();
                return true;
            }

            @Override
            protected void notifyUnmodified() {
                super.notifyUnmodified();
                ((Environment) this.env).removeSaveCookie();
            }

            @Override 
            protected CloneableEditor createCloneableEditor() {
                return new CloneableEditor(this);
            }

            @Override
            public boolean close(boolean ask) {
                return super.close(ask);
            }

            private final class FormGEditor implements GuardedEditorSupport {

                StyledDocument doc = null;

                public StyledDocument getDocument() {
                    return FormGEditor.this.doc;
                }
            }
            private FormGEditor guardedEditor;
            private GuardedSectionsProvider guardedProvider;

            @Override
            protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
                if (guardedEditor == null) {
                    guardedEditor = new FormGEditor();
                    GuardedSectionsFactory gFactory = GuardedSectionsFactory.find("text/x-java");
                    if (gFactory != null) {
                        guardedProvider = gFactory.create(guardedEditor);
                    }
                }

                if (guardedProvider != null) {
                    guardedEditor.doc = doc;
                    Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
                    Reader reader = guardedProvider.createGuardedReader(stream, c);
                    try {
                        kit.read(reader, doc, 0);
                    } finally {
                        reader.close();
                    }
                } else {
                    super.loadFromStreamToKit(doc, stream, kit);
                }
            }

            @Override
            protected void saveFromKitToStream(StyledDocument doc, EditorKit kit, OutputStream stream) throws IOException, BadLocationException {
                if (guardedProvider != null) {
                    Charset c = FileEncodingQuery.getEncoding(this.getDataObject().getPrimaryFile());
                    Writer writer = guardedProvider.createGuardedWriter(stream, c);
                    try {
                        kit.write(writer, doc, 0, doc.getLength());
                    } finally {
                        writer.close();
                    }
                } else {
                    super.saveFromKitToStream(doc, kit, stream);
                }
            }
        }
    } // GuardedDataObject

}

