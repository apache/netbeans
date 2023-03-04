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

import java.awt.*;

/**
 * This is a widget with a level-of-details feature. The visibility of children is based on the zoom factor of a scene.
 * <p>
 * For <code>&lt; hardMinimalZoom</code> and <code>&gt; hardMaximalZoom</code> the children are not painted.<br>
 * For <code>&lt; softMinimalZoom</code> and <code>&gt; sortMaximalZoom</code> the children are partially painted using alpha-blending.<br>
 * Between <code>softMinimalZoom</code> and <code>softMaximalZoom</code> the children are painted normally.
 *
 * @author David Kaspar
 */
public class LevelOfDetailsWidget extends Widget {

    private double hardMinimalZoom;
    private double softMinimalZoom;
    private double softMaximalZoom;
    private double hardMaximalZoom;

    /**
     * Creates a level-of-details widget.
     * @param scene the scene
     * @param hardMinimalZoom the hard minimal zoom factor
     * @param softMinimalZoom the sort minimal zoom factor
     * @param softMaximalZoom the sort maximal zoom factor
     * @param hardMaximalZoom the hard maximal zoom factor
     */
    public LevelOfDetailsWidget(Scene scene, double hardMinimalZoom, double softMinimalZoom, double softMaximalZoom, double hardMaximalZoom) {
        super (scene);
        this.hardMinimalZoom = hardMinimalZoom;
        this.softMinimalZoom = softMinimalZoom;
        this.softMaximalZoom = softMaximalZoom;
        this.hardMaximalZoom = hardMaximalZoom;
    }

    /**
     * Paints children based on the zoom factor.
     */
    public void paintChildren () {
        double zoom = getScene ().getZoomFactor();
        if (zoom <= hardMinimalZoom  ||  zoom >= hardMaximalZoom)
            return;

        Graphics2D gr = getGraphics();
        Composite previousComposite = null;
        if (hardMinimalZoom < zoom  &&  zoom < softMinimalZoom) {
            double diff = softMinimalZoom - hardMinimalZoom;
            if (diff > 0.0) {
                diff = (zoom - hardMinimalZoom) / diff;
                previousComposite = gr.getComposite();
                gr.setComposite(AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) diff));
            }
        } else if (softMaximalZoom < zoom  &&  zoom < hardMaximalZoom) {
            double diff = hardMaximalZoom - softMaximalZoom;
            if (diff > 0.0) {
                diff = (hardMaximalZoom - zoom) / diff;
                previousComposite = gr.getComposite();
                gr.setComposite(AlphaComposite.getInstance (AlphaComposite.SRC_OVER, (float) diff));
            }
        }

        super.paintChildren ();

        if (previousComposite != null)
            gr.setComposite(previousComposite);
    }

    /**
     * Checks whether a specified local location is a part of a widget based on the zoom factor.
     * @param localLocation the local location
     * @return true, it it is
     */
    public boolean isHitAt(Point localLocation) {
        double zoom = getScene().getZoomFactor();
        if (zoom < hardMinimalZoom  ||  zoom > hardMaximalZoom)
            return false;
        return super.isHitAt(localLocation);
    }
    
}
