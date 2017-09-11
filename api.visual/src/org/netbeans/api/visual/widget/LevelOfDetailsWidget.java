/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
