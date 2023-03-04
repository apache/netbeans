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

package org.netbeans.test.junit.results;

import junit.framework.Test;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.jellytools.modules.junit.testcases.JunitTestCase;
import org.netbeans.test.junit.utils.ResultWindowOperator;
import org.netbeans.test.junit.utils.Utilities;

/**
 *
 * @author max.sauer@sun.com
 */
public class ResultsWindowTest extends JunitTestCase {
    /** path to sample files */
    private static final String TEST_PACKAGE_PATH =
            "org.netbeans.test.junit.testresults";
    
    /** name of sample package */
    private static final String TEST_PACKAGE_NAME = TEST_PACKAGE_PATH+".test";
    
    /**
     * Adds tests to suite
     * @return created suite
     */
    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(ResultsWindowTest.class).addTest(
            "testResultWindowOpened").enableModules(".*").clusters(".*"));
    }
    
    /** Creates a new instance of ResultsWindowTest */
    public ResultsWindowTest(String testName) {
        super(testName);
    }
    
    /** Tests visiblility of results window */
    public void testResultWindowOpened() {
        //open Test package
        Node n = Utilities.openFile(Utilities.TEST_PACKAGES_PATH +
                "|" + TEST_PACKAGE_NAME + "|" + Utilities.TEST_CLASS_NAME);

        Utilities.takeANap(5000);
        JPopupMenuOperator jpmo = n.callPopup();
        Utilities.takeANap(5000);
        jpmo.pushMenu(Utilities.RUN_FILE);
        Utilities.takeANap(9000);
        ResultWindowOperator rwo = ResultWindowOperator.invoke();
        assertTrue("Junit Output window should be visible", rwo.isVisible());
        rwo.close(); //close it
        assertFalse("Junit Output window is visible," +
                "should be closed", rwo.isShowing());
    }
    
    /**
     * Test whether filter button inside results window is enabled
     */
    public void testFilterButtonEnabled() {
        Node n = Utilities.openFile(Utilities.TEST_PACKAGES_PATH + "|"
                + TEST_PACKAGE_NAME + "|EmptyJUnitTest");
        JPopupMenuOperator jpmo = n.callPopup();
        jpmo.pushMenu(Utilities.RUN_FILE);
        Utilities.takeANap(4000);
        ResultWindowOperator rwo = ResultWindowOperator.invoke();
        assertTrue("Filter button should eb enabled",
                rwo.isFilterButtonEnabled());
        
    }
    
    
    /**
     * Test functionality of filter button
     * Runs suite with three tests:
     * one with both failing and succeeding tests
     * one with only failing
     * one with only succeeding
     * (testresults.test.TestResultsSuite from JunitTestProject)
     */
    public void testPressFilterButton() {
        Utilities.testWholeProject();
        Utilities.takeANap(4000);
        ResultWindowOperator rwo = new ResultWindowOperator();
        rwo.pushFilterButton();

        //TODO: Finish this test        
    }
    
    
}
