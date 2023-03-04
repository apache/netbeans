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
package org.netbeans.modules.visual.laf;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.laf.LookFeel;

import java.awt.*;
import javax.swing.UIManager;

/**
 * @author David Kaspar
 */
public class DefaultLookFeel extends LookFeel {

    private static final Color COLOR_SELECTED = new Color (0x447BCD);
    private static final Color COLOR_HIGHLIGHTED = COLOR_SELECTED.darker ();
    private static final Color COLOR_HOVERED = COLOR_SELECTED.brighter ();
    private static final int MARGIN = 3;
    private static final int ARC = 10;
    private static final int MINI_THICKNESS = 1;

    private static final Border BORDER_NORMAL = BorderFactory.createEmptyBorder (MARGIN, MARGIN);
    private static final Border BORDER_HOVERED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border BORDER_SELECTED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_SELECTED, COLOR_SELECTED.darker ());

    private static final Border MINI_BORDER_NORMAL = BorderFactory.createEmptyBorder (MINI_THICKNESS);
    private static final Border MINI_BORDER_HOVERED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border MINI_BORDER_SELECTED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_SELECTED, COLOR_SELECTED.darker ());

    public Paint getBackground () {
        return UIManager.getColor("Label.background");
        //return Color.WHITE;
    }

    public Color getForeground () {
        return UIManager.getColor("Label.foreground");
        //return Color.BLACK;
    }

    public Border getBorder (ObjectState state) {
        if (state.isHovered ())
            return BORDER_HOVERED;
        if (state.isSelected ())
            return BORDER_SELECTED;
        if (state.isFocused ())
            return BORDER_HOVERED;
        return BORDER_NORMAL;
    }

    public Border getMiniBorder (ObjectState state) {
        if (state.isHovered ())
            return MINI_BORDER_HOVERED;
        if (state.isSelected ())
            return MINI_BORDER_SELECTED;
        if (state.isFocused ())
            return MINI_BORDER_HOVERED;
        return MINI_BORDER_NORMAL;
    }

    public boolean getOpaque (ObjectState state) {
        return state.isHovered ()  ||  state.isSelected ();
    }

    public Color getLineColor (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return getForeground ();//Color.BLACK;
    }

    public Paint getBackground (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return getBackground ();//Color.WHITE;
    }

    public Color getForeground (ObjectState state) {
        return state.isSelected () ? (Color)getBackground () : getForeground ();//Color.WHITE : Color.BLACK;
    }

    public int getMargin () {
        return MARGIN;
    }

}
