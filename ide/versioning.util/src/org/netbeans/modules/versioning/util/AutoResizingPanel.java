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

package org.netbeans.modules.versioning.util;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Window;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Panel that, when asked to do so, resizes the windows ancestor
 * ({@code JDialog}, {@code JFrame}, {@code JWindow}) such that it fits
 * this panel at its preferred size.
 * To trigger the resize operation, call method {@link #resizeAsNecessary}.
 *
 * @author Marian Petras
 */
public class AutoResizingPanel extends JPanel {

    private Dimension requestedSize;

    public AutoResizingPanel() {
        super();
    }

    public AutoResizingPanel(LayoutManager layout) {
        super(layout);
    }

    public AutoResizingPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public AutoResizingPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    public void enlargeHorizontallyAsNecessary() {
        int currWidth = getWidth();
        int currHeight = getHeight();
        Dimension prefSize = getPreferredSize();
        enlargeAsNecessary(currWidth,
                           currHeight,
                           Math.max(currWidth, prefSize.width),
                           currHeight);
    }

    public void enlargeVerticallyAsNecessary() {
        int currWidth = getWidth();
        int currHeight = getHeight();
        Dimension prefSize = getPreferredSize();
        enlargeAsNecessary(currWidth,
                           currHeight,
                           currWidth,
                           Math.max(currHeight, prefSize.height));
    }

    public void enlargeAsNecessary() {
        int currWidth = getWidth();
        int currHeight = getHeight();
        Dimension prefSize = getPreferredSize();
        enlargeAsNecessary(currWidth,
                           currHeight,
                           Math.max(currWidth, prefSize.width),
                           Math.max(currHeight, prefSize.height));
    }

    private void enlargeAsNecessary(int currentWidth,
                                    int currentHeight,
                                    int requestedWidth,
                                    int requestedHeight) {
        if ((currentWidth >= requestedWidth) && (currentHeight >= requestedHeight)) {
            /* the panel is large enough */
            return;
        }

        Window window = SwingUtilities.getWindowAncestor(this);
        if (window == null) {
            return;
        }

        try {
            requestedSize = new Dimension(requestedWidth, requestedHeight);
            window.pack();
        } finally {
            requestedSize = null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return (requestedSize != null) ? requestedSize
                                       : super.getPreferredSize();
    }

}
