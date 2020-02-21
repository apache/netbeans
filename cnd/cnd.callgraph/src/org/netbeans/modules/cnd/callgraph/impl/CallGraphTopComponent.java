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

package org.netbeans.modules.cnd.callgraph.impl;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphUI;
import org.netbeans.modules.cnd.callgraph.api.ui.Catalog;
import org.openide.awt.MouseUtils;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 */
public final class CallGraphTopComponent extends TopComponent {

    private static CallGraphTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/netbeans/modules/cnd/callgraph/resources/call_graph.png"; // NOI18N
    
    private static final String PREFERRED_ID = "CallGraphTopComponent"; // NOI18N

    private JPopupMenu pop;
    private PopupListener listener;
    private CloseListener closeL;
    private Catalog catalog;


    private CallGraphTopComponent() {
        catalog = new DefaultCatalog();
        initComponents();
        setName(getMessage("CTL_CallGraphTopComponent")); // NOI18N
        setToolTipText(getMessage( "HINT_CallGraphTopComponent")); // NOI18N
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        setFocusCycleRoot(true);
    }
    
    String getMessage(String key) {
        return catalog.getMessage(key);
    }

    public void setModel(CallModel model, CallGraphUI graphUI) {
        CallGraphPanel panel = new CallGraphPanel(graphUI);
        this.catalog = graphUI == null || graphUI.getCatalog() == null ? new DefaultCatalog() : graphUI.getCatalog();
        setName(getMessage( "CTL_CallGraphTopComponent")); // NOI18N
        setToolTipText(getMessage( "HINT_CallGraphTopComponent")); // NOI18N        
        panel.setName(model.getName());
        panel.setToolTipText(panel.getName()+" - "+getMessage("CTL_CallGraphTopComponent")); // NOI18N
        if (false) {
            addPanel(panel);
        } else {
            addTabPanel(panel);
        }
        panel.setModel(model);
        panel.requestFocusInWindow();
    }

    void addPanel(JPanel panel) {
        setName(panel.getToolTipText());
        removeAll();
        add(panel, BorderLayout.CENTER);
        validate();
    }
    
    void addTabPanel(JPanel panel) {
        if (getComponentCount() == 0) {
            add(panel, BorderLayout.CENTER);
        } else {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).addTab(panel.getName() + "  ", null, panel, panel.getToolTipText()); // NOI18N
                ((JTabbedPane) comp).setSelectedComponent(panel);
                comp.validate();
            } else if (comp instanceof JButton) {
                setName(panel.getToolTipText());
                remove(comp);
                add(panel, BorderLayout.CENTER);
            } else {
                setName(getMessage("CTL_CallGraphTopComponent")); // NOI18N
                remove(comp);
                JTabbedPane pane = TabbedPaneFactory.createCloseButtonTabbedPane();
                pane.addMouseListener(listener);
                pane.addPropertyChangeListener(closeL);
                add(pane, BorderLayout.CENTER);
                pane.addTab(comp.getName() + "  ", null, comp, ((JPanel) comp).getToolTipText()); //NOI18N
                pane.addTab(panel.getName() + "  ", null, panel, panel.getToolTipText()); //NOI18N
                pane.setSelectedComponent(panel);
                pane.validate();
            }
        }
        validate();
        requestActive();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(CallGraphTopComponent.class, "NO_VIEW_AVAILABLE")); // NOI18N
        jButton1.setEnabled(false);
        jButton1.setFocusable(false);
        add(jButton1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized CallGraphTopComponent getDefault() {
        if (instance == null) {
            instance = new CallGraphTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the CallGraphTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized CallGraphTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(CallGraphTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system."); // NOI18N
            return getDefault();
        }
        if (win instanceof CallGraphTopComponent) {
            return (CallGraphTopComponent) win;
        }
        Logger.getLogger(CallGraphTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID + // NOI18N
                "' ID. That is a potential source of errors and unexpected behavior."); // NOI18N
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
    // TODO add custom code on component opening
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        requestActive();
    }

    @Override
    public void requestActive() {
        super.requestActive();
        CallGraphPanel graph = getCurrentCallGraphPanel();
        if (graph != null) {
            graph.requestFocus();
            graph.requestFocusInWindow();
        }
    }

    private CallGraphPanel getCurrentCallGraphPanel(){
        if (getComponentCount() > 0) {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                comp = ((JTabbedPane)comp).getSelectedComponent();
                if (comp instanceof CallGraphPanel) {
                    return (CallGraphPanel) comp;
                }
            } else if (comp instanceof CallGraphPanel) {
                    return (CallGraphPanel) comp;
            }
        }
        return null;
    }
    
    @Override
    public void componentClosed() {
        if (getComponentCount() == 0) {
            return;
        }
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) comp;
            Component[] c =  pane.getComponents();
            for (int i = 0; i< c.length; i++) {
                if (c[i] instanceof CallGraphPanel) {
                    removePanel((CallGraphPanel) c[i]);
                }
            }
        } else if (comp instanceof CallGraphPanel) {
            removePanel((CallGraphPanel) comp);
        }
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private void removePanel(JPanel panel) {
        Component comp = getComponentCount() > 0 ? getComponent(0) : null;
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            if (panel == null) {
                panel = (JPanel) tabs.getSelectedComponent();
            }
            tabs.remove(panel);
            if (tabs.getComponentCount() == 1) {
                Component c = tabs.getComponent(0);
                tabs.removeMouseListener(listener);
                tabs.removePropertyChangeListener(closeL);
                remove(tabs);
                add(c, BorderLayout.CENTER);
                setName(((JPanel)c).getToolTipText());
            }
        } else if (comp instanceof CallGraphPanel)  {
            remove(comp);
            add(jButton1, BorderLayout.CENTER);
            setName(getMessage( "CTL_CallGraphTopComponent")); // NOI18N
            close();
        } else {
            close();
        }
        validate();
    }

    private void closeAllButCurrent() {
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            Component current = tabs.getSelectedComponent();
            Component[] c =  tabs.getComponents();
            for (int i = 0; i< c.length; i++) {
                if (c[i]!=current) {
                    if (c[i] instanceof CallGraphPanel) {
                       removePanel((CallGraphPanel) c[i]);
                    }
                }
            }
        }
    }

    private static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return CallGraphTopComponent.getDefault();
        }
    }

    private class CloseListener implements PropertyChangeListener {
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
                removePanel((JPanel) evt.getNewValue());
            }
        }
    }

    private class PopupListener extends MouseUtils.PopupMouseAdapter {        
        @Override
        protected void showPopup (MouseEvent e) {
            pop.show(CallGraphTopComponent.this, e.getX(), e.getY());
        }
    }

    private class Close extends AbstractAction {
        public Close() {
            super(getMessage( "LBL_CloseWindow"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            removePanel(null);
        }
    }
    
    private final class CloseAll extends AbstractAction {
        public CloseAll() {
            super(getMessage( "LBL_CloseAll"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }
    
    private class CloseAllButCurrent extends AbstractAction {
        public CloseAllButCurrent() {
            super(getMessage("LBL_CloseAllButCurrent"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            closeAllButCurrent();
        }
    }
}
