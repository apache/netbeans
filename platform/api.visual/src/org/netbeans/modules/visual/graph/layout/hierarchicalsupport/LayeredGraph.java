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
package org.netbeans.modules.visual.graph.layout.hierarchicalsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Vertex;
/**
 *
 * @author ptliu
 */
public class LayeredGraph<N, E> {
    private DirectedGraph<N, E> originalGraph;
    private List<List<Vertex<N>>> layers;
    
    /** Creates a new instance of LayeredGraph */
    protected LayeredGraph(DirectedGraph<N, E> originalGraph) {
        this.originalGraph = originalGraph;
        layers = new ArrayList<>();
    }
    
    /**
     *
     *
     */
    public static <N, E> LayeredGraph<N, E> createGraph(DirectedGraph<N, E> originalGraph) {
        LayeredGraph<N, E> graph = new LayeredGraph<>(originalGraph);
        graph.createGraph();
        
        return graph;
    }
    
    /**
     *
     *
     */
    protected void createGraph() {
        Collection<Vertex<N>> rootVertices = originalGraph.getRootVertices() ;
        for (Vertex<N> rootVertex : rootVertices) {
            assignLayers(rootVertex, 0);
        }
    }
    
    /**
     *
     *
     */
    public DirectedGraph<N, E> getOriginalGraph() {
        return originalGraph;
    }
    
    
    /**
     *
     *
     */
    private void assignLayers(Vertex<N> vertex, int index) {
        assignLayer(vertex, index);
        
        Collection<Vertex<N>> vertices = vertex.getLowerNeighbors();
        for (Vertex<N> nv : vertices) {
            int nvIndex = nv.getNumber();
            
            if (nvIndex <= index) {
                assignLayers(nv, index+1);
            }
        }
    }
    
    
    /**
     *
     *
     */
    public void assignLayer(Vertex<N> vertex, int index) {
        int prevIndex = vertex.getNumber();
        
        if (prevIndex != -1) {
            List<Vertex<N>> layer = getLayer(prevIndex);
            layer.remove(vertex);
        }
        
        List<Vertex<N>> layer = getLayer(index);
        layer.add(vertex);
        vertex.setNumber(index);
        vertex.setY(index);
        vertex.setX(layer.size());
    }
    
    /**
     *
     *
     */
    public List<List<Vertex<N>>> getLayers() {
        return layers;
    }
    
    /**
     *
     *
     */
    public List<Vertex<N>> getLayer(int index) {
        int size = layers.size();
        
        if (index >= size) {
            for (int i = size; i <= index; i++)
                layers.add(new ArrayList<>());
        }
        
        return layers.get(index);
    }
    
    /**
     *
     *
     */
    public boolean[][] computeAdjacencyMatrix(int upperLayerIndex) {
        List<Vertex<N>> upperLayer = layers.get(upperLayerIndex);
        List<Vertex<N>> lowerLayer = layers.get(upperLayerIndex+1);
        int upperLayerSize = upperLayer.size();
        int lowerLayerSize = lowerLayer.size();
        
        boolean[][] matrix = new boolean[upperLayerSize][lowerLayerSize];
        
        for (int j = 0; j < upperLayerSize; j++) {
            Vertex<N> v = upperLayer.get(j);
            if (v != null) {
                Collection<Vertex<N>> vertices = v.getLowerNeighbors();
                for (Vertex<N> nv : vertices) {
                    int k = lowerLayer.indexOf(nv);
                    
                    if (k > -1) {
                        matrix[j][k] = true;
                        //System.out.println(j + "," + k + " is set");
                    }
                }
            }
        }
        
        return matrix;
    }
    
    /**
     *
     *
     */
    public float[] computeLowerBarycenters(int upperLayerIndex) {
        boolean[][] matrix = computeAdjacencyMatrix(upperLayerIndex);
        List<Vertex<N>> upperLayer = layers.get(upperLayerIndex);
        List<Vertex<N>> lowerLayer = layers.get(upperLayerIndex+1);
        int upperLayerSize = upperLayer.size();
        int lowerLayerSize = lowerLayer.size();
        float lowerBarycenters[] = new float[lowerLayerSize];

        for (int k = 0; k < lowerLayerSize; k++) {
            float sum = 0;
            float count = 0;
            for (int j = 0; j < upperLayerSize; j++) {
                if (matrix[j][k]) {
                    Vertex<N> jv = upperLayer.get(j);
                    sum += jv.getX(); 
                    count++;
                }
            }
            Vertex<N> kv = lowerLayer.get(k);
            lowerBarycenters[k] = sum/count;
        }
        
        return lowerBarycenters;
    }
    
    
    /**
     *
     *
     */
    public float[] computeUpperBarycenters(int upperLayerIndex) {
        boolean[][] matrix = computeAdjacencyMatrix(upperLayerIndex);
        List<Vertex<N>> upperLayer = layers.get(upperLayerIndex);
        List<Vertex<N>> lowerLayer = layers.get(upperLayerIndex+1);
        int upperLayerSize = upperLayer.size();
        int lowerLayerSize = lowerLayer.size();
        float upperBarycenters[] = new float[upperLayerSize];
        
        for (int j = 0; j < upperLayerSize; j++) {
            float sum = 0;
            float count = 0;
            for (int k = 0; k < lowerLayerSize; k++) {
                if (matrix[j][k]) {
                    Vertex<N> kv = lowerLayer.get(k);
                    sum += kv.getX(); 
                    count++;
                }
            }
            
            Vertex<N> jv = upperLayer.get(j);
            upperBarycenters[j] = sum/count;
      
            //System.out.println("jv = " + jv + " barycenter = " + upperBarycenters[j]);
        }
        
        return upperBarycenters;
    }
    
}
