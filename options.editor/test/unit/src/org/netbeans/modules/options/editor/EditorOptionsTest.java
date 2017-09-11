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
        // and one panel from core/options/keymap
        assertEquals (3, getCategories ().size ());
    }
    
    public void testOptionsCategories () {
        for(OptionsCategory oc : getCategories()) {
            assertNotNull (oc.getCategoryName ());
            assertNotNull (oc.getIcon());
            assertNotNull (oc.getTitle ());
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
        Lookup masterLookup = new ProxyLookup(lookups.toArray (new Lookup [lookups.size ()]));
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
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
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
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
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
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
        
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
        Lookup masterLookup = new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
        
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
