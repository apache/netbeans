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


import java.awt.GraphicsEnvironment;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;

/** 
 * Test Mode activation behavior.
 * 
 * @author Marek Slama
 * 
 */
public class ModeActivationTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(ModeActivationTest.class);
    }

    public ModeActivationTest (String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    /**
     * Test basic behavior when Mode is activated. TC is docked into Mode, opened, activated,
     * closed. During this activation state of Mode is tested.
     */
    public void testActivate () throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        
        PersistenceHandler.getDefault().load();
        
        //This must be unit test as we need minimum winsys config
        //if default minimum winsys config is changed this test must be changed too.
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        Mode activeMode = wmi.getActiveMode();
        assertNull("No mode is activated ie. active mode must be null",activeMode);
        
        //Mode cannot be activated when it is empty
        Mode editor = wmi.getDefaultEditorMode();
        wmi.setActiveMode((ModeImpl) editor);
        activeMode = wmi.getActiveMode();
        assertNull("Ignore mode activation when mode is empty",activeMode);
        
        //Editor mode must be empty
        TopComponent [] tcs = editor.getTopComponents();
        assertEquals("Mode editor must be empty",tcs.length,0);

        //Dock TC into mode
        TopComponent tc = new TopComponent();
        
        //As tc is not yet docked into any mode this must return null
        Mode m = wmi.findMode(tc);
        assertNull("No mode for TC",m);
        
        editor.dockInto(tc);
        //Editor mode must contain one TC
        tcs = editor.getTopComponents();
        assertEquals("Mode editor must contain one TC", 1, tcs.length);
        
        //Mode cannot be activated when it does not contain opened TC
        wmi.setActiveMode((ModeImpl) editor);
        activeMode = wmi.getActiveMode();
        assertNull("Mode cannot be activated when it does not contain opened TC",activeMode);
        
        m = wmi.findMode(tc);
        assertEquals("Mode editor must be found for TC", editor, m);
        
        //TC is closed
        assertFalse("TC is closed",tc.isOpened());
        
        tc.open();
        //TC is opened
        assertTrue("TC is opened",tc.isOpened());
        tc.requestActive();
        
        //Editor mode is now activated
        activeMode = wmi.getActiveMode();
        assertEquals("Editor mode is now activated",editor,activeMode);
        
        //Check active tc
        TopComponent activeTC = wmi.getRegistry().getActivated();
        assertEquals("TC is now active",tc,activeTC);
        
        tc.close();
        //TC is closed
        assertFalse("TC is closed",tc.isOpened());
        
        //No mode is now activated
        activeMode = wmi.getActiveMode();
        assertNull("No mode is activated ie. active mode must be null", activeMode);
    }
    
}
