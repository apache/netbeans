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
package org.netbeans.modules.debugger.jpda.visual.ui;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.modules.debugger.jpda.visual.JavaComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.options.Options;
import org.netbeans.modules.debugger.jpda.visual.spi.ComponentInfo;
import org.netbeans.modules.debugger.jpda.visual.spi.RemoteScreenshot;
import org.netbeans.modules.debugger.jpda.visual.spi.ScreenshotUIManager;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.SaveAction;
import org.openide.awt.Actions;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * This component draws the screenshot of a remote application.
 * 
 * @author Martin Entlicher
 */
public class ScreenshotComponent extends TopComponent {
    
    private static final Logger logger = Logger.getLogger(ScreenshotComponent.class.getName());
    
    private static final Map<DebuggerEngine, Set<ScreenshotComponent>> openedScreenshots =
                         new HashMap<DebuggerEngine, Set<ScreenshotComponent>>();
    private static volatile ScreenshotComponent activeScreenshotComponent;
    
    private static final String[] ZOOM_PERCENTS = { "10%", "25%", "50%", "75%", "100%", "150%", "200%", "300%" }; // NOI18N
    private static final String PROP_ZOOM = "zoom";     // NOI18N
    @StaticResource
    private static final String HIERARCHY_CHANGES_ICON = "org/netbeans/modules/debugger/jpda/visual/resources/hierarchy.png";   // NOI18N
    
    private RemoteScreenshot screenshot;
    private ScreenshotUIManager manager;
    private NavigatorLookupHint componentHierarchyNavigatorHint = new ComponentHierarchyNavigatorHint();
    private ComponentNode componentNodes;
    private ScreenshotCanvas canvas;
    private JScrollPane scrollPane;
    private boolean propertiesOpened;
    
    public ScreenshotComponent(RemoteScreenshot screenshot, ScreenshotUIManager manager) {
        this.screenshot = screenshot;
        this.manager = manager;
        setFocusable(true);
        screenshot.getImage();
        ScreenshotCanvas c = new ScreenshotCanvas(screenshot.getImage());
        this.canvas = c;
        scrollPane = new JScrollPane(c);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        JToolBar toolBar = createToolBar();
        add(toolBar, BorderLayout.NORTH);
        String title = screenshot.getTitle();
        title = (title == null) ? NbBundle.getMessage(ScreenshotComponent.class, "LBL_DebuggerSnapshot") :
                    NbBundle.getMessage(ScreenshotComponent.class, "LBL_DebuggerSnapshotOf", title);
        setDisplayName(title);
        componentNodes = new ComponentNode(screenshot.getComponentInfo());
        ComponentHierarchy.getInstance().getExplorerManager().setRootContext(componentNodes);
        ComponentInfo firstCi = getFirstCustomComponent(componentNodes);
        if (firstCi != null) {
            c.listener.selectComponent(firstCi, false);
        } else {
            setActivatedNodes(new Node[] { componentNodes });
        }
        addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                switch (c) {
                    case '+':
                    case '=': canvas.zoomIn();
                              break;
                    case '-':
                    case '_': canvas.zoomOut();
                              break;
                    case '1': canvas.zoom(1.0);
                              break;
                    case '2': canvas.zoom(2.0);
                              break;
                    case '3': canvas.zoom(3.0);
                              break;
                    case '4': canvas.zoom(4.0);
                              break;
                    case '7': canvas.zoom(1.0/4.0);
                              break;
                    case '8': canvas.zoom(1.0/3.0);
                              break;
                    case '9': canvas.zoom(1.0/2.0);
                              break;
                    case 'X':
                    case 'x': canvas.zoomFit();
                              break;
                }
            }
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        });
    }
    
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar(NbBundle.getMessage(ScreenshotComponent.class, "CTL_ToolBarName"));
        toolBar.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ScreenshotComponent.class, "CTL_ToolBarA11yName"));
        toolBar.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ScreenshotComponent.class, "CTL_ToolBarA11yDescr"));
        toolBar.add(createZoomInButton());
        toolBar.add(createZoomOutButton());
        toolBar.addSeparator();
        toolBar.add(createZoomOrigButton());
        toolBar.addSeparator();
        toolBar.add(createZoomFitButton());
        toolBar.addSeparator();
        toolBar.add(createZoomPercent());
        toolBar.addSeparator();
        toolBar.add(createSaveButton());
        toolBar.addSeparator();
        toolBar.add(createHierarchyChangesButton());
        toolBar.addSeparator();
        return toolBar;
    }
    
    private JButton createZoomInButton() {
        JButton inButton = new JButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/debugger/jpda/visual/resources/zoomIn.gif")));
        inButton.setToolTipText(NbBundle.getMessage(ScreenshotComponent.class, "TLTP_ZoomIn"));
        inButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ScreenshotComponent.class, "LBL_ZoomInA11yDescr"));
        inButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.zoomIn();
            }
        });
        inButton.setAlignmentX(CENTER_ALIGNMENT);
        return inButton;
    }
    
    private JButton createZoomOutButton() {
        JButton outButton = new JButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/debugger/jpda/visual/resources/zoomOut.gif")));
        outButton.setToolTipText(NbBundle.getMessage(ScreenshotComponent.class, "TLTP_ZoomOut"));
        outButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ScreenshotComponent.class, "LBL_ZoomOutA11yDescr"));
        outButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.zoomOut();
            }
        });
        outButton.setAlignmentX(CENTER_ALIGNMENT);
        return outButton;
    }
    
    private JButton createZoomOrigButton() {
        JButton origButton = new JButton(NbBundle.getMessage(ScreenshotComponent.class, "LBL_ZoomOrig"));
        origButton.setToolTipText(NbBundle.getMessage(ScreenshotComponent.class, "TLTP_ZoomOrig"));
        origButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ScreenshotComponent.class, "LBL_ZoomOrigA11yDescr"));
        origButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.zoom(1);
            }
        });
        origButton.setAlignmentX(CENTER_ALIGNMENT);
        return origButton;
    }
    
    private JButton createZoomFitButton() {
        JButton fitButton = new JButton(NbBundle.getMessage(ScreenshotComponent.class, "LBL_ZoomFit"));
        fitButton.setToolTipText(NbBundle.getMessage(ScreenshotComponent.class, "TLTP_ZoomFit"));
        fitButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ScreenshotComponent.class, "LBL_ZoomFitA11yDescr"));
        fitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.zoomFit();
            }
        });
        fitButton.setAlignmentX(CENTER_ALIGNMENT);
        return fitButton;
    }
    
    private JComboBox createZoomPercent() {
        final JComboBox cb = new JComboBox(ZOOM_PERCENTS);
        final boolean[] ignoreChanges = { false };
        cb.setEditable(true);
        Font font = cb.getEditor().getEditorComponent().getFont();
        int textWidth = 0;
        FontMetrics fm = cb.getEditor().getEditorComponent().getFontMetrics(font);
        for (String zp : ZOOM_PERCENTS) {
            int sw = fm.stringWidth(zp);
            textWidth = Math.max(textWidth, sw);
        }
        for (Component c : cb.getComponents()) {
            if (c instanceof AbstractButton) {
                AbstractButton ab = (AbstractButton) c;
                Insets insets = ab.getInsets();
                int buttonWidth = insets.left +
                                  Math.max(c.getMinimumSize().width, c.getPreferredSize().width) +
                                  insets.right;
                Dimension dim = new Dimension(buttonWidth + textWidth + 10, cb.getPreferredSize().height);
                cb.setPreferredSize(dim);
                cb.setMinimumSize(dim);
                cb.setMaximumSize(dim);
                break;
            }
        }
        //cb.setPreferredSize(new Dimension(textWidth, cb.getPreferredSize().height));
        cb.setPrototypeDisplayValue(ZOOM_PERCENTS[ZOOM_PERCENTS.length - 1]);
        cb.setMaximumSize(cb.getPreferredSize());
        cb.setToolTipText(NbBundle.getMessage(ScreenshotComponent.class, "TLTP_ZoomPercent"));
        cb.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ScreenshotComponent.class, "LBL_ZoomPercentA11yDescr"));
        double scale = canvas.getScale();
        int pc = (int) (scale * 100);
        String str = Integer.toString(pc) + '%';
        cb.setSelectedItem(str);
        cb.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                Component child = e.getChild();
                if (child instanceof AbstractButton) {
                    int buttonWidth = child.getMinimumSize().width;
                    Font font = cb.getEditor().getEditorComponent().getFont();
                    int textWidth = cb.getEditor().getEditorComponent().getFontMetrics(font).stringWidth(ZOOM_PERCENTS[ZOOM_PERCENTS.length - 1]);
                    int width = buttonWidth + textWidth + 10;
                    Dimension dim = new Dimension(width, cb.getPreferredSize().height);
                    cb.setPreferredSize(dim);
                    cb.setMinimumSize(dim);
                    cb.setMaximumSize(dim);
                    cb.removeContainerListener(this);
                }
            }
            @Override
            public void componentRemoved(ContainerEvent e) {}
        });
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ignoreChanges[0]) return;
                String selected = (String) cb.getSelectedItem();
                selected = selected.trim();
                if (selected.endsWith("%")) {
                    selected = selected.substring(0, selected.length() - 1);
                }
                int pc;
                try {
                    pc = Integer.parseInt(selected);
                    double scale = pc / 100.0;
                    canvas.setScale(scale);
                } catch (NumberFormatException nfex) {
                    // Wrong value - set the current one.
                    pc = (int) (canvas.getScale() * 100);
                    String str = Integer.toString(pc) + '%';
                    ignoreChanges[0] = true;
                    try {
                        cb.setSelectedItem(str);
                    } finally {
                        ignoreChanges[0] = false;
                    }
                }
            }
        });
        canvas.addPropertyChangeListener(PROP_ZOOM, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                double newScale = (Double) evt.getNewValue();
                int pc = (int) (newScale * 100);
                String str = Integer.toString(pc) + '%';
                if (!str.equals(cb.getSelectedItem())) {
                    ignoreChanges[0] = true;
                    try {
                        cb.setSelectedItem(str);
                    } finally {
                        ignoreChanges[0] = false;
                    }
                }
            }
        });
        cb.setAlignmentX(CENTER_ALIGNMENT);
        return cb;
    }
    
    private JButton createSaveButton() {
        SaveAction sa = SaveAction.get(SaveAction.class);
        Action saveAction = sa.createContextAwareInstance(Lookups.singleton(new ScreenshotSavable()));
        JButton jb = new JButton();
        Actions.connect(jb, saveAction);
        return jb;
    }
    
    @NbBundle.Messages({"TT_TrackingComponentChanges=Tracking locations of component hierarchy changes (may have impact on application performance)",
                        "TTL_TrackComponentChangesConfirm=Confirm Tracking of Component Hierarchy Changes",
                        "MSG_TrackComponentChangesConfirm=Tracking of component hierarchy changes allows you to "+
                                "navigate to the place where components were added to their containers.\n"+
                                "The tracking may have an impact on performace of the application.\n"+
                                "Turn on the tracking? You may need to restart the application to take an effect."})
    private JToggleButton createHierarchyChangesButton() {
        final JToggleButton jtb = new JToggleButton(ImageUtilities.loadImageIcon(HIERARCHY_CHANGES_ICON, false));
        boolean tcc = Options.isTrackComponentChanges();
        jtb.setSelected(tcc);
        jtb.setToolTipText(Bundle.TT_TrackingComponentChanges());
        jtb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean tcc = jtb.isSelected();
                if (tcc) {
                    NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                            Bundle.MSG_TrackComponentChangesConfirm(),
                            Bundle.TTL_TrackComponentChangesConfirm(),
                            NotifyDescriptor.YES_NO_OPTION
                    );
                    Object result = DialogDisplayer.getDefault().notify(confirmation);
                    if (result != NotifyDescriptor.YES_OPTION) {
                        jtb.setSelected(false);
                        return ;
                    }
                }
                Options.setTrackComponentChanges(tcc);
            }
        });
        return jtb;
    }
    
    private static ComponentInfo getFirstCustomComponent(Node node) {
        ComponentInfo ci = node.getLookup().lookup(ComponentInfo.class);
        if (ci instanceof JavaComponentInfo && ((JavaComponentInfo) ci).isCustomType()) {
            return ci;
        } else {
            Node[] nodes = node.getChildren().getNodes();
            for (Node n : nodes) {
                ComponentInfo fci = getFirstCustomComponent(n);
                if (fci != null) {
                    return fci;
                }
            }
        }
        return null;
    }
    
    public static ScreenshotComponent getActive() {
        return activeScreenshotComponent;
        /*
        WindowManager wm = WindowManager.getDefault();
        synchronized (openedScreenshots) {
            for (Set<ScreenshotComponent> ssc : openedScreenshots.values()) {
                for (ScreenshotComponent sc : ssc) {
                    Mode m = wm.findMode(sc);
                    if (m != null && m.getSelectedTopComponent() == sc) {
                        return sc;
                    }
                }
            }
        }
        return null;
         */
    }
    
    public ScreenshotUIManager getManager() {
        return manager;
    }
    
    public ComponentInfo getSelectedComponent() {
        Node[] nodes = getActivatedNodes();
        if (nodes.length > 0) {
            return nodes[0].getLookup().lookup(ComponentInfo.class);
        }
        return null;
    }

    @Override
    public Lookup getLookup() {
        Lookup lookup = super.getLookup();
        return new ProxyLookup(lookup, Lookups.singleton(componentHierarchyNavigatorHint));
    }

    @Override
    protected void componentActivated() {
        logger.fine("componentActivated() root = "+componentNodes+", ci = "+componentNodes.getLookup().lookup(ComponentInfo.class));
        activeScreenshotComponent = this;
        ComponentHierarchy.getInstance().getExplorerManager().setRootContext(componentNodes);
        ComponentHierarchy.getInstance().getExplorerManager().setExploredContext(componentNodes);
        canvas.activated();
    }

    @Override
    protected void componentDeactivated() {
        super.componentDeactivated();
        //canvas.deactivated();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing();
        TopComponent properties = WindowManager.getDefault().findTopComponent("properties");    // NOI18N
        if (properties != null) {
            if (!properties.isOpened()) {
                propertiesOpened = true;
                properties.open();
            }
        }
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden();
        if (propertiesOpened) {
            propertiesOpened = false;
            TopComponent properties = WindowManager.getDefault().findTopComponent("properties");    // NOI18N
            if (properties != null) {
                properties.close();
            }
        }
    }
    
    @Override
    protected void componentOpened() {
        synchronized (openedScreenshots) {
            Set<ScreenshotComponent> components = openedScreenshots.get(screenshot.getDebuggerEngine());
            if (components == null) {
                components = new HashSet<ScreenshotComponent>();
                openedScreenshots.put(screenshot.getDebuggerEngine(), components);
            }
            components.add(this);
        }
    }
    
    @Override
    protected void componentClosed() {
        if (activeScreenshotComponent == this) {
            activeScreenshotComponent = null;
        }
        synchronized (openedScreenshots) {
            Set<ScreenshotComponent> components = openedScreenshots.get(screenshot.getDebuggerEngine());
            if (components != null) {
                components.remove(this);
                if (components.isEmpty()) {
                    openedScreenshots.remove(screenshot.getDebuggerEngine());
                }
            }
        }
        canvas.deactivated();
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public static void closeScreenshots(DebuggerEngine engine) {
        synchronized (openedScreenshots) {
            Set<ScreenshotComponent> components = openedScreenshots.get(engine);
            if (components != null) {
                final Set<ScreenshotComponent> theComponents = new HashSet<ScreenshotComponent>(components);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        for (ScreenshotComponent c : theComponents) {
                            c.close();
                        }
                    }
                });
            }
        }
    }
    
    public void markBreakpoint(ComponentInfo ci) {
        canvas.markBreakpoint(ci);
    }
    
    public void unmarkBreakpoint(ComponentInfo ci) {
        canvas.unmarkBreakpoint(ci);
    }
    
    private class ScreenshotCanvas extends JComponent {
        
        private Image image;
        private Rectangle selection;
        private final Set<Rectangle> breakpoints = new HashSet<Rectangle>();
        private Listener listener;
        private boolean active;
        private double scale = 1D;
        private final double SCALLE_FACTOR = Math.sqrt(2.0D);  // Treated as static
        
        public ScreenshotCanvas(Image image) {
            this.image = image;
            listener = new Listener();
            updateSize();
        }
        
        private void updateSize() {
            ImageObserver io = new ImageObserver() {
                @Override
                public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                    boolean hasHeight = (infoflags & ImageObserver.HEIGHT) > 0;
                    boolean hasWidth = (infoflags & ImageObserver.WIDTH) > 0;
                    if (!hasHeight || !hasWidth) {
                        return true;
                    }
                    ScreenshotCanvas.this.setSize(width, height);
                    ScreenshotCanvas.this.setPreferredSize(ScreenshotCanvas.this.getSize());
                    return false;
                }
            };
            int width = (int) (scale * image.getWidth(io)) + 2;
            int height = (int) (scale * image.getHeight(io)) + 2;
            if (width > 0 && height > 0) {
                setSize(width, height);
                setPreferredSize(getSize());
            }
        }

        @Override
        public void paintComponent(Graphics g) {
            int imageWidth = image.getWidth(this);
            int imageHeight = image.getHeight(this);
            int scaledWidth = (int) (scale * imageWidth);
            int scaledHeight = (int) (scale * imageHeight);
            g.drawImage(image, 1, 1, scaledWidth + 1, scaledHeight + 1,
                        0, 0, imageWidth, imageHeight, this);
            g.drawRect(0, 0, scaledWidth + 2, scaledHeight + 2);
            synchronized (breakpoints) {
                Color c = g.getColor();
                g.setColor(Color.RED);
                for (Rectangle r : breakpoints) {
                    g.drawRect((int) (scale * r.x), (int) (scale * r.y),
                               (int) (scale * r.width) + 1, (int) (scale * r.height) + 1);
                }
                g.setColor(c);
            }
            if (selection != null) {
                Color c = g.getColor();
                g.setColor(Color.BLUE);
                g.drawRect((int) (scale * selection.x), (int) (scale * selection.y),
                           (int) (scale * selection.width) + 1, (int) (scale * selection.height) + 1);
                g.setColor(c);
            }
            //image.getSource();
        }
        
        void activated() {
            if (selection != null) {
                listener.selectComponentAt((int) (scale * selection.x) + 1, (int) (scale * selection.y) + 1, true);
            }
            if (!active) {
                addMouseListener(listener);
                ComponentHierarchy.getInstance().getExplorerManager().addPropertyChangeListener(listener);
            }
            active = true;
        }
        
        void deactivated() {
            if (!active) return ;
            active = false;
            removeMouseListener(listener);
            ComponentHierarchy.getInstance().getExplorerManager().removePropertyChangeListener(listener);
        }
        
        private Rectangle getBreakpointRectangle(ComponentInfo ci) {
            Rectangle r = ci.getWindowBounds();
            return new Rectangle(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
        }

        private void markBreakpoint(ComponentInfo ci) {
            Rectangle r;
            synchronized (breakpoints) {
                r = getBreakpointRectangle(ci);
                breakpoints.add(r);
            }
            repaint(r.x, r.y, r.width + 3, r.height + 3);
        }

        private void unmarkBreakpoint(ComponentInfo ci) {
            Rectangle r;
            synchronized (breakpoints) {
                r = getBreakpointRectangle(ci);
                breakpoints.remove(r);
            }
            repaint(r.x, r.y, r.width + 3, r.height + 3);
        }
        
        public void zoomIn() {
            setScale(scale * SCALLE_FACTOR);
        }
        
        public void zoomOut() {
            double newScale = scale / SCALLE_FACTOR;
            if (newScale * image.getWidth(null) < 1 && newScale * image.getHeight(null) < 1) {
                // Too small to further zoom out
                return ;
            }
            setScale(newScale);
        }
        
        public void zoomFit() {
            Rectangle bounds = scrollPane.getViewportBorderBounds();
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            if (imageWidth <= 0 || imageHeight <= 0) {
                // nothing known yet
                return;
            }
            double scalew = ((double) bounds.width)/(imageWidth + 2);
            double scaleh = ((double) bounds.height)/(imageHeight + 2);
            setScale(Math.min(scalew, scaleh));
        }
        
        public void zoom(double newScale) {
            int imageWidth = image.getWidth(null);
            int imageHeight = image.getHeight(null);
            if (imageWidth <= 0 || imageHeight <= 0) {
                // nothing known yet
                return;
            }
            if (newScale * imageWidth < 1 && newScale * imageHeight < 1) {
                newScale = Math.min(((double) 1)/imageWidth, ((double) 1)/imageHeight);
            }
            setScale(newScale);
        }
        
        private void setScale(double scale) {
            if (this.scale != scale) {
                Point2D.Double center = computeCenterPoint();
                double oldScale = this.scale;
                this.scale = scale;
                updateSize();
                setCenterPoint(center);
                repaint();
                firePropertyChange(PROP_ZOOM, oldScale, scale);
            }
        }
        
        public double getScale() {
            return scale;
        }
        
        /** Computes the current image point, that is in the center in the current view area. */
        private Point2D.Double computeCenterPoint() {
            Point p = scrollPane.getViewport().getViewPosition();
            Rectangle viewRect = scrollPane.getViewport().getViewRect();
            p.x += viewRect.width/2;
            p.y += viewRect.height/2;
            return new Point2D.Double(p.x / scale, p.y / scale);
        }
        
        /** Sets the current view area to have the image point in it's center. */
        private void setCenterPoint(Point2D.Double pd) {
            Rectangle viewRect = scrollPane.getViewport().getViewRect();
            Point p = new Point((int) (pd.x * scale), (int) (pd.y * scale));
            p.x -= viewRect.width/2;
            if (p.x < 0) p.x = 0;
            p.y -= viewRect.height/2;
            if (p.y < 0) p.y = 0;
            scrollPane.getViewport().setViewPosition(p);
        }
        
        void save(File file) throws IOException {
            ImageWriter iw = null;
            String name = file.getName();
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                String extension = name.substring(i + 1);
                Iterator<ImageWriter> imageWritersBySuffix = ImageIO.getImageWritersBySuffix(extension);
                if (imageWritersBySuffix.hasNext()) {
                    iw = imageWritersBySuffix.next();
                }
            }
            if (iw != null) {
                file.delete();
                ImageOutputStream ios = ImageIO.createImageOutputStream(file);
                iw.setOutput(ios);
                try {
                    iw.write((BufferedImage) image);
                } finally {
                    iw.dispose();
                    ios.flush();
                    ios.close();
                }
            } else {
                ImageIO.write((BufferedImage) image, "PNG", file);
            }
        }
        
        private class Listener implements MouseListener, PropertyChangeListener {

            @Override
            public void mouseClicked(MouseEvent e) {
                //selectComponentAt(e.getX(), e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    selectComponentAt(e.getX(), e.getY(), false);
                    showPopupMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectComponentAt(e.getX(), e.getY(), false);
                if (e.isPopupTrigger()) {
                    showPopupMenu(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
            
            private void showPopupMenu(int x, int y) {
                Node[] activatedNodes = getActivatedNodes();
                if (activatedNodes.length == 1) {
                    Action[] actions = activatedNodes[0].getActions(true);
                    JPopupMenu contextMenu = Utilities.actionsToPopup(actions, ScreenshotComponent.this);
                    contextMenu.show(ScreenshotComponent.this.canvas, x, y);
                }
            }
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                logger.fine("propertyChange("+evt+") propertyName = "+evt.getPropertyName());
                String propertyName = evt.getPropertyName();
                if (ExplorerManager.PROP_SELECTED_NODES.equals(propertyName)) {
                    Node[] nodes = ComponentHierarchy.getInstance().getExplorerManager().getSelectedNodes();
                    ComponentInfo ci = null;
                    if (nodes.length > 0) {
                        ci = nodes[0].getLookup().lookup(ComponentInfo.class);
                    }
                    logger.fine("nodes = "+Arrays.toString(nodes)+" => selectComponent("+ci+")");
                    selectComponent(ci, false);
                } else if (ExplorerManager.PROP_ROOT_CONTEXT.equals(propertyName)) {
                    deactivated();
                }
            }
            
            private void selectComponentAt(int x, int y, boolean reActivated) {
                x -= 1;
                x = (int) (x / scale);
                y -= 1;
                y = (int) (y / scale);
                ComponentInfo ci = screenshot.findAt(x, y);
                logger.fine("Component Info at "+x+", "+y+" is: "+((ci != null) ? ci.getDisplayName() : null));
                selectComponent(ci, reActivated);
            }
            
            private void selectComponent(ComponentInfo ci, boolean reActivated) {
                Node node = null;
                if (ci != null) {
                    Rectangle oldSelection = null;
                    if (selection != null) {
                        oldSelection = selection;
                    }
                    selection = ci.getWindowBounds();
                    if (oldSelection != null) {
                        if (oldSelection.equals(selection) && !reActivated) {
                            return ; // already selected
                        }
                        repaint((int) (scale * oldSelection.x), (int) (scale * oldSelection.y),
                                (int) (scale * oldSelection.width) + 3, (int) (scale * oldSelection.height) + 3);
                    }
                    repaint((int) (scale * selection.x), (int) (scale * selection.y),
                            (int) (scale * selection.width) + 3, (int) (scale * selection.height) + 3);
                    logger.fine("New selection = "+selection);
                    node = componentNodes.findNodeFor(ci);
                    logger.fine("FindNodeFor("+ci+") on '"+componentNodes+"' gives: "+node);
                }
                Node[] nodes;
                if (node != null) {
                    nodes = new Node[] { node };
                } else {
                    nodes = new Node[] {};
                }
                logger.fine("setActivated/SelectedNodes("+Arrays.toString(nodes)+")");
                setActivatedNodes(nodes);
                try {
                    ComponentHierarchy.getInstance().getExplorerManager().setSelectedNodes(nodes);
                } catch (PropertyVetoException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
        
    }
    
    @NbBundle.Messages({"# {0} - The file name to overwrite",
                        "MSG_Overwrite=Do you want to overwrite {0}?",
                        "CTL_ImageFiles=Image Files"})
    private class ScreenshotSavable implements Savable {

        @Override
        public void save() throws IOException {
            FileChooserBuilder fchb = new FileChooserBuilder(ScreenshotComponent.class);
            String[] writerFileSuffixes = ImageIO.getWriterFileSuffixes();
            fchb.setFileFilter(new FileNameExtensionFilter(Bundle.CTL_ImageFiles(), writerFileSuffixes));
            File file = fchb.showSaveDialog();
            if (file != null) {
                if (file.exists()) {
                    NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Bundle.MSG_Overwrite(file.getName()), toString());
                    Object doOverwrite = DialogDisplayer.getDefault().notify(nd);
                    if (!NotifyDescriptor.YES_OPTION.equals(doOverwrite)) {
                        return ;
                    }
                }
                canvas.save(file);
            }
        }

        @Override
        public String toString() {
            return ScreenshotComponent.this.getDisplayName();
        }
        
    }
    
}
