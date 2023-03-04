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
package org.netbeans.modules.autoupdate.ui;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction;
import org.openide.awt.Actions;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class InitialTabTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(InitialTabTest.class);
    }

    public InitialTabTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    public void testInitialTab() throws Exception {
        Action action = Actions.forID("System", "org.netbeans.modules.autoupdate.ui.actions.PluginManagerAction");
        assertNotNull("Action found", action);
        action.actionPerformed(new ActionEvent(this, 100, "local"));
        
        assertEquals("local", PluginManagerAction.getPluginManagerUI().getSelectedTabName());
    }
    
}
