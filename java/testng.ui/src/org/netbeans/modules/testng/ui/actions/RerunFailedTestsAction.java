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
package org.netbeans.modules.testng.ui.actions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.api.TestNGSupport.Action;
import org.netbeans.modules.testng.api.TestNGUtils;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation.TestExecutor;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

@NbBundle.Messages("CTL_RerunFailedTestsAction=Re-run Failed Tests")
public final class RerunFailedTestsAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(RerunFailedTestsAction.class.getName());

    public RerunFailedTestsAction() {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Lookup l = activatedNodes[0].getLookup();
        Project p = l.lookup(Project.class);
        if (p == null) {
            FileObject fileObject = l.lookup(FileObject.class);
            if (fileObject != null) {
                p = FileOwnerQuery.getOwner(fileObject);
            } else {
                return false;
            }
        }
        if (TestNGSupport.isActionSupported(Action.RUN_FAILED, p)) {
            return TestNGSupport.findTestNGSupport(p).createExecutor(p).hasFailedTests();
        }
        return false;
    }

    protected void performAction(Node[] activatedNodes) {
        Lookup l = activatedNodes[0].getLookup();
        Project p = l.lookup(Project.class);
        if (p == null) {
            FileObject fileObject = l.lookup(FileObject.class);
            if (fileObject != null) {
                p = FileOwnerQuery.getOwner(fileObject);
            }
        }
        TestExecutor exec = TestNGSupport.findTestNGSupport(p).createExecutor(p);
        assert exec.hasFailedTests();
        TestConfig conf = TestNGUtils.getTestConfig(p.getProjectDirectory(), true, null, null, null);
        try {
            exec.execute(Action.RUN_FAILED, conf);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public String getName() {
        return Bundle.CTL_RerunFailedTestsAction();
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

