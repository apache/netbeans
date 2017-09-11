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
public class OrthogonalRepresentation {

    private Map<Face, OrthogonalShape> shapes;
    private EmbeddedPlanarGraph originalGraph;
    private Vertex cornerVertex;

    public static OrthogonalRepresentation createGraph(EmbeddedPlanarGraph graph) {
        return new OrthogonalRepresentation(graph);
    }

    private OrthogonalRepresentation(EmbeddedPlanarGraph graph) {
        shapes = new LinkedHashMap<Face, OrthogonalShape>();
        this.originalGraph = graph;
    }

    public EmbeddedPlanarGraph getOriginalGraph() {
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

    public void setCornerVertex(Vertex cornerVertex) {
        this.cornerVertex = cornerVertex;
    }

    public Vertex getCornerVertex() {
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

    public class OrthogonalShape {

        private Map<Dart, Tuple> tupleMap;
        private Face face;

        OrthogonalShape(Face face) {
            this.face = face;
            tupleMap = new LinkedHashMap<Dart, Tuple>();
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

        public void updateTuple(Tuple tuple, Collection<Edge> newEdges) {

            Dart originalDart = tuple.getDart();
            Edge originalEdge = originalDart.getEdge();
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

        public void insertEdge(Edge edge) {
            List<Dart> removedDarts = face.replaceDarts(edge);

            if (removedDarts.isEmpty()) {
                return;
            }

            for (Dart d : removedDarts) {
                tupleMap.remove(d);
            }

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

    public class Tuple {

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
