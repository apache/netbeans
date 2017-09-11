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

package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.ui.api.TestCreatorPanelDisplayer;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.gsf.testrunner.api.TestCreatorProvider;
import org.netbeans.modules.gsf.testrunner.ui.api.UICommonUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * 
 * @author  theofanis
 */
@ActionID(id = "org.netbeans.modules.gsf.testrunner.ui.TestCreatorAction", category = "UnitTests")
@ActionRegistration(displayName = "#LBL_CreateCommonTestAction")
@ActionReferences(value = {
    @ActionReference(path = "UI/ToolActions")})
@NbBundle.Messages({"LBL_CreateCommonTestAction=Create/Update Tests"})
public class TestCreatorAction extends NodeAction {
    
    private static final Logger LOGGER = Logger.getLogger(TestCreatorAction.class.getName());
    
    /** Creates a new instance of TestCreatorAction */
    public TestCreatorAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(TestCreatorAction.class, "LBL_CreateCommonTestAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean asynchronous() {
        return false;
    }
    
    @NbBundle.Messages({"MSG_no_FOs_from_Nodes=No File Object found for the selected Nodes."})
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        if (activatedNodes.length == 1) {
            FileObject fo = UICommonUtils.getFileObjectFromNode(activatedNodes[0]);
            if(fo == null) {
                return false;
            }
            Project p = FileOwnerQuery.getOwner(fo);
            if(p == null) {
                return false;
            }
            // do not display the action when user selects a node under project's node
            // that is identical to project's root (e.g. web.clientproject's Sources node)
            if(p.getProjectDirectory().equals(fo) && !ProjectUtils.getInformation(p).getDisplayName().equals(activatedNodes[0].getDisplayName())) {
                return false;
            }
        }
        FileObject[] activatedFOs = UICommonUtils.getFileObjectsFromNodes(activatedNodes);
        if(activatedFOs == null) {
            LOGGER.log(Level.FINE, "{0}", Bundle.MSG_no_FOs_from_Nodes());
            return false;
        }
        Collection<? extends Lookup.Item<TestCreatorProvider>> providers = Lookup.getDefault().lookupResult(TestCreatorProvider.class).allItems();
        boolean enable;
        for (Lookup.Item<TestCreatorProvider> provider : providers) {
            enable = provider.getInstance().enable(activatedFOs);
            if(enable) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
	TestCreatorPanelDisplayer.getDefault().displayPanel(UICommonUtils.getFileObjectsFromNodes(activatedNodes), null, null);
    }
    
}
