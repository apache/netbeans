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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public final class TransparentToolBar extends JPanel {

    private static Boolean NEEDS_PANEL;
    private static Boolean CUSTOM_FILLER;
    
    private final JToolBar toolbar;
    private final ItemListener listener = new ItemListener();

    
    public TransparentToolBar() {
        toolbar = needsPanel() ? null : new JToolBar();
        setOpaque(false);
        if (toolbar == null) {
            // Toolbar is a JPanel (GTK)
            setLayout(new HorizontalLayout(false));
        } else {
            // Toolbar is a JToolBar (default)
            toolbar.setBorderPainted(false);
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setOpaque(false);
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            setLayout(new BorderLayout());
            add(toolbar, BorderLayout.CENTER);
        }
        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (isShowing()) {
                        removeHierarchyListener(this);
                        for (Component c : getComponents()) {
                            if (c instanceof AbstractButton) {
                                listener.refresh((AbstractButton)c);
                            }
                        }
                    }
                }
            }
        });
    }

    public void addItem(JComponent c) {
        c.setOpaque(false);

        if (c instanceof JButton)
            ((JButton)c).setDefaultCapable(false);

        if (toolbar != null) {
            toolbar.add(c);
        } else {
            add(c);
            if (c instanceof AbstractButton) {
                AbstractButton b = (AbstractButton) c;
                b.addMouseListener(listener);
                b.addChangeListener(listener);
                b.addFocusListener(listener);
                b.setRolloverEnabled(true);
            }
        }
    }

    public void removeItem(JComponent c) {
        if (toolbar != null) {
            toolbar.remove(c);
        } else {
            if (c instanceof AbstractButton) {
                c.removeMouseListener(listener);
                ((AbstractButton) c).removeChangeListener(listener);
                c.removeFocusListener(listener);
            }
            remove(c);
        }
    }
    
    public void addSeparator() {
        JToolBar.Separator separator = new JToolBar.Separator();
        separator.setOrientation(JToolBar.Separator.VERTICAL);
        addItem(separator);
    }
    
    public void addFiller() {
        Dimension minDim = new Dimension(0, 0);
        Dimension maxDim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        final boolean customFiller = customFiller();
        Box.Filler filler = new Box.Filler(minDim, minDim, maxDim) {
            public Dimension getPreferredSize() {
                if (customFiller) {
                    int currentWidth = TransparentToolBar.this.getSize().width;
                    int minimumWidth = TransparentToolBar.this.getMinimumSize().width;
                    int extraWidth = currentWidth - minimumWidth;
                    return new Dimension(Math.max(extraWidth, 0), 0);
                } else {
                    return super.getPreferredSize();
                }
            }
            protected void paintComponent(Graphics g) {}
        };
        addItem(filler);
    }
    

    private static boolean needsPanel() {
        if (NEEDS_PANEL == null) NEEDS_PANEL = UIUtils.isGTKLookAndFeel();
        return NEEDS_PANEL;
    }
    
    private static boolean customFiller() {
        if (CUSTOM_FILLER == null) CUSTOM_FILLER = UIUtils.isGTKLookAndFeel() ||
                                                  UIUtils.isNimbusLookAndFeel();
        return CUSTOM_FILLER;
    }

            
    private static final class ItemListener extends MouseAdapter implements ChangeListener, FocusListener {

        private static final String PROP_HOVERED = "BUTTON_HOVERED"; // NOI18N

        public void mouseEntered(MouseEvent e) {
            AbstractButton b = (AbstractButton) e.getSource();
            b.putClientProperty(PROP_HOVERED, Boolean.TRUE);
            refresh(b);
        }

        public void mouseExited(MouseEvent e) {
            AbstractButton b = (AbstractButton) e.getSource();
            b.putClientProperty(PROP_HOVERED, Boolean.FALSE);
            refresh(b);
        }

        public void stateChanged(ChangeEvent e) {
            refresh((AbstractButton) e.getSource());
        }

        public void focusGained(FocusEvent e) {
            refresh((AbstractButton) e.getSource());
        }

        public void focusLost(FocusEvent e) {
            refresh((AbstractButton) e.getSource());
        }

        private void refresh(final AbstractButton b) {
            b.setBackground(UIUtils.getProfilerResultsBackground());
            boolean hovered = Boolean.TRUE.equals(b.getClientProperty(PROP_HOVERED));
            boolean filled = b.isEnabled() && (hovered || b.isSelected() || b.isFocusOwner());
            b.setOpaque(filled);
            b.setContentAreaFilled(filled);
            b.repaint();
        }
        
    }
}
