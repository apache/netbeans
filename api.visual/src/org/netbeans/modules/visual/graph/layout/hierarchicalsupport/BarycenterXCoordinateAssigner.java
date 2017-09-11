/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
    public LayeredGraph assignCoordinates(LayeredGraph graph) {
        List<List<Vertex>> layers = graph.getLayers();
        int size = layers.size();
        int maxIteration = 1;
        
        for (int iteration = 0; iteration < maxIteration; iteration++) {
            // downward phase
            //System.out.println("downward phase");
            for (int i = 0; i < size-1; i++) {
                float lowerBarycenters[] = graph.computeLowerBarycenters(i);
                List<Vertex> lowerLayer = graph.getLayer(i+1);
                int upPriorities[] = computeUpPriorities(lowerLayer);
                moveVertices(lowerLayer, lowerBarycenters, upPriorities);
            }
            
            //System.out.println("upward phase");
            // upward phase
            for (int i = size-2; i >= 0; i--) {
                float upperBarycenters[] = graph.computeUpperBarycenters(i);
                List<Vertex> upperLayer = graph.getLayer(i);
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
    private void moveVertices(List<Vertex> layer, float[] barycenters,
            int[] priorities) {
        int size = layer.size();
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Vertex jv = layer.get(j);
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
                        Vertex kv = layer.get(k);
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
                        Vertex hv = layer.get(hpIndex);
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
                        Vertex kv = layer.get(k);
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
    private int[] computeUpPriorities(List<Vertex> layer) {
        int size = layer.size();
        int upPriorities[] = new int[size];
        int maxUpPriority = -1;
        
        for (int i = 0; i < size; i++) {
            Vertex v = layer.get(i);
            
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
            Vertex v = layer.get(i);
            
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
    private int[] computeDownPriorities(List<Vertex> layer) {
        int size = layer.size();
        int downPriorities[] = new int[size];
        int maxDownPriority = -1;
        
        for (int i = 0; i < size; i++) {
            Vertex v = layer.get(i);
            
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
            Vertex v = layer.get(i);
            
            if (v instanceof DummyVertex) {
                downPriorities[i] = maxDownPriority;
            }
        }
        
        return downPriorities;
    }
}
