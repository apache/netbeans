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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
     * @return List<Point> of control points
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
