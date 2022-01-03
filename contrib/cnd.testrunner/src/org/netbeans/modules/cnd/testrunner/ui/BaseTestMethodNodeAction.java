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

import java.util.Collection;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.ui.api.TestNodeAction;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Base class for actions associated with a test method node.
 *
 */
abstract class BaseTestMethodNodeAction extends TestNodeAction {

    private static final Logger LOGGER = Logger.getLogger(BaseTestMethodNodeAction.class.getName());

    protected final Testcase testcase;
    protected final Project project;
    protected final String name;

    public BaseTestMethodNodeAction(Testcase testcase, Project project, String name) {
        this.testcase = testcase;
        this.project = project;
        this.name = name;
    }

    @Override
    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return name;
        }
        return super.getValue(key);
    }

    protected String getTestMethod() {
        return testcase.getClassName() + "/" + testcase.getName(); //NOI18N
    }

//    protected FileObject getTestSourceRoot() {
//        PythonProject baseProject = project.getLookup().lookup(PythonProject.class);
//        // need to use test source roots, not source roots -- see the comments in #135680
//        FileObject[] testRoots = baseProject.getTestSourceRootFiles();
//        // if there are not test roots, return the project root -- works in rails projects
//        return 0 == testRoots.length ? project.getProjectDirectory() : testRoots[0];
//    }
//
//    protected TestRunner getTestRunner(TestRunner.TestType testType) {
//        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
//        for (TestRunner each : testRunners) {
//            if (each.supports(testType)) {
//                return each;
//            }
//        }
//        return null;
//    }
}
