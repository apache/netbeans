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

package org.netbeans.modules.jumpto.symbol;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
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
import org.netbeans.modules.jumpto.SearchHistory;
import org.netbeans.modules.jumpto.common.UiUtils;
import org.netbeans.modules.jumpto.settings.GoToSettings;
import org.netbeans.modules.jumpto.type.UiOptions;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author  Petr Hrebejk
 */
class GoToPanelImpl extends JPanel implements GoToPanel<SymbolDescriptor> {

    private static final Icon WAIT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/wait.gif", false); // NOI18N
    private static final Icon WARN_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/jumpto/resources/warning.png", false); // NOI18N

    private static final int BRIGHTER_COLOR_COMPONENT = 10;
    private final ContentProvider contentProvider;
    private boolean containsScrollPane;
    private final JLabel messageLabel;
    private SymbolDescriptor selectedSymbol;

    // Time when the serach stared (for debugging purposes)
    private long time = -1;

    private final SearchHistory searchHistory;

    // handling http://netbeans.org/bugzilla/show_bug.cgi?id=203528
    // if the whole search argument (in the name JTextField) is selected and something is pasted in it's place,
    // notify the DocumentListener because it will first call removeUpdate() and then inserteUpdate().
    // When removeUpdate() is called we should not call update() because it messes the messageLabel's text.
    private boolean pastedFromClipboard = false;

    /** Creates new form GoToPanel */
    public GoToPanelImpl( ContentProvider contentProvider ) throws IOException {
        this.contentProvider = contentProvider;
        initComponents();
        containsScrollPane = true;
        ((AbstractDocument)nameField.getDocument()).setDocumentFilter(UiUtils.newUserInputFilter());
        matchesList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
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
        messageLabel.setText(NbBundle.getMessage(GoToPanelImpl.class, "TXT_NoSymbolsFound")); // NOI18N
        messageLabel.setFont(matchesList.getFont());

        // matchesList.setBackground( bgColorBrighter );
        // matchesScrollPane1.setBackground( bgColorBrighter );
        matchesList.setCellRenderer(
                contentProvider.getListCellRenderer(
                matchesList,
                caseSensitive.getModel()));
        contentProvider.setListModel( this, null );

        PatternListener pl = new PatternListener( this );
        nameField.getDocument().addDocumentListener(pl);
        matchesList.addListSelectionListener(pl);
        caseSensitive.setSelected(UiOptions.GoToSymbolDialog.getCaseSensitive());
        caseSensitive.addItemListener(pl);
        preferOpen.setSelected(GoToSettings.getDefault().isSortingPreferOpenProjects());
        preferOpen.addItemListener(pl);

        searchHistory = new SearchHistory(GoToPanelImpl.class, nameField);
    }

    @Override
    public void removeNotify() {
        searchHistory.saveHistory();
        super.removeNotify();
    }

    @Override
    public boolean isCaseSensitive () {
        return this.caseSensitive.isSelected();
    }
    
    private boolean isPreferOpenProjects() {
        return this.preferOpen.isSelected();
    }

    @Override
    public long getStartTime() {
        return this.time;
    }

    /** Sets the model.
     * Threading: Requires EDT.
     * @param the model to set
     * @param finished true for final update
     * @return true if model has changed
     */
    @Override
    public boolean setModel(
            @NonNull final ListModel model,
            final boolean finished) {
        assert SwingUtilities.isEventDispatchThread();
        matchesList.setModel(model);
        if (model.getSize() > 0 || getText() == null || getText().isBlank() ) {
            matchesList.setSelectedIndex(0);
            setListPanelContent(null,false);
            if ( time != -1 ) {
                GoToSymbolAction.LOGGER.log(
                        Level.FINE,
                        "Real search time {0} ms.",    //NOI18N
                        (System.currentTimeMillis() - time));
                time = -1;
            }
            return true;
        } else if (finished) {
            setListPanelContent(NbBundle.getMessage(GoToPanelImpl.class, "TXT_NoSymbolsFound") ,false );
            return false;
        } else {
            return false;
        }
    }

    /**
     * Revalidates the model
     * @param finished true for final update
     * @return true if model has changed
     */
    @Override
    public boolean revalidateModel (final boolean finished) {
        return setModel(matchesList.getModel(), finished);
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

    public void setSelectedSymbol() {
        selectedSymbol = matchesList.getSelectedValue();
    }

    public SymbolDescriptor getSelectedSymbol() {
        return selectedSymbol;
    }

    @Override
    public void setWarning(String warningMessage) {
        if (warningMessage != null) {
            jLabelWarning.setIcon(WARN_ICON);
            jLabelWarning.setBorder(BorderFactory.createEmptyBorder(3, 1, 1, 1));
        } else {
            jLabelWarning.setIcon(null);
            jLabelWarning.setBorder(null);
        }
        jLabelWarning.setText(warningMessage);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("deprecation")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        jLabelText = new JLabel();
        nameField = new JTextField();
        jLabelList = new JLabel();
        listPanel = new JPanel();
        matchesScrollPane1 = new JScrollPane();
        matchesList = new JList<>();
        jLabelWarning = new JLabel();
        caseSensitive = new JCheckBox();
        preferOpen = new JCheckBox();
        jLabelLocation = new JLabel();
        jTextFieldLocation = new JTextField();

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setFocusable(false);
        setNextFocusableComponent(nameField);
        setLayout(new GridBagLayout());

        jLabelText.setLabelFor(nameField);
        Mnemonics.setLocalizedText(jLabelText, NbBundle.getMessage(GoToPanelImpl.class, "TXT_GoToSymbol_TypeName_Label")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        add(jLabelText, gridBagConstraints);

        nameField.setFont(new Font("Monospaced", 0, getFontSize()));
        nameField.setBorder(BorderFactory.createEtchedBorder());
        nameField.addActionListener(this::nameFieldActionPerformed);
        nameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                nameFieldKeyPressed(evt);
            }
            public void keyReleased(KeyEvent evt) {
                nameFieldKeyReleased(evt);
            }
            public void keyTyped(KeyEvent evt) {
                nameFieldKeyTyped(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 8, 0);
        add(nameField, gridBagConstraints);
        nameField.getAccessibleContext().setAccessibleName("Symbol &Name (prefix, camel case: \"AA\" or \"AbcAb\", wildcards: \"?\" \"*\", exact match: end with space):");
        nameField.getAccessibleContext().setAccessibleDescription("Symbol Name (prefix, camel case: \"AA\" or \"AbcAb\", wildcards: \"?\" \"*\", exact match: end with space)");

        jLabelList.setLabelFor(matchesList);
        Mnemonics.setLocalizedText(jLabelList, NbBundle.getMessage(GoToPanelImpl.class, "TXT_GoToSymbol_MatchesList_Label")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        add(jLabelList, gridBagConstraints);

        listPanel.setBorder(BorderFactory.createEtchedBorder());
        listPanel.setName("dataPanel"); // NOI18N
        listPanel.setLayout(new BorderLayout());

        matchesScrollPane1.setBorder(null);
        matchesScrollPane1.setFocusable(false);

        matchesList.setFont(new Font("Monospaced", 0, getFontSize()));
        matchesList.setVisibleRowCount(15);
        matchesList.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent evt) {
                matchesListMouseReleased(evt);
            }
        });
        matchesScrollPane1.setViewportView(matchesList);
        matchesList.getAccessibleContext().setAccessibleName("Symbols &Found :");
        matchesList.getAccessibleContext().setAccessibleDescription("Symbols Found");

        listPanel.add(matchesScrollPane1, BorderLayout.CENTER);

        jLabelWarning.setFocusable(false);
        listPanel.add(jLabelWarning, BorderLayout.PAGE_END);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new Insets(0, 0, 8, 0);
        add(listPanel, gridBagConstraints);

        Mnemonics.setLocalizedText(caseSensitive, NbBundle.getMessage(GoToPanelImpl.class, "CTL_CaseSensitive")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 0, 8, 0);
        add(caseSensitive, gridBagConstraints);
        caseSensitive.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GoToPanelImpl.class, "AD_CaseSensitive")); // NOI18N

        Mnemonics.setLocalizedText(preferOpen, NbBundle.getMessage(GoToPanelImpl.class, "CTL_PreferOpenProjects")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(0, 0, 8, 0);
        add(preferOpen, gridBagConstraints);

        jLabelLocation.setText(NbBundle.getMessage(GoToPanelImpl.class, "LBL_GoToSymbol_LocationJLabel")); // NOI18N
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 4, 0);
        add(jLabelLocation, gridBagConstraints);

        jTextFieldLocation.setEditable(false);
        jTextFieldLocation.setFocusable(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
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
            //handling http://netbeans.org/bugzilla/show_bug.cgi?id=203528
            Object o = nameField.getInputMap().get(KeyStroke.getKeyStrokeForEvent(evt));
            if (o instanceof String) {
                String action = (String) o;
                if ("paste-from-clipboard".equals(action)) {
                    String selectedTxt = nameField.getSelectedText();
                    String txt = nameField.getText();
                    if (selectedTxt != null && txt != null) {
                        if (selectedTxt.length() == txt.length()) {
                            pastedFromClipboard = true;
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_nameFieldKeyPressed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        if (contentProvider.hasValidContent()) {
            contentProvider.closeDialog();
            setSelectedSymbol();
        }
    }//GEN-LAST:event_nameFieldActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox caseSensitive;
    private JLabel jLabelList;
    private JLabel jLabelLocation;
    private JLabel jLabelText;
    private JLabel jLabelWarning;
    private JTextField jTextFieldLocation;
    private JPanel listPanel;
    private JList<SymbolDescriptor> matchesList;
    private JScrollPane matchesScrollPane1;
    private JTextField nameField;
    private JCheckBox preferOpen;
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

    void setListPanelContent( String message ,boolean waitIcon ) {

        if ( message == null && !containsScrollPane ) {
           listPanel.remove( messageLabel );
           listPanel.add( matchesScrollPane1 );
           containsScrollPane = true;
           revalidate();
           repaint();
        }
        else if ( message != null ) { 
           jTextFieldLocation.setText(""); 
           messageLabel.setText(message);
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
    private Pair<String,JComponent> listActionFor(KeyEvent ev) {
        InputMap map = matchesList.getInputMap();
        if (map.get(KeyStroke.getKeyStrokeForEvent(ev)) instanceof String str) {
            return Pair.<String,JComponent>of(str, matchesList);
        }
        map = matchesScrollPane1.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (map.get(KeyStroke.getKeyStrokeForEvent(ev)) instanceof String str) {
            return Pair.<String,JComponent>of(str, matchesScrollPane1);
        }
        return null;
    }

    private boolean boundScrollingKey(KeyEvent ev) {
        final Pair<String,JComponent> p = listActionFor(ev);
        if (p == null) {
            return false;
        }
        final String action = p.first();
        // See BasicListUI, MetalLookAndFeel:
        return "selectPreviousRow".equals(action) || // NOI18N
        "selectNextRow".equals(action) || // NOI18N
        // "selectFirstRow".equals(action) || // NOI18N
        // "selectLastRow".equals(action) || // NOI18N
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
        final Action a = target.getActionMap().get(action);
        if (a != null) {
            a.actionPerformed(new ActionEvent(target, 0, action));
        }
    }

    private static class PatternListener implements DocumentListener, ListSelectionListener, ItemListener {

        private final GoToPanelImpl dialog;

        PatternListener( GoToPanelImpl dialog  ) {
            this.dialog = dialog;
        }

        // DocumentListener ----------------------------------------------------

        @Override
        public void changedUpdate( DocumentEvent e ) {
            update();
        }

        @Override
        public void removeUpdate( DocumentEvent e ) {
            // handling http://netbeans.org/bugzilla/show_bug.cgi?id=203528
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

        @Override
        public void itemStateChanged(ItemEvent e) {
            UiOptions.GoToSymbolDialog.setCaseSensitive(dialog.isCaseSensitive());
            boolean restart = false;
            if (GoToSettings.getDefault().isSortingPreferOpenProjects() != dialog.isPreferOpenProjects()) {
                GoToSettings.getDefault().setSortingPreferOpenProjects(dialog.isPreferOpenProjects());
                restart = true; // todo comparator should be able to handle this without restart
            }
            update(restart);
        }

        // ListSelectionListener -----------------------------------------------

        @Override
        public void valueChanged(@NonNull final ListSelectionEvent ev) {
            // got "Not computed yet" text sometimes
            SymbolDescriptor selected = dialog.matchesList.getSelectedValue();
            if (selected != null) {
                dialog.jTextFieldLocation.setText(selected.getFileDisplayPath());
            } else {
                dialog.jTextFieldLocation.setText("");  //NOI18N
            }
        }
        
        private void update() {
            this.update(false);
        }

        private void update(boolean restart) {
            dialog.time = System.currentTimeMillis();
            if (restart) {
                dialog.contentProvider.setListModel(dialog, null);
            }
            String text = dialog.getText();
            if (dialog.contentProvider.setListModel(dialog,text)) {
                dialog.setListPanelContent(NbBundle.getMessage(GoToPanelImpl.class, "TXT_Searching"),true); // NOI18N
            }
        }
    }


    public static interface ContentProvider {

        public ListCellRenderer<SymbolDescriptor> getListCellRenderer(JList<SymbolDescriptor> list, ButtonModel caseSensitive);

        public boolean setListModel( GoToPanel panel, String text  );

        public void closeDialog();

        public boolean hasValidContent ();

    }

}
