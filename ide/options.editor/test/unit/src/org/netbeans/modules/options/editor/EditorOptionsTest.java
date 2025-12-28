/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.options.editor;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.core.startup.Main;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.settings.storage.EditorTestLookup;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Lookup;

import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;


/**
 *
 * @author Jan Jancura
 */
public class EditorOptionsTest extends NbTestCase {
    
    protected @Override void setUp() throws Exception {
        super.setUp();
        
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getClassLoader().getResource("org/netbeans/core/resources/mf-layer.xml"),
                getClass().getClassLoader().getResource("org/netbeans/core/ui/resources/layer.xml"),
                getClass().getClassLoader().getResource("org/netbeans/modules/options/editor/mf-layer.xml"),
                getClass().getClassLoader().getResource("org/netbeans/modules/options/keymap/mf-layer.xml"),
                getClass().getClassLoader().getResource("org/netbeans/modules/options/editor/test-layer.xml"),
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader()
        );
                
        // This is here to initialize Nb URL factory (org.netbeans.core.startup),
        // which is needed by Nb EntityCatalog (org.netbeans.core).
        // Also see the test dependencies in project.xml
        Main.initializeURLFactory();
    }
    
    public EditorOptionsTest (String testName) {
        super (testName);
    }
    
    public void testPanelsRegistration () {
        // there are two panels registered from this module - Editor, Fonts & Colors
        // and two panels from core/options/keymap (General, Keymaps)
        assertEquals (4, getCategories ().size ());
    }
    
    public void testOptionsCategories () {
        for(OptionsCategory oc : getCategories()) {
            assertNotNull (oc + " no category name", oc.getCategoryName ());
            assertNotNull (oc + " no icon", oc.getIcon());
        }
    }
    
    public void testUpdateOk () {
        List<OptionsPanelController> controllers = new ArrayList<OptionsPanelController>();
        List<Lookup> lookups = new ArrayList<Lookup>();
        for(OptionsCategory oc : getCategories()) {
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup(lookups.toArray (new Lookup [0]));
        for(OptionsPanelController pc : controllers) {
            JComponent c = pc.getComponent (masterLookup);
            pc.update ();
            pc.applyChanges ();
        }
    }
    
    public void testUpdateCancel () {
        List<OptionsPanelController> controllers = new ArrayList<OptionsPanelController>();
        List<Lookup> lookups = new ArrayList<Lookup>();
        for(OptionsCategory oc : getCategories()) {
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[0]));
        for(OptionsPanelController pc : controllers) {
            JComponent c = pc.getComponent (masterLookup);
            pc.update ();
            pc.cancel ();
        }
    }
    
    public void testOk () {
        List<OptionsPanelController> controllers = new ArrayList<OptionsPanelController>();
        List<Lookup> lookups = new ArrayList<Lookup>();
        for(OptionsCategory oc : getCategories()) {
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[0]));
        for(OptionsPanelController pc : controllers) {
            JComponent c = pc.getComponent (masterLookup);
            pc.update();
            pc.applyChanges ();
        }
    }
    
    public void testCancel () {
        
        // 1) load PanelControllers and init master lookup
        List<OptionsPanelController> controllers = new ArrayList<OptionsPanelController>();
        List<Lookup> lookups = new ArrayList<Lookup>();
        for(OptionsCategory oc : getCategories()) {
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[0]));
        
        // 2) create panels & call cancel on all PanelControllers
        for(OptionsPanelController pc : controllers) {
            JComponent c = pc.getComponent (masterLookup);
            pc.update();
            pc.cancel ();
        }
    }
    
    public void testChangedAndValid () {
        
        // 1) load PanelControllers and init master lookup
        List<OptionsPanelController> controllers = new ArrayList<OptionsPanelController>();
        List<Lookup> lookups = new ArrayList<Lookup>();
        for(OptionsCategory oc : getCategories()) {
            OptionsPanelController pc = oc.create ();
            controllers.add (pc);
            lookups.add (pc.getLookup ());
        }
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[0]));
        
        // 2) create panels & call cancel on all PanelControllers
        for(OptionsPanelController pc : controllers) {
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            JComponent c = pc.getComponent (masterLookup);
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.cancel ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
            pc.applyChanges ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
        }
        
        for(OptionsPanelController pc : controllers) {
            JComponent c = pc.getComponent (masterLookup);
            pc.update ();
            assertFalse ("isChanged should be false if there is no change! (controller = " + pc + ")", pc.isChanged ());
            assertTrue ("isvalid should be true if there is no change! (controller = " + pc + ")", pc.isValid ());
        }
    }

    private Collection<? extends OptionsCategory> getCategories () {
//        FileObject fo = FileUtil.getConfigFile ("OptionsDialog");
//        Lookup lookup = new FolderLookup (DataFolder.findFolder (fo)).
//            getLookup ();
//        return new ArrayList (lookup.lookup (
//            new Lookup.Template (OptionsCategory.class)
//        ).allInstances ());
        return Lookups.forPath("OptionsDialog").lookupAll(OptionsCategory.class);
    }
}
