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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.junit.api.JUnitTestSuite;
import org.netbeans.modules.java.testrunner.OutputUtils;
import org.netbeans.modules.gsf.testrunner.api.TestMethodNodeAction;
import org.netbeans.modules.junit.ui.api.JUnitTestsuiteNode;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author answer
 */
public class AntJUnitTestsuiteNode extends JUnitTestsuiteNode {

    public AntJUnitTestsuiteNode(String suiteName, boolean filtered, String projectType, String testingFramework) {
        super(suiteName, filtered, projectType, testingFramework);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        Action preferred = getPreferredAction();
        if (preferred != null) {
            actions.add(preferred);
        }

        FileObject testFO = ((JUnitTestSuite)getSuite()).getSuiteFO();
        if (testFO != null){
            ActionProvider actionProvider = OutputUtils.getActionProvider(testFO);
            if (actionProvider != null){
                List supportedActions = Arrays.asList(actionProvider.getSupportedActions());
                Lookup nodeContext = Lookups.singleton(testFO);

                if (supportedActions.contains(ActionProvider.COMMAND_TEST_SINGLE) &&
                        actionProvider.isActionEnabled(ActionProvider.COMMAND_TEST_SINGLE, nodeContext)) {
                    actions.add(new TestMethodNodeAction(actionProvider, 
                            nodeContext, ActionProvider.COMMAND_TEST_SINGLE, Bundle.LBL_RerunTest()));
                }
                if (supportedActions.contains(ActionProvider.COMMAND_DEBUG_TEST_SINGLE) &&
                        actionProvider.isActionEnabled(ActionProvider.COMMAND_DEBUG_TEST_SINGLE, nodeContext)) {
                    actions.add(new TestMethodNodeAction(actionProvider,
                            nodeContext, ActionProvider.COMMAND_DEBUG_TEST_SINGLE, Bundle.LBL_DebugTest()));
                }
            }
        }
        
        return actions.toArray(new Action[0]);
    }
    
}
