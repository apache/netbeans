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
package org.netbeans.modules.visual.graph.layout.hierarchicalsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.visual.graph.layout.hierarchicalsupport.DirectedGraph.Vertex;
/**
 *
 * @author ptliu
 */
public class LayeredGraph {
    private DirectedGraph originalGraph;
    private List<List<Vertex>> layers;
    
    /** Creates a new instance of LayeredGraph */
    protected LayeredGraph(DirectedGraph originalGraph) {
        this.originalGraph = originalGraph;
        layers = new ArrayList<List<Vertex>>();
    }
    
    /**
     *
     *
     */
    public static LayeredGraph createGraph(DirectedGraph originalGraph) {
        LayeredGraph graph = new LayeredGraph(originalGraph);
        graph.createGraph();
        
        return graph;
    }
    
    /**
     *
     *
     */
    protected void createGraph() {
        Collection<Vertex> rootVertices = originalGraph.getRootVertices() ;
        for (Vertex rootVertex : rootVertices) {
            assignLayers(rootVertex, 0);
        }
    }
    
    /**
     *
     *
     */
    public DirectedGraph getOriginalGraph() {
        return originalGraph;
    }
    
    
    /**
     *
     *
     */
    private void assignLayers(Vertex vertex, int index) {
        assignLayer(vertex, index);
        
        Collection<Vertex> vertices = vertex.getLowerNeighbors();
        for (Vertex nv : vertices) {
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
    public void assignLayer(Vertex vertex, int index) {
        int prevIndex = vertex.getNumber();
        
        if (prevIndex != -1) {
            List<Vertex> layer = getLayer(prevIndex);
            layer.remove(vertex);
        }
        
        List<Vertex> layer = getLayer(index);
        layer.add(vertex);
        vertex.setNumber(index);
        vertex.setY(index);
        vertex.setX(layer.size());
    }
    
    /**
     *
     *
     */
    public List<List<Vertex>> getLayers() {
        return layers;
    }
    
    /**
     *
     *
     */
    public List<Vertex> getLayer(int index) {
        int size = layers.size();
        
        if (index >= size) {
            for (int i = size; i <= index; i++)
                layers.add(new ArrayList<Vertex>());
        }
        
        return layers.get(index);
    }
    
    /**
     *
     *
     */
    public boolean[][] computeAdjacencyMatrix(int upperLayerIndex) {
        List<Vertex> upperLayer = layers.get(upperLayerIndex);
        List<Vertex> lowerLayer = layers.get(upperLayerIndex+1);
        int upperLayerSize = upperLayer.size();
        int lowerLayerSize = lowerLayer.size();
        
        boolean[][] matrix = new boolean[upperLayerSize][lowerLayerSize];
        
        for (int j = 0; j < upperLayerSize; j++) {
            Vertex v = upperLayer.get(j);
            if (v != null) {
                Collection<Vertex> vertices = v.getLowerNeighbors();
                for (Vertex nv : vertices) {
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
        List<Vertex> upperLayer = layers.get(upperLayerIndex);
        List<Vertex> lowerLayer = layers.get(upperLayerIndex+1);
        int upperLayerSize = upperLayer.size();
        int lowerLayerSize = lowerLayer.size();
        float lowerBarycenters[] = new float[lowerLayerSize];
        
        float[] barycenters = new float[lowerLayerSize];
        for (int k = 0; k < lowerLayerSize; k++) {
            float sum = 0;
            float count = 0;
            for (int j = 0; j < upperLayerSize; j++) {
                if (matrix[j][k]) {
                    Vertex jv = upperLayer.get(j);
                    sum += jv.getX(); 
                    count++;
                }
            }
            Vertex kv = lowerLayer.get(k);
            lowerBarycenters[k] = sum/count;
            //System.out.println("kv = " + kv + " barycenter = " + lowerBarycenters[k]);
        }
        
        return lowerBarycenters;
    }
    
    
    /**
     *
     *
     */
    public float[] computeUpperBarycenters(int upperLayerIndex) {
        boolean[][] matrix = computeAdjacencyMatrix(upperLayerIndex);
        List<Vertex> upperLayer = layers.get(upperLayerIndex);
        List<Vertex> lowerLayer = layers.get(upperLayerIndex+1);
        int upperLayerSize = upperLayer.size();
        int lowerLayerSize = lowerLayer.size();
        float upperBarycenters[] = new float[upperLayerSize];
        
        for (int j = 0; j < upperLayerSize; j++) {
            float sum = 0;
            float count = 0;
            for (int k = 0; k < lowerLayerSize; k++) {
                if (matrix[j][k]) {
                    Vertex kv = lowerLayer.get(k);
                    sum += kv.getX(); 
                    count++;
                }
            }
            
            Vertex jv = upperLayer.get(j);
            upperBarycenters[j] = sum/count;
      
            //System.out.println("jv = " + jv + " barycenter = " + upperBarycenters[j]);
        }
        
        return upperBarycenters;
    }
    
}
