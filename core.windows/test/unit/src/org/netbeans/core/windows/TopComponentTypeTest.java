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

