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
    private Map<N, Vertex> vertexMap;
    private GraphScene scene;
    private UniversalGraph<N, E> uGraph;
    private Collection<E> edges;

    /** Creates a new instance of UndirectedGraph */
    private MixedGraph(UniversalGraph<N, E> uGraph, GraphScene scene) {
        this.uGraph = uGraph;
        this.scene = scene;
        this.nodes = uGraph.getNodes();
        this.edges = uGraph.getEdges() ;

        vertexMap = new HashMap<N, Vertex>();
    }

    public static <N, E> MixedGraph createGraph(UniversalGraph<N, E> uGraph, GraphScene scene) {
        MixedGraph<N, E> graph = new MixedGraph<N, E>(uGraph, scene);
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
            
            Vertex sourceVertex = getVertex(source);
            Vertex targetVertex = getVertex(target);
            
            sourceVertex.addUpperNeighbor(targetVertex);
            targetVertex.addLowerNeighbor(sourceVertex);
            
            targetVertex.addNeighbor(sourceVertex);
        }
        
        for (N node : nodes) {
            Vertex vertex = getVertex(node);
        }
        

    //printGraph();
    }
    
    /**
     *
     *
     */
    public Collection<Vertex> getVertices() {
        return vertexMap.values();
    }

    /**
     *
     *
     */
    private Vertex getVertex(N node) {
        Vertex vertex = vertexMap.get(node);

        if (vertex == null) {
            vertex = new Vertex(node);
            vertexMap.put(node, vertex);
        }

        return vertex;
    }

    /**
     *
     *
     */
    private void printGraph() {
        for (Vertex v : getVertices()) {
            System.out.println("vertex = " + v);
            Collection<Vertex> vertices = v.getNeighbors() ;
            for (Vertex nv : vertices) {
                System.out.println("\tneighbor = " + nv);
            }
        }
    }

    /**
     *
     *
     */
    public static class Vertex <N> {

        private N node;
        private ArrayList<Vertex> upperNeighbors;
        private ArrayList<Vertex> lowerNeighbors;
        private ArrayList<Vertex> neighbors;
        private Object vertexData;

        public Vertex(N node) {
            this.node = node;
            neighbors = new ArrayList<Vertex>();
        }

        public void addNeighbor(Vertex vertex) {
            neighbors.add(vertex);
        }

        public void removeNeighbor(Vertex vertex) {
            neighbors.remove(vertex);
        }

        public void addLowerNeighbor(Vertex vertex) {
            if (!lowerNeighbors.contains(vertex)) {
                lowerNeighbors.add(vertex);
            }
        }

        public void removeLowerNeighbor(Vertex vertex) {
            lowerNeighbors.remove(vertex);
        }

        public Collection<Vertex> getLowerNeighbors() {
            return Collections.unmodifiableCollection(lowerNeighbors);
        }

        public void addUpperNeighbor(Vertex vertex) {
            if (!upperNeighbors.contains(vertex)) {
                upperNeighbors.add(vertex);
            }
        }

        public void removeUpperNeighbor(Vertex vertex) {
            upperNeighbors.remove(vertex);
        }

        public Collection<Vertex> getUpperNeighbors() {
            return Collections.unmodifiableCollection(upperNeighbors);
        }

        public Collection<Vertex> getNeighbors() {
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
