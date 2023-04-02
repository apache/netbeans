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
package org.netbeans.jellytools.widgets;

import java.awt.Point;
import java.util.List;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jellytools.TopComponentOperator;

/**
 * Handle org.netbeans.api.visual.widget.ConnectionWidget object which connects
 * source and target widgets.
 * <p>
 * Usage:<br>
 * <pre>
        TopComponentOperator tco = new TopComponentOperator("My scene");
        LabelWidgetOperator lwo0 = new LabelWidgetOperator(tco, "Label 0");
        lwo0.performPopupAction("An action");
        LabelWidgetOperator lwo1 = new LabelWidgetOperator(tco, "Label 1");
        // drag from one widget to another to make connection
        lwo0.dragNDrop(lwo1);
        ConnectionWidgetOperator cwo = new ConnectionWidgetOperator(lwo0, lwo1);
        Point sourceControlPoint = cwo.getSourceControlPoint();
        Point targetControlPoint = cwo.getTargetControlPoint();
 * </pre>
 * 
 * @see WidgetOperator
 * @author Jiri Skrivanek
 */
public class ConnectionWidgetOperator extends WidgetOperator {

    /** Creates operator for given ConnectionWidget.
     * @param widget ConnectionWidget to create operator for
     */
    public ConnectionWidgetOperator(ConnectionWidget widget) {
        super(widget);
    }

    /** Waits for index-th ConnectionWidget under given parent Widget.
     * @param parentWidgetOper parent WidgetOperator
     * @param index index of widget to be found
     */
    public ConnectionWidgetOperator(WidgetOperator parentWidgetOper, int index) {
        super(parentWidgetOper, new ConnectionWidgetChooser(), index);
    }

    /** Waits for index-th ConnectionWidget under given parent Widget.
     * @param parentWidgetOper parent WidgetOperator
     */
    public ConnectionWidgetOperator(WidgetOperator parentWidgetOper) {
        this(parentWidgetOper, 0);
    }

    /** Waits for ConnectionWidget with given source and target widgets.
     * @param sourceWidgetOper source widget
     * @param targetWidgetOper target widget
     */
    public ConnectionWidgetOperator(WidgetOperator sourceWidgetOper, WidgetOperator targetWidgetOper) {
        super(sourceWidgetOper.getSceneOperator(), new ConnectionWidgetChooser(sourceWidgetOper, targetWidgetOper));
    }

    /** Waits for ConnectionWidget under given TopComponent.
     * @param tco TopComponentOperator to find widgets in
     */
    public ConnectionWidgetOperator(TopComponentOperator tco) {
        this(tco, 0);
    }

    /** Waits for index-th ConnectionWidget under given TopComponent.
     * @param tco TopComponentOperator to find widgets in
     * @param index index of widget to be found
     */
    public ConnectionWidgetOperator(TopComponentOperator tco, int index) {
        super(tco, new ConnectionWidgetChooser(), index);
    }

    /** WidgetChooser to find ConnectionWidget with specified source and target widgets. */
    private static final class ConnectionWidgetChooser implements WidgetChooser {

        private WidgetOperator sourceWidgetOper;
        private WidgetOperator targetWidgetOper;

        /** Used to find ConnectionWidget. */
        public ConnectionWidgetChooser() {
        }

        public ConnectionWidgetChooser(WidgetOperator sourceWidgetOper, WidgetOperator targetWidgetOper) {
            this.sourceWidgetOper = sourceWidgetOper;
            this.targetWidgetOper = targetWidgetOper;
        }

        public boolean checkWidget(Widget widget) {
            if (widget instanceof ConnectionWidget) {
                if (sourceWidgetOper != null && targetWidgetOper != null) {
                    ConnectionWidgetOperator cwo = new ConnectionWidgetOperator((ConnectionWidget) widget);
                    if (sourceWidgetOper.getWidget() == cwo.getSourceWidgetOperator().getWidget() &&
                            targetWidgetOper.getWidget() == cwo.getTargetWidgetOperator().getWidget()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
            return false;
        }

        public String getDescription() {
            return "ConnectionWidget" +
                    (sourceWidgetOper != null && targetWidgetOper != null ? " with source " + sourceWidgetOper + " and target " + targetWidgetOper : "");
        }
    }

    /** Returns control point of source widget.
     * @return Point representing control point of source widget.
     */
    public Point getSourceControlPoint() {
        return (Point) runMapping(new MapAction("getFirstControlPoint") {

            public Object map() {
                return ((ConnectionWidget) widget).getFirstControlPoint();
            }
        });
    }

    /** Returns control point of target widget.
     * @return Point representing control point of target widget.
     */
    public Point getTargetControlPoint() {
        return (Point) runMapping(new MapAction("getLastControlPoint") {

            public Object map() {
                return ((ConnectionWidget) widget).getLastControlPoint();
            }
        });
    }

    /** Returns index-th control point.
     * @param index index of requested control point
     * @return Point representing control point of source widget.
     */
    public Point getControlPoint(final int index) {
        return (Point) runMapping(new MapAction("getControlPoint") {

            public Object map() {
                return ((ConnectionWidget) widget).getControlPoint(index);
            }
        });
    }

    /** Returns list of control points.
     * @return {@code List<Point>} of control points
     */
    @SuppressWarnings("unchecked")
    public List<Point> getControlPoints() {
        return (List<Point>) runMapping(new MapAction("getControlPoints") {

            public Object map() {
                return ((ConnectionWidget) widget).getControlPoints();
            }
        });
    }

    /** Returns WidgetOperator of source widget of this connection.
     * @return WidgetOperator instance of source widget of this connection.
     */
    public WidgetOperator getSourceWidgetOperator() {
        return new WidgetOperator((Widget) runMapping(new MapAction("getSourceAnchor().getRelatedWidget()") {

            public Object map() {
                return ((ConnectionWidget) widget).getSourceAnchor().getRelatedWidget();
            }
        }));
    }

    /** Returns WidgetOperator of target widget of this connection.
     * @return WidgetOperator instance of target widget of this connection.
     */
    public WidgetOperator getTargetWidgetOperator() {
        return new WidgetOperator((Widget) runMapping(new MapAction("getTargetAnchor().getRelatedWidget()") {

            public Object map() {
                return ((ConnectionWidget) widget).getTargetAnchor().getRelatedWidget();
            }
        }));
    }
}
