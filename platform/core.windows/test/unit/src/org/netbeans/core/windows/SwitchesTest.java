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

import java.util.Locale;

import org.netbeans.junit.NbTestCase;

/** 
 * Test Mode activation behavior.
 * 
 * @author Marek Slama
 * 
 */
public class SwitchesTest extends NbTestCase {

    public SwitchesTest (String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }

    private Locale defLocale;
    @Override
    protected void setUp() throws Exception {
//        super.setUp();
//        defLocale = Locale.getDefault();
//        Locale.setDefault(new Locale("te_ST"));
    }
    
    @Override
    protected void tearDown() {
//        Locale.setDefault(defLocale);
    }
    
    public void testDndDisabled() throws Exception {
//        assertFalse(Switches.isTopComponentDragAndDropEnabled());
//        assertFalse(WindowDnDManager.isDnDEnabled());
    }

    public void testUndockingDisabled() throws Exception {
//        assertFalse(Switches.isTopComponentUndockingEnabled());
//        assertFalse( new UndockWindowAction( new TopComponent()).isEnabled() );
    }

    public void testSlidingDisabled() throws Exception {
//        assertFalse(Switches.isTopComponentSlidingEnabled());
//        assertFalse( new TabbedAdapter(Constants.MODE_KIND_VIEW).getContainerWinsysInfo().isTopComponentSlidingEnabled() );
    }

    public void testMaximizationDisabled() throws Exception {
//        assertFalse(Switches.isTopComponentMaximizationEnabled());
//        assertFalse( new MaximizeWindowAction(new TopComponent()).isEnabled() );
    }

    public void testViewClosingDisabled() throws Exception {
//        assertFalse(Switches.isViewTopComponentClosingEnabled());
//        assertFalse( new TabbedAdapter(Constants.MODE_KIND_VIEW).getContainerWinsysInfo().isTopComponentClosingEnabled() );
    }

    public void testEditorClosingDisabled() throws Exception {
//        assertFalse(Switches.isEditorTopComponentClosingEnabled());
//        assertFalse( new TabbedAdapter(Constants.MODE_KIND_EDITOR).getContainerWinsysInfo().isTopComponentClosingEnabled() );
    }
}

