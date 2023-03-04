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
package org.netbeans.modules.visual.graph.layout.orthogonalsupport;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.UniversalGraph;
import org.netbeans.api.visual.widget.Widget;

public class MGraph<N, E> {

    private Collection<N> nodes;
    private Map<N, Vertex<N>> vertexMap;
    private Map<E, Edge<E>> edgeMap;
    private Collection<Vertex<N>> vertices;
    private UniversalGraph<N, E> uGraph = null;
    private GraphScene<N, E> scene = null;

    /**
     * 
     * @param uGraph
     * @param scene
     */
    protected MGraph(UniversalGraph<N, E> uGraph, GraphScene<N, E> scene) {
        this.uGraph = uGraph;
        this.scene = scene;
        this.nodes = uGraph.getNodes();

        vertexMap = new HashMap<>();
        edgeMap = new LinkedHashMap<>();
        vertices = new ArrayList<>();

        DummyVertex.resetCounter();
    }

    /**
     * 
     * @param uGraph
     * @param scene
     * @return
     */
    public static <N, E> MGraph<N, E> createGraph(UniversalGraph<N, E> uGraph, GraphScene<N, E> scene) {
        MGraph<N, E> graph = new MGraph<N, E>(uGraph, scene);
        graph.createGraph();
        return graph;
    }

    /**
     * 
     */
    protected void createGraph() {
        for (N node : nodes) {
            //will create a vertex if one does not exist.
            Vertex<N> v = getVertex(node);
            Widget widget = scene.findWidget(node);
            Rectangle bounds = widget.getBounds();

            Dimension size = new Dimension(bounds.width, bounds.height);

            v.setSize(size);

            //out going edges
            Collection<E> nodeEdges = uGraph.findNodeEdges(node, true, false);

            for (E edge : nodeEdges) {

                N destNode = uGraph.getEdgeTarget(edge);
                Vertex<N> nv = getVertex(destNode);

                // Implicitly preserve the direction of the edge
                Edge<E> e = getEdge(edge, v, nv);
                v.addNeighbor(nv);
                v.addEdge(e);
                nv.addEdge(e);
            }

            //incoming edges
            nodeEdges = uGraph.findNodeEdges(node, false, true);
            for (E edge : nodeEdges) {
                N destNode = uGraph.getEdgeSource(edge);
                Vertex<N> nv = getVertex(destNode);

                // Implicitly preserve the direction of the edge.
                Edge<E> e = getEdge(edge, nv, v);
                v.addNeighbor(nv);
                v.addEdge(e);
                nv.addEdge(e);
            }
        }
    }

    /**
     * 
     * @return
     */
    public Collection<Vertex<N>> getVertices() {
        return Collections.unmodifiableCollection(vertices);
    }

    /**
     * 
     * @return
     */
    public Collection<Edge<E>> getEdges() {
        return Collections.unmodifiableCollection(edgeMap.values());
    }

    /**
     * 
     * @param node
     * @return
     */
    protected Vertex<N> getVertex(N node) {
        Vertex<N> vertex = vertexMap.get(node);

        if (vertex == null) {
            vertex = createVertex(node);
            vertexMap.put(node, vertex);
            vertices.add(vertex);
        }

        return vertex;
    }

    /**
     * 
     * @param node
     * @return
     */
    protected Vertex<N> createVertex(N node) {
        return new Vertex<>(node);
    }

    /**
     * 
     * @param edgeDE
     * @param v
     * @param w
     * @return
     */
    protected Edge<E> getEdge(E edgeDE, Vertex<N> v, Vertex<N> w) {
        Edge<E> edge = edgeMap.get(edgeDE);

        if (edge == null) {
            edge = createEdge(v, w, edgeDE);
            edgeMap.put(edgeDE, edge);
        }

        return edge;
    }

    /**
     * 
     * @param v
     * @param w
     * @param edgeDE
     * @return
     */
    protected Edge<E> createEdge(Vertex<N> v, Vertex<N> w, E edgeDE) {
        return new Edge<>(v, w, edgeDE);
    }

    /**
     * 
     * @param edge
     * @param type
     * @return
     */
    public DummyVertex<N> insertDummyVertex(Edge<?> edge, DummyVertex.Type type) {
        Edge<E> originalEdge;

        if (edge instanceof DummyEdge) {
            @SuppressWarnings("unchecked")
            Edge<E> tmp = (Edge<E>) ((DummyEdge<?>) edge).getOriginalEdge();
            originalEdge = tmp;
        } else {
            @SuppressWarnings("unchecked")
            Edge<E> tmp = (Edge<E>) edge;
            originalEdge = tmp;
        }

        DummyVertex<N> dv = createDummyVertex(originalEdge, type);
        vertices.add(dv);

        Vertex<?> v = edge.getV();
        Vertex<?> w = edge.getW();

        v.removeEdge(edge);
        v.removeNeighbor(w);
        v.addNeighbor(dv);
        dv.addNeighbor(v);
        DummyEdge<E> de = createDummyEdge(v, dv, originalEdge);
        v.addEdge(de);
        dv.addEdge(de);

        w.removeEdge(edge);
        w.removeNeighbor(v);
        w.addNeighbor(dv);
        dv.addNeighbor(w);
        de = createDummyEdge(dv, w, originalEdge);
        w.addEdge(de);
        dv.addEdge(de);

        return dv;
    }

    /**
     * 
     * @param originalEdge
     * @param type
     * @return
     */
    protected DummyVertex<N> createDummyVertex(Edge<?> originalEdge, DummyVertex.Type type) {
        return new DummyVertex<>(originalEdge, type);
    }

    /**
     * 
     * @param v
     * @param w
     * @return
     */
    public DummyEdge<E> addDummyEdge(Vertex<?> v, Vertex<?> w) {
        DummyEdge<E> de = createDummyEdge(v, w, null);
        v.addEdge(de);
        w.addEdge(de);
        v.addNeighbor(w);
        w.addNeighbor(v);

        return de;
    }

    /**
     * 
     * @param v
     * @param w
     * @param originalEdge
     * @return
     */
    protected DummyEdge<E> createDummyEdge(Vertex<?> v, Vertex<?> w,
            Edge<E> originalEdge) {
        return new DummyEdge<>(v, w, originalEdge);
    }

    /**
     * 
     * @param type
     * @return
     */
    public DummyVertex<N> addDummyVertex(DummyVertex.Type type) {
        DummyVertex<N> dv = createDummyVertex(null, type);
        vertices.add(dv);

        return dv;
    }

    /**
     *
     *
     */
    public void printGraph() {
        int count = 0;
        for (Vertex<N> v : getVertices()) {
            Logger.log(1, count + ") vertex = " + v + " (" + v.getX() + ", " + v.getY() + ")");
            count++;
            //out going edges
            N node = v.getNodeDesignElement();
            if (node == null) {
                continue;
            } //if the vertex is a dummy, there is no 
            //node associated with it.

            Collection<E> nodeEdges = uGraph.findNodeEdges(node, true, false);
            Logger.log(1, "\toutgoing edges:");
            for (E edge : nodeEdges) {
                Logger.log(1, "\t\t" + edge);
            }

            nodeEdges = uGraph.findNodeEdges(node, false, true);
            Logger.log(1, "\tincoming edges:");
            for (E edge : nodeEdges) {
                Logger.log(1, "\t\t" + edge);
            }

            Logger.log(1, "\tneighbors:");
            Collection<Vertex<?>> neighbors = v.getNeighbors();
            for (Vertex<?> nv : neighbors) {
                Logger.log(1, "\t\t" + nv);
            }

        }

        Logger.log(1, "------------------\n------------------");
        count = 0;
        for (Edge<E> e : getEdges()) {
            Logger.log(1, count + ") edge = " + e);
            count++;
        }
    }

    /**
     * 
     * @param N
     */
    public static class Vertex<N> {

        private N node;
        private Collection<Vertex<?>> neighbors;
        private Collection<Edge<?>> edges;
        private int number = -1;
        private Object vertexData;
        private float x;
        private float y;
        private Dimension size = null;

        /**
         * 
         * @param node
         */
        public Vertex(N node) {
            this.node = node;
            neighbors = new LinkedHashSet<>();
            edges = new LinkedHashSet<>();
        }

        /**
         * 
         * @return
         */
        public Dimension getSize() {
            return size;
        }

        /**
         * 
         * @param dim
         */
        public void setSize(Dimension dim) {
            this.size = dim;
        }

        /**
         * 
         * @return
         */
        public float getX() {
            return x;
        }

        /**
         * 
         * @param x
         */
        public void setX(float x) {
            this.x = x;
        }

        /**
         * 
         * @return
         */
        public float getY() {
            return y;
        }

        /**
         * 
         * @param y
         */
        public void setY(float y) {
            this.y = y;
        }

        /**
         * 
         * @param vertex
         */
        public void addNeighbor(Vertex<?> vertex) {
            neighbors.add(vertex);
        }

        /**
         * 
         * @param vertex
         */
        public void removeNeighbor(Vertex<?> vertex) {
            neighbors.remove(vertex);
        }

        /**
         * 
         * @return
         */
        public Collection<Vertex<?>> getNeighbors() {
            return neighbors;
        }

        /**
         * 
         * RESOLVE: need to optimize this.

         * @param neighbor
         * @return
         */
        public Edge<?> getEdge(Vertex<?> neighbor) {
            for (Edge<?> e : edges) {
                if (e.contains(neighbor)) {
                    return e;
                }
            }

            return null;
        }

        /**
         * 
         * @param edge
         */
        public void addEdge(Edge<?> edge) {
            edges.add(edge);
        }

        /**
         * 
         * @param edge
         */
        public void removeEdge(Edge<?> edge) {
            edges.remove(edge);
        }

        /**
         * 
         * @return
         */
        public Collection<Edge<?>> getEdges() {
            return edges;
        }

        /**
         * 
         * @return
         */
        public N getNodeDesignElement() {
            return node;
        }

        /**
         * 
         * @return
         */
        public int getDegree() {
            return neighbors.size();
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
         * @param number
         */
        public void setNumber(int number) {
            this.number = number;
        }

        /**
         * 
         * @param data
         */
        public void setVertexData(Object data) {
            this.vertexData = data;
        }

        /**
         * 
         * @return
         */
        public Object getVertexData() {
            return vertexData;
        }

        @Override
        public String toString() {
            return "vertex : " + node;// + " number = " + number;

        }
    }

    /**
     * TODO: Should be in MGraph
     */
    public static class Edge<E> {

        public enum Direction {

            HORIZONTAL, VERTICAL, UP, DOWN, LEFT, RIGHT
        }
        private Vertex<?> v;
        private Vertex<?> w;
        private E edge;
        private Direction direction;
        private int weight;
        private Object edgeData;

        /**
         * 
         * @param v
         * @param w
         * @param edge
         */
        public Edge(Vertex<?> v, Vertex<?> w, E edge) {
            this.v = v;
            this.w = w;
            this.edge = edge;
        }

        /**
         * 
         * @return
         */
        public Vertex<?> getV() {
            return v;
        }

        /**
         * 
         * @return
         */
        public Vertex<?> getW() {
            return w;
        }

        /**
         * 
         * @return
         */
        public int getWeight() {
            return weight;
        }

        /**
         * 
         * @param weight
         */
        public void setWeight(int weight) {
            this.weight = weight;
        }

        /**
         * 
         * @return
         */
        public E getEdgeDesignElement() {
            return edge;
        }

        /**
         * 
         * @param data
         */
        public void setEdgeData(Object data) {
            this.edgeData = data;
        }

        /**
         * 
         * @return
         */
        public Object getEdgeData() {
            return edgeData;
        }

        /**
         * 
         * @param vertex
         * @return
         */
        public boolean contains(Vertex<?> vertex) {
            return (this.v == vertex || this.w == vertex);
        }

        /**
         * 
         * @param edge
         * @return
         */
        public boolean shareVertex(Edge<?> edge) {
            return contains(edge.v) || contains(edge.w);
        }

        /**
         * 
         * @param vertex
         * @return
         */
        public Vertex<?> getOppositeVertex(Vertex<?> vertex) {
            if (v == vertex) {
                return w;
            } else if (w == vertex) {
                return v;
            }

            return null;
        }

        /**
         * 
         * @param direction
         */
        public void setDirection(Direction direction) {
            this.direction = direction;
        }

        /**
         * 
         * @return
         */
        public Direction getDirection() {
            return direction;
        }

        @Override
        public String toString() {
            return "edge : " + edge + "\n  v = " + v + "\n  w = " + w;
        }
    }

    /**
     * 
     */
    public static class DummyVertex<N> extends Vertex<N> {

        public enum Type {

            CROSSING, HYPEREDGE, BEND, TEMPORARY
        };
        
        private static int counter = 0;
        private Edge<?> originalEdge;
        private Type type;
        private int index;

        /**
         * 
         * @param originalEdge
         * @param type
         */
        public DummyVertex(Edge<?> originalEdge, Type type) {
            super(null);
            this.originalEdge = originalEdge;
            this.type = type;
            index = --counter;
        }

        /**
         * 
         * @param type
         */
        public DummyVertex(Type type) {
            this(null, type);
        }

        /**
         * 
         */
        public static void resetCounter() {
            counter = 0;
        }

        /**
         * 
         * @param originalEdge
         */
        public void setOriginalEdge(Edge<?> originalEdge) {
            this.originalEdge = originalEdge;
        }

        /**
         * 
         * @return
         */
        public Edge<?> getOriginalEdge() {
            return originalEdge;
        }

        /**
         * 
         * @return
         */
        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return "dummy vertex " + index;
        }
    }

    /**
     * 
     */
    public static class DummyEdge<E> extends Edge<E> {

        private Edge<?> originalEdge;

        /**
         * 
         * @param v
         * @param w
         * @param originalEdge
         */
        public DummyEdge(Vertex<?> v, Vertex<?> w, Edge<E> originalEdge) {
            super(v, w, null);
            this.originalEdge = originalEdge;
        }

        /**
         * 
         * @param v
         * @param w
         */
        public DummyEdge(Vertex<?> v, Vertex<?> w) {
            this(v, w, null);
        }

        /**
         * 
         * @param originalEdge
         */
        public void setOriginalEdge(Edge<?> originalEdge) {
            this.originalEdge = originalEdge;
        }

        /**
         * 
         * @return
         */
        public Edge<?> getOriginalEdge() {
            return originalEdge;
        }

        @Override
        public String toString() {
            return "dummy " + super.toString();
        }
    }
}
