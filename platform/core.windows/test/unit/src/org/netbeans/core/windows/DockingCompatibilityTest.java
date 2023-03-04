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

package org.netbeans.core.windows;

import org.netbeans.junit.*;

import org.openide.windows.*;


/** Test to guarantee that the compatibility for docking operations is
 * preserved for components written against release 3.5 and later and
 * that such components can be docked.
 *
 * @author Jaroslav Tulach
 */
public class DockingCompatibilityTest extends NbTestCase {
    private Mode mode;

    public DockingCompatibilityTest (String name) {
        super (name);
    }

    @Override
    protected void setUp() throws Exception {
        mode = WindowManager.getDefault().getCurrentWorkspace().createMode("OwnMode", "displayName", null);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testSimplyOpenedComponentCanBeDockedWhereeverItWants () throws Exception {
        TopComponent tc = new TopComponent ();
        tc.open ();
        
        assertCanBeDocked (tc, Boolean.TRUE);
    }
    
    public void testComponentPutIntoOwnModeCanBeDockedAsWell () {
        TopComponent tc = new TopComponent ();
        mode.dockInto (tc);
        tc.open ();
        
        assertCanBeDocked (tc, Boolean.TRUE);
    }

    public void testComponentPlacedDirectlyIntoEditorModeHasToStayThere () {
        Mode editor = WindowManager.getDefault ().findMode ("editor");
        assertNotNull ("Shall not be null", editor);
        TopComponent tc = new TopComponent ();
        editor.dockInto (tc);
        assertCanBeDocked (tc, null);
    }
    
    
    private static void assertCanBeDocked (TopComponent tc, Boolean expectedValue) {
        assertEquals (
            expectedValue,  
            tc.getClientProperty (Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE)
        );
    }
}

