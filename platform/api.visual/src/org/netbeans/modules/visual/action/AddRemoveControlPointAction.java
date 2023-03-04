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
package org.netbeans.modules.visual.action;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;

/**
 * @author Alex
 */
public class AddRemoveControlPointAction extends WidgetAction.Adapter {

    private double createSensitivity;
    private double deleteSensitivity;
    private ConnectionWidget.RoutingPolicy routingPolicy;

    public AddRemoveControlPointAction (double createSensitivity, double deleteSensitivity, ConnectionWidget.RoutingPolicy routingPolicy) {
        this.createSensitivity = createSensitivity;
        this.deleteSensitivity = deleteSensitivity;
        this.routingPolicy = routingPolicy;
    }

    public State mouseClicked(Widget widget, WidgetMouseEvent event) {
        if(event.getButton()==MouseEvent.BUTTON1 && event.getClickCount()==2  &&  widget instanceof ConnectionWidget) {
            addRemoveControlPoint ((ConnectionWidget) widget, event.getPoint ());
            return State.CONSUMED;
        }
        return State.REJECTED;
    }
    
    /**
     * Adds or removes a control point on a specified location
     * @param widget the connection widget
     * @param localLocation the local location
     */
    private void addRemoveControlPoint (ConnectionWidget widget, Point localLocation) {
        ArrayList<Point> list = new ArrayList<Point> (widget.getControlPoints ());
        if (!removeControlPoint (localLocation, list, deleteSensitivity)) {
            Point exPoint = null;
            int index = 0;
            for (Point elem : list) {
                if (exPoint != null) {
                    Line2D l2d = new Line2D.Double (exPoint, elem);
                    if (l2d.ptSegDist (localLocation) < createSensitivity) {
                        list.add (index, localLocation);
                        break;
                    }
                }
                exPoint = elem;
                index++;
            }
        }
        if (routingPolicy != null)
            widget.setRoutingPolicy (routingPolicy);
        widget.setControlPoints (list, false);
    }
    
    private boolean removeControlPoint(Point point, ArrayList<Point> list, double deleteSensitivity){
        for (Point elem : list) {
            if (elem.distance (point) < deleteSensitivity) {
                list.remove (elem);
                return true;
            }
        }
        return false;
    }

}
