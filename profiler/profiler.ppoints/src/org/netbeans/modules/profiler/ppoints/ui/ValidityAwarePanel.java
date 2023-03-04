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

package org.netbeans.modules.profiler.ppoints.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;


/**
 *
 * @author Jiri Sedlacek
 */
public abstract class ValidityAwarePanel extends JPanel implements Scrollable {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private List<ValidityListener> listeners = new ArrayList<ValidityListener>();
    private boolean isValid;
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Component getInitialFocusTarget() {
        return null;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return null;
    }

    @Override
    public Dimension getPreferredSize() {
        if (getParent() instanceof JViewport) {
            return getMinimumSize();
        } else {
            return super.getPreferredSize();
        }
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 50;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Container parent = getParent();

        if (!(parent instanceof JViewport)) {
            return false;
        }

        return getMinimumSize().width < ((JViewport) parent).getWidth();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    public void addValidityListener(ValidityListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public boolean areSettingsValid() {
        return isValid;
    }

    public void removeValidityListener(ValidityListener listener) {
        listeners.remove(listener);
    }

    protected void fireValidityChanged(boolean isValid) {
        this.isValid = isValid;

        for (ValidityListener listener : listeners) {
            listener.validityChanged(isValid);
        }
    }
}
