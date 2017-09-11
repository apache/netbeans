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
package org.netbeans.modules.visual.laf;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.laf.LookFeel;

import java.awt.*;
import javax.swing.UIManager;

/**
 * @author David Kaspar
 */
public class DefaultLookFeel extends LookFeel {

    private static final Color COLOR_SELECTED = new Color (0x447BCD);
    private static final Color COLOR_HIGHLIGHTED = COLOR_SELECTED.darker ();
    private static final Color COLOR_HOVERED = COLOR_SELECTED.brighter ();
    private static final int MARGIN = 3;
    private static final int ARC = 10;
    private static final int MINI_THICKNESS = 1;

    private static final Border BORDER_NORMAL = BorderFactory.createEmptyBorder (MARGIN, MARGIN);
    private static final Border BORDER_HOVERED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border BORDER_SELECTED = BorderFactory.createRoundedBorder (ARC, ARC, MARGIN, MARGIN, COLOR_SELECTED, COLOR_SELECTED.darker ());

    private static final Border MINI_BORDER_NORMAL = BorderFactory.createEmptyBorder (MINI_THICKNESS);
    private static final Border MINI_BORDER_HOVERED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_HOVERED, COLOR_HOVERED.darker ());
    private static final Border MINI_BORDER_SELECTED = BorderFactory.createRoundedBorder (MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, MINI_THICKNESS, COLOR_SELECTED, COLOR_SELECTED.darker ());

    public Paint getBackground () {
        return UIManager.getColor("Label.background");
        //return Color.WHITE;
    }

    public Color getForeground () {
        return UIManager.getColor("Label.foreground");
        //return Color.BLACK;
    }

    public Border getBorder (ObjectState state) {
        if (state.isHovered ())
            return BORDER_HOVERED;
        if (state.isSelected ())
            return BORDER_SELECTED;
        if (state.isFocused ())
            return BORDER_HOVERED;
        return BORDER_NORMAL;
    }

    public Border getMiniBorder (ObjectState state) {
        if (state.isHovered ())
            return MINI_BORDER_HOVERED;
        if (state.isSelected ())
            return MINI_BORDER_SELECTED;
        if (state.isFocused ())
            return MINI_BORDER_HOVERED;
        return MINI_BORDER_NORMAL;
    }

    public boolean getOpaque (ObjectState state) {
        return state.isHovered ()  ||  state.isSelected ();
    }

    public Color getLineColor (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return getForeground ();//Color.BLACK;
    }

    public Paint getBackground (ObjectState state) {
        if (state.isHovered ())
            return COLOR_HOVERED;
        if (state.isSelected ())
            return COLOR_SELECTED;
        if (state.isHighlighted ()  || state.isFocused ())
            return COLOR_HIGHLIGHTED;
        return getBackground ();//Color.WHITE;
    }

    public Color getForeground (ObjectState state) {
        return state.isSelected () ? (Color)getBackground () : getForeground ();//Color.WHITE : Color.BLACK;
    }

    public int getMargin () {
        return MARGIN;
    }

}
