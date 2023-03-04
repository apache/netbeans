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

package org.netbeans.modules.palette;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTargetDragEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Lookup;

/**
 * PaletteCategory implementation based on Nodes.
 *
 * @author S. Aubrecht
 */
public class DefaultCategory implements Category, NodeListener {
    
    private Node categoryNode;
    private ArrayList<CategoryListener> categoryListeners = new ArrayList<CategoryListener>( 3 );
    private Item[] items;
    
    /** 
     * Creates a new instance of DefaultPaletteCategory 
     *
     * @param categoryNode Node representing the category.
     */
    public DefaultCategory( Node categoryNode ) {
        this.categoryNode = categoryNode;
        this.categoryNode.addNodeListener( this );
        this.categoryNode.addPropertyChangeListener( new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                notifyListeners();
            }
        });
    }

    public Image getIcon(int type) {
        return categoryNode.getIcon( type );
    }

    public void addCategoryListener( CategoryListener listener ) {
        synchronized( categoryListeners ) {
            categoryListeners.add( listener );
        }
    }

    public void removeCategoryListener( CategoryListener listener ) {
        synchronized( categoryListeners ) {
            categoryListeners.remove( listener );
        }
    }

    public Action[] getActions() {
        return categoryNode.getActions( false );
    }

    public String getShortDescription() {
        return categoryNode.getShortDescription();
    }

    public Item[] getItems() {
        if( null == items ) {
            Node[] children = categoryNode.getChildren().getNodes( DefaultModel.canBlock() );
            Item[] newItems = new Item[children.length];
            for( int i=0; i<children.length; i++ ) {
                newItems[i] = new DefaultItem( children[i] );
            }
            items = newItems;
        }
        return items;
    }

    public String getName() {
        return categoryNode.getName();
    }

    public String getDisplayName() {
        return categoryNode.getDisplayName();
    }

    protected void notifyListeners() {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                CategoryListener[] listeners;
                synchronized( categoryListeners ) {
                    listeners = new CategoryListener[categoryListeners.size()];
                    listeners = categoryListeners.toArray( listeners );
                }
                for( int i=0; i<listeners.length; i++ ) {
                    listeners[i].categoryModified( DefaultCategory.this );
                }
            }
        });
    }
    
    /** Fired when a set of new children is added.
    * @param ev event describing the action
    */
    public synchronized void childrenAdded(NodeMemberEvent ev) {
        items = null;
        notifyListeners();
    }

    /** Fired when a set of children is removed.
    * @param ev event describing the action
    */
    public synchronized void childrenRemoved(NodeMemberEvent ev) {
        items = null;
        notifyListeners();
    }

    /** Fired when the order of children is changed.
    * @param ev event describing the change
    */
    public synchronized void childrenReordered(NodeReorderEvent ev) {
        items = null;
        notifyListeners();
    }

    /** Fired when the node is deleted.
    * @param ev event describing the node
    */
    public synchronized void nodeDestroyed(NodeEvent ev) {
        categoryNode.removeNodeListener( this );
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if( Node.PROP_DISPLAY_NAME.equals( evt.getPropertyName() ) ) {
            notifyListeners();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if( !(obj instanceof DefaultCategory) )
            return false;
        
        return categoryNode.equals( ((DefaultCategory) obj).categoryNode );
    }

    public Transferable getTransferable() {
        try {
            return categoryNode.drag();
        } catch( IOException ioE ) {
            Logger.getLogger( DefaultCategory.class.getName() ).log( Level.INFO, null, ioE );
        }
        return null;
    }
    
    public Lookup getLookup() {
        return categoryNode.getLookup();
    }
    
    private int itemToIndex( Item item ) {
        if( null == item ) {
            return -1;
        }
        Node node = item.getLookup().lookup(Node.class);
        if( null != node ) {
            Index order = categoryNode.getCookie(Index.class);
            if( null != order ) {
                return order.indexOf( node );
            }
        }
        return -1;
    }
    
    public boolean dragOver( DropTargetDragEvent e ) {
        DragAndDropHandler handler = getDragAndDropHandler();
        return handler.canDrop( getLookup(), e.getCurrentDataFlavors(), e.getDropAction() );
    }

    public boolean dropItem( Transferable dropItem, int dndAction, Item target, boolean dropBefore ) {
        int targetIndex = itemToIndex( target );
        if( !dropBefore ) {
            targetIndex++;
        }
        DragAndDropHandler handler = getDragAndDropHandler();
        boolean res = handler.doDrop( getLookup(), dropItem, dndAction, targetIndex );
        items = null;
        return res;
    }
    
    private DragAndDropHandler getDragAndDropHandler() {
        return categoryNode.getLookup().lookup(DragAndDropHandler.class);
    }
    
    @Override
    public String toString() {
        return categoryNode.getDisplayName();
    }
}
