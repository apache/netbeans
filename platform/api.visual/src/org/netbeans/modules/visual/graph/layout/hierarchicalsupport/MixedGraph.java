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
package org.netbeans.modules.visual.graph.layout.hierarchicalsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.layout.UniversalGraph;

/**
 *
 * @author ptliu
 */
public class MixedGraph<N, E> {

    private Collection<N> nodes;
    private Map<N, Vertex<N>> vertexMap;
    private GraphScene<N, E> scene;
    private UniversalGraph<N, E> uGraph;
    private Collection<E> edges;

    /** Creates a new instance of UndirectedGraph */
    private MixedGraph(UniversalGraph<N, E> uGraph, GraphScene<N, E> scene) {
        this.uGraph = uGraph;
        this.scene = scene;
        this.nodes = uGraph.getNodes();
        this.edges = uGraph.getEdges() ;

        vertexMap = new HashMap<>();
    }

    public static <N, E> MixedGraph<N, E> createGraph(UniversalGraph<N, E> uGraph, GraphScene<N, E> scene) {
        MixedGraph<N, E> graph = new MixedGraph<>(uGraph, scene);
        graph.createGraph();
        //graph.printGraph();
        return graph;
    }

    /**
     *
     *
     */
//    private void createGraph() {
//        for (NodeDesignElement node : nodes) {
//            Vertex v = getVertex(node);
//
//            for (EdgeDesignElement edge : node.getOutputEdges()) {
//                NodeDesignElement destNode = edge.getTargetNode();
//                Vertex nv = getVertex(destNode);
//
//                v.addLowerNeighbor(nv);
//                nv.addUpperNeighbor(v);
//
//            }
//
//            for (EdgeDesignElement edge : node.getInputEdges()) {
//                NodeDesignElement srcNode = edge.getSourceNode();
//
//                v.addNeighbor(getVertex(srcNode));
// 
//            }
//        }
//
//        printGraph();
//    }

    protected void createGraph() {
        for (E e: edges) {
            
            N source = uGraph.getEdgeSource(e) ;
            N target = uGraph.getEdgeTarget(e) ;
            
            Vertex<N> sourceVertex = getVertex(source);
            Vertex<N> targetVertex = getVertex(target);
            
            sourceVertex.addUpperNeighbor(targetVertex);
            targetVertex.addLowerNeighbor(sourceVertex);
            
            targetVertex.addNeighbor(sourceVertex);
        }
        
        for (N node : nodes) {
            Vertex<N> vertex = getVertex(node);
        }
        

    //printGraph();
    }
    
    /**
     *
     *
     */
    public Collection<Vertex<N>> getVertices() {
        return vertexMap.values();
    }

    /**
     *
     *
     */
    private Vertex<N> getVertex(N node) {
        Vertex<N> vertex = vertexMap.get(node);

        if (vertex == null) {
            vertex = new Vertex<>(node);
            vertexMap.put(node, vertex);
        }

        return vertex;
    }

    /**
     *
     *
     */
    private void printGraph() {
        for (Vertex<?> v : getVertices()) {
            System.out.println("vertex = " + v);
            Collection<Vertex<?>> vertices = v.getNeighbors() ;
            for (Vertex<?> nv : vertices) {
                System.out.println("\tneighbor = " + nv);
            }
        }
    }

    /**
     *
     *
     */
    public static class Vertex<N> {

        private N node;
        private ArrayList<Vertex<?>> upperNeighbors;
        private ArrayList<Vertex<?>> lowerNeighbors;
        private ArrayList<Vertex<?>> neighbors;
        private Object vertexData;

        public Vertex(N node) {
            this.node = node;
            neighbors = new ArrayList<>();
        }

        public void addNeighbor(Vertex<?> vertex) {
            neighbors.add(vertex);
        }

        public void removeNeighbor(Vertex<?> vertex) {
            neighbors.remove(vertex);
        }

        public void addLowerNeighbor(Vertex<?> vertex) {
            if (!lowerNeighbors.contains(vertex)) {
                lowerNeighbors.add(vertex);
            }
        }

        public void removeLowerNeighbor(Vertex<?> vertex) {
            lowerNeighbors.remove(vertex);
        }

        public Collection<Vertex<?>> getLowerNeighbors() {
            return Collections.unmodifiableCollection(lowerNeighbors);
        }

        public void addUpperNeighbor(Vertex<?> vertex) {
            if (!upperNeighbors.contains(vertex)) {
                upperNeighbors.add(vertex);
            }
        }

        public void removeUpperNeighbor(Vertex<?> vertex) {
            upperNeighbors.remove(vertex);
        }

        public Collection<Vertex<?>> getUpperNeighbors() {
            return Collections.unmodifiableCollection(upperNeighbors);
        }

        public Collection<Vertex<?>> getNeighbors() {
            return neighbors;
        }

        public N getNodeDesignElement() {
            return node;
        }

        public int getDegree() {
            return neighbors.size();
        }

        public void setVertexData(Object data) {
            this.vertexData = data;
        }

        public Object getVertexData() {
            return vertexData;
        }

        public String toString() {
            return super.toString() + " : " + node;
        }
    }
}
