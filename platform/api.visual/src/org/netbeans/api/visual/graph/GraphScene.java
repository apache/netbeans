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
package org.netbeans.api.visual.graph;

import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.util.GeomUtil;

import java.util.*;

/**
 * This class holds and manages graph-oriented model.
 * <p>
 * In comparison with the GraphScene class, in this class the graph consists of nodes and edges. Each edge could be attach to a single source and target node.
 * <p>
 * The class is abstract and manages only data model and mapping with widgets. The graphics (widgets) has to be supplied
 * by a developer by overriding the attachNodeWidget, attachEdgeWidget, attachEdgeSourceAnchor and attachEdgeTargetAnchor abstract methods.
 * <p>
 * This class is using generics and allows you to specify type representation for nodes and edges in the graph model. Example:
 * <pre>
 * class MyGraph extends GraphScene&lt;MyNode, MyEdge&gt; { ... }
 * </pre>
 * Since the type of nodes and edges could be the same, all node and edge instances has to be unique within the whole scene.
 * <p>
 * Node and Edge should not be a Widget. It should work properly but in that case the ObjectScene class is loosing its purpose
 * because there is no need to manage a mapping of an instance to the same instance.
 *
 * @author David Kaspar
 */
public abstract class GraphScene<N, E> extends ObjectScene {

    private HashSet<N> nodes = new HashSet<N> ();
    private Set<N> nodesUm = Collections.unmodifiableSet (nodes);

    private HashSet<E> edges = new HashSet<E> ();
    private Set<E> edgesUm = Collections.unmodifiableSet (edges);

    private HashMap<E, N> edgeSourceNodes = new HashMap<E, N> ();
    private HashMap<E, N> edgeTargetNodes = new HashMap<E, N> ();

    private HashMap<N, List<E>> nodeInputEdges = new HashMap<N, List<E>> ();
    private HashMap<N, List<E>> nodeOutputEdges = new HashMap<N, List<E>> ();

    /**
     * Creates a graph scene.
     */
    public GraphScene () {
    }

    /**
     * Adds a node.
     * @param node the node to be added; the node must not be null, must not be already in the model, must be unique in the model
     *           (means: there is no other node or edge in the model has is equal to this node)
     *           and must not be a Widget
     * @return the widget that is created by attachNodeWidget; null if the node is non-visual
     */
    public final Widget addNode (N node) {
        assert node != null  &&  ! nodes.contains (node);
        Widget widget = attachNodeWidget (node);
        nodes.add (node);
        nodeInputEdges.put (node, new ArrayList<E> ());
        nodeOutputEdges.put (node, new ArrayList<E> ());
        addObject (node, widget);
        notifyNodeAdded (node, widget);
        return widget;
    }

    /**
     * Removes a node.
     * @param node the node to be removed; the node must not be null and must be already in the model
     */
    public final void removeNode (N node) {
        assert node != null  &&  nodes.contains (node);
        for (E edge : findNodeEdges (node, true, false))
            setEdgeSource (edge, null);
        for (E edge : findNodeEdges (node, false, true))
            setEdgeTarget (edge, null);
        nodeInputEdges.remove (node);
        nodeOutputEdges.remove (node);
        nodes.remove (node);
        Widget widget = findWidget (node);
        detachNodeWidget (node, widget);
        removeObject (node);
    }

    /**
     * Removes a specified node with all edges that are attached to the node.
     * @param node the node to be removed
     */
    public final void removeNodeWithEdges (N node) {
        for (E edge : findNodeEdges (node, true, true))
            if (isEdge (edge))
                removeEdge (edge);
        removeNode (node);
    }

    /**
     * Returns a collection of all nodes registered in the graph model.
     * @return the collection of all nodes registered in the graph model
     */
    public final Collection<N> getNodes () {
        return nodesUm;
    }

    /**
     * Adds an edge.
     * @param edge the edge to be added; the edge must not be null, must not be already in the model, must be unique in the model
     *           (means: there is no other node or edge in the model has is equal to this edge)
     *           and must not be a Widget
     * @return the widget that is created by attachEdgeWidget; null if the edge is non-visual
     */
    public final Widget addEdge (E edge) {
        assert edge != null  &&  ! edges.contains (edge);
        Widget widget = attachEdgeWidget (edge);
        edges.add (edge);
        addObject (edge, widget);
        notifyEdgeAdded (edge, widget);
        return widget;
    }

    /**
     * Removes an edge and detaches it from its source and target nodes.
     * @param edge the edge to be removed; the edge must not be null and must be already in the model
     */
    public final void removeEdge (E edge) {
        assert edge != null  &&  edges.contains (edge);
        setEdgeSource (edge, null);
        setEdgeTarget (edge, null);
        edges.remove (edge);
        edgeSourceNodes.remove (edge);
        edgeTargetNodes.remove (edge);
        Widget widget = findWidget (edge);
        detachEdgeWidget (edge, widget);
        removeObject (edge);
    }

    /**
     * Returns a collection of all edges registered in the graph model.
     * @return the collection of all edges registered in the graph model
     */
    public final Collection<E> getEdges () {
        return edgesUm;
    }

    /**
     * Sets an edge source.
     * @param edge the edge which source is going to be changed
     * @param sourceNode the source node; if null, then the edge source will be detached
     */
    public final void setEdgeSource (E edge, N sourceNode) {
        assert edge != null  &&  edges.contains (edge);
        if (sourceNode != null)
            assert nodes.contains (sourceNode);
        N oldNode = edgeSourceNodes.put (edge, sourceNode);
        if (GeomUtil.equals (oldNode, sourceNode))
            return;
        if (oldNode != null)
            nodeOutputEdges.get (oldNode).remove (edge);
        if (sourceNode != null)
            nodeOutputEdges.get (sourceNode).add (edge);
        attachEdgeSourceAnchor (edge, oldNode, sourceNode);
    }

    /**
     * Sets an edge target.
     * @param edge the edge which target is going to be changed
     * @param targetNode the target node; if null, then the edge target will be detached
     */
    public final void setEdgeTarget (E edge, N targetNode) {
        assert edge != null  &&  edges.contains (edge);
        if (targetNode != null)
            assert nodes.contains (targetNode);
        N oldNode = edgeTargetNodes.put (edge, targetNode);
        if (GeomUtil.equals (oldNode, targetNode))
            return;
        if (oldNode != null)
            nodeInputEdges.get (oldNode).remove (edge);
        if (targetNode != null)
            nodeInputEdges.get (targetNode).add (edge);
        attachEdgeTargetAnchor (edge, oldNode, targetNode);
    }

    /**
     * Returns an edge source.
     * @param edge the edge
     * @return the edge source; null, if edge does not have source attached
     */
    public final N getEdgeSource (E edge) {
        return edgeSourceNodes.get (edge);
    }

    /**
     * Returns an edge target.
     * @param edge the edge
     * @return the edge target; null, if edge does not have target attached
     */
    public final N getEdgeTarget (E edge) {
        return edgeTargetNodes.get (edge);
    }

    /**
     * Returns a collection of edges that are attached to a specified node.
     * @param node the node which edges connections are searched for
     * @param allowOutputEdges if true, the output edges are included in the collection; if false, the output edges are not included
     * @param allowInputEdges if true, the input edges are included in the collection; if false, the input edges are not included
     * @return the collection of edges
     */
    public final Collection<E> findNodeEdges (N node, boolean allowOutputEdges, boolean allowInputEdges) {
        assert isNode (node) : "Node " + node + " does not exist in the scene";
        ArrayList<E> list = new ArrayList<E> ();
        if (allowInputEdges)
            list.addAll (nodeInputEdges.get (node));
        if (allowOutputEdges)
            list.addAll (nodeOutputEdges.get (node));
        return list;
    }

    /**
     * Returns a collection of edges that are between a specified source and target nodes.
     * @param sourceNode the source node
     * @param targetNode the target node
     * @return the collection of edges with the specified source and target nodes
     */
    public final Collection<E> findEdgesBetween (N sourceNode, N targetNode) {
        assert isNode (sourceNode) : "Source node " + sourceNode + " is not in the scene";
        assert isNode (targetNode) : "Target node " + targetNode + " is not in the scene";
        HashSet<E> list = new HashSet<E> ();
        List<E> inputEdges = nodeInputEdges.get (targetNode);
        List<E> outputEdges = nodeOutputEdges.get (sourceNode);
        for (E edge : inputEdges)
            if (outputEdges.contains (edge))
                list.add (edge);
        return list;
    }

    /**
     * Checks whether an object is registered as a node in the graph model.
     * @param object the object; must not be a Widget
     * @return true, if the object is registered as a node
     */
    public boolean isNode (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return nodes.contains (object);
    }

    /**
     * Checks whether an object is registered as a edge in the graph model.
     * @param object the object; must not be a Widget
     * @return true, if the object is registered as a edge
     */
    public boolean isEdge (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return edges.contains (object);
    }

    /**
     * Called by the addNode method to notify that a node is added into the graph model.
     * @param node the added node
     * @param widget the widget created by the attachNodeWidget method as a visual representation of the node
     */
    protected void notifyNodeAdded (N node, Widget widget) {
    }

    /**
     * Called by the addEdge method to notify that an edge is added into the graph model.
     * @param edge the added node
     * @param widget the widget created by the attachEdgeWidget method as a visual representation of the edge
     */
    protected void notifyEdgeAdded (E edge, Widget widget) {
    }

    /**
     * Called by the removeNode method to notify that a node is removed from the graph model.
     * The default implementation removes the node widget from its parent widget.
     * @param node the removed node
     * @param widget the removed node widget; null if the node is non-visual
     */
    protected void detachNodeWidget (N node, Widget widget) {
        if (widget != null)
            widget.removeFromParent ();
    }

    /**
     * Called by the removeEdge method to notify that an edge is removed from the graph model.
     * The default implementation removes the edge widget from its parent widget.
     * @param edge the removed edge
     * @param widget the removed edge widget; null if the edge is non-visual
     */
    protected void detachEdgeWidget (E edge, Widget widget) {
        if (widget != null)
            widget.removeFromParent ();
    }

    /**
     * Called by the addNode method before the node is registered to acquire a widget that is going to represent the node in the scene.
     * The method is responsible for creating the widget, adding it into the scene and returning it from the method.
     * @param node the node that is going to be added
     * @return the widget representing the node; null, if the node is non-visual
     */
    protected abstract Widget attachNodeWidget (N node);

    /**
     * Called by the addEdge method before the edge is registered to acquire a widget that is going to represent the edge in the scene.
     * The method is responsible for creating the widget, adding it into the scene and returning it from the method.
     * @param edge the edge that is going to be added
     * @return the widget representing the edge; null, if the edge is non-visual
     */
    protected abstract Widget attachEdgeWidget (E edge);

    /**
     * Called by the setEdgeSource method to notify about the changing the edge source in the graph model.
     * The method is responsible for attaching a new source node to the edge in the visual representation.
     * <p>
     * Usually it is implemented as:
     * <pre>
     * Widget sourceNodeWidget = findWidget (sourceNode);
     * Anchor sourceAnchor = AnchorFactory.createRectangularAnchor (sourceNodeWidget)
     * ConnectionWidget edgeWidget = (ConnectionWidget) findWidget (edge);
     * edgeWidget.setSourceAnchor (sourceAnchor);
     * </pre>
     * @param edge the edge which source is changed in graph model
     * @param oldSourceNode the old source node
     * @param sourceNode the new source node
     */
    protected abstract void attachEdgeSourceAnchor (E edge, N oldSourceNode, N sourceNode);

    /**
     * Called by the setEdgeTarget method to notify about the changing the edge target in the graph model.
     * The method is responsible for attaching a new target node to the edge in the visual representation.
     * <p>
     * Usually it is implemented as:
     * <pre>
     * Widget targetNodeWidget = findWidget (targetNode);
     * Anchor targetAnchor = AnchorFactory.createRectangularAnchor (targetNodeWidget)
     * ConnectionWidget edgeWidget = (ConnectionWidget) findWidget (edge);
     * edgeWidget.setTargetAnchor (targetAnchor);
     * </pre>
     * @param edge the edge which target is changed in graph model
     * @param oldTargetNode the old target node
     * @param targetNode the new target node
     */
    protected abstract void attachEdgeTargetAnchor (E edge, N oldTargetNode, N targetNode);

    /**
     * This class is a particular GraphScene where nodes and edges are represented with String class.
     */
    public abstract static class StringGraph extends GraphScene<String, String> {
        
    }

}
