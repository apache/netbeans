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
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTest;

/**
 * Test FilesTabOperator.
 *
 * @author Jiri Skrivanek
 */
public class FilesTabOperatorTest extends JellyTestCase {

    public static final String[] tests = new String[]{
        "testInvoke",
        "testTree",
        "testGetProjectNode",
        "testVerify"
    };

    public FilesTabOperatorTest(java.lang.String testName) {
        super(testName);
    }

    public static NbTest suite() {
        return (NbTest) NbModuleSuite.create(
                NbModuleSuite.createConfiguration(FilesTabOperatorTest.class).
                addTest(tests).
                enableModules(".*").clusters(".*"));
    }

    /** Print out test name. */
    @Override
    public void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");
        openDataProjects("SampleProject");
    }
    private static FilesTabOperator filesOper;

    /**
     * Test of invoke method.
     */
    public void testInvoke() {
        FilesTabOperator.invoke().close();
        filesOper = FilesTabOperator.invoke();
    }

    /**
     * Test of tree method.
     */
    public void testTree() {
        RuntimeTabOperator.invoke();
        // has to make tab visible
        filesOper.tree();
    }

    /**
     * Test of getRootNode method.
     */
    public void testGetProjectNode() {
        filesOper.getProjectNode("SampleProject");   // NOI18N
    }

    /**
     * Test of verify method.
     */
    public void testVerify() {
        filesOper.verify();
    }
}
