/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.testng.ui;

import org.netbeans.modules.testng.api.TestNGTestcase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.SingleMethod;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Marian Petras
 * @author Lukas Jungmann
 */
final class TestNGMethodNode extends TestMethodNode {

    private InstanceContent ic;

    public TestNGMethodNode(Testcase testcase, Project project) {
        this(testcase, project, new InstanceContent());
    }

    private TestNGMethodNode(Testcase tc, Project p, InstanceContent ic) {
        super(tc, p, new AbstractLookup(ic));
        this.ic = ic;
    }

    @Override
    public Action[] getActions(boolean context) {
        SingleMethod sm = new SingleMethod(getTestcase().getClassFileObject(), getTestcase().getTestName());
        ic.add(sm);
        ic.add(getTestcase());
        List<Action> actions = new ArrayList<Action>();
        Action preferred = getPreferredAction();
        if (preferred != null) {
            actions.add(preferred);
        }

        for (ActionProvider ap : Lookup.getDefault().lookupAll(ActionProvider.class)) {
            List<String> supportedActions = Arrays.asList(ap.getSupportedActions());
            if (!getTestcase().isConfigMethod() && supportedActions.contains(SingleMethod.COMMAND_RUN_SINGLE_METHOD)) {
                actions.add(new TestMethodNodeAction(ap, Lookups.singleton(sm), SingleMethod.COMMAND_RUN_SINGLE_METHOD, "LBL_RerunTest"));
            }
            if (!getTestcase().isConfigMethod() && supportedActions.contains(SingleMethod.COMMAND_DEBUG_SINGLE_METHOD)) {
                actions.add(new TestMethodNodeAction(ap, Lookups.singleton(sm), SingleMethod.COMMAND_DEBUG_SINGLE_METHOD, "LBL_DebugTest"));
            }
        }
	actions.addAll(Arrays.asList(super.getActions(context)));
        return actions.toArray(new Action[actions.size()]);
    }

    @Override
    public Action getPreferredAction() {
        return new JumpAction(this, null);
    }

    public TestNGTestcase getTestcase() {
        return (TestNGTestcase) testcase;
    }

    @Override
    public String getHtmlDisplayName() {
        return !getTestcase().isConfigMethod() ? super.getHtmlDisplayName()
                : "<i>" + super.getHtmlDisplayName() + "</i>";
    }
}
