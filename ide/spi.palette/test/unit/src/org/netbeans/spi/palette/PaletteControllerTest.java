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

package org.netbeans.spi.palette;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import junit.framework.TestCase;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.Utils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Stanislav Aubrecht
 */
public class PaletteControllerTest extends TestCase {

    private PaletteController controller;
    private Node rootNode;
    private DummyActions actions;
    private Model model;
    private Settings settings;
    
    public PaletteControllerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        actions = new DummyActions();
        rootNode = DummyPalette.createPaletteRoot();
        controller = PaletteFactory.createPalette( rootNode, actions );
        model = controller.getModel();
        settings = controller.getSettings();
    }

    /**
     * Test of addPropertyChangeListener method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testAddPropertyChangeListener() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        MyPropertyChangeListener myListener = new MyPropertyChangeListener();
        controller.addPropertyChangeListener( myListener );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( PaletteController.PROP_SELECTED_ITEM, myListener.getPropertyName() );
        assertEquals( item.getLookup(), myListener.getValue() );
        
        myListener.clear();
        model.clearSelection();

        assertEquals( PaletteController.PROP_SELECTED_ITEM, myListener.getPropertyName() );
        assertEquals( Lookup.EMPTY, myListener.getValue() );
    }

    /**
     * Test of removePropertyChangeListener method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testRemovePropertyChangeListener() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        MyPropertyChangeListener myListener = new MyPropertyChangeListener();
        controller.addPropertyChangeListener( myListener );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( PaletteController.PROP_SELECTED_ITEM, myListener.getPropertyName() );
        assertEquals( item.getLookup(), myListener.getValue() );
        
        controller.removePropertyChangeListener( myListener );
        myListener.clear();
        model.clearSelection();

        assertEquals( null, myListener.getPropertyName() );
        assertEquals( null, myListener.getValue() );
    }

    /**
     * Test of getSelectedItem method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testGetSelectedItem() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( item.getLookup(), controller.getSelectedItem() );
        
        cat = model.getCategories()[3];
        item = cat.getItems()[5];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );

        assertEquals( item.getLookup(), controller.getSelectedItem() );

        model.clearSelection();

        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
    }

    /**
     * Test of getSelectedCategory method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testGetSelectedCategory() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( cat.getLookup(), controller.getSelectedCategory() );
        
        cat = model.getCategories()[0];
        item = cat.getItems()[5];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );

        assertEquals( cat.getLookup(), controller.getSelectedCategory() );

        cat = model.getCategories()[4];
        item = cat.getItems()[6];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );

        assertEquals( cat.getLookup(), controller.getSelectedCategory() );

        model.clearSelection();

        assertEquals( Lookup.EMPTY, controller.getSelectedCategory() );
    }

    /**
     * Test of clearSelection method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testClearSelection() {
        //make sure nothing is selected by default
        model.clearSelection();
        
        assertEquals( Lookup.EMPTY, controller.getSelectedItem() );
        
        Category cat = model.getCategories()[0];
        Item item = cat.getItems()[0];
        model.setSelectedItem( cat.getLookup(), item.getLookup() );
        
        assertEquals( cat.getLookup(), controller.getSelectedCategory() );
        
        controller.clearSelection();
        assertEquals( Lookup.EMPTY, controller.getSelectedCategory() );
        
        controller.clearSelection();
        assertEquals( Lookup.EMPTY, controller.getSelectedCategory() );
    }

    /**
     * Test of resetPalette method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testCustomResetPalette() {
        MyActions actions = new MyActions();
        PaletteController myController = PaletteFactory.createPalette( DummyPalette.createPaletteRoot(), actions, null, null );
        
        Utils.resetPalette( myController, settings );
        assertTrue( actions.customResetInvoked );
    }

    /**
     * Test of setPaletteFilter method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testRefresh() throws IOException {
        MyPaletteFilter filter = new MyPaletteFilter( false );
        
        PaletteController myController = PaletteFactory.createPalette( DummyPalette.createPaletteRoot(), new DummyActions(), filter, null );
        
        Model myModel = myController.getModel();
        
        
        Category[] categories = myModel.getCategories();
        assertEquals( 9, categories.length );
        for( int i=0; i<categories.length; i++ ) {
            assertEquals( 9, categories[i].getItems().length );
        }
        
        filter.setEnabled( true );
        myController.refresh();
        
        categories = myModel.getCategories();
        for( int i=0; i<categories.length; i++ ) {
            //System.out.println( categories[i].getName() );
            assertTrue( filter.isValidName( categories[i].getName() ) );
            
            Item[] items = categories[i].getItems();
            for( int j=0; j<items.length; j++ ) {
                //System.out.println( items[j].getName() );
                assertTrue( filter.isValidName( items[j].getName() ) );
            }
        }
        
        filter.setEnabled( false );
        myController.refresh();
        
        categories = myModel.getCategories();
        assertEquals( 9, categories.length );
        for( int i=0; i<categories.length; i++ ) {
            assertEquals( 9, categories[i].getItems().length );
        }
    }

    /**
     * Test of showCustomizer method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testShowCustomizer() {
        ProxyModel myModel = new ProxyModel( model );
        controller.setModel( myModel );
        
        controller.showCustomizer();
        
        assertTrue( myModel.showCustomizerCalled );
    }

    /**
     * Test of getRoot method, of class org.netbeans.modules.palette.api.PaletteController.
     */
    public void testGetRoot() {
        assertEquals( rootNode.getName(), controller.getRoot().lookup( Node.class ).getName() );
    }

    private static class MyPropertyChangeListener implements PropertyChangeListener {
        private String propertyName;
        private Object newValue;
        
        public void propertyChange( PropertyChangeEvent evt ) {
            propertyName = evt.getPropertyName();
            newValue = evt.getNewValue();
        }
        
        public String getPropertyName() {
            return propertyName;
        }
        
        public Object getValue() {
            return newValue;
        }
        
        public void clear() {
            propertyName = null;
            newValue = null;
        }
    }
    
    private static class MyPaletteFilter extends PaletteFilter {
        
        private boolean isEnabled;
        
        public MyPaletteFilter( boolean enabled ) {
            this.isEnabled = enabled;
        }
        
        public boolean isValidItem(Lookup lkp) {
            if( !isEnabled )
                return true;
            
            Node node = (Node)lkp.lookup( Node.class );
            
            return nodeNameEndsWith1or2or3( node );
        }

        public boolean isValidCategory(Lookup lkp) {
            if( !isEnabled )
                return true;
            
            Node node = (Node)lkp.lookup( Node.class );
            
            return nodeNameEndsWith1or2or3( node );
        }
        
        private boolean nodeNameEndsWith1or2or3( Node node ) {
            if( null == node )
                return false;
            
            return isValidName( node.getName() );
        }
        
        public boolean isValidName( String name ) {
            if( null == name )
                return false;
            
            return name.endsWith( "1" ) || name.endsWith( "2" ) || name.endsWith( "3" );
        }
        
        public void setEnabled( boolean enable ) {
            this.isEnabled = enable;
        }
    }
    
    private static class MyActions extends PaletteActions {
        
        boolean customResetInvoked = false;

        public Action[] getImportActions() {
            return null;
        }

        public Action[] getCustomPaletteActions() {
            return null;
        }

        public Action[] getCustomCategoryActions(Lookup category) {
            return null;
        }

        public Action[] getCustomItemActions(Lookup item) {
            return null;
        }

        public Action getPreferredAction(Lookup item) {
            return null;
        }

        @Override
        public Action getResetAction() {
            return new AbstractAction() {
                public void actionPerformed(ActionEvent arg0) {
                    customResetInvoked = true;
                }
            };
        }
    }
}
