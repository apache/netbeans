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

package org.netbeans.modules.jumpto.file;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.jumpto.SearchHistory;
import org.netbeans.modules.jumpto.common.UiUtils;
import org.netbeans.spi.jumpto.file.FileDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Pair;

/**
 *
 * @author  Petr Hrebejk
 * @author  Andrei Badea
 * @author  Tomas Zezula
 */
public class FileSearchPanel extends javax.swing.JPanel implements ActionListener {

    @StaticResource
    private static final String WAIT_ICON_RES = "org/netbeans/modules/jumpto/resources/wait.gif";    // NOI18N
    private static final String GLOBAL_OPTIONS_CATEGORY = "Editor/goto"; //NOI18N
    private static Icon WAIT_ICON = ImageUtilities.loadImageIcon(WAIT_ICON_RES, false);
    public static final String SEARCH_IN_PROGRES = NbBundle.getMessage(FileSearchPanel.class, "TXT_SearchingOtherProjects"); // NOI18N
    private static Icon WARN_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/warning.png", false); // NOI18N
    private static final Logger LOG = Logger.getLogger(FileSearchPanel.class.getName());
    private static final int BRIGHTER_COLOR_COMPONENT = 10;
    private final ContentProvider contentProvider;
    private final Project currentProject;
    private boolean containsScrollPane;
    private final JLabel messageLabel;
    private List<?> selectedItems;
    /* package */ long time;

    private FileDescriptor[] selectedFile;

    private final SearchHistory searchHistory;

    // handling http://netbeans.org/bugzilla/show_bug.cgi?id=203119
    // if the whole search argument (in the fileName JTextField) is selected and something is pasted in it's place,
    // notify the DocumentListener because it will first call removeUpdate() and then inserteUpdate().
    // When removeUpdate() is called we should not call update() because it messes the messageLabel's text.
    private boolean pastedFromClipboard = false;

    public FileSearchPanel(ContentProvider contentProvider, Project currentProject) {
        this.contentProvider = contentProvider;
        this.currentProject = currentProject;

        initComponents();
        ((AbstractDocument)fileNameTextField.getDocument()).setDocumentFilter(UiUtils.newUserInputFilter());
        this.containsScrollPane = true;
        Color bgColorBrighter = new Color(
                                    Math.min(getBackground().getRed() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getGreen() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getBlue() + BRIGHTER_COLOR_COMPONENT, 255)
                            );
        messageLabel = new JLabel();
        messageLabel.setBackground(bgColorBrighter);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(true);
        messageLabel.setText(NbBundle.getMessage(FileSearchPanel.class, "TXT_NoTypesFound")); // NOI18N
        messageLabel.setFont(resultList.getFont());

        caseSensitiveCheckBox.setSelected(FileSearchOptions.getCaseSensitive());
        hiddenFilesCheckBox.setSelected(FileSearchOptions.getShowHiddenFiles());
        mainProjectCheckBox.setSelected(FileSearchOptions.getPreferMainProject());
        searchByFolders.setSelected(FileSearchOptions.getSearchByFolders());

        if ( currentProject == null ) {
            mainProjectCheckBox.setEnabled(false);
            mainProjectCheckBox.setSelected(false);
        } else {
            mainProjectCheckBox.setText(NbBundle.getMessage(
                FileSearchPanel.class,
                "FMT_CurrentProjectLabel",
                ProjectUtils.getInformation(currentProject).getDisplayName()));
        }

        mainProjectCheckBox.addActionListener(this);
        caseSensitiveCheckBox.addActionListener(this);
        hiddenFilesCheckBox.addActionListener(this);
        hiddenFilesCheckBox.setVisible(false);
	searchByFolders.addActionListener(this);

        resultList.setCellRenderer( contentProvider.getListCellRenderer(
                resultList,
                fileNameTextField.getDocument(),
                caseSensitiveCheckBox.getModel(),
                mainProjectCheckBox.getModel(),
                searchByFolders.getModel()));
        resultList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedItems = resultList.getSelectedValuesList();
                LOG.log(
                    Level.FINE,
                    "New selected items: {0}",  //NOI18N
                    selectedItems);
            }
        });
        contentProvider.setListModel(this, null, false);

        fileNameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            public void insertUpdate(DocumentEvent e) {
                update();
            }

            public void removeUpdate(DocumentEvent e) {
                // handling http://netbeans.org/bugzilla/show_bug.cgi?id=203119
                if (pastedFromClipboard) {
                    pastedFromClipboard = false;
                } else {
                    update();
                }
            }
        });

        searchHistory = new SearchHistory(FileSearchPanel.class, fileNameTextField);
    }

    @Override
    public void removeNotify() {
        searchHistory.saveHistory();
        super.removeNotify();
    }


    void revalidateModel(final boolean done) {
        setModel(resultList.getModel(), done);
    }

    //Good for setting model form any thread
    void setModel(
            @NonNull final ListModel model,
            final boolean done) {
        // XXX measure time here
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                LOG.log(
                    Level.FINE,
                    "Reset selected items");    //NOI18N
                selectedItems = null;
                resultList.setModel(model);
                if (done) {
                    setListPanelContent(null,false);
                }
            }
        });
    }

    void searchProgress() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                if (resultList.getModel().getSize() > 0) {
                    if (!containsScrollPane) {
                        setListPanelContent(null,false);
                        setWarning(NbBundle.getMessage(
                            FileSearchPanel.class,
                            "TXT_PartialResults"));
                    }
                    final int index = resultList.getSelectedIndex();
                    if (index == -1) {
                        LOG.log(
                            Level.FINE,
                            "Select first item.");  //NOI18N
                        resultList.setSelectedIndex(0);
                    } else if (selectedItems != null && !selectedItems.isEmpty()) {
                        LOG.log(
                            Level.FINE,
                            "Reselect selected items"); //NOI18N
                        final int[] indexes = new int[selectedItems.size()];
                        final ListModel<?> model = resultList.getModel();
                        int startj = 0, i = 0;
                        for (Object selectedItem : selectedItems) {
                            for (int j = startj; j<model.getSize(); j++) {
                                if (selectedItem == model.getElementAt(j)) {
                                    startj = j;
                                    indexes[i] = j;
                                    break;
                                }
                            }
                            i++;
                        }
                        resultList.setSelectedIndices(indexes);
                        resultList.ensureIndexIsVisible(indexes[0]);
                    }
                }
            }
        });
    }

    boolean searchCompleted(final boolean success) {
        assert SwingUtilities.isEventDispatchThread();
        setWarning(null);
        boolean res;
        if (success) {
            res = true;
            String msg = null;
            if (resultList.getModel().getSize() == 0) {
                try {
                   Pattern.compile(getText().replace(".", "\\.").replace( "*", ".*" ).replace( '?', '.' ), Pattern.CASE_INSENSITIVE); // NOI18N
                   msg = NbBundle.getMessage(FileSearchPanel.class, "TXT_NoTypesFound");
               } catch (PatternSyntaxException pse) {
                   msg = NbBundle.getMessage(FileSearchPanel.class, "TXT_SyntaxError", pse.getDescription(),pse.getIndex());
               }
               res = false;
            } else if (resultList.getSelectedIndex() == -1) {
                resultList.setSelectedIndex(0);
            }
            setListPanelContent(msg, false);
        } else {
            res = false;
        }
        return res;
    }

    private void setListPanelContent( String message, boolean waitIcon ) {
        if ( message == null && !containsScrollPane ) {
           listPanel.remove( messageLabel );
           listPanel.add( resultScrollPane );
           containsScrollPane = true;
           revalidate();
           repaint();
        }
        else if ( message != null ) {
           jTextFieldLocation.setText("");
           messageLabel.setText(message);
           messageLabel.setIcon( waitIcon ? WAIT_ICON : null);
           if ( containsScrollPane ) {
               listPanel.remove( resultScrollPane );
               listPanel.add( messageLabel );
               containsScrollPane = false;
           }
           revalidate();
           repaint();
       }
    }

    public boolean isShowHiddenFiles() {
        return hiddenFilesCheckBox.isSelected();
    }

    public boolean isPreferedProject() {
        return mainProjectCheckBox.isSelected();
    }

    public boolean isCaseSensitive() {
        return caseSensitiveCheckBox.isSelected();
    }

    public boolean isSearchByFolders() {
        return searchByFolders.isSelected();
    }

    private void update() {
        update(false);
    }

    // Forcing a refresh is costly, but reloads the configuration settings
    private void update(boolean forceRefresh) {
        time = System.currentTimeMillis();
        final String text = getText();
        if (contentProvider.setListModel(this, text, forceRefresh)) {
            setListPanelContent(NbBundle.getMessage(FileSearchPanel.class, "TXT_Searching"),true);
        }
    }

    private void setWarning(String warningMessage) {
        if (warningMessage != null) {
            jLabelWarningMessage.setIcon(WARN_ICON);
            jLabelWarningMessage.setBorder(
                BorderFactory.createEmptyBorder(3, 1, 1, 1));
        } else {
            jLabelWarningMessage.setIcon(null);
            jLabelWarningMessage.setBorder(null);
        }
        jLabelWarningMessage.setText(warningMessage);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fileNameLabel = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        resultLabel = new javax.swing.JLabel();
        listPanel = new javax.swing.JPanel();
        resultScrollPane = new javax.swing.JScrollPane();
        resultList = new javax.swing.JList();
        jLabelWarningMessage = new javax.swing.JLabel();
        caseSensitiveCheckBox = new javax.swing.JCheckBox();
        hiddenFilesCheckBox = new javax.swing.JCheckBox();
        mainProjectCheckBox = new javax.swing.JCheckBox();
        searchByFolders = new javax.swing.JCheckBox();
        gotoSettingsBtn = new javax.swing.JButton();
        jLabelLocation = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setPreferredSize(new java.awt.Dimension(575, 280));
        setLayout(new java.awt.GridBagLayout());

        fileNameLabel.setFont(fileNameLabel.getFont());
        fileNameLabel.setLabelFor(fileNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileNameLabel, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "CTL_FileName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(fileNameLabel, gridBagConstraints);

        fileNameTextField.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        fileNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNameTextFieldActionPerformed(evt);
            }
        });
        fileNameTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fileNameTextFieldKeyPressed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        add(fileNameTextField, gridBagConstraints);
        fileNameTextField.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AN_SearchText")); // NOI18N
        fileNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_SearchText")); // NOI18N

        resultLabel.setLabelFor(resultList);
        org.openide.awt.Mnemonics.setLocalizedText(resultLabel, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "CTL_MatchingFiles")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(resultLabel, gridBagConstraints);

        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        listPanel.setLayout(new java.awt.BorderLayout());

        resultScrollPane.setBorder(null);

        resultList.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        resultList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                resultListMouseReleased(evt);
            }
        });
        resultList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                resultListValueChanged(evt);
            }
        });
        resultScrollPane.setViewportView(resultList);
        resultList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AN_MatchingList")); // NOI18N
        resultList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_MatchingList")); // NOI18N

        listPanel.add(resultScrollPane, java.awt.BorderLayout.CENTER);
        listPanel.add(jLabelWarningMessage, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(listPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitiveCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_CaseSensitive")); // NOI18N
        caseSensitiveCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        add(caseSensitiveCheckBox, gridBagConstraints);
        caseSensitiveCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_CaseSensitive")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hiddenFilesCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_HiddenFiles")); // NOI18N
        hiddenFilesCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 0);
        add(hiddenFilesCheckBox, gridBagConstraints);
        hiddenFilesCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_HiddenFiles")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(mainProjectCheckBox, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_PreferMainProject")); // NOI18N
        mainProjectCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 0);
        add(mainProjectCheckBox, gridBagConstraints);
        mainProjectCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_PreferMainProject")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(searchByFolders, "Search by Folders");
        searchByFolders.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 8, 0, 0);
        add(searchByFolders, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(gotoSettingsBtn, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "CTL_Manage")); // NOI18N
        gotoSettingsBtn.setMargin(new java.awt.Insets(2, 10, 2, 10));
        gotoSettingsBtn.setMaximumSize(new java.awt.Dimension(80, 24));
        gotoSettingsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gotoSettingsBtnActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 8, 0, 0);
        add(gotoSettingsBtn, gridBagConstraints);

        jLabelLocation.setLabelFor(jTextFieldLocation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelLocation, org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "LBL_Location")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 4, 0);
        add(jLabelLocation, gridBagConstraints);
        jLabelLocation.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AN_Location")); // NOI18N
        jLabelLocation.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FileSearchPanel.class, "AD_Location")); // NOI18N

        jTextFieldLocation.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jTextFieldLocation, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void fileNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNameTextFieldActionPerformed
    if (contentProvider.hasValidContent()) {
        contentProvider.closeDialog();
        setSelectedFile();
    }
}//GEN-LAST:event_fileNameTextFieldActionPerformed

private void resultListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultListMouseReleased
    if ( evt.getClickCount() == 2 ) {
        fileNameTextFieldActionPerformed(null);
    }
}//GEN-LAST:event_resultListMouseReleased

private void resultListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_resultListValueChanged
        final Object svObject = resultList.getSelectedValue();
        if ( svObject instanceof FileDescriptor ) {
            jTextFieldLocation.setText(((FileDescriptor)svObject).getFileDisplayPath());
        } else {
            jTextFieldLocation.setText(""); //NOI18N
        }
}//GEN-LAST:event_resultListValueChanged

    @CheckForNull
    private Pair<String,JComponent> listActionFor(KeyEvent ev) {
        InputMap map = resultList.getInputMap();
        Object o = map.get(KeyStroke.getKeyStrokeForEvent(ev));
        if (o instanceof String) {
            return Pair.<String,JComponent>of((String)o, resultList);
        }
        map = resultScrollPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        o = map.get(KeyStroke.getKeyStrokeForEvent(ev));
        if (o instanceof String) {
            return Pair.<String,JComponent>of((String)o, resultScrollPane);
        }
        return null;
    }

    private void fileNameTextFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fileNameTextFieldKeyPressed
        final Pair<String,JComponent> p = listActionFor(evt);
        final String actionKey = p == null ? null : p.first();
        final JComponent actionTarget = p == null ? null : p.second();

        // see JavaFastOpen.boundScrollingKey()
        boolean isListScrollAction =
            "selectPreviousRow".equals(actionKey) || // NOI18N
            "selectPreviousRowExtendSelection".equals(actionKey) || // NOI18N
            "selectNextRow".equals(actionKey) || // NOI18N
            "selectNextRowExtendSelection".equals(actionKey) || // NOI18N
            "scrollUp".equals(actionKey) || // NOI18N
            "scrollUpExtendSelection".equals(actionKey) || // NOI18N
            "scrollDown".equals(actionKey) || // NOI18N
            "scrollDownExtendSelection".equals(actionKey); // NOI18N

        int selectedIndex = resultList.getSelectedIndex();
        ListModel model = resultList.getModel();
        int modelSize = model.getSize();

        // Wrap around
        if ( "selectNextRow".equals(actionKey) &&
              ( selectedIndex == modelSize - 1 ||
                ( selectedIndex == modelSize - 2 &&
                  model.getElementAt(modelSize - 1) == SEARCH_IN_PROGRES )
             ) ) {
            resultList.setSelectedIndex(0);
            resultList.ensureIndexIsVisible(0);
            return;
        }
        else if ( "selectPreviousRow".equals(actionKey) &&
                   selectedIndex == 0 ) {
            int last = modelSize - 1;
            if ( model.getElementAt(last) == SEARCH_IN_PROGRES ) {
                last--;
            }
            resultList.setSelectedIndex(last);
            resultList.ensureIndexIsVisible(last);
            return;
        }

        if (isListScrollAction) {
            assert actionTarget != null;
            final Action a = actionTarget.getActionMap().get(actionKey);
            a.actionPerformed(new ActionEvent(actionTarget, 0, (String)actionKey));
            evt.consume();
        } else {
            //handling http://netbeans.org/bugzilla/show_bug.cgi?id=203119
            Object o = fileNameTextField.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));
            if (o instanceof String) {
                String action = (String) o;
                if ("paste-from-clipboard".equals(action)) {
                    String selectedTxt = fileNameTextField.getSelectedText();
                    String txt = fileNameTextField.getText();
                    if (selectedTxt != null && txt != null) {
                        if (selectedTxt.length() == txt.length()) {
                            pastedFromClipboard = true;
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_fileNameTextFieldKeyPressed

    private void gotoSettingsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gotoSettingsBtnActionPerformed
        OptionsDisplayer.getDefault().open(GLOBAL_OPTIONS_CATEGORY, true);

        update(true);
    }//GEN-LAST:event_gotoSettingsBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox caseSensitiveCheckBox;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JButton gotoSettingsBtn;
    private javax.swing.JCheckBox hiddenFilesCheckBox;
    private javax.swing.JLabel jLabelLocation;
    private javax.swing.JLabel jLabelWarningMessage;
    private javax.swing.JTextField jTextFieldLocation;
    private javax.swing.JPanel listPanel;
    private javax.swing.JCheckBox mainProjectCheckBox;
    private javax.swing.JLabel resultLabel;
    private javax.swing.JList resultList;
    private javax.swing.JScrollPane resultScrollPane;
    private javax.swing.JCheckBox searchByFolders;
    // End of variables declaration//GEN-END:variables

    @Override
    public void actionPerformed(ActionEvent e) {
        if ( e.getSource() == caseSensitiveCheckBox ) {
            FileSearchOptions.setCaseSensitive(caseSensitiveCheckBox.isSelected());
        }
        else if ( e.getSource() == hiddenFilesCheckBox ) {
            FileSearchOptions.setShowHiddenFiles(hiddenFilesCheckBox.isSelected());
        }
        else if ( e.getSource() == mainProjectCheckBox ) {
            FileSearchOptions.setPreferMainProject(isPreferedProject());
        }
        else if ( e.getSource() == searchByFolders ) {
            FileSearchOptions.setSearchByFolders(searchByFolders.isSelected());
        }

        update();
    }

    /** Sets the initial text to find in case the user did not start typing yet. */
    public void setInitialText( final String text ) {
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                String textInField = fileNameTextField.getText();
                if ( textInField == null || textInField.trim().length() == 0 ) {
                    fileNameTextField.setText(text);
                    final int len = fileNameTextField.getText().length();   //The text may be changed by DocumentFilter
                    fileNameTextField.setCaretPosition(len);
                    fileNameTextField.setSelectionStart(0);
                    fileNameTextField.setSelectionEnd(len);
                }
            }
        });
    }

    private String getText() {
        try {
            String text = fileNameTextField.getDocument().getText(0, fileNameTextField.getDocument().getLength());
            return text;
        } catch( BadLocationException ex ) {
            return null;
        }
    }

    private int getFontSize () {
        return this.resultLabel.getFont().getSize();
    }

    public void setSelectedFile() {
        List<FileDescriptor> list = NbCollections.checkedListByCopy(Arrays.asList(resultList.getSelectedValues()), FileDescriptor.class, true);
        selectedFile = list.toArray(new FileDescriptor[0]);
    }

    public FileDescriptor[] getSelectedFiles() {
        return selectedFile;
    }

   public Project getCurrentProject() {
       return currentProject;
   }

    public static interface ContentProvider {

        public ListCellRenderer getListCellRenderer(
                @NonNull JList list,
                @NonNull Document nameDocument,
                @NonNull ButtonModel caseSensitive,
                @NonNull ButtonModel colorPrefered,
                @NonNull ButtonModel searchFolders);

        public boolean setListModel(FileSearchPanel panel, String text, boolean forceRefresh);

        public void closeDialog();

        public boolean hasValidContent ();

    }

}
