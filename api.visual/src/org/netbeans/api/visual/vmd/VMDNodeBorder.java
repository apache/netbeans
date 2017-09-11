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
package org.netbeans.api.visual.vmd;

import org.netbeans.api.visual.border.Border;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * @author David Kaspar
 */
class VMDNodeBorder implements Border {

    private Color colorBorder;
    private Insets insets;
    private Stroke stroke;
    private Color color1;
    private Color color2;
    private Color color3;
    private Color color4;
    private Color color5;

    VMDNodeBorder (Color colorBorder, int thickness, Color color1, Color color2, Color color3, Color color4, Color color5) {
        this.colorBorder = colorBorder;
        this.insets = new Insets (thickness, thickness, thickness, thickness);
        this.stroke = new BasicStroke (thickness);
        this.color1 = color1;
        this.color2 = color2;
        this.color3 = color3;
        this.color4 = color4;
        this.color5 = color5;
    }

    public Insets getInsets () {
        return insets;
    }

    public void paint (Graphics2D gr, Rectangle bounds) {
        Shape previousClip = gr.getClip ();
        gr.clip (new RoundRectangle2D.Float (bounds.x, bounds.y, bounds.width, bounds.height, 4, 4));

        drawGradient (gr, bounds, color1, color2, 0f, 0.3f);
        drawGradient (gr, bounds, color2, color3, 0.3f, 0.764f);
        drawGradient (gr, bounds, color3, color4, 0.764f, 0.927f);
        drawGradient (gr, bounds, color4, color5, 0.927f, 1f);

        gr.setColor (colorBorder);
        Stroke previousStroke = gr.getStroke ();
        gr.setStroke (stroke);
        gr.draw (new RoundRectangle2D.Float (bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, 4, 4));
        gr.setStroke (previousStroke);

        gr.setClip (previousClip);
    }

    private void drawGradient (Graphics2D gr, Rectangle bounds, Color color1, Color color2, float y1, float y2) {
        y1 = bounds.y + y1 * bounds.height;
        y2 = bounds.y + y2 * bounds.height;
        gr.setPaint (new GradientPaint (bounds.x, y1, color1, bounds.x, y2, color2));
        gr.fill (new Rectangle.Float (bounds.x, y1, bounds.x + bounds.width, y2));
    }

    public boolean isOpaque () {
        return true;
    }

}
