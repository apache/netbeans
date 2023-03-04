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
package org.netbeans.performance.j2ee.dialogs;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.performance.j2ee.setup.J2EEBaseSetup;

/**
 * Test of Add Server Instance dialog
 *
 * @author cyhelsky@netbeans.org
 */
public class AddServerInstanceDialogTest extends PerformanceTestCase {

    private String MENU, TITLE;
    private Node thenode;

    /**
     * Creates a new instance of AddServerInstanceDialog
     *
     * @param testName
     */
    public AddServerInstanceDialogTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }

    /**
     * Creates a new instance of AddServerInstanceDialog
     *
     * @param testName
     * @param performanceDataName
     */
    public AddServerInstanceDialogTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EEBaseSetup.class).addTest(AddServerInstanceDialogTest.class).suite();
    }

    public void testAddServerInstanceDialog() {
        doMeasurement();
    }

    @Override
    public void initialize() {
        MENU = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.actions.Bundle", "LBL_Add_Server_Instance"); //"Add Server..."
        TITLE = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.wizard.Bundle", "LBL_ASIW_Title"); //"Add Server Instance"

        String path = Bundle.getStringTrimmed("org.netbeans.modules.server.ui.manager.Bundle", "ACSN_ServerList"); //"Servers"
        JMenuBarOperator jmbo = new JMenuBarOperator(MainWindowOperator.getDefault().getJMenuBar());
        jmbo.pushMenu("window"); //NOI18N
        jmbo.closeSubmenus();
        jmbo.pushMenuNoBlock("window|&Services"); //NOI18N
        thenode = new Node(RuntimeTabOperator.invoke().getRootNode(), path);
        thenode.select();
    }

    public void prepare() {
    }

    public ComponentOperator open() {
        thenode.callPopup().pushMenu(MENU);
        return new NbDialogOperator(TITLE);
    }
}
