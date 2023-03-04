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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import javax.swing.JPanel;

/**
 * Panel which takes minimum, preferred and maximum sizes from its (only) child.
 * The panel can have only one child and it cannot be assigned a layout manager.
 * It always lays out its child such that the child covers the whole placeholder
 * panel.
 *
 * @author Marian Petras
 */
public class PlaceholderPanel extends JPanel {

    private static final Dimension ZERO_DIMENSION = new Dimension(0, 0);
    private static final Dimension MAX_DIMENSION = new Dimension(Short.MAX_VALUE,
                                                                 Short.MAX_VALUE);

    public PlaceholderPanel() {
        this(true);
    }

    public PlaceholderPanel(boolean isDoubleBuffered) {
        super(null, isDoubleBuffered);
    }

    public void setComponent(Component comp) {
        if (isEmpty()) {
            add(comp);
        } else {
            removeAll();
            add(comp);
            revalidate();
            repaint();
        }
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        if (mgr != null) {
            throw new UnsupportedOperationException(
                "PlaceholderPanel's layout manager cannot be changed.");//NOI18N
        }
    }

    @Override
    public Dimension getMinimumSize() {
        if (isEmpty()) {
            return new Dimension(ZERO_DIMENSION);
        }

        return getOccupant().getMinimumSize();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isEmpty()) {
            return new Dimension(ZERO_DIMENSION);
        }

        return getOccupant().getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        if (isEmpty()) {
            return new Dimension(MAX_DIMENSION);
        }

        return getOccupant().getMaximumSize();
    }


    @Override
    public void doLayout() {
        Component comp = getOccupant();
        if (comp != null) {
            comp.setBounds(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        if (!isEmpty()) {
            throw new IllegalStateException(
                    "This placeholder is already occupied.");           //NOI18N
        }

        super.addImpl(comp, constraints, index);
        updateOpacity();
        invalidate();
    }

    @Override
    public void remove(Component comp) {
        super.remove(comp);
        updateOpacity();
        invalidate();
    }

    @Override
    public void remove(int index) {
        super.remove(index);
        updateOpacity();
        invalidate();
    }

    @Override
    public synchronized void remove(MenuComponent popup) {
        super.remove(popup);
        updateOpacity();
        invalidate();
    }

    @Override
    public void removeAll() {
        super.removeAll();
        updateOpacity();
        invalidate();
    }

    private void updateOpacity() {
        setOpaque(isEmpty());
    }

    private Component getOccupant() {
        return isEmpty() ? null : getComponent(0);
    }

    public boolean isEmpty() {
        return getComponentCount() == 0;
    }

}
