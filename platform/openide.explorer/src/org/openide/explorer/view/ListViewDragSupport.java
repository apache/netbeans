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
package org.openide.explorer.view;

import org.openide.nodes.Node;

import java.awt.Point;
import java.awt.dnd.*;

import javax.swing.JList;


/**
*
* @author Dafe Simonek, Jiri Rechtacek
*/
class ListViewDragSupport extends ExplorerDragSupport {
    // Attributes

    /** Holds selected indices - it's here only
    * as a workaround for sun's bug */

    /*int[] oldSelection;
    int[] curSelection;*/

    // Associations

    /** The view that manages viewing the data in a tree. */
    protected ListView view;

    /** The tree which we are supporting (our client) */
    protected JList list;

    // Operations

    /** Creates new TreeViewDragSupport, initializes gesture */
    public ListViewDragSupport(ListView view, JList list) {
        this.comp = list;
        this.view = view;
        this.list = list;
    }

    int getAllowedDropActions() {
        return view.getAllowedDropActions();
    }

    protected int getAllowedDragActions() {
        return view.getAllowedDragActions();
    }

    /** Initiating the drag */
    public void dragGestureRecognized(DragGestureEvent dge) {
        super.dragGestureRecognized(dge);
    }

    /** Utility method. Returns either selected nodes in the list
    * (if cursor hotspot is above some selected node) or the node
    * the cursor points to.
    * @return Node array or null if position of the cursor points
    * to no node.
    */
    @Override
    Node[] obtainNodes(DragGestureEvent dge) {
        Point dragOrigin = dge.getDragOrigin();
        int index = list.locationToIndex(dragOrigin);
        if (index < 0) {
            return null;
        }
        
        Object obj = list.getModel().getElementAt(index);

        if (obj instanceof VisualizerNode) {
            obj = ((VisualizerNode) obj).node;
        }

        // check conditions
        if ((index < 0)) {
            return null;
        }

        if (!(obj instanceof Node)) {
            return null;
        }

        Node[] result = null;

        if (list.isSelectedIndex(index)) {
            // cursor is above selection, so return all selected indices
            Object[] selected = list.getSelectedValues();
            result = new Node[selected.length];

            for (int i = 0; i < selected.length; i++) {
                if (selected[i] instanceof VisualizerNode) {
                    result[i] = ((VisualizerNode) selected[i]).node;
                } else {
                    if (!(selected[i] instanceof Node)) {
                        return null;
                    }

                    result[i] = (Node) selected[i];
                }
            }
        } else {
            // return only the node the cursor is above
            result = new Node[] { (Node) obj };
        }

        return result;
    }
}
 // end of ListViewDragSupport
