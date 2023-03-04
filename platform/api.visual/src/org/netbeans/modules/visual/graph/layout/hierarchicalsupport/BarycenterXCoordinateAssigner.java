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
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.DummyVertex;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class BarycenterXCoordinateAssigner {
    
    /** Creates a new instance of BarycenterXCoordinateAssigner */
    public BarycenterXCoordinateAssigner() {
    }

    
    /**
     *
     *
     */
    public <N, E> LayeredGraph<N, E> assignCoordinates(LayeredGraph<N, E> graph) {
        List<List<Vertex<N>>> layers = graph.getLayers();
        int size = layers.size();
        int maxIteration = 1;
        
        for (int iteration = 0; iteration < maxIteration; iteration++) {
            // downward phase
            //System.out.println("downward phase");
            for (int i = 0; i < size-1; i++) {
                float lowerBarycenters[] = graph.computeLowerBarycenters(i);
                List<Vertex<N>> lowerLayer = graph.getLayer(i+1);
                int upPriorities[] = computeUpPriorities(lowerLayer);
                moveVertices(lowerLayer, lowerBarycenters, upPriorities);
            }
            
            //System.out.println("upward phase");
            // upward phase
            for (int i = size-2; i >= 0; i--) {
                float upperBarycenters[] = graph.computeUpperBarycenters(i);
                List<Vertex<N>> upperLayer = graph.getLayer(i);
                int downPriorities[] = computeDownPriorities(upperLayer);
                moveVertices(upperLayer, upperBarycenters, downPriorities);
            }
        }
        
        return graph;
    }
    
    /**
     *
     *
     */
    private <N> void moveVertices(List<Vertex<N>> layer, float[] barycenters,
            int[] priorities) {
        int size = layer.size();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Vertex<N> jv = layer.get(j);
                int jp = priorities[j]; 
                float jbc = barycenters[j]; 
                float jhd = jv.getX(); 
                
                //System.out.println("jv = " + jv + " jp = " + jp +
                //        " jbc = " + jbc + " jhd = " + jhd);
                
                // If the current horizontal distance is to the
                // left of the barycenter, we try to move it
                // close to the barycenter
                if (jhd < jbc) {
                    // First check to see if there are equal or
                    // higher priority vertex to the right
                    int hpIndex = -1;
                    for (int k = j+1; k < size; k++) {
                        if (priorities[k] >= jp) {
                            hpIndex = k;
                            break;
                        }
                    }
                    
                    //System.out.println("hpIndex = " + hpIndex);
                    
                    float nhd = jbc;  // new horizontal distance
                    float lastIndex = size;
                    
                    if (hpIndex != -1) {
                        lastIndex = hpIndex;
                        // If there is a higher priority vertex to the right,
                        Vertex<N> hv = layer.get(hpIndex);
                        float hhd = hv.getX(); 
                        float spacing = (hhd-jhd)/(hpIndex-j);
                        //System.out.println("spacing = " + spacing);
                        
                        if (spacing > 1.0f) {
                            nhd = jhd + (spacing - 1.0f);
                            //System.out.println("nhd = " + nhd);
                            if (nhd > jbc) {
                                nhd = jbc;
                                //System.out.println("nhd reset to bc " + nhd);
                            }
                        } else {
                            continue;
                        }
                    }
                    
                    if (nhd <= jhd) continue;
                    
                    //System.out.println("moving jv = " + jv + " to " + nhd);
                    jv.setX((int)nhd);
                    float chd = nhd;  //current horizontal distance
                    
                    for (int k = j+1; k < lastIndex; k++) {
                        Vertex<N> kv = layer.get(k);
                        float khd = kv.getX(); 
                        
                        if (khd <= (chd + 1.0f)) {
                            chd = chd + 1.0f;
                            //System.out.println("moving kv = " + kv + " to " + chd);
                            kv.setX((int)chd);
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
    
    
    /**
     *
     *
     */
    private <N> int[] computeUpPriorities(List<Vertex<N>> layer) {
        int size = layer.size();
        int upPriorities[] = new int[size];
        int maxUpPriority = -1;
        
        for (int i = 0; i < size; i++) {
            Vertex<N> v = layer.get(i);
            
            if (!(v instanceof DummyVertex)) {
                int upPriority = v.getUpperNeighbors().size();
                
                upPriorities[i] = upPriority;
       
                if (upPriority > maxUpPriority)
                    maxUpPriority = upPriority;
            }
        }
        
        maxUpPriority++;
        
        // assign each dummy vertex with max priority + 1
        for (int i = 0; i < size; i++) {
            Vertex<N> v = layer.get(i);
            
            if (v instanceof DummyVertex) {
                upPriorities[i] = maxUpPriority;
            }
        }
        
        return upPriorities;
    }
    
    
    /**
     *
     *
     */
    private <N> int[] computeDownPriorities(List<Vertex<N>> layer) {
        int size = layer.size();
        int downPriorities[] = new int[size];
        int maxDownPriority = -1;
        
        for (int i = 0; i < size; i++) {
            Vertex<N> v = layer.get(i);
            
            if (!(v instanceof DummyVertex)) {
                int downPriority = v.getLowerNeighbors().size();
                //System.out.println("downPriority = " + downPriority);
                downPriorities[i] = downPriority;
    
                if (downPriority > maxDownPriority)
                    maxDownPriority = downPriority;
            }
        }
        
        maxDownPriority++;
        
        // assign each dummy vertex with max priority + 1
        for (int i = 0; i < size; i++) {
            Vertex<N> v = layer.get(i);
            
            if (v instanceof DummyVertex) {
                downPriorities[i] = maxDownPriority;
            }
        }
        
        return downPriorities;
    }
}
