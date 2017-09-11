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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
