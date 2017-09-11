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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.actions;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.Action;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;
import org.openide.util.ContextAwareAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Tests SaveAsAction.
 * 
 * @author S. Aubrecht
 */
public class SaveAsActionTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(SaveAsActionTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    private TopComponent editorWithSaveAs;
    private TopComponent editorWithoutSaveAs;

    private TopComponent viewWithSaveAs;
    private TopComponent viewWithoutSaveAs;
    
    private Mode editorMode;
    
    public SaveAsActionTest(String name) {
        super(name);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        Lookup editorLkp = Lookups.fixed( new SaveAsCapable() {
            public void saveAs(FileObject folder, String name) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        Lookup viewLkp = Lookups.fixed( new SaveAsCapable() {
            public void saveAs(FileObject folder, String name) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        editorWithSaveAs = new TopComponent( editorLkp );
        editorWithSaveAs.setName("editorWithSaveAs");
        editorWithoutSaveAs = new TopComponent();
        editorWithoutSaveAs.setName("editorWithoutSaveAs");
        
        viewWithSaveAs = new TopComponent( viewLkp );
        viewWithSaveAs.setName("viewWithSaveAs");
        viewWithoutSaveAs = new TopComponent();
        viewWithoutSaveAs.setName("viewWithoutSaveAs");
        
        WindowManager wm = WindowManager.getDefault();
        
        editorMode = wm.getCurrentWorkspace().createMode("editor", null, null);
        Mode viewMode = wm.getCurrentWorkspace().createMode("view", null, null);
        
        editorMode.dockInto( editorWithSaveAs );
        editorMode.dockInto( editorWithoutSaveAs );
        
        viewMode.dockInto( viewWithSaveAs );
        viewMode.dockInto( viewWithoutSaveAs );

        editorWithSaveAs.open();
        editorWithoutSaveAs.open();
        viewWithSaveAs.open();
        viewWithoutSaveAs.open();
        
        assertTrue(Arrays.asList(wm.getOpenedTopComponents(editorMode)).contains(editorWithSaveAs));
        assertTrue(Arrays.asList(wm.getOpenedTopComponents(editorMode)).contains(editorWithoutSaveAs));
        assertTrue(Arrays.asList(wm.getOpenedTopComponents(viewMode)).contains(viewWithSaveAs));
        assertTrue(Arrays.asList(wm.getOpenedTopComponents(viewMode)).contains(viewWithoutSaveAs));
    }

    protected @Override void tearDown() throws Exception {
        editorWithSaveAs.close();
        editorWithoutSaveAs.close();
        viewWithSaveAs.close();
        viewWithoutSaveAs.close();
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testSaveAsActionDisabledForNonEditorWindows() throws Exception {
        ContextAwareAction action = SaveAsAction.create();
        
        viewWithoutSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == viewWithoutSaveAs );
        
        assertFalse( "action is disabled when SaveAsCapable is not present in active TC lookup", action.isEnabled() );
    }
    
    public void testSaveAsActionDisabledForViewsWithSaveAsCapable() throws Exception {
        ContextAwareAction action = SaveAsAction.create();
        
        viewWithSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == viewWithSaveAs );
        
        assertFalse( "action is disabled other window than editor is activated", action.isEnabled() );
    }
    
    public void testSaveAsActionEnabledForEditorsOnly() throws Exception {
        ContextAwareAction action = SaveAsAction.create();
        
        editorWithSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == editorWithSaveAs );
        
        assertTrue( "action is enabled for editor windows with SaveAsCapable in their Lookup", action.isEnabled() );
    }

    @RandomlyFails // NB-Core-Build #3579, #3580
    public void testSaveAsActionDoesNotRefreshWithoutListeners() throws Exception {
        SaveAsAction action = (SaveAsAction)SaveAsAction.create();
        
        PropertyChangeListener l = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
            }
        };
        
        action.addPropertyChangeListener( l );
        /* No apparent reason for this; isEnabled notes isDirty is true, and there is no active component:
        assertTrue( "action will not refresh its state when it has no registered listener", action.isEnabled() );
         */
        
        editorWithSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == editorWithSaveAs );
        
        assertTrue( "action will not refresh its state when it has no registered listener", action.isEnabled() );
        
        editorWithoutSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == editorWithoutSaveAs );
        assertFalse( "action will not refresh its state when it has no registered listener", action.isEnabled() );
        action.removePropertyChangeListener(l);
        
        editorWithSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == editorWithSaveAs );
        assertFalse( "action will not refresh its state when it has no registered listener", action._isEnabled() );
    }
    
    public void testSaveAsActionDisabledForEditorsWithoutSaveAsCapable() throws Exception {
        ContextAwareAction action = SaveAsAction.create();
        
        editorWithoutSaveAs.requestActive();
        assertTrue( TopComponent.getRegistry().getActivated() == editorWithoutSaveAs );
        assertFalse( "action is disabled for editor windows without SaveAsCapable in their Lookup", action.isEnabled() );
    }
    
    public void testActionStatusUpdatedOnLookupChange() throws Exception {
        final InstanceContent content = new InstanceContent();
        Lookup lkp = new AbstractLookup( content );
        final SaveAsCapable saveAsImpl = new SaveAsCapable() {
            public void saveAs(FileObject folder, String name) throws IOException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        
        TopComponent tc = new TopComponent( lkp );
        editorMode.dockInto( tc );
        tc.open();
        tc.requestActive();
        assertTrue(Arrays.asList(WindowManager.getDefault().getOpenedTopComponents(editorMode)).contains(tc));
        
        ContextAwareAction action = SaveAsAction.create();
        assertFalse( "action is disabled for editor windows without SaveAsCapable in their Lookup", action.isEnabled() );
        
        Action a = action.createContextAwareInstance( tc.getLookup() );
        
        content.add( saveAsImpl );
        assertTrue( "action is enabled for editor windows with SaveAsCapable in their Lookup", a.isEnabled() );
        content.remove( saveAsImpl );
        assertFalse( "action is disabled for editor windows without SaveAsCapable in their Lookup", a.isEnabled() );
    }
}
