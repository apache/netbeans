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
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Index;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author S. Aubrecht
 */
public class DragAndDropHandlerTest extends AbstractPaletteTestHid {
    
    public DragAndDropHandlerTest(String testName) {
        super(testName);
    }

    public void testCustomize() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Item item = model.getCategories()[0].getItems()[0];
        
        DragAndDropHandler handler = DragAndDropHandler.getDefault();
        
        ExTransferable t = ExTransferable.create( item.cut() );
        DataFlavor[] flavorsBefore = t.getTransferDataFlavors();
        
        handler.customize( t, item.getLookup() );
        
        DataFlavor[] flavorsAfter = t.getTransferDataFlavors();
        assertEquals( "Default implementation does nothing", flavorsBefore.length, flavorsAfter.length );
        for( int i=0; i<flavorsBefore.length; i++ ) {
            assertEquals( "Default implementation does nothing", flavorsBefore[i], flavorsAfter[i] );
        }
    }
    
    public void testCanDrop() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category cat = model.getCategories()[0];
        
        DragAndDropHandler handler = DragAndDropHandler.getDefault();
        
        DataFlavor[] flavors = new DataFlavor[] { PaletteController.ITEM_DATA_FLAVOR };
        assertTrue( handler.canDrop( cat.getLookup(), flavors, DnDConstants.ACTION_COPY_OR_MOVE ) );
        
        flavors = new DataFlavor[] { new DataFlavor( "text/xml" )  };
        assertFalse( handler.canDrop( cat.getLookup(), flavors, DnDConstants.ACTION_COPY_OR_MOVE ) );
    }
    
    public void testCanDropText() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category cat = model.getCategories()[0];
        
        DragAndDropHandler handler = new TextDragAndDropHandler();
        
        DataFlavor[] flavors = new DataFlavor[] { new DataFlavor( "text/xml" )  };
        assertTrue( handler.canDrop( cat.getLookup(), flavors, DnDConstants.ACTION_COPY_OR_MOVE ) );
        
        flavors = new DataFlavor[] { new DataFlavor( "text/html" )  };
        assertTrue( handler.canDrop( cat.getLookup(), flavors, DnDConstants.ACTION_COPY_OR_MOVE ) );
        
        flavors = new DataFlavor[] { new DataFlavor( "unsupported/mimetype" )  };
        assertFalse( handler.canDrop( cat.getLookup(), flavors, DnDConstants.ACTION_COPY_OR_MOVE ) );
    }
    
    public void testDoDropReorderItemWithinCategory() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        Category cat = categories[0];
        Item[] itemsBeforeMove = cat.getItems();
        
        Item source = itemsBeforeMove[0];
        Item target = itemsBeforeMove[itemsBeforeMove.length-1];
        
        DragAndDropHandler handler = DragAndDropHandler.getDefault();
        
        assertTrue( handler.doDrop( cat.getLookup(), source.cut(), DnDConstants.ACTION_COPY_OR_MOVE, itemsBeforeMove.length ) );
        
        pc.refresh();
        
        Item[] itemsAfterMove = pc.getModel().getCategories()[0].getItems();
        
        assertEquals( itemsBeforeMove.length, itemsAfterMove.length );
        assertEquals( source.getName(), itemsAfterMove[itemsAfterMove.length-1].getName() );
        assertEquals( itemsBeforeMove[1].getName(), itemsAfterMove[0].getName() );
        assertEquals( target.getName(), itemsAfterMove[itemsAfterMove.length-1-1].getName() );
    }
    
    public void testDoDropItemToOtherCategory() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        Category srcCat = categories[0];
        Item[] srcItemsBefore = srcCat.getItems();
        Item dropItem = srcItemsBefore[0];

        Category tgtCat = categories[1];
        Item[] tgtItemsBefore = tgtCat.getItems();
        Item target = tgtItemsBefore[5];
        
        DragAndDropHandler handler = DragAndDropHandler.getDefault();
        
        assertTrue( handler.doDrop( tgtCat.getLookup(), dropItem.cut(), DnDConstants.ACTION_COPY_OR_MOVE, 5 ) );
        
        //force all nodes in the palette to update their children
        pc.refresh();
        categories = model.getCategories();
        srcCat = categories[0];
        tgtCat = categories[1];
        
        Item[] srcItemsAfter = srcCat.getItems();
        Item[] tgtItemsAfter = tgtCat.getItems();
        
        assertEquals( srcItemsBefore.length, srcItemsAfter.length+1 );
        for( int i=0; i<srcItemsAfter.length; i++ ) {
            assertEquals( srcItemsBefore[i+1].getName(), srcItemsAfter[i].getName() );
        }
        
        assertEquals( tgtItemsBefore.length, tgtItemsAfter.length-1 );
        assertEquals( target.getName(), tgtItemsAfter[5+1].getName() );
        assertEquals( dropItem.getName(), tgtItemsAfter[5].getName() );
    }
    
    public void testCanReorderCategories() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        DragAndDropHandler handler = DragAndDropHandler.getDefault();
        
        assertTrue( handler.canReorderCategories( model.getRoot() ) );
        
        assertFalse( handler.canReorderCategories( new NoIndexCookieNode().getLookup() ) );
    }
    
    public void testMoveCategory() throws Exception {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] catBefore = model.getCategories();
        
        Category srcCat = catBefore[0];
        Category tgtCat = catBefore[5];
        
        DragAndDropHandler handler = DragAndDropHandler.getDefault();
        
        assertTrue( handler.moveCategory( srcCat.getLookup(), 5 ) );
        
        pc.refresh();
        
        Category[] catAfter = model.getCategories();
        
        assertEquals( catBefore.length, catAfter.length );
        assertEquals( srcCat.getName(), catAfter[4].getName() );
        assertEquals( catBefore[1].getName(), catAfter[0].getName() );
        assertEquals( tgtCat.getName(), catAfter[5].getName() );
    }
    
    private static class NoIndexCookieNode extends AbstractNode {
        public NoIndexCookieNode() {
            super( Children.LEAF );
        }

        public org.openide.nodes.Node.Cookie getCookie(Class type) {
            if( Index.class.equals( type ) )
                return null;
            return super.getCookie(type);
        }
    }
    
    private static class TextDragAndDropHandler extends DragAndDropHandler {
        public TextDragAndDropHandler() {
            super( true );
        }

        @Override
        public void customize(ExTransferable t, Lookup item) {
        }
    }
}
