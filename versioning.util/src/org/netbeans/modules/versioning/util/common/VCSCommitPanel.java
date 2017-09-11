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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.versioning.util.common;

import org.openide.util.ImageUtilities;
import org.netbeans.modules.versioning.util.common.CollapsiblePanel.FilesPanel;
import org.netbeans.modules.versioning.util.common.CollapsiblePanel.HookPanel;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.netbeans.modules.versioning.util.DialogBoundsPreserver;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.openide.DialogDescriptor;
import org.openide.util.HelpCtx;
import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.prefs.Preferences;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeEvent;
import java.awt.Component;
import javax.swing.Box;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.util.NbBundle;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.awt.Dimension;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.hooks.VCSHook;
import org.netbeans.modules.versioning.hooks.VCSHookContext;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.AutoResizingPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static java.awt.Component.CENTER_ALIGNMENT;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.BoxLayout.X_AXIS;
import javax.swing.KeyStroke;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;
import static javax.swing.SwingConstants.EAST;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import javax.swing.UIManager;
import javax.swing.text.Keymap;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;

/**
 *
 * @author  pk97937
 * @author  Tomas Stupka
 * @author  Marian Petras
 */
public abstract class VCSCommitPanel<F extends VCSFileNode> extends AutoResizingPanel implements PreferenceChangeListener, TableModelListener, ChangeListener, PropertyChangeListener {

    public static final String PROP_COMMIT_EXCLUSIONS       = "commitExclusions";    // NOI18N
    
    static final Object EVENT_SETTINGS_CHANGED = new Object();   
    
    private final AutoResizingPanel basePanel = new AutoResizingPanel();

    final JPanel progressPanel = new JPanel();
    private final JLabel errorLabel = new JLabel();
    private final JPanel parametersPane1 = new JPanel();
    
    private final JButton commitButton = new JButton();
    private final JButton cancelButton = new JButton();
        
    private VCSCommitTable commitTable;
    
    private JTabbedPane tabbedPane;
        
    private final Preferences preferences;
    private final VCSCommitParameters parameters;
    private final Map<String, VCSCommitFilter> filters = new LinkedHashMap<String, VCSCommitFilter>();
    private final VCSCommitDiffProvider diffProvider;
    private final VCSCommitPanelModifier modifier;
    private static final String ERROR_COLOR;
    static {
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (c == null) {
            ERROR_COLOR = "#990000"; //NOI18N
        } else {
            ERROR_COLOR = getColorString(c);
        }
    }

    /** Creates new form CommitPanel */
    public VCSCommitPanel(VCSCommitTable table, VCSCommitParameters parameters, Preferences preferences, Collection<? extends VCSHook> hooks, VCSHookContext hooksContext, List<VCSCommitFilter> filters, VCSCommitDiffProvider diffProvider) {
        this.parameters = parameters;
        this.commitTable = table;
        this.diffProvider = diffProvider;
        this.modifier = table.getCommitModifier();
        
        parameters.addChangeListener(this);
        
        boolean selected = false;
        for (VCSCommitFilter f : filters) {
            assert (f.isSelected() && !selected) || !f.isSelected();
            selected = f.isSelected();
            this.filters.put(f.getID(), f);
        }       
        
        if(hooks == null) {
            hooks = Collections.emptyList();
        }
        initComponents(hooks, hooksContext);

        commitTable.setCommitPanel(this);
        this.preferences = preferences;
    }
    
    public VCSCommitTable<F> getCommitTable() {
        return commitTable;
    }
    
    public VCSCommitFilter getSelectedFilter() {
        for (VCSCommitFilter f : filters.values()) {
            if(f.isSelected()) {
                return f;
            }
        }
        assert false : "no filter selected"; // there always must be one NOI18N
        return null;
    }

    private JPanel getProgressPanel() {
        return progressPanel;
    }
    
    public void setErrorLabel(String htmlErrorLabel) {
        errorLabel.setText("<html><font color=\"" + ERROR_COLOR + "\">" + htmlErrorLabel + "</font></html>"); //NOI18N
        errorLabel.setVisible(!htmlErrorLabel.isEmpty());
    }    

    @Override
    public void addNotify() {
        super.addNotify();
        
        preferences.addPreferenceChangeListener(this);
        commitTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);          
    }

    public VCSCommitParameters getParameters() {
        return parameters;
    }

    @Override
    public void removeNotify() {
        commitTable.getTableModel().removeTableModelListener(this);
        preferences.removePreferenceChangeListener(this);
        super.removeNotify();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {        
        if (evt.getKey().startsWith(PROP_COMMIT_EXCLUSIONS)) { // XXX - need setting
            Runnable inAWT = new Runnable() {
                @Override
                public void run() {
                    commitTable.dataChanged();
                    listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
                }
            };
            // this can be called from a background thread - e.g. change of exclusion status in Versioning view
            if (EventQueue.isDispatchThread()) {
                inAWT.run();
            } else {
                EventQueue.invokeLater(inAWT);
            }
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
    }
    
    protected abstract void computeNodes();
    
    protected boolean isCommitButtonEnabled() {
        return commitButton.isEnabled();
    }

    protected void enableCommitButton(boolean enabled) {
        commitButton.setEnabled(enabled);
    }    
    
    protected void stopProgress() {
        JPanel p = getProgressPanel();
        p.removeAll();
        p.setVisible(false);
    }

    protected void setupProgress(String message, JComponent progressComponent) {
        JPanel p = getProgressPanel();
        p.setLayout(new BoxLayout(p, X_AXIS));
        JLabel l = new JLabel(message);
        p.add(l);
        p.add(makeHorizontalStrut(l, progressComponent, RELATED, p));
        p.add(progressComponent);               
    }
    
    protected void showProgress() {
        JPanel p = getProgressPanel();
        p.setVisible(true);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI Layout Code">
    private void initComponents(Collection<? extends VCSHook> hooks, VCSHookContext hooksContext) {
        org.openide.awt.Mnemonics.setLocalizedText(commitButton, modifier.getMessage(VCSCommitPanelModifier.BundleMessage.COMMIT_BUTTON_LABEL));
        commitButton.getAccessibleContext().setAccessibleName(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.COMMIT_BUTTON_ACCESSIBLE_NAME));
        commitButton.getAccessibleContext().setAccessibleDescription(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.COMMIT_BUTTON_ACCESSIBLE_DESCRIPTION));
        
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "CTL_Commit_Action_Cancel")); //NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "ACSN_Commit_Action_Cancel")); //NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "ACSD_Commit_Action_Cancel"));//NOI18N
        
        getAccessibleContext().setAccessibleName(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.PANEL_ACCESSIBLE_NAME));
        getAccessibleContext().setAccessibleDescription(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.PANEL_ACCESSIBLE_DESCRIPTION));
        
        basePanel.setBorder(createEmptyBorder(10,             // top
                                    getContainerGap(WEST),    // left
                                    0,                        // bottom
                                    getContainerGap(EAST)));  // right
        
        basePanel.setLayout(new BoxLayout(basePanel, Y_AXIS));
        
        // parameters panel -> holds all commit parameters specific 
        // for the given VCS system - message, switches, etc.
        parametersPane1.setLayout(new BorderLayout());
        parametersPane1.add(parameters.getPanel());                
        parametersPane1.setAlignmentX(LEFT_ALIGNMENT);        
        basePanel.add(parametersPane1);
        
        // files table        
        FilesPanel filesPanel = new FilesPanel(this, filters, parameters.getPanel().getPreferredSize().height);
        basePanel.add(makeVerticalStrut(parametersPane1, filesPanel, RELATED, this));        
        basePanel.add(filesPanel);
        
        // hooks area
        int hooksWidth = -1;
        if(!hooks.isEmpty()) {
            HookPanel hooksPanel = new HookPanel(this, hooks, hooksContext);
            hooksPanel.setAlignmentX(LEFT_ALIGNMENT);
            basePanel.add(hooksPanel);
            basePanel.add(makeVerticalStrut(hooksPanel, errorLabel, RELATED, this));
            hooksWidth = hooksPanel.getPreferedWidth();
        } else {
            basePanel.add(makeVerticalStrut(filesPanel, errorLabel, RELATED, this));            
        }
        
        // bottom panel -> error label, progres, ...
        JPanel bottomPanel = new VerticallyNonResizingPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, Y_AXIS));
        bottomPanel.add(progressPanel);
        bottomPanel.add(errorLabel);
        errorLabel.setAlignmentY(CENTER_ALIGNMENT);                
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/versioning/util/resources/error.gif", false)); // NOI18N        
        
        progressPanel.setAlignmentY(LEFT_ALIGNMENT);        
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setBorder(createEmptyBorder(10,           // top
                                    getContainerGap(WEST),    // left
                                    0,                        // bottom
                                    getContainerGap(EAST)));  // right
        
        basePanel.add(bottomPanel);
        
        int baseWidth = basePanel.getPreferredSize().width;
        int baseHeight = basePanel.getPreferredSize().height;
        int parametersWidth = parametersPane1.getPreferredSize().width;
        
        if(baseWidth < parametersWidth) baseWidth = parametersWidth;
        if(baseWidth < hooksWidth) baseWidth = hooksWidth;
        basePanel.setPreferredSize(new Dimension(baseWidth, baseHeight));
        
        setLayout(new BoxLayout(this, Y_AXIS));
        add(basePanel);  
        
        
    }// </editor-fold>

    static Component makeVerticalStrut(JComponent compA,
                                        JComponent compB,
                                        ComponentPlacement relatedUnrelated, 
                                        JPanel parent) {
        int height = LayoutStyle.getInstance().getPreferredGap(
                            compA,
                            compB,
                            relatedUnrelated,
                            SOUTH,
                            parent);
        return Box.createVerticalStrut(height);
    }

    private static Component makeFlexibleHorizontalStrut(int minWidth,
                                                  int prefWidth,
                                                  int maxWidth) {
        return new Box.Filler(new Dimension(minWidth,  0),
                              new Dimension(prefWidth, 0),
                              new Dimension(maxWidth,  0));
    }

    static Component makeHorizontalStrut(JComponent compA,
                                              JComponent compB,
                                              ComponentPlacement relatedUnrelated,
                                              JPanel parent) {
            int width = LayoutStyle.getInstance().getPreferredGap(
                                compA,
                                compB,
                                relatedUnrelated,
                                WEST,
                                parent);
            return Box.createHorizontalStrut(width);
    }
            
    int getContainerGap(int direction) {
        return LayoutStyle.getInstance().getContainerGap(this,
                                                               direction,
                                                               null);
    }

    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(VCSCommitPanel.class, msgKey);
    }
    
    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == tabbedPane && tabbedPane.getSelectedComponent() == basePanel) {
            if(diffProvider != null) {
                commitTable.setModifiedFiles(diffProvider.getModifiedFiles());
            }
        } else if(e.getSource() == parameters || e.getSource() == commitTable) {
            valuesChanged();    
        }
    }

    private void valuesChanged() {
        String errroMsg = null;
        boolean commitable = true;
        try {
            commitable = parameters.isCommitable();            
            if(!commitable) {
                errroMsg = parameters.getErrorMessage();
                return;
            } 
            
            commitable &= commitTable.containsCommitable();
            errroMsg = commitTable.getErrorMessage();
            if(!commitable) {
                return;
            }             
            
        } finally {
            commitButton.setEnabled(commitable);
            setErrorLabel(errroMsg != null ? errroMsg : "");             // NOI18N            
        }        
    }
    
    @NbBundle.Messages({
        "VCSCommitPanel.DiffTab.name=Diff"
    })
    void openDiff (F[] nodes, List<F> allNodes) {
        if(diffProvider == null) {
            return;
        }
        boolean newDiff = false;
        
        F[] nodeArray = allNodes.toArray((F[]) java.lang.reflect.Array.newInstance((Class<F>) nodes.getClass().getComponentType(), allNodes.size()));
        JComponent component = diffProvider.getDiffComponent(nodeArray); 
        if (component == null) {
            // fallback to old behavior (one by one tabs)
            for (VCSFileNode node : nodes) {
                initializeCommitPane();
                File file = node.getFile();
                component = diffProvider.getDiffComponent(file); 
                if (component != null) {
                    if (tabbedPane.indexOfComponent(component) == -1) {
                        tabbedPane.addTab(file.getName(), component);
                    }
                    tabbedPane.setSelectedComponent(component);
                    tabbedPane.requestFocusInWindow();
                    newDiff = true;
                }
            }
        } else {
            initializeCommitPane();
            if (nodes.length > 0) {
                diffProvider.selectFile(nodes[0].getFile());
            }
            if (tabbedPane.indexOfComponent(component) == -1) {
                int existingTab = tabbedPane.indexOfTab(Bundle.VCSCommitPanel_DiffTab_name());
                if (existingTab != -1) {
                    tabbedPane.remove(existingTab);
                }
                tabbedPane.addTab(Bundle.VCSCommitPanel_DiffTab_name(), component);
            }
            tabbedPane.setSelectedComponent(component);
            tabbedPane.requestFocusInWindow();
            newDiff = true;
        }
        if(newDiff) {
            revalidate();
            repaint();
        }
    }

    private void initializeCommitPane () {
        if (tabbedPane == null) {
            tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
            tabbedPane.addPropertyChangeListener(this);
            tabbedPane.addTab(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.TABS_MAIN_NAME), basePanel);
            tabbedPane.setPreferredSize(basePanel.getPreferredSize());
            add(tabbedPane);
            tabbedPane.addChangeListener(this);
        }
    }
    
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        boolean ret = super.processKeyBinding(ks, e, condition, pressed);

        // XXX #250546 Reason of overriding: to process global shortcut.
        if ((JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT == condition) && (ret == false) && !e.isConsumed()) {

            Keymap km = Lookup.getDefault().lookup(Keymap.class);
            Action action = (km != null) ? km.getAction(ks) : null;

            if (action == null) {
                return false;
            }

            if (action instanceof CallbackSystemAction) {
                CallbackSystemAction csAction = (CallbackSystemAction) action;
                if (tabbedPane != null) {
                    Action a = tabbedPane.getActionMap().get(csAction.getActionMapKey());
                    if (a != null) {
                        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, Utilities.keyToString(ks)));
                        return true;
                    }
                }
            }
            return false;
        } else {
            return ret;
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (TabbedPaneFactory.PROP_CLOSE.equals(evt.getPropertyName())) {
            JComponent comp = (JComponent) evt.getNewValue();
            removeTab(comp);
        }
    }
    
    private void removeTab (JComponent comp) {
        if (basePanel != comp && tabbedPane != null && tabbedPane.getTabCount() > 1) {
            tabbedPane.remove(comp);
            revalidate();
        }
    }

    /**
     * Returns true if trying to commit from the commit tab or the user confirmed his action
     * @return
     */
    boolean canCommit() {
        boolean result = true;
        if (tabbedPane != null && tabbedPane.getSelectedComponent() != basePanel) {
            NotifyDescriptor nd = new NotifyDescriptor(modifier.getMessage(VCSCommitPanelModifier.BundleMessage.MESSAGE_FINISHING_FROM_DIFF),
                    modifier.getMessage(VCSCommitPanelModifier.BundleMessage.MESSAGE_FINISHING_FROM_DIFF_TITLE),
                    NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null, NotifyDescriptor.YES_OPTION);
            result = NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd);
        }
        return result;
    }

    public boolean open(VCSContext context, HelpCtx helpCtx) {
        String contentTitle = Utils.getContextDisplayName(context);
        return open(context, helpCtx, org.openide.util.NbBundle.getMessage(VCSCommitPanel.class, "CTL_CommitDialog_Title", contentTitle)); // NOI18N
    }

    public boolean open(VCSContext context, HelpCtx helpCtx, String title) {
        final DialogDescriptor dd = new DialogDescriptor(this,
              title,
              true,
              new Object[] {commitButton, cancelButton},
              commitButton,
              DialogDescriptor.DEFAULT_ALIGN,
              helpCtx,
              null);
        ActionListener al;
        dd.setButtonListener(al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dd.setClosingOptions(new Object[] {commitButton, cancelButton});
                SaveCookie[] saveCookies = diffProvider.getSaveCookies();
                if (cancelButton == e.getSource()) {
                    if (saveCookies.length > 0) {
                        if (SaveBeforeClosingDiffConfirmation.allSaved(saveCookies) || !isShowing()) {
                            EditorCookie[] editorCookies = diffProvider.getEditorCookies();
                            for (EditorCookie cookie : editorCookies) {
                                cookie.open();
                            }
                        } else {
                            dd.setClosingOptions(new Object[0]);
                        }
                    }
                    dd.setValue(cancelButton);
                } else if (commitButton == e.getSource()) {
                    if (saveCookies.length > 0 && !SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
                        dd.setClosingOptions(new Object[0]);
                    } else if (!canCommit()) {
                        dd.setClosingOptions(new Object[0]);
                    }
                    dd.setValue(commitButton);
                }
            }
        });
        
        final VCSCommitTable table = getCommitTable();
        computeNodes();
        addVersioningListener(new VersioningListener() {
            @Override
            public void versioningEvent(VersioningEvent event) {
                valuesChanged();
            }
        });
        table.getTableModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                valuesChanged();
            }
        });

        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        WindowListener windowListener = new DialogBoundsPreserver(preferences, this.getClass().getName());
        dialog.addWindowListener(windowListener);
        dialog.pack();
        windowListener.windowOpened(new WindowEvent(dialog, WindowEvent.WINDOW_OPENED));
        dialog.setVisible(true);
        
        if (dd.getValue() == DialogDescriptor.CLOSED_OPTION) {
            al.actionPerformed(new ActionEvent(cancelButton, ActionEvent.ACTION_PERFORMED, null));
        }
        return dd.getValue() == commitButton;
    }

    VCSCommitPanelModifier getModifier () {
        return modifier;
    }

    private static String getColorString (Color c) {
        return "#" + getHex(c.getRed()) + getHex(c.getGreen()) + getHex(c.getBlue()); //NOI18N
    }

    private static String getHex (int i) {
        String hex = Integer.toHexString(i & 0x000000FF);
        if (hex.length() == 1) {
            hex = "0" + hex; //NOI18N
        }
        return hex;
    }
      
}
