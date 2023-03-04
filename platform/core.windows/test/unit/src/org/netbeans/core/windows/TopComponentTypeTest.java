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

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.junit.*;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

import org.openide.windows.*;


/** 
 * Ensure that TopComponent type - "editor" / "view" - is interpreted correctly.
 * 
 * @author S. Aubrecht
 */
public class TopComponentTypeTest extends NbTestCase {

    public TopComponentTypeTest (String name) {
        super (name);
    }

    protected boolean runInEQ () {
        return true;
    }
     
    public void testIsEditorTopComponent () throws Exception {
        TopComponent tc = new TopComponent ();
        Mode mode = WindowManagerImpl.getInstance().createMode( "editorMode", Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        mode.dockInto( tc );
        
        assertTrue( WindowManagerImpl.getInstance().isEditorTopComponent( tc ) );
        assertTrue( WindowManagerImpl.getInstance().isEditorMode( mode ) );
    }
    
    public void testIsViewTopComponent () throws Exception {
        TopComponent tc = new TopComponent ();
        Mode mode = WindowManagerImpl.getInstance().createMode( "viewMode", Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        mode.dockInto( tc );
        
        assertFalse( WindowManagerImpl.getInstance().isEditorTopComponent( tc ) );
        assertFalse( WindowManagerImpl.getInstance().isEditorMode( mode ) );
    }
    
    public void testUnknownTopComponent () throws Exception {
        TopComponent tc = new TopComponent ();
        //no mode defined for the topcomponent
        
        assertFalse( WindowManagerImpl.getInstance().isEditorTopComponent( tc ) );
    }
     
    public void testIsOpenedEditorTopComponent() throws Exception {
        final TopComponent editorTc = new TopComponent ();
        final TopComponent viewTc = new TopComponent ();
        Mode editorMode = WindowManagerImpl.getInstance().createMode( "editorMode", Constants.MODE_KIND_EDITOR, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        Mode viewMode = WindowManagerImpl.getInstance().createMode( "viewMode", Constants.MODE_KIND_VIEW, Constants.MODE_STATE_JOINED, false, new SplitConstraint[0] );
        Logger logger = Logger.getLogger( WindowManagerImpl.class.getName() );
        final MyHandler handler = new MyHandler();
        logger.addHandler( handler );
        
        assertTrue( WindowManagerImpl.getInstance().isEditorMode( editorMode ) );
        assertFalse( WindowManagerImpl.getInstance().isEditorMode( viewMode ) );
        editorMode.dockInto( editorTc );
        viewMode.dockInto( viewTc );
        
        final boolean[] res = new boolean[2]; 
        Task t = RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                assertFalse( SwingUtilities.isEventDispatchThread() );
                res[0] = WindowManagerImpl.getInstance().isOpenedEditorTopComponent( editorTc );
                res[1] = WindowManagerImpl.getInstance().isOpenedEditorTopComponent( viewTc );
            }
        });
        t.waitFinished();
        assertNull( handler.latestLogRecord );
        
        assertFalse( res[0] );
        assertFalse( res[1] );
        
        editorTc.open();
        viewTc.open();
        assertTrue( editorTc.isOpened() );
        assertTrue( viewTc.isOpened() );
        
        t = RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                assertFalse( SwingUtilities.isEventDispatchThread() );
                res[0] = WindowManagerImpl.getInstance().isOpenedEditorTopComponent( editorTc );
                res[1] = WindowManagerImpl.getInstance().isOpenedEditorTopComponent( viewTc );
            }
        });
        t.waitFinished();
        assertNull( handler.latestLogRecord );
        assertTrue( res[0] );
        assertFalse( res[1] );
    }
    
    public void testNull() {
        assertFalse( WindowManagerImpl.getInstance().isEditorMode( null ) );
        assertFalse( WindowManagerImpl.getInstance().isEditorTopComponent( null ) );
        assertFalse( WindowManagerImpl.getInstance().isOpenedEditorTopComponent( null ) );
    }

    private class MyHandler extends Handler {

        private LogRecord latestLogRecord;
        
        public void publish(LogRecord rec) {
            this.latestLogRecord = rec;
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }
        
    }
}

