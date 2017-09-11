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
