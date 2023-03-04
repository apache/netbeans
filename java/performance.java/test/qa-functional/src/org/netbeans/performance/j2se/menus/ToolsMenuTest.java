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
package org.netbeans.performance.j2se.menus;

import junit.framework.Test;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Performance test for tools menu invoked when a various node is selected.</p>
 * <p>
 * Each test method reads the label of tested menu. During @link prepare given
 * node is selected and menu is pushed using mouse. The menu is then closed
 * using escape key.
 *
 * @author Radim Kubacki, mmirilovic@netbeans.org
 */
public class ToolsMenuTest extends MainMenuTest {

    protected static Node dataObjectNode;

    /**
     * Creates a new instance of ToolsMenu
     *
     * @param testName test name
     */
    public ToolsMenuTest(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
    }

    /**
     * Creates a new instance of ToolsMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public ToolsMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(ToolsMenuTest.class, "testJavaToolsMenu", "testXmlToolsMenu", "testTxtToolsMenu")
                .suite();
    }

    public void testJavaToolsMenu() {
        testToolsMenu("Main.java");
    }

    public void testXmlToolsMenu() {
        testToolsMenu("xmlfile.xml");
    }

    public void testTxtToolsMenu() {
        testToolsMenu("textfile.txt");
    }

    @Override
    public void prepare() {
        if (dataObjectNode != null) {
            dataObjectNode.select();
        }
    }

    private void testToolsMenu(String file) {
        dataObjectNode = new Node(new SourcePackagesNode("PerformanceTestData"), "org.netbeans.test.performance|" + file);
        super.testMenu("org.netbeans.core.ui.resources.Bundle", "Menu/Tools");
    }
}
