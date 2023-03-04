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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action.Shortcut;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;

/** Test of org.netbeans.jellytools.actions.Action.
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class ActionTest extends JellyTestCase {

    public static String[] tests = new String[]{
        "testPerformMenu",
        "testPerformMenuOnNode",
        "testPerformPopup",
        "testPerformPopupOnNodes",
        "testPerformPopupOnComponent",
        "testPerformAPI",
        "testPerformAPIOnNodes",
        "testPerformShortcut",
        "testTestNodesMenu",
        "testTestNodesPopup",
        "testTestNodesAPI",
        "testTestNodesShortcut"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(ActionTest.class, tests);
    }

    /** method called before each testcase
     */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
    }

    /** method called after each testcase
     */
    @Override
    protected void tearDown() {
        setDefaultMode(Action.POPUP_MODE);
    }

    public void testPerformMenu() {
        setDefaultMode(Action.API_MODE);
        // open Window|Properties
        new Action(new PropertiesAction().getMenuPath(), null).perform();
        new PropertySheetOperator().close();
    }

    /** Test to perform main menu action on node. */
    public void testPerformMenuOnNode() {
        Node n = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java");
        // "Tools"
        String toolsItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "CTL_Tools");
        // "Add to Favorites"
        String addToFavoritesItem = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle", "ACT_Add");
        // "Tools|Add To Favorites"
        try {
            new Action(toolsItem + "|" + addToFavoritesItem, null).perform(n);
        } catch (TimeoutExpiredException e) {
            // Try it once more because sometimes for an uknown reason is Tools menu not fully populated first time.
            // See http://www.netbeans.org/issues/show_bug.cgi?id=85853.
            // push Escape key to ensure there is no open menu
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
            new Action(toolsItem + "|" + addToFavoritesItem, null).perform(n);
        }
        new FavoritesOperator().close();
    }

    /** simple test case
     */
    public void testPerformPopup() {
        try {
            new Action(null, "anything").performPopup();
            fail("UnsupportedOperationException not thrown");
        } catch (UnsupportedOperationException e) {
            // it is ok that it throws exception
        }
    }

    /** simple test case
     */
    public void testPerformPopupOnNodes() {
        setDefaultMode(Action.MENU_MODE);
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        Node nodes[] = {
            new Node(sourceNode, "sample1|SampleClass1.java"), // NOI18N
            new Node(sourceNode, "sample1.sample2|SampleClass2.java") // NOI18N
        };
        // "Open"
        String openItem = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");// NOI18N
        new Action(null, openItem).perform(nodes);
        EditorOperator eo = new EditorOperator("SampleClass");// NOI18N
    }

    /** simple test case
     */
    public void testPerformPopupOnComponent() {
        setDefaultMode(Action.API_MODE);
        EditorOperator op = new EditorOperator("SampleClass");// NOI18N
        // "Code Folds"
        String codeFoldsItem = Bundle.getStringTrimmed("org.netbeans.modules.editor.Bundle", "Menu/View/CodeFolds");
        // "Code Folds|Expand Folds"
        new Action(null, codeFoldsItem + "|Expand Fold").perform(op);
        EditorOperator.closeDiscardAll();
    }

    /** simple test case
     */
    public void testPerformAPI() {
        new OptionsViewAction().performAPI();
        new OptionsOperator().close();
    }

    /** simple test case
     */
    public void testPerformAPIOnNodes() {
        setDefaultMode(Action.POPUP_MODE);
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        Node nodes[] = {
            new Node(sourceNode, "sample1|SampleClass1.java"), // NOI18N
            new Node(sourceNode, "sample1.sample2|SampleClass2.java") // NOI18N
        };
        new Action(null, null, "org.openide.actions.PropertiesAction").perform(nodes);// NOI18N
        new PropertySheetOperator().close();
    }

    /** simple test case
     */
    public void testPerformShortcut() {
        setDefaultMode(Action.MENU_MODE);
        // open global properties CTRL+Shift+7
        new Action(null, null, null, new Shortcut(KeyEvent.VK_7, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)).perform();
        new PropertySheetOperator().close();
    }
    private static Node[] nodesField;

    private static Node[] getNodes() {
        if (nodesField == null) {
            nodesField = new Node[]{
                FilesTabOperator.invoke().getProjectNode("SampleProject"),
                ProjectsTabOperator.invoke().getProjectRootNode("SampleProject"),
                RuntimeTabOperator.invoke().getRootNode()
            };
        }
        return nodesField;
    }

    /** simple test case */
    public void testTestNodesMenu() {
        Node[] nodes = getNodes();
        try {
            new Action("", "").performMenu(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes[1] = null;
        try {
            new Action("", "").performMenu(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes = null;
        try {
            new Action("", "").performMenu(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        try {
            new Action("", "").performMenu(new Node[0]);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
    }

    /** simple test case
     */
    public void testTestNodesPopup() {
        Node[] nodes = getNodes();
        try {
            new Action("", "").performPopup(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes[1] = null;
        try {
            new Action("", "").performPopup(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes = null;
        try {
            new Action("", "").performPopup(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        try {
            new Action("", "").performPopup(new Node[0]);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
    }

    /** simple test case
     */
    public void testTestNodesAPI() {
        Node[] nodes = getNodes();
        try {
            new Action("", "", "java.lang.Object").performAPI(nodes);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes[1] = null;
        try {
            new Action("", "", "java.lang.Object").performAPI(nodes);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes = null;
        try {
            new Action("", "", "java.lang.Object").performAPI(nodes);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        try {
            new Action("", "", "java.lang.Object").performAPI(new Node[0]);  // NOI18N
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
    }

    /** simple test case
     */
    public void testTestNodesShortcut() {
        Node[] nodes = getNodes();
        try {
            new Action("", "", new Shortcut(0)).performShortcut(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes[1] = null;
        try {
            new Action("", "", new Shortcut(0)).performShortcut(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        nodes = null;
        try {
            new Action("", "", new Shortcut(0)).performShortcut(nodes);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
        try {
            new Action("", "", new Shortcut(0)).performShortcut(new Node[0]);
            fail("IllegalArgumentException not thrown");  // NOI18N
        } catch (IllegalArgumentException e) {
        }
    }

    private void setDefaultMode(int mode) {
        JemmyProperties.setCurrentProperty("Action.DefaultMode", new Integer(mode));  // NOI18N
    }
}
