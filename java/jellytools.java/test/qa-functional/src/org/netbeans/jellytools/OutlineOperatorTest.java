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

import junit.framework.Test;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.nodes.JavaNode;
import org.netbeans.jellytools.nodes.OutlineNode;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.openide.nodes.Node.Property;

/**
 *
 * @author Vojtech Sigler
 */
public class OutlineOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[]{"testNodes"};

    public OutlineOperatorTest(String isName) {
        super(isName);
    }

    /**
     * Method used for explicit test suite definition
     *
     * @return created suite
     */
    public static Test suite() {
        return createModuleTest(OutlineOperatorTest.class, tests);
    }

    @Override
    public void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        JavaNode lrTestClass = new JavaNode(new SourcePackagesNode("SampleProject"), "sample1.outline|TestOutline.java");
        lrTestClass.open();
        EditorOperator eo = new EditorOperator("TestOutline.java");
        eo.setCaretPosition(67, 1);
        new ToggleBreakpointAction().perform();
        String windowMenu = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window");
        String debugMenu = Bundle.getStringTrimmed("org.netbeans.modules.debugger.resources.Bundle", "Menu/Window/Debug");
        String watchesItem = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchesAction");
        new Action(windowMenu + "|" + debugMenu + "|" + watchesItem, null).perform();
        String debug = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
        String newWatch = Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_New_Watch");
        for (int i = 0; i < 3; i++) {
            new ActionNoBlock(debug + "|" + newWatch, null).performMenu();
            NbDialogOperator dia = new NbDialogOperator(Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "CTL_WatchDialog_Title"));
            JEditorPaneOperator txtWatch = new JEditorPaneOperator(dia);
            txtWatch.typeText("test");
            dia.ok();
        }
        new DebugJavaFileAction().perform(lrTestClass);
        MainWindowOperator.getDefault().waitStatusText("stopped at");
    }

    @Override
    public void tearDown() {
        new FinishDebuggerAction().perform();
        new EditorOperator("TestOutline.java").close();
    }

    public void testNodes() throws Exception {
        TopComponentOperator tcoVariables = new TopComponentOperator("Variables");
        TopComponentOperator tcoBreakpoints = new TopComponentOperator("Breakpoints");
        TopComponentOperator tcoWatches = new TopComponentOperator(
                Bundle.getString("org.netbeans.modules.debugger.ui.views.Bundle", "CTL_Watches_view"));
        OutlineOperator lrOO = new OutlineOperator(tcoWatches);
        lrOO.getRootNode("test").expand();
        lrOO.getRootNode("test", 1).expand();
        OutlineNode lrNode = lrOO.getRootNode("test", 2);
        lrNode.expand();
        OutlineNode lrNewNode = new OutlineNode(lrNode, "test");
        new Action(null, Bundle.getStringTrimmed("org.netbeans.modules.debugger.jpda.ui.actions.Bundle",
                "CTL_CreateVariable")).performPopup(lrNewNode);
        OutlineNode lrFixedWatch = lrOO.getRootNode("test");

        int lnNodeRow = lrOO.getLocationForPath(lrNewNode.getTreePath()).y;
        int lnFixedRow = lrOO.getLocationForPath(lrFixedWatch.getTreePath()).y;
        Property lrNodeProperty = (Property) lrOO.getValueAt(lnNodeRow, 2);
        Property lrFixedProperty = (Property) lrOO.getValueAt(lnFixedRow, 2);
        assertTrue("Values of the original node and the fixed watch do not match!",
                lrNodeProperty.getValue().toString().equals(lrFixedProperty.getValue().toString()));
    }
}
