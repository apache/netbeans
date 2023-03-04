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

package org.netbeans.performance.languages;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.modules.performance.utilities.CommonUtilities;

/**
 *
 * @author mkhramov@netneans.org
 */
public class ScriptingUtilities extends CommonUtilities {
    private static  String menuItemName;
    static {
        try {
        menuItemName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "LBL_Fix_Missing_Server_Action");
        } catch (Exception ex) {}
    }
    private static  String dialogName;
    static {
        try {
        dialogName = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "LBL_Resolve_Missing_Server_Title");
        } catch (Exception ex) {}
    }
    
    public static void verifyAndResolveMissingWebServer(String projectName, String serverName) {
        ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode(projectName);
             
        if(!isServerMissingMenuAvaialable(projectName)) {
            return;
        }
        
        projectNode.performPopupActionNoBlock(menuItemName);
        
        NbDialogOperator missingServerDialog = new NbDialogOperator(dialogName);
        JListOperator serversList = new JListOperator(missingServerDialog);
        serversList.selectItem(serverName);
        missingServerDialog.ok();
        
    }
    private static boolean isServerMissingMenuAvaialable(String projectName) {
        ProjectRootNode projectNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        try {
            projectNode.verifyPopup(menuItemName);
        } catch(JemmyException jex) {
            return false;
        }
        return true;
    }
    // Usage: ScriptingUtilities.invokePTO();
    public static ProjectsTabOperator invokePTO() {
        ProjectsTabOperator testOp = null;
        try {
            testOp = new ProjectsTabOperator();
        } catch (TimeoutExpiredException tex) {
            MainWindowOperator mv = MainWindowOperator.getDefault();
            JMenuBarOperator menuBar = mv.menuBar();
            //menuBar.pushMenu("Window|Projects");
            JMenuItemOperator item = menuBar.showMenuItem("Window|Projects");
            item.clickMouse();
            testOp = new ProjectsTabOperator();
        }       
        return testOp;
    }
}
