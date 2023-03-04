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

package org.netbeans.lib.profiler.ui.components;

import java.awt.*;


public class AnimationLayout implements LayoutManager {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private Dimension lockedSize;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a new <code>AnimationLayout</code>.
     */
    public AnimationLayout() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setLockedSize(Dimension lockedSize) {
        this.lockedSize = lockedSize;
    }

    /**
     * Adds the specified component to the layout. Not used by this class.
     * @param name the name of the component
     * @param comp the component to be added
     */
    public void addLayoutComponent(String name, Component comp) {
    }

    /**
     * Lays out the container. This method lets each component take
     * its preferred size by reshaping the components in the
     * target container in order to satisfy the alignment of
     * this <code>AnimationLayout</code> object.
     *
     * @param target the specified component being laid out
     * @see Container
     * @see java.awt.Container#doLayout
     */
    public void layoutContainer(Container target) {
        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();

            if (target.getComponentCount() > 0) {
                Component m = target.getComponent(0);

                if (m.isVisible()) {
                    Dimension d = lockedSize;

                    if (d == null) {
                        d = target.getSize();
                        d.width -= insets.left;
                        d.width -= insets.right;
                        d.height -= insets.top;
                        d.height -= insets.bottom;
                    }

                    m.setLocation(insets.left, insets.top);
                    m.setSize(d.width, d.height);
                }
            }
        }
    }

    /**
     * Returns the minimum dimensions needed to layout the <i>visible</i>
     * components contained in the specified target container.
     *
     * @param target the component which needs to be laid out
     * @return the minimum dimensions to lay out the
     *         subcomponents of the specified container
     * @see #preferredLayoutSize
     * @see java.awt.Container
     * @see java.awt.Container#doLayout
     */
    public Dimension minimumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);

            if (target.getComponentCount() > 0) {
                Component m = target.getComponent(0);

                if (m.isVisible()) {
                    dim = m.getMinimumSize();
                }
            }

            // actually this resizes the component instead of container - cannot be here      
            //    	Insets insets = target.getInsets();
            //    	dim.width += insets.left + insets.right;
            //    	dim.height += insets.top + insets.bottom;
            return dim;
        }
    }

    /**
     * Returns the preferred dimensions for this layout given the
     * <i>visible</i> components in the specified target container.
     *
     * @param target the component which needs to be laid out
     * @return the preferred dimensions to lay out the
     *         subcomponents of the specified container
     * @see Container
     * @see #minimumLayoutSize
     * @see java.awt.Container#getPreferredSize
     */
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);

            if (target.getComponentCount() > 0) {
                Component m = target.getComponent(0);

                if (m.isVisible()) {
                    dim = m.getPreferredSize();
                }
            }

            // actually this resizes the component instead of container - cannot be here
            //    	Insets insets = target.getInsets();
            //    	dim.width += insets.left + insets.right;
            //    	dim.height += insets.top + insets.bottom;
            //    	
            return dim;
        }
    }

    /**
     * Removes the specified component from the layout. Not used by
     * this class.
     * @param comp the component to remove
     * @see       java.awt.Container#removeAll
     */
    public void removeLayoutComponent(Component comp) {
    }

    /**
     * Returns a string representation of this <code>AnimationLayout</code>
     * object and its values.
     *
     * @return a string representation of this layout
     */
    public String toString() {
        return getClass().getName() + ", lockedSize: " + lockedSize; // NOI18N
    }
}
