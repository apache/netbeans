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
 * In comparison with the GraphScene class, in this class the graph consists of nodes, edges and pins. Each pin is assigned to a node (the assigned cannot be change at any time.
 * Each edge could be attach to a single source and target pin.
 * <p>
 * The class is abstract and manages only data model and mapping with widgets. The graphics (widgets) has to be supplied
 * by a developer by overriding the attachNodeWidget, attachPinWidget, attachEdgeWidget, attachEdgeSourceAnchor and attachEdgeTargetAnchor abstract methods.
 * <p>
 * This class is using generics and allows you to specify type representation for nodes and edges in the graph model. Example:
 * <pre>
 * class MyGraph extends GraphScene&lt;MyNode, MyEdge, MyPin&gt; { ... }
 * </pre>
 * Since the type of nodes, edges and pins could be the same, all node, edge and pin instances have to be unique within the whole scene.
 *
 * @author David Kaspar
 */
// TODO - is it asserted that removing a node removes all its pins
// TODO - is it asserted that removing a pin disconnects all the attached edges
public abstract class GraphPinScene<N, E, P> extends ObjectScene {

    private HashSet<N> nodes = new HashSet<N> ();
    private Set<N> nodesUm = Collections.unmodifiableSet (nodes);
    private HashSet<E> edges = new HashSet<E> ();
    private Set<E> edgesUm = Collections.unmodifiableSet (edges);
    private HashSet<P> pins = new HashSet<P> ();
    private Set<P> pinsUm = Collections.unmodifiableSet (pins);

    private HashMap<N, HashSet<P>> nodePins = new HashMap<N, HashSet<P>> ();
    private HashMap<P, N> pinNodes = new HashMap<P, N> ();
    private HashMap<E, P> edgeSourcePins = new HashMap<E, P> ();
    private HashMap<E, P> edgeTargetPins = new HashMap<E, P> ();
    private HashMap<P, List<E>> pinInputEdges = new HashMap<P, List<E>> ();
    private HashMap<P, List<E>> pinOutputEdges = new HashMap<P, List<E>> ();

    /**
     * Creates a graph scene.
     */
    public GraphPinScene () {
    }

    /**
     * Adds a node.
     * @param node the node to be added; the node must not be null, must not be already in the model, must be unique in the model
     *           (means: there is no other node, edge or pin in the model has is equal to this node)
     *           and must not be a Widget
     * @return the widget that is created by attachNodeWidget; null if the node is non-visual
     */
    public final Widget addNode (N node) {
        assert node != null : "Null parameter";
        assert ! nodes.contains (node) : "Node (" + node + ") already added";
        Widget widget = attachNodeWidget (node);
        nodes.add (node);
        nodePins.put (node, new HashSet<P> ());
        addObject (node, widget);
        notifyNodeAdded (node, widget);
        return widget;
    }

    /**
     * Removes a node with all pins that are assigned to the node.
     * @param node the node to be removed; the node must not be null and must be already in the model
     */
    public final void removeNode (N node) {
        assert node != null  &&  nodes.contains (node);
        for (P pin : new HashSet<P> (nodePins.get (node)))
            removePin (pin);
        nodes.remove (node);
        nodePins.remove (node);
        Widget widget = findWidget (node);
        detachNodeWidget (node, widget);
        removeObject (node);
    }

    /**
     * Removes a node with all pins that are assign to the node and with all edges that are connected to the pins.
     * @param node the node to be removed; the node must not be null and must be already in the model
     */
    public final void removeNodeWithEdges (N node) {
        assert node != null && nodes.contains (node);
        for (P pin : nodePins.get (node))
            for (E edge : findPinEdges (pin, true, true))
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
     *           (means: there is no other node, edge or pin in the model has is equal to this edge)
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
     * Removes an edge and detaches it from its source and target pins.
     * @param edge the edge to be removed; the edge must not be null and must be already in the model
     */
    public final void removeEdge (E edge) {
        assert edge != null  &&  edges.contains (edge);
        setEdgeSource (edge, null);
        setEdgeTarget (edge, null);
        edges.remove (edge);
        edgeSourcePins.remove (edge);
        edgeTargetPins.remove (edge);
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
     * Adds a pin and assigns it to a specified node.
     * @param node the node where the pin is assigned to
     * @param pin the pin to be added; the pin must not be null, must not be already in the model, must be unique in the model
     *           (means: there is no other node, edge or pin in the model has is equal to this pin)
     *           and must not be a Widget
     * @return the widget that is created by attachPinWidget; null if the pin is non-visual
     */
    public final Widget addPin (N node, P pin) {
        assert node != null  &&  pin != null  &&  ! pins.contains (pin);
        Widget widget = attachPinWidget (node, pin);
        pins.add (pin);
        nodePins.get (node).add (pin);
        pinNodes.put (pin, node);
        pinInputEdges.put (pin, new ArrayList<E> ());
        pinOutputEdges.put (pin, new ArrayList<E> ());
        addObject (pin, widget);
        notifyPinAdded (node, pin, widget);
        return widget;
    }

    /**
     * Removes an pin and detaches all edges that are connected to it.
     * @param pin the pin to be removed; the pin must not be null and must be already in the model
     */
    public final void removePin (P pin) {
        assert pin != null  &&  pins.contains (pin);
        for (E edge : findPinEdges (pin, true, false))
            setEdgeSource (edge, null);
        for (E edge : findPinEdges (pin, false, true))
            setEdgeTarget (edge, null);
        pins.remove (pin);
        N node = pinNodes.remove (pin);
        nodePins.get (node).remove (pin);
        pinInputEdges.remove (pin);
        pinOutputEdges.remove (pin);
        Widget widget = findWidget (pin);
        detachPinWidget (pin, widget);
        removeObject (pin);
    }

    /**
     * Removes a pin with all edges that are connected to the pin.
     * @param pin the pin to be removed; the pin must not be null and must be already in the model
     */
    public final void removePinWithEdges (P pin) {
        assert pin != null && pins.contains (pin);
        for (E edge : findPinEdges (pin, true, true))
            if (isEdge (edge))
                removeEdge (edge);
        removePin (pin);
    }

    /**
     * Returns a node where the pin assigned to.
     * @param pin the pin
     * @return the node where the pin assigned to.
     */
    public final N getPinNode (P pin) {
        return pinNodes.get (pin);
    }

    /**
     * Returns all pins registered in the graph model.
     * @return the collection of all pins registered in the graph model
     */
    public final Collection<P> getPins () {
        return pinsUm;
    }

    /**
     * Returns a collection of pins that are assigned to a specified node
     * @param node the node
     * @return the collection of pins; null if node does not exist in the scene
     */
    public final Collection<P> getNodePins (N node) {
        if (node == null)
            return null;
        HashSet<P> ps = nodePins.get (node);
        if (ps == null)
            return null;
        return Collections.unmodifiableCollection (ps);
    }

    /**
     * Sets an edge source.
     * @param edge the edge which source is going to be changed
     * @param sourcePin the source pin; if null, then the edge source will be detached
     */
    public final void setEdgeSource (E edge, P sourcePin) {
        assert edge != null  &&  edges.contains (edge);
        if (sourcePin != null)
            assert pins.contains (sourcePin);
        P oldPin = edgeSourcePins.put (edge, sourcePin);
        if (GeomUtil.equals (oldPin, sourcePin))
            return;
        if (oldPin != null)
            pinOutputEdges.get (oldPin).remove (edge);
        if (sourcePin != null)
            pinOutputEdges.get (sourcePin).add (edge);
        attachEdgeSourceAnchor (edge, oldPin, sourcePin);
    }

    /**
     * Sets an edge target.
     * @param edge the edge which target is going to be changed
     * @param targetPin the target pin; if null, then the edge target will be detached
     */
    public final void setEdgeTarget (E edge, P targetPin) {
        assert edge != null  &&  edges.contains (edge);
        if (targetPin != null)
            assert pins.contains (targetPin);
        P oldPin = edgeTargetPins.put (edge, targetPin);
        if (GeomUtil.equals (oldPin, targetPin))
            return;
        if (oldPin != null)
            pinInputEdges.get (oldPin).remove (edge);
        if (targetPin != null)
            pinInputEdges.get (targetPin).add (edge);
        attachEdgeTargetAnchor (edge, oldPin, targetPin);
    }

    /**
     * Returns an edge source.
     * @param edge the edge
     * @return the edge source; null, if edge does not have source attached
     */
    public final P getEdgeSource (E edge) {
        return edgeSourcePins.get (edge);
    }

    /**
     * Returns an edge target.
     * @param edge the edge
     * @return the edge target; null, if edge does not have target attached
     */
    public final P getEdgeTarget (E edge) {
        return edgeTargetPins.get (edge);
    }

    /**
     * Returns a collection of edges that are attached to a specified pin.
     * @param pin the pin which edges connections are searched for
     * @param allowOutputEdges if true, the output edges are included in the collection; if false, the output edges are not included
     * @param allowInputEdges if true, the input edges are included in the collection; if false, the input edges are not included
     * @return the collection of edges
     */
    public final Collection<E> findPinEdges (P pin, boolean allowOutputEdges, boolean allowInputEdges) {
        assert isPin (pin) : "Pin " + pin + " is not in the scene";
        ArrayList<E> list = new ArrayList<E> ();
        if (allowInputEdges)
            list.addAll (pinInputEdges.get (pin));
        if (allowOutputEdges)
            list.addAll (pinOutputEdges.get (pin));
        return list;
    }

    /**
     * Returns a collection of edges that are between a source and a target pin.
     * @param sourcePin the source pin
     * @param targetPin the target pin
     * @return the collection of edges
     */
    public final Collection<E> findEdgesBetween (P sourcePin, P targetPin) {
        assert isPin (sourcePin) : "Source pin " + sourcePin + " is not in the scene";
        assert isPin (targetPin) : "Target pin " + targetPin + " is not in the scene";
        HashSet<E> list = new HashSet<E> ();
        List<E> inputEdges = pinInputEdges.get (targetPin);
        List<E> outputEdges = pinOutputEdges.get (sourcePin);
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
     * Checks whether an object is registered as an edge in the graph model.
     * @param object the object; must not be a Widget
     * @return true, if the object is registered as a edge
     */
    public boolean isEdge (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return edges.contains (object);
    }

    /**
     * Checks whether an object is registered as a pin in the graph model.
     * @param object the object; must not be a Widget
     * @return true, if the object is registered as a pin
     */
    public boolean isPin (Object object) {
        assert ! (object instanceof Widget) : "Use findObject method for getting an object assigned to a specific Widget"; // NOI18N
        return pins.contains (object);
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
     * Called by the addPin method to notify that a pin is added into the graph model.
     * @param node the node where the pin is assigned to
     * @param pin the added pin
     * @param widget the widget created by the attachPinWidget method as a visual representation of the pin
     */
    protected void notifyPinAdded (N node, P pin, Widget widget) {
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
     * Called by the removePin method to notify that a pin is removed from the graph model.
     * The default implementation removes the pin widget from its parent widget.
     * @param pin the removed pin
     * @param widget the removed pin widget; null if the pin is non-visual
     */
    protected void detachPinWidget (P pin, Widget widget) {
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
     * Called by the addPin method before the pin is registered to acquire a widget that is going to represent the pin in the scene.
     * The method is responsible for creating the widget, adding it into the scene and returning it from the method.
     * @param node the node where the pin is assigned to
     * @param pin the pin that is going to be added
     * @return the widget representing the pin; null, if the pin is non-visual
     */
    protected abstract Widget attachPinWidget (N node, P pin);

    /**
     * Called by the setEdgeSource method to notify about the changing the edge source in the graph model.
     * The method is responsible for attaching a new source pin to the edge in the visual representation.
     * <p>
     * Usually it is implemented as:
     * <pre>
     * Widget sourcePinWidget = findWidget (sourcePin);
     * Anchor sourceAnchor = AnchorFactory.createRectangularAnchor (sourcePinWidget)
     * ConnectionWidget edgeWidget = (ConnectionWidget) findWidget (edge);
     * edgeWidget.setSourceAnchor (sourceAnchor);
     * </pre>
     * @param edge the edge which source is changed in graph model
     * @param oldSourcePin the old source pin
     * @param sourcePin the new source pin
     */
    protected abstract void attachEdgeSourceAnchor (E edge, P oldSourcePin, P sourcePin);

    /**
     * Called by the setEdgeTarget method to notify about the changing the edge target in the graph model.
     * The method is responsible for attaching a new target pin to the edge in the visual representation.
     * <p>
     * Usually it is implemented as:
     * <pre>
     * Widget targetPinWidget = findWidget (targetPin);
     * Anchor targetAnchor = AnchorFactory.createRectangularAnchor (targetPinWidget)
     * ConnectionWidget edgeWidget = (ConnectionWidget) findWidget (edge);
     * edgeWidget.setTargetAnchor (targetAnchor);
     * </pre>
     * @param edge the edge which target is changed in graph model
     * @param oldTargetPin the old target pin
     * @param targetPin the new target pin
     */
    protected abstract void attachEdgeTargetAnchor (E edge, P oldTargetPin, P targetPin);

    /**
     * This class is a particular GraphPinScene where nodes, edges and pins are represented with String class.
     */
    public abstract static class StringGraph extends GraphPinScene<String, String, String> {

    }

}
