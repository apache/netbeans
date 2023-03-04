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
import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.PasteActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;

/** Test DocumentsDialogOperator.
 *
 * @author Jiri Skrivanek
 */
public class DocumentsDialogOperatorTest extends JellyTestCase {

    private static DocumentsDialogOperator documentsOper;
    private static Node editableSourceNode;
    public static String[] tests = new String[]{
        "testInvoke",
        "testVerify",
        "testSelectDocument",
        "testSelectDocuments",
        "testGetDescription",
        "testSaveDocuments",
        "testCloseDocuments",
        "testSwitchToDocument"
    };

    public DocumentsDialogOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(DocumentsDialogOperatorTest.class, ".*", ".*", tests);
    }

    @Override
    public void setUp() throws IOException {
        System.out.println("########  " + getName() + "  #######");
        openDataProjects("SampleProject");
    }

    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        EditorOperator.closeDiscardAll();
        Node sourcePackagesNode = new Node(new ProjectsTabOperator().getProjectRootNode("SampleProject"), "Source Packages");
        Node sample1 = new Node(sourcePackagesNode, "sample1");  // NOI18N
        Node sample2 = new Node(sourcePackagesNode, "sample1.sample2");  // NOI18N
        Node node = new Node(sample1, "SampleClass1");// NOI18N
        new OpenAction().performAPI(node);
        node = new Node(sample2, "SampleClass2");// NOI18N
        new OpenAction().performAPI(node);
        // copy node to be able to write in
        new CopyAction().performAPI(node);
        new PasteActionNoBlock().performAPI(sample2);
        String copyClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_CopyClass");
        NbDialogOperator copyClassOper = new NbDialogOperator(copyClassTitle);
        // "Refactor"
        String refactorLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish");
        new JButtonOperator(copyClassOper, refactorLabel).push();
        copyClassOper.getTimeouts().setTimeout("Waiter.WaitingTime", 30000);
        copyClassOper.waitClosed();
        editableSourceNode = new Node(sample2, "SampleClass21");// NOI18N
        new OpenAction().performAPI(editableSourceNode);
        documentsOper = DocumentsDialogOperator.invoke();
    }

    /**
     * Test of verify method.
     */
    public void testVerify() {
        documentsOper.verify();
    }

    /**
     * Test of selectDocument method.
     */
    public void testSelectDocument() {
        documentsOper.selectDocument("SampleClass1.java"); // NOI18N
        assertEquals("Wrong document selected.", "SampleClass1.java",
                documentsOper.lstDocuments().getSelectedValue().toString());  // NOI18N
        documentsOper.selectDocument(2);
        assertEquals("Wrong document selected.", 2, documentsOper.lstDocuments().getSelectedIndex());  // NOI18N
    }

    /**
     * Test of selectDocuments method.
     */
    public void testSelectDocuments() {
        String[] documents = {"SampleClass1.java", "SampleClass2.java"}; // NOI18N
        documentsOper.selectDocuments(documents);
        Object[] selected = documentsOper.lstDocuments().getSelectedValues();
        for (int i = 0; i < selected.length; i++) {
            assertEquals("Wrong document selected by names.", documents[i], selected[i].toString());
        }
        // test one document
        documentsOper.selectDocuments(new String[]{"SampleClass21.java"}); // NOI18N
        assertEquals("Wrong document selected.", "SampleClass21.java",
                documentsOper.lstDocuments().getSelectedValue().toString());  // NOI18N

        int[] indexes = {0, 1};
        documentsOper.selectDocuments(indexes);
        assertTrue("Wrong documents selected by indexes.",
                Arrays.equals(indexes, documentsOper.lstDocuments().getSelectedIndices()));  // NOI18N
        // test one document
        documentsOper.selectDocuments(new int[]{2});
        assertEquals("Wrong document selected.", 2, documentsOper.lstDocuments().getSelectedIndex());  // NOI18N
    }

    /**
     * Test of getDescription method.
     */
    public void testGetDescription() {
        documentsOper.selectDocument("SampleClass1.java"); // NOI18N
        assertTrue("Wrong description obtain.", documentsOper.getDescription().indexOf("SampleClass1.java") > -1); // NOI18N
    }

    /**
     * Test of saveDocuments method.
     */
    public void testSaveDocuments() {
        EditorOperator eo = new EditorOperator("SampleClass21.java"); // NOI18N
        eo.insert("//dummy\n", 1, 1); // NOI18N
        documentsOper.selectDocument("SampleClass21.java");  // NOI18N
        documentsOper.saveDocuments();
        boolean modified = eo.isModified();
        eo.closeDiscard();
        assertFalse("Document is not saved.", modified);//NOI18N
    }

    /**
     * Test of closeDocuments method.
     */
    public void testCloseDocuments() {
        documentsOper.selectDocument("SampleClass2.java");  // NOI18N
        documentsOper.closeDocuments();
        assertTrue("Document was not closed.", documentsOper.lstDocuments().getModel().getSize() == 1);
    }

    /**
     * Test of switchToDocument method.
     */
    public void testSwitchToDocument() {
        documentsOper.selectDocument("SampleClass1.java"); //NOI18N
        documentsOper.switchToDocument();
        // clean up - delete editable source
        new DeleteAction().performAPI(editableSourceNode);
        DeleteAction.confirmDeletion();
    }
}
