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

package org.netbeans.modules.profiler.snaptracer.impl.swing;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;

/**
 * Predefined JScrollPane to be used in VisualVM, for example in details views.
 * Use UISupport.createScrollableContainer() method instead of instantiating
 * this class directly if creating scrollable container for the Options panel.
 *
 * @author Jiri Sedlacek
 */
public final class ScrollableContainer extends JScrollPane {

    /**
     * Creates new instance of ScrollableContainer.
     * 
     * @param view component to be displayed
     */
    public ScrollableContainer(JComponent view) {
        this(view, VERTICAL_SCROLLBAR_AS_NEEDED,
             HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }

    /**
     * Creates new instance of ScrollableContainer.
     *
     * @param view component to be displayed
     * @param vsbPolicy policy flag for the vertical scrollbar
     * @param hsbPolicy policy flag for the horizontal scrollbar
     */
    public ScrollableContainer(JComponent view, int vsbPolicy, int hsbPolicy) {
        setViewportView(new ScrollableContents(view));

        setVerticalScrollBarPolicy(vsbPolicy);
        setHorizontalScrollBarPolicy(hsbPolicy);

        setBorder(BorderFactory.createEmptyBorder());
        setViewportBorder(BorderFactory.createEmptyBorder());

        getViewport().setOpaque(false);
        setOpaque(false);
    }


    // --- Scrollable container ------------------------------------------------

    private class ScrollableContents extends JPanel implements Scrollable {

        public ScrollableContents(JComponent contents) {
            super(new BorderLayout());
            setOpaque(false);
            add(contents, BorderLayout.CENTER);
        }

        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect,
                                              int orientation, int direction) {
            return 20;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect,
                                               int orientation, int direction) {
            return (int)(visibleRect.height * 0.9d);
        }

        public boolean getScrollableTracksViewportWidth() {
            if (getHorizontalScrollBarPolicy() == HORIZONTAL_SCROLLBAR_NEVER)
                return true;

            Container parent = getParent();
            if (!(parent instanceof JViewport)) return false;
            return getMinimumSize().width < ((JViewport)parent).getWidth();
        }

        public boolean getScrollableTracksViewportHeight() {
            if (getVerticalScrollBarPolicy() == VERTICAL_SCROLLBAR_NEVER)
                return true;

            Container parent = getParent();
            if (!(parent instanceof JViewport)) return false;
            return getMinimumSize().height < ((JViewport)parent).getHeight();
        }

    }

}
