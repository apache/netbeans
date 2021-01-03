/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.python.platform;

import javax.swing.JList;
import javax.swing.ListModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.modules.python.api.PythonPlatform;

public class PythonPlatformManagerTest extends JellyTestCase {

    /**
     * Creates suite from particular test cases. You can define order of
     * testcases here.
     *
     * @return
     */
    public static Test suite() {
        Configuration testConfig = NbModuleSuite.createConfiguration(PythonPlatformManagerTest.class);
        testConfig = testConfig.addTest("testDefaultPlatform");
        testConfig = testConfig.clusters(".*").enableModules(".*");
        return testConfig.suite();
    }

    public PythonPlatformManagerTest(String testName) {
        super(testName);
    }

    /**
     * Called before every test case.
     */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
    }

    public void testDefaultPlatform() {
        MainWindowOperator.getDefault().menuBar().pushMenuNoBlock("Tools|Python Platforms");
        PythonPlatformOperator ppo = new PythonPlatformOperator();
        
        PythonPlatform platform = (PythonPlatform)ppo.platformList().getElementAt(0);
        assertTrue("Expected Python Platform to be first, but was: " + platform, platform.getName().startsWith("Jython "));
        JButtonOperator btClose = ppo.btClose();
        btClose.press();
    }
}
