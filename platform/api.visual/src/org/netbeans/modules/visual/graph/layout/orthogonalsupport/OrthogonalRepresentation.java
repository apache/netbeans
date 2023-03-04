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

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.Face.Dart;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class OrthogonalRepresentation<N, E> {

    private Map<Face, OrthogonalShape> shapes;
    private EmbeddedPlanarGraph<N, E> originalGraph;
    private Vertex<N> cornerVertex;

    public static <N, E> OrthogonalRepresentation<N, E> createGraph(EmbeddedPlanarGraph<N, E> graph) {
        return new OrthogonalRepresentation<>(graph);
    }

    private OrthogonalRepresentation(EmbeddedPlanarGraph<N, E> graph) {
        shapes = new LinkedHashMap<>();
        this.originalGraph = graph;
    }

    public EmbeddedPlanarGraph<N, E> getOriginalGraph() {
        return originalGraph;
    }

    public OrthogonalShape getShape(Face face) {
        OrthogonalShape shape = shapes.get(face);
        if (shape == null) {
            shape = new OrthogonalShape(face);
            shapes.put(face, shape);
        }

        return shape;
    }

    public Collection<OrthogonalShape> getShapes() {
        return shapes.values();
    }

    public void setCornerVertex(Vertex<N> cornerVertex) {
        this.cornerVertex = cornerVertex;
    }

    public Vertex<N> getCornerVertex() {
        return cornerVertex;
    }

    @Override
    public String toString() {
        String s = "Orthogonal Representation:\n";

        for (OrthogonalShape shape : shapes.values()) {
            s = s + shape;
        }

        return s;
    }

    public static class OrthogonalShape {

        private Map<Dart, Tuple> tupleMap;
        private Face face;

        OrthogonalShape(Face face) {
            this.face = face;
            tupleMap = new LinkedHashMap<>();
            for (Dart d : face.getDarts()) {
                Tuple t = new Tuple(d);
                tupleMap.put(d, t);
            }
        }

        public Face getFace() {
            return face;
        }

        public Tuple getTuple(Dart dart) {
            return tupleMap.get(dart);
        }

        public void updateTuple(Tuple tuple, Collection<Edge<?>> newEdges) {
            Dart originalDart = tuple.getDart();
            Edge<?> originalEdge = originalDart.getEdge();
            List<Dart> newDarts = face.replaceDart(originalDart, newEdges);
            tupleMap.remove(originalDart);
            BitSet bends = tuple.getBends();
            int size = newDarts.size();

            for (int i = 0; i < size; i++) {
                Dart newDart = newDarts.get(i);
                Tuple newTuple = new Tuple(newDart);
                tupleMap.put(newDart, newTuple);

                if (i == 0) {
                    newTuple.setAngles(tuple.getAngles());
                } else {
                    if (bends.length() > 0) {
                        boolean bit = bends.get(i - 1);
                        if (bit) {
                            newTuple.setAngles(3);
                        } else {
                            newTuple.setAngles(1);
                        }
                    } else {
                        newTuple.setAngles(2);
                    }
                }
            }

            // Check to see if there is a reverse dart
            Dart reverseDart = face.getDart(originalEdge);
            if (reverseDart != null) {
                Tuple reverseTuple = getTuple(reverseDart);
                updateTuple(reverseTuple, newEdges);
            }
        }

        public void insertEdge(Edge<?> edge) {
            List<Dart> removedDarts = face.replaceDarts(edge);

            if (removedDarts.isEmpty()) {
                return;
            }

            tupleMap.keySet().removeAll(removedDarts);

            // Create the new tuple
            Dart newDart = face.getDart(edge);
            Tuple newTuple = new Tuple(newDart);
            tupleMap.put(newDart, newTuple);
            newTuple.setAngles(1);

            // Update the angle of the next tuple to 180
            Dart nextDart = face.getNextDart(newDart);
            Tuple nextTuple = getTuple(nextDart);
            nextTuple.setAngles(2);
        }

        public String toString() {
            String s = "Shape:\n";
            s = s + face;

            return s;
        }
    }

    public static class Tuple {

        private Dart dart;
        private BitSet bends;
        private int angles;

        public Tuple(Dart dart) {
            this.dart = dart;
            bends = new BitSet();
        }

        public Dart getDart() {
            return dart;
        }

        public BitSet getBends() {
            return bends;
        }

        public int getNumberOfBends() {
            int length = bends.length();

            if (length > 0) {
                length--;
            }
            return length;
        }

        public int getAngles() {
            return angles;
        }

        public void setAngles(int angles) {
            if (angles == 0) {
                (new Exception()).printStackTrace();
            }
            this.angles = angles;
        }

        public String toString() {
            String s = "Tuple:\n";
            s = s + dart + "\n";
            s = s + "angles = " + angles + "\n";
            s = s + "bends = " + bends + "\n";
            s = s + "# of bends = " + getNumberOfBends() + "\n";
            s = s + "direction = " + dart.getEdge().getDirection() + "\n";
            return s;
        }
    }
}
