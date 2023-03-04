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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class DualGraph<N, E> {

    private EmbeddedPlanarGraph<N, E> originalGraph;
    private Map<Face, FaceVertex> vertexMap;
    private Collection<FaceVertex> vertices;
    private Map<Edge<?>, FaceEdge> edgeMap;
    private Collection<FaceEdge> edges;
    private Collection<Edge<?>> edgesToIgnore;
    private Collection<Face> facesToIgnore;

    /**
     * 
     * @param graph
     * @param facesToIgnore
     * @param edgesToIgnore
     * @return
     */
    public static <N, E> DualGraph<N, E> createGraph(EmbeddedPlanarGraph<N, E> graph,
            Collection<Face> facesToIgnore,
            Collection<Edge<?>> edgesToIgnore) {
        DualGraph<N, E> dualGraph = new DualGraph<>(graph, facesToIgnore, edgesToIgnore);
        dualGraph.createGraph();

        return dualGraph;
    }

    /**
     * 
     * @param graph
     * @param facesToIgnore
     * @param edgesToIgnore
     */
    private DualGraph(EmbeddedPlanarGraph<N, E> graph, Collection<Face> facesToIgnore,
            Collection<Edge<?>> edgesToIgnore) {
        this.originalGraph = graph;
        this.facesToIgnore = facesToIgnore;
        this.edgesToIgnore = edgesToIgnore;

        vertexMap = new HashMap<>();
        vertices = new ArrayList<>();
        edgeMap = new HashMap<>();
        edges = new ArrayList<>();
    }

    /**
     * 
     */
    private void createGraph() {
        createFaces();
        createEdges();
    }

    /**
     * 
     */
    private void createFaces() {
        for (Face f : originalGraph.getFaces()) {
            // never ignore the outer face
            if (!facesToIgnore.contains(f) || f.isOuterFace()) {
                getVertex(f);
            }
        }
    }

    /**
     * 
     */
    private void createEdges() {
        for (FaceVertex fv : getVertices()) {
            for (FaceVertex gv : getVertices()) {
                if (fv == gv) {
                    continue;
                }
                for (Edge<?> e : fv.getFace().getEdges()) {
                    if (edgesToIgnore.contains(e)) {
                        continue;
                    }
                    if (gv.getFace().containsEdge(e)) {
                        FaceEdge faceEdge = getEdge(fv, gv, e);
                        fv.addEdge(faceEdge);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    public void updateFaces() {
        int faceCount = originalGraph.getFaces().size();
        int vertexCount = vertices.size();

        if (faceCount > vertexCount) {
            createFaces();
        } else if (faceCount < vertexCount) {
            vertices.clear();
            vertexMap.clear();
            createFaces();
        }
    }

    /**
     * 
     */
    public void updateEdges() {
        edges.clear();
        edgeMap.clear();

        for (FaceVertex fv : getVertices()) {
            fv.getEdges().clear();
        }

        createEdges();
    }

    /**
     * 
     * @return
     */
    public EmbeddedPlanarGraph<N, E> getOriginalGraph() {
        return originalGraph;
    }

    /**
     * 
     * @return
     */
    public Collection<FaceVertex> getVertices() {
        return vertices;
    }

    /**
     * 
     * @return
     */
    public Collection<FaceEdge> getEdges() {
        return edges;
    }

    /**
     * 
     * @param face
     * @return
     */
    private FaceVertex getVertex(Face face) {
        FaceVertex vertex = vertexMap.get(face);

        if (vertex == null) {
            vertex = new FaceVertex(face);
            vertexMap.put(face, vertex);
            vertices.add(vertex);
        }

        return vertex;
    }

    /**
     * 
     * @param f
     * @param g
     * @param e
     * @return
     */
    private FaceEdge getEdge(FaceVertex f, FaceVertex g, Edge<?> e) {
        FaceEdge edge = edgeMap.get(e);

        if (edge == null) {
            edge = new FaceEdge(f, g, e);
            edgeMap.put(e, edge);
            edges.add(edge);
        }

        return edge;
    }

    /**
     * TODO: need to optimize
     * @param e
     * @return
     */
    public Collection<FaceVertex> getVerticesBorderingEdge(Edge<?> e) {
        Collection<FaceVertex> result = new ArrayList<>();

        for (FaceVertex v : getVertices()) {
            if (v.getFace().containsEdge(e)) {
                result.add(v);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        String s = "DualGraph:\n";

        s = s + "vertices:\n";
        for (FaceVertex fv : vertices) {
            s = s + "\t" + fv + "\n";
        }

        s = s + "edges\n";
        for (FaceEdge fe : edges) {
            s = s + "\t" + fe + "\n";
        }

        return s;
    }

    /**
     * 
     */
    public static class FaceVertex {

        private Face face;
        private Collection<FaceEdge> edges;

        /**
         * 
         * @param face
         */
        public FaceVertex(Face face) {
            this.face = face;
            edges = new LinkedHashSet<FaceEdge>();
        }

        /**
         * 
         * @return
         */
        public Face getFace() {
            return face;
        }

        /**
         * 
         * @return
         */
        public Collection<FaceEdge> getEdges() {
            return edges;
        }

        /**
         * 
         * @param edge
         */
        public void addEdge(FaceEdge edge) {
            if (!edges.contains(edge)) {
                edges.add(edge);
            }
        }

        @Override
        public String toString() {
            return "FaceVertex: " + face.toString();
        }
    }

    /**
     * 
     */
    public static class FaceEdge {

        private FaceVertex f;
        private FaceVertex g;
        private Edge<?> edge;

        /**
         * 
         * @param f
         * @param g
         * @param e
         */
        public FaceEdge(FaceVertex f, FaceVertex g, Edge<?> e) {
            this.f = f;
            this.g = g;
            this.edge = e;
        }

        /**
         * 
         * @return
         */
        public FaceVertex getF() {
            return f;
        }

        /**
         * 
         * @return
         */
        public FaceVertex getG() {
            return g;
        }

        /**
         * 
         * @return
         */
        public Edge<?> getEdge() {
            return edge;
        }

        /**
         * 
         * @param v
         * @return
         */
        public boolean contains(FaceVertex v) {
            return (f == v) || (g == v);
        }

        /**
         * 
         * @param v
         * @return
         */
        public FaceVertex getOppositeVertex(FaceVertex v) {
            if (v == f) {
                return g;
            } else if (v == g) {
                return f;
            }

            return null;
        }

        /**
         * 
         * @param v
         * @return
         */
        public FaceVertex getVertex(Vertex<?> v) {
            if (f.face.containsVertex(v)) {
                return f;
            }
            if (g.face.containsVertex(v)) {
                return g;
            }
            return null;
        }

        @Override
        public String toString() {
            String s = "FaceEdge:\n";
            s = s + "\t" + f + "\n";
            s = s + "\t" + g + "\n";
            s = s + "\t" + edge + "\n";

            return s;
        }
    }
}
