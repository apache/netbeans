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
package org.netbeans.modules.web.jsf.navigation.graph;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.vmd.*;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import java.awt.*;
import org.netbeans.api.visual.anchor.PointShapeFactory;

/**
 * @author David Kaspar
 */
public class PFENotModifiableScheme extends VMDColorScheme {

    private static final Color COLOR60_SELECT = new Color( 210,210,210);
    private static final Color COLOR60_HOVER = new Color( 200, 200, 200);
    private static final Color COLOR60_HOVER_BACKGROUND = new Color (0xB0C3E1);
    private static final Color COLOR_NORMAL =  new Color(230,230,230);
    private static final Color COLOR_HIGHLIGHTED = new Color (0x316AC5);
    private static final PointShape POINT_SHAPE_IMAGE = PointShapeFactory.createImagePointShape (ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-pin.png")); // NOI18N

    private static final Border BORDER_PIN = BorderFactory.createOpaqueBorder (2, 8, 2, 8);
    private static final Border BORDER60_PIN_SELECT = BorderFactory.createCompositeBorder (BorderFactory.createLineBorder (0, 1, 0, 1, COLOR60_SELECT), BorderFactory.createLineBorder (2, 7, 2, 7, COLOR60_SELECT));

    public void installUI (VMDNodeWidget widget) {
    }

    public void updateUI (VMDNodeWidget widget, ObjectState previousState, ObjectState state) {

    }

    public void installUI (VMDConnectionWidget widget) {
        widget.setSourceAnchorShape (AnchorShape.NONE);
        widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints (true);
    }

 
    public void updateUI (VMDConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isSelected ()) {
            widget.setForeground (COLOR60_SELECT);
        } else if (state.isHighlighted ()) {
            widget.setForeground (COLOR_HIGHLIGHTED);
        } else if (state.isHovered ()  ||  state.isFocused ()) {
            widget.setForeground (COLOR60_HOVER);
        } else {
            widget.setForeground (COLOR_NORMAL);
        }

        if (state.isSelected ()  ||  state.isHovered ()) {
            widget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
            widget.setControlPointCutDistance (0);
        } else {
            widget.setControlPointShape (PointShape.NONE);
            widget.setEndPointShape (POINT_SHAPE_IMAGE);
            widget.setControlPointCutDistance (5);
        }
    }

    public void installUI (VMDPinWidget widget) {
        widget.setBorder (BORDER_PIN);
        widget.setBackground (COLOR60_HOVER_BACKGROUND);
    }

    public void updateUI (VMDPinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque (state.isHovered ()  ||  state.isFocused ());
        if (state.isSelected ()) {
            widget.setBorder (BORDER60_PIN_SELECT);
        } else {
            widget.setBorder (BORDER_PIN);
        }
    }

    public int getNodeAnchorGap (VMDNodeAnchor anchor) {
        return 4;
    }

    public boolean isNodeMinimizeButtonOnRight (VMDNodeWidget widget) {
        return true;
    }

    public Image getMinimizeWidgetImage (VMDNodeWidget widget) {
        return widget.isMinimized ()
                ? ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-expand-60.png") // NOI18N
                : ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-collapse-60.png"); // NOI18N
    }

    public Widget createPinCategoryWidget (VMDNodeWidget widget, String categoryDisplayName) {
        return VMDFactory.getOriginalScheme ().createPinCategoryWidget (widget, categoryDisplayName);
    }



}
