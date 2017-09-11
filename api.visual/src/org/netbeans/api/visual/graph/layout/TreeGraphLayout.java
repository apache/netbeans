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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.visual.graph.layout;

import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.util.*;

/**
 * This class supplies the tree graph-oriented layout for a GraphScene.
 *
 * @author David Kaspar
 */
public class TreeGraphLayout<N, E> extends GraphLayout {

    private GraphScene<N, E> scene;
    private int originX;
    private int originY;
    private int verticalGap;
    private int horizontalGap;
    private boolean vertical;

    /**
     * Creates a graph-oriented tree layout.
     * @param scene the GraphScene where the layout is used
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     */
    public TreeGraphLayout (GraphScene<N, E> scene, int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        this.scene = scene;
        this.originX = originX;
        this.originY = originY;
        this.verticalGap = verticalGap;
        this.horizontalGap = horizontalGap;
        this.vertical = vertical;
    }

    /**
     * Invokes the layout for a specified root node.
     * @param rootNode the root node
     */
    public final void layout (N rootNode) {
        if (rootNode == null)
            return;
        Collection<N> allNodes = scene.getNodes ();
        ArrayList<N> nodesToResolve = new ArrayList<N> (allNodes);

        HashSet<N> loadedSet = new HashSet<N> ();
        Node root = new Node (rootNode, loadedSet);
        nodesToResolve.removeAll (loadedSet);
        if (vertical) {
            root.allocateHorizontally ();
            root.resolveVertically (originX, originY);
        } else {
            root.allocateVertically ();
            root.resolveHorizontally (originX, originY);
        }

        final HashMap<N, Point> resultPosition = new HashMap<N, Point> ();
        root.upload (resultPosition);

        for (N node : nodesToResolve) {
            Point position = new Point ();
            // TODO - resolve others
            resultPosition.put (node, position);
        }

        for (Map.Entry<N, Point> entry : resultPosition.entrySet ())
            scene.findWidget (entry.getKey ()).setPreferredLocation (entry.getValue ());
    }

    /**
     * Collects a collection of children nodes of a specified node.
     * @param node the node
     * @return the collection of children
     */
    protected Collection<N> resolveChildren (N node) {
        Collection<E> edges = scene.findNodeEdges (node, true, false);
        HashSet<N> nodes = new HashSet<N> ();
        for (E edge : edges)
            nodes.add (scene.getEdgeTarget (edge));
        return nodes;
    }

    private class Node {

        private N node;
        private ArrayList<Node> children;

        private Rectangle relativeBounds;
        private int space;
        private int totalSpace;
        private Point point;

        private Node (N node, HashSet<N> loadedSet) {
            this.node = node;
            loadedSet.add (node);

            Collection<N> list = resolveChildren (node);
            children = new ArrayList<Node> ();
            for (N child : list)
                if (! loadedSet.contains (child))
                    children.add (new Node (child, loadedSet));
        }

        private int allocateHorizontally () {
            Widget widget = scene.findWidget (node);
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += horizontalGap;
                space += children.get (i).allocateHorizontally ();
            }
            totalSpace = Math.max (space, relativeBounds.width);
            return totalSpace;
        }

        private void resolveVertically (int x, int y) {
            point = new Point (x + totalSpace / 2, y - relativeBounds.y);
            x += (totalSpace - space) / 2;
            y += relativeBounds.height + verticalGap;
            for (Node child : children) {
                child.resolveVertically (x, y);
                x += child.totalSpace + horizontalGap;
            }
        }

        private int allocateVertically () {
            Widget widget = scene.findWidget (node);
            widget.getLayout ().layout (widget);
            relativeBounds = widget.getPreferredBounds ();
            space = 0;
            for (int i = 0; i < children.size (); i++) {
                if (i > 0)
                    space += verticalGap;
                space += children.get (i).allocateVertically ();
            }
            totalSpace = Math.max (space, relativeBounds.height);
            return totalSpace;
        }

        private void resolveHorizontally (int x, int y) {
            point = new Point (x - relativeBounds.x, y + totalSpace / 2);
            x += relativeBounds.width + horizontalGap;
            y += (totalSpace - space) / 2;
            for (Node child : children) {
                child.resolveHorizontally (x, y);
                y += child.totalSpace + verticalGap;
            }
        }

        private void upload (HashMap<N, Point> result) {
            result.put (node, point);
            for (Node child : children)
                child.upload (result);
        }
    }
    
    @Override
    protected void performGraphLayout(UniversalGraph graph) {
        Collection<N> allNodes = scene.getNodes ();
        ArrayList<N> rootNodes = new ArrayList<N>() ;
        for (N node: allNodes) {
            Collection<E> inputEdges = scene.findNodeEdges(node, false, true) ;
            if (inputEdges==null || inputEdges.size()==0) {
                rootNodes.add(node);
            }
        }
        
        for (N rootNode: rootNodes) {
            this.layout(rootNode);
        }
    }

    @Override
    protected void performNodesLayout(UniversalGraph graph, Collection nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
