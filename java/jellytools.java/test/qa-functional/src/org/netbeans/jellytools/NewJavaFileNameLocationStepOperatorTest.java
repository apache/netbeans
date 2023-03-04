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
package org.netbeans.jellytools;

import java.io.IOException;
import junit.framework.Test;

/**
 * Test of org.netbeans.jellytools.NewFileNameLocationStepOperator.
 *
 * @author tb115823
 */
public class NewJavaFileNameLocationStepOperatorTest extends JellyTestCase {

    public static NewJavaFileNameLocationStepOperator op;
    public static String[] tests = new String[]{
        "testInvoke", "testComponents"};

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NewJavaFileNameLocationStepOperatorTest.class, tests);
    }

    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }

    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public NewJavaFileNameLocationStepOperatorTest(String testName) {
        super(testName);
    }

    /** Test of invoke method. Opens New File wizard and waits for the dialog. */
    public void testInvoke() {
        NewFileWizardOperator wop = NewFileWizardOperator.invoke();
        wop.selectProject("SampleProject"); //NOI18N
        // Java
        String javaClassesLabel = Bundle.getString("org.netbeans.modules.java.project.Bundle", "Templates/Classes");
        wop.selectCategory(javaClassesLabel);
        wop.selectFileType("Java Class");
        wop.next();
        op = new NewJavaFileNameLocationStepOperator();
    }

    public void testComponents() {
        op.txtObjectName().setText("NewObject"); // NOI18N
        assertEquals("Project name not propagated from previous step", "SampleProject", op.txtProject().getText()); // NOI18N
        op.selectSourcePackagesLocation();
        op.selectPackage("sample1"); // NOI18N
        String filePath = op.txtCreatedFile().getText();
        assertTrue("Created file path doesn't contain SampleProject.", filePath.indexOf("SampleProject") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain sample1 package name.", filePath.indexOf("sample1") > 0);  // NOI18N
        assertTrue("Created file path doesn't contain NewObject name.", filePath.indexOf("NewObject") > 0);  //NOI18N
        op.cancel();
    }
}
