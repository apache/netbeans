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
package org.netbeans.modules.profiler.heapwalk.details.jdk.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.Method;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.heapwalk.details.spi.DetailsProvider;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "Utils_NoDetails=<No details>"
})
final class Utils {
    
    static String getFontName(Instance instance, Heap heap) {
        String name = getFieldString(instance, "name");                         // NOI18N
        if (name == null) {
            Instance font2DHandle = (Instance)instance.getValueOfField(
                    "font2DHandle");                                            // NOI18N
            if (font2DHandle != null) {
                Instance font2D = (Instance)font2DHandle.getValueOfField(
                        "font2D");                                              // NOI18N
                if (font2D != null) {
                    name = getFieldString(instance, "fullName");                // NOI18N
                    if (name == null)
                        name = getFieldString(instance, "nativeFontName");      // NOI18N
                }
            }
        }
        return name;
    }
    
    static String getFieldString(Instance instance, String field) {
        Object _s = instance.getValueOfField(field);
        if (_s instanceof Instance) {
            try {
                Class proxy = Class.forName("org.netbeans.lib.profiler.heap.HprofProxy"); // NOI18N
                Method method = proxy.getDeclaredMethod("getString", Instance.class); // NOI18N
                method.setAccessible(true);
                return (String) method.invoke(proxy, _s);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }
    
    static final class PlaceholderIcon implements Icon {
        
        private final int width;
        private final int height;
        
        PlaceholderIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.WHITE);
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawLine(x, y, x + width - 1, y + height - 1);
            g.drawLine(x, y + height - 1, x + width - 1, y);
        }
        
    }
    
    static final class PlaceholderPanel extends JPanel {
        
        private static final Color LINE =
                          UIManager.getLookAndFeel().getID().equals("Metal") ?  // NOI18N
                          UIManager.getColor("Button.darkShadow") :             // NOI18N
                          UIManager.getColor("Button.shadow");                  // NOI18N
        
        private final JLabel label;
        
        PlaceholderPanel(String className) {
            super(null);
            
            putClientProperty("className", className);
            
            setOpaque(true);
            setBorder(BorderFactory.createLineBorder(LINE));
            
            label = new JLabel(BrowserUtils.getSimpleType(className), JLabel.CENTER);
            label.setOpaque(true);
        }
        
        public void doLayout() {
            Dimension s = getSize();
            Dimension p = label.getPreferredSize();
            
            int x = (s.width - p.width) / 2;
            int y = (s.height - p.height) / 2;
            
            label.setBounds(x, y, p.width, p.height);
        }
        
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(LINE);
            g.drawLine(0, 0, getWidth() - 1, getHeight() - 1);
            g.drawLine(0, getHeight() - 1, getWidth() - 1, 0);
            
            Point p = label.getLocation();
            g.translate(p.x, p.y);
            label.paint(g);
            g.translate(-p.x, -p.y);
        }
        
    }
    
    static final class JPopupMenuImpl extends JPopupMenu {
        
        public void setVisible(boolean visible) {}
        public boolean isVisible() { return true; }

        // Workarounds for best apperance of JPopupMenu preview
        public Component add(Component comp) {
            if (comp instanceof JComponent)
                ((JComponent)comp).setOpaque(false);
            return super.add(comp);
        }
        public void addNotify() {
            super.addNotify();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { repaint(); }
            });
        }
        
    }
    
    abstract static class InstanceBuilder<T> {

        InstanceBuilder(Instance instance, Heap heap) {}

        protected void setupInstance(T instance) {}

        protected T createInstanceImpl() { return null; }
        
        final T createInstance() {
            T instance = createInstanceImpl();
            if (instance != null) setupInstance(instance);
            return instance;
        }

    }
    
    abstract static class View<T extends InstanceBuilder> extends DetailsProvider.View implements Scrollable {
        
        private static final int DASH_SIZE = 20;
        
        private final int margin;
        private final boolean pattern;
        private final boolean stretch;
        
        private Component component;
        private JPanel glassPane;
        
        View(Instance instance, Heap heap) {
            this(10, true, false, instance, heap);
        }
        
        View(int margin, boolean pattern, boolean stretch, Instance instance, Heap heap) {
            super(instance, heap);
            this.margin = margin;
            this.pattern = pattern;
            this.stretch = stretch;
        }
        
        protected T getBuilder(Instance instance, Heap heap) {
            return null;
        }
        
        protected Component getComponent(T builder) {
            return null;
        }
        
        protected void setupGlassPane(JPanel glassPane) {}
        
        protected final void computeView(Instance instance, Heap heap) {
            final T builder = getBuilder(instance, heap);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    removeAll();
                    component = builder == null ? null : getComponent(builder);
                    if (component != null) {
                        component.setVisible(true);
                        if (stretch) {
                            add(component, BorderLayout.CENTER);
                        } else {
                            glassPane = new JPanel(null) {
                                public Point getToolTipLocation(MouseEvent event) {
                                    Point p = event.getPoint();
                                    p.translate(15, 15);
                                    return p;
                                }
                            };
                            glassPane.setOpaque(false);
                            glassPane.addMouseListener(new MouseAdapter() {});
                            glassPane.addMouseMotionListener(new MouseMotionAdapter() {});
                            glassPane.addKeyListener(new KeyAdapter() {});
                            setupGlassPane(glassPane);
                            add(glassPane);
                            
                            setLayout(null);
                            add(component);
                        }
                        if (component.getWidth() == 0 || component.getHeight() == 0)
                            component.setSize(component.getPreferredSize());
                        Dimension d = component.getSize();
                        d.width += margin;
                        d.height += margin;
                        setPreferredSize(d);
                        setBackground(UIUtils.getProfilerResultsBackground());
                        setForeground(UIUtils.getDarker(getBackground()));
                    } else {
                        component = new JLabel(Bundle.Utils_NoDetails(), JLabel.CENTER);
                        component.setEnabled(false);
                        add(component, BorderLayout.CENTER);
                    }
                    revalidate();
                    doLayout();
                    repaint();
                }
            });
        }
        
        public void doLayout() {
            if (getLayout() != null) {
                super.doLayout();
            } else {
                Dimension size = getSize();
                Dimension comp = component.getSize();
                
                int x = comp.width >= size.width ? 0 :
                        (size.width - comp.width) / 2;
                int y = comp.height >= size.height ? 0 :
                        (size.height - comp.height) / 2;
                
                component.move(x, y); // required to correctly setup JPopupMenu
                
                glassPane.setBounds(component.getBounds());
            }
        }
        
        protected void paintComponent(Graphics g) {
            if (!pattern || component == null) {
                super.paintComponent(g);
            } else {
                int x = 0;
                int y = 0;
                int w = getWidth();
                int h = getHeight();
                
                while (y <= h) {
                    boolean flag = (y / DASH_SIZE) % 2 == 0;
                    while (x <= w) {
                        g.setColor(flag ? getBackground() : getForeground());
                        g.fillRect(x, y, DASH_SIZE, DASH_SIZE);
                        x += DASH_SIZE;
                        flag = !flag;
                    }
                    x = 0;
                    y += DASH_SIZE;
                }
            }
        }
        
        public Dimension getPreferredScrollableViewportSize() {
            return null;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            // Scroll almost one screen
            Container parent = getParent();
            if ((parent == null) || !(parent instanceof JViewport)) return 50;
            return (int)(((JViewport)parent).getHeight() * 0.95f);
        }

        public boolean getScrollableTracksViewportHeight() {
            // Allow dynamic vertical enlarging of the panel but request the vertical scrollbar when needed
            Container parent = getParent();
            if ((parent == null) || !(parent instanceof JViewport)) return false;
            return getPreferredSize().height < ((JViewport)parent).getHeight();
        }

        public boolean getScrollableTracksViewportWidth() {
            // Allow dynamic horizontal enlarging of the panel but request the vertical scrollbar when needed
            Container parent = getParent();
            if ((parent == null) || !(parent instanceof JViewport)) return false;
            return getPreferredSize().width < ((JViewport)parent).getWidth();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }
        
    }
    
}
