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


/**
 * Tests correct tracking of editor/non-editor windows.
 * 
 * @author S. Aubrecht
 */
public class TopComponentTrackerTest extends NbTestCase {

    public TopComponentTrackerTest (String name) {
        super (name);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }
     
    public void testViewTopComponent() throws Exception {
        Mode viewMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        Mode editorMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        
        TopComponent viewTC = new PersistentTC("tc1");
        
        TopComponentTracker tracker = TopComponentTracker.getDefault();
        
        assertFalse( tracker.isViewTopComponent( viewTC ) );
        assertTrue( tracker.isEditorTopComponent( viewTC ) );
        
        viewMode.dockInto( viewTC );
        
        assertFalse( tracker.isViewTopComponent( viewTC ) );
        assertTrue( tracker.isEditorTopComponent( viewTC ) );
        
        viewTC.open();
        
        assertTrue( tracker.isViewTopComponent( viewTC ) );
        assertFalse( tracker.isEditorTopComponent( viewTC ) );
        
        viewTC.close();
        editorMode.dockInto( viewTC );
        viewTC.open();
        
        assertTrue( tracker.isViewTopComponent( viewTC ) );
        assertFalse( tracker.isEditorTopComponent( viewTC ) );
    }
     
    public void testSlidingMode() throws Exception {
        Mode slidingMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_SLIDING, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        Mode editorMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        
        TopComponent viewTC = new PersistentTC("tc1");
        
        TopComponentTracker tracker = TopComponentTracker.getDefault();
        
        assertFalse( tracker.isViewTopComponent( viewTC ) );
        assertTrue( tracker.isEditorTopComponent( viewTC ) );
        
        slidingMode.dockInto( viewTC );
        
        assertFalse( tracker.isViewTopComponent( viewTC ) );
        assertTrue( tracker.isEditorTopComponent( viewTC ) );
        
        viewTC.open();
        
        assertTrue( tracker.isViewTopComponent( viewTC ) );
        assertFalse( tracker.isEditorTopComponent( viewTC ) );
        
        viewTC.close();
        editorMode.dockInto( viewTC );
        viewTC.open();
        
        assertTrue( tracker.isViewTopComponent( viewTC ) );
        assertFalse( tracker.isEditorTopComponent( viewTC ) );
    }
     
    public void testEditorTopComponent() throws Exception {
        Mode viewMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        Mode editorMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        
        TopComponentTracker tracker = TopComponentTracker.getDefault();
        
        TopComponent editorTC = new PersistentTC("tc2");
        editorMode.dockInto( editorTC );
        editorTC.open();
        
        assertFalse( tracker.isViewTopComponent( editorTC ) );
        assertTrue( tracker.isEditorTopComponent( editorTC ) );
        
        editorTC.close();
        viewMode.dockInto( editorTC );
        editorTC.open();
        
        assertFalse( tracker.isViewTopComponent( editorTC ) );
        assertTrue( tracker.isEditorTopComponent( editorTC ) );
    }
     
    public void testNonPersistentTopComponent() throws Exception {
        Mode viewMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        Mode editorMode = WindowManagerImpl.getInstance().createMode(null,
                Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, true, new SplitConstraint[0] );
        
        TopComponentTracker tracker = TopComponentTracker.getDefault();
        
        TopComponent nonPersitentTC = new NonPersistentTC();
        editorMode.dockInto( nonPersitentTC );
        nonPersitentTC.open();
        
        assertFalse( tracker.isViewTopComponent( nonPersitentTC ) );
        assertTrue( tracker.isEditorTopComponent( nonPersitentTC ) );
        
        nonPersitentTC.close();
        viewMode.dockInto( nonPersitentTC );
        nonPersitentTC.open();
        
        assertTrue( tracker.isViewTopComponent( nonPersitentTC ) );
        assertFalse( tracker.isEditorTopComponent( nonPersitentTC ) );
        
        TopComponent nonPersitentTC2 = new NonPersistentTC();
        viewMode.dockInto( nonPersitentTC2 );
        nonPersitentTC2.open();
        
        assertTrue( tracker.isViewTopComponent( nonPersitentTC2 ) );
        assertFalse( tracker.isEditorTopComponent( nonPersitentTC2 ) );
        
        nonPersitentTC2.close();
        editorMode.dockInto( nonPersitentTC2 );
        nonPersitentTC2.open();

        assertFalse( tracker.isViewTopComponent( nonPersitentTC2 ) );
        assertTrue( tracker.isEditorTopComponent( nonPersitentTC2 ) );
    }
    
    private static class PersistentTC extends TopComponent {
        private final String prefId;
        
        public PersistentTC( String id ) {
            this.prefId = id;
            putClientProperty( Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE, Boolean.TRUE );
        }
        
        @Override
        public int getPersistenceType() {
            return PERSISTENCE_ALWAYS;
        }

        @Override
        protected String preferredID() {
            return prefId;
        }
    }
    
    private static class NonPersistentTC extends TopComponent {
        
        public NonPersistentTC() {
            putClientProperty( Constants.TOPCOMPONENT_ALLOW_DOCK_ANYWHERE, Boolean.TRUE );
        }
        
        @Override
        public int getPersistenceType() {
            return PERSISTENCE_NEVER;
        }
    }
}
