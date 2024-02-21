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

package org.netbeans.modules.mercurial.ui.commit;

import javax.swing.LayoutStyle;
import javax.swing.Icon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.versioning.util.UndoRedoSupport;
import javax.swing.event.ChangeEvent;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.versioning.util.TemplateSelector;
import java.awt.Component;
import java.awt.Container;
import javax.swing.Box;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versioning.util.VerticallyNonResizingPanel;
import org.openide.cookies.SaveCookie;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.versioning.hooks.HgHook;
import org.netbeans.modules.versioning.hooks.HgHookContext;
import org.netbeans.modules.mercurial.ui.diff.MultiDiffPanel;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.spellchecker.api.Spellchecker;
import org.netbeans.modules.versioning.util.AutoResizingPanel;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.netbeans.modules.versioning.util.common.SectionButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.util.ImageUtilities;
import static java.awt.Component.BOTTOM_ALIGNMENT;
import static java.awt.Component.CENTER_ALIGNMENT;
import static java.awt.Component.LEFT_ALIGNMENT;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;
import javax.swing.KeyStroke;
import static javax.swing.SwingConstants.SOUTH;
import static javax.swing.SwingConstants.WEST;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.modules.versioning.util.common.CommitMessageMouseAdapter;
import org.openide.awt.TabbedPaneFactory;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.CallbackSystemAction;

/**
 *
 * @author  pk97937
 * @author  Marian Petras
 */
public class CommitPanel extends AutoResizingPanel implements PreferenceChangeListener, TableModelListener,
        ChangeListener, ActionListener, PropertyChangeListener, DocumentListener {

    private final AutoResizingPanel basePanel = new AutoResizingPanel();
    static final Object EVENT_SETTINGS_CHANGED = new Object();
    private static final boolean DEFAULT_DISPLAY_FILES = true;
    private static final boolean DEFAULT_DISPLAY_HOOKS = false;
    private static final Icon ICON_INFO = ImageUtilities.loadImageIcon("org/netbeans/modules/mercurial/resources/icons/info.png", true); //NOI18N
    private static final Icon ICON_WARNING = ImageUtilities.loadImageIcon("org/netbeans/modules/mercurial/resources/icons/warning.gif", true); //NOI18N

    final JLabel filesLabel = new JLabel();
    final PlaceholderPanel progressPanel = new PlaceholderPanel();
    private final JPanel filesPanel = new JPanel(new GridLayout(1, 1));
    private final SectionButton filesSectionButton = new SectionButton();
    private final JPanel filesSectionPanel2 = new JPanel();
    private final PlaceholderPanel hookSectionPanel = new PlaceholderPanel();
    private final SectionButton hooksSectionButton = new SectionButton();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel jLabel2 = new JLabel();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final JTextArea messageTextArea = new JTextArea();
    private final JLabel recentLink = new JLabel();
    private final JLabel templateLink = new JLabel();
    final JCheckBox cbAllFiles = new JCheckBox();
    final JCheckBox cbAuthor = new JCheckBox();
    final JComboBox cmbUser = new JComboBox();
    
    private CommitTable commitTable;
    private Collection<HgHook> hooks = Collections.emptyList();
    private HgHookContext hookContext;
    private JTabbedPane tabbedPane;
    private HashMap<File, MultiDiffPanel> displayedDiffs = new HashMap<File, MultiDiffPanel>();
    private UndoRedoSupport um;
    private String warningMessage;
    private boolean userValid;
    private String user;

    /** Creates new form CommitPanel */
    public CommitPanel() {
        initComponents();
        initInteraction();
    }

    void setCommitTable(CommitTable commitTable) {
        this.commitTable = commitTable;
    }
    
    void setErrorLabel(String htmlErrorLabel) {
        jLabel2.setText(htmlErrorLabel);
        if (htmlErrorLabel == null || htmlErrorLabel.isEmpty()) {
            jLabel2.setIcon(null);
            displayWarning();
        } else {
            jLabel2.setIcon(ICON_INFO);
        }
    }

    void setWarningMessage (String message) {
        this.warningMessage = message;
        displayWarning();
    } 

    @Override
    public void addNotify() {
        super.addNotify();

        HgModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        commitTable.getTableModel().addTableModelListener(this);
        listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        initCollapsibleSections();
        TemplateSelector ts = new TemplateSelector(HgModuleConfig.getDefault().getPreferences());
        if (ts.isAutofill()) {
            messageTextArea.setText(ts.getTemplate());
        } else {
            String lastCommitMessage = HgModuleConfig.getDefault().getLastCanceledCommitMessage(CommitAction.KEY_CANCELED_MESSAGE);
            if (lastCommitMessage.isEmpty() && new StringSelector.RecentMessageSelector(HgModuleConfig.getDefault().getPreferences()).isAutoFill()) {
                List<String> messages = Utils.getStringList(HgModuleConfig.getDefault().getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES);
                if (messages.size() > 0) {
                    lastCommitMessage = messages.get(0);
                }
            }
            messageTextArea.setText(lastCommitMessage);
        }
        messageTextArea.selectAll();
        um = UndoRedoSupport.register(messageTextArea);
    }

    void closed () {
        for (Map.Entry<File, MultiDiffPanel> e : displayedDiffs.entrySet()) {
            e.getValue().componentClosed();
        }
    }

    boolean isUserValid () {
        return userValid || !cbAuthor.isSelected();
    }

    String getUser () {
        return cbAuthor.isSelected() && isUserValid() ? user: null;
    }

    private void initCollapsibleSections() {
        initSectionButton(filesSectionButton, filesSectionPanel2,
                          "initFilesPanel",                             //NOI18N
                          DEFAULT_DISPLAY_FILES);
        if (!hooks.isEmpty()) {
            Mnemonics.setLocalizedText(hooksSectionButton, (hooks.size() == 1)
                                           ? hooks.iterator().next().getDisplayName()
                                           : getMessage("LBL_Advanced")); // NOI18N                 
            initSectionButton(hooksSectionButton, hookSectionPanel,
                              "initHooksPanel",                         //NOI18N
                              DEFAULT_DISPLAY_HOOKS);
        } else {
            hooksSectionButton.setVisible(false);
        }
    }

    private void initSectionButton(final SectionButton button,
                                   final JPanel panel,
                                   final String initPanelMethodName,
                                   final boolean defaultSectionDisplayed) {
        if (defaultSectionDisplayed) {
            displaySection(panel, initPanelMethodName);
        } else {
            hideSection(panel);
        }
        button.setSelected(defaultSectionDisplayed);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (panel.isVisible()) {
                    hideSection(panel);
                } else {
                    displaySection(panel, initPanelMethodName);
                }
            }
        });
    }

    private void displaySection(Container sectionPanel,
                                String initPanelMethodName) {
        if (sectionPanel.getComponentCount() == 0) {
            invokeInitPanelMethod(initPanelMethodName);
        }
        sectionPanel.setVisible(true);
        enlargeVerticallyAsNecessary();
    }

    private void hideSection(JPanel sectionPanel) {
        sectionPanel.setVisible(false);
    }

    private void invokeInitPanelMethod(String methodName) {
        try {
            getClass().getDeclaredMethod(methodName).invoke(this);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void initFilesPanel() {

        /* this method is called using reflection from 'invokeInitPanelMethod()' */

        filesPanel.add(commitTable.getComponent());
        filesPanel.setPreferredSize(new Dimension(0, 2 * messageTextArea.getPreferredSize().height));

        filesSectionPanel2.setLayout(new BoxLayout(filesSectionPanel2, Y_AXIS));
        filesSectionPanel2.add(filesLabel);
        filesSectionPanel2.add(makeVerticalStrut(filesLabel, filesPanel, RELATED));
        filesSectionPanel2.add(filesPanel);
        filesSectionPanel2.add(makeVerticalStrut(filesPanel, cbAllFiles, RELATED));
        filesSectionPanel2.add(cbAllFiles);

        filesLabel.setAlignmentX(LEFT_ALIGNMENT);
        filesPanel.setAlignmentX(LEFT_ALIGNMENT);
        cbAllFiles.setAlignmentX(LEFT_ALIGNMENT);
    }

    private void initHooksPanel() {

        /* this method is called using reflection from 'invokeInitPanelMethod()' */

        assert !hooks.isEmpty();

        if (hooks.size() == 1) {
            hookSectionPanel.add(hooks.iterator().next().createComponent(hookContext));
        } else {
            JTabbedPane hooksTabbedPane = new JTabbedPane();
            for (HgHook hook : hooks) {
                hooksTabbedPane.add(hook.createComponent(hookContext),
                                    hook.getDisplayName().replace("&", ""));
            }
            hookSectionPanel.add(hooksTabbedPane);
        }
    }

    String getCommitMessage() {
        return messageTextArea.getText();
    }

    @Override
    public void removeNotify() {
        commitTable.getTableModel().removeTableModelListener(this);
        HgModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        if (um != null) {
            um.unregister();
            um = null;
        }
        super.removeNotify();
    }

    void setHooks(Collection<HgHook> hooks, HgHookContext context) {
        if (hooks == null) {
            hooks = Collections.emptyList();
        }
        this.hooks = hooks;
        this.hookContext = context;
    }

    private void onBrowseRecentMessages() {
        StringSelector.RecentMessageSelector selector = new StringSelector.RecentMessageSelector(HgModuleConfig.getDefault().getPreferences());
        String message = selector.getRecentMessage(getMessage("CTL_CommitForm_RecentTitle"),  // NOI18N
                                               getMessage("CTL_CommitForm_RecentPrompt"),  // NOI18N
            Utils.getStringList(HgModuleConfig.getDefault().getPreferences(), CommitAction.RECENT_COMMIT_MESSAGES));
        if (message != null) {
            messageTextArea.replaceSelection(message);
        }
    }

    private void onTemplate() {
        TemplateSelector ts = new TemplateSelector(HgModuleConfig.getDefault().getPreferences());
        if(ts.show("org.netbeans.modules.mercurial.ui.commit.TemplatePanel")) { //NOI18N
            messageTextArea.setText(ts.getTemplate());
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
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
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI Layout Code">
    private void initComponents() {

        jLabel1.setLabelFor(messageTextArea);
        Mnemonics.setLocalizedText(jLabel1, getMessage("CTL_CommitForm_Message")); // NOI18N

        recentLink.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/recent_messages.png"))); // NOI18N
        recentLink.setToolTipText(getMessage("CTL_CommitForm_RecentMessages")); // NOI18N

        templateLink.setIcon(new ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/load_template.png"))); // NOI18N
        templateLink.setToolTipText(getMessage("CTL_CommitForm_LoadTemplate")); // NOI18N
        
        Mnemonics.setLocalizedText(cbAllFiles, getMessage("CTL_CommitForm_CommitAllFiles.text")); //NOI18N
        cbAllFiles.setToolTipText(getMessage("CTL_CommitForm_CommitAllFiles.TTtext")); // NOI18N
        
        Mnemonics.setLocalizedText(cbAuthor, getMessage("CTL_CommitForm_CommitAuthor.text")); //NOI18N
        cbAuthor.setToolTipText(getMessage("CTL_CommitForm_CommitAuthor.TTtext")); // NOI18N
        cbAuthor.setEnabled(false);
        cmbUser.setEditable(true);
        cmbUser.setEnabled(false);

        messageTextArea.setColumns(70);    //this determines the preferred width of the whole dialog
        messageTextArea.setLineWrap(true);
        messageTextArea.setRows(4);
        messageTextArea.setTabSize(4);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setMinimumSize(new Dimension(100, 18));
        jScrollPane1.setViewportView(messageTextArea);
        messageTextArea.getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitForm_Message")); // NOI18N
        messageTextArea.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitForm_Message")); // NOI18N
        messageTextArea.addMouseListener(new CommitMessageMouseAdapter());

        Mnemonics.setLocalizedText(filesSectionButton, getMessage("LBL_CommitDialog_FilesToCommit")); // NOI18N
        Mnemonics.setLocalizedText(filesLabel, getMessage("CTL_CommitForm_FilesToCommit")); // NOI18N

        Mnemonics.setLocalizedText(hooksSectionButton, getMessage("LBL_Advanced")); // NOI18N

        Mnemonics.setLocalizedText(jLabel2, "jLabel2");

        JPanel topPanel = new VerticallyNonResizingPanel();
        topPanel.setLayout(new BoxLayout(topPanel, X_AXIS));
        topPanel.add(jLabel1);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(recentLink);
        topPanel.add(makeHorizontalStrut(recentLink, templateLink, LayoutStyle.ComponentPlacement.RELATED));
        topPanel.add(templateLink);
        jLabel1.setAlignmentY(BOTTOM_ALIGNMENT);
        recentLink.setAlignmentY(BOTTOM_ALIGNMENT);
        templateLink.setAlignmentY(BOTTOM_ALIGNMENT);
        
        JPanel userPanel = new JPanel() {
            @Override
            public Dimension getMaximumSize () {
                return super.getPreferredSize();
            }
        };
        userPanel.setLayout(new BoxLayout(userPanel, X_AXIS));
        userPanel.add(cbAuthor);
        userPanel.add(makeHorizontalStrut(cbAuthor, cmbUser, LayoutStyle.ComponentPlacement.RELATED));
        userPanel.add(cmbUser);
        cbAuthor.setAlignmentX(LEFT_ALIGNMENT);

        JPanel bottomPanel = new VerticallyNonResizingPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, X_AXIS));
        bottomPanel.add(jLabel2);
        bottomPanel.add(makeFlexibleHorizontalStrut(15, 90, Short.MAX_VALUE));
        bottomPanel.add(progressPanel);
        jLabel2.setAlignmentY(CENTER_ALIGNMENT);
        progressPanel.setAlignmentY(LEFT_ALIGNMENT);

        basePanel.setLayout(new BoxLayout(basePanel, Y_AXIS));
        basePanel.add(topPanel);
        basePanel.add(makeVerticalStrut(jLabel1, jScrollPane1, LayoutStyle.ComponentPlacement.RELATED));
        basePanel.add(jScrollPane1);
        basePanel.add(makeVerticalStrut(jScrollPane1, userPanel, LayoutStyle.ComponentPlacement.UNRELATED));
        basePanel.add(userPanel);
        basePanel.add(makeVerticalStrut(userPanel, filesSectionButton, LayoutStyle.ComponentPlacement.UNRELATED));
        filesSectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, filesSectionButton.getMaximumSize().height));
        basePanel.add(filesSectionButton);
        basePanel.add(makeVerticalStrut(filesSectionButton, filesSectionPanel2, LayoutStyle.ComponentPlacement.RELATED));
        basePanel.add(filesSectionPanel2);
        basePanel.add(makeVerticalStrut(filesSectionPanel2, hooksSectionButton, LayoutStyle.ComponentPlacement.UNRELATED));
        hooksSectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, hooksSectionButton.getMaximumSize().height));
        basePanel.add(hooksSectionButton);
        basePanel.add(makeVerticalStrut(hooksSectionButton, hookSectionPanel, LayoutStyle.ComponentPlacement.RELATED));
        basePanel.add(hookSectionPanel);
        basePanel.add(makeVerticalStrut(hookSectionPanel, jLabel2, LayoutStyle.ComponentPlacement.UNRELATED));
        basePanel.add(bottomPanel);
        setLayout(new BoxLayout(this, Y_AXIS));
        add(basePanel);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        userPanel.setAlignmentX(LEFT_ALIGNMENT);
        jScrollPane1.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        filesSectionPanel2.setAlignmentX(LEFT_ALIGNMENT);
        hooksSectionButton.setAlignmentX(LEFT_ALIGNMENT);
        hookSectionPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        basePanel.setBorder(createEmptyBorder(26,                       //top
                                    getContainerGap(WEST),    //left
                                    0,                        //bottom
                                    15));                     //right

        getAccessibleContext().setAccessibleName(getMessage("ACSN_CommitDialog")); // NOI18N
        getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CommitDialog")); // NOI18N
    }// </editor-fold>

    private void initInteraction() {
        recentLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        recentLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onBrowseRecentMessages();
            }
        });
        templateLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        templateLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onTemplate();
            }
        });
        Spellchecker.register (messageTextArea);
        cbAllFiles.addActionListener(this);
        cbAuthor.addActionListener(this);
        ((JTextComponent) cmbUser.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);
        initActions();
    }

    private Component makeVerticalStrut(JComponent compA,
                                        JComponent compB,
                                        LayoutStyle.ComponentPlacement relatedUnrelated) {
        int height = LayoutStyle.getInstance().getPreferredGap(
                            compA,
                            compB,
                            relatedUnrelated,
                            SOUTH,
                            this);
        return Box.createVerticalStrut(height);
    }

    private Component makeHorizontalStrut(JComponent compA,
                                          JComponent compB,
                                          LayoutStyle.ComponentPlacement relatedUnrelated) {
        int width = LayoutStyle.getInstance().getPreferredGap(
                            compA,
                            compB,
                            relatedUnrelated,
                            WEST,
                            this);
        return Box.createHorizontalStrut(width);
    }

    private Component makeFlexibleHorizontalStrut(int minWidth,
                                                  int prefWidth,
                                                  int maxWidth) {
        return new Box.Filler(new Dimension(minWidth,  0),
                              new Dimension(prefWidth, 0),
                              new Dimension(maxWidth,  0));
    }

    private int getContainerGap(int direction) {
        return LayoutStyle.getInstance().getContainerGap(this,
                                                               direction,
                                                               null);
    }

    private static String getMessage(String msgKey) {
        return NbBundle.getMessage(CommitPanel.class, msgKey);
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
            commitTable.setModifiedFiles(new HashSet<File>(getModifiedFiles().keySet()));
        }
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == cbAllFiles) {
            commitTable.setChangesEnabled(!cbAllFiles.isSelected());
            listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        } else if (e.getSource() == cbAuthor) {
            cmbUser.setEnabled(cbAuthor.isSelected());
            listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
        if (e.getDocument() == ((JTextComponent) cmbUser.getEditor().getEditorComponent()).getDocument()) {
            boolean oldUserValid = userValid;
            user = cmbUser.getEditor().getItem().toString().trim();
            userValid = !user.isEmpty();
            if (userValid != oldUserValid && cbAuthor.isSelected()) {
                listenerSupport.fireVersioningEvent(EVENT_SETTINGS_CHANGED);
            }
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

    void openDiff (HgFileNode[] nodes) {
        for (HgFileNode node : nodes) {
            if (tabbedPane == null) {
                initializeTabs();
            }
            File file = node.getFile();
            MultiDiffPanel panel = displayedDiffs.get(file);
            if (panel == null) {
                panel = new MultiDiffPanel(file, HgRevision.BASE, HgRevision.CURRENT, node.getInformation(), false, -1); // switch the last parameter to true if editable diff works poorly
                displayedDiffs.put(file, panel);
            }
            if (tabbedPane.indexOfComponent(panel) == -1) {
                tabbedPane.addTab(file.getName(), panel);
            }
            tabbedPane.setSelectedComponent(panel);
            tabbedPane.requestFocusInWindow();
            panel.requestActive();
        }
        revalidate();
        repaint();
    }

    /**
     * Returns save cookies available for files in the commit table
     * @return
     */
    SaveCookie[] getSaveCookies() {
        return getModifiedFiles().values().toArray(new SaveCookie[0]);
    }

    /**
     * Returns editor cookies available for modified and not open files in the commit table
     * @return
     */
    EditorCookie[] getEditorCookies() {
        LinkedList<EditorCookie> allCookies = new LinkedList<EditorCookie>();
        for (Map.Entry<File, MultiDiffPanel> e : displayedDiffs.entrySet()) {
            EditorCookie[] cookies = e.getValue().getEditorCookies(true);
            if (cookies.length > 0) {
                allCookies.add(cookies[0]);
            }
        }
        return allCookies.toArray(new EditorCookie[0]);
    }

    /**
     * Returns true if trying to commit from the commit tab or the user confirmed his action
     * @return
     */
    boolean canCommit() {
        boolean result = true;
        if (tabbedPane != null && tabbedPane.getSelectedComponent() != basePanel) {
            NotifyDescriptor nd = new NotifyDescriptor(NbBundle.getMessage(CommitPanel.class, "MSG_CommitDialog_CommitFromDiff"), //NOI18N
                    NbBundle.getMessage(CommitPanel.class, "LBL_CommitDialog_CommitFromDiff"), //NOI18N
                    NotifyDescriptor.YES_NO_OPTION, NotifyDescriptor.QUESTION_MESSAGE, null, NotifyDescriptor.YES_OPTION);
            result = NotifyDescriptor.YES_OPTION == DialogDisplayer.getDefault().notify(nd);
        }
        return result;
    }

    private void initializeTabs () {
         tabbedPane = TabbedPaneFactory.createCloseButtonTabbedPane();
         tabbedPane.addPropertyChangeListener(this);
         tabbedPane.addTab(NbBundle.getMessage(CommitPanel.class, "CTL_CommitDialog_Tab_Commit"), basePanel); //NOI18N
         tabbedPane.setPreferredSize(basePanel.getPreferredSize());
         add(tabbedPane);
         tabbedPane.addChangeListener(this);
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

    private HashMap<File, SaveCookie> getModifiedFiles () {
        HashMap<File, SaveCookie> modifiedFiles = new HashMap<File, SaveCookie>();
        for (Map.Entry<File, MultiDiffPanel> e : displayedDiffs.entrySet()) {
            SaveCookie[] cookies = e.getValue().getSaveCookies(false);
            if (cookies.length > 0) {
                modifiedFiles.put(e.getKey(), cookies[0]);
            }
        }
        return modifiedFiles;
    }

    private void displayWarning () {
        if (warningMessage != null && jLabel2.getText().isEmpty()) {
            jLabel2.setText(warningMessage);
            jLabel2.setIcon(ICON_WARNING);
        }
    }

    private void initActions () {
        InputMap inputMap = getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        ActionMap actionMap = getActionMap();
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_R, KeyEvent.ALT_DOWN_MASK, false ), "messageHistory" ); //NOI18N
        actionMap.put("messageHistory", new AbstractAction() { //NOI18N
            @Override
            public void actionPerformed (ActionEvent e) {
                onBrowseRecentMessages();
            }
        });
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_L, KeyEvent.ALT_DOWN_MASK, false ), "messageTemplate" ); //NOI18N
        actionMap.put("messageTemplate", new AbstractAction() { //NOI18N
            @Override
            public void actionPerformed (ActionEvent e) {
                onTemplate();
            }
        });
    }
}
