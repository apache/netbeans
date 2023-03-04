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
