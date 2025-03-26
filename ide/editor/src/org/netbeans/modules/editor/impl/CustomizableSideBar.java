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

package org.netbeans.modules.editor.impl;

import java.util.logging.Level;
import org.netbeans.modules.editor.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.WeakEventListenerList;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 *  Editor Customizable Side Bar.
 *  Contains components for particular MIME type as defined in XML layer.
 *
 *  @author  Martin Roskanin
 */
public final class CustomizableSideBar {
    
    /**
     * Client property of the Editor component, which constraints sidebars which are attached to the editor.
     * Note that changes to the property after the component was displayed has no effect - sidebars cannot be hidden or shown programmatically.
     */
    public static final String PROP_SELECT_SIDEBAR_LOCATIONS = "nbeditorui.selectSidebarLocations"; // NOI18N

    // -J-Dorg.netbeans.modules.editor.impl.CustomizableSideBar.level=FINE
    private static final Logger LOG = Logger.getLogger(CustomizableSideBar.class.getName());
    
    private static final Map<JTextComponent, Map<SideBarPosition, Reference<JPanel>>> CACHE = new WeakHashMap<JTextComponent, Map<SideBarPosition, Reference<JPanel>>>(5);
    private static final Map<String, WeakEventListenerList> LISTENERS = new HashMap<String, WeakEventListenerList>(5);

    private static final Map<MimePath, Lookup.Result<SideBarFactoriesProvider>> LR = new WeakHashMap<MimePath, Lookup.Result<SideBarFactoriesProvider>>(5);
    private static final Map<Lookup.Result<SideBarFactoriesProvider>, LookupListener> LL = new WeakHashMap<Lookup.Result<SideBarFactoriesProvider>, LookupListener>(5);
    
    private static final String COLOR_WEST_SIDEBARS = "west-sidebars-color"; // NOI18N
    
    private CustomizableSideBar() {
    }

    /** Add weak listener to listen to change of activity of documents or components.
     * The caller must
     * hold the listener object in some instance variable to prevent it
     * from being garbage collected.
     * @param l listener to add
     */
    public static void addChangeListener(String mimeType, ChangeListener l) {
        synchronized (LISTENERS){
            WeakEventListenerList listenerList = (WeakEventListenerList)LISTENERS.get(mimeType);
            if (listenerList == null) {
                listenerList = new WeakEventListenerList();
                LISTENERS.put(mimeType, listenerList);
            }
            listenerList.add(ChangeListener.class, l);
        }
    }

    /** Remove listener for changes in activity. It's optional
     * to remove the listener. It would be done automatically
     * if the object holding the listener would be garbage collected.
     * @param l listener to remove
     */
    public static void removeChangeListener(String mimeType, ChangeListener l) {
        synchronized (LISTENERS){
            WeakEventListenerList listenerList = LISTENERS.get(mimeType);
            if (listenerList != null) {
                listenerList.remove(ChangeListener.class, l);
            }
        }
    }

    private static void fireChange(String mimeType) {
        ChangeListener[] listeners = null;
        
        synchronized (LISTENERS){
            WeakEventListenerList listenerList = LISTENERS.get(mimeType);
            if (listenerList != null) {
                listeners = (ChangeListener[])listenerList.getListeners(ChangeListener.class);
            }
        }

        if (listeners != null && listeners.length > 0) {
            ChangeEvent evt = new ChangeEvent(CustomizableSideBar.class);
            for (ChangeListener l : listeners) {
                l.stateChanged(evt);
            }
        }
    }
    

    public static Map<SideBarPosition, JComponent> getSideBars(JTextComponent target) {
        assert SwingUtilities.isEventDispatchThread() : "Side bars can only be accessed from AWT"; //NOI18N
        return getSideBarsInternal(target);
    }

    public static void resetSideBars(JTextComponent target) {
        synchronized (CACHE) {
            CACHE.put(target, null);
        }
    }

    private static Map<SideBarPosition, JComponent> getSideBarsInternal(JTextComponent target) {
        synchronized (CACHE) {
            Map<SideBarPosition, Reference<JPanel>> panelsMap = CACHE.get(target);
            
            if (panelsMap != null) {
                Map<SideBarPosition, JComponent> map = new HashMap<SideBarPosition, JComponent>();
                
                for(Map.Entry<SideBarPosition, Reference<JPanel>> entry : panelsMap.entrySet()) {
                    SideBarPosition pos = entry.getKey();
                    Reference<JPanel> ref = entry.getValue();
                    if (ref != null) {
                        JPanel panel = ref.get();
                        if (panel != null) {
                            map.put(pos, panel);
                        } else {
                            break;
                        }
                    }
                }
                
                if (map.size() == panelsMap.size()) {
                    // All components from the cache
                    return map;
                }
            }
        }
        
        // Should not run under the lock, see #107056, #107656
        Map<SideBarPosition, List<JComponent>> sideBarsMap = createSideBarsMap(target);

        synchronized (CACHE) {
            Map<SideBarPosition, Reference<JPanel>> panelsMap = new HashMap<SideBarPosition, Reference<JPanel>>();
            Map<SideBarPosition, JComponent> map = new HashMap<SideBarPosition, JComponent>();
            
            for(Map.Entry<SideBarPosition, List<JComponent>> entry : sideBarsMap.entrySet()) {
                SideBarPosition pos = entry.getKey();
                List<JComponent> sideBars = entry.getValue();
                
                JPanel panel = pos.getPosition() == SideBarPosition.WEST ?
                        new WestSidebarHolder(target) :
                        new JPanel();
                panel.setLayout(new BoxLayout(panel, pos.getAxis()));
                
                for(JComponent c : sideBars) {
                    panel.add(c);
                }
                
                panelsMap.put(pos, new WeakReference<JPanel>(panel));
                map.put(pos, panel);
            }

            CACHE.put(target, panelsMap);
            return map;
        }
    }
    
    /**
     * Width of the sidebar separator line
     */
    private static final int SIDEBAR_HOLDER_SEPARATOR_WIDTH = 1;
    
    /**
     * Width of the sidebar's gap which has the same background as the main editor
     */
    private static final int SIDEBAR_GAP_WIDTH = 1;
    
    /**
     * Degenerated "right line border"; displays line at the right only. Pads
     * the line with the editor component's background, so the divisor and editor's
     * graphics (i.e. brace ruler) are separated.
     */
    private static final class WestSidebarHolder extends JPanel implements LookupListener, PropertyChangeListener, Runnable {
        /**
         * The text editor, whose background will be used as a padding
         */
        private final JComponent bkgSource;
        
        private final Lookup.Result<FontColorSettings>   colorResult;
        
        private Color   lineColor;
        
        private Color   textBkColor;
        
        private int     thickness = 1;
        
        private boolean disableBackground = true;
        
        @SuppressWarnings("LeakingThisInConstructor")
        public WestSidebarHolder(JTextComponent target) {
            this.bkgSource = target;
            
            String mimeType = NbEditorUtilities.getMimeType(target);
            colorResult = MimeLookup.getLookup(mimeType).lookupResult(FontColorSettings.class);
            colorResult.addLookupListener(WeakListeners.create(LookupListener.class, this, colorResult));
            bkgSource.addPropertyChangeListener(WeakListeners.propertyChange(this, "background", bkgSource));
            getInsets().set(0, 0, 0, 1);
            setOpaque(true);
        }

        @Override
        public Color getBackground() {
            if (disableBackground) {
                return bkgSource.getBackground();
            } else {
                return super.getBackground();
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            run();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == bkgSource && "background".equals(evt.getPropertyName())) {
                run();
            }
        }

        @Override
        public void run() {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(this);
                return;
            }
            updateUIConfig();
        }

        public int getThickness() {
            return SIDEBAR_HOLDER_SEPARATOR_WIDTH;
        }

        @Override
        public void addNotify() {
            updateUIConfig();
            super.addNotify();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // paint the border:
            paintBorder(this, g, 0, 0, getWidth(), getHeight());
        }
        
        private void updateUIConfig() {
            Iterator<? extends FontColorSettings> it = colorResult.allInstances().iterator();
            if (!it.hasNext()) {
                return;
            }
            disableBackground = false;
            FontColorSettings fcs = it.next();
            Color line;
            Color back;
            
            AttributeSet as = fcs.getFontColors(COLOR_WEST_SIDEBARS);
            if (as == null) {
                // backwards - compatible behaviour: use the line number stuff:
                as = fcs.getFontColors(FontColorNames.LINE_NUMBER_COLORING);
                if (as == null) {
                    // should not happen, except tests
                    back = Color.black;
                } else {
                    back = (Color)as.getAttribute(StyleConstants.Background);
                }
                line = null;
            } else {
                back = (Color)as.getAttribute(StyleConstants.Background);
                line = (Color)as.getAttribute(StyleConstants.Foreground);
            }
            textBkColor = bkgSource.getBackground();
            if (back == null) {
                back = bkgSource.getBackground();
                disableBackground = true;
            }
            setBackground(back);
            if (line == null || line.equals(back)) {
                lineColor = null;
            } else {
                this.lineColor = line;
            }
            // just in case that some line width may have changed
            revalidate();
            repaint();
        }

        @Override
        public Insets getInsets() {
            Insets s = super.getInsets();
            if (lineColor != null) {
                s.right += getThickness();
            }
            s.right += SIDEBAR_GAP_WIDTH;
            return s;
        }
        
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            int thick = getThickness();
            if (!(g instanceof Graphics2D)) {
                return;
            }
            
            Graphics2D g2d = (Graphics2D)g.create();
            if (thick >= 1) {
                g2d.setColor(this.lineColor);
                int x2 = x + width - ((thick +1) / 2);
                g2d.drawLine(x2 - SIDEBAR_GAP_WIDTH, 
                        0, x2 - SIDEBAR_GAP_WIDTH, y + height - 1);
            }
            g2d.setColor(textBkColor);
            int gap = width - SIDEBAR_GAP_WIDTH;
            g2d.drawRect(gap, 0, width - gap, height);
        }
    }
    
    @SuppressWarnings("deprecation")
    private static Map<SideBarPosition, List<JComponent>> createSideBarsMap(JTextComponent target) {
        String mimeType = NbEditorUtilities.getMimeType(target);
        Map<SideBarPosition, List> factoriesMap = getFactoriesMap(mimeType);
        Map<SideBarPosition, List<JComponent>> sideBarsMap = new HashMap<SideBarPosition, List<JComponent>>(factoriesMap.size());
        
        Collection<String> locations = null;
        
        String constraint = (String)target.getClientProperty(PROP_SELECT_SIDEBAR_LOCATIONS);
        if (constraint != null) {
            locations = Arrays.asList(constraint.split(","));
        }
        
        // XXX: We should better let clients to register a regexp filter
        boolean errorStripeOnly = Boolean.TRUE.equals(target.getClientProperty("errorStripeOnly")); //NOI18N

        for(SideBarPosition pos : factoriesMap.keySet()) {
            if (locations != null &&
                !locations.contains(pos.getPositionName())) {
                continue;
            }
            List factoriesList = factoriesMap.get(pos);
            
            // Get sideBars list
            List<JComponent> sideBars = sideBarsMap.get(pos);
            if (sideBars == null) {
                sideBars = new ArrayList<JComponent>();
                sideBarsMap.put(pos, sideBars);
            }
            
            // Create side bars from the factories for this position
            for(Object f : factoriesList) {
                final JComponent sideBar;
                if (f instanceof org.netbeans.editor.SideBarFactory) {
                    sideBar = ((org.netbeans.editor.SideBarFactory)f).createSideBar(target);
                } else if (f instanceof org.netbeans.spi.editor.SideBarFactory) {
                    sideBar = ((org.netbeans.spi.editor.SideBarFactory)f).createSideBar(target);
                } else {
                    LOG.fine("Unexpected sidebar instance: " + f);
                    continue;
                }
                if (sideBar == null) {
                    LOG.fine("Ignoring null side bar created by the factory: " + f); //NOI18N
                    continue;
                }
                
                if (errorStripeOnly && !"errorStripe".equals(sideBar.getName())) { //NOI18N
                    LOG.fine("Error stripe sidebar only. Ignoring '" + sideBar.getName() + "' side bar created by the factory: " + f); //NOI18N
                    continue;
                }

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Created sidebar " + sideBar + "; IHC=" + System.identityHashCode(sideBar) + '\n');
                }
                
                sideBars.add(sideBar);
            }
        }
        
        return sideBarsMap;
    }

    public static Map<SideBarPosition, List> getFactoriesMap(String mimeType) {
        MimePath mimePath = MimePath.parse(mimeType);
        
        Lookup.Result<SideBarFactoriesProvider> lR = LR.get(mimePath);
        if (lR == null) {
            lR = MimeLookup.getLookup(mimePath).lookupResult(SideBarFactoriesProvider.class);
            
            LookupListener listener = LL.get(lR);
            if (listener == null) {
                listener = new MyLookupListener(mimeType);
                LL.put(lR, listener);
            }
            
            lR.addLookupListener(listener);
            LR.put(mimePath, lR);
        }
        
        Collection<? extends SideBarFactoriesProvider> providers = lR.allInstances();
        assert providers.size() == 1 : "There should always be only one SideBarFactoriesProvider; provider-count="
                + providers.size() + ", mimeType=" + mimeType + ", providers: " + providers; //NOI18N

        
        SideBarFactoriesProvider provider = providers.iterator().next();
        return provider.getFactories();
    }
    
    public static final class SideBarPosition {
        public static final int WEST  = 1;
        public static final int NORTH = 2;
        public static final int SOUTH = 3;
        public static final int EAST  = 4;
        
        public static final String WEST_NAME   = "West"; // NOI18N
        public static final String NORTH_NAME  = "North"; // NOI18N
        public static final String SOUTH_NAME  = "South"; // NOI18N
        public static final String EAST_NAME   = "East"; // NOI18N
        
        private int position;
        private boolean scrollable;
        
        SideBarPosition(FileObject fo) {
            Object position = fo.getAttribute("location"); // NOI18N
            if (position == null) {
                // Compatibility:
                position = fo.getAttribute("position"); // NOI18N
            }
            
            if (position instanceof String) {
                String positionName = (String) position;
                
                if (WEST_NAME.equals(positionName)) {
                    this.position = WEST;
                } else {
                    if (NORTH_NAME.equals(positionName)) {
                        this.position = NORTH;
                    } else {
                        if (SOUTH_NAME.equals(positionName)) {
                            this.position = SOUTH;
                        } else {
                            if (EAST_NAME.equals(positionName)) {
                                this.position = EAST;
                            } else {
                                if (Logger.getLogger("global").isLoggable(Level.FINE))
                                    Logger.getLogger("global").log(Level.FINE, "Unsupported position: " + positionName);
                                
                                this.position = WEST;
                            }
                        }
                    }
                }
            } else {
                this.position = WEST;
            }
            
            Object scrollable = fo.getAttribute("scrollable"); // NOI18N
            
            if (scrollable instanceof Boolean) {
                this.scrollable = ((Boolean) scrollable).booleanValue();
            } else {
                this.scrollable = true;
            }
            
            if (this.scrollable && (this.position == SOUTH || this.position == EAST)) {
                if (Logger.getLogger("global").isLoggable(Level.FINE))
                    Logger.getLogger("global").log(Level.FINE, "Unsupported combination: scrollable == true, position=" + getBorderLayoutPosition());
            }
        }
        
        public String getPositionName() {
            switch (position) {
                case EAST:
                    return EAST_NAME;
                case SOUTH:
                    return SOUTH_NAME;
                case WEST:
                    return WEST_NAME;
                case NORTH:
                    return NORTH_NAME;
                default:
                    throw new IllegalArgumentException();
            }
        }
        
        public int hashCode() {
            return scrollable ? position : - position;
        }
        
        public boolean equals(Object o) {
            if (o instanceof SideBarPosition) {
                SideBarPosition p = (SideBarPosition) o;
                
                if (scrollable != p.scrollable)
                    return false;
                
                if (position != p.position)
                    return false;
                
                return true;
            }
            
            return false;
        }
        
        public int getPosition() {
            return position;
        }
        
        private static String[] borderLayoutConstants = new String[] {"", BorderLayout.WEST, BorderLayout.NORTH, BorderLayout.SOUTH, BorderLayout.EAST};
        
        public String getBorderLayoutPosition() {
            return borderLayoutConstants[getPosition()];
        }
        
        private static int[] axisConstants = new int[] {-1, BoxLayout.X_AXIS, BoxLayout.Y_AXIS, BoxLayout.Y_AXIS, BoxLayout.X_AXIS};
        
        private int getAxis() {
            return axisConstants[getPosition()];
        }
        
        public boolean isScrollable() {
            return scrollable;
        }
        
        public String toString() {
            return "[SideBarPosition: scrollable=" + scrollable + ", position=" + position + "]"; // NOI18N
        }
    } // End of SideBarPosition class

    private static class MyLookupListener implements LookupListener {
        private String mimeType;
        
        public MyLookupListener(String mimeType) {
            this.mimeType = mimeType;
        }
        
        public void resultChanged(LookupEvent ev) {
            synchronized (CACHE) {
                ArrayList<JTextComponent> toRemove = new ArrayList<JTextComponent>();
                
                for(JTextComponent jtc : CACHE.keySet()) {
                    String mimeType = NbEditorUtilities.getMimeType(jtc);
                    if (mimeType.equals(this.mimeType)) {
                        toRemove.add(jtc);
                    }
                }
                
                CACHE.keySet().removeAll(toRemove);
            }
            
            fireChange(mimeType);
        }
    } // End of MyLookupListener class

}
