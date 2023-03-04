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
package org.netbeans.modules.visual.vmd;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.vmd.*;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.anchor.PointShapeFactory;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import java.awt.*;

/**
 * @author David Kaspar
 */
public class VMDNetBeans60ColorScheme extends VMDColorScheme {

    public static final Color COLOR60_SELECT = new Color (0xFF8500);
    public static final Color COLOR60_HOVER = new Color (0x5B67B0);
    public static final Color COLOR60_HOVER_BACKGROUND = new Color (0xB0C3E1);

    private static final Border BORDER60 = VMDFactory.createVMDNodeBorder (VMDOriginalColorScheme.COLOR_NORMAL, 2, VMDOriginalColorScheme.COLOR1, VMDOriginalColorScheme.COLOR2, VMDOriginalColorScheme.COLOR3, VMDOriginalColorScheme.COLOR4, VMDOriginalColorScheme.COLOR5);
    private static final Border BORDER60_SELECT = VMDFactory.createVMDNodeBorder (COLOR60_SELECT, 2, VMDOriginalColorScheme.COLOR1, VMDOriginalColorScheme.COLOR2, VMDOriginalColorScheme.COLOR3, VMDOriginalColorScheme.COLOR4, VMDOriginalColorScheme.COLOR5);
    private static final Border BORDER60_HOVER = VMDFactory.createVMDNodeBorder (COLOR60_HOVER, 2, VMDOriginalColorScheme.COLOR1, VMDOriginalColorScheme.COLOR2, VMDOriginalColorScheme.COLOR3, VMDOriginalColorScheme.COLOR4, VMDOriginalColorScheme.COLOR5);

    private static final Border BORDER60_PIN_SELECT = BorderFactory.createCompositeBorder (BorderFactory.createLineBorder (0, 1, 0, 1, COLOR60_SELECT), BorderFactory.createLineBorder (2, 7, 2, 7, COLOR60_SELECT));
//        private static final Border BORDER60_PIN_HOVER = BorderFactory.createLineBorder (2, 8, 2, 8, COLOR60_HOVER);

    private static final PointShape POINT_SHAPE60_IMAGE = PointShapeFactory.createImagePointShape (ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-pin-60.png")); // NOI18N

    public void installUI (VMDNodeWidget widget) {
        widget.setBorder (BORDER60);

        Widget header = widget.getHeader ();
        header.setBackground (COLOR60_HOVER_BACKGROUND);
        header.setBorder (VMDOriginalColorScheme.BORDER_PIN);

        Widget pinsSeparator = widget.getPinsSeparator ();
        pinsSeparator.setForeground (VMDOriginalColorScheme.BORDER_CATEGORY_BACKGROUND);
    }

    public void updateUI (VMDNodeWidget widget, ObjectState previousState, ObjectState state) {
        if (! previousState.isSelected ()  &&  state.isSelected ())
            widget.bringToFront ();

        boolean hover = state.isHovered () || state.isFocused ();
        widget.getHeader ().setOpaque (hover);

        if (state.isSelected ())
            widget.setBorder (BORDER60_SELECT);
        else if (state.isHovered ())
            widget.setBorder (BORDER60_HOVER);
        else if (state.isFocused ())
            widget.setBorder (BORDER60_HOVER);
        else
            widget.setBorder (BORDER60);
    }

    public void installUI (VMDConnectionWidget widget) {
        widget.setSourceAnchorShape (AnchorShape.NONE);
        widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints (true);
    }

    public void updateUI (VMDConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isSelected ())
            widget.setForeground (COLOR60_SELECT);
        else if (state.isHighlighted ())
            widget.setForeground (VMDOriginalColorScheme.COLOR_HIGHLIGHTED);
        else if (state.isHovered ()  ||  state.isFocused ())
            widget.setForeground (COLOR60_HOVER);
        else
            widget.setForeground (VMDOriginalColorScheme.COLOR_NORMAL);

        if (state.isSelected ()  ||  state.isHovered ()) {
            widget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
            widget.setControlPointCutDistance (0);
        } else {
            widget.setControlPointShape (PointShape.NONE);
            widget.setEndPointShape (POINT_SHAPE60_IMAGE);
            widget.setControlPointCutDistance (5);
        }
    }

    public void installUI (VMDPinWidget widget) {
        widget.setBorder (VMDOriginalColorScheme.BORDER_PIN);
        widget.setBackground (COLOR60_HOVER_BACKGROUND);
    }

    public void updateUI (VMDPinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque (state.isHovered ()  ||  state.isFocused ());
        if (state.isSelected ())
            widget.setBorder (BORDER60_PIN_SELECT);
        else
            widget.setBorder (VMDOriginalColorScheme.BORDER_PIN);
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
        return VMDOriginalColorScheme.createPinCategoryWidgetCore (widget, categoryDisplayName, false);
    }

}
