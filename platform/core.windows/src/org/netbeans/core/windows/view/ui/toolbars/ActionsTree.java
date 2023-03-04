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

package org.netbeans.core.windows.view.ui.toolbars;


import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.tree.*;
import org.openide.explorer.view.*;
import org.openide.nodes.Node;

/**
 * A tree displaying a hierarchy of Node in a similar fashion as the TreeView does
 * except for unnecessary popmenus and drag'n'drop implementation.
 *
 * @author Stanislav Aubrecht
 */
public class ActionsTree extends BeanTreeView implements DragGestureListener, DragSourceListener {
    
    private Cursor dragMoveCursor = DragSource.DefaultMoveDrop;
    private Cursor dragNoDropCursor = DragSource.DefaultMoveNoDrop;
    
    /** Creates a new instance of ActionsTree */
    public ActionsTree() {
        setRootVisible( false );
        tree.setCellRenderer( new NodeRenderer() );
        tree.setShowsRootHandles( true );
        setDragSource( false );
        setDropTarget( false );
        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer( tree, DnDConstants.ACTION_MOVE, this );
        setQuickSearchAllowed( true );
        setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath path = tree.getPathForLocation( dge.getDragOrigin().x, dge.getDragOrigin().y );
        if( null != path ) {
            Object obj = path.getLastPathComponent();
            if( tree.getModel().isLeaf( obj ) ) {
                try {
                    Node node = Visualizer.findNode( obj );
                    Transferable t = node.drag();
                    dge.getDragSource().addDragSourceListener( this );
                    dge.startDrag( dragNoDropCursor, t );
                } catch( InvalidDnDOperationException e ) {
                    //#214776 - somebody didn't finish their dnd operation properly
                    Logger.getLogger(ActionsTree.class.getName()).log(Level.INFO, e.getMessage(), e);
                } catch( IOException e ) {
                    Logger.getLogger(ActionsTree.class.getName()).log(Level.WARNING, null, e);
                }
            }
        }
    }

    @Override
    public void dragExit(java.awt.dnd.DragSourceEvent dse) {
        dse.getDragSourceContext().setCursor( dragNoDropCursor );
    }

    @Override
    public void dropActionChanged(java.awt.dnd.DragSourceDragEvent dsde) {
    }

    @Override
    public void dragOver(java.awt.dnd.DragSourceDragEvent e) {
        DragSourceContext context = e.getDragSourceContext();
        int action = e.getDropAction();
        if ((action & DnDConstants.ACTION_MOVE) != 0) {
            context.setCursor( dragMoveCursor );
        } else {
            context.setCursor( dragNoDropCursor );
        }
    }

    @Override
    public void dragEnter(java.awt.dnd.DragSourceDragEvent dsde) {
        dragOver( dsde );
    }

    @Override
    public void dragDropEnd(java.awt.dnd.DragSourceDropEvent dsde) {
    }
}
