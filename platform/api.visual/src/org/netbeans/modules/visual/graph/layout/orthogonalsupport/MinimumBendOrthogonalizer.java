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
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
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
    public <N, E> Collection<OrthogonalRepresentation<N, E>> orthogonalize(
            Collection<EmbeddedPlanarGraph<N, E>> epgs) {
        Collection<OrthogonalRepresentation<N, E>> ors = new ArrayList<>();

        for (EmbeddedPlanarGraph<N, E> epg : epgs) {
            FlowNetwork<N, E> network = FlowNetwork.createGraph(epg);

            computeMinimumCostFlowNetwork(network);

            network.removeSourceAndSink();
            OrthogonalRepresentation<N, E> or = computeOrthogonalRepresentation(network);
            ors.add(or);

        }

        return ors;
    }

    /**
     * 
     * @param network
     */
    private <N, E> void computeMinimumCostFlowNetwork(FlowNetwork<N, E> network) {
        ResidualFlowNetwork<N, E> residualNetwork = new ResidualFlowNetwork<>(network);

        int totalFlow = 0;
        int production = network.getSource().getProduction();


        while (totalFlow < production) {
            Collection<ResidualArc<N>> path = computeDijkstraShortestPath(residualNetwork);

            // Flow is blocked, we simply return;
            if (path == null) {
                return;
            }
            int minFlow = Integer.MAX_VALUE;

            for (Arc<N> arc : path) {
                int capacity = arc.getCapacity();
                if (capacity < minFlow) {
                    minFlow = capacity;
                }
            }

            totalFlow += minFlow;

            for (ResidualArc<N> ra : path) {
                Arc<N> arc = ra.getArc();
                ResidualArc<N> residualArc = null;
                ResidualArc<N> reverseResidualArc = null;
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
    private <N, E> Collection<ResidualArc<N>> computeDijkstraShortestPath(ResidualFlowNetwork<N, E> network) {
        Collection<Node<N>> nodes = network.getNodes();
        Map<Node<N>, ResidualArc<N>> paths = new HashMap<>();
        final Map<Node<N>, Integer> distances = new HashMap<>();
        Node<N> sourceNode = network.getSource();

        for (Node<N> node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(sourceNode, 0);

        PriorityQueue<Node<N>> priorityQueue = createPriorityQueue(distances);
        for (Node<N> node : nodes) {
            priorityQueue.offer(node);
        }

        Set<Node<N>> visitedNodes = new HashSet<Node<N>>();

        while (priorityQueue.size() > 0) {
            Node<N> node = priorityQueue.poll();

            if (distances.get(node) == Integer.MAX_VALUE) {
                continue;
            }
            if (visitedNodes.contains(node)) {
                continue;
            }
            for (Arc<N> arc : node.getOutputArcs()) {
                if (arc.getCapacity() > 0) {
                    computeRelaxation(arc, paths, distances, priorityQueue);
                }
            }

            visitedNodes.add(node);
        }

        Collection<ResidualArc<N>> shortestPath = new ArrayList<>();
        Node<N> currentNode = network.getSink();

        while (currentNode != sourceNode) {
            Arc<N> arc = paths.get(currentNode);

            if (arc == null) {
                return null;
            }
            shortestPath.add((ResidualArc<N>) arc);
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
    private <N> void computeRelaxation(Arc<N> arc, Map<Node<N>, ResidualArc<N>> paths,
            Map<Node<N>, Integer> distances, PriorityQueue<Node<N>> queue) {
        Node<N> srcNode = arc.getSourceNode();
        Node<N> destNode = arc.getDestinationNode();
        int sd = distances.get(srcNode);
        int dd = distances.get(destNode);
        int cost = arc.getCost();

        if (dd > sd + cost) {
            distances.put(destNode, sd + cost);
            paths.put(destNode, (ResidualArc<N>) arc);

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
    private <N> PriorityQueue<Node<N>> createPriorityQueue(final Map<Node<N>, Integer> distances) {
        return new PriorityQueue<>(
                distances.size(),
                new Comparator<Node<?>>() {

                    public int compare(Node<?> o1, Node<?> o2) {
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
    private <N, E> OrthogonalRepresentation<N, E> computeOrthogonalRepresentation(FlowNetwork<N, E> network) {
        OrthogonalRepresentation<N, E> or = OrthogonalRepresentation.createGraph(network.getOriginalGraph());

        for (Arc<N> arc : network.getArcs()) {
            if (arc.isVertexArc()) {
                Vertex<N> v = arc.getSourceNode().getVertex();
                Face f = arc.getDestinationNode().getFace();
                OrthogonalShape shape = or.getShape(f);
                Dart d = arc.getDart();

                Tuple t = shape.getTuple(d);
                t.setAngles(arc.getFlow() + 1);
            } else if (arc.isFaceArc()) {
                Node<N> srcNode = arc.getSourceNode();
                Node<N> destNode = arc.getDestinationNode();
                Face f = srcNode.getFace();
                Arc<N> reverseArc = destNode.getArcToVia(srcNode, arc.getDart());
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
