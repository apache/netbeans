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
package org.netbeans.api.visual.layout;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.graph.GraphPinScene;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.modules.visual.layout.*;
import org.netbeans.modules.visual.util.GeomUtil;

/**
 * This class is a factory of all built-in layouts.
 *
 * @author David Kaspar
 */
public final class LayoutFactory {

    private static final AbsoluteLayout LAYOUT_ABSOLUTE = new AbsoluteLayout ();
    private static final OverlayLayout LAYOUT_OVERLAY = new OverlayLayout ();

    /**
     * Alignment of children widgets within a calculated widget used by FlowLayout (vertical and horizontal flow layout).
     */
    public static enum SerialAlignment {

        LEFT_TOP, CENTER, RIGHT_BOTTOM, JUSTIFY
    }

    /**
     * Alignment of children widgets within a calculated connection widgets used by default layout used in a connection widget.
     */
    public enum ConnectionWidgetLayoutAlignment {

        NONE, 
        CENTER, 
        TOP_CENTER, 
        BOTTOM_CENTER, 
        CENTER_LEFT, 
        CENTER_RIGHT, 
        TOP_LEFT, 
        TOP_RIGHT, 
        BOTTOM_LEFT, 
        BOTTOM_RIGHT, 
        CENTER_SOURCE, 
        CENTER_TARGET, 
        BOTTOM_SOURCE, 
        BOTTOM_TARGET, 
        TOP_SOURCE, 
        TOP_TARGET
    }

    private LayoutFactory () {
    }

    /**
     * Creates an absolute layout where widgets are located at placed defined by their preferredLocation.
     * The instance can be shared by multiple widgets.
     * @return the absolute layout
     */
    public static Layout createAbsoluteLayout () {
        return LAYOUT_ABSOLUTE;
    }

    /**
     * Creates a vertical flow layout with default style where widgets are placed vertically one to the bottom from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining height of the parent widget is split.
     * @return the vertical flow layout
     * @deprecated use createVerticalFlowLayout method instead
     */
    public static Layout createVerticalLayout () {
        GeomUtil.LOG.warning ("LayoutFactory.createVerticalLayout() method is deprecated. Use LayoutFactory.createVerticalFlowLayout() method instead."); // NOI18N
        return createVerticalFlowLayout (null, 0);
    }

    /**
     * Creates a vertical flow layout with a specific style where widgets are placed vertically one to the bottom from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining height of the parent widget is split.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the vertical flow layout
     * @deprecated use createVerticalFlowLayout (alignment, gap) method instead
     */
    public static Layout createVerticalLayout (SerialAlignment alignment, int gap) {
        GeomUtil.LOG.warning ("LayoutFactory.createVerticalLayout(alignment,gap) method is deprecated. Use LayoutFactory.createVerticalFlowLayout(alignment,gap) method instead."); // NOI18N
        return new FlowLayout (true, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    /**
     * Creates a vertical flow layout with default style where widgets are placed vertically one to the bottom from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining height of the parent widget is split.
     * @return the vertical flow layout
     */
    public static Layout createVerticalFlowLayout () {
        return createVerticalFlowLayout (null, 0);
    }

    /**
     * Creates a vertical flow layout with a specific style where widgets are placed vertically one to the bottom from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining height of the parent widget is split.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the vertical flow layout
     */
    public static Layout createVerticalFlowLayout (SerialAlignment alignment, int gap) {
        return new FlowLayout (true, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    /**
     * Creates a horizontal flow layout with default style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining width of the parent widget is split.
     * @return the horizontal flow layout
     * @deprecated use createHorizontalFlowLayout method instead
     */
    public static Layout createHorizontalLayout () {
        GeomUtil.LOG.warning ("LayoutFactory.createHorizontalLayout() method is deprecated. Use LayoutFactory.createHorizontalFlowLayout() method instead."); // NOI18N
        return createHorizontalFlowLayout (null, 0);
    }

    /**
     * Creates a horizontal flow layout with a specific style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining width of the parent widget is split.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the horizontal flow layout
     * @deprecated use createHorizontalFlowLayout (alignment, gap) method instead
     */
    public static Layout createHorizontalLayout (SerialAlignment alignment, int gap) {
        GeomUtil.LOG.warning ("LayoutFactory.createHorizontalLayout(alignment,gap) method is deprecated. Use LayoutFactory.createHorizontalFlowLayout(alignment,gap) method instead."); // NOI18N
        return new FlowLayout (false, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }
    
    /**
     * Creates a horizontal flow layout with default style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining width of the parent widget is split.
     * @return the horizontal flow layout
     */
    public static Layout createHorizontalFlowLayout () {
        return createHorizontalFlowLayout (null, 0);
    }

    /**
     * Creates a horizontal flow layout with a specific style where widgets are placed horizontally one to the right from another.
     * The instance can be shared by multiple widgets.
     * If child widget constraint is an Number value,
     * then its integer value is takes as a weight in which the remaining width of the parent widget is split.
     * @param alignment the alignment
     * @param gap the gap between widgets
     * @return the horizontal flow layout
     */
    public static Layout createHorizontalFlowLayout (SerialAlignment alignment, int gap) {
        return new FlowLayout (false, alignment != null ? alignment : SerialAlignment.JUSTIFY, gap);
    }

    /**
     * Creates a card layout where all children widgets except the active one are hidden. The active one is the only shown.
     * The active widget could be managed using LayoutFactory.getActiveCard and LayoutFactory.setActiveCard methods.
     * The instance cannot be shared.
     * @param cardLayoutWidget the widget where the card layout is going to be used
     * @return the card layout
     */
    public static Layout createCardLayout (Widget cardLayoutWidget) {
        assert cardLayoutWidget != null;
        return new CardLayout (cardLayoutWidget);
    }

    /**
     * Returns active card of a specified widget where a card layout is used.
     * @param cardLayoutWidget the widget with card layout
     * @return the active widget
     */
    public static Widget getActiveCard (Widget cardLayoutWidget) {
        Layout layout = cardLayoutWidget.getLayout ();
        return layout instanceof CardLayout ? ((CardLayout) layout).getActiveChildWidget () : null;
    }

    /**
     * Sets active card of a specified widget where a card layout is used.
     * @param widget the widget with card layout
     * @param activeChildWidget the new active widget
     */
    public static void setActiveCard (Widget widget, Widget activeChildWidget) {
        Layout layout = widget.getLayout ();
        if (layout instanceof CardLayout)
            ((CardLayout) layout).setActiveChildWidget (activeChildWidget);
    }

    /**
     * Returns a fill layout where all children widgets has the boundary at the biggest one of them or
     * they are expanded to the parent widget boundaries during justification.
     * The instance can be shared by multiple widgets.
     * @return the fill layout
     * @deprecated use createOverlayLayout method instead
     */
    public static Layout createFillLayout () {
        GeomUtil.LOG.warning ("LayoutFactory.createFillLayout() method is deprecated. Use LayoutFactory.createOverlayLayout() method instead."); // NOI18N
        return createOverlayLayout ();
    }

    /**
     * Returns a overlay layout where all children widgets has the boundary at the biggest one of them or
     * they are expanded to the parent widget boundaries during justification.
     * The instance can be shared by multiple widgets.
     * @return the overlay layout
     */
    public static Layout createOverlayLayout () {
        return LAYOUT_OVERLAY;
    }

    /**
     * Returns a scene layout which performs one-time layout using specified devolve-layout.
     * The instance cannot be shared.
     * @param widget the
     * @param devolveLayout the layout that is going to be used for one-time layout
     * @param animate if true, then setting preferredLocation is gone animated
     * @return the scene layout
     */
    public static SceneLayout createDevolveWidgetLayout (Widget widget, Layout devolveLayout, boolean animate) {
        return new DevolveWidgetLayout (widget, devolveLayout, animate);
    }

    /**
     * Creates a scene layout which performs a specified graph-oriented layout on a specified GraphScene.
     * @param graphScene the graph scene
     * @param graphLayout the graph layout
     * @return the scene layout
     */
    public static <N,E> SceneLayout createSceneGraphLayout (final GraphScene<N,E> graphScene, final GraphLayout<N,E> graphLayout) {
        assert graphScene != null  &&  graphLayout != null;
        return new SceneLayout(graphScene) {
            protected void performLayout () {
                graphLayout.layoutGraph (graphScene);
            }
        };
    }

    /**
     * Creates a scene layout which performs a specified graph-oriented layout on a specified GraphPinScene.
     * @param graphPinScene the graph pin scene
     * @param graphLayout the graph layout
     * @return the scene layout
     */
    public static <N,E> SceneLayout createSceneGraphLayout (final GraphPinScene<N,E,?> graphPinScene, final GraphLayout<N,E> graphLayout) {
        assert graphPinScene != null && graphLayout != null;
        return new SceneLayout(graphPinScene) {
            protected void performLayout () {
                graphLayout.layoutGraph (graphPinScene);
            }
        };
    }

}
