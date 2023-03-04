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
import org.netbeans.core.windows.persistence.PersistenceManager;
import org.netbeans.junit.*;
import org.openide.util.Lookup;

import org.openide.windows.*;


/**
 * Tests correct tracking of editor/non-editor windows.
 * 
 * @author S. Aubrecht
 */
public class RoleTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(RoleTest.class);
    }

    public RoleTest (String name) {
        super (name);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }
     
    public void testDefault() throws Exception {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        assertNull( wm.getRole() );
    }
    
    public void testSwitchRole() throws Exception {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();

        assertNull( wm.getRole() );

        assertTrue( wm.switchRole( "_unit_test_", false ) );

        assertEquals( "_unit_test_", wm.getRole() );
        assertEquals( "_unit_test_", PersistenceManager.getDefault().getRole() );
        assertEquals( "Windows2Local-_unit_test_", PersistenceManager.getDefault().getRootLocalFolder().getPath() );

        assertTrue( wm.switchRole( null, false ) );

        assertNull( wm.getRole() );
        assertNull( PersistenceManager.getDefault().getRole() );
        assertEquals( "Windows2Local", PersistenceManager.getDefault().getRootLocalFolder().getPath() );
    }
    
    public void testNotifyClosed() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();

        final int[] counter = new int[1];

        TopComponent tc = new TopComponent() {

            @Override
            protected void componentClosed() {
                counter[0]++;
            }

        };

        tc.open();

        assertEquals( 0, counter[0] );

        assertTrue( wm.switchRole( "_unit_test_", false ) );

        assertEquals( "_unit_test_", wm.getRole() );

        assertEquals( 1, counter[0] );
        assertFalse( tc.isOpened() );

        assertTrue( wm.switchRole( null, false ) );
        assertNull( wm.getRole() );
    }
    
    @RandomlyFails // NB-Core-Build Unstable #9938, #9950, other builds passed
    public void testKeepDocuments() {
        WindowManagerImpl wm = WindowManagerImpl.getInstance();
        
        WindowManagerImpl.getInstance().resetModel();
        PersistenceManager.getDefault().reset(); //keep mappings to TopComponents created so far
        PersistenceHandler.getDefault().clear();
                
        assertNull( wm.getRole() );
        
        for( TopComponent tc : TopComponent.getRegistry().getOpened() ) {
            tc.close();
        }

        TopComponent tc = new TopComponent();

        tc.open();
        
        assertTrue( wm.isEditorTopComponent( tc ) );

        assertTrue( wm.switchRole( "_unit_test_", true ) );

        assertEquals( "_unit_test_", wm.getRole() );

        assertTrue( tc.isOpened() );

        assertTrue( wm.switchRole( null, true ) );
        assertNull( wm.getRole() );
        
        assertTrue( tc.isOpened() );
    }
}
