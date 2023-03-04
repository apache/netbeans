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
import junit.framework.Test;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.JavaProjectsTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/** Test org.netbeans.jellytools.actions.FindAction
 *
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class FindActionTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testPerformPopup",
        "testPerformMenu",
        "testPerformAPI",
        "testPerformShortcut"
    };

    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public FindActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(FindActionTest.class, tests);
    }

    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
    }

    /** Test performPopup */
    public void testPerformPopup() {
        Node node = new JavaProjectsTabOperator().getProjectRootNode("SampleProject"); // NOI18N
        new FindAction().performPopup(node);
        new FindInFilesOperator().close();
    }

    /** Test performMenu */
    public void testPerformMenu() {
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1"); // NOI18N
        new FindAction().performMenu(node);
        new FindInFilesOperator().close();
    }

    /** Test performAPI */
    public void testPerformAPI() {
        new FindAction().performAPI();
        new FindInFilesOperator().close();
    }

    /** Test performShortcut */
    public void testPerformShortcut() {
        new FindAction().performShortcut();
        new FindInFilesOperator().close();
        // On some linux it may happen autorepeat is activated and it 
        // opens dialog multiple times. So, we need to close all modal dialogs.
        // See issue http://www.netbeans.org/issues/show_bug.cgi?id=56672.
        closeAllModal();
    }
}
