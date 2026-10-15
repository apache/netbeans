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

package org.netbeans.modules.junit.ui.api;

import org.netbeans.modules.java.testrunner.ui.api.JumpAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.TestMethodNode;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.junit.api.JUnitTestcase;
import org.openide.util.Lookup;

/**
 *
 * @author answer
 */
public class JUnitTestMethodNode extends TestMethodNode {
    private final String projectType;
    private final String testingFramework;

    public JUnitTestMethodNode(Testcase testcase, Project project, Lookup lookup, String projectType, String testingFramework) {
        super(testcase, project, lookup);
        this.projectType = projectType;
        this.testingFramework = testingFramework;
    }

    public JUnitTestMethodNode(Testcase testcase, Project project, String projectType, String testingFramework) {
        super(testcase, project);
        this.projectType = projectType;
        this.testingFramework = testingFramework;
    }

    @Override
    public Action getPreferredAction() {
        return new JumpAction(this, null, projectType, testingFramework);
    }

    @Override
    public JUnitTestcase getTestcase() {
        return (JUnitTestcase) testcase;
    }

}
