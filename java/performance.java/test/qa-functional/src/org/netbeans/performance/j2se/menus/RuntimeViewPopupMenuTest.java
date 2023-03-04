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
import static org.netbeans.jellytools.JellyTestCase.emptyConfiguration;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.modules.performance.utilities.ValidatePopupMenuOnNodes;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of popup menu on Servers node in Services View
 *
 * @author juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class RuntimeViewPopupMenuTest extends ValidatePopupMenuOnNodes {

    private static RuntimeTabOperator runtimeTab;

    /**
     * Creates a new instance of RuntimeViewPopupMenu
     *
     * @param testName test name
     */
    public RuntimeViewPopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of RuntimeViewPopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public RuntimeViewPopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2SESetup.class, "testCloseMemoryToolbar").addTest(RuntimeViewPopupMenuTest.class).suite();
    }

    public void testServerRegistryPopupMenuRuntime() {
        dataObjectNode = new Node(RuntimeTabOperator.invoke().getRootNode(), "Servers");
        doMeasurement();
    }
}
