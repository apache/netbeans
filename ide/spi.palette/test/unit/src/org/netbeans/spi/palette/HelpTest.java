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

import java.beans.BeanInfo;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.netbeans.modules.palette.Settings;
import org.netbeans.modules.palette.ui.PalettePanel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/**
 *
 * @author S. Aubrecht
 */
public class HelpTest extends AbstractPaletteTestHid {

    public HelpTest(String testName) {
        super(testName);
    }

    public void testItemHelp() throws Exception {
        FileObject item1 = getItemFile( categoryNames[0], itemNames[0][0] );
        FileObject item2 = getItemFile( categoryNames[0], itemNames[0][1] );
        
        item1.setAttribute( PaletteController.ATTR_HELP_ID, "DummyHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        Item[] items = categories[0].getItems();
        
        Node node1 = items[0].getLookup().lookup( Node.class );
        Node node2 = items[1].getLookup().lookup( Node.class );
        
        HelpCtx help1 = node1.getHelpCtx();
        HelpCtx help2 = node2.getHelpCtx();

        assertEquals( "Custom help", "DummyHelpId", help1.getHelpID() );
        assertEquals( "Default help", HelpCtx.DEFAULT_HELP, help2 );
    }

    public void testCategoryHelp() throws Exception {
        FileObject cat1 = getCategoryFile( categoryNames[0] );
        FileObject cat2 = getCategoryFile( categoryNames[1] );
        
        cat1.setAttribute( PaletteController.ATTR_HELP_ID, "DummyHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        Node node1 = categories[0].getLookup().lookup( Node.class );
        Node node2 = categories[1].getLookup().lookup( Node.class );
        
        HelpCtx help1 = node1.getHelpCtx();
        HelpCtx help2 = node2.getHelpCtx();

        assertEquals( "Custom help", "DummyHelpId", help1.getHelpID() );
        assertNull( "Default help", help2 );
    }

    public void testRootHelpCustom() throws Exception {
        paletteRootFolder.setAttribute( PaletteController.ATTR_HELP_ID, "DummyHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Node node = model.getRoot().lookup( Node.class );
        
        HelpCtx help = node.getHelpCtx();

        assertEquals( "Custom help", "DummyHelpId", help.getHelpID() );
    }

    public void testRootHelpDefault() throws Exception {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Node node = model.getRoot().lookup( Node.class );
        
        HelpCtx help = node.getHelpCtx();

        assertNull( "Custom help", help );
    }

    public void testPalettePanelCustom() throws Exception {
        FileObject item1 = getItemFile( categoryNames[0], itemNames[0][0] );
        FileObject cat2 = getCategoryFile( categoryNames[1] );
        
        item1.setAttribute( PaletteController.ATTR_HELP_ID, "DummyItemHelpId" );
        cat2.setAttribute( PaletteController.ATTR_HELP_ID, "DummyCategoryHelpId" );
        paletteRootFolder.setAttribute( PaletteController.ATTR_HELP_ID, "DummyRootHelpId" );
        
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        PalettePanel panel = PalettePanel.getDefault();
        panel.setContent( pc, model, pc.getSettings() );
        panel.refresh();
        
        HelpCtx help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "No category, no item selected", "DummyRootHelpId", help.getHelpID() );

        //UI is not fully initialized at this point
//        pc.setSelectedItem( model.getCategories()[1].getLookup(), model.getCategories()[1].getItems()[0].getLookup() );
//        help = panel.getHelpCtx();
//        assertNotNull( help );
//        assertEquals( "Category selected, selected item has no custom help", "DummyCategoryHelpId", help.getHelpID() );
        
        pc.setSelectedItem( model.getCategories()[0].getLookup(), model.getCategories()[0].getItems()[0].getLookup() );
        help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "Category selected, selected item has custom help", "DummyItemHelpId", help.getHelpID() );
    }

    public void testPalettePanelDefault() throws Exception {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        PalettePanel panel = PalettePanel.getDefault();
        panel.setContent( pc, model, pc.getSettings() );
        
        HelpCtx help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "No custom help defined", "CommonPalette", help.getHelpID() );
        
        pc.setSelectedItem( model.getCategories()[0].getLookup(), model.getCategories()[0].getItems()[0].getLookup() );
        help = panel.getHelpCtx();
        assertNotNull( help );
        assertEquals( "No custom help defined", "CommonPalette", help.getHelpID() );
    }
}
