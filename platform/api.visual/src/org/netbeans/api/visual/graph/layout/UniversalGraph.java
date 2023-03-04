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
package org.netbeans.api.visual.graph.layout;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.GraphPinScene;

import java.util.Collection;
import java.util.ArrayList;

/**
 * This interface represents an universal graph.
 * It unifies a view on a GraphScene and GraphPinScene as a nodes-edges only graph.
 * This is commonly used by graph-oriented layout algorithms.
 *
 * @author David Kaspar
 */
public abstract class UniversalGraph<N,E> {

    /**
     * Returns a related scene as a ObjectScene.
     * @return the related scene which is represented by the universal graph.
     */
    public abstract ObjectScene getScene ();

    /**
     * Returns a collection of nodes in the graph.
     * @return the collection of nodes
     */
    public abstract Collection<N> getNodes ();

    /**
     * Returns a collection of edges in the graph.
     * @return the collection of edges
     */
    public abstract Collection<E> getEdges ();

    /**
     * Returns edges that are attached to a specified node.
     * @param node the node
     * @param allowsOutputEdges if true then it finds all edges that has the node as their source
     * @param allowsInputEdges if true then it finds all edges that has the node as their target
     * @return the collection of all found edges
     */
    public abstract Collection<E> findNodeEdges (N node, boolean allowsOutputEdges, boolean allowsInputEdges);

    /**
     * Returns an edge source.
     * @param edge the edge
     * @return the edge source
     */
    public abstract N getEdgeSource (E edge);

    /**
     * Returns an edge target.
     * @param edge the edge
     * @return the edge target
     */
    public abstract N getEdgeTarget (E edge);

    
    static <N,E> UniversalGraph<N, E> createUniversalGraph (final GraphScene<N, E> scene) {
        return new UniversalGraph<N, E>() {

            public ObjectScene getScene () {
                return scene;
            }

            public Collection<N> getNodes () {
                return scene.getNodes ();
            }

            public Collection<E> getEdges () {
                return scene.getEdges ();
            }

            public Collection<E> findNodeEdges (N node, boolean allowsOutputEdges, boolean allowsInputEdges) {
                return scene.findNodeEdges (node, allowsOutputEdges, allowsInputEdges);
            }

            public N getEdgeSource (E edge) {
                return scene.getEdgeSource (edge);
            }

            public N getEdgeTarget (E edge) {
                return scene.getEdgeTarget (edge);
            }

        };
    }

    static <N,E,P> UniversalGraph<N, E> createUniversalGraph (final GraphPinScene<N, E, P> scene) {
        return new UniversalGraph<N, E>() {

            public ObjectScene getScene () {
                return scene;
            }

            public Collection<N> getNodes () {
                return scene.getNodes ();
            }

            public Collection<E> getEdges () {
                return scene.getEdges ();
            }

            public Collection<E> findNodeEdges (N node, boolean allowsOutputEdges, boolean allowsInputEdges) {
                ArrayList<E> list = new ArrayList<E> ();
                for (P pin : scene.getNodePins (node))
                    list.addAll (scene.findPinEdges (pin, allowsOutputEdges, allowsInputEdges));
                return list;
            }

            public N getEdgeSource (E edge) {
                return scene.getPinNode (scene.getEdgeSource (edge));
            }

            public N getEdgeTarget (E edge) {
                return scene.getPinNode (scene.getEdgeTarget (edge));
            }

        };
    }

}

