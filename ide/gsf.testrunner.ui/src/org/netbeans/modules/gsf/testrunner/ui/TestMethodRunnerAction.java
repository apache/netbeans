/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.gsf.testrunner.ui;

import java.util.Collection;
import org.netbeans.api.progress.ProgressHandle;
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
        final ProgressHandle ph = ProgressHandle.createHandle(Bundle.Search_For_Provider(), runMethodTask);
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
