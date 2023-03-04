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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.palette.DefaultModel;
import org.netbeans.modules.palette.ui.TextImporter;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
/**
 * <p>An abstract class implemented by palette clients to implement drag and drop
 * of new items into the palette window and to customize the default Transferable 
 * instance of items being dragged from the palette window to editor area.</p>
 *
 * <p>Client's can support multiple DataFlavors that may help to enable/disable the drop
 * when dragging an item over different editor area parts that allow only certain
 * item types to be dropped into them.</p>
 *
 * @author S. Aubrecht
 */
public abstract class DragAndDropHandler {

    private boolean isTextDnDEnabled;
    
    private static DragAndDropHandler defaultHandler;
    
    public DragAndDropHandler() {
        this( false );
    }
    
    /**
     * Subclass this class and use this c'tor with <code>true</code> parameter to support text drag and drop
     * into the palette to create new custom code clips. Subclassed instance must be then provided
     * to <code>PaletteFactory</code> when creating <code>PaletteController</code>.
     * @param textDnDEnabled True to allow text to be dropped into the palette window.
     */
    protected DragAndDropHandler( boolean textDnDEnabled ) {
        this.isTextDnDEnabled = textDnDEnabled;
    }
    
    static DragAndDropHandler getDefault() {
        if( null == defaultHandler )
            defaultHandler = new DefaultDragAndDropHandler();
        return defaultHandler;
    }
    
    /**
     * Add your own custom DataFlavor as need to suppor drag-over a different
     * parts of editor area.
     *
     * @param t Item's default Transferable.
     * @param item Palette item's Lookup.
     *
     */
    public abstract void customize( ExTransferable t, Lookup item );
    
    /**
     * @param targetCategory Lookup of the category under the drop cursor.
     * @param flavors Supported DataFlavors.
     * @param dndAction Drop action type.
     *
     * @return True if the given category can accept the item being dragged.
     */
    public boolean canDrop( Lookup targetCategory, DataFlavor[] flavors, int dndAction ) {
        for( int i=0; i<flavors.length; i++ ) {
            if( PaletteController.ITEM_DATA_FLAVOR.equals( flavors[i] ) ) {
                return true;
            }
        }
        return (isTextDnDEnabled && DataFlavor.selectBestTextFlavor(flavors) != null);
    }
    
    /**
     * Perform the drop operation and add the dragged item into the given category.
     *
     * @param targetCategory Lookup of the category that accepts the drop.
     * @param item Transferable holding the item being dragged.
     * @param dndAction Drag'n'drop action type.
     * @param dropIndex Zero-based position where the dragged item should be dropped.
     *
     * @return True if the drop has been successful, false otherwise.
     */
    public boolean doDrop( Lookup targetCategory, Transferable item, int dndAction, int dropIndex ) {
        Node categoryNode = (Node)targetCategory.lookup( Node.class );
        try {
            //first check if we're reordering items within the same category
            if( item.isDataFlavorSupported( PaletteController.ITEM_DATA_FLAVOR ) ) {
                Lookup itemLookup = (Lookup)item.getTransferData( PaletteController.ITEM_DATA_FLAVOR );
                if( null != itemLookup ) {
                    Node itemNode = (Node)itemLookup.lookup( Node.class );
                    if( null != itemNode ) {
                        Index order = (Index)categoryNode.getCookie( Index.class );
                        if( null != order && order.indexOf( itemNode ) >= 0 ) {
                            //the drop item comes from the targetCategory so let's 
                            //just change the order of items
                            return moveItem( targetCategory, itemLookup, dropIndex );
                        }
                    }
                }
            }
            PasteType paste = categoryNode.getDropType( item, dndAction, dropIndex );
            if( null != paste ) {
                Node[] itemsBefore = categoryNode.getChildren().getNodes( DefaultModel.canBlock() );
                paste.paste();
                Node[] itemsAfter = categoryNode.getChildren().getNodes( DefaultModel.canBlock() );
                
                if( itemsAfter.length == itemsBefore.length+1 ) {
                    int currentIndex = -1;
                    Node newItem = null;
                    for( int i=itemsAfter.length-1; i>=0; i-- ) {
                        newItem = itemsAfter[i];
                        currentIndex = i;
                        for( int j=0; j<itemsBefore.length; j++ ) {
                            if( newItem.equals( itemsBefore[j] ) ) {
                                newItem = null;
                                break;
                            }
                        }
                        if( null != newItem ) {
                            break;
                        }
                    }
                    if( null != newItem && dropIndex >= 0 ) {
                        if( currentIndex < dropIndex )
                            dropIndex++;
                        moveItem( targetCategory, newItem.getLookup(), dropIndex );
                    }
                }
                return true;
            }
            if( isTextDnDEnabled && null != DataFlavor.selectBestTextFlavor(item.getTransferDataFlavors()) ) {
                importTextIntoPalette( targetCategory, item, dropIndex );
                return false; //return false to retain the original dragged text in its source
            }
        } catch( IOException ioE ) {
            Logger.getLogger( DragAndDropHandler.class.getName() ).log( Level.INFO, null, ioE );
        } catch( UnsupportedFlavorException e ) {
            Logger.getLogger( DragAndDropHandler.class.getName() ).log( Level.INFO, null, e );
        }
        return false;
    }
    
    /**
     * Move palette item to a new position in its current category.
     *
     * @param category Lookup of the category that contains the dragged item.
     * @param itemToMove Lookup of the item that is going to be moved to a new position.
     * @param moveToIndex Zero-based index to category's children where the item should move to.
     *
     * @return True if the move operation was successful.
     */
    private boolean moveItem( Lookup category, Lookup itemToMove, int moveToIndex ) {
        Node categoryNode = (Node)category.lookup( Node.class );
        if( null == categoryNode )
            return false;
        Node itemNode = (Node)itemToMove.lookup( Node.class );
        if( null == itemNode )
            return false;
        
        Index order = (Index)categoryNode.getCookie( Index.class );
        if( null == order ) {
            return false;
        }
        
        int sourceIndex = order.indexOf( itemNode );
        if( sourceIndex < moveToIndex ) {
            moveToIndex--;
        }
        order.move( sourceIndex, moveToIndex );
        return true;
    }
    
    /**
     * @param paletteRoot Lookup of palette's root node.
     * @return True if it is possible to reorder categories by drag and drop operations.
     */
    public boolean canReorderCategories( Lookup paletteRoot ) {
        Node rootNode = (Node)paletteRoot.lookup( Node.class );
        if( null != rootNode ) {
            return null != rootNode.getCookie( Index.class );
        }
        return false;
    }
    
    /**
     * Move the given category to a new position.
     *
     * @param category The lookup of category that is being dragged.
     * @param moveToIndex Zero-based index to palette's root children Nodes 
     * where the category should move to.
     * @return True if the move operation was successful.
     */
    public boolean moveCategory( Lookup category, int moveToIndex ) {
        Node categoryNode = (Node)category.lookup( Node.class );
        if( null == categoryNode )
            return false;
        Node rootNode = categoryNode.getParentNode();
        if( null == rootNode )
            return false;
        
        Index order = (Index)rootNode.getCookie( Index.class );
        if( null == order ) {
            return false;
        }
        
        int sourceIndex = order.indexOf( categoryNode );
        if( sourceIndex < moveToIndex ) {
            moveToIndex--;
        }
        order.move( sourceIndex, moveToIndex );
        return true;
    }
    
    private boolean importTextIntoPalette( Lookup targetCategory, Transferable item, int dropIndex ) 
            throws IOException, UnsupportedFlavorException {
        
        DataFlavor flavor = DataFlavor.selectBestTextFlavor( item.getTransferDataFlavors() );
        if( null == flavor )
            return false;
        
        String textToImport = extractText( item, flavor );
        SwingUtilities.invokeLater( new TextImporter( textToImport, targetCategory, dropIndex ) );
        return true;
    }
    
    private String extractText( Transferable t, DataFlavor flavor ) 
            throws IOException, UnsupportedFlavorException {
        
        Reader reader = flavor.getReaderForText(t);
        if( null == reader )
            return null;
        StringBuffer res = new StringBuffer();
        char[] buffer = new char[4*1024];
        int len;
        while( (len=reader.read( buffer )) > 0 ) {
            res.append(buffer, 0, len);
        }
        
        return res.toString();
    }
    
    private static final class DefaultDragAndDropHandler extends DragAndDropHandler {
        public void customize(ExTransferable t, Lookup item) {
            //do nothing
        }
    }
}
