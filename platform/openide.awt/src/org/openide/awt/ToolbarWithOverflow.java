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
package org.openide.awt;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import org.openide.util.Mutex;
import org.openide.util.VectorIcon;

/**
 * ToolbarWithOverflow provides a component which is useful for displaying commonly used
 * actions.  It adds an overflow button when the toolbar becomes too small to show all the
 * available actions.
 *
 * @author Th. Oikonomou
 * @since 7.51
 */
public class ToolbarWithOverflow extends JToolBar {

    private JButton overflowButton;
    private JPopupMenu popup;
    private JToolBar overflowToolbar;
    private boolean displayOverflowOnHover = true;
    private final String PROP_PREF_ICON_SIZE = "PreferredIconSize"; //NOI18N
    private final String PROP_DRAGGER = "_toolbar_dragger_"; //NOI18N
    private final String PROP_JDEV_DISABLE_OVERFLOW = "nb.toolbar.overflow.disable"; //NOI18N
    private AWTEventListener awtEventListener;
    private ComponentAdapter componentAdapter;
    // keep track of the overflow popup that is showing, possibly from another overflow button, in order to hide it if necessary
    private static JPopupMenu showingPopup = null;

    /**
     * Creates a new tool bar; orientation defaults to
     * <code>HORIZONTAL</code>.
     */
    public ToolbarWithOverflow() {
        this(HORIZONTAL);
    }

    /**
     * Creates a new tool bar with the specified
     * <code>orientation</code>. The
     * <code>orientation</code> must be either
     * <code>HORIZONTAL</code> or
     * <code>VERTICAL</code>.
     *
     * @param orientation the orientation desired
     */
    public ToolbarWithOverflow(int orientation) {
        this(null, orientation);
    }

    /**
     * Creates a new tool bar with the specified
     * <code>name</code>. The name is used as the title of the undocked tool
     * bar. The default orientation is
     * <code>HORIZONTAL</code>.
     *
     * @param name the name of the tool bar
     */
    public ToolbarWithOverflow(String name) {
        this(name, HORIZONTAL);
    }

    /**
     * Creates a new tool bar with a specified
     * <code>name</code> and
     * <code>orientation</code>. All other constructors call this constructor.
     * If
     * <code>orientation</code> is an invalid value, an exception will be
     * thrown.
     *
     * @param name the name of the tool bar
     * @param orientation the initial orientation -- it must be     *		either <code>HORIZONTAL</code> or <code>VERTICAL</code>
     * @exception IllegalArgumentException if orientation is neither
     * <code>HORIZONTAL</code> nor <code>VERTICAL</code>
     */
    public ToolbarWithOverflow(String name, int orientation) {
        super(name, orientation);
        setupOverflowButton();
        popup = new SafePopupMenu();
        popup.setBorderPainted(false);
        popup.setBorder(BorderFactory.createEmptyBorder());
        overflowToolbar = new SafeToolBar("overflowToolbar", orientation == HORIZONTAL ? VERTICAL : HORIZONTAL);
        overflowToolbar.setFloatable(false);
        overflowToolbar.setBorder(BorderFactory.createLineBorder(UIManager.getColor("controlShadow"), 1));
    }

    private ComponentListener getComponentListener() {
        if (componentAdapter == null) {
            componentAdapter = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    maybeAddOverflow();
                }
            };
        }
        return componentAdapter;
    }

    private AWTEventListener getAWTEventListener() {
        if (awtEventListener == null) {
            awtEventListener = new AWTEventListener() {
                @Override
                public void eventDispatched(AWTEvent event) {
                    MouseEvent e = (MouseEvent) event;
                    if(isVisible() && !isShowing() && popup.isShowing()) {
			showingPopup = null;
                        popup.setVisible(false);
                        return;
                    }
                    if (event.getSource() == popup) {
                        if (popup.isShowing() && e.getID() == MouseEvent.MOUSE_EXITED) {
                            int minX = popup.getLocationOnScreen().x;
                            int maxX = popup.getLocationOnScreen().x + popup.getWidth();
                            int minY = popup.getLocationOnScreen().y;
                            int maxY = popup.getLocationOnScreen().y + popup.getHeight();
                            if (e.getXOnScreen() < minX || e.getXOnScreen() >= maxX || e.getYOnScreen() < minY || e.getYOnScreen() >= maxY) {
				showingPopup = null;
                                popup.setVisible(false);
                            }
                        }
                    } else {
                        if (popup.isShowing() && overflowButton.isShowing() && (e.getID() == MouseEvent.MOUSE_MOVED || e.getID() == MouseEvent.MOUSE_EXITED)) {
                            int minX = overflowButton.getLocationOnScreen().x;
                            int maxX = getOrientation() == HORIZONTAL ? minX + popup.getWidth()
                                    : minX + overflowButton.getWidth() + popup.getWidth();
                            int minY = overflowButton.getLocationOnScreen().y;
                            int maxY = getOrientation() == HORIZONTAL ? minY + overflowButton.getHeight() + popup.getHeight()
                                    : minY + popup.getHeight();
                            if (e.getXOnScreen() < minX || e.getYOnScreen() < minY || e.getXOnScreen() > maxX || e.getYOnScreen() > maxY) {
				showingPopup = null;
                                popup.setVisible(false);
                            }
                        }
                    }
                }
            };
        }
        return awtEventListener;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!Boolean.TRUE.equals(getClientProperty(PROP_JDEV_DISABLE_OVERFLOW))) {
	    addComponentListener(getComponentListener());
	    Toolkit.getDefaultToolkit().addAWTEventListener(getAWTEventListener(), AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
	}
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (componentAdapter != null) {
            removeComponentListener(componentAdapter);
        }
        if (awtEventListener != null) {
            Toolkit.getDefaultToolkit().removeAWTEventListener(awtEventListener);
        }
    }

    @Override
    public void updateUI() {
	Mutex.EVENT.readAccess(new Runnable() {
	    @Override
	    public void run() {
		superUpdateUI();
	    }
	});
    }

    final void superUpdateUI() {
        super.updateUI();
    }

    /**
     * Returns whether the overflow should be displayed on hover or not. The
     * default value is <code>true</code>.
     *
     * @return <code>true</code> if overflow is displayed on hover; <code>false</code> otherwise
     */
    public boolean isDisplayOverflowOnHover() {
        return displayOverflowOnHover;
    }

    /**
     * Sets whether the overflow should be displayed on hover or not. The
     * default value is <code>true</code>.
     *
     * @param displayOverflowOnHover if <code>true</code>, the overflow will be displayed on hover;
     * <code>false</code> otherwise
     */
    public void setDisplayOverflowOnHover(boolean displayOverflowOnHover) {
        this.displayOverflowOnHover = displayOverflowOnHover;
        setupOverflowButton();
    }
    
    @Override
    public Dimension getPreferredSize() {
        Component[] comps = getAllComponents();
        Insets insets = getInsets();
        int width = null == insets ? 0 : insets.left + insets.right;
        int height = null == insets ? 0 : insets.top + insets.bottom;
        for (int i = 0; i < comps.length; i++) {
            Component comp = comps[i];
	    if (!comp.isVisible()) {
		continue;
	    }
            width += getOrientation() == HORIZONTAL ? comp.getPreferredSize().width : comp.getPreferredSize().height;
            height = Math.max( height, (getOrientation() == HORIZONTAL 
                        ? (comp.getPreferredSize().height + (insets == null ? 0 : insets.top + insets.bottom))
                        : (comp.getPreferredSize().width) + (insets == null ? 0 : insets.left + insets.right)));
        }
        if(overflowToolbar.getComponentCount() > 0) {
            width += getOrientation() == HORIZONTAL ? overflowButton.getPreferredSize().width : overflowButton.getPreferredSize().height;
        }
        Dimension dim = getOrientation() == HORIZONTAL ? new Dimension(width, height) : new Dimension(height, width);
        return dim;        
    }

    @Override
    public void setOrientation(int o) {
        super.setOrientation(o);
        setupOverflowButton();
    }
    
    @Override
    public void removeAll() {
        super.removeAll();
        overflowToolbar.removeAll();
    }
    
    @Override
    public void validate() {
        if (!Boolean.TRUE.equals(getClientProperty(PROP_JDEV_DISABLE_OVERFLOW))) {
            int visibleButtons = computeVisibleButtons();
            if (visibleButtons == -1) {
                handleOverflowRemoval();
            } else {
                handleOverflowAddittion(visibleButtons);
            }
        }
        super.validate();
    }
    
    private void setupOverflowButton() {
        overflowButton = new JButton(getOrientation() == HORIZONTAL
                ? ToolbarArrowIcon.INSTANCE_VERTICAL : ToolbarArrowIcon.INSTANCE_HORIZONTAL)
        {
            @Override
            public void updateUI() {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        superUpdateUI();
                    }
                });
            }

            private void superUpdateUI() {
                super.updateUI();
            }
        };
        overflowButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(popup.isShowing()) {
		    showingPopup = null;
                    popup.setVisible(false);
                } else {
                    displayOverflow();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
		if(showingPopup != null && showingPopup != popup) {
                    showingPopup.setVisible(false);
		    showingPopup = null;
		}
                if(displayOverflowOnHover) {
                    displayOverflow();
                }
            }
        });
    }
    
    private void displayOverflow() {
        if( !overflowButton.isShowing() ) {
            return;
        }
        int x = getOrientation() == HORIZONTAL ? overflowButton.getLocationOnScreen().x : overflowButton.getLocationOnScreen().x + overflowButton.getWidth();
        int y = getOrientation() == HORIZONTAL ? overflowButton.getLocationOnScreen().y + overflowButton.getHeight() : overflowButton.getLocationOnScreen().y;
        popup.setLocation(x, y);
	showingPopup = popup;
        popup.setVisible(true);
    }

    /**
     * Determines if an overflow button should be added to or removed from the toolbar.
     */
    private void maybeAddOverflow() {
        validate();
        repaint();
    }

    private int computeVisibleButtons() {
        if (isShowing()) {
            int w = getOrientation() == HORIZONTAL ? overflowButton.getIcon().getIconWidth() + 4 : getWidth() - getInsets().left - getInsets().right;
            int h = getOrientation() == HORIZONTAL ? getHeight() - getInsets().top - getInsets().bottom : overflowButton.getIcon().getIconHeight() + 4;
            overflowButton.setMaximumSize(new Dimension(w, h));
            overflowButton.setMinimumSize(new Dimension(w, h));
            overflowButton.setPreferredSize(new Dimension(w, h));
        }
        handleIconResize();
        Component[] comps = getAllComponents();
        int sizeSoFar = 0;
        int maxSize = getOrientation() == HORIZONTAL ? getWidth() : getHeight();
        int overflowButtonSize = getOrientation() == HORIZONTAL ? overflowButton.getPreferredSize().width : overflowButton.getPreferredSize().height;
        int showingButtons = 0; // all that return true from isVisible()
        int visibleButtons = 0; // all visible that fit into the given space (maxSize)
        Insets insets = getInsets();
        if( null != insets ) {
            sizeSoFar = getOrientation() == HORIZONTAL ? insets.left+insets.right : insets.top+insets.bottom;
        }
        for (int i = 0; i < comps.length; i++) {
            Component comp = comps[i];
            if( !comp.isVisible() ) {
		continue;
	    }
            if (showingButtons == visibleButtons) {
                int size = getOrientation() == HORIZONTAL ? comp.getPreferredSize().width : comp.getPreferredSize().height;
                if (sizeSoFar + size <= maxSize) {
                    sizeSoFar += size;
                    visibleButtons++;
                }
            }
            showingButtons++;
        }
        if (visibleButtons < showingButtons && visibleButtons > 0 && sizeSoFar + overflowButtonSize > maxSize) {
            // overflow button needed but would not have enough space, remove one more button
            visibleButtons--;
        }
        if (visibleButtons == 0 && comps.length > 0
                && comps[0] instanceof JComponent
                && Boolean.TRUE.equals(((JComponent) comps[0]).getClientProperty(PROP_DRAGGER))) {
            visibleButtons = 1; // always include the dragger if present
        }
        if (visibleButtons == showingButtons) {
            visibleButtons = -1;
        }
        return visibleButtons;
    }

    private void handleOverflowAddittion(int visibleButtons) {
        Component[] comps = getAllComponents();
        removeAll();
        overflowToolbar.setOrientation(getOrientation() == HORIZONTAL ? VERTICAL : HORIZONTAL);
        popup.removeAll();

        for (Component comp : comps) {
            if (visibleButtons > 0) {
                add(comp);
                if (comp.isVisible()) {
                    visibleButtons--;
                }
            } else {
                overflowToolbar.add(comp);
            }
        }
        popup.add(overflowToolbar);
        add(overflowButton);
    }

    private void handleOverflowRemoval() {
        if (overflowToolbar.getComponents().length > 0) {
            remove(overflowButton);
            handleIconResize();
            for (Component comp : overflowToolbar.getComponents()) {
                add(comp);
            }
            overflowToolbar.removeAll();
            popup.removeAll();
        }
    }

    private void handleIconResize() {
        for (Component comp : overflowToolbar.getComponents()) {
            boolean smallToolbarIcons = getClientProperty(PROP_PREF_ICON_SIZE) == null;
            if (smallToolbarIcons) {
                ((JComponent) comp).putClientProperty(PROP_PREF_ICON_SIZE, null);
            } else {
                ((JComponent) comp).putClientProperty(PROP_PREF_ICON_SIZE, Integer.valueOf(24));
            }
        }
    }

    private Component[] getAllComponents() {
        Component[] toolbarComps;
        Component[] overflowComps = overflowToolbar.getComponents();
        if (overflowComps.length == 0) {
            toolbarComps = getComponents();
        } else {
            if (getComponentCount() > 0) {
                toolbarComps = new Component[getComponents().length - 1];
                System.arraycopy(getComponents(), 0, toolbarComps, 0, toolbarComps.length);
            } else {
                toolbarComps = new Component[0];
            }
        }
        Component[] comps = new Component[toolbarComps.length + overflowComps.length];
        System.arraycopy(toolbarComps, 0, comps, 0, toolbarComps.length);
        System.arraycopy(overflowComps, 0, comps, toolbarComps.length, overflowComps.length);
        return comps;
    }
    
    private static class SafeToolBar extends JToolBar {
        
        public SafeToolBar( String name, int orientation ) {
            super( name, orientation );
        }
        
        @Override
        public void updateUI() {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    superUpdateUI();
                }
            });
        }

        final void superUpdateUI() {
            super.updateUI();
        }
    }
    
    private static class SafePopupMenu extends JPopupMenu {
        @Override
        public void updateUI() {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    superUpdateUI();
                }
            });
        }

        final void superUpdateUI() {
            super.updateUI();
        }
    }

    /**
     * Vectorized version of {@code toolbar_arrow_horizontal.png} and
     * {@code toolbar_arrow_vertical.png}.
     */
    private static final class ToolbarArrowIcon extends VectorIcon {
        public static final Icon INSTANCE_HORIZONTAL = new ToolbarArrowIcon(true);
        public static final Icon INSTANCE_VERTICAL = new ToolbarArrowIcon(false);
        private final boolean horizontal;

        private ToolbarArrowIcon(boolean horizontal) {
            super(11, 11);
            this.horizontal = horizontal;
        }

        @Override
        protected void paintIcon(Component c, Graphics2D g, int width, int height, double scaling) {
            if (horizontal) {
                // Rotate 90 degrees counterclockwise.
                g.rotate(-Math.PI / 2.0, width / 2.0, height / 2.0);
            }
            // Draw two chevrons pointing downwards. Make strokes a little thicker at low scalings.
            double strokeWidth = 0.8 * scaling + 0.3;
            g.setStroke(new BasicStroke((float) strokeWidth));
            final Color color;
            if (UIManager.getBoolean("nb.dark.theme")) {
              // Foreground brightness level taken from the combobox dropdown on Darcula.
              color = new Color(187, 187, 187, 255);
            } else {
              color = new Color(50, 50, 50, 255);
            }
            g.setColor(color);
            for (int i = 0; i < 2; i++) {
                final int y = round((1.4 + 4.1 * i) * scaling);
                final double arrowWidth = round(5.0 * scaling);
                final double arrowHeight = round(3.0 * scaling);
                final double marginX = (width - arrowWidth) / 2.0;
                final double arrowMidX = marginX + arrowWidth / 2.0;
                // Clip the top of the chevrons.
                g.clipRect(0, y, width, height);
                Path2D.Double arrowPath = new Path2D.Double();
                arrowPath.moveTo(arrowMidX - arrowWidth / 2.0, y);
                arrowPath.lineTo(arrowMidX, y + arrowHeight);
                arrowPath.lineTo(arrowMidX + arrowWidth / 2.0, y);
                g.draw(arrowPath);
            }
        }
    }
}
