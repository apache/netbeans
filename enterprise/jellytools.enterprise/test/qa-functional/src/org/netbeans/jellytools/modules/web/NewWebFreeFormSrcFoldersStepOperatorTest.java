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
package org.netbeans.jellytools.modules.web;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;

/**
 * Test of NewWebFreeFormSrcFoldersStepOperator.
 *
 * @author Martin Schovanek
 */
public class NewWebFreeFormSrcFoldersStepOperatorTest extends JellyTestCase {

    /**
     * Constructor required by JUnit.
     *
     * @param testName method name to be used as testcase
     */
    public NewWebFreeFormSrcFoldersStepOperatorTest(String testName) {
        super(testName);
    }

    /** Method used for explicit testsuite definition
     * @return  created suite
     */
    public static Test suite() {
        return createModuleTest(NewWebFreeFormSrcFoldersStepOperatorTest.class);
    }

    @Override
    public void setUp() {
        System.out.println("### " + getName() + " ###");
    }

    /** Invokes and verifies the dialog. */
    public void testVerify() throws IOException {
        NewWebFreeFormNameStepOperator nameStep = NewWebFreeFormNameStepOperator.invoke();
        System.out.println("project=" + new File(getDataDir(), "WebFreeFormSrc").getCanonicalPath());
        nameStep.setProjectLocation(new File(getDataDir(), "WebFreeFormSrc").getCanonicalPath());
        nameStep.next();
        new NewWebFreeFormActionsStepOperator().next();
        new NewWebFreeFormWebSrcStepOperator().next();
        NewWebFreeFormSrcFoldersStepOperator srcFoldersStep = new NewWebFreeFormSrcFoldersStepOperator();
        srcFoldersStep.verify();
        srcFoldersStep.close();
    }
}
