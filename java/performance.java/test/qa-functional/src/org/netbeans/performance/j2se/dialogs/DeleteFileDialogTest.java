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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.DeleteAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of Delete File Dialog
 *
 * @author mmirilovic@netbeans.org
 */
public class DeleteFileDialogTest extends PerformanceTestCase {

    private static Node testNode;
    private String TITLE;

    /**
     * Creates a new instance of RefactorRenameDialog
     *
     * @param testName test name
     */
    public DeleteFileDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of RefactorRenameDialog
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public DeleteFileDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class)
                .addTest(DeleteFileDialogTest.class)
                .suite();
    }

    public void testDeleteFileDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        TITLE = "Delete";
        testNode = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|Class.java");
    }

    @Override
    public void prepare() {
        // do nothing
    }

    @Override
    public ComponentOperator open() {
        // invoke Delete from the popup menu
        new DeleteAction().performShortcut(testNode);
        return new NbDialogOperator(TITLE);
    }

    @Override
    public void close() {
        if (testedComponentOperator != null && testedComponentOperator.isShowing()) {
            ((NbDialogOperator) testedComponentOperator).cancel();
        }
    }
}
