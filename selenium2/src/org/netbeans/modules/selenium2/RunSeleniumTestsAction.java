/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
