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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeActionProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.spi.project.ActionProvider;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class RunTestAction extends NodeAction {

    @Override
    public String getName() {
        return getString("TestActionName"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return;
        }
        Node n = activatedNodes[0];
        Project project = (Project) n.getValue("Project"); // NOI18N
        assert project != null;
//        Folder folder = (Folder) n.getValue("Folder"); // NOI18N
//        assert folder != null;
//        Node thisNode = (Node) n.getValue("This"); // NOI18N
//        assert thisNode != null;
//
//        List<Folder> list = folder.getAllTests();
//        if (folder.isTest()) {
//            list.add(folder);
//        }
//        if (list.size() > 0) {
//            StringBuffer message = new StringBuffer("Would run the following test(s):\n\n"); // NOI18N
//            for (Folder f : list) {
//                message.append("  ").append(f.getDisplayName()).append("\n"); // NOI18N
//            }
//            message.append("\nTest(s) would build and run with output directed to output window. Two posibilities (will have to decided):\n"); // NOI18N
//            message.append("1): output is parsed similary to how build output is parsed and failed tests are hyperlinked for easy navigation back to the failed test.\n"); // NOI18N
//            message.append("2): full-featured GUI frontend (similar to JUnit GUI frontend) with support for test progress, summary, and hyperlinks back to failed tests.\n"); // NOI18N
//            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE);
//            DialogDisplayer.getDefault().notify(nd);
//        }

        ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
        if (ap != null) {
            InstanceContent ic = new InstanceContent();
            ic.add(project);
            Folder targetFolder = (Folder) n.getValue("Folder"); // NOI18N
            if (targetFolder != null) {
                ic.add(targetFolder);
            }
            DataObject d = n.getLookup().lookup(DataObject.class);
            if (d != null) {
                ic.add(d.getPrimaryFile());
            }
            ap.invokeAction(ActionProvider.COMMAND_TEST_SINGLE, new AbstractLookup(ic));
        }
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        Node n = activatedNodes[0];
        Folder folder = (Folder) n.getValue("Folder"); // NOI18N
        if (folder == null) {
            return false;
        }
        if (folder.isTest() || folder.isTestRootFolder()) {
            return true;
        }
        List<Folder> list = folder.getAllTests();
        return list.size() > 0;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private String getString(String s) {
        return NbBundle.getBundle(getClass()).getString(s);
    }
}
