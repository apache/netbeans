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
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodDebuggerProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.TaskListener;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Theofanis Oikonomou
 */
@ActionID(id = "org.netbeans.modules.gsf.testrunner.TestMethodDebuggerAction", category = "CommonTestRunner")
@ActionRegistration(lazy = false, displayName = "#LBL_Action_DebugTestMethod")
@ActionReferences(value = {
    @ActionReference(path = "Editors/text/x-java/Popup", position = 1797)})
@NbBundle.Messages({"LBL_Action_DebugTestMethod=Debug Focused Test Method"})
public class TestMethodDebuggerAction extends NodeAction {

    private RequestProcessor.Task debugMethodTask;
    private TestMethodDebuggerProvider debugMethodProvider;

    /**
     * Creates a new instance of TestMethodDebuggerAction
     */
    public TestMethodDebuggerAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(TestMethodDebuggerAction.class, "LBL_Action_DebugTestMethod");
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
    protected void performAction(final Node[] activatedNodes) {
        final Collection<? extends TestMethodDebuggerProvider> providers = Lookup.getDefault().lookupAll(TestMethodDebuggerProvider.class);
        RequestProcessor RP = new RequestProcessor("TestMethodDebuggerAction", 1, true);   // NOI18N
        debugMethodTask = RP.create(new Runnable() {
            @Override
            public void run() {
                for (TestMethodDebuggerProvider provider : providers) {
                    if (provider.canHandle(activatedNodes[0])) {
                        debugMethodProvider = provider;
                        break;
                    }
                }
            }
        });
        final ProgressHandle ph = ProgressHandle.createHandle(Bundle.Search_For_Provider(), debugMethodTask);
        debugMethodTask.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(org.openide.util.Task task) {
                ph.finish();
                if (debugMethodProvider == null) {
                    StatusDisplayer.getDefault().setStatusText(Bundle.No_Provider_Found());
                } else {
                    debugMethodProvider.debugTestMethod(activatedNodes[0]);
                }
            }
        });
        ph.start();
        debugMethodTask.schedule(0);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        if (debugMethodTask != null && !debugMethodTask.isFinished()) {
            return false;
        }
        Collection<? extends TestMethodDebuggerProvider> providers = Lookup.getDefault().lookupAll(TestMethodDebuggerProvider.class);
        for (TestMethodDebuggerProvider provider : providers) {
            if (provider.isTestClass(activatedNodes[0])) {
                return true;
            }
        }
        return false;
    }

}
