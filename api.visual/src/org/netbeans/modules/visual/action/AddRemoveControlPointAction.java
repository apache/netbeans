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
