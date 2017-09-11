/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.spi.navigator.NavigatorDisplayer;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithToolbar;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/** Navigator TopComponent. Simple visual envelope for navigator graphics
 * content. Behaviour is delegated and separated into NavigatorController.
 *
 * @author Dafe Simonek
 */
public final class NavigatorTC extends TopComponent implements NavigatorDisplayer {
    
    /** singleton instance */
    private static NavigatorTC instance;
    
    /** Currently active panel in navigator (or null if empty) */
    private NavigatorPanel selectedPanel;
    /** A list of panels currently available (or null if empty) */
    private List<? extends NavigatorPanel> panels;
    /** Controller, controls behaviour and reacts to user actions */
    private NavigatorController controller;
    /** label signalizing no available providers */
    private final JLabel notAvailLbl = new JLabel(
            NbBundle.getMessage(NavigatorTC.class, "MSG_NotAvailable")); //NOI18N
    /** Listener for the panel selector combobox */
    private ActionListener panelSelectionListener;
    /** Testing purposes - component representing selected panel toolbar */
    private JComponent toolbarComponent;

    /** Creates new NavigatorTC, singleton */
    private NavigatorTC() {
        initComponents();
        
        setName(NbBundle.getMessage(NavigatorTC.class, "LBL_Navigator")); //NOI18N
        setIcon(ImageUtilities.loadImage("org/netbeans/modules/navigator/resources/navigator.png")); //NOI18N
        // accept focus when empty to work correctly in nb winsys
        setFocusable(true);
        // special title for sliding mode
        // XXX - please rewrite to regular API when available - see issue #55955
        putClientProperty("SlidingName", getName());
        getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(NavigatorTC.class, "ACC_DESC_NavigatorTC")); //NOI18N
        
        notAvailLbl.setHorizontalAlignment(SwingConstants.CENTER);
        notAvailLbl.setEnabled(false);
        Color usualWindowBkg = UIManager.getColor("Tree.background"); //NOI18N
        notAvailLbl.setBackground(usualWindowBkg != null ? usualWindowBkg : Color.white);
        // to ensure our background color will have effect
        notAvailLbl.setOpaque(true);

        holderPanel.setOpaque(false);

        panelSelectionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = panelSelector.getSelectedIndex();
                if (panels != null && idx >= 0 && idx < panels.size()
                        && panels.get(idx) != selectedPanel) {
                    NavigatorTC.this.firePropertyChange(PROP_PANEL_SELECTION,
                            selectedPanel, panels.get(idx));
                }
            }
        };
        panelSelector.addActionListener(panelSelectionListener);

        associateLookup(
            new ProxyLookup(
                new Lookup [] { 
                    Lookups.singleton(getActionMap()), 
                    getController().getPanelLookup() 
                }));
        
        // empty initially
        setToEmpty();
        if( "Aqua".equals(UIManager.getLookAndFeel().getID()) ) {
            Color backColor = UIManager.getColor("NbExplorerView.background"); //NOI18N
            setBackground(backColor);
            notAvailLbl.setBackground(backColor);
            setOpaque(true);
            holderPanel.setOpaque(true);
            holderPanel.setBackground(backColor);
            holderPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, 
                    UIManager.getColor("NbSplitPane.background")));//NOI18N
        }
    }

    @Override
    public String getShortName() {
        return NbBundle.getMessage(NavigatorTC.class, "LBL_Navigator"); //NOI18N
    }

    /** Singleton accessor, finds instance in winsys structures */
    public static final NavigatorTC getInstance () {
        NavigatorTC navTC = (NavigatorTC)WindowManager.getDefault().
                        findTopComponent("navigatorTC"); //NOI18N
        if (navTC == null) {
            // shouldn't happen under normal conditions
            navTC = privateGetInstance();
            Logger.getAnonymousLogger().warning(
                "Could not locate the navigator component via its winsys id"); //NOI18N
        }
        return navTC;
    }
    
    /** Singleton intance accessor, to be used only from module's layer.xml
     * file, winsys section and as fallback from getInstance().
     *
     * Please don't call directly otherwise.
     */ 
    public static final NavigatorTC privateGetInstance () {
        if (instance == null) {
            instance = new NavigatorTC();
        }
        return instance;
    }

    @Override
    public boolean allowAsyncUpdate() {
        return true;
    }

    @Override
    public TopComponent getTopComponent() {
        return this;
    }

    /** Shows given navigator panel's component
     */
    @Override
    public void setSelectedPanel (NavigatorPanel panel) {
        int panelIdx = panels.indexOf(panel);
        assert panelIdx != -1 : "Panel to select is not available"; //NOI18N
        
        if (panel.equals(selectedPanel)) {
            return;
        }
        
        this.selectedPanel = panel;
        assert SwingUtilities.isEventDispatchThread() : "Called in AWT queue.";
        JComponent comp = this.selectedPanel.getComponent();
        if (comp == null) {
                    Throwable npe = new NullPointerException(
                            "Method " + this.selectedPanel.getClass().getName() +  //NOI18N
                            ".getComponent() must not return null under any condition!"  //NOI18N
                    );
                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, npe);

        } else {
            contentArea.removeAll();
            contentArea.add(panel.getComponent(), BorderLayout.CENTER);

            pnlToolbar.removeAll();
            if (panel instanceof NavigatorPanelWithToolbar && ((NavigatorPanelWithToolbar)panel).getToolbarComponent() != null) {
                toolbarComponent = ((NavigatorPanelWithToolbar)panel).getToolbarComponent();
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weightx = 1.0;
                pnlToolbar.add(toolbarComponent, gbc);
                pnlToolbar.setVisible(true);
            } else {
                toolbarComponent = null;
                pnlToolbar.setVisible(false);
            }
            revalidate();
            repaint();
        }
        panelSelector.removeActionListener(panelSelectionListener);
        // #93123: follow-up, synchronizing combo selection with content area selection
        panelSelector.setSelectedIndex(panelIdx);
        panelSelector.addActionListener(panelSelectionListener);
    }
    
    /** Returns panel currently selected.
     * @return Panel currently selected or null if navigator is empty
     */
    @Override
    public NavigatorPanel getSelectedPanel () {
        return selectedPanel;
    }
    
    /** Only for tests. List of panels currently contained in navigator component.
     * @return List of NavigatorPanel instances or null if navigator is empty
     */
    public List<? extends NavigatorPanel> getPanels () {
        return panels;
    }
    
    /** Sets content of navigator to given panels, selecting given one
     * @param panels List of panels
     * @param select Panel to be selected, shown
     */ 
    @Override
    public void setPanels (List<? extends NavigatorPanel> panels, NavigatorPanel select) {
        this.panels = panels;
        int panelsCount = panels == null ? -1 : panels.size();
        selectedPanel = null;
        toolbarComponent = null;
        // clear regular content
        panelSelector.removeActionListener(panelSelectionListener);
        contentArea.removeAll();
        panelSelector.removeAllItems();
        // no panel, so make UI look empty
        if (panelsCount <= 0) {
            setToEmpty();
        } else {
            // #63777: hide panel selector when only one panel available
            holderPanel.setVisible(panelsCount != 1 || (select instanceof NavigatorPanelWithToolbar && ((NavigatorPanelWithToolbar)select).getToolbarComponent() != null));
            boolean selectFound = false;
            for (NavigatorPanel curPanel : panels) {
                panelSelector.addItem(curPanel.getDisplayName());
                if (curPanel == select) {
                    selectFound = true;
                }
            }
            panelSelector.addActionListener(panelSelectionListener);
            if (selectFound) {
                setSelectedPanel(select);
            } else {
                setSelectedPanel(panels.get(0));
            }
            // show if was hidden
            resetFromEmpty();
        }
    }
    
    /** Returns combo box, UI for selecting proper panels. For tests only. */
    JComboBox getPanelSelector () {
        return panelSelector;
    }
    
    // Window System related methods >>

    @Override
    public String preferredID () {
        return "navigatorTC"; //NOI18N
    }

    @Override
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }

    /** Overriden to pass focus directly into content panel */
    @SuppressWarnings("deprecation")
    @Override
    public boolean requestFocusInWindow () {
        if (selectedPanel != null) {
            return selectedPanel.getComponent().requestFocusInWindow();
        } else {
            return super.requestFocusInWindow();
        }
    }

    @Override
    public void requestFocus() {
        if (selectedPanel != null) {
            selectedPanel.getComponent().requestFocus();
        } else {
            super.requestFocus();
        }
    }

    /** Defines nagivator Help ID */
    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx("navigator.java");
    }

    // << Window system

    @Override
    public UndoRedo getUndoRedo() {
        if (selectedPanel == null || !(selectedPanel instanceof NavigatorPanelWithUndo)) {
            return UndoRedo.NONE;
        }
        return ((NavigatorPanelWithUndo)selectedPanel).getUndoRedo();
    }
    
    /** Accessor for controller which controls UI behaviour */
    public NavigatorController getController () {
        if (controller == null) {
            controller = new NavigatorController(this);
        }
        return controller;
    }

    /**
     * For testing
     */
    JComponent getToolbar() {
        return toolbarComponent;
    }

    /*************** private stuff ************/
    
    /** Removes regular UI content and sets UI to empty state */
    private void setToEmpty () {
        if (notAvailLbl.isShowing()) {
            // already empty
            return;
        }
        remove(holderPanel);
        holderPanel.setVisible(false);
        remove(contentArea);
        add(notAvailLbl, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    
    /** Puts regular UI content back */
    private void resetFromEmpty () {
        if (contentArea.isShowing()) {
            // content already shown
        }
        remove(notAvailLbl);
        add(holderPanel, BorderLayout.NORTH);
        add(contentArea, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        holderPanel = new javax.swing.JPanel();
        panelSelector = new javax.swing.JComboBox();
        pnlToolbar = new javax.swing.JPanel();
        contentArea = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        holderPanel.setLayout(new java.awt.GridBagLayout());

        panelSelector.setMinimumSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        holderPanel.add(panelSelector, gridBagConstraints);

        pnlToolbar.setOpaque(false);
        pnlToolbar.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        holderPanel.add(pnlToolbar, gridBagConstraints);

        add(holderPanel, java.awt.BorderLayout.NORTH);

        contentArea.setLayout(new java.awt.BorderLayout());
        add(contentArea, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel contentArea;
    private javax.swing.JPanel holderPanel;
    private javax.swing.JComboBox panelSelector;
    private javax.swing.JPanel pnlToolbar;
    // End of variables declaration//GEN-END:variables

    
    
    
}
