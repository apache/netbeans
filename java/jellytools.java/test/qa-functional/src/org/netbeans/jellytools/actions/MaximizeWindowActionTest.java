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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/**
 * Test org.netbeans.jellytools.actions.MaximizeWindowAction and
 * org.netbeans.jellytools.actions.RestoreWindowAction.
 *
 * @author Jiri Skrivanek
 */
public class MaximizeWindowActionTest extends JellyTestCase {

    /**
     * constructor required by JUnit
     *
     * @param testName method name
     */
    public MaximizeWindowActionTest(String testName) {
        super(testName);
    }

    /** method used for explicit testsuite definition
     */
    public static Test suite() {
        return createModuleTest(MaximizeWindowActionTest.class);
    }

    @Override
    protected void setUp() throws IOException {
        System.out.println("### " + getName() + " ###");  // NOI18N
        openDataProjects("SampleProject");
    }

    /** Test performPopup */
    public void testPerformPopup() {
        // test editor TopComponent
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().performAPI(node);
        EditorOperator eo = new EditorOperator("SampleClass1.java"); // NOI18N
        new MaximizeWindowAction().performPopup(eo);
        new RestoreWindowAction().performPopup(eo);
        eo.closeDiscard();
        // test editor TopComponent
        ProjectsTabOperator pto = new ProjectsTabOperator();
        new MaximizeWindowAction().performPopup(pto);
        new RestoreWindowAction().performPopup(pto);
    }

    /** Test performAPI  */
    public void testPerformAPI() {
        // test editor TopComponent
        Node node = new Node(new SourcePackagesNode("SampleProject"), "sample1|SampleClass1.java"); // NOI18N
        new OpenAction().performAPI(node);
        EditorOperator eo = new EditorOperator("SampleClass1.java"); // NOI18N
        new MaximizeWindowAction().performAPI(eo);
        new RestoreWindowAction().performAPI(eo);
        eo.closeDiscard();
        // test non editor TopComponent
        ProjectsTabOperator pto = new ProjectsTabOperator();
        new MaximizeWindowAction().performAPI(pto);
        new RestoreWindowAction().performAPI();
    }
}
