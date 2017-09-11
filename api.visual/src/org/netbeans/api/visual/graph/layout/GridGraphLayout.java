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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectScene;

import java.awt.*;
import java.util.*;

/**
 * This class represents a graph-oriented layout which organizes nodes into a grid.
 * <p>
 * You can call setChecker method to enable checker style.
 * If the parameter is true, that the layout uses only half of nodes on a grid like a single color on a chess board.
 * <p>
 * You can define horizontal and vertical gaps between nodes using the setGaps method.
 *
 * @author David Kaspar
 */
public final class GridGraphLayout<N,E> extends GraphLayout<N,E> {

    private boolean checker = false;
    private int horizontalGap = 64;
    private int verticalGap = 64;

    /**
     * Creates a grid graph layout.
     */
    public GridGraphLayout () {
    }

    /**
     * Sets a checker style.
     * @param checker if true, then checker style is enabled and nodes are organized like a single color on a chess board.
     * @return the instance of the grid graph layout
     */
    public GridGraphLayout<N,E> setChecker (boolean checker) {
        this.checker = checker;
        return this;
    }

    /**
     * Sets horizontal and vertical gap between nodes.
     * @param horizontalGap the horizontal gap
     * @param verticalGap the vertical gap
     * @return the instance of the grid graph layout
     */
    public GridGraphLayout<N,E> setGaps (int horizontalGap, int verticalGap) {
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
        return this;
    }

    /**
     * Performs the grid graph layout on an universal graph.
     * @param graph the universal graph
     */
    protected void performGraphLayout (UniversalGraph<N, E> graph) {
        Collection<N> allNodes = graph.getNodes ();
        HashSet<N> unresolvedNodes = new HashSet<N> (allNodes);

        final HashMap<N, Collection<N>> node2connected = new HashMap<N,Collection<N>> ();
        for (N node : unresolvedNodes) {
            HashSet<N> connected = new HashSet<N> ();
            for (E edge : graph.findNodeEdges (node, true, false)) {
                N n = graph.getEdgeTarget (edge);
                if (n != null)
                    connected.add (n);
            }
            for (E edge : graph.findNodeEdges (node, false, true)) {
                N n = graph.getEdgeSource (edge);
                if (n != null)
                    connected.add (n);
            }
            node2connected.put (node, connected);
        }

        LinkedList<N> queue = new LinkedList<N> ();
        HashMap<N, Point> node2grid = new HashMap<N, Point> ();
        Rectangle gridBounds = new Rectangle ();

        for (;;) {
            N node = queue.isEmpty () ? findNodeWithMaxEdges (unresolvedNodes, node2connected) : queue.poll ();
            if (node == null)
                break;
            unresolvedNodes.remove (node);
            Point center = node2grid.get (node);
            if (center == null) {
                center = findCenter (node2grid);
                node2grid.put (node, center);
                gridBounds.add (center);
            }
            Point index = new Point ();
            ArrayList<N> connected = new ArrayList<N> (node2connected.get (node));
            Collections.sort (connected, new Comparator<N>() {
                public int compare (N node1, N node2) {
                    return node2connected.get (node1).size () - node2connected.get (node2).size ();
                }
            });

            for (N conn : connected) {
                if (unresolvedNodes.contains (conn))
                    queue.offer (conn);
                if (node2grid.containsKey (conn))
                    continue;
                Point grid = resolvePoint (node2grid, center, index);
                node2grid.put (conn, grid);
                gridBounds.add (grid);
            }
        }

        ObjectScene scene = graph.getScene ();
        int[] xAxis = new int[gridBounds.width + 1];
        int[] yAxis = new int[gridBounds.height + 1];

        for (N node : allNodes) {
            Widget widget = scene.findWidget (node);
            if (widget == null)
                continue;
            Rectangle bounds = widget.getBounds ();
            if (bounds == null)
                continue;
            Point grid = node2grid.get (node);
            xAxis[grid.x - gridBounds.x] = Math.max (xAxis[grid.x - gridBounds.x], bounds.width);
            yAxis[grid.y - gridBounds.y] = Math.max (yAxis[grid.y - gridBounds.y], bounds.height);
        }

        int pos;

        pos = horizontalGap / 2;
        for (int i = 0; i < xAxis.length; i++) {
            int add = xAxis[i];
            xAxis[i] = pos;
            pos += add + horizontalGap;
        }
        pos = verticalGap / 2;
        for (int i = 0; i < yAxis.length; i++) {
            int add = yAxis[i];
            yAxis[i] = pos;
            pos += add + verticalGap;
        }

        for (N node : allNodes) {
            Widget widget = scene.findWidget (node);
            if (widget == null)
                continue;
            Rectangle bounds = widget.getBounds ();
            if (bounds == null)
                continue;
            Point grid = node2grid.get (node);
            setResolvedNodeLocation (graph, node, new Point (xAxis[grid.x - gridBounds.x] - bounds.x, yAxis[grid.y - gridBounds.y]));
        }
    }

    private <N> Point resolvePoint (HashMap<N, Point> node2grid, Point center, Point index) {
        for (;;) {
            int max = 8 * index.y;
            index.x ++;
            if (index.x >= max) {
                index.y ++;
                index.x -= max;
            }

            Point point = index2point (index);
            point.x += center.x;
            point.y += center.y;

            if (checker)
                if (((point.x + point.y) & 1) != 0)
                    continue;

            if (! isOccupied (node2grid, point))
                return point;
        }
    }

    private <N> Point findCenter (HashMap<N, Point> node2grid) {
        int add = checker ? 2 : 1;
        for (int x = 0; ; x += add) {
            Point point = new Point (x, 0);
            if (! isOccupied (node2grid, point))
                return point;
        }
    }

    private static Point index2point (Point index) {
        int indexPos = index.x;
        int indexLevel = index.y;
        if (indexPos < indexLevel)
            return new Point (indexLevel, indexPos);
        else if (indexPos < 3 * indexLevel)
            return new Point (indexLevel - (indexPos - indexLevel), indexLevel);
        else if (indexPos < 5 * indexLevel)
            return new Point (- indexLevel, indexLevel - (indexPos - 3 * indexLevel));
        else if (indexPos < 7 * indexLevel)
            return new Point ((indexPos - 5 * indexLevel) - indexLevel, - indexLevel);
        else if (indexPos < 8 * indexLevel)
            return new Point (indexLevel, (indexPos - 7 * indexLevel) - indexLevel);
        throw new InternalError ("Index: " + indexPos);
    }

    private static <N> boolean isOccupied (HashMap<N, Point> node2grid, Point point) {
        for (Point p : node2grid.values ()) {
            if (point.x == p.x  &&  point.y == p.y)
                return true;
        }
        return false;
    }

    private static <N> N findNodeWithMaxEdges (HashSet<N> unresolvedNodes, HashMap<N, Collection<N>> node2connected) {
        N bestNode = null;
        int bestCount = Integer.MIN_VALUE;
        for (N node : unresolvedNodes) {
            int i = node2connected.get (node).size ();
            if (i > bestCount) {
                bestNode = node;
                bestCount = i;
            }
        }
        return bestNode;
    }

    /**
     * Should perform nodes layout. Currently unsupported.
     * @param graph the universal graph
     * @param nodes the collection of nodes to resolve
     */
    protected void performNodesLayout (UniversalGraph<N, E> graph, Collection<N> nodes) {
        throw new UnsupportedOperationException (); // TODO
    }

}
