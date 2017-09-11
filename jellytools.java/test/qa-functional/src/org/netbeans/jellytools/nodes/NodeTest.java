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
package org.netbeans.jellytools.nodes;

import java.io.IOException;
import java.util.Arrays;
import javax.swing.tree.TreePath;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.CopyAction;
import org.netbeans.jellytools.actions.PasteActionNoBlock;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jellytools.testutils.JavaNodeUtils;

/** Test of org.netbeans.jellytools.nodes.
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class NodeTest extends JellyTestCase {

    static final String[] tests = {
        "testConstructor",
        "testSelect",
        "testSelectDoubleClick",
        "testPerformAPIAction",
        "testPerformAPIActionNoBlock",
        "testPerformMenuAction",
        "testPerformMenuActionNoBlock",
        "testPerformPopupAction",
        "testPerformPopupActionNoBlock",
        "testGetPath",
        "testGetParentPath",
        "testGetChildren",
        "testIsLeaf",
        "testIsPresent",
        "testVerifyPopup",
        "testWaitNotPresent",
        "testWaitChildNotPresent",
        "testIsChildPresent",
        "testNodeRecreated"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public NodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(NodeTest.class, tests);
    }
    private static Node projectRootNode;
    private static Node sourcePackagesNode;
    private static Node sample1Node;
    private static Node sampleClass1Node;

    /** method called before each testcase */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (projectRootNode == null) {
            projectRootNode = new ProjectsTabOperator().getProjectRootNode("SampleProject");
        }
        if (sourcePackagesNode == null) {
            sourcePackagesNode = new SourcePackagesNode("SampleProject");
        }
        if (sample1Node == null) {
            sample1Node = new Node(sourcePackagesNode, "sample1"); // NOI18N
        }
        if (sampleClass1Node == null) {
            sampleClass1Node = new Node(sample1Node, "SampleClass1.java"); // NOI18N
        }
    }

    /** method called after each testcase */
    @Override
    protected void tearDown() {
    }

    /** Test constructor  */
    public void testConstructor() {
        Node n = new Node(ProjectsTabOperator.invoke().tree(), "SampleProject");

        assertTrue(n.isPresent());
        assertEquals(n.getText(), "SampleProject");

        Node sample1NodeLocal = new Node(sourcePackagesNode, "sample1"); // NOI18N
        Node node = new Node(sample1NodeLocal, "SampleClass1.java"); // NOI18N
        assertEquals(node.getText(), "SampleClass1.java");

        String children[] = sample1NodeLocal.getChildren();
        assertTrue(children.length > 0);
        for (int i = 0; i < children.length; i++) {
            assertEquals(new Node(sample1NodeLocal, i).getText(), children[i]);
        }
    }

    /** Test select */
    public void testSelect() {
        new ProjectsTabOperator().makeComponentVisible();
        sourcePackagesNode.select();
        TreePath[] tp = sourcePackagesNode.tree().getSelectionPaths();
        assertNotNull(tp);
        assertEquals(tp.length, 1);
        assertEquals(tp[0].getLastPathComponent().toString(), sourcePackagesNode.getText());

        Node n2 = new Node(sourcePackagesNode, "|");
        n2.addSelectionPath();
        tp = n2.tree().getSelectionPaths();
        assertNotNull(tp);
        assertEquals(tp.length, 2);
        assertEquals(tp[0].getLastPathComponent().toString(), sourcePackagesNode.getText());
        assertEquals(tp[1].getLastPathComponent().toString(), n2.getText());
    }

    /** Test whether select() called twice is not considered as double click. */
    public void testSelectDoubleClick() {
        boolean expanded = sourcePackagesNode.isExpanded();
        sourcePackagesNode.select();
        sourcePackagesNode.select();
        assertEquals("Not wanted double click. Wrong result of isExpanded().", expanded, sourcePackagesNode.isExpanded());  // NOI18N
    }

    /** Test performAPIAction */
    public void testPerformAPIAction() {
        sampleClass1Node.performAPIAction("org.openide.actions.PropertiesAction");  // NOI18N
        JavaNodeUtils.closeProperties("SampleClass1.java"); // NOI18N
    }

    /** Test performAPIActionNoBlock */
    public void testPerformAPIActionNoBlock() {
        Node node = new SourcePackagesNode("SampleProject"); // NOI18N
        node.performAPIActionNoBlock("org.netbeans.modules.search.FindInFilesAction");    // NOI18N
        new FindInFilesOperator().close();
    }

    /** Test performMenuAction */
    public void testPerformMenuAction() {
        // Window|IDE Tools|Properties
        String propertiesPath = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window")
                + "|IDE Tools|"
                + Bundle.getStringTrimmed("org.openide.actions.Bundle", "Properties");
        sampleClass1Node.performMenuAction(propertiesPath);
        JavaNodeUtils.closeProperties("SampleClass1.java"); // NOI18N
    }

    /** Test performMenuActionNoBlock */
    public void testPerformMenuActionNoBlock() {
        String helpItem = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Help");
        String aboutItem = Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle", "About");
        sourcePackagesNode.performMenuActionNoBlock(helpItem + "|" + aboutItem);    // NOI18N
        String aboutTitle = Bundle.getString("org.netbeans.core.startup.Bundle", "CTL_About_Title");
        new JDialogOperator(aboutTitle).requestClose();
    }

    /** Test performPopupAction  */
    public void testPerformPopupAction() {
        // "Open"
        String openItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        sampleClass1Node.performPopupAction(openItem);
        new EditorOperator("SampleClass1.java").close();    // NOI18N
    }

    /** Test performPopupActionNoBlock */
    public void testPerformPopupActionNoBlock() {
        // "Delete"
        String deleteItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
        sampleClass1Node.performPopupActionNoBlock(deleteItem);
        JavaNodeUtils.closeSafeDeleteDialog();
    }

    /** Test getPath */
    public void testGetPath() {
        JTreeOperator tree = RuntimeTabOperator.invoke().tree();
        assertEquals(new Node(tree, "").getPath(), "");
        new ProjectsTabOperator().makeComponentVisible();
        assertTrue("Path ends with sample1", sample1Node.getPath().endsWith("sample1"));// NOI18N
        assertTrue("Paths ends with sample1|SampleClass1.java", sampleClass1Node.getPath().endsWith("sample1|SampleClass1.java")); // NOI18N
    }

    /** Test getParentPath */
    public void testGetParentPath() {
        JTreeOperator tree = RuntimeTabOperator.invoke().tree();
        assertNull(new Node(tree, "").getParentPath());
        new ProjectsTabOperator().makeComponentVisible();
        assertEquals(projectRootNode.getParentPath(), "");
        assertTrue("Parent path ends with sample1", sampleClass1Node.getParentPath().endsWith("sample1")); // NOI18N
    }

    /** golden test case of getChildren  */
    public void testGetChildren() {
        String children[] = sample1Node.getChildren();
        assertNotNull(children);
        Arrays.sort(children);
        String[] expectedChildren = {
            "JFrameSample.java",
            "SampleClass1.java",
            "html.html",
            "image.gif",
            "properties.properties",
            "unrecognized",
            "url.url"
        };
        assertEquals("Wrong number of children.", expectedChildren.length, children.length);
        for (int i = 0; i < children.length; i++) {
            assertTrue("Wrong child.", expectedChildren[i].equals(children[i]));
        }
    }

    /** Test isLeaf */
    public void testIsLeaf() {
        assertFalse(sample1Node.isLeaf());
        assertTrue(sampleClass1Node.isLeaf());
    }

    /** Test isPresent */
    public void testIsPresent() {
        new CopyAction().performAPI(sampleClass1Node);
        performPaste(sample1Node);
        Node node = new Node(sample1Node, "SampleClass11"); // NOI18N        
        assertTrue(node.isPresent());
        JavaNodeUtils.performSafeDelete(node);
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        assertTrue(!node.isPresent());
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        // "Find..."
        String findItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Find");
        // "Delete"
        String deleteItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
        sample1Node.verifyPopup(new String[]{findItem, deleteItem});
    }

    /** Test of waitNotPresent() method. */
    public void testWaitNotPresent() {
        new CopyAction().performAPI(sampleClass1Node);    // NOI18N
        performPaste(sample1Node);
        final Node duplNode = new Node(sample1Node, "SampleClass11"); // NOI18N        
        JavaNodeUtils.performSafeDelete(duplNode);
        duplNode.waitNotPresent();
    }

    /** Test of waitChildNotPresent() method. */
    public void testWaitChildNotPresent() {
        new CopyAction().performAPI(sampleClass1Node);// NOI18N
        performPaste(sample1Node);
        final Node duplNode = new Node(sample1Node, "SampleClass11");// NOI18N        
        JavaNodeUtils.performSafeDelete(duplNode);
        sample1Node.waitChildNotPresent("SampleClass11");// NOI18N
    }

    /** Test of isChildPresent() method. */
    public void testIsChildPresent() {
        assertTrue("Child SampleClass1.java should be present in sample1 package.", sample1Node.isChildPresent("SampleClass1.java"));    // NOI18N
    }

    /** Test functionality when node is re-created. */
    public void testNodeRecreated() throws Exception {
        new CopyAction().performAPI(sampleClass1Node);
        performPaste(sample1Node);
        final Node duplNode = new Node(sample1Node, "SampleClass11"); // NOI18N
        TreePath tp1 = duplNode.getTreePath();
        JavaNodeUtils.performSafeDelete(duplNode);
        new CopyAction().performAPI(sampleClass1Node);
        performPaste(sample1Node);
        try {
            assertFalse("Original TreePath should be invalid.", !tp1.getLastPathComponent().toString().isEmpty() && duplNode.tree().getRowForPath(tp1) > 0);
            assertTrue("TreePath should be valid after re-creation.", duplNode.tree().getRowForPath(duplNode.getTreePath()) > 0);
        } finally {
            JavaNodeUtils.performSafeDelete(new Node(sample1Node, "SampleClass11"));
        }
    }

    /** Perform paste action and confirm refactoring dialog. */
    private static void performPaste(Node node) {
        new PasteActionNoBlock().performAPI(node);
        String copyClassTitle = Bundle.getString("org.netbeans.modules.refactoring.java.ui.Bundle", "LBL_CopyClass");
        NbDialogOperator copyClassOper = new NbDialogOperator(copyClassTitle);
        // "Refactor"
        String refactorLabel = Bundle.getStringTrimmed("org.netbeans.modules.refactoring.spi.impl.Bundle", "CTL_Finish");
        new JButtonOperator(copyClassOper, refactorLabel).push();
        copyClassOper.waitClosed();
    }
}
