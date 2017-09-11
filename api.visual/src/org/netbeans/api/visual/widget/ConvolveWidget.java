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

import org.netbeans.api.visual.border.BorderFactory;

import java.awt.image.ConvolveOp;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.awt.*;

/**
 * The widget which applies a convolve filter to a graphics rendered by the children.
 * <p>
 * Children are painted to an offscreen buffer which is later painted with a convolve filter applied to it.
 * <p>
 * Because of the offscreen buffer, be careful about the size of the widget. The buffer stays allocated
 * even after the painting and it is also expanding only (when required). You can clear the buffer using clearCache method.
 *
 * @author David Kaspar
 */
public class ConvolveWidget extends Widget {

    private static final Color TRANSPARENT = new Color (0, 0, 0, 0);

    private ConvolveOp convolveOp;
    private BufferedImage image;
    private Graphics2D imageGraphics;

    /**
     * Creates a convolve widget with a specified ColvolveOp.
     * @param scene the scene
     * @param convolveOp the convolve operation
     */
    public ConvolveWidget (Scene scene, ConvolveOp convolveOp) {
        super (scene);
        this.convolveOp = convolveOp;
        Kernel kernel = convolveOp.getKernel ();
        setBorder (BorderFactory.createEmptyBorder (kernel.getWidth (), kernel.getHeight ()));
    }

    /**
     * Returns a convolve operation.
     * @return the convolve operation
     */
    public ConvolveOp getConvolveOp () {
        return convolveOp;
    }

    /**
     * Sets a convolve operation.
     * @param convolveOp the convolve operation
     */
    public void setConvolveOp (ConvolveOp convolveOp) {
        this.convolveOp = convolveOp;
        repaint ();
    }

    /**
     * Clears an offscreen buffer.
     */
    public void clearCache () {
        if (imageGraphics != null)
            imageGraphics.dispose ();
        image = null;

    }

    /**
     * Paints the children into the offscreen buffer and then the buffer is rendered regularly using the convolve operation.
     */
    protected void paintChildren () {
        Rectangle bounds = getBounds ();
        if (image == null  ||  image.getWidth () < bounds.width  ||  image.getHeight () < bounds.height) {
            clearCache ();
            image = new BufferedImage (bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);
            imageGraphics = image.createGraphics ();
        }

        Graphics2D previousGraphics = getScene ().getGraphics ();
        imageGraphics.translate (- bounds.x, - bounds.y);
        imageGraphics.setBackground (TRANSPARENT);
        imageGraphics.clearRect (bounds.x, bounds.y, bounds.width, bounds.height);

        getScene ().setGraphics (imageGraphics);
        super.paintChildren ();
        getScene ().setGraphics (previousGraphics);

        imageGraphics.translate (bounds.x, bounds.y);

        getGraphics ().drawImage (image, convolveOp, bounds.x, bounds.y);
    }

}
