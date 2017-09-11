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
package org.netbeans.api.visual.anchor;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.model.StateModel;
import org.netbeans.modules.visual.anchor.*;

import java.awt.*;

/**
 * This is a factory of all built-in anchor implementations. Anchors are designed to be shared by multiple instances of proxy anchors and connection widgets.
 * @author David Kaspar
 */
public final class AnchorFactory {

    private AnchorFactory () {
    }

    /**
     * Represents possible orthogonal directions used by directional anchor.
     */
    public enum DirectionalAnchorKind {
        HORIZONTAL, VERTICAL//, DIRECTION_4, DIRECTION_8
    }

    /**
     * Creates a anchor with fixed scene location.
     * @param location the scene location
     * @return the anchor
     */
    public static Anchor createFixedAnchor (Point location) {
        return new FixedAnchor (location);
    }

    /**
     * Creates a proxy anchor with delegates the computation one of specified anchors based on state in a model.
     * @param model the model with state
     * @param anchors the slave anchors
     * @return the anchor
     */
    public static Anchor createProxyAnchor (StateModel model, Anchor... anchors) {
        return model != null  &&  model.getMaxStates () == anchors.length ? new ProxyAnchor (model, anchors) : null;
    }

    /**
     * Creates an anchor with always computes a point in the center of specified widget.
     * @param widget the widget
     * @return the anchor
     */
    public static Anchor createCenterAnchor (Widget widget) {
        return widget != null ? new CenterAnchor (widget) : null;
    }

    /**
     * Creates an anchor which computes a point as the one on a circle around specified widget.
     * The point is the closest one to location of opposite anchor.
     * @param widget the widget
     * @param radius the radius of the circle
     * @return the anchor
     */
    public static Anchor createCircularAnchor (Widget widget, int radius) {
        return widget != null  &&  radius >= 0 ? new CircularAnchor (widget, radius) : null;
    }

    /**
     * Creates an anchor which computes a point as the one on the boundary of spacified widget.
     * The point is the closest one to location of opposite anchor.
     * @param widget the widget
     * @return the anchor
     */
    public static Anchor createRectangularAnchor (Widget widget) {
        return createRectangularAnchor (widget, true);
    }

    /**
     * Creates an anchor which computes a point as the one on the boundary of spacified widget.
     * The point is the closest one to location of opposite anchor.
     * @param widget the widget
     * @param includeBorders if true, then the boundary is widget bounds;
     *         if null then the boundary is widget client-area (bounds without borders)
     * @return the anchor
     */
    public static Anchor createRectangularAnchor (Widget widget, boolean includeBorders) {
        return widget != null ? new RectangularAnchor (widget, includeBorders) : null;
    }

    /**
     * Creates a directional anchor with computes a point as the one in the middle of the boundary side of specified widget.
     * The side is the closest one to the opposite anchor.
     * @param widget the widget
     * @param kind the kind of directional anchor
     * @return the anchor
     */
    public static Anchor createDirectionalAnchor (Widget widget, DirectionalAnchorKind kind) {
        return createDirectionalAnchor (widget, kind, 0);
    }

    /**
     * Creates a directional anchor with computes a point as the one in the middle of the boundary side of specified widget.
     * The side is the closest one to the opposite anchor.
     * @param widget the widget
     * @param kind the kind of directional anchor
     * @param gap the gap between the widget and the anchor location
     * @return the anchor
     */
    public static Anchor createDirectionalAnchor (Widget widget, DirectionalAnchorKind kind, int gap) {
        return widget != null && kind != null ? new DirectionalAnchor (widget, kind, gap) : null;
    }

    /**
     * Creates a free rectangular anchor. IT is similar to rectangular anchor but it is designed to be used together with FreeRouter.
     * @param widget the widget
     * @param includeBorders true if borders has to be included in the boundary
     * @return the anchor
     */
    public static Anchor createFreeRectangularAnchor (Widget widget, boolean includeBorders) {
        return widget != null ? new FreeRectangularAnchor (widget, includeBorders) : null;
    }
}
