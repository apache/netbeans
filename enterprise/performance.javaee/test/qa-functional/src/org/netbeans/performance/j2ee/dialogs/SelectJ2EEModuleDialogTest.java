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
package org.netbeans.performance.j2ee.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EESetup;

/**
 * Test of Project Properties Window
 *
 * @author mmirilovic@netbeans.org
 */
public class SelectJ2EEModuleDialogTest extends PerformanceTestCase {

    private static Node testNode;

    /**
     * Creates a new instance of SelectJ2EEModuleDialogTest
     *
     * @param testName
     */
    public SelectJ2EEModuleDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    /**
     * Creates a new instance of SelectJ2EEModuleDialogTest
     *
     * @param testName
     * @param performanceDataName
     */
    public SelectJ2EEModuleDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 2000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EESetup.class).addTest(SelectJ2EEModuleDialogTest.class).suite();
    }

    public void testSelectJ2EEModuleDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        JTreeOperator tree = new ProjectsTabOperator().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        String JAVA_EE_MODULES = Bundle.getStringTrimmed(
                "org.netbeans.modules.j2ee.earproject.ui.Bundle",
                "LBL_LogicalViewNode");
        testNode = new Node(new ProjectRootNode(tree, "TestApplication"), JAVA_EE_MODULES);
    }

    public void prepare() {
        // do nothing
    }

    public ComponentOperator open() {
        testNode.performPopupActionNoBlock("Add Java EE Module...");
        return new NbDialogOperator("Add Java EE Module");
    }
}
