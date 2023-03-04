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
package javahelp.gui;

import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.HelpOperator;

import org.netbeans.junit.NbModuleSuite;

import junit.framework.Test; 

/**
 * JellyTestCase test case with implemented Java Help Test support stuff
 *
 * @author  juhrik@netbeans.org
 */
public class JavaHelpDialogStableTest extends JellyTestCase {

    private HelpOperator helpWindow;

    /** Creates a new instance of JavaHelpDialogTest */
    public JavaHelpDialogStableTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(JavaHelpDialogTest.class)
                .addTest("testHelpF1")
                .addTest("testHelpFromMenu")
                .addTest("testHelpByButtonNonModal")
                .addTest("testHelpByButtonModal")
                .addTest("testContextualSearch")
                .addTest("testHelpByButtonNestedModal")
                .enableModules(".*")
                .clusters(".*") );
    }

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
        closeAllModal();

        if (helpWindow != null && helpWindow.isVisible()) {
            helpWindow.close();
        }

        helpWindow = null;
    }

    
    /** Test could be executed internaly in Forte without XTest
     * @param args arguments from command line
     */
//    public static void main(String[] args) {
//        junit.textui.TestRunner.run(suite());
//    }
}
