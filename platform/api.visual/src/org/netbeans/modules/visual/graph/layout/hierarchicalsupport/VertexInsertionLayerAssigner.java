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
import java.util.List;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.DummyVertex;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Edge;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class VertexInsertionLayerAssigner {
    
    /**
     * Creates a new instance of VertexInsertionLayerAssigner 
     */
    public VertexInsertionLayerAssigner() {
    }
    
    /**
     *
     *
     */
    public <N, E> LayeredGraph<N, E> assignLayers(DirectedGraph<N, E> graph) {
        LayeredGraph<N, E> layeredGraph = LayeredGraph.createGraph(graph);
        
        insertDummyVertices(layeredGraph);
        
        return layeredGraph;
    }
    

    /**
     *
     *
     */
    private <N, E> void insertDummyVertices(LayeredGraph<N, E> graph) {
        DirectedGraph<N, E> originalGraph = graph.getOriginalGraph();
        List<List<Vertex<N>>> layers = graph.getLayers();
        
        for (int i = 0; i < layers.size(); i++) {
            List<Vertex<N>> layer = layers.get(i);
            
            for (Vertex<N> v : layer) {
                int layerIndex = v.getNumber();
                
                // work around concurrent modification exception
                Collection<Edge<?>> edges = new ArrayList<>(v.getOutgoingEdges());
                for (Edge<?> e : edges) {
                    Vertex<?> nv = e.getTarget();
                    int nvLayerIndex = nv.getNumber();
                    
                    if (nvLayerIndex > layerIndex+1) {
                        @SuppressWarnings("unchecked")
                        Edge<E> tmp = (Edge<E>) e;
                        Vertex<N> dummyVertex = originalGraph.insertDummyVertex(tmp, DummyVertex.Type.BEND);
                        graph.assignLayer(dummyVertex,  layerIndex+1);
                    }
                }
            }
        }
    }   
}
