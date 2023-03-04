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
package org.netbeans.modules.visual.graph.layout.orthogonalsupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.Face.Dart;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class FlowNetwork<N, E> {

    private EmbeddedPlanarGraph<N, E> originalGraph;
    private Map<Vertex<N>, Node<N>> vertexNodeMap;
    private Map<Face, Node<N>> faceNodeMap;
    public Collection<Node<N>> nodes;
    public Collection<Arc<N>> arcs;
    public Node<N> source;
    public Node<N> sink;

    /**
     * 
     * @param graph
     * @return
     */
    public static <N, E> FlowNetwork<N, E> createGraph(EmbeddedPlanarGraph<N, E> graph) {
        FlowNetwork<N, E> network = new FlowNetwork<>(graph);
        network.createGraph();

        return network;
    }

    /**
     * 
     * @param graph
     */
    private FlowNetwork(EmbeddedPlanarGraph<N, E> graph) {
        this.originalGraph = graph;
        vertexNodeMap = new HashMap<>();
        faceNodeMap = new HashMap<>();
        nodes = new ArrayList<>();
        arcs = new ArrayList<>();
    }

    /**
     * 
     */
    private void createGraph() {
        Collection<Vertex<N>> vertices = originalGraph.getOriginalGraph().getVertices();

        Collection<Face> faces = originalGraph.getFaces();

        for (Vertex<N> v : vertices) {

            Node<N> vn = getNode(v);

            for (Face f : faces) {
                if (f.containsVertex(v)) {
                    Node<N> fn = getNode(f);
                    Collection<Dart> darts = f.getDartsFrom(v);
                    for (Dart d : darts) {
                        addArc(vn, fn, d);
                    }
                }
            }
        }


        for (Face f : faces) {
            Node<N> fn = getNode(f);

            for (Dart d : f.getDarts()) {
                Face nf = originalGraph.getOppositeFace(f, d);
                Node<N> nfn = getNode(nf);
                addArc(fn, nfn, d);
            }
        }

        // Now create source and sink
        source = new Node<>();
        source.isSource = true;
        sink = new Node<>();
        sink.isSink = true;

        int sourceProduction = 0;
        int sinkProduction = 0;
        for (Node<N> n : getNodes()) {
            if (n.production > 0) {
                Arc<N> arc = addArc(source, n);
                arc.capacity = n.production;
                arc.cost = 0;
                sourceProduction += n.production;
            } else if (n.production < 0) {
                Arc<N> arc = addArc(n, sink);
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
    public EmbeddedPlanarGraph<N, E> getOriginalGraph() {
        return originalGraph;
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Node<N> getNode(Vertex<N> vertex) {
        Node<N> node = vertexNodeMap.get(vertex);

        if (node == null) {
            node = new Node<>(vertex);
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
    public Node<N> getNode(Face face) {
        Node<N> node = faceNodeMap.get(face);

        if (node == null) {
            node = new Node<>(face);
            faceNodeMap.put(face, node);
            nodes.add(node);
        }

        return node;
    }

    /**
     * 
     * @return
     */
    public Node<N> getSource() {
        return source;
    }

    /**
     * 
     * @return
     */
    public Node<N> getSink() {
        return sink;
    }

    /**
     * 
     * @param sourceNode
     * @param destNode
     * @param dart
     * @return
     */
    public Arc<N> addArc(Node<N> sourceNode, Node<N> destNode, Dart dart) {
        Arc<N> arc = new Arc<>(sourceNode, destNode, dart);
        arcs.add(arc);
        return arc;
    }

    /**
     * 
     * @param sourceNode
     * @param destNode
     * @return
     */
    public Arc<N> addArc(Node<N> sourceNode, Node<N> destNode) {
        return addArc(sourceNode, destNode, null);
    }

    /**
     * 
     * @param arc
     */
    public void removeArc(Arc<N> arc) {
        arcs.remove(arc);

        Node<N> node = arc.getSourceNode();
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
    public Collection<Node<N>> getNodes() {
        return nodes;
    }

    /**
     * 
     * @return
     */
    public Collection<Arc<N>> getArcs() {
        return arcs;
    }

    /**
     * 
     */
    public void removeSourceAndSink() {
        nodes.remove(source);
        nodes.remove(sink);

        List<Arc<N>> _arcs = new ArrayList<>(source.getOutputArcs());
        for (Arc<N> a : _arcs) {
            removeArc(a);
        }

        _arcs = new ArrayList<>(sink.getInputArcs());
        for (Arc<N> a : _arcs) {
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
        for (Node<N> n : nodes) {
            s = s + n + "\n";
        }

        return s;
    }

    /**
     * 
     */
    public static class Node<N> {

        private Face face;
        private Vertex<N> vertex;
        private Collection<Arc<N>> inputArcs;
        private Collection<Arc<N>> outputArcs;
        private int production;
        private boolean isSource;
        private boolean isSink;

        /**
         * 
         */
        public Node() {
            inputArcs = new ArrayList<>();
            outputArcs = new ArrayList<>();
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
        public Node(Vertex<N> vertex) {
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
        public Vertex<N> getVertex() {
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
        public void addInputArc(Arc<N> arc) {
            inputArcs.add(arc);
        }

        /**
         * 
         * @param arc
         */
        public void removeInputArc(Arc<N> arc) {
            inputArcs.remove(arc);
        }

        /**
         * 
         * @param arc
         */
        public void addOutputArc(Arc<N> arc) {
            outputArcs.add(arc);
        }

        /**
         * 
         * @param arc
         */
        public void removeOutputArc(Arc<N> arc) {
            outputArcs.remove(arc);
        }

        /**
         * 
         * @return
         */
        public Collection<Arc<N>> getInputArcs() {
            return inputArcs;
        }

        /**
         * 
         * @return
         */
        public Collection<Arc<N>> getOutputArcs() {
            return outputArcs;
        }

        /**
         * 
         * @param node
         * @param dart
         * @return
         */
        public Arc<N> getArcToVia(Node<N> node, Dart dart) {
            Edge<?> edge = dart.getEdge();
            for (Arc<N> arc : outputArcs) {
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

    public static class Arc<N> {

        private Node<N> sourceNode;
        private Node<N> destinationNode;
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
        public Arc(Node<N> source, Node<N> destination, Dart dart) {
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
        public Node<N> getSourceNode() {
            return sourceNode;
        }

        /**
         * 
         * @return
         */
        public Node<N> getDestinationNode() {
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

    public static class ResidualFlowNetwork<N, E> extends FlowNetwork<N, E> {

        private Map<Arc<N>, ResidualArc<N>> arcToResidualArcMap;
        private Map<Arc<N>, ResidualArc<N>> arcToReverseResidualArcMap;
        private FlowNetwork<N, E> network;
        private Map<Node<N>, Node<N>> nodeMap;

        /**
         * 
         * @param network
         */
        public ResidualFlowNetwork(FlowNetwork<N, E> network) {
            super(null);

            this.network = network;
            nodeMap = new HashMap<>();
            arcToResidualArcMap = new HashMap<>();
            arcToReverseResidualArcMap = new HashMap<>();

            source = getNode(network.getSource());
            source.isSource = true;
            sink = getNode(network.getSink());
            sink.isSink = true;
            for (Arc<N> arc : network.getArcs()) {
                addResidualArc(arc);
            }
        }

        /**
         * 
         * @param arc
         */
        private void addResidualArc(Arc<N> arc) {
            // Assume all flow equals zero at the start
            Node<N> sourceNode = getNode(arc.getSourceNode());
            Node<N> destNode = getNode(arc.getDestinationNode());

            // Forward arc
            ResidualArc<N> residualArc = new ResidualArc<>(sourceNode, destNode, arc, false);
            arcToResidualArcMap.put(arc, residualArc);

            // Reverse arc
            ResidualArc<N> reverseResidualArc = new ResidualArc<>(destNode, sourceNode, arc, true);
            arcToReverseResidualArcMap.put(arc, reverseResidualArc);
        }

        /**
         * 
         * @param arc
         * @return
         */
        public ResidualArc<N> getResidualArcFromArc(Arc<N> arc) {
            return arcToResidualArcMap.get(arc);
        }

        /**
         * 
         * @param arc
         * @return
         */
        public ResidualArc<N> getReverseResidualArcFromArc(Arc<N> arc) {
            return arcToReverseResidualArcMap.get(arc);
        }

        /**
         * 
         * @param node
         * @return
         */
        private Node<N> getNode(Node<N> node) {
            Node<N> residualNode = nodeMap.get(node);

            if (residualNode == null) {
                if (node.isVertexNode()) {
                    residualNode = getNode(node.getVertex());
                } else if (node.isFaceNode()) {
                    residualNode = getNode(node.getFace());
                } else {
                    residualNode = new Node<>();
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
    public static class ResidualArc<N> extends Arc<N> {

        private boolean isReverse;
        private Arc<N> arc;

        /**
         * 
         * @param sourceNode
         * @param destNode
         * @param arc
         * @param isReverse
         */
        public ResidualArc(Node<N> sourceNode, Node<N> destNode,
                Arc<N> arc, boolean isReverse) {
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
        public Arc<N> getArc() {
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
