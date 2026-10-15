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

import org.netbeans.modules.testng.api.TestNGTestSuite;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.testng.ui.actions.DebugTestClassAction;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Marian Petras
 */
public final class TestNGSuiteNode extends TestsuiteNode {

    private InstanceContent ic;

    /**
     *
     * @param  suiteName  name of the test suite, or {@code ANONYMOUS_SUITE}
     *                    in the case of anonymous suite
     * @see  ResultDisplayHandler#ANONYMOUS_SUITE
     */
    public TestNGSuiteNode(final String suiteName, final boolean filtered) {
        this(suiteName, filtered, new InstanceContent());
    }

    private TestNGSuiteNode(String suiteName, boolean filtered, InstanceContent ic) {
        super(null, suiteName, filtered, new AbstractLookup(ic));
        this.ic = ic;
    }

    @Override
    public Action[] getActions(boolean context) {
        FileObject fo = ((TestNGTestSuite) getSuite()).getSuiteFO();
        if (fo != null) {
            ic.add(fo);
        }
        List<Action> actions = new ArrayList<Action>();
        Action preferred = getPreferredAction();
        if (preferred != null) {
            actions.add(preferred);
        }
        actions.add(SystemAction.get(DebugTestClassAction.class));
        return actions.toArray(new Action[0]);
    }

    @Override
    public Action getPreferredAction() {
        return new JumpAction(this, null);
    }


}
