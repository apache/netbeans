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

package org.netbeans.spi.palette;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.util.Lookup;

/**
 *
 * @author Stanislav Aubrecht
 */
public class ModelTest extends AbstractPaletteTestHid {

    public ModelTest(String testName) {
        super(testName);
    }

    /**
     * Test of getName method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetName() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        assertEquals( getRootFolderName(), model.getName() );
    }

    /**
     * Test of getCategories method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetCategories() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        assertEquals( categoryNames.length, categories.length );
        for( int i=0; i<categories.length; i++ ) {
            assertEquals( categoryNames[i], categories[i].getName() );
        }
    }

    /**
     * Test of getActions method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetActions() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();
        Action[] modelActions = model.getActions();
        Action[] rootActions = actions.getCustomPaletteActions();
        for( int i=0; i<rootActions.length; i++ ) {
            if( null == rootActions[i] )
                continue;
            boolean found = false;
            for( int j=0; j<modelActions.length; j++ ) {
                if( null == modelActions[j] )
                    continue;
                if( modelActions[j].equals( rootActions[i] ) ) {
                    found = true;
                    break;
                }
            }
            assertTrue( "Action " + rootActions[i].getValue( Action.NAME ) + " not found in palette actions.", found );
        }
    }

    /**
     * Test of getSelectedItem method, of class org.netbeans.modules.palette.Model.
     */
    public void testSelectedItemAndCategory() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();

        assertNull( "No item is selected by default", model.getSelectedItem() );
        assertNull( "No category is selected by default", model.getSelectedCategory() );
        
        Category[] categories = model.getCategories();
        Category catToSelect = categories[3];
        Item itemToSelect = catToSelect.getItems()[4];
        
        model.setSelectedItem( catToSelect.getLookup(), itemToSelect.getLookup() );
        
        assertEquals( catToSelect, model.getSelectedCategory() );
        assertEquals( itemToSelect, model.getSelectedItem() );
        
        model.clearSelection();
        
        assertNull( "No item is selected after clearSelection()", model.getSelectedItem() );
        assertNull( "No category is selected after clearSelection()", model.getSelectedCategory() );
    }

    /**
     * Test of getRoot method, of class org.netbeans.modules.palette.Model.
     */
    public void testGetRoot() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Lookup rootLookup = model.getRoot();
        
        DataFolder df = (DataFolder)rootLookup.lookup( DataFolder.class );
        assertNotNull( df );
        
        FileObject fo = df.getPrimaryFile();
        assertNotNull( fo );
        
        assertEquals( getRootFolderName(), fo.getName() );
    }

    /**
     * Test of moveCategory method, of class org.netbeans.modules.palette.Model.
     */
    public void testMoveCategoryBefore() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        Category source = categories[0];
        Category target = categories[categories.length-1];
        
        model.moveCategory( source, target, true );
        
        pc.refresh();
        
        Category[] movedCategories = model.getCategories();
        assertEquals( categories.length, movedCategories.length );
        assertEquals( target.getName(), movedCategories[movedCategories.length-1].getName() );
        assertEquals( source.getName(), movedCategories[movedCategories.length-1-1].getName() );
    }

    /**
     * Test of moveCategory method, of class org.netbeans.modules.palette.Model.
     */
    public void testMoveCategoryAfter() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        Category source = categories[0];
        Category target = categories[categories.length-1];
        
        model.moveCategory( source, target, false );
        
        pc.refresh();
        
        Category[] movedCategories = model.getCategories();
        assertEquals( categories.length, movedCategories.length );
        assertEquals( target.getName(), movedCategories[movedCategories.length-1-1].getName() );
        assertEquals( source.getName(), movedCategories[movedCategories.length-1].getName() );
    }

    /**
     * Test of moveCategory method, of class org.netbeans.modules.palette.Model.
     */
    public void testMoveCategorySamePosition() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        Category source = categories[0];
        Category target = categories[0];
        
        model.moveCategory( source, target, false );
        
        Category[] movedCategories = model.getCategories();
        assertEquals( categories.length, movedCategories.length );
        assertEquals( target, movedCategories[0] );
        assertEquals( source, movedCategories[0] );
    }

    /**
     * Test of getName method, of class org.netbeans.modules.palette.Model.
     */
    public void testCustomRefresh() throws IOException {
        CustomRefresh refresh = new CustomRefresh();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new CustomRefreshActions( refresh ) );
        pc.refresh();
        assertTrue( refresh.actionInvoked );
}
    
    private static class CustomRefreshActions extends DummyActions {
        private Action refresh;
        public CustomRefreshActions( Action refresh ) {
            this.refresh = refresh;
        }
    
        @Override
        public Action getRefreshAction() {
            return refresh;
        }
    }

    private static class CustomRefresh extends AbstractAction {
        private boolean actionInvoked = false;
        
        public CustomRefresh() {
        }
    
        public void actionPerformed(ActionEvent arg0) {
            actionInvoked = true;
        }
    }
}
