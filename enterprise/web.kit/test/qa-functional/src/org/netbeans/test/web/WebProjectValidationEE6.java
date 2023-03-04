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
package org.netbeans.test.web;

import junit.framework.Test;

/**
 * Test web project Java EE 6. It is a base class for other sub classes.
 */
public class WebProjectValidationEE6 extends WebProjectValidation {

    public static String[] TESTS = {
        "testNewWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testNewHTML", "testCreateTLD", "testCreateTagHandler", "testNewSegment",
        "testNewDocument", "testJSPNavigator", "testHTMLNavigator",
        "testCompileAllJSP", "testCompileJSP", "testCleanAndBuildProject",
        "testRedeployProject", "testRunProject", "testRunJSP",
        "testViewServlet", "testRunServlet", "testRunHTML", "testRunTag",
        "testFinish"
    };

    /** Need to be defined because of JUnit */
    public WebProjectValidationEE6(String name) {
        super(name);
        PROJECT_NAME = "WebJavaEE6Project"; // NOI18N
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, WebProjectValidationEE6.class, TESTS);
    }

    protected String getEEVersion() {
        return JAVA_EE_6;
    }
}
