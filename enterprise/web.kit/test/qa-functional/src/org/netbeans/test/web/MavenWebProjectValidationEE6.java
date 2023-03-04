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
 *
 * @author Jindrich Sedek
 */
public class MavenWebProjectValidationEE6 extends MavenWebProjectValidation {

    @SuppressWarnings("hiding")
    public static final String[] TESTS = {
        "testNewMavenWebProject",
        "testNewJSP", "testNewJSP2", "testNewServlet", "testNewServlet2",
        "testCreateTLD", "testCreateTagHandler", "testNewHTML",
        "testNewSegment", "testNewDocument",
        "testCleanAndBuildProject", "testRunProject", "testRunJSP",
        "testViewServlet", "testRunServlet", "testRunTag", "testRunHTML",
        "testFinish"
    };
    
    public MavenWebProjectValidationEE6(String name) {
        super(name);
        PROJECT_NAME = "WebMavenProjectEE6";
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, MavenWebProjectValidationEE6.class, TESTS);
    }

    @Override
    protected String getEEVersion() {
        return JAVA_EE_6;
    }
}
