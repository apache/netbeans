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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;
import org.netbeans.api.extexecution.print.LineConvertors;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gradle.java.api.output.Location;
import org.netbeans.modules.gradle.test.GradleTestcase;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.junit.ui.api.JUnitTestMethodNode;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.NestedClass;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

import static org.netbeans.spi.project.SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
import static org.netbeans.spi.project.SingleMethod.COMMAND_RUN_SINGLE_METHOD;

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
        if ((actionProvider != null) && testcase instanceof GradleTestcase gradleTestcase) {
            List<String> supportedActions = Arrays.asList(actionProvider.getSupportedActions());
            boolean runSupported = supportedActions.contains(COMMAND_RUN_SINGLE_METHOD);
            boolean debugSupported = supportedActions.contains(COMMAND_DEBUG_SINGLE_METHOD);

            FileObject testFO = findFileObject(getTestLocation());
            JvmTestOperationDescriptor op = gradleTestcase.getOperation();
            // reporting adds signature to method name, this needs to be stripped away
            String mName = op.getMethodName();
            if(mName != null) {
                mName = mName.replaceFirst("[^\\p{javaJavaIdentifierPart}].*", "");
            }
            String tcName = op.getClassName();

            SingleMethod methodSpec;
            if (tcName != null && tcName.contains("$")) {
                String[] nestedSplit = tcName.split("\\$", 2);
                String[] topLevelSplit = nestedSplit[0].split("\\.");
                methodSpec = new SingleMethod(mName, new NestedClass(nestedSplit[1].replace("$", "."), topLevelSplit[topLevelSplit.length - 1], testFO));
            } else {
                if (tcName != null) {
                    String[] topLevelSplit = tcName.split("\\.");
                    if (!testFO.getName().equals(topLevelSplit[topLevelSplit.length - 1])) {
                        methodSpec = new SingleMethod(mName, new NestedClass("", topLevelSplit[topLevelSplit.length - 1], testFO));
                    } else {
                        methodSpec = new SingleMethod(testFO, mName);
                    }
                } else {
                    methodSpec = new SingleMethod(testFO, mName);
                }
            }

            Lookup nodeContext = Lookups.fixed(methodSpec);

            if (runSupported) {
                actions.add(new ReRunTestAction(actionProvider, nodeContext, COMMAND_RUN_SINGLE_METHOD, Bundle.LBL_RerunTest()));
            }

            if (debugSupported) {
                actions.add(new ReRunTestAction(actionProvider, nodeContext, COMMAND_DEBUG_SINGLE_METHOD, Bundle.LBL_DebugTest()));
            }
        }
        return actions.toArray(Action[]::new);
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
