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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.ActionMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.awt.MouseUtils;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author  Jan Becicka
 */
public class RefactoringPanelContainer extends TopComponent {
    
    private static RefactoringPanelContainer usages = null;
    private static RefactoringPanelContainer refactorings = null;
    private transient boolean isVisible = false;
    private JPopupMenu pop;
    /** Popup menu listener */
    private PopupListener listener;
    private CloseListener closeL;
    private boolean isRefactoring;
    private static Image REFACTORING_BADGE = ImageUtilities.loadImage( "org/netbeans/modules/refactoring/api/resources/refactoringpreview.png" ); // NOI18N
    private static Image USAGES_BADGE = ImageUtilities.loadImage( "org/netbeans/modules/refactoring/api/resources/findusages.png" ); // NOI18N
    
    private RefactoringPanelContainer() {
        this("", false);
    }
    /** Creates new form RefactoringPanelContainer */
    private RefactoringPanelContainer(String name, boolean isRefactoring) {
        setName(name);
        setToolTipText(name);
        setFocusable(true);
        setLayout(new java.awt.BorderLayout());
        setMinimumSize(new Dimension(1,1));
        getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(RefactoringPanelContainer.class, "ACSD_usagesPanel")
        );
        pop = new JPopupMenu();
        pop.add(new Close());
        pop.add(new CloseAll());
        pop.add(new CloseAllButCurrent());
        listener = new PopupListener();
        closeL = new CloseListener();
        this.isRefactoring = isRefactoring;
        setFocusCycleRoot(true);
        JLabel label = new JLabel(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_NoUsages"));
        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setEnabled(false);
        this.add(label, BorderLayout.CENTER);
        initActions();
    }
    
    void addPanel(JPanel panel) {
        RefactoringPanel.checkEventThread();
        if (getComponentCount() == 0) {
            add(panel, BorderLayout.CENTER);
        } else {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                ((JTabbedPane) comp).addTab(panel.getName(), null, panel, panel.getToolTipText());
                ((JTabbedPane) comp).setSelectedComponent(panel);
                comp.validate();
            } else if (comp instanceof JLabel) {
                remove(comp);
                add(panel, BorderLayout.CENTER);
            } else {
                remove(comp);
                JTabbedPane pane = TabbedPaneFactory.createCloseButtonTabbedPane();
                pane.addMouseListener(listener);
                pane.addPropertyChangeListener(closeL);
                add(pane, BorderLayout.CENTER);
                pane.addTab(comp.getName(), null, comp, ((JPanel) comp).getToolTipText());
                pane.addTab(panel.getName(), null, panel, panel.getToolTipText());
                pane.setSelectedComponent(panel);
                pane.validate();
            }
        }
        if (!isVisible) {
            isVisible = true;
            open();
        }
        validate();
        requestActive();
    }
    
    void removePanel(JPanel panel) {
        RefactoringPanel.checkEventThread();
        Component comp = getComponentCount() > 0 ? getComponent(0) : null;
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            if (panel == null) {
                panel = (JPanel) tabs.getSelectedComponent();
            }
            tabs.remove(panel);
            if (tabs.getTabCount() == 1) {
                Component c = tabs.getComponentAt(0);
                tabs.removeMouseListener(listener);
                tabs.removePropertyChangeListener(closeL);
                remove(tabs);
                add(c, BorderLayout.CENTER);
            }
            validate();
        } else {
            if (comp != null)
                remove(comp);
            isVisible = false;
            close();
        }
    }
    
    void closeAllButCurrent() {
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            Component current = tabs.getSelectedComponent();
            int tabCount = tabs.getTabCount();
            // #172039: do not use tabs.getComponents()
            Component[] c = new Component[tabCount - 1];
            for (int i = 0, j = 0; i < tabCount; i++) {
                Component tab = tabs.getComponentAt(i);
                if (tab != current) {
                    c[j++] = tab;
                }
            }
            for (int i = 0; i < c.length; i++) {
                ((RefactoringPanel) c[i]).close();
            }
        }
    }
    
    public static synchronized RefactoringPanelContainer getUsagesComponent() {
        if ( usages == null ) {
            usages = (RefactoringPanelContainer) WindowManager.getDefault().findTopComponent( "find-usages" ); //NOI18N
            if (usages == null) {
                // #156401: WindowManager.findTopComponent may fail
                usages = createUsagesComponent();
            }
        } 
        return usages;
    }
    
    public static synchronized RefactoringPanelContainer getRefactoringComponent() {
        if (refactorings == null) {
            refactorings = (RefactoringPanelContainer) WindowManager.getDefault().findTopComponent( "refactoring-preview" ); //NOI18N
            if (refactorings == null) {
                // #156401: WindowManager.findTopComponent may fail
                refactorings = createRefactoringComponent();
            }
        } 
        return refactorings;
    }
    
    public static synchronized RefactoringPanelContainer createRefactoringComponent() {
        if (refactorings == null) {
            refactorings = new RefactoringPanelContainer(org.openide.util.NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_Refactoring"), true);
        }
        return refactorings;
    }
    
    public static synchronized RefactoringPanelContainer createUsagesComponent() {
        if (usages == null) {
            usages = new RefactoringPanelContainer(org.openide.util.NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_Usages"), false);
        }
        return usages;
    }
    
    @Override
    protected void componentClosed() {
        isVisible = false;
        if (getComponentCount() == 0) {
            return ;
        }
        Component comp = getComponent(0);
        if (comp instanceof JTabbedPane) {
            JTabbedPane pane = (JTabbedPane) comp;
            // #172039: do not use tabs.getComponents()
            Component[] c = new Component[pane.getTabCount()];
            for (int i = 0; i < c.length; i++) {
                c[i] = pane.getComponentAt(i);
            }
            for (int i = 0; i < c.length; i++) {
                ((RefactoringPanel) c[i]).close();
            }
        } else if (comp instanceof RefactoringPanel) {
            ((RefactoringPanel) comp).close();
        }
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        requestFocusInWindow();
    }
    
    @Override
    public boolean requestFocusInWindow() {
        boolean value = super.requestFocusInWindow();
        Component comp = getRefactoringPanelComp();
        if(comp != null) {
            return comp.requestFocusInWindow();
        } else {
            return value;
        }
    }

    //#41258: In the SDI, requestFocus is called rather than requestFocusInWindow:
    @Override
    public void requestFocus() {
        super.requestFocus();
        Component comp = getRefactoringPanelComp();
        if(comp != null) {
            comp.requestFocus();
        }
    }
    
    private Component getRefactoringPanelComp() {
        RefactoringPanel.checkEventThread();
        Component comp = getComponentCount() > 0 ? getComponent(0) : null;
        if (comp instanceof JTabbedPane) {
            JTabbedPane tabs = (JTabbedPane) comp;
            return tabs.getSelectedComponent();
        } else {
            return comp;
        }
    }
    
    @Override
    protected String preferredID() {
        return "RefactoringPanel"; // NOI18N
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
    
    private void initActions() {
        ActionMap map = getActionMap();

        map.put("jumpNext", new PrevNextAction (false)); // NOI18N
        map.put("jumpPrev", new PrevNextAction (true)); // NOI18N
    }
    
    public RefactoringPanel getCurrentPanel() {
        if (getComponentCount() > 0) {
            Component comp = getComponent(0);
            if (comp instanceof JTabbedPane) {
                JTabbedPane tabs = (JTabbedPane) comp;
                return (RefactoringPanel) tabs.getSelectedComponent();
            } else {
                if (comp instanceof RefactoringPanel)
                    return (RefactoringPanel) comp;
            }
        }
        return null;
    }
    
    private final class PrevNextAction extends javax.swing.AbstractAction {
        private boolean prev;
        
        public PrevNextAction (boolean prev) {
            this.prev = prev;
        }

        @Override
        public void actionPerformed (java.awt.event.ActionEvent actionEvent) {
            RefactoringPanel panel = getCurrentPanel();
            if (panel != null) {
                if (prev) {
                    panel.selectPrevUsage(true);
                } else {
                    panel.selectNextUsage(true);
                }
            }
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
    /**
    * Class to showing popup menu
    */
    private class PopupListener extends MouseUtils.PopupMouseAdapter {        

        /**
         * Called when the sequence of mouse events should lead to actual showing popup menu
         */
        @Override
        protected void showPopup (MouseEvent e) {
            pop.show(RefactoringPanelContainer.this, e.getX(), e.getY());
        }
    } // end of PopupListener
        
    private class Close extends AbstractAction {
        
        public Close() {
            super(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_CloseWindow"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            removePanel(null);
        }
    }
    
    private final class CloseAll extends AbstractAction {
        
        public CloseAll() {
            super(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_CloseAll"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            close();
        }
    }
    
    private class CloseAllButCurrent extends AbstractAction {
        
        public CloseAllButCurrent() {
            super(NbBundle.getMessage(RefactoringPanelContainer.class, "LBL_CloseAllButCurrent"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            closeAllButCurrent();
        }
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RefactoringPanelContainer.class.getName() + (isRefactoring ? ".refactoring-preview" : ".find-usages") ); //NOI18N
    }

    @Override
    public java.awt.Image getIcon() {
        if (isRefactoring)
            return REFACTORING_BADGE;
        else
            return USAGES_BADGE;
    }
}
