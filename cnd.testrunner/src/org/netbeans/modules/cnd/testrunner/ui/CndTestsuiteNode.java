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

package org.netbeans.modules.cnd.testrunner.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.gsf.testrunner.ui.api.Locator;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class CndTestsuiteNode extends TestsuiteNode {

    public CndTestsuiteNode(String suiteName, boolean filtered) {
        super(null, suiteName, filtered, Lookups.singleton(new Locator() {

            public void jumpToSource(Node node) {
                Action jumpTo = node.getPreferredAction();
                if (jumpTo != null) {
                    jumpTo.actionPerformed(null);
                }
            }
        }));
    }

    private Testcase getFirstTestCase() {
        if (report == null) {
            return null;
        }
        return report.getTests().isEmpty() ? null : report.getTests().iterator().next();
    }

    @Override
    public Action getPreferredAction() {
        Testcase testcase = getFirstTestCase();
        if (testcase == null) {
            // need to have at least one test case to locate the test file
            return null;
        }
//        TestType type = TestType.valueOf(testcase.getType());
        //if (TestType.RSPEC == type) {
        //    //XXX: not the exact location of the class
        //    return new JumpToCallStackAction(this, CndTestMethodNode.getTestLocation(testcase, report.getProject()), 1);
        //}
        return new JumpToTestAction(testcase, report.getProject(), NbBundle.getMessage(CndTestsuiteNode.class, "LBL_GoToSource"), true);
    }

    @Override
    public Action[] getActions(boolean context) {
        if (context) {
            return new Action[0];
        }
        List<Action> actions = new ArrayList<Action>(3);
        Action preferred = getPreferredAction();
        if (preferred != null) {
            actions.add(preferred);
        }
        Testcase testcase = getFirstTestCase();
        // these actions are enable only if the suite had at least one test (otherwise
        // we can't reliably locate the test file)
        if (testcase != null) {
            actions.add(new RunTestSuiteAction(testcase, report.getProject(), NbBundle.getMessage(CndTestMethodNode.class, "LBL_RerunTest"), false));
            actions.add(new RunTestSuiteAction(testcase, report.getProject(), NbBundle.getMessage(CndTestMethodNode.class, "LBL_DebugTest"), true));
//            actions.add(new DisplayOutputForNodeAction(getOutput(), testcase.getSession()));
        }
        return actions.toArray(new Action[actions.size()]);
    }

}
