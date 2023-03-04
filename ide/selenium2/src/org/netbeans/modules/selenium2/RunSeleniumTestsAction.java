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
package org.netbeans.modules.selenium2;

import org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils;
import org.netbeans.modules.selenium2.api.Selenium2Support;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Theofanis Oikonomou
 */

@ActionID(id = "org.netbeans.modules.selenium2.RunSeleniumTestsAction", category = "Project")
@ActionRegistration(displayName = "#LBL_RunSeleniumTestsAction", lazy = false)
@ActionReferences({
    @ActionReference(path = "Projects/org-netbeans-modules-j2ee-clientproject/Actions", position = 1101),
    @ActionReference(path = "Projects/org-netbeans-modules-j2ee-ejbjarproject/Actions", position = 1101),
    @ActionReference(path = "Projects/org-netbeans-modules-java-j2seproject/Actions", position = 1101),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 1201),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 501),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 721),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 1101),
    @ActionReference(path = "Projects/package/Actions", position = 99)
})
@NbBundle.Messages({"LBL_RunSeleniumTestsAction=Run Selenium Tests"})
public class RunSeleniumTestsAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Selenium2Support.runTests(UICommonUtils.getFileObjectsFromNodes(activatedNodes), true);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return Selenium2Support.isSupportEnabled(UICommonUtils.getFileObjectsFromNodes(activatedNodes));
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return Bundle.LBL_RunSeleniumTestsAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
