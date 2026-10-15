/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.visual.graph.layout;

import org.netbeans.api.visual.graph.layout.TreeGraphLayoutAlignment;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.Widget;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author David Kaspar
 * @author Erhard Pointl
 */
public final class TreeGraphLayout<N,E> extends GraphLayout<N,E> {

    private int originX;
    private int originY;
    private int verticalGap;
    private int horizontalGap;
    private boolean vertical;
    /** Indicates whether the gap between two nodes of the same level should be minimized. */
    private boolean minimizeGap;
    /** Inicates where the nodes should be aligned within the level. */
    private TreeGraphLayoutAlignment alignment;

    private N rootNode;

     /**
     * Constructor
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     */
    public TreeGraphLayout (int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        this.originX = originX;
        this.originY = originY;
        this.verticalGap = verticalGap;
        this.horizontalGap = horizontalGap;
        this.vertical = vertical;
        this.minimizeGap = false;
        this.alignment = TreeGraphLayoutAlignment.TOP;
    }

    /**
     * Constructor
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @param minimizeGap if true, then minimize the gap between cells; if false do the normal tree layout.
     * @param alignment alignment the alignment of the nodes in their level. Choose wheter {@code TreeGraphLayout.Alignment.TOP}, {@code TreeGraphLayout.Alignment.CENTER} or {@code TreeGraphLayout.Alignment.BOTTOM}
     * @since 2.25
     */
    public TreeGraphLayout(int originX, int originY, int verticalGap, int horizontalGap, boolean vertical, boolean minimizeGap, TreeGraphLayoutAlignment alignment) {
        this(originX, originY, verticalGap, horizontalGap, vertical);
        this.minimizeGap = minimizeGap;
        this.alignment = alignment;
    }

    public void setRootNode (N rootNode) {
        this.rootNode = rootNode;
    }

    public void setProperties (int originX, int originY, int verticalGap, int horizontalGap, boolean vertical) {
        if (vertical) {
            this.originX = originX;
            this.originY = originY;
            this.verticalGap = verticalGap;
            this.horizontalGap = horizontalGap;
         } else {
            this.originX = originY;
            this.originY = originX;
            this.verticalGap = horizontalGap;
            this.horizontalGap = verticalGap;
        }
        this.vertical = vertical;
        this.minimizeGap = false;
        this.alignment = TreeGraphLayoutAlignment.TOP;
    }

    /**
     * Method to set the properties of this {@link TreeGraphLayout}.
     * @param originX the x-axis origin
     * @param originY the y-axis origin
     * @param verticalGap the vertical gap between cells
     * @param horizontalGap the horizontal gap between cells
     * @param vertical if true, then layout organizes the graph vertically; if false, then horizontally
     * @param minimizeGap if true, then minimize the gap between cells; if false do the normal tree layout.
     * @param alignment alignment the alignment of the nodes in their level. Choose wheter {@code TreeGraphLayout.Alignment.TOP}, {@code TreeGraphLayout.Alignment.CENTER} or {@code TreeGraphLayout.Alignment.BOTTOM}
     * @since 2.25
     */
    public void setProperties(int originX, int originY, int verticalGap, int horizontalGap, boolean vertical, boolean minimizeGap, TreeGraphLayoutAlignment alignment) {
        this.setProperties(originX, originY, verticalGap, horizontalGap, vertical);
        this.minimizeGap = minimizeGap;
        this.alignment = alignment;
    }

    @Override
    protected void performGraphLayout (UniversalGraph<N, E> graph) {
        if (rootNode == null)
            return;
        Collection<N> allNodes = graph.getNodes ();
        if (! allNodes.contains (rootNode))
            return;
        ArrayList<N> nodesToResolve = new ArrayList<N> (allNodes);

        HashSet<N> loadedSet = new HashSet<N> ();
        Node root = new Node (graph, rootNode, loadedSet);
        nodesToResolve.removeAll (loadedSet);

        Map<Integer, Integer> map = root.getMaxSpaceForEveryLevel(graph);

        List<Node.LeftRight> envelope = root.layout(originX, originY, map, 0);

        // correction of originX, if needed
        int moveDistance = Integer.MIN_VALUE;
        // get the most left position and determine the distance to move
        for (Node.LeftRight leftRight : envelope) {
            if (leftRight.getLeft() < originX && originX - leftRight.getLeft() > moveDistance) {
                moveDistance = originX - leftRight.getLeft();
            }
        }

        if (moveDistance == Integer.MIN_VALUE) {
            moveDistance = 0;
        }

        if (!vertical) {
            root.invert(moveDistance);
        } else {
            root.relativeBoundsCorrectionX(moveDistance);
        }

        final HashMap<N, Point> resultPosition = new HashMap<N, Point> ();
        root.upload (resultPosition);

        for (N node : nodesToResolve) {
            Point position = new Point ();
            // TODO - resolve others
            resultPosition.put (node, position);
        }

        for (Map.Entry<N, Point> entry : resultPosition.entrySet ())
            setResolvedNodeLocation (graph, entry.getKey (), entry.getValue ());
    }

    @Override
    protected void performNodesLayout (UniversalGraph<N, E> universalGraph, Collection<N> nodes) {
        throw new UnsupportedOperationException (); // TODO
    }

    private class Node {

        private N node;
        private ArrayList<Node> children;

        private Rectangle relativeBounds;
        private int space;
        private Point point;

        private Node (UniversalGraph<N, E> graph, N node, HashSet<N> loadedSet) {
            this.node = node;
            loadedSet.add (node);

            children = new ArrayList<Node> ();
            for (E edge: graph.findNodeEdges (node, true, false)) {
                N child = graph.getEdgeTarget (edge);
                if (child != null  &&  ! loadedSet.contains (child))
                    children.add (new Node (graph, child, loadedSet));
            }
        }

        /**
         * This class represents an element of the envelope of a (sub)tree.
         * @since 2.25
         */
        private class LeftRight {

            /** the most left coordinate of this element of the envelope. */
            private int left;
            /** hte most right coordinate of this element of the envelope. */
            private int right;

            /**
             * Constructor
             *
             * @param left the most left coordinate of this {@link LeftRight}.
             * @param right the most right coordinate of this {@link LeftRight}.
             * @since 2.25
             */
            public LeftRight(int left, int right) {
                this.left = left;
                this.right = right;
            }

            /**
             * Getter of the most left coordinate of this {@link LeftRight}.
             * @return the most left coordinate of this {@link LeftRight}.
             * @since 2.25
             */
            public int getLeft() {
                return left;
            }

            /**
             * Getter of the most right coordinate of this {@link LeftRight}.
             * @return the most right coordinate of this {@link LeftRight}.
             * @since 2.25
             */
            public int getRight() {
                return right;
            }

            /**
             * Setter of the most left coordinate of this {@link LeftRight}.
             * @param left the most left coordinate to set.
             * @since 2.25
             */
            public void setLeft(int left) {
                this.left = left;
            }

            /**
             * Setter of the most right coordinate of this {@link LeftRight}.
             * @param right the most right coordinate to set.
             * @since 2.25
             */
            public void setRight(int right) {
                this.right = right;
            }
        }

        /**
         * This method calculates the maximal space for every level and returns the result as a {@link Map} containing the level (which is equal to the depth of the tree) and the height/width of the biggest node in this level.
         * @param graph
         * @return the result as a {@link Map} containing the level (which is equal to the depth of the tree) and the height/width of the biggest node in this level.
         * @since 2.25
         */
        private Map<Integer, Integer> getMaxSpaceForEveryLevel(UniversalGraph<N, E> graph) {
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            calculateMaxSpace(graph, map, 0);
            return map;
        }

        /**
         * Method to determine the maximal space (horizontaly or verticaly) of each level in the tree.
         * @param map the {@link Map} to which the maximal space for each level should be stored.
         * @param lvl the level to analyse.
         * @return the result of the maximal space calculation for the given level and the sublevels.
         * @since 2.25
         */
        private Map<Integer, Integer> calculateMaxSpace(UniversalGraph<N, E> graph, Map<Integer, Integer> map, int lvl) {

            Widget widget = graph.getScene().findWidget(node);
            widget.getLayout().layout(widget);
            relativeBounds = widget.getPreferredBounds();

            if (vertical) {
                space = relativeBounds.height;
            } else {
                space = relativeBounds.width;
            }

            if (map.get(lvl) != null) {
                // lvl is in list, but height is greater than the old one.
                if (map.get(lvl) < space) {
                    map.put(lvl, space);
                }
            } else {
                // lvl isn't in the map right now.
                map.put(lvl, space);
            }

            lvl++;

            // do iteration over all children of the current node and calculate
            // the maxSpace
            for (Node n : children) {
                n.calculateMaxSpace(graph, map, lvl);
            }
            return map;
        }

        /**
         * Method which is doing the layout based on the given x - position, y - position, map of levels and their maximal used space and the actual level.
         * @param x the x position for doing the layout.
         * @param y the y position for doing the layout.
         * @param lvl the auctual level for doing the layout.
         * @return the envelope of the subtrees for which the layout is already done.
         * @since 2.25
         */
        private List<LeftRight> layout(int x, int y, Map<Integer, Integer> map, int lvl) {

            List<LeftRight> leftright = null;

            // if this node is a leaf (has no childs) ==> place the node at x,y and x and x + width to the envelope.
            if (children.size() == 0) {
                leftright = new ArrayList<LeftRight>();

                // do the horizontal alignment
                y = doHorizontalPlacementInLevel(y, map, lvl);

                if (vertical) {
                    y = y - relativeBounds.y;
                    leftright.add(new LeftRight(x, x + relativeBounds.width));
                } else {
                    y = y - relativeBounds.x;
                    leftright.add(new LeftRight(x, x + relativeBounds.height));
                }

                point = new Point(x, y);
                return leftright;
            }

            lvl++;

            for (int i = 0; i < children.size(); i++) {
                if (i == 0) {
                    leftright = children.get(i).layout(x, (y + map.get(lvl - 1) + verticalGap), map, lvl);
                } else {
                    List<LeftRight> secound = children.get(i).layout(x, (y + map.get(lvl - 1) + verticalGap), map, lvl);

                    int leftlength = leftright.size();
                    int rightlength = secound.size();

                    int diff = rightlength - leftlength;

                    // Variable used for calculation of the distance to move the right subtree.
                    int moveDist = Integer.MIN_VALUE;

                    // Try to minimize the gap and move subtrees as close as possible to each other.
                    if (minimizeGap) {
                        // Caluculate the max distance to move
                        for (int k = leftlength - 1; k >= 0; k--) {
                            if (k + diff >= 0 && k >= 0) {
                                int tmpmaxoverlap = leftright.get(k).right - secound.get(k + diff).left;
                                if (tmpmaxoverlap > moveDist) {
                                    moveDist = tmpmaxoverlap;
                                }
                            }
                        }
                    } else {
                        int maxRight = Integer.MIN_VALUE;

                        for (LeftRight l : leftright) {
                            maxRight = Math.max(maxRight, l.right);
                        }

                        int minLeft = Integer.MAX_VALUE;

                        for (LeftRight s : secound) {
                            minLeft = Math.min(minLeft, s.left);
                        }
                        moveDist = maxRight - minLeft;
                    }

                    if (moveDist > Integer.MIN_VALUE) {
                        int dx = moveDist + horizontalGap;
                        children.get(i).point.x += dx;
                        children.get(i).moveChildrenHorizontally(dx);
                    }

                    // update moved tree envelope
                    for (LeftRight lr : secound) {
                        lr.setLeft(lr.getLeft() + moveDist + horizontalGap);
                        lr.setRight(lr.getRight() + moveDist + horizontalGap);
                    }

                    // store the overall envelope of leftright and secound in leftright
                    for (int j = rightlength - 1; j >= 0; j--) {
                        // leftright wasn't as long as secound, so these elements have to be added to the envelope
                        if (j < secound.size() - leftright.size()) {
                            leftright.add(0, secound.get(j));
                        } else if ((j - diff) >= 0 && j >= 0) {
                            // if the left envelope of secound is smaller than the left envelope of leftright, might never happen.
                            if (leftright.get(j - diff).left > secound.get(j).left) {
                                leftright.get(j - diff).setLeft(secound.get(j).left);
                            }
                            // if the right envelope of secound is greater than the right envelope of leftright, might happen every time when a subtree was added by a move to the right.
                            if (leftright.get(j - diff).right < secound.get(j).right) {
                                leftright.get(j - diff).setRight(secound.get(j).right);
                            }
                        }
                    }
                }
            }

            lvl--;

            // do the horizontal alignment
            int yAlignment = doHorizontalPlacementInLevel(y, map, lvl);

            if (minimizeGap) {
                if (vertical) {
                    point = new Point(((leftright.get(leftright.size() - 1).right + leftright.get(leftright.size() - 1).left) / 2) - (relativeBounds.width / 2), yAlignment - relativeBounds.y);
                    leftright.add(new LeftRight(point.x, point.x + relativeBounds.width));
                } else {
                    point = new Point(((leftright.get(leftright.size() - 1).right + leftright.get(leftright.size() - 1).left) / 2) - (relativeBounds.height / 2), yAlignment - relativeBounds.x);
                    leftright.add(new LeftRight(point.x, point.x + relativeBounds.height));
                }
            } else {
                int leftMin = Integer.MAX_VALUE;
                int rightMax = Integer.MIN_VALUE;
                for (LeftRight l : leftright) {
                    leftMin = Math.min(leftMin, l.left);
                    rightMax = Math.max(rightMax, l.right);
                }

                assert leftMin != Integer.MAX_VALUE : "whether envelope was empty or it had no valid leftMin value!";
                assert rightMax != Integer.MIN_VALUE : "whether envelope was empty or it had no vaild rightMin value!";

                if (vertical) {
                    point = new Point(((leftMin + rightMax) / 2) - (relativeBounds.width / 2), yAlignment - relativeBounds.y);
                    leftright.add(new LeftRight(point.x, point.x + relativeBounds.width));
                } else {
                    point = new Point(((leftMin + rightMax) / 2) - (relativeBounds.height / 2), yAlignment - relativeBounds.x);
                    leftright.add(new LeftRight(point.x, point.x + relativeBounds.height));
                }
            }

            return leftright;
        }

        /**
         * Method to move the children of the actual {@link Node} horizontally
         * by the given distance dx.
         * @param dx the distance to move the children horizontally.
         * @since 2.25
         */
        private void moveChildrenHorizontally(int dx) {
            for (Node n : children) {
                n.point.x += dx;
                n.moveChildrenHorizontally(dx);
            }
        }

        /**
         * Doing the alignment whether on TOP, in the CENTER or at the BOTTOM of the level.
         * @param y the top y coordinate of the level.
         * @param map the map of levels and their maximal spaces.
         * @param lvl the level for which the alignment should be done.
         * @return the y coordinate of the aligned node.
         * @since 2.25
         */
        private int doHorizontalPlacementInLevel(int y, Map<Integer, Integer> map, int lvl) {
            int yAlignment = 0;

            // do the alignment
            if (alignment == TreeGraphLayoutAlignment.TOP) {
                // nothing to do
                yAlignment = y;
            } else if (alignment == TreeGraphLayoutAlignment.CENTER) {
                // place the widget in the center of the max.
                yAlignment = y + ((map.get(lvl) - space) / 2);
            } else if (alignment == TreeGraphLayoutAlignment.BOTTOM) {
                yAlignment = y + (map.get(lvl) - space);
            }
            return yAlignment;
        }

        /**
         * Method to invert x and y. Also relativeBounds correction is done and a moveDistance (x-axis) can be given.
         * @param moveDistance the distance to move the subtree (x-axis).
         * @since 2.25
         */
        private void invert(int moveDistance) {
            int tmpx = point.x + moveDistance;
            point.x = point.y;
            point.y = tmpx - relativeBounds.y;
            for (Node n : children) {
                n.invert(moveDistance);
            }
        }

        /**
         * Method to do a relativeBoundsCorrection (x-axis) and also to move the Node for a given distance (also x-axis).
         * @param moveDistance the distance to move the subtree (x-axis).
         * @since 2.25
         */
        private void relativeBoundsCorrectionX(int moveDistance) {
            point.x = point.x - relativeBounds.x + moveDistance;
            for (Node n : children) {
                n.relativeBoundsCorrectionX(moveDistance);
            }
        }

        private void upload (HashMap<N, Point> result) {
            result.put (node, point);
            for (Node child : children)
                child.upload (result);
        }
    }

}
