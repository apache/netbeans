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
package org.netbeans.modules.visual.graph.layout.orthogonalsupport;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge.Direction;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;

public class DirectionalGraph {

    private OrthogonalRepresentation or;
    private Direction direction;
    private Direction barDirection;
    private ArrayList<Bar> bars;
    private Map<Vertex, Bar> barMap;
    private Set<Edge> visitedEdges;
    private Map<Vertex, Edge> forwardEdges;
    private Map<Vertex, Edge> reverseEdges;

    /**
     * 
     * @param or
     * @param direction
     * @return
     */
    public static DirectionalGraph createGraph(OrthogonalRepresentation or,
            Direction direction) {
        DirectionalGraph graph = new DirectionalGraph(or, direction);
        graph.createGraph();

        return graph;
    }

    /**
     * 
     * @param or
     * @param direction
     */
    private DirectionalGraph(OrthogonalRepresentation or, Direction direction) {
        this.or = or;
        barMap = new HashMap<Vertex, Bar>();
        bars = new ArrayList<Bar>();
        visitedEdges = new HashSet<Edge>();
        forwardEdges = new HashMap<Vertex, Edge>();
        reverseEdges = new HashMap<Vertex, Edge>();

        this.direction = direction;

        if (direction == Direction.HORIZONTAL) {
            barDirection = Direction.VERTICAL;
        } else {
            barDirection = Direction.HORIZONTAL;
        }

    }

    /**
     * 
     */
    private void createGraph() {
        Vertex cornerVertex = getCornerVertex();
        Collection<Vertex> rootVertices = getRootVertices(cornerVertex);
        assignEdgeDirections(rootVertices, new HashSet<Vertex>());

        visitedEdges.clear();
        createBar(cornerVertex, null);

        computeTopologicalNumbering();
    }

    /**
     * RESOLVE: Need to figure out a better way to assign directions
     * to each edges. It is kludge at best.
     * @param vertices
     * @param skippedVertices
     */
    private void assignEdgeDirections(Collection<Vertex> vertices,
            Set<Vertex> skippedVertices) {
        Collection<Vertex> oppositeVertices = new ArrayList<Vertex>();

        for (Vertex v : vertices) {
            Collection<Edge> edges = v.getEdges();
            for (Edge e : edges) {
                if (e.getDirection() == direction &&
                        reverseEdges.get(v) != e) {
                    forwardEdges.put(v, e);
                    Vertex w = e.getOppositeVertex(v);
                    reverseEdges.put(w, e);
                    oppositeVertices.add(w);
                }
            }
        }

        if (!oppositeVertices.isEmpty()) {
            HashSet<Vertex> additionalVertices = new LinkedHashSet<Vertex>();

            for (Vertex v : oppositeVertices) {
                Collection<Vertex> prevVertices = new ArrayList<Vertex>();
                Set<Vertex> parentVertices = computeParentVertices(v);

                Collection<Edge> edges = v.getEdges();
                for (Edge e : edges) {
                    if (e.getDirection() == barDirection) {
                        Vertex currentVertex = e.getOppositeVertex(v);
                        Edge prevEdge = e;
                        boolean terminate = false;

                        while (!oppositeVertices.contains(currentVertex) &&
                                !additionalVertices.contains(currentVertex) &&
                                !skippedVertices.contains(currentVertex)) {
                            if (!containsReverseEdge(currentVertex, new HashSet<Edge>()) &&
                                    !reachableToParentVertex(currentVertex, parentVertices,
                                    new HashSet<Edge>())) {
                                additionalVertices.add(currentVertex);
                                prevVertices.add(currentVertex);
                                boolean found = false;

                                Collection<Edge> nextEdges = currentVertex.getEdges();
                                for (Edge nextEdge : nextEdges) {
                                    if (nextEdge != prevEdge &&
                                            nextEdge.getDirection() == barDirection) {
                                        currentVertex = nextEdge.getOppositeVertex(currentVertex);
                                        prevEdge = nextEdge;
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                    break;
                                }
                            } else {
                                additionalVertices.removeAll(prevVertices);
                                skippedVertices.add(v);
                                terminate = true;
                                break;
                            }
                        }

                        if (terminate) {
                            break;
                        }
                    }
                }
            }

            oppositeVertices.addAll(additionalVertices);
            assignEdgeDirections(oppositeVertices, skippedVertices);
        }
    }

    /**
     * 
     * @param v
     * @return
     */
    private Set<Vertex> computeParentVertices(Vertex v) {
        HashSet<Vertex> parentVertices = new HashSet<Vertex>();

        Edge reverseEdge = reverseEdges.get(v);
        Vertex currentVertex = v;

        while (reverseEdge != null) {
            Vertex parentVertex = reverseEdge.getOppositeVertex(currentVertex);
            parentVertices.add(parentVertex);
            currentVertex = parentVertex;
            reverseEdge = reverseEdges.get(currentVertex);
        }

        return parentVertices;
    }

    /**
     * 
     * @param v
     * @param visitedEdges
     * @return
     */
    private boolean containsReverseEdge(Vertex v, Set<Edge> visitedEdges) {

        Collection<Edge> edges = v.getEdges();
        for (Edge e : edges) {
            if (e.getDirection() == direction &&
                    !visitedEdges.contains(e)) {
                //Logger.log (1, "e = " + e);
                visitedEdges.add(e);
                if (reverseEdges.get(v) == e) {
                    return true;
                } else if (containsReverseEdge(e.getOppositeVertex(v), visitedEdges)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 
     * @param v
     * @param parentVertices
     * @param visitedEdges
     * @return
     */
    private boolean reachableToParentVertex(Vertex v, Set<Vertex> parentVertices,
            Set<Edge> visitedEdges) {

        Collection<Edge> edges = v.getEdges();
        for (Edge e : edges) {
            if (e.getDirection() == direction && !visitedEdges.contains(e)) {
                visitedEdges.add(e);
                Vertex w = e.getOppositeVertex(v);

                if (checkSideway(w, parentVertices, visitedEdges) ||
                        reachableToParentVertex(w, parentVertices, visitedEdges)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 
     * @param v
     * @param parentVertices
     * @param visitedEdges
     * @return
     */
    private boolean checkSideway(Vertex v, Set<Vertex> parentVertices,
            Set<Edge> visitedEdges) {

        Collection<Edge> edges = v.getEdges();
        for (Edge e : edges) {
            if (e.getDirection() == barDirection &&
                    !visitedEdges.contains(e)) {
                visitedEdges.add(e);
                Vertex currentVertex = e.getOppositeVertex(v);

                while (true) {
                    if (parentVertices.contains(currentVertex)) {
                        return true;
                    }

                    Edge nextEdge = null;
                    Collection<Edge> edges2 = currentVertex.getEdges();
                    for (Edge ce : edges2) {
                        if (ce.getDirection() == barDirection &&
                                !visitedEdges.contains(ce)) {
                            visitedEdges.add(ce);
                            nextEdge = ce;
                            break;
                        }
                    }

                    if (nextEdge != null) {
                        currentVertex = nextEdge.getOppositeVertex(currentVertex);
                    } else {
                        break;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 
     * @param vertex
     * @param parentBar
     * @return
     */
    private Bar createBar(Vertex vertex, Bar parentBar) {
        Bar bar = barMap.get(vertex);

        if (bar == null) {
            bar = new Bar(barDirection);
            bars.add(bar);
            bar.addVertex(vertex);
            barMap.put(vertex, bar);

            Collection<Edge> edges = vertex.getEdges();
            for (Edge edge : edges) {
                if (edge.getDirection() == barDirection) {
                    Vertex currentVertex = edge.getOppositeVertex(vertex);
                    Edge prevEdge = edge;

                    while (true) {
                        bar.addVertex(currentVertex);
                        barMap.put(currentVertex, bar);
                        Edge foundEdge = null;

                        Collection<Edge> edges2 = currentVertex.getEdges();
                        for (Edge e : edges2) {
                            if (e != prevEdge &&
                                    e.getDirection() == barDirection) {
                                foundEdge = e;
                                break;
                            }
                        }

                        if (foundEdge == null) {
                            break;
                        }

                        prevEdge = foundEdge;
                        currentVertex = foundEdge.getOppositeVertex(currentVertex);
                    }
                }
            }

            for (Vertex v : bar.getVertices()) {
                Collection<Edge> edges2 = v.getEdges();
                for (Edge e : edges2) {
                    if (e.getDirection() == direction &&
                            forwardEdges.get(v) == e &&
                            !visitedEdges.contains(e)) {
                        visitedEdges.add(e);
                        Bar neighbor = createBar(e.getOppositeVertex(v), bar);
                        if (neighbor != parentBar) {
                            bar.addNeighbor(neighbor);
                        }
                    }
                }
            }
        }

        return bar;
    }

    /**
     * 
     * @return
     */
    public Collection<Bar> getBars() {
        return bars;
    }

    /**
     * 
     * @param cornerVertex
     * @return
     */
    private Collection<Vertex> getRootVertices(Vertex cornerVertex) {
        ArrayList<Vertex> rootVertices = new ArrayList<Vertex>();

        Vertex currentVertex = cornerVertex;
        Edge prevEdge = null;
        rootVertices.add(cornerVertex);

        while (true) {
            Edge foundEdge = null;

            Collection<Edge> edges = currentVertex.getEdges();
            for (Edge e : edges) {
                if (e != prevEdge &&
                        e.getDirection() == barDirection) {
                    foundEdge = e;
                    break;
                }
            }

            if (foundEdge == null) {
                break;
            }
            prevEdge = foundEdge;
            currentVertex = foundEdge.getOppositeVertex(currentVertex);
            rootVertices.add(currentVertex);
        }

        return rootVertices;
    }

    /**
     * 
     * @return
     */
    private Vertex getCornerVertex() {
        Vertex cornerVertex = or.getCornerVertex();

        if (cornerVertex != null) {
            return cornerVertex;
        }

        EmbeddedPlanarGraph epg = or.getOriginalGraph();
        Face outerFace = epg.getOuterFace();
        List<Vertex> vertices = outerFace.getVertices();

        Vertex candidate1 = null;
        Vertex candidate2 = null;
        Vertex candidate3 = null;

        for (Vertex v : vertices) {
            int hEdgeCount = 0;
            int vEdgeCount = 0;

            Collection<Edge> edges = v.getEdges();
            for (Edge e : edges) {
                Direction direction = e.getDirection();

                if (direction == Direction.HORIZONTAL) {
                    hEdgeCount++;
                } else {
                    vEdgeCount++;
                }
            }

            if (hEdgeCount == 1 && vEdgeCount == 1) {
                if (candidate1 == null) {
                    candidate1 = v;
                }
            } else if ((hEdgeCount == 1 && vEdgeCount == 0 &&
                    direction == Direction.HORIZONTAL) ||
                    (hEdgeCount == 0 && vEdgeCount == 1 &&
                    direction == Direction.VERTICAL)) {
                if (candidate2 == null) {
                    candidate2 = v;
                }
            } else if (hEdgeCount == 1 && vEdgeCount == 0 ||
                    hEdgeCount == 0 && vEdgeCount == 1) {
                candidate3 = v;
            }

        }

        if (candidate1 != null) {
            return candidate1;
        }

        if (candidate2 != null) {
            return candidate2;
        }

        if (candidate3 != null) {
            return candidate3;
        }

        return null;
    }

    public void computeTopologicalNumbering() {
        int offset = 0;

        if (or.getCornerVertex() != null) {
            offset = -1;
        }

        Bar sourceBar = bars.get(0);
        sourceBar.setNumber(offset);

        for (Bar bar : bars) {
            if (bar == sourceBar) {
                continue;
            }

            int length = computeLongestPathLength(sourceBar, bar);
            bar.setNumber(length + offset);
        }
    }

    /**
     * TODO: Need to optimize.
     * @param sourceBar
     * @param destinationBar
     * @return
     */
    private int computeLongestPathLength(Bar sourceBar, Bar destinationBar) {

        if (sourceBar == destinationBar) {
            return 0;
        }

        int maxLength = -1;

        for (Bar n : sourceBar.getNeighbors()) {
            if (n.equals(sourceBar)) {
                continue;
            }
            int length = computeLongestPathLength(n, destinationBar);
            if (length == -1) {
                continue;
            }

            length++;
            if (length > maxLength) {
                maxLength = length;
            }
        }

        return maxLength;
    }

    /**
     * 
     * @return
     */
    public String toString() {
        String s = direction + " Graph:\n";

        for (Bar b : getBars()) {
            s = s + b;
        }

        return s;
    }

    /**
     * 
     */
    public static class Bar implements Comparable {

        private Collection<Vertex> vertices;
        private Collection<Bar> neighbors;
        private Direction direction;
        private int number;

        /**
         * 
         * @param direction
         */
        public Bar(Direction direction) {
            this.direction = direction;
            vertices = new ArrayList<Vertex>();
            neighbors = new HashSet<Bar>();
        }

        /**
         * 
         * @param vertex
         */
        public void addVertex(Vertex vertex) {
            vertices.add(vertex);
            Dimension d = vertex.getSize();

            checkMaximumSize(d);
        }

        /**
         * 
         * @param d
         */
        private void checkMaximumSize(Dimension d) {
            if (direction.equals(Direction.HORIZONTAL)) {
            } else {
            }
        }

        /**
         * 
         */
        public void resolveGrid() {
        }

        /**
         * 
         * @return
         */
        public Collection<Vertex> getVertices() {
            return vertices;
        }

        /**
         * 
         * @param neighbor
         */
        public void addNeighbor(Bar neighbor) {
            neighbors.add(neighbor);
        }

        /**
         * 
         * @return
         */
        public Collection<Bar> getNeighbors() {
            return neighbors;
        }

        /**
         * 
         * @param number
         */
        public void setNumber(int number) {
            this.number = number;
        }

        /**
         * 
         * @return
         */
        public int getNumber() {
            return number;
        }

        /**
         * 
         * @return
         */
        public String toString() {
            String s = "\t" + direction + " Bar:\n";
            s = s + "\t\tNumber = " + number + "\n";
            s = s + "\t\tVertices:\n";
            for (Vertex v : vertices) {
                s = s + "\t\t\t" + v + "\n";
            }
            s = s + "\t\tNeighbors =" + neighbors.size() + "\n";

            for (Bar n : neighbors) {
                s = s + "\t\t\t Bar " + n.getNumber() + "\n";
            }

            return s;
        }

        /**
         * 
         * @param o
         * @return
         */
        public int compareTo(Object o) {
            if (!(o instanceof Bar)) {
                return 0;
            }

            Bar b = (Bar) o;
            return number > b.getNumber() ? 1 : -1;

        }
    }
}
