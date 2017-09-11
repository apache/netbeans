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
package org.netbeans.modules.visual.graph.layout.orthogonalsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.Face.Dart;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class FlowNetwork {

    private EmbeddedPlanarGraph originalGraph;
    private Map<Vertex, Node> vertexNodeMap;
    private Map<Face, Node> faceNodeMap;
    public Collection<Node> nodes;
    public Collection<Arc> arcs;
    public Node source;
    public Node sink;

    /**
     * 
     * @param graph
     * @return
     */
    public static FlowNetwork createGraph(EmbeddedPlanarGraph graph) {
        FlowNetwork network = new FlowNetwork(graph);
        network.createGraph();

        return network;
    }

    /**
     * 
     * @param graph
     */
    private FlowNetwork(EmbeddedPlanarGraph graph) {
        this.originalGraph = graph;
        vertexNodeMap = new HashMap<Vertex, Node>();
        faceNodeMap = new HashMap<Face, Node>();
        nodes = new ArrayList<Node>();
        arcs = new ArrayList<Arc>();
    }

    /**
     * 
     */
    private void createGraph() {
        Collection<Vertex> vertices = originalGraph.getOriginalGraph().getVertices();

        Collection<Face> faces = originalGraph.getFaces();

        for (Vertex v : vertices) {

            Node vn = getNode(v);

            for (Face f : faces) {
                if (f.containsVertex(v)) {
                    Node fn = getNode(f);
                    Collection<Dart> darts = f.getDartsFrom(v);
                    for (Dart d : darts) {
                        addArc(vn, fn, d);
                    }
                }
            }
        }


        for (Face f : faces) {
            Node fn = getNode(f);

            for (Dart d : f.getDarts()) {
                Face nf = originalGraph.getOppositeFace(f, d);
                Node nfn = getNode(nf);
                addArc(fn, nfn, d);
            }
        }

        // Now create source and sink
        source = new Node();
        source.isSource = true;
        sink = new Node();
        sink.isSink = true;

        int sourceProduction = 0;
        int sinkProduction = 0;
        for (Node n : getNodes()) {
            if (n.production > 0) {
                Arc arc = addArc(source, n);
                arc.capacity = n.production;
                arc.cost = 0;
                sourceProduction += n.production;
            } else if (n.production < 0) {
                Arc arc = addArc(n, sink);
                arc.capacity = -n.production;
                arc.cost = 0;
                sinkProduction += n.production;
            }
        }

        source.production = sourceProduction;
        sink.production = sinkProduction;
    }

    /**
     * 
     * @return
     */
    public EmbeddedPlanarGraph getOriginalGraph() {
        return originalGraph;
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Node getNode(Vertex vertex) {
        Node node = vertexNodeMap.get(vertex);

        if (node == null) {
            node = new Node(vertex);
            vertexNodeMap.put(vertex, node);
            nodes.add(node);
        }

        return node;
    }

    /**
     * 
     * @param face
     * @return
     */
    public Node getNode(Face face) {
        Node node = faceNodeMap.get(face);

        if (node == null) {
            node = new Node(face);
            faceNodeMap.put(face, node);
            nodes.add(node);
        }

        return node;
    }

    /**
     * 
     * @return
     */
    public Node getSource() {
        return source;
    }

    /**
     * 
     * @return
     */
    public Node getSink() {
        return sink;
    }

    /**
     * 
     * @param sourceNode
     * @param destNode
     * @param dart
     * @return
     */
    public Arc addArc(Node sourceNode, Node destNode, Dart dart) {
        Arc arc = new Arc(sourceNode, destNode, dart);
        arcs.add(arc);
        return arc;
    }

    /**
     * 
     * @param sourceNode
     * @param destNode
     * @return
     */
    public Arc addArc(Node sourceNode, Node destNode) {
        return addArc(sourceNode, destNode, null);
    }

    /**
     * 
     * @param arc
     */
    public void removeArc(Arc arc) {
        arcs.remove(arc);

        Node node = arc.getSourceNode();
        if (node != null) {
            node.removeOutputArc(arc);
        }

        node = arc.getDestinationNode();
        if (node != null) {
            node.removeInputArc(arc);
        }
    }

    /**
     * 
     * @return
     */
    public Collection<Node> getNodes() {
        return nodes;
    }

    /**
     * 
     * @return
     */
    public Collection<Arc> getArcs() {
        return arcs;
    }

    /**
     * 
     */
    public void removeSourceAndSink() {
        nodes.remove(source);
        nodes.remove(sink);

        ArrayList<Arc> _arcs = new ArrayList<Arc>(source.getOutputArcs());
        for (Arc a : _arcs) {
            removeArc(a);
        }

        _arcs = new ArrayList<Arc>(sink.getInputArcs());
        for (Arc a : _arcs) {
            removeArc(a);
        }
    }

    /**
     * 
     * @return
     */
    public String toString() {
        String s = "Flow Network\n";
        s = s + "Source:\n" + source + "\n";
        s = s + "Sink:\n" + sink + "\n";
        s = s + "Nodes:\n";
        for (Node n : nodes) {
            s = s + n + "\n";
        }

        return s;
    }

    /**
     * 
     */
    public static class Node {

        private Face face;
        private Vertex vertex;
        private Collection<Arc> inputArcs;
        private Collection<Arc> outputArcs;
        private int production;
        private boolean isSource;
        private boolean isSink;

        /**
         * 
         */
        public Node() {
            inputArcs = new ArrayList<Arc>();
            outputArcs = new ArrayList<Arc>();
        }

        /**
         * 
         * @param face
         */
        public Node(Face face) {
            this();
            this.face = face;
            int degree = face.getDegree();

            if (face.isOuterFace()) {
                production = -(1 * degree + 4);
            } else {
                if (degree == 2) {
                    production = 0;
                } else {
                    production = -(1 * degree - 4);
                }
            }
        }

        /**
         * 
         * @param vertex
         */
        public Node(Vertex vertex) {
            this();
            this.vertex = vertex;

            // According to Tamassia's book, it's 4.
            // According to the dissertation, it's 4 - vertex.getDegree();
            production = 4 - vertex.getDegree();
        }

        /**
         * 
         * @return
         */
        public Vertex getVertex() {
            return vertex;
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
        public boolean isFaceNode() {
            return face != null;
        }

        /**
         * 
         * @return
         */
        public boolean isVertexNode() {
            return vertex != null;
        }
        
        /**
         * 
         * @param arc
         */
        public void addInputArc(Arc arc) {
            inputArcs.add(arc);
        }

        /**
         * 
         * @param arc
         */
        public void removeInputArc(Arc arc) {
            inputArcs.remove(arc);
        }

        /**
         * 
         * @param arc
         */
        public void addOutputArc(Arc arc) {
            outputArcs.add(arc);
        }

        /**
         * 
         * @param arc
         */
        public void removeOutputArc(Arc arc) {
            outputArcs.remove(arc);
        }

        /**
         * 
         * @return
         */
        public Collection<Arc> getInputArcs() {
            return inputArcs;
        }

        /**
         * 
         * @return
         */
        public Collection<Arc> getOutputArcs() {
            return outputArcs;
        }

        /**
         * 
         * @param node
         * @param dart
         * @return
         */
        public Arc getArcToVia(Node node, Dart dart) {
            Edge edge = dart.getEdge();
            for (Arc arc : outputArcs) {
                if (arc.getDestinationNode() == node &&
                        arc.getDart().getEdge() == edge) {
                    return arc;
                }
            }

            return null;
        }

        /**
         * 
         * @return
         */
        public int getProduction() {
            return production;
        }

        /**
         * 
         * @param production
         */
        public void setProduction(int production) {
            this.production = production;
        }

        /**
         * 
         * @return
         */
        @Override
        public String toString() {
            String s = "Node: \n";
            if (isSource) {
                s = "Source Node:\n";
            } else if (isSink) {
                s = "Sink Node:\n";
            }
            s = s + "hashCode: " + hashCode() + "\n";
            s = s + "vertex: " + vertex + "\n";
            s = s + "face: " + face + "\n";
            s = s + "production: " + production + "\n";

            /*
            s = s + "output arcs: \n";
            for (Arc a : outputArcs) {
            s = s + a + "\n";
            }
            
            s = s + "input arcs: \n";
            for (Arc a : inputArcs) {
            s = s + a + "\n";
            }
             */
            return s;
        }
    }

    public static class Arc {

        private Node sourceNode;
        private Node destinationNode;
        private int capacity;
        private int cost;
        private int flow;
        private int lowerBound;
        private Dart dart;

        /**
         * 
         * @param source
         * @param destination
         * @param dart
         */
        public Arc(Node source, Node destination, Dart dart) {
            this.sourceNode = source;
            this.destinationNode = destination;
            this.dart = dart;

            flow = 0;
            if (sourceNode.isVertexNode() || destination.isVertexNode()) {
                lowerBound = 0;
                capacity = 3;  // 3 according to the dissertation

                cost = 0;
            } else if (sourceNode.isFaceNode() && destinationNode.isFaceNode()) {
                lowerBound = 0;
                capacity = Integer.MAX_VALUE;
                cost = 1;
            }

            sourceNode.addOutputArc(this);
            destinationNode.addInputArc(this);
        }

        /**
         * 
         * @return
         */
        public Node getSourceNode() {
            return sourceNode;
        }

        /**
         * 
         * @return
         */
        public Node getDestinationNode() {
            return destinationNode;
        }

        /**
         * 
         * @return
         */
        public Dart getDart() {
            return dart;
        }

        /**
         * 
         * @return
         */
        public int getCost() {
            return cost;
        }

        /**
         * 
         * @param cost
         */
        public void setCost(int cost) {
            this.cost = cost;
        }

        /**
         * 
         * @return
         */
        public int getCapacity() {
            return capacity;
        }

        /**
         * 
         * @param capacity
         */
        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        /**
         * 
         * @param flow
         */
        public void setFlow(int flow) {
            this.flow = flow;
        }

        /**
         * 
         * @return
         */
        public int getFlow() {
            return flow;
        }

        /**
         * 
         * @param delta
         */
        public void addFlow(int delta) {
            flow += delta;
        }

        /**
         * 
         * @param delta
         */
        public void substractCapacity(int delta) {
            capacity -= delta;
        }

        /**
         * 
         * @param delta
         */
        public void addCapacity(int delta) {
            capacity += delta;
        }

        /**
         * 
         * @return
         */
        public boolean isVertexArc() {
            if (sourceNode.isVertexNode()) {
                return true;
            }
            return false;
        }

        /**
         * 
         * @return
         */
        public boolean isFaceArc() {
            if (sourceNode.isFaceNode() && destinationNode.isFaceNode()) {
                return true;
            }

            return false;
        }

        /**
         * 
         * @return
         */
        public String toString() {
            String s = "Arc:\n";
            s = s + "capacity = " + capacity + "\n";
            s = s + "cost = " + cost + "\n";
            s = s + "flow = " + flow + "\n";
            s = s + "lowerBound = " + lowerBound + "\n";

            return s;
        }
    }

    public static class ResidualFlowNetwork extends FlowNetwork {

        private Map<Arc, ResidualArc> arcToResidualArcMap;
        private Map<Arc, ResidualArc> arcToReverseResidualArcMap;
        private FlowNetwork network;
        private Map<Node, Node> nodeMap;

        /**
         * 
         * @param network
         */
        public ResidualFlowNetwork(FlowNetwork network) {
            super(null);

            this.network = network;
            nodeMap = new HashMap<Node, Node>();
            arcToResidualArcMap = new HashMap<Arc, ResidualArc>();
            arcToReverseResidualArcMap = new HashMap<Arc, ResidualArc>();

            source = getNode(network.getSource());
            source.isSource = true;
            sink = getNode(network.getSink());
            sink.isSink = true;
            for (Arc arc : network.getArcs()) {
                addResidualArc(arc);
            }
        }

        /**
         * 
         * @param arc
         */
        private void addResidualArc(Arc arc) {
            // Assume all flow equals zero at the start
            Node sourceNode = getNode(arc.getSourceNode());
            Node destNode = getNode(arc.getDestinationNode());

            // Forward arc
            ResidualArc residualArc = new ResidualArc(sourceNode, destNode, arc, false);
            arcToResidualArcMap.put(arc, residualArc);

            // Reverse arc
            ResidualArc reverseResidualArc = new ResidualArc(destNode, sourceNode, arc, true);
            arcToReverseResidualArcMap.put(arc, reverseResidualArc);
        }

        /**
         * 
         * @param arc
         * @return
         */
        public ResidualArc getResidualArcFromArc(Arc arc) {
            return arcToResidualArcMap.get(arc);
        }

        /**
         * 
         * @param arc
         * @return
         */
        public ResidualArc getReverseResidualArcFromArc(Arc arc) {
            return arcToReverseResidualArcMap.get(arc);
        }

        /**
         * 
         * @param node
         * @return
         */
        private Node getNode(Node node) {
            Node residualNode = nodeMap.get(node);

            if (residualNode == null) {
                if (node.isVertexNode()) {
                    residualNode = getNode(node.getVertex());
                } else if (node.isFaceNode()) {
                    residualNode = getNode(node.getFace());
                } else {
                    residualNode = new Node();
                    residualNode.setProduction(node.getProduction());
                    nodes.add(residualNode);
                }

                nodeMap.put(node, residualNode);
            }

            return residualNode;
        }
    }

    /**
     * 
     */
    public static class ResidualArc extends Arc {

        private boolean isReverse;
        private Arc arc;

        /**
         * 
         * @param sourceNode
         * @param destNode
         * @param arc
         * @param isReverse
         */
        public ResidualArc(Node sourceNode, Node destNode,
                Arc arc, boolean isReverse) {
            super(sourceNode, destNode, null);
            this.isReverse = isReverse;
            this.arc = arc;

            if (!isReverse) {
                setCapacity(arc.getCapacity() - arc.getFlow());
            } else {
                setCapacity(arc.getFlow());
            }

            setCost(arc.getCost());
        }

        /**
         * 
         * @return
         */
        public boolean isReverse() {
            return isReverse;
        }

        /**
         * 
         * @return
         */
        public Arc getArc() {
            return arc;
        }

        /**
         * 
         * @return
         */
        @Override
        public String toString() {
            String s = "ResidualArc:\n";
            s = s + "isReverse = " + isReverse + "\n";
            s = s + "capacity = " + getCapacity() + "\n";
            s = s + "cost = " + getCost() + "\n";
            s = s + "flow = " + getFlow() + "\n";

            return s;
        }
    }
}
