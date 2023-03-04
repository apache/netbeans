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

package org.netbeans.modules.java.graph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import javax.swing.JComponent;
import org.netbeans.api.visual.animator.Animator;
import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.widget.Scene;

/**
 *
 * @author Dafe Simonek
 */
class CenteredZoomAnimator extends Animator {

    private double sourceZoom;
    private double targetZoom;
    private Point center;

    CenteredZoomAnimator(SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    /** Starts animation to target zoomFactor with given point (in scene coordinates)
     * to keep in center.
     *
     * @param zoomFactor target zoom factor
     * @param center center point, in scene coordinates
     */
    public void setZoomFactor (double zoomFactor, Point center) {
        sourceZoom = getScene ().getZoomFactor ();
        targetZoom = zoomFactor;
        this.center = center;
        start ();
    }

    public double getTargetZoom () {
        return targetZoom;
    }

    public Point getCenter () {
        return center;
    }

    @Override public void tick(double progress) {
        double nextZoom = progress >= 1.0 ? targetZoom :
            (sourceZoom + progress * (targetZoom - sourceZoom));

        Scene scene = getScene();
        JComponent view = scene.getView ();

        if (view != null) {
            Point viewLocation = view.getVisibleRect ().getLocation();
            Dimension viewSize = view.getVisibleRect ().getSize();
            Point oldCenter = scene.convertSceneToView (center);

            ((DependencyGraphScene)scene).setMyZoomFactor (nextZoom);
            scene.validate (); // HINT - forcing to change preferred size of the JComponent view

            Point newCenter = scene.convertSceneToView (center);
            Rectangle viewBounds = view.getVisibleRect();
            Point visibleCenter = new Point((int)viewBounds.getCenterX(), (int)viewBounds.getCenterY());
            newCenter.x += Math.round((newCenter.x - visibleCenter.x) * progress);
            newCenter.y += Math.round((newCenter.y - visibleCenter.y) * progress);

            view.scrollRectToVisible (new Rectangle (
                    newCenter.x - oldCenter.x + viewLocation.x,
                    newCenter.y - oldCenter.y + viewLocation.y,
                    viewSize.width,
                    viewSize.height
            ));
        } else {
            ((DependencyGraphScene)scene).setMyZoomFactor (nextZoom);
        }
    }

}
