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
package org.netbeans.api.visual.graph.layout;

import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;

import java.util.Collection;
import java.util.ArrayList;
import java.awt.*;

/**
 * This class represents a graph-orinted layout.
 * The layout is invoked using layoutGraph methods.
 * The layoutNodes method could be called for resolving locations of a sub-set of all nodes.
 * <p>
 * Internally the invoked method creates an UniversalGraph from the scene in the arguments.
 * Then it calls the particular (performGraphLayout or performNodesLayout) methods with the UniversalGraph as a parameter.
 * These protected methods are implemented by the GraphLayout based class and performs particular layout using an UniversalGraph.
 *
 * @author David Kaspar
 */
public abstract class GraphLayout<N,E> {

    private boolean animated = true;
    private final ArrayList<GraphLayoutListener<N,E>> listeners = new ArrayList<GraphLayoutListener<N,E>> ();

    /**
     * Returns whether the layout uses animations.
     * @return true if animated
     */
    public final boolean isAnimated () {
        return animated;
    }

    /**
     * Sets whether the layout is animated.
     * @param animated if true, then the layout is animated
     */
    public final void setAnimated (boolean animated) {
        this.animated = animated;
    }

    /**
     * Adds a graph layout listener.
     * @param listener the graph layout listener
     */
    public final void addGraphLayoutListener (GraphLayoutListener<N,E> listener) {
        synchronized (listeners) {
            listeners.add (listener);
        }
    }

    /**
     * Removes a graph layout listener.
     * @param listener the graph layout listener
     */
    public final void removeGraphLayoutListener (GraphLayoutListener<N,E> listener) {
        synchronized (listeners) {
            // .add replaced with .remove for fix to bug http://netbeans.org/bugzilla/show_bug.cgi?id=197502
            // listeners.add (listener);
            listeners.remove (listener);
        }
    }

    /**
     * Invokes graph-oriented layout on a GraphScene.
     * @param graphScene the graph scene
     */
    public final void layoutGraph (GraphScene<N,E> graphScene) {
        GraphLayoutListener<N,E>[] listeners = createListenersCopy ();

        UniversalGraph<N,E> graph = UniversalGraph.createUniversalGraph (graphScene);

        for (GraphLayoutListener<N,E> listener : listeners)
            listener.graphLayoutStarted (graph);

        performGraphLayout (graph);

        for (GraphLayoutListener<N,E> listener : listeners)
            listener.graphLayoutFinished (graph);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <N,E> GraphLayoutListener<N,E>[] createListenersCopy () {
        GraphLayoutListener<N,E>[] listeners;
        synchronized (this.listeners) {
            listeners = this.listeners.toArray (new GraphLayoutListener[0]);
        }
        return listeners;
    }

    /**
     * Invokes graph-oriented layout on a GraphPinScene.
     * @param graphPinScene the graph pin scene
     */
    public final void layoutGraph (GraphPinScene<N,E,?> graphPinScene) {
        GraphLayoutListener<N,E>[] listeners = createListenersCopy ();

        UniversalGraph<N,E> graph = UniversalGraph.createUniversalGraph (graphPinScene);

        for (GraphLayoutListener<N,E> listener : listeners)
            listener.graphLayoutStarted (graph);

        performGraphLayout (graph);

        for (GraphLayoutListener<N,E> listener : listeners)
            listener.graphLayoutFinished (graph);
    }

    /**
     * Invokes resolving of locations for a collection of nodes in a GraphScene.
     * @param graphScene the graph scene
     * @param nodes the collection of nodes to resolve
     */
    public final void layoutNodes (GraphScene<N,E> graphScene, Collection<N> nodes) {
        GraphLayoutListener<N,E>[] listeners = createListenersCopy ();

        UniversalGraph<N, E> graph = UniversalGraph.createUniversalGraph (graphScene);

        for (GraphLayoutListener<N, E> listener : listeners)
            listener.graphLayoutStarted (graph);

        performNodesLayout (graph, nodes);

        for (GraphLayoutListener<N, E> listener : listeners)
            listener.graphLayoutFinished (graph);
    }

    /**
     * Invokes resolving of locations for a collection of nodes in a GraphPinScene.
     * @param graphPinScene the graph pin scene
     * @param nodes the collection of nodes to resolve
     */
    public final void layoutNodes (GraphPinScene<N,E,?> graphPinScene, Collection<N> nodes) {
        GraphLayoutListener<N,E>[] listeners = createListenersCopy ();

        UniversalGraph<N, E> graph = UniversalGraph.createUniversalGraph (graphPinScene);

        for (GraphLayoutListener<N, E> listener : listeners)
            listener.graphLayoutStarted (graph);

        performNodesLayout (graph, nodes);

        for (GraphLayoutListener<N, E> listener : listeners)
            listener.graphLayoutFinished (graph);
    }

    /**
     * Should be called to set a new resolved preferred location of a node.
     * @param graph the universal graph
     * @param node the node with resolved location
     * @param newPreferredLocation the new resolved location
     */
    protected final void setResolvedNodeLocation (UniversalGraph<N,E> graph, N node, Point newPreferredLocation) {
        ObjectScene scene = graph.getScene ();

        Widget widget = scene.findWidget (node);
        if (widget == null)
            return;

        Point previousPreferredLocation = widget.getPreferredLocation ();

        if (animated)
            scene.getSceneAnimator ().animatePreferredLocation (widget, newPreferredLocation);
        else
            widget.setPreferredLocation (newPreferredLocation);

        GraphLayoutListener<N,E>[] listeners = createListenersCopy ();

        for (GraphLayoutListener<N,E> listener : listeners)
            listener.nodeLocationChanged (graph, node, previousPreferredLocation, newPreferredLocation);
    }

    /**
     * Implements and performs particular graph-oriented algorithm of a UniversalGraph.
     * Call <code>GraphLayout.setResolvedNodeLocation</code> method for setting the resolved node location.
     * @param graph the universal graph on which the layout should be performed
     */
    protected abstract void performGraphLayout (UniversalGraph<N,E> graph);

    /**
     * Implements and performs particular location resolution of a collection of nodes in a UniversalGraph.
     * Call <code>GraphLayout.setResolvedNodeLocation</code> method for setting the resolved node location.
     * @param graph the universal graph on which the nodes should be resolved
     * @param nodes the collection of nodes to be resolved
     */
    protected abstract void performNodesLayout (UniversalGraph<N,E> graph, Collection<N> nodes);

}
