/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
                return l.toArray(new String[0]);
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
