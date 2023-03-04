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
package org.netbeans.performance.j2ee.menus;

import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.performance.j2ee.setup.J2EEBaseSetup;

/**
 * Test of popup menu on nodes in Runtime View
 *
 * @author juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class AppServerPopupMenuTest extends PerformanceTestCase {

    protected static Node glassFishNode;

    /**
     * Creates a new instance of AppServerPopupMenuTest
     *
     * @param testName
     */
    public AppServerPopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of AppServerPopupMenuTest
     *
     * @param testName
     * @param performanceDataName
     */
    public AppServerPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EEBaseSetup.class).addTest(AppServerPopupMenuTest.class).suite();
    }

    public void testAppServerPopupMenuRuntime() {
        glassFishNode = new Node(new RuntimeTabOperator().getRootNode(), "Servers|GlassFish");
        doMeasurement();
    }

    /**
     * Closes the popup by sending ESC key event.
     */
    @Override
    public void close() {
        //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        // Above sometimes fails in QUEUE mode waiting to menu become visible.
        // This pushes Escape on underlying JTree which should be always visible
        glassFishNode.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }

    @Override
    public void prepare() {
        glassFishNode.select();
    }

    @Override
    public ComponentOperator open() {
        return glassFishNode.callPopup();
    }
}
