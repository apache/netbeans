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
        return actions.toArray(new Action[0]);
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
