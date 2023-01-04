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

package org.netbeans.lib.profiler.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MenuComponent;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;


/**
 *
 * @author Jiri Sedlacek
 */
public class JTitledPanel extends JPanel {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    private static class ThinBevelBorder extends BevelBorder {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ThinBevelBorder(int bevelType, Color highlight, Color shadow) {
            super(bevelType, highlight.brighter(), highlight, shadow, shadow.brighter());
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = insets.top = insets.right = insets.bottom = 1;

            return insets;
        }

        @Override
        protected void paintLoweredBevel(Component c, Graphics g, int x, int y, int width, int height) {
            if (!c.isEnabled()) {
                return;
            }

            Color oldColor = g.getColor();
            int h = height;
            int w = width;

            g.translate(x, y);

            g.setColor(getShadowOuterColor(c));
            g.drawLine(0, 0, 0, h - 1);
            g.drawLine(1, 0, w - 1, 0);

            g.setColor(getHighlightInnerColor(c));
            g.drawLine(1, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, 1, w - 1, h - 2);

            g.translate(-x, -y);
            g.setColor(oldColor);
        }

        @Override
        protected void paintRaisedBevel(Component c, Graphics g, int x, int y, int width, int height) {
            if (!c.isEnabled()) {
                return;
            }

            Color oldColor = g.getColor();
            int h = height;
            int w = width;

            g.translate(x, y);

            g.setColor(getHighlightInnerColor(c));
            g.drawLine(0, 0, 0, h - 1);
            g.drawLine(1, 0, w - 1, 0);

            g.setColor(getShadowOuterColor(c));
            g.drawLine(0, h - 1, w - 1, h - 1);
            g.drawLine(w - 1, 0, w - 1, h - 2);

            g.translate(-x, -y);
            g.setColor(oldColor);
        }
    }

    private class DoubleClickListener extends MouseAdapter {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public void mouseClicked(MouseEvent e) {
            if ((e.getModifiers() == InputEvent.BUTTON1_MASK) && (e.getClickCount() == 2)) {
                if (isMaximized()) {
                    restore();
                } else {
                    maximize();
                }
            }

            ;
        }
    }

    private static class ImageIconButton extends JButton implements MouseListener {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private Border emptyBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        private Border loweredBorder = new ThinBevelBorder(BevelBorder.LOWERED, Color.WHITE, Color.GRAY);
        private Border raisedBorder = new ThinBevelBorder(BevelBorder.RAISED, Color.WHITE, Color.GRAY);
        private boolean focused = false;
        private boolean pressed = false;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public ImageIconButton(ImageIcon icon) {
            super();

            GrayFilter enabledFilter = new GrayFilter(true, 35);
            ImageProducer prod = new FilteredImageSource(icon.getImage().getSource(), enabledFilter);
            Icon grayIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(prod));
            GrayFilter disabledFilter = new GrayFilter(true, 60);
            prod = new FilteredImageSource(icon.getImage().getSource(), disabledFilter);

            Icon disabledIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(prod));

            setIcon(grayIcon);
            setRolloverIcon(icon);
            setPressedIcon(icon);
            setDisabledIcon(disabledIcon);
            setIconTextGap(0);
            setBorder(emptyBorder);
            setFocusable(false);
            setContentAreaFilled(false);

            setPreferredSize(new Dimension(icon.getIconWidth() + 8, icon.getIconHeight() + 8));

            addMouseListener(this);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
            focused = true;

            if (pressed) {
                setBorder(loweredBorder);
            } else {
                setBorder(raisedBorder);
            }
        }

        public void mouseExited(MouseEvent e) {
            focused = false;
            setBorder(emptyBorder);
        }

        public void mousePressed(MouseEvent e) {
            pressed = true;
            setBorder(loweredBorder);
        }

        public void mouseReleased(MouseEvent e) {
            pressed = false;

            if (focused) {
                setBorder(raisedBorder);
            } else {
                setBorder(emptyBorder);
            }
        }
    }

    // --- Presenter -------------------------------------------------------------
    private class Presenter extends JToggleButton {
        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public Presenter() {
            super();

            if (JTitledPanel.this.getIcon() == null) {
                setText(JTitledPanel.this.getTitle());
                setToolTipText(JTitledPanel.this.getTitle());
            } else {
                setIcon(JTitledPanel.this.getIcon());
                setToolTipText(JTitledPanel.this.getTitle());
            }

            setSelected(JTitledPanel.this.isVisible());
            addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JTitledPanel.this.setVisible(isSelected());
                    }
                });
            JTitledPanel.this.addComponentListener(new ComponentAdapter() {
                @Override
                    public void componentShown(ComponentEvent e) {
                        setSelected(true);
                    }

                @Override
                    public void componentHidden(ComponentEvent e) {
                        setSelected(false);
                    }
                });
            addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("enabled".equals(evt.getPropertyName())) {
                            JTitledPanel.this.setButtonsEnabled(isEnabled()); // NOI18N
                        }
                    }
                });
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width += 20;
            return d;
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final ImageIcon closePanelIcon = Icons.getImageIcon(GeneralIcons.CLOSE_PANEL);
    private static final ImageIcon maximizePanelIcon = Icons.getImageIcon(GeneralIcons.MAXIMIZE_PANEL);
    private static final ImageIcon restorePanelIcon = Icons.getImageIcon(GeneralIcons.RESTORE_PANEL);
    private static final ImageIcon minimizePanelIcon = Icons.getImageIcon(GeneralIcons.MINIMIZE_PANEL);
    public static final int STATE_CLOSED = 1000;
    public static final int STATE_RESTORED = 1001;
    public static final int STATE_MAXIMIZED = 1002;
    public static final int STATE_MINIMIZED = 1003;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractButton presenter;
    private Icon icon;
    private JButton closePanelButton;
    private JButton maximizePanelButton;
    private JButton minimizePanelButton;
    private JButton restorePanelButton;
    private JPanel contentPanel;
    private JPanel titlePanel;
    private String title;
    private Collection<ActionListener> actionListeners = new CopyOnWriteArraySet<ActionListener>();
    private boolean showButtons;
    private int state;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public JTitledPanel(String title) {
        this(title, null);
    }

    public JTitledPanel(String title, Icon icon) {
        this(title, icon, false);
    }

    public JTitledPanel(String title, Icon icon, boolean showButtons) {
        super();
        this.title = title;
        this.icon = icon;
        this.showButtons = showButtons;
        initComponents();
        restore();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setButtonsEnabled(boolean enabled) {
        closePanelButton.setEnabled(enabled);
        maximizePanelButton.setEnabled(enabled);
        restorePanelButton.setEnabled(enabled);
        minimizePanelButton.setEnabled(enabled);
    }

    public boolean isClosed() {
        return getState() == STATE_CLOSED;
    }

    public JPanel getContentPanel() {
        if (contentPanel == null) {
            contentPanel = new JPanel();
        }

        return contentPanel;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public void setLayout(LayoutManager mgr) {
        getContentPanel().setLayout(mgr);
    }

    @Override
    public LayoutManager getLayout() {
        return getContentPanel().getLayout();
    }

    public boolean isMaximized() {
        return getState() == STATE_MAXIMIZED;
    }

    public boolean isMinimized() {
        return getState() == STATE_MINIMIZED;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(0, titlePanel.getPreferredSize().height);
    }

    public AbstractButton getPresenter() {
        if (presenter == null) {
            presenter = new Presenter();
        }

        return presenter;
    }

    public boolean isRestored() {
        return getState() == STATE_RESTORED;
    }

    public int getState() {
        if (!isVisible()) {
            state = STATE_CLOSED;
        }

        if (isVisible() && (state == STATE_CLOSED)) {
            state = STATE_RESTORED;
        }

        return state;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public Component add(Component comp) {
        return getContentPanel().add(comp);
    }

    @Override
    public Component add(Component comp, int index) {
        return getContentPanel().add(comp, index);
    }

    @Override
    public void add(Component comp, Object constraints) {
        getContentPanel().add(comp, constraints);
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        getContentPanel().add(comp, constraints, index);
    }

    @Override
    public Component add(String name, Component comp) {
        return getContentPanel().add(name, comp);
    }

    @Override
    public void add(PopupMenu popup) {
        getContentPanel().add(popup);
    }

    public void addActionListener(ActionListener listener) {
        actionListeners.add(listener);
    }

    public boolean areButtonsEnabled() {
        return closePanelButton.isEnabled();
    }

    public void close() {
        if (isClosed()) {
            return;
        }

        setVisible(false);
        state = STATE_CLOSED;
        fireActionPerformed();
    }

    public void maximize() {
        if (isMaximized()) {
            return;
        }

        maximizePanelButton.setVisible(false);
        restorePanelButton.setVisible(true);
        minimizePanelButton.setVisible(true);
        contentPanel.setVisible(true);
        state = STATE_MAXIMIZED;
        fireActionPerformed();
    }

    public void minimize() {
        if (isMinimized()) {
            return;
        }

        maximizePanelButton.setVisible(true);
        restorePanelButton.setVisible(true);
        minimizePanelButton.setVisible(false);
        contentPanel.setVisible(false);
        state = STATE_MINIMIZED;
        fireActionPerformed();
    }

    @Override
    public void remove(Component component) {
        getContentPanel().remove(component);
    }

    @Override
    public void remove(MenuComponent component) {
        getContentPanel().remove(component);
    }

    @Override
    public void remove(int index) {
        getContentPanel().remove(index);
    }

    public void removeActionListener(ActionListener listener) {
        actionListeners.remove(listener);
    }

    @Override
    public void removeAll() {
        getContentPanel().removeAll();
    }

    public void restore() {
        if (isRestored()) {
            return;
        }

        maximizePanelButton.setVisible(true);
        restorePanelButton.setVisible(false);
        minimizePanelButton.setVisible(true);
        contentPanel.setVisible(true);
        state = STATE_RESTORED;
        fireActionPerformed();
    }
    
    protected Component[] getAdditionalControls() {
        return null;
    }

    protected Color getTitleBorderColor() {
        return UIManager.getLookAndFeel().getID().equals("Metal") ? // NOI18N
                          UIManager.getColor("Button.darkShadow") : // NOI18N
                          UIManager.getColor("Button.shadow"); // NOI18N
    }

    private void fireActionPerformed() {
        for (ActionListener l : actionListeners) {
            l.actionPerformed(new ActionEvent(this, getState(), ""));
        }
    }

    private void initComponents() {
        DoubleClickListener dblClickListener = new DoubleClickListener();
        
        titlePanel = new JPanel(new GridBagLayout());
        titlePanel.addMouseListener(dblClickListener);
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
                             BorderFactory.createLineBorder(getTitleBorderColor()),
                             BorderFactory.createEmptyBorder(2, 5, 2, 2)));
        titlePanel.setOpaque(true);
        titlePanel.setBackground(UIUtils.getDarker(UIUtils.getProfilerResultsBackground()));
        
        GridBagConstraints gbc;
        
        if (icon != null) {
            gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 0, 0, 4);
            JLabel iconLabel = new JLabel(icon) {
                @Override
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
            iconLabel.setOpaque(false);
            iconLabel.addMouseListener(dblClickListener);
            titlePanel.add(iconLabel, gbc);
        }
        
        JLabel titleLabel = new JLabel(title) {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        titleLabel.setForeground(UIManager.getColor("ToolTip.foreground")); // NOI18N
        titleLabel.setFont(UIManager.getFont("ToolTip.font")); // NOI18N
        titleLabel.setOpaque(false);
        titleLabel.addMouseListener(dblClickListener);
        titlePanel.add(titleLabel, new GridBagConstraints());
        
        gbc = new GridBagConstraints();
        gbc.weightx = 1f;
        gbc.weighty = 1f;
        JPanel spacer = new JPanel(null);
        spacer.addMouseListener(dblClickListener);
        spacer.setOpaque(false);
        titlePanel.add(spacer, gbc);
        
        Component[] additionalControls = getAdditionalControls();
        if (additionalControls != null && additionalControls.length > 0)
            for (Component c : additionalControls)
                titlePanel.add(c, new GridBagConstraints());

        minimizePanelButton = new ImageIconButton(minimizePanelIcon) {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        minimizePanelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    minimize();
                }
                ;
            });
        //if (showButtons) titlePanel.add(minimizePanelButton, new GridBagConstraints());

        maximizePanelButton = new ImageIconButton(maximizePanelIcon) {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        maximizePanelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    maximize();
                }
                ;
            });
        //if (showButtons) titlePanel.add(maximizePanelButton, new GridBagConstraints());

        restorePanelButton = new ImageIconButton(restorePanelIcon) {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        restorePanelButton.setVisible(false);
        restorePanelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    restore();
                }
                ;
            });
        //if (showButtons) titlePanel.add(restorePanelButton, new GridBagConstraints());
        
        closePanelButton = new ImageIconButton(closePanelIcon) {
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        closePanelButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    close();
                }
                ;
            });
        if (showButtons) titlePanel.add(closePanelButton, new GridBagConstraints());

        super.setLayout(new BorderLayout()); // overridden for 'this'
        super.add(titlePanel, BorderLayout.NORTH); // overridden for 'this'
        super.add(contentPanel, BorderLayout.CENTER); // overridden for 'this'
    }
}
