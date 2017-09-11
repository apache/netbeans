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
package org.netbeans.modules.visual.border;

import org.netbeans.api.visual.border.Border;

import java.awt.*;

/**
 * @author David Kaspar
 */
public final class ImageBorder implements Border {

    private Insets borderInsets;
    private Insets imageInsets;
    private Image image;
    private int width, height;
    private int verStep, horStep;
    private int verEdge, horEdge;

    public ImageBorder (Insets borderInsets, Insets imageInsets, Image image) {
        this.borderInsets = borderInsets;
        this.imageInsets = imageInsets;
        this.image = image;
        width = image.getWidth (null);
        height = image.getHeight (null);
        horEdge = width - this.imageInsets.right;
        verEdge = height - this.imageInsets.bottom;
        horStep = horEdge - this.imageInsets.left;
        verStep = verEdge - this.imageInsets.top;
    }

    public Insets getInsets () {
        return borderInsets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        int destVerMax = bounds.y + bounds.height;
        int destHorMax = bounds.x + bounds.width;
        int destVerEdge = destVerMax - imageInsets.bottom;
        int destHorEdge = destHorMax - imageInsets.right;

        int horInner = bounds.width - imageInsets.left - imageInsets.right;
        int xdiv = horInner / horStep;
        int xmod = horInner % horStep;

        gr.drawImage (image, bounds.x, bounds.y, bounds.x + xmod + imageInsets.left, bounds.y + imageInsets.top, 0, 0, xmod + imageInsets.left, imageInsets.top, null);
        gr.drawImage (image, destHorEdge - xmod, destVerEdge, destHorMax, destVerMax, horEdge - xmod, verEdge, width, height, null);

        for (int i = 0, x = bounds.x + xmod + imageInsets.left; i < xdiv; i ++, x += horStep) {
            gr.drawImage (image, x, bounds.y, x + horStep, bounds.y + imageInsets.top, imageInsets.left, 0, horEdge, imageInsets.top, null);
            gr.drawImage (image, x - xmod, destVerEdge, x - xmod + horStep, destVerMax, imageInsets.left, verEdge, horEdge, height, null);
        }

        int verInner = bounds.height - imageInsets.top - imageInsets.bottom;
        int ydiv = verInner / verStep;
        int ymod = verInner % verStep;

        gr.drawImage (image, destHorEdge, bounds.y, destHorMax, bounds.y + ymod + imageInsets.top, horEdge, 0, width, ymod + imageInsets.top, null);
        gr.drawImage (image, bounds.x, destVerEdge - ymod, bounds.x + imageInsets.left, destVerMax, 0, verEdge - ymod, imageInsets.left, height, null);

        for (int i = 0, y = bounds.y + ymod + imageInsets.top; i < ydiv; i ++, y += verStep) {
            gr.drawImage (image, destHorEdge, y, destHorMax, y + verStep, horEdge, imageInsets.top, width, verEdge, null);
            gr.drawImage (image, bounds.x, y - ymod, bounds.x + imageInsets.left, y - ymod + verStep, 0, imageInsets.top, imageInsets.left, verEdge, null);
        }
    }

    public boolean isOpaque () {
        return false;
    }

}
