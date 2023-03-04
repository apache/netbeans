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
package org.netbeans.jellytools.actions;

import java.io.IOException;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.junit.NbTestSuite;

/**
 * Test of org.netbeans.jellytools.actions.FindInFilesAction.
 *
 * @author Jiri Skrivanek
 */
public class FindInFilesActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testPerformPopup",
        "testPerformMenu",
        "testPerformAPI"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FindInFilesActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition */
    public static NbTestSuite suite() {
        return (NbTestSuite) createModuleTest(FindInFilesActionTest.class, tests);
    }

    /** Redirect output to log files, wait before each test case. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Test of performPopup method. */
    public void testPerformPopup() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performPopup(node);
        new FindInFilesOperator().close();
    }

    /** Test of performMenu method. */
    public void testPerformMenu() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performMenu(node);
        new FindInFilesOperator().close();
        // need to wait here because next menu can disappear
        new EventTool().waitNoEvent(500);
        new FindInFilesAction().performMenu();
        new FindInFilesOperator().close();
    }

    /** Test of performAPI method. */
    public void testPerformAPI() {
        Node node = new ProjectsTabOperator().getProjectRootNode("SampleProject");  // NOI18N
        new FindInFilesAction().performAPI(node);
        new FindInFilesOperator().close();
        new FindInFilesAction().performAPI(new ProjectsTabOperator());
        new FindInFilesOperator().close();
        new FindInFilesAction().performAPI();
        new FindInFilesOperator().close();
    }
}
