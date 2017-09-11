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
