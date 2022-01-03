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
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.DiffViewAction;
import org.netbeans.modules.gsf.testrunner.ui.api.Locator;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class CndTestMethodNode extends TestMethodNode {

    public CndTestMethodNode(Testcase testcase, Project project) {
        super(testcase, project, Lookups.singleton(new Locator() {

            public void jumpToSource(Node node) {
                node.getPreferredAction().actionPerformed(null);
            }
        }));
    }

    /**
     */
    @Override
    public Action getPreferredAction() {
        // the location to jump from the node
        String testLocation = getTestLocation(testcase, getProject());
        String stackTrace = getTestCaseLineFromStackTrace(testcase);
        String jumpToLocation = stackTrace != null
                ? stackTrace
                : testLocation;

        return jumpToLocation == null
                ? new JumpToTestAction(testcase, getProject(), NbBundle.getMessage(CndTestMethodNode.class, "LBL_GoToSource"), false)
                : new JumpToCallStackAction(this, jumpToLocation);
    }
    
    static String getTestLocation(Testcase testcase, Project project) {
        if (testcase.getLocation() == null) {
            return null;
        }
//        PythonPlatform platform = PythonPlatform.platformFor(project);
//        if (platform != null && platform.isJython()) {
//            // XXX: return no location for Jython -- ExampleMethods#implementation_backtrace
//            // behaves differently for MRI and Jython, on Jython the test file itself is not present
//            return null;
//        }
        return testcase.getLocation();
    }

        /**
     * Gets the line from the stack trace representing the last line in the test class.
     * If that can't be resolved
     * then returns the second line of the stack trace (the
     * first line represents the error message) or <code>null</code> if there
     * was no (usable) stack trace attached.
     *
     * @return
     */
    private static String getTestCaseLineFromStackTrace(Testcase testcase) {
        if (testcase.getTrouble() == null) {
            return null;
        }
        String[] stacktrace = testcase.getTrouble().getStackTrace();
        if (stacktrace == null || stacktrace.length <= 1) {
            return null;
        }

        // Skip unittest.py stuff
        int j = 1;
        for (; j < stacktrace.length; j++) {
            if (!stacktrace[j].contains("unittest.py")) { // NOI18N
                break;
            }
        }

        // Rails specific - maybe this doesn't apply to Python/Django?
//        if (stacktrace.length > 2) {
//            String underscoreName = PythonUtils.camelToUnderlinedName(testcase.getClassName());
//            for (int i = 0; i < stacktrace.length; i++) {
//                if (stacktrace[i].contains(underscoreName) && stacktrace[i].contains(testcase.getName())) {
//                    return stacktrace[i];
//                }
//            }
//        }

        if (j == stacktrace.length) {
            j = stacktrace.length-1;
        }

        return stacktrace[j];
    }


    @Override
    public Action[] getActions(boolean context) {
        if (context) {
            return new Action[0];
        }
        List<Action> actions = new ArrayList<Action>();
        actions.add(getPreferredAction());
        actions.add(new DiffViewAction(testcase));
        actions.add(new RunTestMethodAction(testcase, getProject(), NbBundle.getMessage(CndTestMethodNode.class, "LBL_RerunTest"), false));
        actions.add(new RunTestMethodAction(testcase, getProject(), NbBundle.getMessage(CndTestMethodNode.class, "LBL_DebugTest"), true));
//        actions.add(new DisplayOutputForNodeAction(testcase.getOutput(), testcase.getSession()));
        return actions.toArray(new Action[actions.size()]);
    }
}
