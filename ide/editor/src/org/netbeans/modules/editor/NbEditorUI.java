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

package org.netbeans.modules.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.accessibility.Accessible;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.windows.TopComponent;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.StatusBar;
import org.netbeans.modules.editor.impl.CustomizableSideBar;
import org.netbeans.modules.editor.impl.CustomizableSideBar.SideBarPosition;
import org.netbeans.modules.editor.impl.StatusLineFactories;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
* Editor UI
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorUI extends EditorUI {

    private FocusListener focusL;

    private boolean attached = false;
    private ChangeListener listener;
    
    /**
     *
     * @deprecated - use {@link #attachSystemActionPerformer(String)} instead
     */
    @Deprecated
    protected SystemActionUpdater createSystemActionUpdater(
        String editorActionName, boolean updatePerformer, boolean syncEnabling) {
        return new SystemActionUpdater(editorActionName, updatePerformer, syncEnabling);
    }

    private static final RequestProcessor WORKER = new RequestProcessor(NbEditorUI.class.getName(), 1, false, false);
    private static final LinkedHashSet<FileObject> objectsToRefresh = new LinkedHashSet<>();
    private static final Object lock = new Object();
    private static final RequestProcessor.Task TASK = WORKER.create(new Runnable() {
        @Override
        public void run() {
            FileObject fo;
            do {
                synchronized (lock) {
                    // let's be fair - get 1-st object
                    Iterator<FileObject> iterator = objectsToRefresh.iterator();
                    if (iterator.hasNext()) {
                        fo = iterator.next();
                        objectsToRefresh.remove(fo);
                    } else {
                        fo = null;
                    }
                }
                if (fo != null) {
                    fo.refresh();
                }
            } while (fo != null);
        }
    });
    
    public NbEditorUI() {
        focusL = new FocusAdapter() {
            public @Override void focusGained(FocusEvent evt) {
                // Refresh file object when component made active
                Document doc = getDocument();
                if (doc != null) {
                    DataObject dob = NbEditorUtilities.getDataObject(doc);
                    if (dob != null) {
                        final FileObject fo = dob.getPrimaryFile();
                        if (fo != null) {
                            // Fixed #48151 - posting the refresh outside of AWT thread
                            synchronized (lock) {
                                objectsToRefresh.add(fo);
                            }
                            TASK.schedule(0);
                        }
                    }
                }

//                // Check if editor is docked and if so then use global status bar.
//                JTextComponent component = getComponent();
//                // Check if component is inside main window
//                boolean underMainWindow = (SwingUtilities.isDescendingFrom(component,
//                WindowManager.getDefault().getMainWindow()));
//                getStatusBar().setVisible(!underMainWindow); // Note: no longer checking the preferences settting
            }

//            @Override
//            public void focusLost(FocusEvent e) {
//                // Clear global panel
//                StatusLineFactories.clearStatusLine();
//            }


        };
    }
    
    
    private static Lookup getContextLookup(java.awt.Component component){
        Lookup lookup = null;
        for (java.awt.Component c = component; c != null; c = c.getParent()) {
            if (c instanceof Lookup.Provider) {
                lookup = ((Lookup.Provider)c).getLookup ();
                if (lookup != null) {
                    break;
                }
            }
        }
        return lookup;
    }
    
    protected void attachSystemActionPerformer(String editorActionName){
        new NbEditorUI.SystemActionPerformer(editorActionName);
    }

    protected @Override void installUI(JTextComponent c) {
        super.installUI(c);

        if (!attached){
            attachSystemActionPerformer("find");
            attachSystemActionPerformer("replace");
            attachSystemActionPerformer(ExtKit.gotoAction);
            attachSystemActionPerformer(ExtKit.showPopupMenuAction);

            // replacing DefaultEditorKit.deleteNextCharAction by BaseKit.removeSelectionAction
            // #41223
            // attachSystemActionPerformer(BaseKit.removeSelectionAction);
            
            attached = true;
        }
        
        c.addFocusListener(focusL);
    }


    protected @Override void uninstallUI(JTextComponent c) {
        super.uninstallUI(c);

        c.removeFocusListener(focusL);
    }

    @Override
    protected int textLimitWidth() {
        Document doc = getDocument();
        if (doc != null) {
            int textLimit = CodeStylePreferences.get(doc).getPreferences().
                    getInt(SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
            if (textLimit > 0) {
                return textLimit;
            }
        }
        return super.textLimitWidth();
    }

    @Override
    protected JComponent createExtComponent() {

        JTextComponent component = getComponent();
        
        JLayeredPane layers = new LayeredEditorPane(component);
        layers.add(component, JLayeredPane.DEFAULT_LAYER, 0);
//        MyInternalFrame window = new MyInternalFrame();
//        layers.add(window, JLayeredPane.PALETTE_LAYER);
//        window.show();

        // Add the scroll-pane with the component to the center
        JScrollPane scroller = new JScrollPane(layers);
        scroller.getViewport().setMinimumSize(new Dimension(4,4));

        // remove default scroll-pane border, winsys will handle borders itself 
        Border empty = BorderFactory.createEmptyBorder();
        // Important:  Do not delete or use null instead, will cause
        //problems on GTK L&F.  Must set both scroller border & viewport
        //border! - Tim
        scroller.setBorder(empty);
        scroller.setViewportBorder(empty);
        
        if (component.getClientProperty("nbeditorui.vScrollPolicy") != null) {
            scroller.setVerticalScrollBarPolicy(
                    (Integer)component.getClientProperty("nbeditorui.vScrollPolicy"));
        }
        if (component.getClientProperty("nbeditorui.hScrollPolicy") != null) {
            scroller.setHorizontalScrollBarPolicy(
                    (Integer)component.getClientProperty("nbeditorui.hScrollPolicy"));
        }
        // extComponent will be a panel
        JComponent ec = new JPanel(new BorderLayout());
        ec.putClientProperty(JTextComponent.class, component);
        ec.add(scroller);

        // Initialize sidebars
        // Need to clear the cache - it's null at this point when opening file but the sidebars
        // would be reused during L&F change (see BaseTextUI.UIWatcher) which would not work properly.
        CustomizableSideBar.resetSideBars(component);
        Map<SideBarPosition, JComponent> sideBars = CustomizableSideBar.getSideBars(component);
        processSideBars(sideBars, ec, scroller);
        
        if (listener == null){
            listener = new SideBarsListener(component);
            CustomizableSideBar.addChangeListener(NbEditorUtilities.getMimeType(component), listener);
        }
        
        // Initialize the corner component
        initGlyphCorner(scroller);

        return ec;
    }
    

    public @Override boolean isLineNumberEnabled() {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        return prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
    }

    public @Override void setLineNumberEnabled(boolean lineNumberEnabled) {
        Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
        boolean visible = prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
        prefs.putBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, !visible);
    }
    
    private static void processSideBars(Map sideBars, JComponent ec, JScrollPane scroller) {
        // Remove all existing sidebars
        ec.removeAll();

        // Add the scroller and the new sidebars
        ec.add(scroller);
        scroller.setRowHeader(null);
        scroller.setColumnHeaderView(null);
//        final MouseDispatcher mouse = new MouseDispatcher((JTextComponent) ec.getClientProperty(JTextComponent.class));
        for (Iterator entries = sideBars.entrySet().iterator(); entries.hasNext(); ) {
            Map.Entry entry = (Map.Entry) entries.next();
            SideBarPosition position = (SideBarPosition) entry.getKey();
            JComponent sideBar = (JComponent) entry.getValue();
            
//            if (position.getPosition() == SideBarPosition.WEST) {
//                JPanel p = new JPanel(new BorderLayout()) {
//
//                    @Override
//                    public void addNotify() {
//                        super.addNotify();
//                        infiltrateContainer(this, mouse, true);
//                    }
//
//                    @Override
//                    public void removeNotify() {
//                        infiltrateContainer(this, mouse, false);
//                        super.removeNotify();
//                    }
//
//                };
//                p.add(sideBar, BorderLayout.CENTER);
//                sideBar = p;
//            }
            
            if (position.isScrollable()) {
                if (position.getPosition() == SideBarPosition.WEST) {
                    scroller.setRowHeaderView(sideBar);
                } else {
                    if (position.getPosition() == SideBarPosition.NORTH) {
                        scroller.setColumnHeaderView(sideBar);
                    } else {
                        throw new IllegalArgumentException("Unsupported side bar position, scrollable = true, position=" + position.getBorderLayoutPosition()); // NOI18N
                    }
                }
            } else {
                ec.add(sideBar, position.getBorderLayoutPosition());
            }
        }
    }
    
//    private static void infiltrateContainer(Container c, MouseDispatcher mouse, boolean add) {
//        for (Component comp : c.getComponents()) {
//            if (add) {
//                comp.addMouseListener(mouse);
//                comp.addMouseMotionListener(mouse);
//            } else {
//                comp.removeMouseListener(mouse);
//                comp.removeMouseMotionListener(mouse);
//            }
//            if (comp instanceof Container) {
//                Container cont = (Container) comp;
//                if (add) {
//                    cont.addContainerListener(mouse);
//                } else {
//                    cont.removeContainerListener(mouse);
//                }
//                infiltrateContainer(cont, mouse,add);
//            }
//        }
//
//    }
//
//    private static final class MouseDispatcher implements MouseListener, MouseMotionListener, ContainerListener {
//
//        private final Component target;
//
//        public MouseDispatcher(Component comp) {
//            this.target = comp;
//        }
//
//        private void redispatch(MouseEvent oe) {
//            if (oe.isConsumed()) {
//                return;
//            }
//            MouseEvent ne = SwingUtilities.convertMouseEvent(
//                    oe.getComponent(), oe, target);
//            target.dispatchEvent(ne);
//        }
//
//        public void mouseDragged(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseMoved(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseClicked(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mousePressed(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseReleased(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseEntered(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void mouseExited(MouseEvent e) {
//            redispatch(e);
//        }
//
//        public void componentAdded(ContainerEvent e) {
//            Component comp = e.getChild();
//            if (comp instanceof Container) {
//                infiltrateContainer((Container) comp, this, true);
//            } else {
//                comp.addMouseListener(this);
//                comp.addMouseMotionListener(this);
//            }
//        }
//
//        public void componentRemoved(ContainerEvent e) {
//            Component comp = e.getChild();
//            if (comp instanceof Container) {
//                infiltrateContainer((Container) comp, this, false);
//            } else {
//                comp.removeMouseListener(this);
//                comp.removeMouseMotionListener(this);
//            }
//        }
//
//    }
    
    private class LayeredEditorPane extends JLayeredPane implements Scrollable, Accessible {

        private final JTextComponent component;

        public LayeredEditorPane(JTextComponent component) {
            this.component = component;
        }

        @Override
        public Dimension getMaximumSize() {
            return component.getMaximumSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return component.getMinimumSize();
        }

        @Override
        public void setPreferredSize(Dimension preferredSize) {
            super.setPreferredSize(preferredSize);
            component.setPreferredSize(preferredSize);
        }

        @Override
        public Dimension getPreferredSize() {
            return component.getPreferredSize();
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);
            component.setSize(width, height);
        }
        
        @Override
        public void setSize(Dimension d) {
            super.setSize(d);
            component.setSize(d);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return component.getPreferredScrollableViewportSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return component.getScrollableUnitIncrement(visibleRect, orientation, direction);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return component.getScrollableBlockIncrement(visibleRect, orientation, direction);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            Container parent = SwingUtilities.getUnwrappedParent(this);
            if (parent instanceof JViewport) {
                return parent.getWidth() > getPreferredSize().width;
            }
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            Container parent = SwingUtilities.getUnwrappedParent(this);
            if (parent instanceof JViewport) {
                return parent.getHeight() > getPreferredSize().height;
            }
            return false;
        }
    }
    
    protected @Override JToolBar createToolBarComponent() {
        return new NbEditorToolBar(getComponent());
    }

    private class SystemActionPerformer implements PropertyChangeListener{

        private String editorActionName;

        private Action editorAction;

        private Action systemAction;
        
        
        SystemActionPerformer(String editorActionName) {
            this.editorActionName = editorActionName;

            synchronized (NbEditorUI.this.getComponentLock()) {
                // if component already installed in EditorUI simulate installation
                JTextComponent component = getComponent();
                if (component != null) {
                    propertyChange(new PropertyChangeEvent(NbEditorUI.this,
                                                           EditorUI.COMPONENT_PROPERTY, null, component));
                }

                NbEditorUI.this.addPropertyChangeListener(this);
            }
        }
        
        private void attachSystemActionPerformer(JTextComponent c){
            if (c == null) return;

            Action action = getEditorAction(c);
            if (action == null) return;

            Action globalSystemAction = getSystemAction(c);
            if (globalSystemAction == null) return;

            if (globalSystemAction instanceof CallbackSystemAction){
                Object key = ((CallbackSystemAction)globalSystemAction).getActionMapKey();
                c.getActionMap ().put (key, action);
            }                        
        }
        
        private void detachSystemActionPerformer(JTextComponent c){
            if (c == null) return;

            Action action = getEditorAction(c);
            if (action == null) return;

            Action globalSystemAction = getSystemAction(c);
            if (globalSystemAction == null) return;

            if (globalSystemAction instanceof CallbackSystemAction){
                Object key = ((CallbackSystemAction)globalSystemAction).getActionMapKey();
                ActionMap am = c.getActionMap();
                if (am != null) {
                    Object ea = am.get(key);
                    if (action.equals(ea)) {
                        am.remove(key);
                    }
                }
            }                        
                                
        }
        
        
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();

                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);
                    attachSystemActionPerformer(component);
                } else { // just deinstalled
                    component = (JTextComponent)evt.getOldValue();
                    component.removePropertyChangeListener(this);
                    detachSystemActionPerformer(component);
                }
            }
        }   

        private synchronized Action getEditorAction(JTextComponent component) {
            if (editorAction == null) {
                BaseKit kit = Utilities.getKit(component);
                if (kit != null) {
                    editorAction = kit.getActionByName(editorActionName);
                }
            }
            return editorAction;
        }

        private Action getSystemAction(JTextComponent c) {
            if (systemAction == null) {
                Action ea = getEditorAction(c);
                if (ea != null) {
                    String saClassName = (String)ea.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
                    if (saClassName != null) {
                        Class saClass;
                        try {
                            saClass = Class.forName(saClassName);
                        } catch (Throwable t) {
                            saClass = null;
                        }

                        if (saClass != null) {
                            systemAction = SystemAction.get(saClass);
                            if (systemAction instanceof ContextAwareAction){
                                Lookup lookup = getContextLookup(c);
                                if (lookup!=null){
                                    systemAction = ((ContextAwareAction)systemAction).createContextAwareInstance(lookup);
                                }
                            }
                            
                        }
                    }
                }
            }
            return systemAction;
        }
        
    }
    

    /**
     *
     * @deprecated use SystemActionPerformer instead
     */
    @Deprecated
    public final class SystemActionUpdater
        implements PropertyChangeListener, ActionPerformer {

        private String editorActionName;

        private boolean updatePerformer;

        private boolean syncEnabling;

        private Action editorAction;

        private Action systemAction;

        private PropertyChangeListener enabledPropertySyncL;
        
        private boolean listeningOnTCRegistry;


        SystemActionUpdater(String editorActionName, boolean updatePerformer,
                            boolean syncEnabling) {
            this.editorActionName = editorActionName;
            this.updatePerformer = updatePerformer;
            this.syncEnabling = syncEnabling;

            synchronized (NbEditorUI.this.getComponentLock()) {
                // if component already installed in EditorUI simulate installation
                JTextComponent component = getComponent();
                if (component != null) {
                    propertyChange(new PropertyChangeEvent(NbEditorUI.this,
                                                           EditorUI.COMPONENT_PROPERTY, null, component));
                }

                NbEditorUI.this.addPropertyChangeListener(this);
            }
        }

        public void editorActivated() {
            Action ea = getEditorAction();
            Action sa = getSystemAction();
            if (ea != null && sa != null) {
                if (updatePerformer) {
                    if (ea.isEnabled() && sa instanceof CallbackSystemAction) {
                        ((CallbackSystemAction)sa).setActionPerformer(this);
                    }
                }

                if (syncEnabling) {
                    if (enabledPropertySyncL == null) {
                        enabledPropertySyncL = new EnabledPropertySyncListener(sa);
                    }
                    ea.addPropertyChangeListener(enabledPropertySyncL);
                }
            }
        }

        public void editorDeactivated() {
            Action ea = getEditorAction();
            Action sa = getSystemAction();
            if (ea != null && sa != null) {
                /*        if (sa instanceof CallbackSystemAction) {
                          CallbackSystemAction csa = (CallbackSystemAction)sa;
                          if (csa.getActionPerformer() == this) {
                            csa.setActionPerformer(null);
                          }
                        }
                */

                if (syncEnabling && enabledPropertySyncL != null) {
                    ea.removePropertyChangeListener(enabledPropertySyncL);
                }
            }
        }

        private void reset() {
            if (enabledPropertySyncL != null) {
                editorAction.removePropertyChangeListener(enabledPropertySyncL);
            }

            /*      if (systemAction != null) {
                    if (systemAction instanceof CallbackSystemAction) {
                      CallbackSystemAction csa = (CallbackSystemAction)systemAction;
                      if (!csa.getSurviveFocusChange() || csa.getActionPerformer() == this) {
                        csa.setActionPerformer(null);
                      }
                    }
                  }
            */

            editorAction = null;
            systemAction = null;
            enabledPropertySyncL = null;
        }

        /** Perform the callback action */
        public void performAction(SystemAction action) {
            JTextComponent component = getComponent();
            Action ea = getEditorAction();
            if (component != null && ea != null) {
                ea.actionPerformed(new ActionEvent(component, 0, "")); // NOI18N
            }
        }
        
        private void startTCRegistryListening() {
            if (!listeningOnTCRegistry) {
                listeningOnTCRegistry = true;
                TopComponent.getRegistry().addPropertyChangeListener(this);
            }
        }
        
        private void stopTCRegistryListening() {
            if (listeningOnTCRegistry) {
                listeningOnTCRegistry = false;
                TopComponent.getRegistry().removePropertyChangeListener(this);
            }
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if (TopComponent.Registry.PROP_ACTIVATED.equals (propName)) {
                TopComponent activated = (TopComponent)evt.getNewValue();

                if(activated instanceof CloneableEditorSupport.Pane)
                    editorActivated();
                else
                    editorDeactivated();
            } else if (EditorUI.COMPONENT_PROPERTY.equals(propName)) {
                JTextComponent component = (JTextComponent)evt.getNewValue();

                if (component != null) { // just installed
                    component.addPropertyChangeListener(this);
                    if (component.isDisplayable()) {
                        startTCRegistryListening();
                    }

                } else { // just deinstalled
                    component = (JTextComponent)evt.getOldValue();
                    component.removePropertyChangeListener(this);
                    stopTCRegistryListening();
                }

                reset();

            } else if ("editorKit".equals(propName)) { // NOI18N
                reset();

            } else if ("ancestor".equals(propName)) { // NOI18N
                if (((Component)evt.getSource()).isDisplayable()) { // now displayable
                    startTCRegistryListening();
                } else { // not displayable
                    stopTCRegistryListening();
                }
            }
        }

        private synchronized Action getEditorAction() {
            if (editorAction == null) {
                BaseKit kit = Utilities.getKit(getComponent());
                if (kit != null) {
                    editorAction = kit.getActionByName(editorActionName);
                }
            }
            return editorAction;
        }

        private Action getSystemAction() {
            if (systemAction == null) {
                Action ea = getEditorAction();
                if (ea != null) {
                    String saClassName = (String)ea.getValue(NbEditorKit.SYSTEM_ACTION_CLASS_NAME_PROPERTY);
                    if (saClassName != null) {
                        Class saClass;
                        try {
                            saClass = Class.forName(saClassName);
                        } catch (Throwable t) {
                            saClass = null;
                        }

                        if (saClass != null) {
                            systemAction = SystemAction.get(saClass);
                        }
                    }
                }
            }
            return systemAction;
        }

        protected @Override void finalize() throws Throwable {
            reset();
        }

    }

    /** Listener that listen on changes of the "enabled" property
    * and if changed it changes the same property of the action
    * given in constructor.
    */
    static class EnabledPropertySyncListener implements PropertyChangeListener {

        Action action;

        EnabledPropertySyncListener(Action actionToBeSynced) {
            this.action = actionToBeSynced;
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                action.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
            }
        }

    }

    private static final class SideBarsListener implements ChangeListener {

        private final JTextComponent component;
        
        public SideBarsListener(JTextComponent component) {
            this.component = component;
        }
        
        public void stateChanged(ChangeEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    EditorUI eui = Utilities.getEditorUI(component);
                    if (eui != null) {
                        JComponent ec = eui.getExtComponent();
                        if (ec != null) {
                            JScrollPane scroller = (JScrollPane) ec.getComponent(0);
                            // remove prior to creating new sidebars
                            ec.removeAll();
                            scroller.setRowHeaderView(null);
                            scroller.setColumnHeaderView(null);
                            Map newMap = CustomizableSideBar.getSideBars(component);
                            processSideBars(newMap, ec, scroller);
                            ec.revalidate();
                            ec.repaint();
                        }
                    }
                }
            });
        }
    } //End of SideBarPosition class
}
