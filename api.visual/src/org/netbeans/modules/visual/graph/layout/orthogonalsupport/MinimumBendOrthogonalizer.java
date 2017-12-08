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
package org.netbeans.modules.visual.graph.layout.orthogonalsupport;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.Face.Dart;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.FlowNetwork.Arc;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.FlowNetwork.Node;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.FlowNetwork.ResidualArc;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.FlowNetwork.ResidualFlowNetwork;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.OrthogonalRepresentation.OrthogonalShape;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.OrthogonalRepresentation.Tuple;

/**
 *
 * @author ptliu
 */
public class MinimumBendOrthogonalizer {

    /** Creates a new instance of MinimumBendOrthogonalizer */
    public MinimumBendOrthogonalizer() {
    }

    /**
     * 
     * @param epgs
     * @return
     */
    public Collection<OrthogonalRepresentation> orthogonalize(
            Collection<EmbeddedPlanarGraph> epgs) {
        Collection<OrthogonalRepresentation> ors = new ArrayList<OrthogonalRepresentation>();

        for (EmbeddedPlanarGraph epg : epgs) {
            FlowNetwork network = FlowNetwork.createGraph(epg);

            computeMinimumCostFlowNetwork(network);

            network.removeSourceAndSink();
            OrthogonalRepresentation or = computeOrthogonalRepresentation(network);
            ors.add(or);

        }

        return ors;
    }

    /**
     * 
     * @param network
     */
    private void computeMinimumCostFlowNetwork(FlowNetwork network) {
        ResidualFlowNetwork residualNetwork = new ResidualFlowNetwork(network);

        int totalFlow = 0;
        int production = network.getSource().getProduction();


        while (totalFlow < production) {
            Collection<ResidualArc> path = computeDijkstraShortestPath(residualNetwork);

            // Flow is blocked, we simply return;
            if (path == null) {
                return;
            }
            int minFlow = Integer.MAX_VALUE;

            for (Arc arc : path) {
                int capacity = arc.getCapacity();
                if (capacity < minFlow) {
                    minFlow = capacity;
                }
            }

            totalFlow += minFlow;

            for (ResidualArc ra : path) {
                Arc arc = ra.getArc();
                ResidualArc residualArc = null;
                ResidualArc reverseResidualArc = null;
                int flow = minFlow;

                if (!ra.isReverse()) {
                    residualArc = ra;
                    reverseResidualArc = residualNetwork.getReverseResidualArcFromArc(arc);
                } else {
                    flow = -flow;
                    reverseResidualArc = ra;
                    residualArc = residualNetwork.getResidualArcFromArc(arc);
                }

                arc.addFlow(flow);
                residualArc.substractCapacity(flow);
                reverseResidualArc.addCapacity(flow);
                reverseResidualArc.setFlow(arc.getFlow());

            }
        }
    }

    /**
     * 
     * @param network
     * @return
     */
    private Collection<ResidualArc> computeDijkstraShortestPath(ResidualFlowNetwork network) {
        Collection<Node> nodes = network.getNodes();
        Map<Node, ResidualArc> paths = new HashMap<Node, ResidualArc>();
        final Map<Node, Integer> distances = new HashMap<Node, Integer>();
        Node sourceNode = network.getSource();

        for (Node node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(sourceNode, 0);

        PriorityQueue<Node> priorityQueue = createPriorityQueue(distances);
        for (Node node : nodes) {
            priorityQueue.offer(node);
        }

        HashSet<Node> visitedNodes = new HashSet<Node>();

        while (priorityQueue.size() > 0) {
            Node node = priorityQueue.poll();

            if (distances.get(node) == Integer.MAX_VALUE) {
                continue;
            }
            if (visitedNodes.contains(node)) {
                continue;
            }
            for (Arc arc : node.getOutputArcs()) {
                if (arc.getCapacity() > 0) {
                    computeRelaxation(arc, paths, distances, priorityQueue);
                }
            }

            visitedNodes.add(node);
        }

        Collection<ResidualArc> shortestPath = new ArrayList<ResidualArc>();
        Node currentNode = network.getSink();

        while (currentNode != sourceNode) {
            Arc arc = paths.get(currentNode);

            if (arc == null) {
                return null;
            }
            shortestPath.add((ResidualArc) arc);
            currentNode = arc.getSourceNode();
        }

        return shortestPath;
    }

    /**
     * 
     * @param arc
     * @param paths
     * @param distances
     * @param queue
     */
    private void computeRelaxation(Arc arc, Map<Node, ResidualArc> paths,
            Map<Node, Integer> distances, PriorityQueue<Node> queue) {
        Node srcNode = arc.getSourceNode();
        Node destNode = arc.getDestinationNode();
        int sd = distances.get(srcNode);
        int dd = distances.get(destNode);
        int cost = arc.getCost();

        if (dd > sd + cost) {
            distances.put(destNode, sd + cost);
            paths.put(destNode, (ResidualArc) arc);

            // There can be multiple instance of the same node in the queue.
            // This is the only way to update the queue.
            queue.offer(destNode);
        }
    }

    /**
     * 
     * @param distances
     * @return
     */
    private PriorityQueue<Node> createPriorityQueue(final Map<Node, Integer> distances) {
        return new PriorityQueue<Node>(
                distances.size(),
                new Comparator<Node>() {

                    public int compare(Node o1, Node o2) {
                        int d1 = distances.get(o1);
                        int d2 = distances.get(o2);

                        if (d1 < d2) {
                            return -1;
                        }
                        if (d1 == d2) {
                            return 0;
                        }
                        return 1;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return this == obj;
                    }
                });
    }

    /**
     * 
     * @param network
     * @return
     */
    private OrthogonalRepresentation computeOrthogonalRepresentation(FlowNetwork network) {
        OrthogonalRepresentation or = OrthogonalRepresentation.createGraph(network.getOriginalGraph());

        for (Arc arc : network.getArcs()) {
            if (arc.isVertexArc()) {
                Vertex v = arc.getSourceNode().getVertex();
                Face f = arc.getDestinationNode().getFace();
                OrthogonalShape shape = or.getShape(f);
                Dart d = (Dart) arc.getDart();

                Tuple t = shape.getTuple(d);
                t.setAngles(arc.getFlow() + 1);
            } else if (arc.isFaceArc()) {
                Node srcNode = arc.getSourceNode();
                Node destNode = arc.getDestinationNode();
                Face f = srcNode.getFace();
                Arc reverseArc = destNode.getArcToVia(srcNode, arc.getDart());
                OrthogonalShape shape = or.getShape(f);
                Dart d = arc.getDart();
                Tuple t = shape.getTuple(d);
                BitSet bends = t.getBends();

                int forwardFlow = arc.getFlow();
                int reverseFlow = reverseArc.getFlow();
                int sum = forwardFlow + reverseFlow;

                if (sum == 0) {
                    continue;
                }
                for (int i = 0; i < forwardFlow; i++) {
                    bends.clear(i);
                }

                for (int i = forwardFlow; i < sum; i++) {
                    bends.set(i);
                }

                bends.set(sum);
            }
        }

        return or;
    }
}
