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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.beans.BeanInfo;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.palette.Category;
import org.netbeans.modules.palette.Item;
import org.netbeans.modules.palette.Model;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
/**
 *
 * @author S. Aubrecht
 */
public class CategoryTest extends AbstractPaletteTestHid {
    
    public CategoryTest(String testName) {
        super(testName);
    }

    /**
     * Test of getName method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetName() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categoryNames.length; i++ ) {
            Node catNode = getCategoryNode( categoryNames[i] );
            assertEquals( catNode.getName(), categories[i].getName() );
        }
    }

    /**
     * Test of getDisplayName method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetDisplayName() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categoryNames.length; i++ ) {
            Node catNode = getCategoryNode( categoryNames[i] );
            assertEquals( catNode.getDisplayName(), categories[i].getDisplayName() );
        }
    }

    /**
     * Test of getShortDescription method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetShortDescription() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categoryNames.length; i++ ) {
            Node catNode = getCategoryNode( categoryNames[i] );
            assertEquals( catNode.getShortDescription(), categories[i].getShortDescription() );
        }
    }

    /**
     * Test of getIcon method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetIcon() throws IOException {
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), new DummyActions() );
        Model model = pc.getModel();
        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categoryNames.length; i++ ) {
            Node catNode = getCategoryNode( categoryNames[i] );
            assertEquals( catNode.getIcon( BeanInfo.ICON_COLOR_16x16 ), categories[i].getIcon( BeanInfo.ICON_COLOR_16x16 ) );
            assertEquals( catNode.getIcon( BeanInfo.ICON_COLOR_32x32 ), categories[i].getIcon( BeanInfo.ICON_COLOR_32x32 ) );
        }
    }

    /**
     * Test of getActions method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetActions() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categoryNames.length; i++ ) {
            Action[] catActions = categories[i].getActions();
            
            Action[] providedActions = actions.getCustomCategoryActions( categories[i].getLookup() );
            
            for( int k=0; k<providedActions.length; k++ ) {
                if( null == providedActions[k] )
                    continue;
                boolean found = false;
                for( int j=0; j<catActions.length; j++ ) {
                    if( null == catActions[j] )
                        continue;
                    if( catActions[j].equals( providedActions[k] ) ) {
                        found = true;
                        break;
                    }
                }
                assertTrue( "Action " + providedActions[k].getValue( Action.NAME ) + " not found in palette actions.", found );
            }
        }
    }

    /**
     * Test of getItems method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetItems() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categories.length; i++ ) {
            Item[] items = categories[i].getItems();
            assertEquals( itemNames[i].length, items.length );
            for( int j=0; j<items.length; j++ ) {
                assertEquals( itemNames[i][j], items[j].getName() );
            }
        }
    }

    /**
     * Test of getTransferable method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetTransferable() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categories.length; i++ ) {
            Transferable t = categories[i].getTransferable();
            assertNotNull( t );
        }
    }

    /**
     * Test of getLookup method, of class org.netbeans.modules.palette.Category.
     */
    public void testGetLookup() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        assertEquals( categoryNames.length, categories.length );
        
        for( int i=0; i<categories.length; i++ ) {
            Lookup lkp = categories[i].getLookup();
            assertNotNull( lkp );
            Node node = (Node)lkp.lookup( Node.class );
            assertEquals( categoryNames[i], node.getName() );
        }
    }

    /**
     * Test of moveItem method, of class org.netbeans.modules.palette.Category.
     */
    public void testMoveItemBefore() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        Category cat = categories[0];
        Item[] itemsBeforeMove = cat.getItems();
        
        Item source = itemsBeforeMove[0];
        Item target = itemsBeforeMove[itemsBeforeMove.length-1];
        
        cat.dropItem( createTransferable( source ), DnDConstants.ACTION_COPY_OR_MOVE, target, true );
        
        Item[] itemsAfterMove = cat.getItems();
        
        assertEquals( itemsBeforeMove.length, itemsAfterMove.length );
        assertEquals( source.getName(), itemsAfterMove[itemsAfterMove.length-1-1].getName() );
        assertEquals( itemsBeforeMove[1].getName(), itemsAfterMove[0].getName() );
        assertEquals( target.getName(), itemsAfterMove[itemsAfterMove.length-1].getName() );
    }

    /**
     * Test of moveItem method, of class org.netbeans.modules.palette.Category.
     */
    public void testMoveItemAfter() throws IOException {
        PaletteActions actions = new DummyActions();
        PaletteController pc = PaletteFactory.createPalette( getRootFolderName(), actions );
        Model model = pc.getModel();

        Category[] categories = model.getCategories();
        
        Category cat = categories[0];
        Item[] itemsBeforeMove = cat.getItems();
        
        Item source = itemsBeforeMove[0];
        Item target = itemsBeforeMove[itemsBeforeMove.length-1];
        
        cat.dropItem( createTransferable( source ), DnDConstants.ACTION_COPY_OR_MOVE, target, false );
        
        Item[] itemsAfterMove = cat.getItems();
        
        assertEquals( itemsBeforeMove.length, itemsAfterMove.length );
        assertEquals( source.getName(), itemsAfterMove[itemsAfterMove.length-1].getName() );
        assertEquals( itemsBeforeMove[1].getName(), itemsAfterMove[0].getName() );
        assertEquals( target.getName(), itemsAfterMove[itemsAfterMove.length-1-1].getName() );
    }

    public void testDropItemBefore() throws IOException {
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
        
        tgtCat.dropItem( dropItem.cut(), DnDConstants.ACTION_COPY_OR_MOVE, target, true );
        
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

    public void testDropItemAfter() throws IOException {
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
        
        tgtCat.dropItem( dropItem.cut(), DnDConstants.ACTION_COPY_OR_MOVE, target, false );
        
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
        assertEquals( target.getName(), tgtItemsAfter[5].getName() );
        assertEquals( dropItem.getName(), tgtItemsAfter[5+1].getName() );
    }
    
    private Transferable createTransferable( final Item item ) {
        return new ExTransferable.Single( PaletteController.ITEM_DATA_FLAVOR ) {
            protected Object getData() throws IOException, UnsupportedFlavorException {
                return item.getLookup();
            }
        };
    }
}
