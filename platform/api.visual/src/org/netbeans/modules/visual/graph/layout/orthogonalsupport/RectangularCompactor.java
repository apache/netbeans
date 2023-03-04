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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.visual.graph.layout.OrthogonalLayout;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.DirectionalGraph.Bar;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.Face.Dart;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.DummyVertex;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge.Direction;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.OrthogonalRepresentation.OrthogonalShape;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.OrthogonalRepresentation.Tuple;

public class RectangularCompactor<N, E> {

    private static int startOfNextBar;

    /**
     * Creates a new instance of RectangularCompactor
     */
    public RectangularCompactor() {
    }

    /**
     * 
     * @param ors
     */
    public void compact(Collection<OrthogonalRepresentation<N, E>> ors) {
        startOfNextBar = 0;
        for (OrthogonalRepresentation<N, E> or : ors) {
            compact(or);
        }
    }

    /**
     * 
     * @param or
     */
    private void compact(OrthogonalRepresentation<N, E> or) {

        insertBendVertices(or);
        assignEdgeDirections(or);
        refineShapes(or);

        DirectionalGraph<N, E> hGraph = DirectionalGraph.createGraph(or, Direction.HORIZONTAL);

        Collection<Bar> bars = hGraph.getBars();
        Bar[] barArray = new Bar[bars.size()];
        bars.toArray(barArray);

        Arrays.sort(barArray);

        int x = startOfNextBar;

        int maxWidth = -1;
        for (Bar bar : barArray) {

//            int number = bar.getNumber();

            x += (maxWidth + OrthogonalLayout.gutter);
            maxWidth = -1;

            for (Vertex<?> v : bar.getVertices()) {

                if (v instanceof DummyVertex) {
                    continue;
                }

                v.setX((float) x);
                Dimension vDim = v.getSize();

                if (vDim != null) {
                    int vWidth = vDim.width;
                    if (vWidth > maxWidth) {
                        maxWidth = vWidth;
                    }
                }
            }

        }

        startOfNextBar = x + maxWidth ;

        DirectionalGraph<?, ?> vGraph = DirectionalGraph.createGraph(or, Direction.VERTICAL);

        bars = vGraph.getBars();
        barArray = new Bar[bars.size()];
        bars.toArray(barArray);

        Arrays.sort(barArray);

        int y = 0;

        int maxHeight = 0;
        for (Bar bar : barArray) {

            y += (maxHeight + OrthogonalLayout.gutter);
            maxHeight = -1;

            for (Vertex<?> v : bar.getVertices()) {

                if (v instanceof DummyVertex) {
                    continue;
                }

                v.setY((float) y);

                Dimension vDim = v.getSize();

                if (vDim != null) {
                    int vHeight = vDim.height;
                    if (vHeight > maxHeight) {
                        maxHeight = vHeight;
                    }
                }
            }

        }
    }

    /**
     * 
     * @param or
     */
    private void insertBendVertices(OrthogonalRepresentation<?, ?> or) {
        MGraph<?, ?> originalGraph = or.getOriginalGraph().getOriginalGraph();
        Map<Edge<?>, Collection<Edge<?>>> edgeMap = new HashMap<>();

        for (OrthogonalShape shape : or.getShapes()) {
            Face face = shape.getFace();

            // Work around current modification exception.
            List<Dart> darts = new ArrayList<>(face.getDarts());

            for (Dart dart : darts) {
                Tuple tuple = shape.getTuple(dart);
                if (tuple == null) {
                    continue;
                }

                int numOfBends = tuple.getNumberOfBends();

                if (numOfBends == 0) {
                    continue;
                }

                Edge<?> edge = dart.getEdge();
                Collection<Edge<?>> newEdges = edgeMap.get(edge);

                if (newEdges == null) {
                    newEdges = new ArrayList<>();
                    edgeMap.put(edge, newEdges);
                    Vertex<?> v = dart.getV();
                    Vertex<?> w = dart.getW();
                    for (int i = 0; i < numOfBends; i++) {
                        Vertex<?> dv = originalGraph.insertDummyVertex(edge, DummyVertex.Type.BEND);
                        newEdges.add(v.getEdge(dv));
                        edge = dv.getEdge(w);
                        v = dv;
                    }
                    newEdges.add(edge);
                }

                shape.updateTuple(tuple, newEdges);
            }
        }
    }

    /**
     * 
     * @param or
     */
    private void assignEdgeDirections(OrthogonalRepresentation<?, ?> or) {
        Face outerFace = or.getOriginalGraph().getFaces().get(0);
        Set<Face> visitedFace = new HashSet<>();
        OrthogonalShape startingShape = or.getShape(outerFace);
        Edge<?> startingEdge = startingShape.getFace().getDarts().get(0).getEdge();
        startingEdge.setDirection(Direction.HORIZONTAL);

        assignEdgeDirections(startingShape, startingEdge, or, visitedFace);

    }

    /**
     * 
     * @param shape
     * @param startingEdge
     * @param or
     * @param visitedFaces
     */
    private void assignEdgeDirections(OrthogonalShape shape, Edge<?> startingEdge,
            OrthogonalRepresentation<?, ?> or, Set<Face> visitedFaces) {
        Face face = shape.getFace();
        visitedFaces.add(face);
        EmbeddedPlanarGraph<?, ?> epg = or.getOriginalGraph();
        List<Dart> darts = face.getDarts();
        int startingIndex = 0;
        int size = darts.size();

        for (int i = 0; i < size; i++) {
            if (startingEdge == darts.get(i).getEdge()) {
                startingIndex = i;
                break;
            }
        }

        int index = startingIndex;
        Direction prevDirection = null;
        while (true) {
            Dart dart = darts.get(index);
            Edge<?> edge = dart.getEdge();
            Direction direction = edge.getDirection();

            if (direction == null) {
                Tuple t = shape.getTuple(dart);
                int angles = t.getAngles();
                direction = computeDirection(prevDirection, angles);
                edge.setDirection(direction);
            }

            Face oppositeFace = epg.getOppositeFace(face, dart);
            if (!oppositeFace.isOuterFace() && !visitedFaces.contains(oppositeFace)) {
                assignEdgeDirections(or.getShape(oppositeFace), edge, or, visitedFaces);
            }

            prevDirection = direction;
            index++;

            if (index == size) {
                index = 0;
            }

            if (index == startingIndex) {
                break;
            }
        }
    }

    /**
     * 
     * @param direction
     * @param turns
     * @return
     */
    private Direction computeDirection(Direction direction, int turns) {
        for (int i = 0; i < turns; i++) {
            if (direction == Direction.HORIZONTAL) {
                direction = Direction.VERTICAL;
            } else {
                direction = Direction.HORIZONTAL;
            }
        }

        return direction;
    }

    /**
     * 
     * @param or
     */
    private void refineShapes(OrthogonalRepresentation<N, E> or) {
        Map<Edge<?>, Collection<Edge<?>>> edgeMap = new HashMap<>();
        OrthogonalShape outerShape = null;

        for (OrthogonalShape shape : or.getShapes()) {
            Face face = shape.getFace();

            // We will deal with outer face separately because it is a special case.
            if (face.isOuterFace()) {
                outerShape = shape;
                continue;
            }

            refineShape(shape, or, edgeMap);
        }

        refineShape(outerShape, or, edgeMap);
        addDummyOuterFace(outerShape, or);
    }

    /**
     * 
     * @param shape
     * @param edgeMap
     */
    private void updateShape(OrthogonalShape shape, Map<Edge<?>, Collection<Edge<?>>> edgeMap) {
        if (edgeMap.isEmpty()) {
            return;
        }

        Face face = shape.getFace();
        Dart currentDart = face.getDarts().get(0);

        while (true) {
            Edge<?> edge = currentDart.getEdge();
            Collection<Edge<?>> newEdges = edgeMap.get(edge);

            if (newEdges != null) {
                Tuple tuple = shape.getTuple(currentDart);
                shape.updateTuple(tuple, newEdges);

                // start from the beginning
                currentDart = face.getDarts().get(0);
            } else {
                currentDart = face.getNextDart(currentDart);
                if (currentDart == face.getDarts().get(0)) {
                    break;
                }
            }
        }
    }

    /**
     * 
     * @param shape
     * @param or
     * @param edgeMap
     */
    private void refineShape(OrthogonalShape shape,
            OrthogonalRepresentation<?, ?> or,
            Map<Edge<?>, Collection<Edge<?>>> edgeMap) {
        updateShape(shape, edgeMap);

        // Do outer face last.
        // May need multiple passes.
        MGraph<?, ?> originalGraph = or.getOriginalGraph().getOriginalGraph();

        while (refineShapeSub(shape, originalGraph, edgeMap)) {
            //do nothing
        }
    }

    /**
     * 
     * @param shape
     * @param originalGraph
     * @param edgeMap
     * @return
     */
    private boolean refineShapeSub(OrthogonalShape shape, MGraph<?, ?> originalGraph,
            Map<Edge<?>, Collection<Edge<?>>> edgeMap) {
        Face face = shape.getFace();
        Logger.log(0, "refining face " + face);
        List<Dart> darts = new ArrayList<>(face.getDarts());
        Dart firstDart = darts.get(0);
        Dart currentDart = firstDart;

        while (true) {
            int turns = 0;
            Dart nextDart = face.getNextDart(currentDart);
            Dart frontDart = null;
            int numOfTurns = 0;

            Logger.log(0, "currentDart = " + currentDart);
            while (true) {
                Logger.log(0, "nextDart = " + nextDart);
                Tuple nextTuple = shape.getTuple(nextDart);
                int angles = nextTuple.getAngles();
                Logger.log(0, "angles = " + angles);

                numOfTurns++;

                if (angles == 1) {
                    turns -= 1;
                    Logger.log(0, "right turn");
                } else if (angles == 3) {
                    turns += 1;
                    Logger.log(0, "left turn");
                } else if (angles == 4) {
                    turns += 2;
                    Logger.log(0, "2 right turns");
                } else if (numOfTurns == 0) {
                    // ignore dart that starts with no bend
                    Logger.log(0, "break");
                    break;
                } else {
                    numOfTurns--;
                    Logger.log(0, "straight");
                }


                if (turns == -1 && numOfTurns > 1) {
                    frontDart = nextDart;
                    Logger.log(0, "FRONT DART = " + frontDart);
                    Logger.log(0, "CURRENT DART = " + currentDart);
                    break;
                } else if (numOfTurns == 4 || turns == 0) {
                    break;
                }

                if (nextDart == currentDart) {
                    break;
                }

                nextDart = face.getNextDart(nextDart);
            }

            if (frontDart != null) {
                Edge<?> currentEdge = currentDart.getEdge();
                Direction direction = currentEdge.getDirection();
                Tuple currentTuple = shape.getTuple(currentDart);

                if (currentTuple.getAngles() != -1) {
                    List<Edge<?>> newEdges = new ArrayList<>();
                    edgeMap.put(currentEdge, newEdges);

                    Vertex<?> dv = originalGraph.insertDummyVertex(currentEdge, DummyVertex.Type.TEMPORARY);
                    Edge<?> de = originalGraph.addDummyEdge(dv, frontDart.getV());

                    if (direction == Direction.VERTICAL) {
                        de.setDirection(Direction.HORIZONTAL);
                    } else {
                        de.setDirection(Direction.VERTICAL);
                    }

                    Vertex<?> currentV = currentDart.getV();
                    Edge<?> de1 = currentV.getEdge(dv);
                    de1.setDirection(direction);
                    newEdges.add(de1);

                    Vertex<?> currentW = currentDart.getW();
                    Edge<?> de2 = dv.getEdge(currentW);
                    de2.setDirection(direction);
                    newEdges.add(de2);

                    shape.updateTuple(shape.getTuple(currentDart), newEdges);
                    shape.insertEdge(de);
                } else {
                    Edge<?> de = originalGraph.addDummyEdge(currentDart.getV(), frontDart.getV());

                    if (direction == Direction.VERTICAL) {
                        de.setDirection(Direction.HORIZONTAL);
                    } else {
                        de.setDirection(Direction.VERTICAL);
                    }

                    shape.insertEdge(de);
                }
                return true;
            } else {
                currentDart = face.getNextDart(currentDart);
            }

            if (currentDart == firstDart) {
                break;
            }
        }

        return false;
    }

    /**
     * 
     * @param shape
     * @param or
     */
    private void addDummyOuterFace(OrthogonalShape shape,
            OrthogonalRepresentation<N, E> or) {
        MGraph<N, E> graph = or.getOriginalGraph().getOriginalGraph();
        List<Edge<?>> dummyEdges1 = new ArrayList<>();
        List<Edge<?>> dummyEdges2 = new ArrayList<>();

        Vertex<N> dv = graph.addDummyVertex(DummyVertex.Type.TEMPORARY);
        Vertex<N> cornerVertex = dv;

        for (int i = 0; i < 4; i++) {
            Vertex<N> dw = null;

            if (i < 3) {
                dw = graph.addDummyVertex(DummyVertex.Type.TEMPORARY);
            } else {
                dw = cornerVertex;
            }

            Edge<?> de = graph.addDummyEdge(dv, dw);
            dummyEdges1.add(de);

            if (i == 0 || i == 2) {
                de.setDirection(Direction.VERTICAL);
            } else {
                de.setDirection(Direction.HORIZONTAL);
            }

            //Logger.log (1,"dummy edge = " + de);
            dummyEdges2.add(null);

            dv = dw;
        }

        Face face = shape.getFace();
        List<Dart> darts = face.getDarts();
        Dart firstDart = darts.get(0);
        Dart lastDart = darts.get(darts.size() - 1);
        Dart currentDart = firstDart;
        int index = 0;
        int counter = 0;
        int firstIndex = -1;
        boolean needToSwap = false;
        boolean swapped = false;
        boolean projected = false;

        if (firstDart.getDirection() == Direction.VERTICAL) {
            index = 1;
        }
        while (true) {

            Dart nextDart = face.getNextDart(currentDart);
            int angles = shape.getTuple(nextDart).getAngles();

            if (angles == 3 || angles == 4) {
                int turns = 0;

                while (true) {
                    Tuple nextTuple = shape.getTuple(nextDart);
                    angles = nextTuple.getAngles();
                    if (angles == 1) {
                        turns -= 1;
                    } else if (angles == 3) {
                        turns += 1;
                    } else if (angles == 4) {
                        turns += 2;
                    }

                    if (turns == 4) {
                        projected = true;

                        if (firstIndex == -1) {
                            firstIndex = index;
                        }
                        Edge<?> de = dummyEdges1.get(index);
                        Direction direction = de.getDirection();
                        Vertex<?> v = de.getV();
                        Vertex<?> w = de.getW();
                        graph.insertDummyVertex(de, DummyVertex.Type.TEMPORARY);
                        dv = graph.insertDummyVertex(de, DummyVertex.Type.TEMPORARY);
                        de = graph.addDummyEdge(currentDart.getW(), dv);

                        if (direction == Direction.HORIZONTAL) {
                            de.setDirection(Direction.VERTICAL);
                        } else {
                            de.setDirection(Direction.HORIZONTAL);
                        }

                        Edge<?> e1 = dv.getEdge(w);
                        e1.setDirection(direction);
                        dummyEdges1.set(index, e1);

                        Edge<?> e2 = v.getEdge(dv);
                        e2.setDirection(direction);

                        if (dummyEdges2.get(index) == null) {
                            dummyEdges2.set(index, e2);
                        }
                        break;
                    }

                    if (nextDart == currentDart) {
                        break;
                    }

                    nextDart = face.getNextDart(nextDart);
                }
            }

            currentDart = face.getNextDart(currentDart);

            if (currentDart == firstDart) {
                break;
            }

            angles = shape.getTuple(currentDart).getAngles();

            if (angles == 1) {
                index--;
                if (index == -1) {
                    index = 3;
                }
                counter--;
            } else if (angles == 3) {
                index++;
                if (index == 4) {
                    index = 0;
                }
                counter++;
            } else if (angles == 4) {
                index += 2;
                if (index >= 4) {
                    index -= 4;
                }
                counter += 2;
            }

            if (counter == 3 && !needToSwap) {
                needToSwap = true;
            }

            if (needToSwap && !swapped) {
                if (index == firstIndex) {
                    swapped = true;

                    Edge<?> de = dummyEdges2.get(firstIndex);
                    if (de != null) {
                        dummyEdges1.set(firstIndex, de);
                    }
                }
            }
        }

        if (projected) {
            or.setCornerVertex(cornerVertex);
        }
    }
}
