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
 * This is a separator widget. Renders a rectangle that is usually expand across the width or height of the parent widget
 * based on an orientation.
 *
 * @author David Kaspar
 */
public class SeparatorWidget extends Widget {

    /**
     * The separator orientation
     */
    public static enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private Orientation orientation;
    private int thickness;

    /**
     * Creates a separator widget.
     * @param scene the scene
     * @param orientation the separator orientation
     */
    public SeparatorWidget (Scene scene, Orientation orientation) {
        super (scene);
        assert orientation != null;
        this.orientation = orientation;
        thickness = 1;
    }

    /**
     * Returns a separator orientation
     * @return the separator orientation
     */
    public Orientation getOrientation () {
        return orientation;
    }

    /**
     * Sets a separator orientation
     * @param orientation the separator orientation
     */
    public void setOrientation (Orientation orientation) {
        assert orientation != null;
        this.orientation = orientation;
        revalidate();
    }

    /**
     * Returns a thickness of the separator.
     * @return the thickness
     */
    public int getThickness () {
        return thickness;
    }

    /**
     * Sets a thickness of the seperator.
     * @param thickness the thickness
     */
    public void setThickness (int thickness) {
        assert thickness >= 0;
        this.thickness = thickness;
        revalidate();
    }

    /**
     * Calculates a client area of the separator widget.
     * @return the calculated client area
     */
    protected Rectangle calculateClientArea () {
        if (orientation == Orientation.HORIZONTAL)
            return new Rectangle (0, 0, 0, thickness);
        else
            return new Rectangle (0, 0, thickness, 0);
    }

    /**
     * Paints the separator widget.
     */
    protected void paintWidget() {
        Graphics2D gr = getGraphics();
        gr.setColor (getForeground());
        Rectangle bounds = getBounds ();
        Insets insets = getBorder ().getInsets ();
        if (orientation == Orientation.HORIZONTAL)
            gr.fillRect (0, 0, bounds.width - insets.left - insets.right, thickness);
        else
            gr.fillRect (0, 0, thickness, bounds.height - insets.top - insets.bottom);
    }
    
}
