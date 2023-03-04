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

import javax.swing.JTable;


/**
*
* @author Dafe Simonek, Jiri Rechtacek
*/
class TableViewDragSupport extends ExplorerDragSupport {

    /** The view that manages viewing the data in a table. */
    protected TableView view;

    /** The table which we are supporting (our client) */
    protected JTable table;

    // Operations

    /** Creates new TreeViewDragSupport, initializes gesture */
    public TableViewDragSupport(TableView view, JTable table) {
        this.comp = table;
        this.view = view;
        this.table = table;
    }

    int getAllowedDropActions() {
        return view.getAllowedDropActions();
    }

    /** Initiating the drag */
    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        super.dragGestureRecognized(dge);
    }

    /** Utility method. Returns either selected nodes in the list
    * (if cursor hotspot is above some selected node) or the node
    * the cursor points to.
    * @return Node array or null if position of the cursor points
    * to no node.
    */
    Node[] obtainNodes(DragGestureEvent dge) {
        Point dragOrigin = dge.getDragOrigin();
        int index = table.rowAtPoint(dge.getDragOrigin());
        Node n = view.getNodeFromRow(index);

        Node[] result = null;

        result = new Node[] { n };

        return result;
    }
}
