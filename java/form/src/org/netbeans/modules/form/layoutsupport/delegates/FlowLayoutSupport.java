/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import org.netbeans.modules.form.layoutsupport.*;

/**
 * Support class for FlowLayout. This is an example of very simple layout
 * with no constraints; just basic drag & drop is implemented.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class FlowLayoutSupport extends AbstractLayoutSupport
{
    /** Gets the supported layout manager class - FlowLayout.
     * @return the class supported by this delegate
     */
    @Override
    public Class getSupportedClass() {
        return FlowLayout.class;
    }

    /** This method calculates position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged; not needed here
     * @param index position (index) of the component in its current container;
     *        not needed here
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component;
     *        not needed here
     * @return index corresponding to the position of the component in the
     *         container
     */
    @Override
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof FlowLayout))
            return -1;

        int vgap = ((FlowLayout) containerDelegate.getLayout()).getVgap();
        Component[] components = containerDelegate.getComponents();
        int[] rowStarts = new int[components.length + 1];
        int[] rowTops = new int[components.length + 1];
        for (int i = 0; i < rowStarts.length; i++) {
            rowStarts[i] = -1;
        }

        // rowStarts keeps indices of the first components on each row
        // rowTops keeps y-position of each row
        
        int lastX = Integer.MAX_VALUE;
        int rowHeight = - vgap;
        int r = 0;
        int i = 0;

        int compIndex = -1;
        assistantParams = 0;
        if ((components.length > 1) || (component.getParent() != containerDelegate)) {
            for (int j = 0; j < components.length; j++) {
                Component comp = components[j];
                if (comp == component) {
                    compIndex = j;
                    comp = components[(j == 0) ? 1 : j-1];
                }
                int posX = comp.getBounds().x;
                if (posX < lastX) {
                    rowStarts[r] = j;
                    rowTops[r] = rowHeight + vgap;
                    rowTops[r] += r > 0 ? rowTops[r-1] :
                                          containerDelegate.getInsets().top;
                    r++;
                    rowHeight = 0;
                }
                rowHeight = Math.max(rowHeight, comp.getSize().height);
                lastX = posX;
            }
            if (r > 0) {
                rowTops[r] = rowTops[r-1] + rowHeight + vgap;
            }

            // find which row the pointer falls in

            r = 0;
            while (rowStarts[i] >= 0) {
                if (posInCont.y < rowTops[i]) {
                    r = i - 1;
                    break;
                }
                i++;
            }

            if (rowStarts[i] < 0) {
                if (posInCont.y >= rowTops[i]) {
                    if (component.getParent() == containerDelegate) assistantParams--;
                    assistantParams += components.length;
                    return components.length;
                }
                else {
                    r = i - 1;
                }
            }

            int m = (r <= 0) ? 0 : rowStarts[r];
            if ((compIndex > -1) && (compIndex < m)) assistantParams--;
            int n = rowStarts[r + 1];

            if (n > components.length || n < 0)
                n = components.length;

            for (i = m; i < n; i++) {
                Component comp = components[i];
                if (comp == component) {
                    assistantParams--;
                    comp = components[(i == 0) ? 1 : i-1];
                }
                Rectangle bounds = comp.getBounds();
                int centerX = bounds.x + bounds.width / 2;
                if (posInCont.x < centerX)
                    break;
            }

            i = i < n ? i : n;
        }
        assistantParams += i;
        return i;
    }

    private int assistantParams;
    @Override
    public String getAssistantContext() {
        return "flowLayout"; // NOI18N
    }

    @Override
    public Object[] getAssistantParams() {
        return new Object[] {Integer.valueOf(assistantParams+1)};
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged, not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for FlowLayout
     * @param newIndex component's index position to be presented
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (true in this case)
     */
    @Override
    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(containerDelegate.getLayout() instanceof FlowLayout))
            return false;
        
        Component[] components = containerDelegate.getComponents();
        int alignment = ((FlowLayout) containerDelegate.getLayout()).getAlignment();
        int hgap = ((FlowLayout) containerDelegate.getLayout()).getHgap();
        int draggedIndex = -1;
        if (component.getParent() == containerDelegate) {
            for (int i=0; i<components.length; i++) {
                if (component == components[i]) {
                    draggedIndex = i;
                }
            }
        }

        int x = 0, y1 = 0, y2 = 0;

        if ((newIndex <= 0) || ((components.length == 1) && (draggedIndex != -1))) {
            if ((components.length == 0) || ((components.length == 1) && (draggedIndex != -1))) {
                if (alignment == FlowLayout.RIGHT) {
                    x = containerDelegate.getSize().width;
                }
                else if (alignment == FlowLayout.LEFT) {
                    x = 0;
                }
                else {
                    x = containerDelegate.getSize().width / 2 - 5;
                }
                y1 = 0;
                y2 = component.getHeight();
            }
            else {
                Rectangle b = components[(draggedIndex == 0) ? 1 : 0].getBounds();
                x = b.x;
                y1 = b.y;
                y2 = b.y + component.getHeight();
            }
        }
        else if ((newIndex >= components.length) ||
            ((newIndex == components.length - 1) && (newIndex == draggedIndex))) {
            int last = components.length - 1;
            Rectangle b = components[(last == draggedIndex) ? last-1 : last].getBounds();
            x = b.x + b.width;
            y1 = b.y;
            y2 = b.y + component.getHeight();
        }
        else {
            Rectangle b = components[(newIndex == draggedIndex) ? newIndex+1 : newIndex].getBounds();
            x = b.x;
            y1 = b.y;
            y2 = b.y + component.getHeight();
        }
        g.drawRect(x - 10 - hgap / 2, y1, 20, y2 - y1);
        return true;
    }
}
