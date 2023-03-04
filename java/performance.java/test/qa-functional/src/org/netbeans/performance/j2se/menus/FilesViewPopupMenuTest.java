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
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of popup menu on nodes in Files View.
 *
 * @author mmirilovic@netbeans.org
 */
public class FilesViewPopupMenuTest extends PerformanceTestCase {

    private static FilesTabOperator filesTab = null;
    private Node node;
    protected static Node dataObjectNode;

    /**
     * Creates a new instance of FilesViewPopupMenu
     *
     * @param testName test name
     */
    public FilesViewPopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of FilesViewPopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public FilesViewPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(FilesViewPopupMenuTest.class)
                .suite();
    }

    public void testProjectNodePopupMenuFiles() {
        node = getProjectNode();
        doMeasurement();
    }

    public void testPackagePopupMenuFiles() {
        node = new Node(getProjectNode(), "src|org|netbeans|test|performance");
        doMeasurement();
    }

    public void testbuildXmlFilePopupMenuFiles() {
        node = new Node(getProjectNode(), "build.xml");
        doMeasurement();
    }

    private Node getProjectNode() {
        if (filesTab == null) {
            filesTab = new FilesTabOperator();
        }

        return filesTab.getProjectNode("PerformanceTestData");
    }

    @Override
    public void prepare() {
        expectedTime = 500;
    }

    @Override
    public void close() {
        node.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    @Override
    public ComponentOperator open() {
        node.callPopup();
        return null;
    }
}
