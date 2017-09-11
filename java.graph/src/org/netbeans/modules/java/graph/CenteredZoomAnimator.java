/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
