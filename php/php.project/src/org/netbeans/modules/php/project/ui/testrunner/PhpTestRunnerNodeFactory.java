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

package org.netbeans.modules.php.project.ui.testrunner;

import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.ui.api.TestRunnerNodeFactory;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.ui.api.TestsuiteNode;
import org.openide.nodes.Node;

public class PhpTestRunnerNodeFactory extends TestRunnerNodeFactory {

    private final JumpToCallStackAction.Callback callback;


    public PhpTestRunnerNodeFactory(JumpToCallStackAction.Callback callback) {
        this.callback = callback;
    }

    @Override
    public Node createTestMethodNode(Testcase testcase, Project project) {
        return new TestMethodNode(testcase, project);
    }

    @Override
    public Node createCallstackFrameNode(String frameInfo, String displayName) {
        return new PhpCallstackFrameNode(frameInfo, displayName, callback);
    }

    @Override
    public TestsuiteNode createTestSuiteNode(String suiteName, boolean filtered) {
        return new TestsuiteNode(suiteName, filtered);
    }
}
