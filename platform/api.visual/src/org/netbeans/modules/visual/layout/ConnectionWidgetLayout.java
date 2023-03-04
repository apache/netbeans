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
package org.netbeans.modules.visual.layout;

import org.netbeans.api.visual.layout.LayoutFactory.ConnectionWidgetLayoutAlignment;
import org.netbeans.modules.visual.util.GeomUtil;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Kaspar
 */
public final class ConnectionWidgetLayout implements Layout {

    private final boolean useStacking = true; // if false, then it is backward compatible; if true, then child widgets with the same placement are stacked vertically
    private final HashMap<Widget, Placement> placements;
    private final HashMap<Placement, ArrayList<Widget>> reverse;

    public ConnectionWidgetLayout () {
        placements = new HashMap<Widget, Placement>();
        reverse = new HashMap<Placement, ArrayList<Widget>> ();
    }

    public void setConstraint (Widget childWidget, ConnectionWidgetLayoutAlignment alignment, float placementInPercentage) {
        assert childWidget != null;
        assert alignment != null;
        setConstraint (childWidget, new Placement (alignment, placementInPercentage));
    }
    
    public void setConstraint (Widget childWidget, ConnectionWidgetLayoutAlignment alignment, int placementAtDistance) {
        assert childWidget != null;
        assert alignment != null;
        setConstraint (childWidget, new Placement (alignment, placementAtDistance));
    }

    public void removeConstraint (Widget childWidget) {
        assert childWidget != null;
        setConstraint(childWidget, null);
    }

    private void setConstraint (final Widget childWidget, final Placement newPlacement) {
        final Placement oldPlacement;
        if (newPlacement != null)
            oldPlacement = placements.put (childWidget, newPlacement);
        else
            oldPlacement = placements.remove (childWidget);

        if (oldPlacement != null  &&  ! oldPlacement.equals (newPlacement))
            reverse.get(oldPlacement).remove (childWidget);
        
        if (newPlacement != null) {
            ArrayList<Widget> list = reverse.get(newPlacement);
            if (list == null) {
                list = new ArrayList<Widget> ();
                reverse.put (newPlacement, list);
            }
            list.add (childWidget);
        }
    }

    public void layout (Widget widget) {
        ConnectionWidget connectionWidget = (ConnectionWidget) widget;

        connectionWidget.calculateRouting ();
        java.util.List<Point> controlPoints = connectionWidget.getControlPoints ();
        boolean empty = controlPoints == null  ||  controlPoints.size () <= 0;

        double totalDistance = 0.0;
        double[] distances = new double[empty ? 0 : controlPoints.size () - 1];
        for (int i = 0; i < distances.length; i ++)
            distances[i] = totalDistance += GeomUtil.distanceSq (controlPoints.get (i), controlPoints.get (i + 1));

        ArrayList<Widget> childrenToResolve = new ArrayList<Widget> (widget.getChildren());
        for (Map.Entry<Placement,ArrayList<Widget>> entry : reverse.entrySet()) {
            Placement placement = entry.getKey ();
            ArrayList<Widget> currentlyResolving = null;
            for (Widget childWidget : entry.getValue()) {
                if (childWidget.getParentWidget() == widget  &&  childWidget.isVisible()) {
                    if (currentlyResolving == null)
                        currentlyResolving = new ArrayList<Widget>();
                    currentlyResolving.add(childWidget);
                }
            }
            if (currentlyResolving == null)
                continue;
            Point point;
            if (empty) {
                point = new Point();
            } else if (placement.isPercentage) {
                float percentage = placement.placementInPercentage;
                if (percentage <= 0.0)
                    point = connectionWidget.getFirstControlPoint ();
                else if (percentage >= 1.0)
                    point = connectionWidget.getLastControlPoint ();
                else
                    point = getLinePointAtPercentage (distances, (int) (percentage * totalDistance), controlPoints);
            } else {
                int distance = placement.placementAtDistance;
                if (distance < 0)
                    point = getLinePointAtPercentage (distances, distance + (int) totalDistance, controlPoints);
                else
                    point = getLinePointAtPercentage (distances, distance, controlPoints);
            }
            layoutChildrenAt (point, placement.alignment, connectionWidget, currentlyResolving);
            childrenToResolve.removeAll (currentlyResolving);
        }
        if (! childrenToResolve.isEmpty())
            layoutChildrenAt (new Point (), null, connectionWidget, childrenToResolve);
    }

    public boolean requiresJustification (Widget widget) {
        return false;
    }

    public void justify (Widget widget) {
    }

    private Point getLinePointAtPercentage (double[] distances, int lineDistance, java.util.List<Point> controlPoints) {
        int index = distances.length - 1;
        for (int i = 0; i < distances.length; i ++) {
            if (lineDistance < distances[i]) {
                index = i;
                break;
            }
        }

        double segmentStartDistance = index > 0 ? distances[index - 1] : 0;
        double segmentLength = distances[index] - segmentStartDistance;
        double segmentDistance = lineDistance - segmentStartDistance;

        if (segmentLength == 0.0) {
            return controlPoints.get (index);
        }

        Point p1 = controlPoints.get (index);
        Point p2 = controlPoints.get (index + 1);

        double segmentFactor = segmentDistance / segmentLength;

        return new Point ((int) (p1.x + (p2.x - p1.x) * segmentFactor), (int) (p1.y + (p2.y - p1.y) * segmentFactor));
    }

    private void layoutChildrenAt (Point linePoint, ConnectionWidgetLayoutAlignment alignment, ConnectionWidget connectionWidget, ArrayList<Widget> children) {
        if (alignment == null)
            alignment = ConnectionWidgetLayoutAlignment.NONE;
        ConnectionWidgetLayoutAlignment adjustedAlignment = getAdjustedAlignment(alignment, connectionWidget);
        if (useStacking) {
            layoutStackedChildrenAt (linePoint, adjustedAlignment, connectionWidget, children);
        } else {
            for (Widget childWidget : children) {
                layoutSingleChildAt (linePoint, adjustedAlignment, connectionWidget, childWidget);
            }
        }
    }
    
    private void layoutStackedChildrenAt (Point linePoint, ConnectionWidgetLayoutAlignment adjustedAlignment, ConnectionWidget connectionWidget, ArrayList<Widget> children) {
        int areaWidth = 0, areaHeight = 0;
        if (adjustedAlignment != ConnectionWidgetLayoutAlignment.NONE) {
            for (Widget childWidget : children) {
                Rectangle bounds = childWidget.getPreferredBounds ();
                areaWidth = Math.max (areaWidth, bounds.width);
                areaHeight += bounds.height;
            }
        }
        Point referencePoint = getReferencePointForAdjustedAlignment(adjustedAlignment, new Rectangle (areaWidth, areaHeight));
        int areaX = linePoint.x - referencePoint.x;
        int areaY = linePoint.y - referencePoint.y;

        int yCursor = 0;
        for (Widget childWidget : children) {
            Rectangle preferredBounds = childWidget.getPreferredBounds ();
            Point location = childWidget.getPreferredLocation ();
            int x = areaX - preferredBounds.x;
            int y = areaY + yCursor - preferredBounds.y;
            if (location != null) {
                x += location.x;
                y += location.y;
            }
            switch (adjustedAlignment) {
                case CENTER_LEFT:
                    break;
                case CENTER_RIGHT:
                    x += areaWidth - preferredBounds.width;
                    break;
                case CENTER:
                    x += (areaWidth - preferredBounds.width) / 2;
                    break;
            }
            yCursor += preferredBounds.height;
            childWidget.resolveBounds (new Point (x, y), preferredBounds);
        }
    }

    private void layoutSingleChildAt (Point linePoint, ConnectionWidgetLayoutAlignment alignment, ConnectionWidget connectionWidget, Widget childWidget) {
        Rectangle preferredBounds = childWidget.getPreferredBounds ();
        Point referencePoint = getReferencePointForAdjustedAlignment (alignment, preferredBounds);
        Point location = childWidget.getPreferredLocation ();
        if (location != null)
            referencePoint.translate (-location.x, -location.y);
        childWidget.resolveBounds (new Point (linePoint.x - referencePoint.x, linePoint.y - referencePoint.y), preferredBounds);
    }

    private ConnectionWidgetLayoutAlignment getAdjustedHorizontalAlignment (ConnectionWidgetLayoutAlignment adjustedAlignment) {
        switch (adjustedAlignment) {
            case TOP_LEFT:
            case CENTER_LEFT:
            case BOTTOM_LEFT:
                return ConnectionWidgetLayoutAlignment.CENTER_LEFT;
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                return ConnectionWidgetLayoutAlignment.CENTER_RIGHT;
            case TOP_CENTER:
            case CENTER:
            case BOTTOM_CENTER:
                return ConnectionWidgetLayoutAlignment.CENTER;
            default:
                return ConnectionWidgetLayoutAlignment.NONE;
        }
    }
    
    private Point getReferencePointForAdjustedAlignment (ConnectionWidgetLayoutAlignment adjustedAlignment, Rectangle rectangle) {
        switch (adjustedAlignment) {
            case BOTTOM_CENTER:
                return new Point (GeomUtil.centerX (rectangle), rectangle.y - 1);
            case BOTTOM_LEFT:
                return new Point (rectangle.x + rectangle.width, rectangle.y - 1);
            case BOTTOM_RIGHT:
                return new Point (rectangle.x - 1, rectangle.y - 1);
            case CENTER:
                return GeomUtil.center (rectangle);
            case CENTER_LEFT:
                return new Point (rectangle.x + rectangle.width, GeomUtil.centerY (rectangle));
            case CENTER_RIGHT:
                return new Point (rectangle.x - 1, GeomUtil.centerY (rectangle));
            case NONE:
                return new Point ();
            case TOP_CENTER:
                return new Point (GeomUtil.centerX (rectangle), rectangle.y + rectangle.height);
            case TOP_LEFT:
                return new Point (rectangle.x + rectangle.width, rectangle.y + rectangle.height);
            case TOP_RIGHT:
                return new Point (rectangle.x - 1, rectangle.y + rectangle.height);
            default:
                return new Point ();
        }
    }
    
    private ConnectionWidgetLayoutAlignment getAdjustedAlignment(ConnectionWidgetLayoutAlignment alignment, ConnectionWidget connectionWidget) {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = alignment;
        
        if(alignment == ConnectionWidgetLayoutAlignment.CENTER_SOURCE) {
            Point sourcePt = connectionWidget.getFirstControlPoint();
            Rectangle sourceBounds = getSourceBounds(connectionWidget);

            retVal = calculateBestCenterAlignment(sourcePt, sourceBounds);
        } else if(alignment == ConnectionWidgetLayoutAlignment.CENTER_TARGET) {
            Point targetPt = connectionWidget.getLastControlPoint();
            Rectangle targetBounds = getTargetBounds(connectionWidget);

            retVal = calculateBestCenterAlignment(targetPt, targetBounds);
        } else if(alignment == ConnectionWidgetLayoutAlignment.BOTTOM_SOURCE) {
            Point sourcePt = connectionWidget.getFirstControlPoint();
            Rectangle sourceBounds = getSourceBounds(connectionWidget);
            
            retVal = calculateBestBottomAlignment(sourcePt, sourceBounds);
        } else if(alignment == ConnectionWidgetLayoutAlignment.BOTTOM_TARGET) {   
            Point targetPt = connectionWidget.getLastControlPoint();
            Rectangle targetBounds = getTargetBounds(connectionWidget);

            retVal = calculateBestBottomAlignment(targetPt, targetBounds);
        } else if(alignment == ConnectionWidgetLayoutAlignment.TOP_SOURCE) {
            Point sourcePt = connectionWidget.getFirstControlPoint();
            Rectangle sourceBounds = getSourceBounds(connectionWidget);
            
            retVal = calculateBestTopAlignment(sourcePt, sourceBounds);
        } else if(alignment == ConnectionWidgetLayoutAlignment.TOP_TARGET) {
            Point targetPt = connectionWidget.getLastControlPoint();
            Rectangle targetBounds = getTargetBounds(connectionWidget);

            retVal = calculateBestTopAlignment(targetPt, targetBounds);
        }
        
        return retVal;
    }
    
    private LayoutFactory.ConnectionWidgetLayoutAlignment calculateBestCenterAlignment(Point point, Rectangle bounds) {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.NONE;
        
        if(bounds != null) {
            if(point.x <= bounds.x) {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_LEFT;
            } else if(point.x >= (bounds.x + bounds.width)) {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_RIGHT;
            } else if(point.y <= bounds.y) {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_CENTER;
            } else if(point.y >= (bounds.y + bounds.height)) {
                retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_CENTER;
            }
        }
        
        return retVal;
    }
    
    private LayoutFactory.ConnectionWidgetLayoutAlignment calculateBestBottomAlignment(Point point, Rectangle bounds) {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.NONE;
        
        if(point.x <= bounds.x) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_LEFT;
        } else if(point.x >= (bounds.x + bounds.width)) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_RIGHT;
        } else if(point.y <= bounds.y) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT;
        } else if(point.y >= (bounds.y + bounds.height)) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_LEFT;
        }
        
        return retVal;
    }
    
    private LayoutFactory.ConnectionWidgetLayoutAlignment calculateBestTopAlignment(Point point, Rectangle bounds) {
        LayoutFactory.ConnectionWidgetLayoutAlignment retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.NONE;
        
        if(point.x <= bounds.x) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_LEFT;
        } else if(point.x >= (bounds.x + bounds.width)) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT;
        } else if(point.y <= bounds.y) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_LEFT;
        } else if(point.y >= (bounds.y + bounds.height)) {
            retVal = LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_RIGHT;
        }
        
        return retVal;
    }
    
    private Rectangle getSourceBounds(ConnectionWidget connectionWidget) {
        Widget source = connectionWidget.getSourceAnchor().getRelatedWidget();
        if (source == null)
            return null;

        Point sourceLocation = source.getLocation();
        Rectangle clientArea = source.getClientArea();
        return new Rectangle(sourceLocation, clientArea.getSize());
    }

    private Rectangle getTargetBounds(ConnectionWidget connectionWidget) {
        Widget target = connectionWidget.getTargetAnchor().getRelatedWidget();
        if (target == null)
            return null;

        Point targetLocation = target.getLocation();
        Rectangle targetArea = target.getClientArea();
        return new Rectangle(targetLocation, targetArea.getSize());
    }
    
    private static class Placement {
        
        private final ConnectionWidgetLayoutAlignment alignment;
        private final boolean isPercentage;
        private final float placementInPercentage;
        private final int placementAtDistance;

        public Placement(ConnectionWidgetLayoutAlignment alignment, float placementInPercentage) {
            this.alignment = alignment;
            this.isPercentage = true;
            this.placementInPercentage = placementInPercentage;
            this.placementAtDistance = 0;
        }

        public Placement(ConnectionWidgetLayoutAlignment alignment, int placementAtDistance) {
            this.alignment = alignment;
            this.isPercentage = false;
            this.placementInPercentage = 0.0f;
            this.placementAtDistance = placementAtDistance;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Placement other = (Placement) obj;
            if (this.alignment != other.alignment) {
                return false;
            }
            if (this.isPercentage != other.isPercentage) {
                return false;
            }
            if (this.placementInPercentage != other.placementInPercentage) {
                return false;
            }
            if (this.placementAtDistance != other.placementAtDistance) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + (this.alignment != null ? this.alignment.hashCode() : 0);
            hash = 17 * hash + (this.isPercentage ? 1 : 0);
            hash = 17 * hash + Float.floatToIntBits(this.placementInPercentage);
            hash = 17 * hash + this.placementAtDistance;
            return hash;
        }
        
    }

}
