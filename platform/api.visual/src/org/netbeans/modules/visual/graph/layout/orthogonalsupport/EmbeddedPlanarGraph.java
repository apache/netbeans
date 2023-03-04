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
import org.netbeans.modules.visual.graph.layout.orthogonalsupport.Face.Dart;

/**
 *
 * @author ptliu
 */
public class EmbeddedPlanarGraph<N, E> {

    private MGraph<N, E> originalGraph;
    private ArrayList<Face> faces;

    /**
     * 
     * @param graph
     * @return
     */
    public static <N, E> EmbeddedPlanarGraph<N, E> createGraph(MGraph<N, E> graph) {
        return new EmbeddedPlanarGraph<>(graph);
    }

    /**
     * 
     * @param graph
     */
    private EmbeddedPlanarGraph(MGraph<N, E> graph) {
        this.originalGraph = graph;
        faces = new ArrayList<>();
    }

    /**
     * 
     * @return
     */
    public MGraph<N, E> getOriginalGraph() {
        return originalGraph;
    }

    /**
     * 
     * @return
     */
    public ArrayList<Face> getFaces() {
        return faces;
    }

    /**
     * 
     * @param newFace
     */
    public void addFace(Face newFace) {
        if (!faces.contains(newFace)) {
            faces.add(newFace);
        }
    }

    /**
     * 
     * @param newFaces
     */
    public void addFaces(Collection<Face> newFaces) {
        faces.addAll(newFaces);
    }

    /**
     * 
     * @param faceToRemove
     */
    public void removeFace(Face faceToRemove) {
        faces.remove(faceToRemove);
    }

    /**
     * 
     * @param facesToRemove
     */
    public void removeFaces(Collection<Face> facesToRemove) {
        faces.removeAll(facesToRemove);
    }

    /**
     * 
     * @param face
     * @param dart
     * @return
     */
    public Face getOppositeFace(Face face, Dart dart) {
        for (Face f : faces) {
            if (f != face) {
                if (f.containsEdge(dart.getEdge())) {
                    return f;
                }
            }
        }

        return null;
    }

    /**
     * 
     * @return
     */
    public Face getOuterFace() {
        for (Face face : faces) {
            if (face.isOuterFace()) {
                return face;
            }
        }

        return null;
    }
}

