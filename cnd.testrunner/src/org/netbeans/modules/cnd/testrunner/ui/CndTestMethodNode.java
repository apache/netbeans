/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
