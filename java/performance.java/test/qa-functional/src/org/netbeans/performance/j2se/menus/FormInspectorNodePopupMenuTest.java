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
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NavigatorOperator;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.utilities.ValidatePopupMenuOnNodes;
import org.netbeans.performance.j2se.setup.J2SESetup;

/**
 * Test of popup menu on node in Component Inspector.
 *
 * @author juhrik@netbeans.org, mmirilovic@netbeans.org
 */
public class FormInspectorNodePopupMenuTest extends ValidatePopupMenuOnNodes {

    /**
     * Creates a new instance of FormInspectorNodePopupMenu
     *
     * @param testName test name
     */
    public FormInspectorNodePopupMenuTest(String testName) {
        super(testName);
    }

    /**
     * Creates a new instance of FormInspectorNodePopupMenu
     *
     * @param testName test name
     * @param performanceDataName data name
     */
    public FormInspectorNodePopupMenuTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
    }

    public static Test suite() {
        return emptyConfiguration()
                .addTest(J2SESetup.class, "testCloseMemoryToolbar", "testOpenDataProject")
                .addTest(FormInspectorNodePopupMenuTest.class)
                .suite();
    }

    public void testFormNodePopupMenuInspector() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        CommonUtilities.openSmallFormFile();
        NavigatorOperator.invokeNavigator();
    }

    @Override
    public void shutdown() {
        EditorOperator.closeDiscardAll();
    }

    @Override
    public void prepare() {
        String path = "[JFrame]";
        dataObjectNode = new Node(new ComponentInspectorOperator().treeComponents(), path);
        super.prepare();
    }

    @Override
    public void close() {
        if (dataObjectNode != null) {
            super.close();
        }
    }
}
