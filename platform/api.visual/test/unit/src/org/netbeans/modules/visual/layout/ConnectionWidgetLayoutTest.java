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
package org.netbeans.modules.visual.layout;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.netbeans.api.visual.layout.LayoutFactory.ConnectionWidgetLayoutAlignment;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.framework.VisualTestCase;

public class ConnectionWidgetLayoutTest extends VisualTestCase {

    public ConnectionWidgetLayoutTest(String testName) {
        super(testName);
    }

    @Test
    public void testConnectionWidgetLayoutNoControlPoint() {
        Scene scene = new Scene();
        ConnectionWidget w1 = new ConnectionWidget(scene);

        ConnectionWidgetLayout connectionWidgetLayout = new ConnectionWidgetLayout();
        List<Point> lc = new ArrayList<>();
        w1.setControlPoints(lc, true);
        connectionWidgetLayout.layout(w1);
    }

    @Test
    public void testConnectionWidgetLayoutTwoControlPoint() {
        Scene scene = new Scene();
        ConnectionWidget w1 = new ConnectionWidget(scene);
        ConnectionWidgetLayout connectionWidgetLayout = new ConnectionWidgetLayout();
        List<Point> lc = new ArrayList<>();
        lc.add(new Point());
        lc.add(new Point());
        w1.setControlPoints(lc, true);
        connectionWidgetLayout.layout(w1);
    }

    @Test
    public void testConnectionWidgetLayoutOnControlPointPercentageInRange() {
        Scene scene = new Scene();
        ConnectionWidget w1 = new ConnectionWidget(scene);
        ConnectionWidgetLayout connectionWidgetLayout = new ConnectionWidgetLayout();
        List<Point> lc = new ArrayList<>();
        lc.add(new Point());
        w1.setControlPoints(lc, true);
        Widget w2 = new Widget(scene);
        w1.addChild(w2);
        connectionWidgetLayout.setConstraint(w2, ConnectionWidgetLayoutAlignment.BOTTOM_CENTER, .5f);
        connectionWidgetLayout.layout(w1);
    }

    @Test
    public void testConnectionWidgetLayoutOnControlPointPercentageUpperRange() {
        Scene scene = new Scene();
        ConnectionWidget w1 = new ConnectionWidget(scene);
        ConnectionWidgetLayout connectionWidgetLayout = new ConnectionWidgetLayout();
        List<Point> lc = new ArrayList<>();
        lc.add(new Point());
        w1.setControlPoints(lc, true);
        Widget w2 = new Widget(scene);
        w1.addChild(w2);
        connectionWidgetLayout.setConstraint(w2, ConnectionWidgetLayoutAlignment.BOTTOM_CENTER, 1.0f);
        connectionWidgetLayout.layout(w1);
    }

    @Test
    public void testConnectionWidgetLayoutOnControlPointPercentageLowerRange() {
        Scene scene = new Scene();
        ConnectionWidget w1 = new ConnectionWidget(scene);
        ConnectionWidgetLayout connectionWidgetLayout = new ConnectionWidgetLayout();
        List<Point> lc = new ArrayList<>();
        lc.add(new Point());
        w1.setControlPoints(lc, true);
        Widget w2 = new Widget(scene);
        w1.addChild(w2);
        connectionWidgetLayout.setConstraint(w2, ConnectionWidgetLayoutAlignment.BOTTOM_CENTER, 1.0f);
        connectionWidgetLayout.layout(w1);
    }
    
    
    @Test
    public void testConnectionWidgetLayoutOnControlPointPlainBelowZero() {
        Scene scene = new Scene();
        ConnectionWidget w1 = new ConnectionWidget(scene);
        ConnectionWidgetLayout connectionWidgetLayout = new ConnectionWidgetLayout();
        List<Point> lc = new ArrayList<>();
        lc.add(new Point());
        w1.setControlPoints(lc, true);
        Widget w2 = new Widget(scene);
        w1.addChild(w2);
        connectionWidgetLayout.setConstraint(w2, ConnectionWidgetLayoutAlignment.BOTTOM_CENTER, -1);
        connectionWidgetLayout.layout(w1);
    }

    @Test
    public void testConnectionWidgetLayoutOnControlPointPlainAboveZero() {
        Scene scene = new Scene();
        ConnectionWidget w1 = new ConnectionWidget(scene);
        ConnectionWidgetLayout connectionWidgetLayout = new ConnectionWidgetLayout();
        List<Point> lc = new ArrayList<>();
        lc.add(new Point());
        w1.setControlPoints(lc, true);
        Widget w2 = new Widget(scene);
        w1.addChild(w2);
        connectionWidgetLayout.setConstraint(w2, ConnectionWidgetLayoutAlignment.BOTTOM_CENTER, 1);
        connectionWidgetLayout.layout(w1);
    }
}
