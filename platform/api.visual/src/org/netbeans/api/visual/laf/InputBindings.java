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

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * This represents input bindings e.g. manages modifiers of actions.
 *
 * @author David Kaspar
 * @since 2.4
 */
public final class InputBindings {

    private int zoomActionModifiers = KeyEvent.CTRL_MASK;
    private int panActionButton = MouseEvent.BUTTON2;
    
    private InputBindings () {
    }

    /**
     * Returns InputEvent modifiers of all zoom actions.
     * @return the modifiers
     * @since 2.4
     */
    public int getZoomActionModifiers () {
        return zoomActionModifiers;
    }

    /**
     * Sets InputEvent modifiers for all zoom actions.
     * @param zoomActionModifiers the modifiers
     * @since 2.4
     */
    public void setZoomActionModifiers (int zoomActionModifiers) {
        this.zoomActionModifiers = zoomActionModifiers;
    }

    /**
     * Returns mouse button for pan action.
     * @return the MouseEvent button code
     * @since 2.40
     */
    public int getPanActionButton () {
        return panActionButton;
    }

    /**
     * Sets mouse button for pan action.
     * @param panActionButton MouseEvent button code
     * @since 2.40
     */
    public void setPanActionButton (int panActionButton) {
        this.panActionButton = panActionButton;
    }

    /**
     * Creates a new input bindings. This is usually used by the Scene class only.
     * @return the input bindings
     * @since 2.4
     */
    public static InputBindings create () {
        return new InputBindings ();
    }

}
