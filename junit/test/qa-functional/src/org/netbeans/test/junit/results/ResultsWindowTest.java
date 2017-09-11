/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
