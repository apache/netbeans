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
package org.netbeans.modules.junit.ant.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.junit.api.JUnitTestcase;
import org.netbeans.modules.java.testrunner.OutputUtils;
import org.netbeans.modules.gsf.testrunner.api.TestMethodNodeAction;
import org.netbeans.modules.junit.ui.api.JUnitTestMethodNode;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import static org.netbeans.spi.project.SingleMethod.COMMAND_RUN_SINGLE_METHOD;
import static org.netbeans.spi.project.SingleMethod.COMMAND_DEBUG_SINGLE_METHOD;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author answer
 */
public class AntJUnitTestMethodNode extends JUnitTestMethodNode {

    public AntJUnitTestMethodNode(Testcase testcase, Project project, Lookup lookup, String projectType, String testingFramework) {
        super(testcase, project, lookup, projectType, testingFramework);
    }

    public AntJUnitTestMethodNode(Testcase testcase, Project project, String projectType, String testingFramework) {
        super(testcase, project, projectType, testingFramework);
    }

    @NbBundle.Messages({
        "LBL_RerunTest=Run Again",
        "LBL_DebugTest=Debug"
    })
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        Action preferred = getPreferredAction();
        if (preferred != null) {
            actions.add(preferred);
        }
        // Method node might belong to an inner class
        FileObject testFO = ((JUnitTestcase) testcase).getClassFileObject(true);
        if (testFO == null) {
            Logger.getLogger(AntJUnitTestMethodNode.class.getName()).log(Level.INFO, "Test running process was probably abnormally interrupted. Could not locate FileObject for {0}", testcase.toString());
            for (Action prefAction : actions) {
                prefAction.setEnabled(false);
            }
        } else {
            boolean parameterized = false;
            try {
                String text = testFO.asText();
                if (text != null) {
                    text = text.replace("\n", "").replace(" ", "");
                    if ((text.contains("@RunWith") || text.contains("@org.junit.runner.RunWith")) //NOI18N
                            && text.contains("Parameterized.class)")) {  //NOI18N
                        parameterized = true;
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            if (!parameterized) {
                ActionProvider actionProvider = OutputUtils.getActionProvider(testFO);
                if (actionProvider != null) {
                    List supportedActions = Arrays.asList(actionProvider.getSupportedActions());

                    SingleMethod methodSpec = new SingleMethod(testFO, testcase.getName());
                    Lookup nodeContext = Lookups.singleton(methodSpec);
                    if (supportedActions.contains(COMMAND_RUN_SINGLE_METHOD)
                            && actionProvider.isActionEnabled(COMMAND_RUN_SINGLE_METHOD, nodeContext)) {
                        actions.add(new TestMethodNodeAction(actionProvider, nodeContext, COMMAND_RUN_SINGLE_METHOD, Bundle.LBL_RerunTest()));
                    }
                    if (supportedActions.contains(COMMAND_DEBUG_SINGLE_METHOD)
                            && actionProvider.isActionEnabled(COMMAND_DEBUG_SINGLE_METHOD, nodeContext)) {
                        actions.add(new TestMethodNodeAction(actionProvider, nodeContext, COMMAND_DEBUG_SINGLE_METHOD, Bundle.LBL_DebugTest()));
                    }
                }
            }
        }
        actions.addAll(Arrays.asList(super.getActions(context)));

        return actions.toArray(new Action[0]);
    }
    
}
