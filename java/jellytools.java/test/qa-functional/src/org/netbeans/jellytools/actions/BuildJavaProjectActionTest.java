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
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 * Test of org.netbeans.jellytools.actions.BuildProjectAction
 *
 * @author Jiri Skrivanek
 */
public class BuildJavaProjectActionTest extends JellyTestCase {

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public BuildJavaProjectActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(BuildJavaProjectActionTest.class, "testPerformPopup");
    }

    @Override
    protected void setUp() throws IOException {
        openDataProjects("SampleProject");
    }

    /** Test performPopup method. */
    public void testPerformPopup() {
        Node n = new ProjectsTabOperator().getProjectRootNode("SampleProject"); // NOI18N
        MainWindowOperator.StatusTextTracer statusTextTracer =
                MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        new BuildJavaProjectAction().performPopup(n);
        // wait status text "Building SampleProject (jar)"
        statusTextTracer.waitText("jar", true); // NOI18N
        // wait status text "Finished building SampleProject (jar)"
        statusTextTracer.waitText("jar", true); // NOI18N
        statusTextTracer.stop();
    }
}
