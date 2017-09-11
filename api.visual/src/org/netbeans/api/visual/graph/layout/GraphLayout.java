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

    @SuppressWarnings ("unchecked")
    private <N,E> GraphLayoutListener<N,E>[] createListenersCopy () {
        GraphLayoutListener<N,E>[] listeners;
        synchronized (this.listeners) {
            listeners = this.listeners.toArray (new GraphLayoutListener[this.listeners.size ()]);
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
