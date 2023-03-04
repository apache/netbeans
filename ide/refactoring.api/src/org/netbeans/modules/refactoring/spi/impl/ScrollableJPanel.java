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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 *
 * @author Ralph Ruijs
 */
public class ScrollableJPanel extends JPanel implements Scrollable {

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(
            Rectangle visible, int orientation, int direction) {
        switch (orientation) {
            case SwingConstants.HORIZONTAL:
                return visible.width * 10 / 100;
            case SwingConstants.VERTICAL:
                return visible.height * 10 / 100;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation); //NOI18N
        }
    }

    @Override
    public int getScrollableBlockIncrement(
            Rectangle visible, int orientation, int direction) {
        switch (orientation) {
            case SwingConstants.HORIZONTAL:
                return visible.width * 100 / 100;
            case SwingConstants.VERTICAL:
                return visible.height * 100 / 100;
            default:
                throw new IllegalArgumentException("Invalid orientation: " + orientation); //NOI18N
        }
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        if (getParent() instanceof JViewport) {
            return (((JViewport) getParent()).getHeight() > getPreferredSize().height);
        }

        return false;
    }
}
