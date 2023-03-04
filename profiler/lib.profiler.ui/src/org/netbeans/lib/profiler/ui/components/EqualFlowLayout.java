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


/** EqualFlowLayout is a layout manager that works the same way as FlowLayout.
 * The only difference is that it sizes the components so that they all have the same width
 * (a width of widest component).
 *
 * @author   Ian Formanek
 * @version  1.00, Nov 12, 1998
 */
public class EqualFlowLayout extends FlowLayout {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -1996929627282401218L;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * Constructs a new Flow Layout with a centered alignment and a
     * default 5-unit horizontal and vertical gap.
     * @since JDK1.0
     */
    public EqualFlowLayout() {
        super();
    }

    /**
     * Constructs a new Flow Layout with the specified alignment and a
     * default 5-unit horizontal and vertical gap.
     * The value of the alignment argument must be one of
     * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
     * or <code>FlowLayout.CENTER</code>.
     * @param align the alignment value
     * @since JDK1.0
     */
    public EqualFlowLayout(int align) {
        super(align);
    }

    /**
     * Creates a new flow layout manager with the indicated alignment
     * and the indicated horizontal and vertical gaps.
     * <p>
     * The value of the alignment argument must be one of
     * <code>FlowLayout.LEFT</code>, <code>FlowLayout.RIGHT</code>,
     * or <code>FlowLayout.CENTER</code>.
     * @param      align   the alignment value.
     * @param      hgap    the horizontal gap between components.
     * @param      vgap    the vertical gap between components.
     * @since      JDK1.0
     */
    public EqualFlowLayout(int align, int hgap, int vgap) {
        super(align, hgap, vgap);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Lays out the container. This method lets each component take
     * its preferred size by reshaping the components in the
     * target container in order to satisfy the constraints of
     * this <code>FlowLayout</code> object.
     * @param target the specified component being laid out.
     * @see java.awt.Container
     * @see       java.awt.Container#doLayout
     * @since     JDK1.0
     */
    public void layoutContainer(Container target) {
        int maxWidth = getMaximumWidth(target);

        synchronized (target.getTreeLock()) {
            Insets insets = target.getInsets();
            int maxwidth = target.getSize().width - (insets.left + insets.right + (getHgap() * 2));
            int nmembers = target.getComponentCount();
            int x = 0;
            int y = insets.top + getVgap();
            int rowh = 0;
            int start = 0;

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    d.width = maxWidth;
                    m.setSize(d.width, d.height);

                    if ((x == 0) || ((x + d.width) <= maxwidth)) {
                        if (x > 0) {
                            x += getHgap();
                        }

                        x += d.width;
                        rowh = Math.max(rowh, d.height);
                    } else {
                        moveComponents2(target, insets.left + getHgap(), y, maxwidth - x, rowh, start, i);
                        x = d.width;
                        y += (getVgap() + rowh);
                        rowh = d.height;
                        start = i;
                    }
                }
            }

            moveComponents2(target, insets.left + getHgap(), y, maxwidth - x, rowh, start, nmembers);
        }
    }

    /**
     * Returns the minimum dimensions needed to layout the components
     * contained in the specified target container.
     * @param target the component which needs to be laid out
     * @return    the minimum dimensions to lay out the
     *                    subcomponents of the specified container.
     * @see #preferredLayoutSize
     * @see       java.awt.Container
     * @see       java.awt.Container#doLayout
     * @since     JDK1.0
     */
    public Dimension minimumLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = m.getMinimumSize();
                    dim.height = Math.max(dim.height, d.height);

                    if (i > 0) {
                        dim.width += getHgap();
                    }

                    dim.width += d.width;
                }
            }

            Insets insets = target.getInsets();
            dim.width += (insets.left + insets.right + (getHgap() * 2));
            dim.height += (insets.top + insets.bottom + (getVgap() * 2));

            return dim;
        }
    }

    /**
     * Returns the preferred dimensions for this layout given the components
     * in the specified target container.
     * @param target the component which needs to be laid out
     * @return    the preferred dimensions to lay out the
     *                    subcomponents of the specified container.
     * @see java.awt.Container
     * @see #minimumLayoutSize
     * @see       java.awt.Container#getPreferredSize
     * @since     JDK1.0
     */
    public Dimension preferredLayoutSize(Container target) {
        int maxWidth = getMaximumWidth(target);

        synchronized (target.getTreeLock()) {
            Dimension dim = new Dimension(0, 0);
            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    dim.height = Math.max(dim.height, d.height);

                    if (i > 0) {
                        dim.width += getHgap();
                    }

                    dim.width += maxWidth;
                }
            }

            Insets insets = target.getInsets();
            dim.width += (insets.left + insets.right + (getHgap() * 2));
            dim.height += (insets.top + insets.bottom + (getVgap() * 2));

            return dim;
        }
    }

    private static int getMaximumWidth(Container target) {
        int maxWidth = 0;

        synchronized (target.getTreeLock()) {
            int nmembers = target.getComponentCount();

            for (int i = 0; i < nmembers; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    Dimension d = m.getPreferredSize();
                    maxWidth = Math.max(d.width, maxWidth);
                }
            }
        }

        return maxWidth;
    }

    /**
     * Centers the elements in the specified row, if there is any slack.
     * @param target the component which needs to be moved
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width dimensions
     * @param height the height dimensions
     * @param rowStart the beginning of the row
     * @param rowEnd the the ending of the row
     */
    private void moveComponents2(Container target, int x, int y, int width, int height, int rowStart, int rowEnd) {
        synchronized (target.getTreeLock()) {
            switch (getAlignment()) {
                case LEFT:
                    break;
                case CENTER:
                    x += (width / 2);

                    break;
                case RIGHT:
                    x += width;

                    break;
            }

            for (int i = rowStart; i < rowEnd; i++) {
                Component m = target.getComponent(i);

                if (m.isVisible()) {
                    m.setLocation(x, y + ((height - m.getSize().height) / 2));
                    x += (getHgap() + m.getSize().width);
                }
            }
        }
    }
}
