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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
