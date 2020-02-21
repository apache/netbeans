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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;
import junit.textui.TestRunner;

/**
 * Test of org.netbeans.jellytools.NewFileNameLocationStepOperator.
 * @author tb115823
 */
public class NewCNDFileNameLocationStepOperatorTest extends CNDTestCase {

    public static NewCNDFileNameLocationStepOperator op;

    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        TestRunner.run(suite());
    }
    
    public static String[] tests = new String[] {
        "testInvoke", "testComponents"};
    
    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {        
        return createModuleTest(NewCNDFileNameLocationStepOperatorTest.class, tests);
    }
    
    protected void setUp() throws IOException {
        System.out.println("### "+getName()+" ###");        
    }
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewCNDFileNameLocationStepOperatorTest(String testName) {
        super(testName);
    }
    
    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() throws Throwable {
        createAndOpenTestProject();

        CNDProjectsTabOperator tab = CNDProjectsTabOperator.invoke();
        tab.getCNDProjectRootNode(getTestProjectName());

        NewFileWizardOperator wop = NewFileWizardOperator.invoke();
        wop.selectProject(getTestProjectName()); //NOI18N
        // C++
        String javaClassesLabel = "C++"; //TODO: find appropriate bundle (unable to locate it so far)
        // C++ Source File
        String javaClassLabel = "C++ Source File"; //TODO: find appropriate bundle (unable to locate it so far)
        wop.selectCategory(javaClassesLabel);
        wop.selectFileType(javaClassLabel);
        wop.next();
        op = new NewCNDFileNameLocationStepOperator();
    }
    
    public void testComponents() {
        op.txtObjectName().setText("NewObject"); // NOI18N
        assertEquals("Project name not propagated from previous step", getTestProjectName(), op.txtProject().getText()); // NOI18N
        op.selectExtension("cpp"); //NOI18N
        
        String filePath = op.txtCreatedFile().getText();
        assertTrue("Created file path doesn't contain " + getTestProjectName() + ".", filePath.indexOf(getTestProjectName()) > 0);  // NOI18N
        assertTrue("Created file path doesn't contain NewObject name.", filePath.indexOf("NewObject") > 0);  //NOI18N
        op.cancel();
    }
    
}
