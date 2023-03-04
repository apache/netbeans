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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.ActiveEditorDrop;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Libor Kotouc
 */
public class PaletteItemTest extends NbTestCase {
  
    private static final String PALETTE_ROOT = "FooPalette";
    private static final String ITEMS_FOLDER = PALETTE_ROOT + "/FooCategory";
    private static final String ITEM_FILE = "FooItem.xml";
    private static final String CLASS_NAME = "org.netbeans.spi.palette.FooItem";
    private static final String BODY = "<fooTag att=''></fooTag>";
    private static final String ICON16 = "org/netbeans/spi/palette/FooItem16.gif";
    private static final String ICON32 = "org/netbeans/spi/palette/FooItem32.gif";
    private static final String BUNDLE_NAME = "org.netbeans.spi.palette.Bundle";
    private static final String NAME_KEY = "NAME_foo-FooItem";
    private static final String TOOLTIP_KEY = "HINT_foo-FooItem";
    
    
    private FileObject itemsFolder;
    
    private Lookup selectedNode;
    
    public PaletteItemTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(new Class[] {RepositoryImpl.class});
        itemsFolder = createItemsFolder();
        assertNotNull(itemsFolder);
    }

    protected void tearDown() throws Exception {
        FileObject[] ch = itemsFolder.getChildren();
        for (int i = 0; i < ch.length; i++)
            ch[i].delete();
        FileObject paletteRoot = itemsFolder.getParent();
        itemsFolder.delete();
        paletteRoot.delete();
    }
    
    public void testReadItemWithActiveEditorDrop() throws Exception {
        FileObject itemFile = createItemFileWithActiveEditorDrop();
        verifyPaletteItem(itemFile);
    }

    public void testReadItemWithBody() throws Exception {
        FileObject itemFile = createItemFileWithBody();
        verifyPaletteItem(itemFile);
    }
    
    public void testReadItemWithInlineDescription() throws Exception {
        FileObject itemFile = createItemFileWithInLineDescription();
        verifyPaletteItem(itemFile);
    }
    
    private void verifyPaletteItem(FileObject itemFile) throws Exception {
        assertNotNull(itemFile);
        
        Node itemNode = DataObject.find(itemFile).getNodeDelegate();
        
        assertEquals("Item loaded with a wrong display name.", NbBundle.getBundle(BUNDLE_NAME).getString(NAME_KEY), itemNode.getDisplayName());
        assertEquals("Item loaded with a wrong description.", NbBundle.getBundle(BUNDLE_NAME).getString(TOOLTIP_KEY), itemNode.getShortDescription());
        assertNotNull("Item loaded with no small icon.", itemNode.getIcon(BeanInfo.ICON_COLOR_16x16));
        assertNotNull("Item loaded with no big icon.", itemNode.getIcon(BeanInfo.ICON_COLOR_32x32));
        
        Object o = itemNode.getLookup().lookup(ActiveEditorDrop.class);
        assertNotNull("Item does not contain ActiveEditorDrop implementation in its lookup.", o);
    }

    public void testFindItem() throws Exception {
        FileObject itemFile = createItemFileWithActiveEditorDrop();
        assertNotNull(itemFile);

        //create palette
        PaletteController pc = PaletteFactory.createPalette(PALETTE_ROOT, new DummyActions());
        
        //find node with ActiveEditorDrop impl in its lookup
        Node root = (Node)pc.getRoot().lookup(Node.class);
        Node[] cats = root.getChildren().getNodes(true);
        Node foundNode = null;
        Node foundCat = null;
        assertEquals("Too many categories", 1, cats.length);
        Node[] items = cats[0].getChildren().getNodes(true);
        assertEquals("Too many items", 1, items.length);
        
        assertTrue("Item not found.",
                    DataObject.find(itemFile).getNodeDelegate().getLookup().lookup(ActiveEditorDrop.class) ==
                                                       items[0].getLookup().lookup(ActiveEditorDrop.class)
        ); 

    }
    
    public void testSelectItem() throws Exception {
        FileObject itemFile = createItemFileWithActiveEditorDrop();
        assertNotNull(itemFile);

        //create palette
        PaletteController pc = PaletteFactory.createPalette(PALETTE_ROOT, new DummyActions());
        pc.addPropertyChangeListener(new PaletteListener(pc));

        //simulate node selection
        Category modelCat = pc.getModel().getCategories()[0];
        Item modelItem = modelCat.getItems()[0];
        pc.getModel().setSelectedItem(modelCat.getLookup(), modelItem.getLookup());
        
        assertNotNull("Selected item does not contain ActiveEditorDrop implementation", 
                selectedNode.lookup(ActiveEditorDrop.class));
    }

    
    //----------------------------------   helpers  ------------------------------------------------------------------

    private class PaletteListener implements PropertyChangeListener {
        PaletteController pc;
        PaletteListener(PaletteController pc) { this.pc = pc; }
        public void propertyChange(PropertyChangeEvent evt) {
            assertEquals("Property " + PaletteController.PROP_SELECTED_ITEM + " was expected.",
                    PaletteController.PROP_SELECTED_ITEM, evt.getPropertyName());
            
            selectedNode = pc.getSelectedItem();
        }
    };
        
    
    private FileObject createItemsFolder() throws IOException {
        FileObject root = FileUtil.getConfigRoot();
        FileObject fooCategory = FileUtil.createFolder(root, ITEMS_FOLDER);
        return fooCategory;
    }
    
    private FileObject createItemFileWithActiveEditorDrop() throws Exception {
        FileObject fo = itemsFolder.createData(ITEM_FILE);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), StandardCharsets.UTF_8);
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.0//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_0.dtd'>");
                writer.write("<editor_palette_item version='1.0'>");
                writer.write("<class name='" + CLASS_NAME + "' />");
                writer.write("<icon16 urlvalue='" + ICON16 + "' />");
                writer.write("<icon32 urlvalue='" + ICON32 + "' />");
                writer.write("<description localizing-bundle='" + BUNDLE_NAME + "' display-name-key='" + NAME_KEY + "' tooltip-key='" + TOOLTIP_KEY + "' />");
                writer.write("</editor_palette_item>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }           
        return fo;
    }
    
    private FileObject createItemFileWithBody() throws Exception {
        FileObject fo = itemsFolder.createData(ITEM_FILE);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), StandardCharsets.UTF_8);
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.0//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_0.dtd'>");
                writer.write("<editor_palette_item version='1.0'>");
                writer.write("<body><![CDATA[" + BODY + "]]></body>");
                writer.write("<icon16 urlvalue='" + ICON16 + "' />");
                writer.write("<icon32 urlvalue='" + ICON32 + "' />");
                writer.write("<description localizing-bundle='" + BUNDLE_NAME + "' display-name-key='" + NAME_KEY + "' tooltip-key='" + TOOLTIP_KEY + "' />");
                writer.write("</editor_palette_item>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }           
        return fo;
    }
    
    private FileObject createItemFileWithInLineDescription() throws Exception {
        FileObject fo = itemsFolder.createData(ITEM_FILE);
        FileLock lock = fo.lock();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), StandardCharsets.UTF_8);
            try {
                writer.write("<?xml version='1.0' encoding='UTF-8'?>");
                writer.write("<!DOCTYPE editor_palette_item PUBLIC '-//NetBeans//Editor Palette Item 1.1//EN' 'http://www.netbeans.org/dtds/editor-palette-item-1_1.dtd'>");
                writer.write("<editor_palette_item version='1.1'>");
                writer.write("<body><![CDATA[" + BODY + "]]></body>");
                writer.write("<icon16 urlvalue='" + ICON16 + "' />");
                writer.write("<icon32 urlvalue='" + ICON32 + "' />");
                writer.write("<inline-description> <display-name>"
                        +NbBundle.getBundle(BUNDLE_NAME).getString(NAME_KEY)+"</display-name> <tooltip>"
                        +NbBundle.getBundle(BUNDLE_NAME).getString(TOOLTIP_KEY)+"</tooltip>  </inline-description>");
                writer.write("</editor_palette_item>");
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
        }           
        return fo;
    }
    
}
