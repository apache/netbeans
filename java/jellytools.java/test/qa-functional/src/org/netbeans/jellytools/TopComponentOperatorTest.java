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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.actions.AttachWindowAction;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.PasteActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JButtonOperator;

/** Test TopComponentOperator.
 *
 * @author Jiri Skrivanek
 */
public class TopComponentOperatorTest extends JellyTestCase {

    static final String[] tests = new String[]{
        "testConstructors",
        "testMakeComponentVisible",
        "testAttachTo",
        "testMaximize",
        "testRestore",
        "testCloneDocument",
        "testPushMenuOnTab",
        "testSave",
        "testCloseDiscard",
        "testCloseWindow",
        "testClose",
        "testCloseAllDocuments"
    };

    public TopComponentOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        return createModuleTest(TopComponentOperatorTest.class, tests);
    }

    /** Print out test name. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }
    private static Node editableSourceNode;
    private static TopComponentOperator tco1;
    private static TopComponentOperator tco2;

    /** Opens two sources to test. */
    private static void initSources() {
        EditorOperator.closeDiscardAll();
        Node sample1 = new Node(new SourcePackagesNode("SampleProject"), "sample1");  // NOI18N
        Node sample2 = new Node(new SourcePackagesNode("SampleProject"), "sample1.sample2");  // NOI18N
        Node node = new Node(sample1, "SampleClass1.java");// NOI18N
        new OpenAction().performAPI(node);
        node = new Node(sample2, "SampleClass2.java");// NOI18N
        new OpenAction().performAPI(node);
        if (editableSourceNode == null) {
            // copy node to be able to write in
            new CopyAction().performAPI(node);
            new PasteActionNoBlock().performAPI(sample2);
            String copyClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_CopyClass");
            NbDialogOperator copyClassOper = new NbDialogOperator(copyClassTitle);
            // "Refactor"
            String refactorLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish");
            new JButtonOperator(copyClassOper, refactorLabel).push();
            copyClassOper.waitClosed();
            editableSourceNode = new Node(sample2, "SampleClass21.java");// NOI18N
        }
        // "Navigator"
        String navigatorLabel = Bundle.getString("org.netbeans.modules.navigator.Bundle", "LBL_Navigator");
        if (TopComponentOperator.findTopComponent(navigatorLabel, 0) != null) {
            // close navigator because it can be mixed with editor
            new TopComponentOperator(navigatorLabel).close();
        }
        tco1 = new TopComponentOperator("SampleClass1.java");  //NOI18N
        tco2 = new TopComponentOperator("SampleClass2.java");  //NOI18N
    }

    /** Test constructors. */
    public void testConstructors() {
        initSources();
        // test constructors
        TopComponentOperator tco = new TopComponentOperator("SampleClass1.java");  //NOI18N
        assertEquals("Constructor new TopComponentOperator(String) failed.", "SampleClass1.java", tco.getName());  //NOI18N
        tco = new TopComponentOperator("SampleClass", 0);  //NOI18N
        TopComponentOperator tcoIndex1 = new TopComponentOperator("SampleClass", 1);  //NOI18N
        assertFalse("Constructor new TopComponentOperator(String, int) failed.", tco.getName().equals(tcoIndex1.getName()));  //NOI18N
        tco = new TopComponentOperator(MainWindowOperator.getDefault(), "SampleClass1.java");  //NOI18N
        assertEquals("Constructor new TopComponentOperator(ContainerOperator, String) failed.", "SampleClass1.java", tco.getName());  //NOI18N
        tco = new TopComponentOperator(MainWindowOperator.getDefault(), "SampleClass", 0);  //NOI18N
        tcoIndex1 = new TopComponentOperator(MainWindowOperator.getDefault(), "SampleClass", 1);  //NOI18N
        assertFalse("Constructor new TopComponentOperator(ContainerOperator, String, int) failed.", tco.getName().equals(tcoIndex1.getName()));  //NOI18N
        new TopComponentOperator(MainWindowOperator.getDefault());  //NOI18N
        new TopComponentOperator(MainWindowOperator.getDefault(), 1);  //NOI18N
    }

    /**
     * Test of makeComponentVisible method.
     */
    public void testMakeComponentVisible() {
        tco1.makeComponentVisible();
        assertTrue("makeComponentVisible doesn't work.", tco1.isShowing());
    }

    /**
     * Test of attachTo method.
     */
    public void testAttachTo() {
        tco1.attachTo("SampleClass2.java", AttachWindowAction.RIGHT);
        tco1.attachTo(tco2, AttachWindowAction.AS_LAST_TAB);
    }

    /**
     * Test of maximize method.
     */
    public void testMaximize() {
        tco1.maximize();
    }

    /**
     * Test of restore method.
     */
    public void testRestore() {
        tco1.restore();
    }

    /**
     * Test of cloneDocument method.
     */
    public void testCloneDocument() {
        tco1.cloneDocument();
        // try to find and close cloned document
        new TopComponentOperator("SampleClass1.java", 1).close();
    }

    /**
     * Test of pushMenuOnTab method.
     */
    public void testPushMenuOnTab() {
        // need to find again tco1 because clone test can close it instead of cloned version
        tco1 = new TopComponentOperator("SampleClass1.java");  //NOI18N
        /** "Clone Document" popup menu item. */
        String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                "LBL_CloneDocumentAction");
        tco1.pushMenuOnTab(popupPath);
        // try to find and close cloned document
        new TopComponentOperator("SampleClass1.java", 1).close();
        // need to find again tco1 because clone test can close it instead of cloned version
        tco1 = new TopComponentOperator("SampleClass1.java");  //NOI18N
    }

    /**
     * Test of save method.
     */
    public void testSave() {
        EditorOperator eo = new EditorOperator(editableSourceNode.getText());
        eo.insert("//dummy\n", 1, 1); // NOI18N
        eo.save();
        boolean modified = eo.isModified();
        assertFalse("Document is not saved.", modified);//NOI18N
    }

    /**
     * Test of closeDiscard method.
     */
    public void testCloseDiscard() {
        EditorOperator eo = new EditorOperator(editableSourceNode.getText());
        eo.insert("//NOT THERE\n", 1, 1); // NOI18N
        eo.closeDiscard();
        new OpenAction().performAPI(editableSourceNode);
        eo = new EditorOperator(editableSourceNode.getText());
        boolean saved = eo.contains("NOT THERE");
        eo.closeDiscard();
        // clean up - delete editable source
        new DeleteAction().perform(editableSourceNode);
        DeleteAction.confirmDeletion();
        assertFalse("Document is not discarded.", saved);//NOI18N
    }

    /**
     * Test of closeWindow method.
     */
    public void testCloseWindow() {
        tco1.closeWindow();
        assertFalse("closeWindow doesn't work", tco1.isShowing());
    }

    /**
     * Test of close method.
     */
    public void testClose() {
        tco2.close();
        assertFalse("close doesn't work", tco2.isShowing());
    }

    /**
     * Test of closeAllDocuments method.
     */
    public void testCloseAllDocuments() {
        initSources();
        tco1.closeAllDocuments();
        assertFalse("closeAllDocuments doesn't work", tco1.isShowing());
    }
}
