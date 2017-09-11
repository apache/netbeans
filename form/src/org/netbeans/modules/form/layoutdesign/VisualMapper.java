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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;

public interface VisualMapper extends LayoutConstants  {

//    String getTopComponentId();

    /**
     * Provides actual bounds (position and size) of a component - as it appears
     * in the visual design area. The position should be in coordinates of the
     * whole design visualization.
     * @param componentId
     * @return actual bounds of given component, null if the component is not
     *         currently visualized in the design area
     */
    Rectangle getComponentBounds(String componentId);
    
    /**
     * Provides actual position and size of the interior of a component
     * container - as it appears in the visual design area. (The interior
     * differs from the outer bounds in that it should reflect the borders
     * or other insets, and also should reflect full layout size of the
     * container even if it is clipped in the outer layout). The positions
     * should be in coordinates of the whole design visualization.
     * @param componentId
     * @return actual interior of given component, null if the component is not
     *         currently visualized in the design area
     */
    Rectangle getContainerInterior(String componentId);

    Dimension getComponentMinimumSize(String componentId);
    Dimension getComponentPreferredSize(String componentId);

    boolean hasExplicitPreferredSize(String componentId);

    /**
     * Provides preferred padding (optimal amount of space) between two components.
     * @param component1Id first component Id
     * @param component2Id second component Id
     * @param dimension the dimension (HORIZONTAL or VERTICAL) in which the
     *        components are positioned
     * @param comp2Alignment the edge (LEADING or TRAILING) at which the second
     *        component is placed next to the first component
     * @param paddingType padding type (RELATED, UNRELATED, SEPARATE or INDENT)
     * @return preferred padding (amount of space) between the given components
     */
    int getPreferredPadding(String component1Id,
                            String component2Id,
                            int dimension,
                            int comp2Alignment,
                            PaddingType paddingType);

    /**
     * Provides preferred padding (optimal amount of space) between a component
     * and its parent's border.
     * @param parentId Id of the parent container
     * @param componentId Id of the component
     * @param dimension the dimension (HORIZONTAL or VERTICAL) in which the
     *        component is positioned
     * @param compALignment the edge (LEADING or TRAILING) of the component
     *        which should be placed next to the parent's border
     * @return preferred padding (amount of space) between the component and its
     *         parent's border
     */
    int getPreferredPaddingInParent(String parentId,
                                    String componentId,
                                    int dimension,
                                    int compAlignment);

    int getBaselinePosition(String componentId, int width, int height);

    boolean[] getComponentResizability(String compId, boolean[] resizability);

    /**
     * Rebuilds the layout of given container. Called if LayoutDesigner needs
     * immediate update of the layout according to the model.
     */
    void rebuildLayout(String containerId);

    void setComponentVisibility(String componentId, boolean visible);

    /**
     * Repaints the entire designer. Used when the LayoutDesigner needs to do
     * repaint not directly initiated by the user.
     * @param forComponentId The id of the component that requires refresh. The
     *        method does not have to do anything if this component is actually
     *        not in the design view.
     */
    void repaintDesigner(String forComponentId);

    Shape getComponentVisibilityClip(String componentId);

    /**
     * In case of combined layout models hierarchy this method provides the
     * sub-components indirectly contained under given component. E.g. tabbed pane.
     * @param compId The component to investigate whether it contains some
            *        sub-components indirectly (in another layout model).
     * @return String array of Ids of sub-components or null
     */
    String[] getIndirectSubComponents(String compId);
}
