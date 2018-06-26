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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.web.jsf.navigation.graph;

import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.vmd.*;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import java.awt.*;
import org.netbeans.api.visual.anchor.PointShapeFactory;

/**
 * @author David Kaspar
 */
public class PFENotModifiableScheme extends VMDColorScheme {

    private static final Color COLOR60_SELECT = new Color( 210,210,210);
    private static final Color COLOR60_HOVER = new Color( 200, 200, 200);
    private static final Color COLOR60_HOVER_BACKGROUND = new Color (0xB0C3E1);
    private static final Color COLOR_NORMAL =  new Color(230,230,230);
    private static final Color COLOR_HIGHLIGHTED = new Color (0x316AC5);
    private static final PointShape POINT_SHAPE_IMAGE = PointShapeFactory.createImagePointShape (ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-pin.png")); // NOI18N

    private static final Border BORDER_PIN = BorderFactory.createOpaqueBorder (2, 8, 2, 8);
    private static final Border BORDER60_PIN_SELECT = BorderFactory.createCompositeBorder (BorderFactory.createLineBorder (0, 1, 0, 1, COLOR60_SELECT), BorderFactory.createLineBorder (2, 7, 2, 7, COLOR60_SELECT));

    public void installUI (VMDNodeWidget widget) {
    }

    public void updateUI (VMDNodeWidget widget, ObjectState previousState, ObjectState state) {

    }

    public void installUI (VMDConnectionWidget widget) {
        widget.setSourceAnchorShape (AnchorShape.NONE);
        widget.setTargetAnchorShape (AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints (true);
    }

 
    public void updateUI (VMDConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isSelected ()) {
            widget.setForeground (COLOR60_SELECT);
        } else if (state.isHighlighted ()) {
            widget.setForeground (COLOR_HIGHLIGHTED);
        } else if (state.isHovered ()  ||  state.isFocused ()) {
            widget.setForeground (COLOR60_HOVER);
        } else {
            widget.setForeground (COLOR_NORMAL);
        }

        if (state.isSelected ()  ||  state.isHovered ()) {
            widget.setControlPointShape (PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape (PointShape.SQUARE_FILLED_BIG);
            widget.setControlPointCutDistance (0);
        } else {
            widget.setControlPointShape (PointShape.NONE);
            widget.setEndPointShape (POINT_SHAPE_IMAGE);
            widget.setControlPointCutDistance (5);
        }
    }

    public void installUI (VMDPinWidget widget) {
        widget.setBorder (BORDER_PIN);
        widget.setBackground (COLOR60_HOVER_BACKGROUND);
    }

    public void updateUI (VMDPinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque (state.isHovered ()  ||  state.isFocused ());
        if (state.isSelected ()) {
            widget.setBorder (BORDER60_PIN_SELECT);
        } else {
            widget.setBorder (BORDER_PIN);
        }
    }

    public int getNodeAnchorGap (VMDNodeAnchor anchor) {
        return 4;
    }

    public boolean isNodeMinimizeButtonOnRight (VMDNodeWidget widget) {
        return true;
    }

    public Image getMinimizeWidgetImage (VMDNodeWidget widget) {
        return widget.isMinimized ()
                ? ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-expand-60.png") // NOI18N
                : ImageUtilities.loadImage ("org/netbeans/modules/visual/resources/vmd-collapse-60.png"); // NOI18N
    }

    public Widget createPinCategoryWidget (VMDNodeWidget widget, String categoryDisplayName) {
        return VMDFactory.getOriginalScheme ().createPinCategoryWidget (widget, categoryDisplayName);
    }



}
