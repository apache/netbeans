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
