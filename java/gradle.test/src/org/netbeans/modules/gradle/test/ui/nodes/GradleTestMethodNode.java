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

package org.netbeans.modules.gradle.test.ui.nodes;

import org.netbeans.modules.gradle.api.execute.RunUtils;
import org.netbeans.modules.gradle.java.api.output.Location;
import org.netbeans.modules.gradle.test.ui.nodes.Bundle;
import org.netbeans.modules.gradle.test.GradleTestcase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.junit.ui.api.JUnitTestMethodNode;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
import static org.netbeans.spi.project.SingleMethod.COMMAND_RUN_SINGLE_METHOD;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class GradleTestMethodNode extends JUnitTestMethodNode implements Location.Finder {

    public GradleTestMethodNode(Testcase testcase, Project project, Lookup lookup, String projectType, String testingFramework) {
        super(testcase, project, lookup, projectType, testingFramework);
    }

    public GradleTestMethodNode(Testcase testcase, Project project, String projectType, String testingFramework) {
        super(testcase, project, projectType, testingFramework);
    }

    @NbBundle.Messages({
        "LBL_RerunTest=Run Again",
        "LBL_DebugTest=Debug"
    })
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>();
        Action preferred = getPreferredAction();
        if (preferred != null) {
            actions.add(getPreferredAction());
        }
        ActionProvider actionProvider = getProject().getLookup().lookup(ActionProvider.class);
        if ((actionProvider != null) && (testcase instanceof GradleTestcase)) {
            List<String> supportedActions = Arrays.asList(actionProvider.getSupportedActions());
            boolean runSupported = supportedActions.contains(COMMAND_RUN_SINGLE_METHOD);
            boolean debugSupported = supportedActions.contains(COMMAND_DEBUG_SINGLE_METHOD);

            JvmTestOperationDescriptor op = ((GradleTestcase) testcase).getOperation();
            String tcName = op.getClassName() + '.' + op.getMethodName();
            Lookup nodeContext = Lookups.singleton(RunUtils.simpleReplaceTokenProvider("selectedMethod", tcName));

            if (runSupported) {
                actions.add(new ReRunTestAction(actionProvider, nodeContext, COMMAND_RUN_SINGLE_METHOD, Bundle.LBL_RerunTest()));
            }

            if (debugSupported) {
                actions.add(new ReRunTestAction(actionProvider, nodeContext, COMMAND_DEBUG_SINGLE_METHOD, Bundle.LBL_DebugTest()));
            }
        }
        return actions.toArray(new Action[0]);
    }

    @Override
    public FileObject findFileObject(Location loc) {
        FileObject fo = getProject().getLookup().lookup(LineConvertors.FileLocator.class).find(loc.getFileName());
        return fo;
    }

    Location getTestLocation() {
        return Location.parseLocation(getTestcase().getLocation());
    }
}
