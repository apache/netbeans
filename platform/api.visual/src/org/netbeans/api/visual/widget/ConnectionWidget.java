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
package org.netbeans.api.visual.widget;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.modules.visual.layout.ConnectionWidgetLayout;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a connection between two location. The locations are resolved by Anchors.
 * The path of the connection is specified by control points which are resolved by Routers (DirectRouter is used by default).
 * <p>
 * The connection is rendered using the foreground color and a specified stroke. It also renders control points,
 * end points (first and last control points) and anchors. The shape of points are defined by assigned AnchorShape and PointShape.
 * <p>
 * For speed optimalization, the connection widget are usually placed in the a separate layer widget that is rendered after
 * the main layer with widgets (that used anchors depends on) is rendered.
 * <p>
 * Line color is defined by foregroundColor property. Note: If you are changing a state of the ConnectionWidget
 * (e.g. using it as a representation of an object in ObjectScene, GraphScene or GraphPinScene classes,
 * then the ConnectionWidget.notifyStateChanged method is automatically called.
 * The built-in implementation of this method overrides foregroundColor based on a new state of the widget
 * (the particular color is resolved by the LookFeel of the scene).
 *
 * @author David Kaspar
 */
// TODO - control points can be modified by accessing: getControlPoints ().get (0).x or y
public class ConnectionWidget extends Widget {

    private static final double HIT_DISTANCE_SQUARE = 16.0;
    private static final Stroke STROKE_DEFAULT = new BasicStroke (1.0f);

    /**
     * This enum represents a policy which is used for re-routing control points of a ConnectionWidget.
     * @since 2.9
     */
    public enum RoutingPolicy {

        /**
         * All control points are rerouted when it is necessary. This is the default policy.
         * @since 2.9
         */
        ALWAYS_ROUTE,

        /**
         * All control points (except end points) are kept at the same location.
         * End points are updated to locations resolved by source and target anchors.
         * Note: This is used when an user customizes/adds/removes control points
         * and the change has to be kept until it is reset by the user (setting policy to ALWAYS_ROUTE.
         * @since 2.9
         */
        UPDATE_END_POINTS_ONLY,

        /**
         * Temporarily disables routing until any end point changes its location. Locations are the first
         * and the last point of control points. When an end point location is changed,
         * then the policy is automatically changed to <code>ALWAYS_ROUTE</code>.
         * Note: This is used by GraphLayouts which routes the path (control points)
         * by themselves and would like to keep the path until user moves with source or target widget/anchor.
         * @since 2.9
         */
        DISABLE_ROUTING_UNTIL_END_POINT_IS_MOVED,

        /**
         * Disable routing completely, so control points are kept at their previous location.
         * Note: This is not often used unless you have to freeze a ConnectionWidget
         * @since 2.9
         */
        DISABLE_ROUTING

    }

    private Anchor sourceAnchor;
    private Anchor targetAnchor;
    private AnchorShape sourceAnchorShape;
    private AnchorShape targetAnchorShape;
    private PointShape controlPointShape;
    private PointShape endPointShape;
    private Router router;
    private boolean routingRequired;
    private List<Point> controlPoints = Collections.emptyList ();
    private List<Point> controlPointsUm = Collections.unmodifiableList (controlPoints);
    private ConnectionWidgetLayout connectionWidgetLayout;
    private Stroke stroke;
    private boolean paintControlPoints;
    private Color lineColor;
    private Cursor controlPointsCursor;
    private int controlPointCutDistance;

    private Anchor.Entry sourceEntry;
    private Anchor.Entry targetEntry;

    private RoutingPolicy routingPolicy;

    /**
     * Creates a connection widget.
     * @param scene the scene
     */
    public ConnectionWidget (Scene scene) {
        super (scene);
        sourceAnchorShape = AnchorShape.NONE;
        targetAnchorShape = AnchorShape.NONE;
        controlPointShape = PointShape.NONE;
        endPointShape = PointShape.NONE;
        router = RouterFactory.createDirectRouter ();
        routingRequired = true;
        connectionWidgetLayout = new ConnectionWidgetLayout ();
        setLayout (connectionWidgetLayout);
        stroke = STROKE_DEFAULT;
        paintControlPoints = false;
        controlPointCutDistance = 0;
        sourceEntry = new ConnectionEntry (true);
        targetEntry = new ConnectionEntry (false);

        routingPolicy = RoutingPolicy.ALWAYS_ROUTE;
    }

    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        setForeground (lineColor != null ? lineColor : getScene ().getLookFeel ().getLineColor (state));
        setPaintControlPoints (state.isSelected ());
    }

    /**
     * Returns a stroke of the connection widget.
     * @return the stroke
     */
    public final Stroke getStroke () {
        return stroke;
    }

    /**
     * Sets a stroke.
     * @param stroke the stroke
     */
    public final void setStroke (Stroke stroke) {
        assert stroke != null;
        this.stroke = stroke;
        repaint (); // TODO - check when to revalidate and when to repaint only
    }

    /**
     * Returns line color of the widget.
     * @return the line color; null if no line color is specified
     */
    public final Color getLineColor () {
        return lineColor;
    }

    /**
     * Sets a line color of the widget.
     * @param lineColor the line color; if null, then the line color will be resolved from LookFeel of the scene.
     */
    public final void setLineColor (Color lineColor) {
        this.lineColor = lineColor;
        ObjectState state = getState ();
        notifyStateChanged (state, state);
    }

    /**
     * Returns whether the control (and end) points are painted
     * @return true, if the control points (and end points) are painted
     */
    public final boolean isPaintControlPoints () {
        return paintControlPoints;
    }

    /**
     * Sets whether the control (and end) points are painted
     * @param paintControlPoints if true, then control points are painted
     */
    public final void setPaintControlPoints (boolean paintControlPoints) {
        this.paintControlPoints = paintControlPoints;
        repaint ();
    }

    /**
     * Returns the cursor for control point.
     * @return the cursor
     * @since 2.3
     */
    public final Cursor getControlPointsCursor () {
        return controlPointsCursor;
    }

    /**
     * Sets a control points cursor. The cursor is used only when mouse is over a visible control point
     * @param controlPointsCursor the control points cursor
     * @since 2.3
     */
    public final void setControlPointsCursor (Cursor controlPointsCursor) {
        this.controlPointsCursor = controlPointsCursor;
    }

    /**
     * Returns the cut distance at control points.
     * @return the cut distance
     * @since 2.5
     */
    public int getControlPointCutDistance () {
        return controlPointCutDistance;
    }

    /**
     * Sets the cut distance at control points.
     * @param controlPointCutDistance if positive number, then the path is cut to render smooth corners;
     *     otherwise the path is rendered using control points only
     * @since 2.5
     */
    public void setControlPointCutDistance (int controlPointCutDistance) {
        this.controlPointCutDistance = controlPointCutDistance;
        repaint ();
    }

    /**
     * Returns a source anchor of the connection widget.
     * @return the source anchor
     */
    public final Anchor getSourceAnchor () {
        return sourceAnchor;
    }

    /**
     * Sets a source anchor of the connection widget.
     * @param sourceAnchor the source anchor
     */
    public final void setSourceAnchor (Anchor sourceAnchor) {
        if (this.sourceAnchor != null)
            this.sourceAnchor.removeEntry (sourceEntry);
        this.sourceAnchor = sourceAnchor;
        if (this.sourceAnchor != null)
            sourceAnchor.addEntry (sourceEntry);
        reroute ();
    }

    /**
     * Returns a target anchor of the connection widget.
     * @return the target anchor
     */
    public final Anchor getTargetAnchor () {
        return targetAnchor;
    }

    /**
     * Sets a target anchor of the connection widget.
     * @param targetAnchor the target anchor
     */
    public final void setTargetAnchor (Anchor targetAnchor) {
        if (this.targetAnchor != null)
            this.targetAnchor.removeEntry (targetEntry);
        this.targetAnchor = targetAnchor;
        if (targetAnchor != null)
            targetAnchor.addEntry (targetEntry);
        reroute ();
    }

    /**
     * Returns an anchor entry representing the source of the connection widget.
     * @return the anchor entry representing the source of the connection widget
     */
    public Anchor.Entry getSourceAnchorEntry () {
        return sourceEntry;
    }

    /**
     * Returns an anchor entry representing the target of the connection widget.
     * @return the anchor entry representing the target of the connection widget
     */
    public Anchor.Entry getTargetAnchorEntry () {
        return targetEntry;
    }

    /**
     * Returns an anchor shape of the source of the connection widget.
     * @return the source anchor shape
     */
    public AnchorShape getSourceAnchorShape () {
        return sourceAnchorShape;
    }

    /**
     * Sets the anchor shape of the source of the connection widget.
     * @param sourceAnchorShape the source anchor shape
     */
    public void setSourceAnchorShape (AnchorShape sourceAnchorShape) {
        assert sourceAnchorShape != null;
        boolean repaintOnly = this.sourceAnchorShape.getRadius () == sourceAnchorShape.getRadius ();
        this.sourceAnchorShape = sourceAnchorShape;
        revalidate (repaintOnly);
    }

    /**
     * Returns an anchor shape of the target of the connection widget.
     * @return the target anchor shape
     */
    public AnchorShape getTargetAnchorShape () {
        return targetAnchorShape;
    }

    /**
     * Sets the anchor shape of the target of the connection widget.
     * @param targetAnchorShape the target anchor shape
     */
    public void setTargetAnchorShape (AnchorShape targetAnchorShape) {
        assert targetAnchorShape != null;
        boolean repaintOnly = this.targetAnchorShape.getRadius () == targetAnchorShape.getRadius ();
        this.targetAnchorShape = targetAnchorShape;
        revalidate (repaintOnly);
    }

    /**
     * Returns a point shape of control points of the connection widget.
     * @return the control points shape
     */
    public PointShape getControlPointShape () {
        return controlPointShape;
    }

    /**
     * Sets a point shape of control points of the connection widget.
     * @param controlPointShape the control points shape
     */
    public void setControlPointShape (PointShape controlPointShape) {
        assert controlPointShape != null;
        boolean repaintOnly = this.controlPointShape.getRadius () == controlPointShape.getRadius ();
        this.controlPointShape = controlPointShape;
        revalidate (repaintOnly);
    }

    /**
     * Returns a point shape of end points of the connection widget.
     * @return the end points shape
     */
    public PointShape getEndPointShape () {
        return endPointShape;
    }

    /**
     * Sets a point shape of end points of the connection widget.
     * @param endPointShape the end points shape
     */
    public void setEndPointShape (PointShape endPointShape) {
        assert endPointShape != null;
        boolean repaintOnly = this.endPointShape.getRadius () == endPointShape.getRadius ();
        this.endPointShape = endPointShape;
        revalidate (repaintOnly);
    }

    /**
     * Returns a routing policy.
     * @return the routing policy
     * @since 2.9
     */
    public final RoutingPolicy getRoutingPolicy () {
        return routingPolicy;
    }

    /**
     * Sets a routing policy. It invokes re-routing in case of routing policy change unless its is changed to DISABLE_ROUTING.
     * @param routingPolicy the new routing policy
     * @since 2.9
     */
    public final void setRoutingPolicy (RoutingPolicy routingPolicy) {
        assert routingPolicy != null;
        if (this.routingPolicy == routingPolicy)
            return;
        boolean changed = routingPolicy != RoutingPolicy.DISABLE_ROUTING;
        this.routingPolicy = routingPolicy;
        if (changed)
            reroute ();
    }

    /**
     * Returns the control-points-based path router of the connection widget.
     * @return the path router
     */
    public final Router getRouter () {
        return router;
    }

    /**
     * Sets a control-points-based path router of the connection widget.
     * @param router the path router
     */
    public final void setRouter (Router router) {
        assert router != null;
        this.router = router;
        reroute ();
    }

    /**
     * Returns a list of control points.
     * @return the list of control points
     */
    public List<Point> getControlPoints () {
        return controlPointsUm;
    }
    
    /**
     * Returns a location of control point at the specified index in the list of control points.
     *
     * @param index index of the control point to return
     * @return the point; null if the control point does not exist
     */
    public Point getControlPoint (int index) {
        if (index < 0  ||  index >= controlPoints.size ())
            return null;
        return new Point (controlPoints.get(index));
    }

    /**
     * Sets control points.
     * @param controlPoints the list of control points
     * @param sceneLocations if true, then controlPoints argyment is taken as a list of scene locations;
     *            if false, then controlPoints argument is taken as a list of local locations
     */
    public void setControlPoints (Collection<Point> controlPoints, boolean sceneLocations) {
        //quick return if possible
        if (controlPoints == null) return ;
        
        if (sceneLocations) {
            Point translation = this.convertLocalToScene (new Point ());
            ArrayList<Point> list = new ArrayList<Point> ();
            for (Point point : controlPoints)
                list.add (new Point (point.x - translation.x, point.y - translation.y));
            this.controlPoints = list;
        } else
            this.controlPoints = new ArrayList<Point> (controlPoints);
        
        this.controlPointsUm = Collections.unmodifiableList (this.controlPoints);
        routingRequired = false;
        revalidate ();
    }

    /**
     * Sets a constraint for a child widget when ConnectionWidgetLayout (by default) is used.
     * @param childWidget the child widget for which the constraint is set
     * @param alignment the alignment specified relatively to the origin point
     * @param placementInPercentage the placement on a path in percentage of the path length
     */
    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, float placementInPercentage) {
        connectionWidgetLayout.setConstraint (childWidget, alignment, placementInPercentage);
    }

    /**
     * Sets a constraint for a child widget when ConnectionWidgetLayout (by default) is used.
     * @param childWidget the child widget for which the constraint is set
     * @param alignment the alignment specified relatively to the origin point
     * @param placementAtDistance the placement on a path in pixels as a distance from the source anchor
     */
    public void setConstraint (Widget childWidget, LayoutFactory.ConnectionWidgetLayoutAlignment alignment, int placementAtDistance) {
        connectionWidgetLayout.setConstraint (childWidget, alignment, placementAtDistance);
    }

    /**
     * Removes a constraint for a child widget.
     * @param childWidget the child widget
     */
    public void removeConstraint (Widget childWidget) {
        connectionWidgetLayout.removeConstraint (childWidget);
    }

    /**
     * Forces path routing.
     */
    public final void calculateRouting () {
        if (routingRequired) {
            switch (routingPolicy) {
                case ALWAYS_ROUTE:
                    setControlPoints (router.routeConnection (this), true);
                    break;
                case UPDATE_END_POINTS_ONLY: {
                    Point sourcePoint = sourceAnchor != null ? sourceAnchor.compute (sourceEntry).getAnchorSceneLocation () : null;
                    Point targetPoint = targetAnchor != null ? targetAnchor.compute (targetEntry).getAnchorSceneLocation () : null;
                    if (sourcePoint == null  ||  targetPoint == null) {
                        controlPoints.clear ();
                        break;
                    }
                    sourcePoint = convertSceneToLocal (sourcePoint);
                    targetPoint = convertSceneToLocal (targetPoint);
                    if (controlPoints.size () < 1)
                        controlPoints.add (sourcePoint);
                    else
                        controlPoints.set (0, sourcePoint);
                    if (controlPoints.size () < 2)
                        controlPoints.add (targetPoint);
                    else
                        controlPoints.set (controlPoints.size () - 1, targetPoint);
                    } break;
                case DISABLE_ROUTING_UNTIL_END_POINT_IS_MOVED: {
                    Point sourcePoint = sourceAnchor != null ? sourceAnchor.compute (sourceEntry).getAnchorSceneLocation () : null;
                    Point firstPoint = getFirstControlPoint ();
                    if (firstPoint != null)
                        firstPoint = convertLocalToScene (firstPoint);
                    if (sourcePoint == null ? firstPoint == null : sourcePoint.equals (firstPoint)) {
                        Point targetPoint = targetAnchor != null ? targetAnchor.compute (targetEntry).getAnchorSceneLocation () : null;
                        Point lastPoint = getLastControlPoint ();
                        if (lastPoint != null)
                            lastPoint = convertLocalToScene (lastPoint);
                        if (targetPoint == null  ? lastPoint == null : targetPoint.equals (lastPoint))
                            break;
                    }
                    routingPolicy = RoutingPolicy.ALWAYS_ROUTE;
                    setControlPoints (router.routeConnection (this), true);
                    } break;
                case DISABLE_ROUTING:
                    break;
                default:
                    throw new IllegalStateException ("Unexpected routing policy: " + routingPolicy); // NOI18N
            }
        }
    }

    /**
     * Calculates a client area of the connection widget.
     * @return the calculated client area
     */
    protected Rectangle calculateClientArea () {
        calculateRouting ();
        int controlPointShapeRadius = controlPointShape.getRadius ();
        int controlPointShapeRadius2 = controlPointShapeRadius + controlPointShapeRadius;
        int endPointShapeRadius = endPointShape.getRadius ();

        Rectangle rect = null;
        for (Point point : controlPoints) {
            Rectangle addRect = new Rectangle (point.x - controlPointShapeRadius, point.y - controlPointShapeRadius, controlPointShapeRadius2, controlPointShapeRadius2);
            if (rect == null)
                rect = addRect;
            else
                rect.add (addRect);
        }

        Point firstPoint = getFirstControlPoint ();
        if (firstPoint != null) {
            int radius = Math.max (sourceAnchorShape.getRadius (), endPointShapeRadius);
            int radius2 = radius + radius;
            if (rect == null)
                rect = new Rectangle (firstPoint.x - radius, firstPoint.y - radius, radius2, radius2);
            else
                rect.add (new Rectangle (firstPoint.x - radius, firstPoint.y - radius, radius2, radius2));
        }

        Point lastPoint = getLastControlPoint ();
        if (lastPoint != null) {
            int radius = Math.max (targetAnchorShape.getRadius (), endPointShapeRadius);
            int radius2 = radius + radius;
            if (rect == null)
                rect = new Rectangle (lastPoint.x - radius, lastPoint.y - radius, radius2, radius2);
            else
                rect.add (new Rectangle (lastPoint.x - radius, lastPoint.y - radius, radius2, radius2));
        }

        if (rect != null)
            rect.grow (2, 2); // TODO - improve line width calculation

        return rect != null ? rect : new Rectangle ();
    }

    /**
     * Returns whether the connection widget is validated and routed.
     * @return true, if the connection widget is validated and routed
     */
    public boolean isValidated () {
        return super.isValidated ()  &&  isRouted ();
    }

    /**
     * Returns whether the connection widget is routed.
     * @return true if the connection widget is routed
     */
    public final boolean isRouted () {
        return ! routingRequired;
    }

    /**
     * Schedules the connection widget for re-routing its path.
     */
    public final void reroute () {
        routingRequired = true;
        revalidate ();
    }

    /**
     * Returns the first control point.
     * @return the first control point; null, if list of control points is empty
     */
    public final Point getFirstControlPoint () {
        if (controlPoints.size () <= 0)
            return null;
        return new Point (controlPoints.get (0));
    }

    /**
     * Returns the last control point.
     * @return the last control point; null, if list of control points is empty
     */
    public final Point getLastControlPoint () {
        int size = controlPoints.size ();
        if (size <= 0)
            return null;
        return new Point (controlPoints.get (size - 1));
    }

    /**
     * Returns the rotation of the source anchor shape.
     * @return the source anchor shape rotation
     */
    private double getSourceAnchorShapeRotation () {
        if (controlPoints.size () <= 1)
            return 0.0;
        Point point1 = controlPoints.get (0);
        Point point2 = controlPoints.get (1);
        return Math.atan2 (point2.y - point1.y, point2.x - point1.x);
    }

    /**
     * Returns the rotation of the target anchor shape.
     * @return the target anchor shape rotation
     */
    public double getTargetAnchorShapeRotation () {
        int size = controlPoints.size ();
        if (size <= 1)
            return 0.0;
        Point point1 = controlPoints.get (size - 1);
        Point point2 = controlPoints.get (size - 2);
        return Math.atan2 (point2.y - point1.y, point2.x - point1.x);
    }

    /**
     * Returns whether a specified local location is a part of the connection widget. It checks whether the location is
     * close to the control-points-based path (up to 4px from the line),
     * close to the anchors (defined by AnchorShape) or
     * close to the control points (PointShape).
     * @param localLocation the local locaytion
     * @return true, if the location is a part of the connection widget
     */
    public boolean isHitAt (Point localLocation) {
        if (! super.isHitAt (localLocation))
                return false;

        List<Point> controlPoints = getControlPoints ();
        for (int i = 0; i < controlPoints.size () - 1; i++) {
            Point point1 = controlPoints.get (i);
            Point point2 = controlPoints.get (i + 1);
            double dist = Line2D.ptSegDistSq (point1.x, point1.y, point2.x, point2.y, localLocation.x, localLocation.y);
            if (dist < HIT_DISTANCE_SQUARE)
                return true;
        }

        return getControlPointHitAt (localLocation) >= 0;
    }

    /**
     * Returns whether the local location hits the first control point (also meant to be the source anchor).
     * @param localLocation the local location
     * @return true if it hits the first control point
     */
    public final boolean isFirstControlPointHitAt (Point localLocation) {
        int endRadius = endPointShape.getRadius ();
        endRadius *= endRadius;
        Point firstPoint = getFirstControlPoint ();
        if (firstPoint != null)
            if (Point2D.distanceSq (firstPoint.x, firstPoint.y, localLocation.x, localLocation.y) <= endRadius)
                return true;
        return false;
    }

    /**
     * Returns whether the local location hits the last control point (also meant to be the target anchor).
     * @param localLocation the local location
     * @return true if it hits the last control point
     */
    public final boolean isLastControlPointHitAt (Point localLocation) {
        int endRadius = endPointShape.getRadius ();
        endRadius *= endRadius;
        Point lastPoint = getLastControlPoint ();
        if (lastPoint != null)
            if (Point2D.distanceSq (lastPoint.x, lastPoint.y, localLocation.x, localLocation.y) <= endRadius)
                return true;
        return false;
    }

    /**
     * Returns an index of a control point that is hit by the local location
     * @param localLocation the local location
     * @return the index; -1 if no control point was hit
     */
    public final int getControlPointHitAt (Point localLocation) {
        int controlRadius = controlPointShape.getRadius ();
        controlRadius *= controlRadius;

        if (isFirstControlPointHitAt (localLocation))
            return 0;

        if (isLastControlPointHitAt (localLocation))
            return controlPoints.size () - 1;

        for (int i = 0; i < controlPoints.size (); i ++) {
            Point point = controlPoints.get (i);
            if (Point2D.distanceSq (point.x, point.y, localLocation.x, localLocation.y) <= controlRadius)
                return i;
        }

        return -1;
    }

    /**
     * Returns a cursor for a specified local location in the widget.
     * If paintControlPoints is true and controlPointsCursor is non-null and local location is over a control point, then it return controlPointsCursor.
     * Otherwise it return value from super.getCursorAt method.
     * @param localLocation the local location
     * @return the cursor
     * @since 2.3
     */
    protected Cursor getCursorAt (Point localLocation) {
        if (paintControlPoints) {
            Cursor pointsCursor = getControlPointsCursor ();
            if (pointsCursor != null  &&  getControlPointHitAt (localLocation) >= 0)
                return pointsCursor;
        }
        return super.getCursorAt (localLocation);
    }

    /**
     * Paints the connection widget (the path, the anchor shapes, the control points, the end points).
     */
    protected void paintWidget () {
        Graphics2D gr = getGraphics ();
        gr.setColor (getForeground ());
        GeneralPath path = null;

        Point firstControlPoint = getFirstControlPoint ();
        Point lastControlPoint = getLastControlPoint ();
                
        //checking to see if we should draw line through the AnchorShape. If the 
        //AnchorShape is hollow, the cutdistance will be true.
        boolean isSourceCutDistance = sourceAnchorShape.getCutDistance () != 0.0;
        boolean isTargetCutDistance = targetAnchorShape.getCutDistance () != 0.0;
        
        double firstControlPointRotation = 
                firstControlPoint != null  &&  (sourceAnchorShape.isLineOriented ()  
                ||  isSourceCutDistance) ? 
                    getSourceAnchorShapeRotation () : 0.0;
        double lastControlPointRotation = 
                lastControlPoint != null  
                &&  (targetAnchorShape.isLineOriented () || isTargetCutDistance) ? 
                    getTargetAnchorShapeRotation () : 0.0;
        
        List<Point> points;
        if ((isSourceCutDistance  ||  isTargetCutDistance)  &&  controlPoints.size () >= 2) {
            points = new ArrayList<Point> (controlPoints);
            points.set (0, new Point (
                firstControlPoint.x + (int) (sourceAnchorShape.getCutDistance () * Math.cos (firstControlPointRotation)),
                firstControlPoint.y + (int) (sourceAnchorShape.getCutDistance () * Math.sin (firstControlPointRotation))
            ));
            points.set (controlPoints.size () - 1, new Point (
                lastControlPoint.x + (int) (targetAnchorShape.getCutDistance () * Math.cos (lastControlPointRotation)),
                lastControlPoint.y + (int) (targetAnchorShape.getCutDistance () * Math.sin (lastControlPointRotation))
            ));
        } else {
            points = controlPoints;
        }

        if (controlPointCutDistance > 0) {
            for (int a = 0; a < points.size () - 1; a ++) {
                Point p1 = points.get (a);
                Point p2 = points.get (a + 1);
                double len = p1.distance (p2);

                if (a > 0) {
                    Point p0 = points.get (a - 1);
                    double ll = p0.distance (p1);
                    if (len < ll)
                        ll = len;
                    ll /= 2;
                    double cll = controlPointCutDistance;
                    if (cll > ll)
                        cll = ll;
                    double direction = Math.atan2 (p2.y - p1.y, p2.x - p1.x);
                    if (!Double.isNaN (direction)) {
                        path = addToPath (path,
                                p1.x + (int) (cll * Math.cos (direction)),
                                p1.y + (int) (cll * Math.sin (direction))
                        );
                    }
                } else {
                    path = addToPath (path, p1.x, p1.y);
                }

                if (a < points.size () - 2) {
                    Point p3 = points.get (a + 2);
                    double ll = p2.distance (p3);
                    if (len < ll)
                        ll = len;
                    ll /= 2;
                    double cll = controlPointCutDistance;
                    if (cll > ll)
                        cll = ll;
                    double direction = Math.atan2 (p2.y - p1.y, p2.x - p1.x);
                    if (!Double.isNaN (direction)) {
                        path = addToPath (path,
                                p2.x - (int) (cll * Math.cos (direction)),
                                p2.y - (int) (cll * Math.sin (direction))
                        );
                    }
                } else {
                    path = addToPath (path, p2.x, p2.y);
                }
            }
        } else {
            for (Point point : points)
                path = addToPath (path, point.x, point.y);
        }
        if (path != null) {
            Stroke previousStroke = gr.getStroke ();
            gr.setPaint (getForeground ());
            gr.setStroke (getStroke ());
            gr.draw (path);
            gr.setStroke (previousStroke);
        }


        AffineTransform previousTransform;

        if (firstControlPoint != null) {
            previousTransform = gr.getTransform ();
            gr.translate (firstControlPoint.x, firstControlPoint.y);
            if (sourceAnchorShape.isLineOriented ())
                gr.rotate (firstControlPointRotation);
            sourceAnchorShape.paint (gr, true);
            gr.setTransform (previousTransform);
        }

        if (lastControlPoint != null) {
            previousTransform = gr.getTransform ();
            gr.translate (lastControlPoint.x, lastControlPoint.y);
            if (targetAnchorShape.isLineOriented ())
                gr.rotate (lastControlPointRotation);
            targetAnchorShape.paint (gr, false);
            gr.setTransform (previousTransform);
        }

        if (paintControlPoints) {
            int last = controlPoints.size () - 1;
            for (int index = 0; index <= last; index ++) {
                Point point = controlPoints.get (index);
                previousTransform = gr.getTransform ();
                gr.translate (point.x, point.y);
                if (index == 0  ||  index == last)
                    endPointShape.paint (gr);
                else
                    controlPointShape.paint (gr);
                gr.setTransform (previousTransform);
            }
        }
    }

    private GeneralPath addToPath (GeneralPath path, int x, int y) {
        if (path == null) {
            path = new GeneralPath ();
            path.moveTo (x, y);
        } else {
            path.lineTo (x, y);
        }
        return path;
    }

    private class ConnectionEntry implements Anchor.Entry {

        private boolean source;

        public ConnectionEntry (boolean source) {
            this.source = source;
        }

        public void revalidateEntry () {
            ConnectionWidget.this.reroute ();
        }

        public ConnectionWidget getAttachedConnectionWidget () {
            return ConnectionWidget.this;
        }

        public boolean isAttachedToConnectionSource () {
            return source;
        }

        public Anchor getAttachedAnchor () {
            return source ? ConnectionWidget.this.getSourceAnchor () : ConnectionWidget.this.getTargetAnchor ();
        }

        public Anchor getOppositeAnchor () {
            return source ? ConnectionWidget.this.getTargetAnchor () : ConnectionWidget.this.getSourceAnchor ();
        }

    }

}
