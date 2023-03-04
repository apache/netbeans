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


import java.util.List;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class BarycenterCrossingMinimizer {
    
    /** Creates a new instance of BarycenterCrossingMinimizer */
    public BarycenterCrossingMinimizer() {
    }
    
    /**
     *
     *
     */
    public <N, E> LayeredGraph<N, E> minimizeCrossings(LayeredGraph<N, E> graph) {
        List<List<Vertex<N>>> layers = graph.getLayers();
        
        if (layers.size() > 1) {
            int maxIteration = 2;
            
            for (int i = 0; i < maxIteration; i++) {
                minimizeCrossingsPhaseI(graph);
            }
            
            minimizeCrossingsPhaseII(graph);
        }
        
        return graph;
    }
  
    /**
     *
     *
     */
    private <N, E> void minimizeCrossingsPhaseI(LayeredGraph<N, E> graph) {
        List<List<Vertex<N>>> layers = graph.getLayers();
        int size = layers.size();
        
        // downward phase
        for (int i = 0; i < size-1; i++) {
            float lowerBarycenters[] = graph.computeLowerBarycenters(i);
            List<Vertex<N>> lowerLayer = layers.get(i+1);
            sortVertices(lowerLayer, lowerBarycenters, false);
        }
        
        // upward phase
        for (int i = size-2; i >= 0; i--) {
            float upperBarycenters[] = graph.computeUpperBarycenters(i);
            List<Vertex<N>> upperLayer = layers.get(i);
            sortVertices(upperLayer, upperBarycenters, false);
        }
    }
    
    
    /**
     *
     *
     */
    private <N, E> void minimizeCrossingsPhaseII(LayeredGraph<N, E> graph) {
        List<List<Vertex<N>>> layers = graph.getLayers();
        int size = layers.size();
        
        // upward phase
        for (int i = size-2; i >= 0; i--) {
            float upperBarycenters[] = graph.computeUpperBarycenters(i);
            List<Vertex<N>> upperLayer = layers.get(i);
            sortVertices(upperLayer, upperBarycenters, true);
            minimizeCrossingsPhaseI(graph);
        }
        
        // downward phase
        for (int i = 0; i < size-1; i++) {
            float lowerBarycenters[] = graph.computeLowerBarycenters(i);
            List<Vertex<N>> lowerLayer = layers.get(i+1);
            sortVertices(lowerLayer, lowerBarycenters, true);
            minimizeCrossingsPhaseI(graph);
        }
    }
    

    /**
     *
     *
     */
    private <N> boolean sortVertices(List<Vertex<N>> vertices,
            float barycenters[], boolean reverseEqualBarycenters) {
        int size = vertices.size();
        boolean changed = false;
        
        for (int i = 0; i < size-1; i++) {
            for (int j = i+1; j < size; j++) {
                Vertex<N> jv = vertices.get(j);
                Vertex<N> iv = vertices.get(i);
                float jbc = barycenters[j]; 
                float ibc = barycenters[i];
                boolean swap = false;
                
                if (reverseEqualBarycenters) {
                    if (jbc <= ibc)
                        swap = true;
                } else {
                    if (jbc < ibc)
                        swap = true;
                }
                
                if (swap) {
                    vertices.set(j, iv);
                    vertices.set(i, jv);
                    barycenters[j] = ibc;
                    barycenters[i] = jbc;
                    iv.setX(j+1);
                    jv.setX(i+1);
                
                    changed = true;
                }
            }
        }
        
        return changed;
    }
}
