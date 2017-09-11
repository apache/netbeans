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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADVisualComponent;
import org.netbeans.modules.form.RADVisualContainer;

/**
 * VisualMapper implementation for layout tests. Works based on explicitly set
 * bounds and baseline positions.
 */

public class FakeLayoutMapper implements VisualMapper {

    private FormModel fm = null;
    private HashMap contInterior = null;
    private HashMap baselinePosition = null;
    private HashMap prefPaddingInParent = null;
    private HashMap prefPadding = null;
    private HashMap compBounds = null;
    private HashMap compMinSize = null;
    private HashMap compPrefSize = null;
    private HashMap hasExplicitPrefSize = null;
    
    public FakeLayoutMapper(FormModel fm, 
                            HashMap contInterior, 
                            HashMap baselinePosition, 
                            HashMap prefPaddingInParent,
                            HashMap compBounds,
                            HashMap compMinSize,
                            HashMap compPrefSize,
                            HashMap hasExplicitPrefSize,
                            HashMap prefPadding) {
        this.fm = fm;
        this.contInterior = contInterior;
        this.baselinePosition = baselinePosition;
        this.prefPaddingInParent = prefPaddingInParent;
        this.compBounds = compBounds;
        this.compMinSize = compMinSize;
        this.compPrefSize = compPrefSize;
        this.hasExplicitPrefSize = hasExplicitPrefSize;
        this.prefPadding = prefPadding;
    }
    
    // -------

    @Override
    public Rectangle getComponentBounds(String componentId) {
        Rectangle r = (Rectangle) compBounds.get(componentId);
        if (r == null && "Form".equals(componentId)) { // NOI18N
            return getContainerInterior(componentId);
        }
        return r;
    }

    @Override
    public Rectangle getContainerInterior(String componentId) {
        return (Rectangle) contInterior.get(componentId);
    }

    @Override
    public Dimension getComponentMinimumSize(String componentId) {
        Dimension d = (Dimension) compMinSize.get(componentId);
        if (d == null) {
            d = getComponentPreferredSize(componentId);
        }
        return d;
    }

    @Override
    public Dimension getComponentPreferredSize(String componentId) {
        Dimension d = (Dimension) compPrefSize.get(componentId);
        if (d == null && "Form".equals(componentId)) {
            Rectangle r = getContainerInterior(componentId);
            d = new Dimension(r.width, r.height);
        }
        return d;
    }

    @Override
    public boolean hasExplicitPreferredSize(String componentId) {
        return ((Boolean) hasExplicitPrefSize.get(componentId)).booleanValue();
    }

    @Override
    public int getBaselinePosition(String componentId, int width, int height) {
        String id = componentId + "-" + width + "-" + height; //NOI18N
        return ((Integer) baselinePosition.get(id)).intValue();
    }

    @Override
    public int getPreferredPadding(String comp1Id,
                                   String comp2Id,
                                   int dimension,
                                   int comp2Alignment,
                                   PaddingType paddingType)
    {
        String id = comp1Id + "-" + comp2Id  + "-" + dimension + "-" + comp2Alignment + "-" // NOI18N
                    + (paddingType != null ? paddingType.ordinal() : 0);
        Integer pad = (Integer) prefPadding.get(id);
        return pad != null ? pad.intValue() : 6;
    }

    @Override
    public int getPreferredPaddingInParent(String parentId,
                                           String compId,
                                           int dimension,
                                           int compAlignment)
    {
        String id = parentId + "-" + compId + "-" + dimension + "-" + compAlignment; //NOI18N
        Integer pad = (Integer) prefPaddingInParent.get(id);
        return pad != null ? pad.intValue() : 10;
    }

    @Override
    public boolean[] getComponentResizability(String compId, boolean[] resizability) {
        resizability[0] = resizability[1] = true;
        return resizability;
    }

    @Override
    public void rebuildLayout(String contId) {
    }

    @Override
    public void setComponentVisibility(String componentId, boolean visible) {
    }

    @Override
    public void repaintDesigner(String forComponentId) {
    }

    @Override
    public Shape getComponentVisibilityClip(String componentId) {
        return null;
    }
    
    @Override
    public String[] getIndirectSubComponents(String compId) {
        RADComponent metacomp = fm.getMetaComponent(compId);
        if (metacomp instanceof RADVisualContainer) {
            List<String> l = collectRootLayoutSubComponents((RADVisualContainer)metacomp, null);
            if (l != null) {
                return l.toArray(new String[l.size()]);
            }
        }
        return null;
    }

    private static List<String> collectRootLayoutSubComponents(RADVisualContainer metacont, List<String> list) {
        for (RADVisualComponent sub : metacont.getSubComponents()) {
            if (sub instanceof RADVisualContainer) {
                RADVisualContainer subcont = (RADVisualContainer) sub;
                if (subcont.getLayoutSupport() == null) {
                    if (list == null) {
                        list = new ArrayList<String>();
                    }
                    list.add(subcont.getId());
                } else {
                    list = collectRootLayoutSubComponents(subcont, list);
                }
            }
        }
        return list;
    }
}
