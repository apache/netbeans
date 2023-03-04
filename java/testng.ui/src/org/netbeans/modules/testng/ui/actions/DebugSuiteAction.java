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
import org.netbeans.modules.testng.api.TestNGSupport;
import org.netbeans.modules.testng.api.TestNGUtils;
import org.netbeans.modules.testng.spi.TestConfig;
import org.netbeans.modules.testng.spi.TestNGSupportImplementation;
import org.netbeans.spi.project.SingleMethod;
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
 * @author lukas
 */
@ActionID(id = "org.netbeans.modules.testng.actions.DebugSuiteAction", category="Build")
@ActionRegistration(displayName = "#CTL_DebugSuiteAction")
@ActionReferences(value = {
    @ActionReference(path = "Loaders/text/x-testng+xml/Actions", position = 270)})
@NbBundle.Messages("CTL_DebugSuiteAction=&Debug Test File")
public class DebugSuiteAction extends NodeAction {

    private static final Logger LOGGER = Logger.getLogger(DebugSuiteAction.class.getName());

    public DebugSuiteAction() {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Lookup l = activatedNodes[0].getLookup();
        FileObject fileObject = l.lookup(FileObject.class);
        if (fileObject != null) {
            Project p = FileOwnerQuery.getOwner(fileObject);
            return TestNGSupport.isActionSupported(TestNGSupport.Action.DEBUG_TESTSUITE, p);
        }
        SingleMethod sm = l.lookup(SingleMethod.class);
        if (sm != null) {
            Project p = FileOwnerQuery.getOwner(sm.getFile());
            return TestNGSupport.isActionSupported(TestNGSupport.Action.DEBUG_TESTSUITE, p);
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Lookup l = activatedNodes[0].getLookup();
        FileObject fo = l.lookup(FileObject.class);
        assert fo != null;
        Project p = FileOwnerQuery.getOwner(fo);
        TestNGSupportImplementation.TestExecutor exec = TestNGSupport.findTestNGSupport(p).createExecutor(p);
        TestConfig conf = TestNGUtils.getTestConfig(fo, false, null, null, null);
        try {
            exec.execute(TestNGSupport.Action.DEBUG_TESTSUITE, conf);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getName() {
        return Bundle.CTL_DebugSuiteAction();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
