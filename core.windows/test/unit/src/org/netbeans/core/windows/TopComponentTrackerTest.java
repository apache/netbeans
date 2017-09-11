/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
