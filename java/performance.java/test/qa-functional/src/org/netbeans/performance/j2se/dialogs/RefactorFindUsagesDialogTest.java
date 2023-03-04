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
package org.netbeans.performance.j2se.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Find Usages Dialog
 *
 * @author mmirilovic@netbeans.org
 */
public class RefactorFindUsagesDialogTest extends PerformanceTestCase {

    private static Node testNode;
    private String TITLE, ACTION;

    /**
     * Creates a new instance of RefactorFindUsagesDialog
     *
     * @param testName test name
     */
    public RefactorFindUsagesDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of RefactorFindUsagesDialog
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public RefactorFindUsagesDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(RefactorFindUsagesDialogTest.class)
                .suite();
    }

    public void testRefactorFindUsagesDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        String BUNDLE = "org.netbeans.modules.refactoring.java.ui.Bundle";
        TITLE = Bundle.getStringTrimmed(BUNDLE, "LBL_WhereUsed");  // "Find Usages"
        ACTION = Bundle.getStringTrimmed(BUNDLE, "LBL_WhereUsedAction"); // "Find Usages..."
        testNode = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Main20kB.java");
    }

    @Override
    public void prepare() {
    }

    @Override
    public ComponentOperator open() {
        testNode.callPopup().pushMenuNoBlock(ACTION);
        return new NbDialogOperator(TITLE);
    }
}
