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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/** Test of org.netbeans.jellytools.actions.ActionNoBlock
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class ActionNoBlockTest extends JellyTestCase {

    public static String[] tests = {
        "testPerformMenu",
        "testPerformPopupOnNodes",
        "testPerformPopupOnComponent",
        "testPerformAPI",
        "testPerformShortcut"
    };
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public ActionNoBlockTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(ActionNoBlockTest.class, tests);
    }
    
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }
    
    /** simple test case
     */
    public void testPerformMenu() {
        /** File|New Project..." main menu path. */
        String menuPath = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/File")
                          + "|"
                          + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewProjectAction_Name");

        new ActionNoBlock(menuPath, null).perform();
        new NewProjectWizardOperator().close();
    }
    
    /** simple test case
     */
    public void testPerformPopupOnNodes() {
        SourcePackagesNode sourceNode = new SourcePackagesNode("SampleProject");
        Node nodes[] = {
            new Node(sourceNode, "sample1|SampleClass1.java"),  // NOI18N
            new Node(sourceNode, "sample1.sample2|SampleClass2.java")   // NOI18N
        };
        String deletePopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
        new ActionNoBlock(null, deletePopup).perform(nodes);
        new NbDialogOperator("Confirm Multiple Object Deletion").no();
        String openPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Open");
        new ActionNoBlock(null, openPopup).perform(nodes[0]);
    }
    
    /** simple test case
     */
    public void testPerformPopupOnComponent() {
        EditorOperator eo = new EditorOperator("SampleClass");// NOI18N
        // "New Watch..."
        String newWatchItem = Bundle.getString("org.netbeans.editor.Bundle", "add-watch");
        new ActionNoBlock(null, newWatchItem).perform(eo);
        // "New Watch"
        String newWatchTitle = Bundle.getString("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title");
        new NbDialogOperator(newWatchTitle).close();
        eo.closeDiscard();
    }
    
    /** simple test case
     */
    public void testPerformAPI() {
        new NewFileAction().performAPI();
        new NewFileWizardOperator().close();
    }
    
    /** simple test case
     */
    public void testPerformShortcut() {
        new ActionNoBlock(null, null, null, new Action.Shortcut(KeyEvent.VK_N, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK)).perform();
        new NewProjectWizardOperator().close();
        // On some linux it may happen autorepeat is activated and it 
        // opens dialog multiple times. So, we need to close all modal dialogs.
        // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
        closeAllModal();
    }
    
}
