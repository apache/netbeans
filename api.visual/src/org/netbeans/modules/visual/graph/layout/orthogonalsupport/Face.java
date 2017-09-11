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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Edge.Direction;
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.MGraph.Vertex;

/**
 *
 * @author ptliu
 */
public class Face {

    private List<Edge> edges;
    private Set<Edge> edgeMap;
    private List<Dart> darts;
    private Set<Dart> dartMap;
    private Map<Dart, Integer> dartIndices;
    private boolean isOuterFace;

    /**
     * 
     */
    public Face() {
        edges = new ArrayList<Edge>();
        edgeMap = new HashSet<Edge>();
        darts = new ArrayList<Dart>();
        dartMap = new HashSet<Dart>();
        dartIndices = new HashMap<Dart, Integer>();
    }

    /**
     * 
     * @return
     */
    public int getDegree() {
        return edges.size();
    }

    /**
     * 
     * @param flag
     */
    public void setOuterFace(boolean flag) {
        this.isOuterFace = flag;
    }

    /**
     * 
     * @return
     */
    public boolean isOuterFace() {
        return isOuterFace;
    }

    /**
     * 
     * @return
     */
    public List<Vertex> getVertices() {
        List<Vertex> vertices = new ArrayList<Vertex>();
        List<Dart> _darts = getDarts();

        for (Dart dart : _darts) {
            vertices.add(dart.getV());
        }

        return vertices;
    }

    /**
     * 
     * @param edge
     */
    public void addEdge(Edge edge) {
        edgeMap.add(edge);
        edges.add(edge);
    }

    /**
     * 
     * @param newEdges
     */
    public void addEdges(Collection<Edge> newEdges) {
        this.edges.addAll(newEdges);
        this.edgeMap.addAll(newEdges);
    }

    /**
     * 
     * @param index
     * @param newEdge
     */
    private void addEdge(int index, Edge newEdge) {
        edges.add(index, newEdge);
        edgeMap.add(newEdge);
    }

    /**
     * 
     * @param edgeToRemove
     */
    private void removeEdge(Edge edgeToRemove) {
        edges.remove(edgeToRemove);

        // It's possible to have two entries of the same edge.
        if (!edges.contains(edgeToRemove)) {
            edgeMap.remove(edgeToRemove);
        }
    }

    /**
     * 
     * @param index
     */
    private void removeEdge(int index) {
        Edge removedEdge = edges.remove(index);

        // It's possible to have two entries of the same edge.
        if (!edges.contains(removedEdge)) {
            edgeMap.remove(removedEdge);
        }
    }

    /**
     * 
     * @return
     */
    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    /**
     * 
     * @param face
     * @return
     */
    public Edge getBorderingEdge(Face face) {
        for (Edge e : face.getEdges()) {
            if (edgeMap.contains(e)) {
                return e;
            }
        }

        return null;
    }

    /**
     * 
     * @param edgeToReplace
     * @param newEdges
     * @return
     */
    public List<Dart> replaceEdge(Edge edgeToReplace, Collection<Edge> newEdges) {
        HashSet<Edge> newEdgeSet = new HashSet<Edge>(newEdges);
        ArrayList<Dart> newDarts = new ArrayList<Dart>();
        Dart dartToReplace = getDart(edgeToReplace);
        Vertex v = dartToReplace.getV();
        Vertex w = dartToReplace.getW();
        int index = getDartIndex(dartToReplace);

        removeEdge(index);
        removeDart(index);

        while (v != w) {
            Edge newEdge = searchAndRemoveEdge(v, newEdgeSet);
            Vertex ov = newEdge.getOppositeVertex(v);
            Dart newDart = new Dart(v, ov, newEdge);

            addEdge(index, newEdge);
            addDart(index, newDart);
            newDarts.add(newDart);
            index++;
            v = ov;
        }

        return newDarts;
    }

    /**
     * 
     * @param dartToReplace
     * @param newEdges
     * @return
     */
    public List<Dart> replaceDart(Dart dartToReplace, Collection<Edge> newEdges) {
        HashSet<Edge> newEdgeSet = new HashSet<Edge>(newEdges);
        ArrayList<Dart> newDarts = new ArrayList<Dart>();
        Vertex v = dartToReplace.getV();
        Vertex w = dartToReplace.getW();
        int index = getDartIndex(dartToReplace);

        removeEdge(index);
        removeDart(index);

        while (v != w) {
            Edge newEdge = searchAndRemoveEdge(v, newEdgeSet);
            Vertex ov = newEdge.getOppositeVertex(v);
            Dart newDart = new Dart(v, ov, newEdge);
            addEdge(index, newEdge);
            addDart(index, newDart);
            newDarts.add(newDart);
            index++;
            v = ov;
        }

        return newDarts;
    }

    /**
     * 
     * @param edgeToAdd
     * @return
     */
    public List<Dart> replaceDarts(Edge edgeToAdd) {
        return replaceDarts(edgeToAdd, null);
    }

    /**
     * 
     * @param edgeToAdd
     * @param startingVertex
     * @return
     */
    public List<Dart> replaceDarts(Edge edgeToAdd, Vertex startingVertex) {
        ArrayList<Dart> removedDarts = new ArrayList<Dart>();
        Vertex v = null;
        Vertex w = null;

        if (startingVertex == null) {
            v = edgeToAdd.getV();
            w = edgeToAdd.getW();
        } else {
            v = startingVertex;
            w = edgeToAdd.getOppositeVertex(v);
        }

        List<Dart> dartsTo = getDartsTo(v);

        if (dartsTo.isEmpty()) {
            return removedDarts;
        }
        Dart firstDart = dartsTo.get(dartsTo.size() - 1);
        //Dart firstDart = getDartTo(v);

        if (firstDart == null) {
            return removedDarts;
        }
        Dart lastDart = getDartFrom(w);
        Dart currentDart = getNextDart(firstDart);

        while (currentDart != lastDart) {
            int index = darts.indexOf(currentDart);
            currentDart = getNextDart(currentDart);
            removeEdge(index);
            removedDarts.add(darts.get(index));
            removeDart(index);
        }

        Dart newDart = new Dart(v, w, edgeToAdd);
        int index = getDartIndex(firstDart) + 1;
        addDart(index, newDart);
        addEdge(index, edgeToAdd);

        return removedDarts;
    }

    /**
     * 
     * @param v
     * @param edges
     * @return
     */
    private Edge searchAndRemoveEdge(Vertex v, Collection<Edge> edges) {
        Edge edge = null;

        for (Edge e : edges) {
            if (e.contains(v)) {
                edge = e;
                break;
            }
        }

        edges.remove(edge);

        return edge;
    }

    /**
     * 
     * @return
     */
    public List<Dart> getDarts() {
        if (darts.size() != edges.size()) {
            createDarts();
        }

        return Collections.unmodifiableList(darts);
    }

    /**
     * 
     * @param startingVertex
     */
    public void createDarts(Vertex startingVertex) {
        Vertex prevVertex = null;

        darts.clear();
        dartMap.clear();

        for (Edge e : edges) {
            if (prevVertex == null) {
                Vertex v = e.getV();
                Vertex w = e.getW();

                if (startingVertex == null) {
                    if (isOuterFace) {
                        if (v.getNumber() < w.getNumber()) {
                            prevVertex = w;
                        } else {
                            prevVertex = v;
                        }
                    } else {
                        if (v.getNumber() < w.getNumber()) {
                            prevVertex = v;
                        } else {
                            prevVertex = w;
                        }
                    }
                } else {
                    prevVertex = startingVertex;
                }
            }

            Vertex nextVertex = e.getOppositeVertex(prevVertex);
            Dart dart = new Dart(prevVertex, nextVertex, e);
            addDart(dart);

            prevVertex = nextVertex;
        }
    }

    /**
     * 
     */
    public void createDarts() {
        createDarts(null);
    }

    /**
     * 
     * @param newDart
     */
    private void addDart(Dart newDart) {
        darts.add(newDart);
        dartMap.add(newDart);

        updateDartIndices(darts.size() - 1, null);
    }

    /**
     * 
     * @param index
     * @param newDart
     */
    private void addDart(int index, Dart newDart) {
        darts.add(index, newDart);
        dartMap.add(newDart);

        updateDartIndices(index, null);
    }

    /**
     * 
     * @param dartToRemove
     */
    private void removeDart(Dart dartToRemove) {
        int index = getDartIndex(dartToRemove);
        darts.remove(index);
        dartMap.remove(dartToRemove);

        updateDartIndices(index, dartToRemove);
    }

    /**
     * 
     * @param index
     */
    private void removeDart(int index) {
        Dart removedDart = darts.remove(index);
        dartMap.remove(removedDart);

        updateDartIndices(index, removedDart);
    }

    /**
     * 
     * @param index
     * @param removedDart
     */
    private void updateDartIndices(int index, Dart removedDart) {
        if (removedDart != null) {
            dartIndices.remove(removedDart);
        }

        int size = darts.size();
        for (int i = index; i < size; i++) {
            dartIndices.put(darts.get(i), i);
        }
    }

    /**
     * 
     * @param v
     * @return
     */
    public List<Dart> getDartsFrom(Vertex v) {
        ArrayList<Dart> darts = new ArrayList<Dart>();

        for (Dart d : getDarts()) {
            if (v == d.getV()) {
                darts.add(d);
            }
        }

        return darts;
    }

    /**
     * 
     * @param v
     * @return
     */
    public Dart getDartFrom(Vertex v) {
        for (Dart d : getDarts()) {
            if (v == d.getV()) {
                return d;
            }
        }

        return null;
    }

    /**
     * 
     * @param v
     * @return
     */
    public List<Dart> getDartsTo(Vertex v) {
        ArrayList<Dart> darts = new ArrayList<Dart>();

        for (Dart d : getDarts()) {
            if (v == d.getW()) {
                darts.add(d);
            }
        }

        return darts;
    }

    /**
     * 
     * @param v
     * @return
     */
    public Dart getDartTo(Vertex v) {
        for (Dart d : getDarts()) {
            if (v == d.getW()) {
                return d;
            }
        }

        return null;
    }

    /**
     * 
     * @param edge
     * @return
     */
    public Dart getDart(Edge edge) {
        for (Dart d : getDarts()) {
            if (d.getEdge() == edge) {
                return d;
            }
        }

        return null;
    }

    /**
     * 
     * @param edge
     * @return
     */
    public List<Dart> getDarts(Edge edge) {
        ArrayList<Dart> _darts = new ArrayList<Dart>();

        for (Dart d : getDarts()) {
            if (d.getEdge() == edge) {
                _darts.add(d);
            }
        }

        return _darts;
    }

    /**
     * 
     * @param edge
     * @param sourceVertex
     * @return
     */
    public Dart getDart(Edge edge, Vertex sourceVertex) {
        for (Dart d : getDarts()) {
            if (d.getEdge() == edge && d.getV() == sourceVertex) {
                return d;
            }
        }

        return null;
    }

    /**
     * 
     * @param face
     * @return
     */
    public Dart getBorderingDart(Face face) {
        Edge edge = getBorderingEdge(face);

        for (Dart d : getDarts()) {
            if (d.getEdge() == edge) {
                return d;
            }
        }

        return null;
    }

    /**
     * 
     * @param e
     * @return
     */
    public boolean containsEdge(Edge e) {
        return edgeMap.contains(e);
    }

    /**
     * 
     * @param d
     * @return
     */
    public boolean containsDart(Dart d) {
        return dartMap.contains(d);
    }

    /**
     * 
     * @param v
     * @return
     */
    public boolean containsVertex(Vertex v) {
        for (Edge e : edges) {
            if (e.contains(v)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param face
     * @return
     */
    public boolean connects(Face face) {
        for (Edge e : getEdges()) {
            for (Edge ne : face.getEdges()) {
                if (e.shareVertex(ne)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * TODO: need to optimize this.
     * @param face
     * @return
     */
    public boolean borders(Face face) {
        for (Edge e : face.edges) {
            if (edgeMap.contains(e)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param edge
     * @return
     */
    public Vertex getCornerVertex(Edge edge) {
        Edge nextEdge = getNextEdge(edge);
        Vertex v = edge.getV();
        Vertex w = edge.getW();
        Vertex nv = nextEdge.getV();
        Vertex nw = nextEdge.getW();

        if (v == nv || v == nw) {
            return v;
        }

        if (w == nv || w == nw) {
            return w;
        }

        // shouldn't happen!
        return null;
    }

    /**
     * RESOLVE: This should be a O(1) operation
     * instead of a linear operation.
     * @param e
     * @return
     */
    public Edge getNextEdge(Edge e) {
        int index = getEdgeIndex(e);

        return edges.get((index + 1) % edges.size());
    }

    /**
     * RESOLVE: Can't really use a Map here because there
     * can be multiple occurrences of the same edge.
     * @param e
     * @return
     */
    private int getEdgeIndex(Edge e) {
        return edges.indexOf(e);
    }

    /**
     * 
     * @param dart
     * @return
     */
    public Vertex getCornerVertex(Dart dart) {
        Dart nextDart = getNextDart(dart);
        Vertex v = dart.getV();
        Vertex w = dart.getW();
        Vertex nv = nextDart.getV();
        Vertex nw = nextDart.getW();

        if (v == nv || v == nw) {
            return v;
        }

        if (w == nv || w == nw) {
            return w;
        }

        // shouldn't happen!
        return null;
    }

    /**
     * 
     * @param dart
     * @return
     */
    public Dart getNextDart(Dart dart) {
        int index = getDartIndex(dart);

        if (index == -1) {
            return null;
        }
        return darts.get((index + 1) % darts.size());
    }

    /**
     * 
     * @param dart
     * @return
     */
    private int getDartIndex(Dart dart) {
        return dartIndices.get(dart);
    }

    /**
     * 
     */
    public void reverseDirection() {
        ArrayList<Edge> l = new ArrayList<Edge>(edges);
        edges.clear();
        edgeMap.clear();

        for (int i = l.size() - 1; i >= 0; i--) {
            addEdge(l.get(i));
        }

        darts.clear();
        dartMap.clear();
    }

    /**
     * 
     * @return
     */
    public String toString() {
        String s = "Face:\n";

        if (isOuterFace) {
            s = "Outer Face:\n";
        }

        s = s + "Edges:\n";
        for (Edge e : edges) {
            s = s + "\t" + e + "\n";
        }

        s = s + "Darts:\n";
        for (Dart d : getDarts()) {
            s = s + "\t" + d + "\n";
        }
        return s;
    }

    /**
     * The difference between a Dart and and Edge is that Dart
     * has direction. Each edge can have two Darts going in
     * the opposite direction.
     */
    public static class Dart extends Edge {

        private Edge e;

        /**
         * 
         * @param v
         * @param w
         * @param e
         */
        public Dart(Vertex v, Vertex w, Edge e) {
            super(v, w, null);
            this.e = e;

        }

        /**
         * 
         * @return
         */
        public Edge getEdge() {
            return e;
        }

        /**
         * 
         * @return
         */
        @Override
        public Direction getDirection() {
            return e.getDirection();
        }

        /**
         * 
         * @return
         */
        @Override
        public String toString() {
            return "dart : v = " + getV() + " w = " + getW() + " direction = " + getDirection();
        }
    }
}
