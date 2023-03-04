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
public class TreeGraphLayout<N, E> extends GraphLayout<N, E> {

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
    protected void performGraphLayout(UniversalGraph<N, E> graph) {
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
    protected void performNodesLayout(UniversalGraph<N,E> graph, Collection<N> nodes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
