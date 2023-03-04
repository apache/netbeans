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
package org.netbeans.api.visual.laf;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.modules.visual.laf.DefaultLookFeel;

import java.awt.*;

/**
 * This class is defining a reusable LookAndFeel fragments e.g. colors.
 *
 * @author David Kaspar
 */
public abstract class LookFeel {

    private static final LookFeel DEFAULT = new DefaultLookFeel ();

    /**
     * Creates a default look and feel. The instance can be shared by multiple scenes.
     * @return the default look and feel
     */
    public static LookFeel createDefaultLookFeel () {
        return DEFAULT;
    }

    /**
     * Returns a default scene background
     * @return the default background
     */
    public abstract Paint getBackground ();

    /**
     * Returns a default scene foreground
     * @return the default foreground
     */
    public abstract Color getForeground ();

    /**
     * Returns a border for a specific state.
     * @param state the state
     * @return the border
     */
    public abstract Border getBorder (ObjectState state);

    /**
     * Returns a minimalistic version of border for a specific state.
     * @param state the state
     * @return the mini-border
     */
    public abstract Border getMiniBorder (ObjectState state);

    /**
     * Returns a opacity value for a specific state.
     * @param state the state
     * @return the opacity value
     */
    public abstract boolean getOpaque (ObjectState state);

    /**
     * Returns a line color for a specific state.
     * @param state the state
     * @return the line color
     */
    public abstract Color getLineColor (ObjectState state);

    /**
     * Returns a background for a specific state.
     * @param state the state
     * @return the background
     */
    public abstract Paint getBackground (ObjectState state);

    /**
     * Returns a foreground for a specific state.
     * @param state the state
     * @return the foreground
     */
    public abstract Color getForeground (ObjectState state);

    /**
     * Returns a margin for a specific state. It is used with borders - usually the value is a inset value of a border.
     * @return the margin
     */
    // TODO - is naming correct?
    public abstract int getMargin ();
/*
    public void updateWidget (Widget widget, WidgetState state) {
        widget.setBorder (getBorder (state));
        widget.setOpaque (getOpaque (state));
        widget.setBackground (getBackground (state));
        widget.setForeground (getForeground (state));
    }
*/
}
