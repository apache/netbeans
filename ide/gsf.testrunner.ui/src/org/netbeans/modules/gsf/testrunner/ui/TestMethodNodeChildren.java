/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.gsf.testrunner.ui;

import org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.gsf.testrunner.ui.api.Manager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras, Erno Mononen
 */
public final class TestMethodNodeChildren extends Children.Array {

    /** */
    private final Testcase testcase;

    /** Creates a new instance of TestMethodNodeChildren */
    public TestMethodNodeChildren(final Testcase testcase) {
        this.testcase = testcase;
    }

    /**
     */
    @Override
    protected void addNotify() {
        Trouble trouble = testcase.getTrouble();

        int stackTraceLength = trouble.getStackTrace() != null ? trouble.getStackTrace().length : 0;
        Node[] children = new Node[stackTraceLength];

        TestRunnerNodeFactory nodeFactory = Manager.getInstance().getNodeFactory();
        for (int i = 0; i < stackTraceLength; i++) {
            if (i == 0 && stackTraceLength >= 2) {
                children[i] = nodeFactory.createCallstackFrameNode(trouble.getStackTrace()[1], trouble.getStackTrace()[0]);
            } else {
                children[i] = nodeFactory.createCallstackFrameNode(trouble.getStackTrace()[i], null);
            }
        }
        
        add(children);
    }
}
