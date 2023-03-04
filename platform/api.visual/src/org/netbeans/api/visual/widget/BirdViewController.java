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
package org.netbeans.api.visual.widget;

import org.netbeans.modules.visual.widget.BirdViewWindow;

import java.awt.*;

/**
 * This class controls a bird view created for a specific scene. The bird is tracking mouse-cursor over the main scene view.
 * You can specify a separate zoom-factor and you can enable and disable it by calling <code>show</code> and <code>hide</code> methods.
 * <p>
 * When a bird view is enabled then it consumes all events of a main scene view therefore you cannot do anything except
 * watch the scene with bird view.
 *
 * @since 2.7
 * @author David Kaspar
 */
public final class BirdViewController {

    private BirdViewWindow birdView;

    BirdViewController (Scene scene) {
        birdView = new BirdViewWindow (scene);
    }

    /**
     * Sets a zoom factor of the bird view.
     * @param zoomFactor the zoom factor
     * @since 2.7
     */
    public void setZoomFactor (double zoomFactor) {
        birdView.setZoomFactor (zoomFactor);
    }

    /**
     * Sets a size of the bird view window.
     * @param size the window size
     * @since 2.7
     */
    public void setWindowSize (Dimension size) {
        birdView.setWindowSize (size);
    }

    /**
     * Enables the bird view. It means that the bird view window will be visible while a mouse cursor is over the visible
     * area of the main scene view.
     * <p>
     * Note: Has to be invoked after <code>Scene.createView</code> method.
     * <p>
     * Note: An user has to initially move cursor over the visible area of the main scene view
     * to show the window up for the first time after the method call.
     * @since 2.7
     */
    public void show () {
        birdView.invokeShow ();
    }

    /**
     * Disables the bird view. It means the bird view window is hidden and the main scene view is not blocked for events.
     * @since 2.7
     */
    public void hide () {
        birdView.invokeHide ();
    }

}
