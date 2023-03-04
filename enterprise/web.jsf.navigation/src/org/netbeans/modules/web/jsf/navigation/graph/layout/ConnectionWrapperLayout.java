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
package org.netbeans.modules.web.jsf.navigation.graph.layout;

import java.util.Date;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Entry;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * This wrapper delegates to original ConnectionLayout, but allows lazy label formating.
 * @author joelle
 */
public class ConnectionWrapperLayout implements Layout {

    private ConnectionWidget connectionWidget;
    private Layout connectionWidgetLayout;
    private LabelWidget label;

    public ConnectionWrapperLayout(ConnectionWidget connectionWidget, LabelWidget label) {
        this.connectionWidget = connectionWidget;
        this.connectionWidgetLayout = connectionWidget.getLayout();
        this.label = label;
    }

    public void layout(Widget widget) {
        connectionWidgetLayout.layout(widget);
        resetLabelConstraint(connectionWidget, label);
    }

    public boolean requiresJustification(Widget widget) {
        return connectionWidgetLayout.requiresJustification(widget);
    }

    public void justify(Widget widget) {
        connectionWidgetLayout.justify(widget);
    }

    private static final Logger LOGGER = Logger.getLogger(ConnectionWrapperLayout.class.toString());
    private static final void resetLabelConstraint(ConnectionWidget connectionWidget, LabelWidget label) {
        assert connectionWidget != null;

        if (label != null) {

            connectionWidget.removeConstraint(label);
            connectionWidget.removeChild(label);
            Anchor sourceAnchor = connectionWidget.getSourceAnchor();
            Entry sourceAnchorEntry = connectionWidget.getSourceAnchorEntry();
            assert sourceAnchor != null;
            assert sourceAnchorEntry != null;

            if (sourceAnchor != null && sourceAnchorEntry != null) {
                EnumSet<Anchor.Direction> directions = sourceAnchor.compute(sourceAnchorEntry).getDirections();
                if (directions.contains(Anchor.Direction.TOP)) {
                    label.setOrientation(LabelWidget.Orientation.ROTATE_90);
                    connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
                } else if (directions.contains(Anchor.Direction.BOTTOM)) {
                    label.setOrientation(LabelWidget.Orientation.ROTATE_90);
                    connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.BOTTOM_RIGHT, 10);
                } else if (directions.contains(Anchor.Direction.RIGHT)) {
                    connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_RIGHT, 10);
                    label.setOrientation(LabelWidget.Orientation.NORMAL);
                } else {
                    connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_LEFT, 10);
                    label.setOrientation(LabelWidget.Orientation.NORMAL);
                }
            } else {
                    LogRecord record = new LogRecord(Level.FINE, "Problems Reseting Label Constraint");
                    record.setSourceClassName("ConnectionWrapperLayout");
                    record.setSourceMethodName("resetLabelConstraint");
                    record.setParameters(new Object[]{connectionWidget, label, new Date()});
                    LOGGER.log(record);
                    connectionWidget.setConstraint(label, LayoutFactory.ConnectionWidgetLayoutAlignment.TOP_LEFT, 10);
                    label.setOrientation(LabelWidget.Orientation.NORMAL);
            }
            connectionWidget.addChild(label);
        }
    }
}
