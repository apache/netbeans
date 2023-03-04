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
package org.netbeans.jellytools.nodes;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.JavaProjectsTabOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;

/**
 * Test of JavaProjectRootNode. Mostly copied/moved from ProjectRootNodeTest,
 * but did not extend ProjectRootNodeTest, because a test library dependency on
 * jellytools module would be necessary.
 *
 * @author Vojtech Sigler
 */
public class JavaProjectRootNodeTest extends JellyTestCase {

    private static JavaProjectRootNode projectRootNode;
    public static String[] tests = new String[]{
        "testVerifyPopup",
        "testFind",
        "testBuildProject",
        "testCleanProject",
        "testProperties"
    };

    /**
     * constructor required by JUnit
     *
     * @param testName method name to be used as testcase
     */
    public JavaProjectRootNodeTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition */
    public static Test suite() {
        return createModuleTest(JavaProjectRootNodeTest.class, tests);
    }

    /** Find node. */
    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
        if (projectRootNode == null) {
            projectRootNode = JavaProjectsTabOperator.invoke().getJavaProjectRootNode("SampleProject"); // NOI18N
        }
    }

    /** Test verifyPopup */
    public void testVerifyPopup() {
        projectRootNode.verifyPopup();
    }

    /** Test find */
    public void testFind() {
        projectRootNode.find();
        new FindInFilesOperator().close();
    }

    /** Test buildProject */
    public void testBuildProject() {
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        projectRootNode.buildProject();
        // wait status text "Building SampleProject (jar)"
        statusTextTracer.waitText("jar", true); // NOI18N
        // wait status text "Finished building SampleProject (jar).
        statusTextTracer.waitText("jar", true); // NOI18N
        statusTextTracer.stop();
    }

    /** Test cleanProject*/
    public void testCleanProject() {
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        projectRootNode.cleanProject();
        // wait status text "Building SampleProject (clean)"
        statusTextTracer.waitText("clean", true); // NOI18N
        // wait status text "Finished building SampleProject (clean).
        statusTextTracer.waitText("clean", true); // NOI18N
        statusTextTracer.stop();
    }

    /** Test properties */
    public void testProperties() {
        projectRootNode.properties();
        new NbDialogOperator("SampleProject").close(); //NOI18N
    }
}
