/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.gsf.testrunner.ui;

import java.util.Collection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodRunnerProvider;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Theofanis Oikonomou
 */
@ActionID(id = "org.netbeans.modules.gsf.testrunner.TestMethodRunnerAction", category = "CommonTestRunner")
@ActionRegistration(lazy = false, displayName = "#LBL_Action_RunTestMethod")
@ActionReferences(value = {
    @ActionReference(path = "Editors/text/x-java/Popup", position = 1795)})
@NbBundle.Messages({"LBL_Action_RunTestMethod=Run Focused Test Method"})
public class TestMethodRunnerAction extends NodeAction {

    private RequestProcessor.Task runMethodTask;
    private TestMethodRunnerProvider runMethodProvider;

    /**
     * Creates a new instance of TestMethodRunnerAction
     */
    public TestMethodRunnerAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(TestMethodRunnerAction.class, "LBL_Action_RunTestMethod");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean asynchronous() {
        return false;
    }

    @Override
    @NbBundle.Messages({"Search_For_Provider=Searching for provider to handle the test method",
        "No_Provider_Found=No provider can handle the test method",
        "Scanning_In_Progress=Scanning in progress, cannot yet identify the name of the test method"})
    protected void performAction(final Node[] activatedNodes) {
        final Collection<? extends TestMethodRunnerProvider> providers = Lookup.getDefault().lookupAll(TestMethodRunnerProvider.class);
        RequestProcessor RP = new RequestProcessor("TestMethodRunnerAction", 1, true);   // NOI18N
        runMethodTask = RP.create(new Runnable() {
            @Override
            public void run() {
                for (TestMethodRunnerProvider provider : providers) {
                    if (provider.canHandle(activatedNodes[0])) {
                        runMethodProvider = provider;
                        break;
                    }
                }
            }
        });
        final ProgressHandle ph = ProgressHandleFactory.createHandle(Bundle.Search_For_Provider(), runMethodTask);
        runMethodTask.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(org.openide.util.Task task) {
                ph.finish();
                if (runMethodProvider == null) {
                    boolean isIndexing = IndexingManager.getDefault().isIndexing();
                    StatusDisplayer.getDefault().setStatusText(isIndexing ? Bundle.Scanning_In_Progress() : Bundle.No_Provider_Found());
                } else {
                    runMethodProvider.runTestMethod(activatedNodes[0]);
                }
            }
        });
        ph.start();
        runMethodTask.schedule(0);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        if (runMethodTask != null && !runMethodTask.isFinished()) {
            return false;
        }
        Collection<? extends TestMethodRunnerProvider> providers = Lookup.getDefault().lookupAll(TestMethodRunnerProvider.class);
        for (TestMethodRunnerProvider provider : providers) {
            if (provider.isTestClass(activatedNodes[0])) {
                return true;
            }
        }
        return false;
    }

}
