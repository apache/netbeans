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
    public LayeredGraph minimizeCrossings(LayeredGraph graph) {
        List<List<Vertex>> layers = graph.getLayers();
        
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
    private void minimizeCrossingsPhaseI(LayeredGraph graph) {
        List<List<Vertex>> layers = graph.getLayers();
        int size = layers.size();
        
        // downward phase
        for (int i = 0; i < size-1; i++) {
            float lowerBarycenters[] = graph.computeLowerBarycenters(i);
            List<Vertex> lowerLayer = layers.get(i+1);
            sortVertices(lowerLayer, lowerBarycenters, false);
        }
        
        // upward phase
        for (int i = size-2; i >= 0; i--) {
            float upperBarycenters[] = graph.computeUpperBarycenters(i);
            List<Vertex> upperLayer = layers.get(i);
            sortVertices(upperLayer, upperBarycenters, false);
        }
    }
    
    
    /**
     *
     *
     */
    private void minimizeCrossingsPhaseII(LayeredGraph graph) {
        List<List<Vertex>> layers = graph.getLayers();
        int size = layers.size();
        
        // upward phase
        for (int i = size-2; i >= 0; i--) {
            float upperBarycenters[] = graph.computeUpperBarycenters(i);
            List<Vertex> upperLayer = layers.get(i);
            sortVertices(upperLayer, upperBarycenters, true);
            minimizeCrossingsPhaseI(graph);
        }
        
        // downward phase
        for (int i = 0; i < size-1; i++) {
            float lowerBarycenters[] = graph.computeLowerBarycenters(i);
            List<Vertex> lowerLayer = layers.get(i+1);
            sortVertices(lowerLayer, lowerBarycenters, true);
            minimizeCrossingsPhaseI(graph);
        }
    }
    

    /**
     *
     *
     */
    private boolean sortVertices(List<Vertex> vertices,
            float barycenters[], boolean reverseEqualBarycenters) {
        int size = vertices.size();
        boolean changed = false;
        
        for (int i = 0; i < size-1; i++) {
            for (int j = i+1; j < size; j++) {
                Vertex jv = vertices.get(j);
                Vertex iv = vertices.get(i);
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
