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
/*
 * Contributor(s): markiewb@netbeans.org
 */
package org.netbeans.modules.jumpto.type;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.jumpto.SearchHistory;
import org.netbeans.modules.jumpto.common.ItemRenderer;
import org.netbeans.modules.jumpto.common.UiUtils;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

/**
 *
 * @author  Petr Hrebejk
 */
public class GoToPanel extends javax.swing.JPanel {
            
    private static final Icon WAIT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/wait.gif", false); // NOI18N
    private static final Icon WARN_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/warning.png", false); // NOI18N
        
    private static final int BRIGHTER_COLOR_COMPONENT = 10;
    private final ContentProvider contentProvider;
    private boolean containsScrollPane;
    JLabel messageLabel;
    private Iterable<? extends TypeDescriptor> selectedTypes = List.of();
    private String oldMessage;
    
    // Time when the serach stared (for debugging purposes)
    long time = -1;

    private final SearchHistory searchHistory;

    // handling http://netbeans.org/bugzilla/show_bug.cgi?id=203512
    // if the whole search argument (in the name JTextField) is selected and something is pasted in it's place,
    // notify the DocumentListener because it will first call removeUpdate() and then inserteUpdate().
    // When removeUpdate() is called we should not call update() because it messes the messageLabel's text.
    private boolean pastedFromClipboard = false;
    
    /** Creates new form GoToPanel */
    public GoToPanel( ContentProvider contentProvider, boolean multiSelection ) throws IOException {
        this.contentProvider = contentProvider;
        initComponents();
        ((AbstractDocument)nameField.getDocument()).setDocumentFilter(UiUtils.newUserInputFilter());
        containsScrollPane = true;
                
        matchesList.setSelectionMode( multiSelection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
        //matchesList.setPrototypeCellValue("12345678901234567890123456789012345678901234567890123456789012345678901234567890");        
        matchesList.addListSelectionListener(null);
        
        Color bgColorBrighter = new Color(
                                    Math.min(getBackground().getRed() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getGreen() + BRIGHTER_COLOR_COMPONENT, 255),
                                    Math.min(getBackground().getBlue() + BRIGHTER_COLOR_COMPONENT, 255)
                            );
        
        messageLabel = new JLabel();
        messageLabel.setBackground(bgColorBrighter);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setEnabled(true);
        messageLabel.setText(NbBundle.getMessage(GoToPanel.class, "TXT_NoTypesFound")); // NOI18N
        messageLabel.setFont(matchesList.getFont());
        
        // matchesList.setBackground( bgColorBrighter );
        // matchesScrollPane1.setBackground( bgColorBrighter );
        matchesList.setCellRenderer( contentProvider.getListCellRenderer(
                matchesList,
                caseSensitive.getModel()
        ));
        contentProvider.setListModel( this, null );
        
        PatternListener pl = new PatternListener( this );
        nameField.getDocument().addDocumentListener(pl);

        caseSensitive.setSelected(UiOptions.GoToTypeDialog.getCaseSensitive());
        caseSensitive.addItemListener(pl);
        prefereOpen.setSelected(GoToSettings.getDefault().isSortingPreferOpenProjects());
        prefereOpen.addItemListener(pl);

        matchesList.addListSelectionListener(pl);
        
        searchHistory = new SearchHistory(GoToPanel.class, nameField);
    }

    @Override
    public void removeNotify() {
        searchHistory.saveHistory();
        super.removeNotify();
    }

    /** Sets the model from different thread
     */
    boolean setModel(ListModel<TypeDescriptor> model) {
        assert SwingUtilities.isEventDispatchThread();
        matchesList.setModel(model);
        if (model.getSize() > 0 || getText() == null || getText().isBlank()) {
            matchesList.setSelectedIndex(0);
            setListPanelContent(null,false);
            if ( time != -1 ) {
                GoToTypeAction.LOGGER.fine("Real search time " + (System.currentTimeMillis() - time) + " ms.");
                time = -1;
            }
            return true;
        } else {
            setListPanelContent( NbBundle.getMessage(GoToPanel.class, "TXT_NoTypesFound") ,false ); // NOI18N
            return false;
        }
    }

    boolean revalidateModel () {
        return setModel(matchesList.getModel());
    }

    /** Sets the initial text to find in case the user did not start typing yet. */
    public void setInitialText( final String text ) {
        SwingUtilities.invokeLater(() -> {
            String textInField = nameField.getText();
            if (textInField == null || textInField.isBlank()) {
                nameField.setText(text);
                nameField.setCaretPosition(text.length());
                nameField.setSelectionStart(0);
                nameField.setSelectionEnd(text.length());
            }
        });
    }
    
    public void setSelectedTypes() {
        selectedTypes = List.copyOf(matchesList.getSelectedValuesList());
    }
    
    public Iterable<? extends TypeDescriptor> getSelectedTypes() {
        return selectedTypes;
    }

    void setWarning(String warningMessage) {
        if (warningMessage != null) {
            jLabelWarning.setIcon(WARN_ICON);
            jLabelWarning.setBorder(BorderFactory.createEmptyBorder(3, 1, 1, 1));
        } else {
            jLabelWarning.setIcon(null);
            jLabelWarning.setBorder(null);
        }
        jLabelWarning.setText(warningMessage);
    }
    
    //handling http://netbeans.org/bugzilla/show_bug.cgi?id=178555
    public void setMouseListener(MouseListener warningMouseListener) {
        if (messageLabel.getMouseListeners().length == 0) {
            messageLabel.addMouseListener(warningMouseListener);
        }
    }
            
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("deprecation")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelText = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jLabelList = new javax.swing.JLabel();
        listPanel = new javax.swing.JPanel();
        matchesScrollPane1 = new javax.swing.JScrollPane();
        matchesList = new javax.swing.JList<>();
        jLabelWarning = new javax.swing.JLabel();
        caseSensitive = new javax.swing.JCheckBox();
        prefereOpen = new javax.swing.JCheckBox();
        jLabelLocation = new javax.swing.JLabel();
        jTextFieldLocation = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setFocusable(false);
        setNextFocusableComponent(nameField);
        setLayout(new java.awt.GridBagLayout());

        jLabelText.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelText, org.openide.util.NbBundle.getMessage(GoToPanel.class, "TXT_GoToType_TypeName_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelText, gridBagConstraints);
        jLabelText.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GoToPanel.class, "GoToPanel.jLabelText.AccessibleContext.accessibleDescription")); // NOI18N

        nameField.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        nameField.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        nameField.addActionListener(this::nameFieldActionPerformed);
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nameFieldKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nameFieldKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                nameFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(nameField, gridBagConstraints);

        jLabelList.setLabelFor(matchesList);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelList, org.openide.util.NbBundle.getMessage(GoToPanel.class, "TXT_GoToType_MatchesList_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelList, gridBagConstraints);

        listPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        listPanel.setName("dataPanel"); // NOI18N
        listPanel.setLayout(new java.awt.BorderLayout());

        matchesScrollPane1.setBorder(null);
        matchesScrollPane1.setFocusable(false);

        matchesList.setFont(new java.awt.Font("Monospaced", 0, getFontSize()));
        matchesList.setVisibleRowCount(15);
        matchesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                matchesListMouseReleased(evt);
            }
        });
        matchesScrollPane1.setViewportView(matchesList);
        matchesList.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(GoToPanel.class, "ACSD_GoToListName")); // NOI18N
        matchesList.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GoToPanel.class, "GoToPanel.matchesList.AccessibleContext.accessibleDescription")); // NOI18N

        listPanel.add(matchesScrollPane1, java.awt.BorderLayout.CENTER);

        jLabelWarning.setFocusable(false);
        listPanel.add(jLabelWarning, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(listPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(caseSensitive, org.openide.util.NbBundle.getMessage(GoToPanel.class, "TXT_GoToType_CaseSensitive")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(caseSensitive, gridBagConstraints);
        caseSensitive.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GoToPanel.class, "GoToPanel.caseSensitive.AccessibleContext.accessibleDescription")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(prefereOpen, org.openide.util.NbBundle.getMessage(GoToPanel.class, "TXT_GoToType_PreferOpenProjects")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(prefereOpen, gridBagConstraints);
        prefereOpen.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(GoToPanel.class, "GoToPanel.prefereOpen.AccessibleContext.accessibleDescription")); // NOI18N

        jLabelLocation.setText(org.openide.util.NbBundle.getMessage(GoToPanel.class, "LBL_GoToType_LocationJLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(jLabelLocation, gridBagConstraints);

        jTextFieldLocation.setEditable(false);
        jTextFieldLocation.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jTextFieldLocation, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void matchesListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_matchesListMouseReleased
        if ( evt.getClickCount() == 2 ) {
            nameFieldActionPerformed( null );
        }
    }//GEN-LAST:event_matchesListMouseReleased

    private void nameFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyTyped
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyTyped

    private void nameFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyReleased
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        }
    }//GEN-LAST:event_nameFieldKeyReleased

    private void nameFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nameFieldKeyPressed
        if (boundScrollingKey(evt)) {
            delegateScrollingKey(evt);
        } else {
            //handling http://netbeans.org/bugzilla/show_bug.cgi?id=203512
            Object o = nameField.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));
            if (o instanceof String action && "paste-from-clipboard".equals(action)) {
                String selectedTxt = nameField.getSelectedText();
                String txt = nameField.getText();
                if (selectedTxt != null && txt != null) {
                    if (selectedTxt.length() == txt.length()) {
                        pastedFromClipboard = true;
                    }
                }
            }
        }
    }//GEN-LAST:event_nameFieldKeyPressed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        if (contentProvider.hasValidContent()) {
            contentProvider.closeDialog();
            setSelectedTypes();
        }
    }//GEN-LAST:event_nameFieldActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox caseSensitive;
    private javax.swing.JLabel jLabelList;
    private javax.swing.JLabel jLabelLocation;
    private javax.swing.JLabel jLabelText;
    private javax.swing.JLabel jLabelWarning;
    private javax.swing.JTextField jTextFieldLocation;
    private javax.swing.JPanel listPanel;
    private javax.swing.JList<TypeDescriptor> matchesList;
    private javax.swing.JScrollPane matchesScrollPane1;
    javax.swing.JTextField nameField;
    private javax.swing.JCheckBox prefereOpen;
    // End of variables declaration//GEN-END:variables

    private String getText() {
        try {
            String text = nameField.getDocument().getText(0, nameField.getDocument().getLength());
            return text;
        }
        catch( BadLocationException ex ) {
            return null;
        }
    }
    
    private int getFontSize () {
        return this.jLabelList.getFont().getSize();
    }
    
    public boolean isCaseSensitive() {
        return this.caseSensitive.isSelected();
    }
    
    public boolean isPreferOpenProjects() {
        return this.prefereOpen.isSelected();
    }

    void updateMessage(@NullAllowed final String message) {
        if (message == null ? oldMessage != null : !message.equals(oldMessage)) {
            setListPanelContent(message,true); // NOI18N
        }
    }

    void setListPanelContent( String message ,boolean waitIcon ) {
        assert SwingUtilities.isEventDispatchThread();
        oldMessage = message;
        if (message == null) {            
            if (!containsScrollPane) {
               listPanel.remove( messageLabel );
               listPanel.add( matchesScrollPane1 );
               containsScrollPane = true;
               revalidate();
               repaint();
            }
        } else {
           jTextFieldLocation.setText(""); 
           //handling http://netbeans.org/bugzilla/show_bug.cgi?id=178555
            messageLabel.setText(waitIcon
                    ? "<html>" + message + "&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"http://www.netbeans.org\">"+NbBundle.getMessage(GoToPanel.class, "TXT_CancelSearch")+"</a></html>" //NOI18N
                    : message);
           messageLabel.setIcon( waitIcon ? WAIT_ICON : null);
           if ( containsScrollPane ) {
               listPanel.remove( matchesScrollPane1 );
               listPanel.add( messageLabel );
               containsScrollPane = false;
           }
           revalidate();
           repaint();
       }                
    }
    
    @CheckForNull
    private Pair<String, JComponent> listActionFor(KeyEvent ev) {
        InputMap map = matchesList.getInputMap();
        if (map.get(KeyStroke.getKeyStrokeForEvent(ev)) instanceof String str) {
            return Pair.of(str, matchesList);
        }
        map = matchesScrollPane1.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (map.get(KeyStroke.getKeyStrokeForEvent(ev)) instanceof String str) {
            return Pair.of(str, matchesScrollPane1);
        }
        return null;
    }

    private boolean boundScrollingKey(KeyEvent ev) {
        final Pair<String,JComponent> p = listActionFor(ev);
        if (p == null) {
            return false;
        }
        String action = p.first();
        // See BasicListUI, MetalLookAndFeel:
        return "selectPreviousRow".equals(action) || // NOI18N
        "selectNextRow".equals(action) || // NOI18N
        "selectPreviousRowExtendSelection".equals(action) ||    //NOI18N
        "selectNextRowExtendSelection".equals(action) || //NOI18N
        "scrollUp".equals(action) || // NOI18N
        "scrollDown".equals(action); // NOI18N
    }

    private void delegateScrollingKey(KeyEvent ev) {
        final Pair<String,JComponent> p = listActionFor(ev);
        if (p == null) {
            return;
        }
        final String action = p.first();
        final JComponent target = p.second();
        // Wrap around
        if ( "selectNextRow".equals(action) && 
            matchesList.getSelectedIndex() == matchesList.getModel().getSize() -1 ) {
            matchesList.setSelectedIndex(0);
            matchesList.ensureIndexIsVisible(0);
            return;
        }
        else if ( "selectPreviousRow".equals(action) &&
                  matchesList.getSelectedIndex() == 0 ) {
            int last = matchesList.getModel().getSize() - 1;
            matchesList.setSelectedIndex(last);
            matchesList.ensureIndexIsVisible(last);
            return;
        }        
        // Plain delegation        
        Action a = target.getActionMap().get(action);
        if (a != null) {
            a.actionPerformed(new ActionEvent(target, 0, action));
        }
    }
    
    private static class PatternListener implements DocumentListener, ItemListener, ListSelectionListener {
               
        private final GoToPanel dialog;
        
        
        PatternListener( GoToPanel dialog ) {
            this.dialog = dialog;
        }

        // DocumentListener ----------------------------------------------------
        
        @Override
        public void changedUpdate( DocumentEvent e ) {            
            update();
        }

        @Override
        public void removeUpdate( DocumentEvent e ) {
            // handling http://netbeans.org/bugzilla/show_bug.cgi?id=203512
            if (dialog.pastedFromClipboard) {
                dialog.pastedFromClipboard = false;
            } else {
                update();
            }
        }

        @Override
        public void insertUpdate( DocumentEvent e ) {
            update();
        }
        
        // Item Listener -------------------------------------------------------
        
        @Override
        public void itemStateChanged (final ItemEvent e) {
            UiOptions.GoToTypeDialog.setCaseSensitive(dialog.isCaseSensitive());
            boolean restart = false;
            if (GoToSettings.getDefault().isSortingPreferOpenProjects() != dialog.isPreferOpenProjects()) {
                GoToSettings.getDefault().setSortingPreferOpenProjects(dialog.isPreferOpenProjects());
                restart = true;  // todo comparator should be able to handle this without restart
            }
            update(restart);
        }
        
        // ListSelectionListener -----------------------------------------------
        
        @Override
        public void valueChanged(@NonNull final ListSelectionEvent ev) {
            TypeDescriptor selected = dialog.matchesList.getSelectedValue();
            if (selected != null) {
                dialog.jTextFieldLocation.setText(selected.getFileDisplayPath());
            } else {
                dialog.jTextFieldLocation.setText("");      //NOI18N
            }
        }
        
        private void update() {
            this.update(false);
        }

        private void update(boolean restart) {
            dialog.time = System.currentTimeMillis();
            final String text = dialog.getText();
            if (restart) {
                dialog.contentProvider.setListModel(dialog, null);
            }            
            if (dialog.contentProvider.setListModel(dialog, text)) {
                dialog.updateMessage(NbBundle.getMessage(GoToPanel.class, "TXT_Searching"));
            }
        }
    }


    public static interface ContentProvider {

        @NonNull
        public ItemRenderer<TypeDescriptor> getListCellRenderer(
                @NonNull JList<TypeDescriptor> list,
                @NonNull ButtonModel caseSensitive);

        public boolean setListModel( GoToPanel panel, String text );

        public void closeDialog();

        public boolean hasValidContent ();
    }

}
