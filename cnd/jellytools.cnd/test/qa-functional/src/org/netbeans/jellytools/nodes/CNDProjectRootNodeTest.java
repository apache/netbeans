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

package org.netbeans.jellytools.nodes;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.FindInFilesOperator;
import org.netbeans.jellytools.CNDProjectsTabOperator;
import org.netbeans.jellytools.CNDTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.TimeoutExpiredException;

/**
 *  Test of CNDProjectRootNode. Mostly copied/moved from ProjectRootNodeTest,
 *  but did not extend ProjectRootNodeTest, because a test library dependency on 
 *  jellytools module would be neccessary.
 *
 */
public class CNDProjectRootNodeTest extends CNDTestCase
{


    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public CNDProjectRootNodeTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition */
    public static Test suite() {        
        return createModuleTest(CNDProjectRootNodeTest.class,
                "testVerifyPopup", "testFind",
                "testBuildProject", "testCleanProject",
                "testProperties");
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    private static CNDProjectRootNode projectRootNode;
    
    /** Find node. */
    protected void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");
        if(projectRootNode == null) {
            createAndOpenTestProject();
            setToolchain("GNU");
            projectRootNode = CNDProjectsTabOperator.invoke().getCNDProjectRootNode(getTestProjectName()); // NOI18N
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
        // wait status text "SampleCNDProject (Build)"
        statusTextTracer.waitText("Build", false); // NOI18N
        // wait status text "Build successful.
        statusTextTracer.waitText("successful", false); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test cleanProject*/
    public void testCleanProject() {
        MainWindowOperator.StatusTextTracer statusTextTracer = MainWindowOperator.getDefault().getStatusTextTracer();
        statusTextTracer.start();
        projectRootNode.cleanProject();
        // wait status text "SampleCNDProject (Clean)"
        statusTextTracer.waitText("Clean", false); // NOI18N
        // wait status text "Clean successful."
        statusTextTracer.waitText("successful", false); // NOI18N
        statusTextTracer.stop();
    }
    
    /** Test properties */
    public void testProperties() {
        projectRootNode.properties();
        new NbDialogOperator(getTestProjectName()).close(); //NOI18N
    }
}
