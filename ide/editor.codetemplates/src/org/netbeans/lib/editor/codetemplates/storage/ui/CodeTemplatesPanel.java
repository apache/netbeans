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

package org.netbeans.lib.editor.codetemplates.storage.ui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.editor.Acceptor;
import org.netbeans.lib.editor.codetemplates.AbbrevDetection;
import org.netbeans.lib.editor.codetemplates.CodeTemplateHint;
import org.netbeans.lib.editor.codetemplates.CodeTemplateParameterImpl;
import org.netbeans.lib.editor.codetemplates.ParametrizedTextParser;
import org.netbeans.lib.editor.codetemplates.spi.CodeTemplateParameter;
import org.netbeans.lib.editor.codetemplates.storage.CodeTemplateSettingsImpl.OnExpandAction;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.InputLine;
import org.openide.awt.Mnemonics;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Implementation of one panel in Options Dialog.
 *
 * @author Jan Jancura
 */
@OptionsPanelController.Keywords(keywords = {"code", "templates", "#KW_CodeTemplates"}, location = OptionsDisplayer.EDITOR, tabTitle="#CTL_CodeTemplates_DisplayName")
public class CodeTemplatesPanel extends JPanel implements ActionListener, ListSelectionListener, KeyListener, DocumentListener {
    
    private static final Logger LOG = Logger.getLogger(CodeTemplatesPanel.class.getName());
    private CodeTemplatesModel  model;

    /** Language selected in the combo by user. */
    private String selectedLanguage;

    /** Language which related info the panel currently displays. */
    private static String panelLanguage;

    /** Points to modified template (its row index in templates table model, NOT view index). */
    private int unsavedTemplateIndex = -1;

    /** Allows to remember last edited template when templates panel gets reopened. */
    private int forceRowIndex = -1;

    /** 
     * Creates new form CodeTemplatesPanel. 
     */
    public CodeTemplatesPanel () {
        initComponents ();
        
        loc(lLanguage, "Language"); //NOI18N
        loc(lTemplates, "Templates"); //NOI18N
        loc(bNew, "New"); //NOI18N
        loc(bRemove, "Remove"); //NOI18N
        loc(lExplandTemplateOn, "ExpandTemplateOn"); //NOI18N
        loc(lOnExpandAction, "OnExpandAction"); //NOI18N
        loc(tabPane, 0, "Expanded_Text", epExpandedText); //NOI18N
        loc(tabPane, 1, "Description", epDescription); //NOI18N
        tabPane.getAccessibleContext().setAccessibleName(loc("AN_tabPane")); //NOI18N
        tabPane.getAccessibleContext().setAccessibleDescription(loc("AD_tabPane")); //NOI18N
        
        cbExpandTemplateOn.addItem(loc("SPACE")); //NOI18N
        cbExpandTemplateOn.addItem(loc("S-SPACE")); //NOI18N
        cbExpandTemplateOn.addItem(loc("TAB")); //NOI18N
        cbExpandTemplateOn.addItem(loc("ENTER")); //NOI18N
        
        cbOnExpandAction.addItem(loc("FORMAT")); //NOI18N
        cbOnExpandAction.addItem(loc("INDENT")); //NOI18N
        cbOnExpandAction.addItem(loc("NOOP")); //NOI18N

        bRemove.setEnabled (false);
        tTemplates.getTableHeader().setReorderingAllowed(false);
        tTemplates.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        epExpandedText.addKeyListener(this);
        epDescription.addKeyListener(this);
        
        lContexts = new javax.swing.JList<>();
        lContexts.setCellRenderer(new ListRenderer());
        CheckListener checkListener = new CheckListener();
        lContexts.addMouseListener(checkListener);
        lContexts.addKeyListener(checkListener);
        spContexts = new javax.swing.JScrollPane(lContexts);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (CodeTemplatesPanel.class, key);
    }
    
    private static void loc(Component c, String key) {
        if (!(c instanceof JLabel)) {
            c.getAccessibleContext().setAccessibleName(loc("AN_" + key)); //NOI18N
            c.getAccessibleContext().setAccessibleDescription(loc("AD_" + key)); //NOI18N
        }
        if (c instanceof AbstractButton) {
            Mnemonics.setLocalizedText((AbstractButton) c, loc("CTL_" + key)); //NOI18N
        } else {
            Mnemonics.setLocalizedText((JLabel) c, loc("CTL_" + key)); //NOI18N
        }
    }
    
    private static void loc(JTabbedPane p, int tabIdx, String key, JEditorPane ep) {
        JLabel label = new JLabel(); // Only for setting tab names

        String tabName = loc("CTL_" + key); //NOI18N
        Mnemonics.setLocalizedText(label, tabName);
        p.setTitleAt(tabIdx, label.getText());

        int idx = Mnemonics.findMnemonicAmpersand(tabName);
        if (idx != -1 && idx + 1 < tabName.length()) {
            char ch = Character.toUpperCase(tabName.charAt(idx + 1));
            p.setMnemonicAt(tabIdx, ch);
            if (ep != null) {
                ep.setFocusAccelerator(ch);
            }
        }
    }
    
    // OptionsCategory.Panel ...................................................
    
    void update () {
        model = new CodeTemplatesModel ();
        String lastPanelLanguage = panelLanguage;
        int lastSelectedRowIndex = tTemplates.getSelectedRow();
        selectedLanguage = null;
        panelLanguage = null;

        cbLanguage.removeActionListener (this);
        bNew.removeActionListener (this);
        bRemove.removeActionListener (this);
        cbExpandTemplateOn.removeActionListener (this);
        cbOnExpandAction.removeActionListener(this);
        tTemplates.getSelectionModel ().removeListSelectionListener (this);
        
        String defaultSelectedLang = null;
        Object selectedItem = cbLanguage.getSelectedItem();
        if (selectedItem instanceof String) {
            defaultSelectedLang = (String) selectedItem;
        }
        cbLanguage.removeAllItems ();
        List<String> languages = new ArrayList<String>(model.getLanguages ());
        Collections.sort (languages);
        for(String l : languages) {
            cbLanguage.addItem(l);
        }
        if (languages.isEmpty ()) {
            cbLanguage.setEnabled (false);
            bNew.setEnabled (false);
            bRemove.setEnabled (false);
            tTemplates.setEnabled (false);
            tabPane.setEnabled (false);
            cbExpandTemplateOn.setEnabled (false);
        }
        KeyStroke expander = model.getExpander ();
        if (KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, KeyEvent.SHIFT_MASK).equals (expander))
            cbExpandTemplateOn.setSelectedIndex (1);
        else
        if (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, 0).equals (expander))
            cbExpandTemplateOn.setSelectedIndex (2);
        else
        if (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0).equals (expander))
            cbExpandTemplateOn.setSelectedIndex (3);
        else
            cbExpandTemplateOn.setSelectedIndex (0);
        
        OnExpandAction onExpandAction = model.getOnExpandAction();
        switch (onExpandAction) {
            case FORMAT:
                cbOnExpandAction.setSelectedIndex(0);
                break;
            case INDENT:
                cbOnExpandAction.setSelectedIndex(1);
                break;
            default:
                cbOnExpandAction.setSelectedIndex(2);
                break;
        }
        
        cbLanguage.addActionListener (this);
        bNew.addActionListener (this);
        bRemove.addActionListener (this);
        cbExpandTemplateOn.addActionListener (this);
        cbOnExpandAction.addActionListener (this);
        tTemplates.getSelectionModel ().addListSelectionListener (this);
        
        // Pre-select a language
        JTextComponent pane = EditorRegistry.lastFocusedComponent();
        if (defaultSelectedLang == null && pane != null) {
            String mimeType = (String)pane.getDocument().getProperty("mimeType"); // NOI18N
            if (mimeType != null) {
                defaultSelectedLang = model.findLanguage(mimeType);
            }
        }
        if (defaultSelectedLang == null) {
            defaultSelectedLang = model.findLanguage("text/x-java"); //NOI18N
        }
        if (defaultSelectedLang == null) {
            defaultSelectedLang = model.findLanguage("text/x-ruby"); //NOI18N
        }
        if (defaultSelectedLang == null) {
            defaultSelectedLang = model.findLanguage("text/x-c++"); //NOI18N
        }
        if (defaultSelectedLang == null && model.getLanguages().size() > 0) {
            defaultSelectedLang = model.getLanguages().get(0);
        }
        forceRowIndex = -1;
        if (defaultSelectedLang != null) {
            cbLanguage.setSelectedItem(defaultSelectedLang);
            if (defaultSelectedLang.equals(lastPanelLanguage)) {
                forceRowIndex = lastSelectedRowIndex;
            }
        }
    }
    
    void applyChanges () {
        saveCurrentTemplate();
        if (model != null) {
            model.saveChanges ();
        }
    }
    
    void cancel () {
    }
    
    boolean dataValid () {
        return true;
    }
    
    boolean isChanged () {
        saveCurrentTemplate();
        if (model == null) return false;
        return model.isChanged ();
    }
    
    // ActionListener ..........................................................
    public void actionPerformed (ActionEvent e) {
        if (e.getSource () == cbLanguage) {
            saveCurrentTemplate ();
            selectedLanguage = (String) cbLanguage.getSelectedItem ();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (selectedLanguage.equals(panelLanguage)) {
                        return;
                    }
                    saveCurrentTemplate();
                    panelLanguage = selectedLanguage;
                    CodeTemplatesModel.TM tableModel = model.getTableModel(panelLanguage);

                    tTemplates.setModel(tableModel);
                    TableColumn c1 = tTemplates.getTableHeader().getColumnModel().getColumn(0);
                    c1.setMinWidth(80);
                    c1.setPreferredWidth(100);
                    c1.setResizable(true);

                    TableColumn c2 = tTemplates.getTableHeader().getColumnModel().getColumn(1);
                    c2.setMinWidth(180);
                    c2.setPreferredWidth(250);
                    c2.setResizable(true);

                    TableColumn c3 = tTemplates.getTableHeader().getColumnModel().getColumn(2);
                    c3.setMinWidth(180);
                    c3.setPreferredWidth(250);
                    c3.setResizable(true);

                    epExpandedText.getDocument().removeDocumentListener(CodeTemplatesPanel.this);
                    epDescription.getDocument().removeDocumentListener(CodeTemplatesPanel.this);
                    epDescription.setEditorKit(CloneableEditorSupport.getEditorKit("text/html")); //NOI18N
                    epExpandedText.setEditorKit(CloneableEditorSupport.getEditorKit(model.getMimeType (panelLanguage)));
                    // Possibly force to select forceRowIndex (if the table has enough rows)
                    int rowCount = tableModel.getRowCount();
                    int selectRowIndex = (forceRowIndex != -1 && forceRowIndex < rowCount)
                            ? forceRowIndex
                            : 0;
                    forceRowIndex = -1;
                    if (selectRowIndex < rowCount) {
                        tTemplates.getSelectionModel().setSelectionInterval(selectRowIndex, selectRowIndex);
                    }
                    // Need to re-add the document listeners since pane.setEditorKit() changes the document
                    epExpandedText.getDocument().addDocumentListener(CodeTemplatesPanel.this);
                    epDescription.getDocument().addDocumentListener(CodeTemplatesPanel.this);
                    
                    ListModel<String> supportedContexts = tableModel.getSupportedContexts();
                    if (supportedContexts.getSize() > 0) {
                        lContexts.setModel(supportedContexts);
                        if (tabPane.getTabCount() < 3) {
                            tabPane.addTab(null, spContexts);
                            loc(tabPane, 2, "Contexts", null); //NOI18N
                        }
                    } else if (tabPane.getTabCount() > 2) {
                        tabPane.remove(2);
                    }
                    editParametersButton.setEnabled(
                            ParameterValidator.containsNonReservedParameter(epExpandedText.getText()));
                }
            });
        } else if (e.getSource () == bNew) {
            saveCurrentTemplate ();
            InputLine descriptor = new InputLine (
                loc ("CTL_Enter_template_name"),
                loc ("CTL_New_template_dialog_title")
            );
            if (DialogDisplayer.getDefault().notify(descriptor) == InputLine.OK_OPTION ) {
                String newAbbrev = descriptor.getInputText().trim();
                
                if (newAbbrev.length() == 0) {
                    DialogDisplayer.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            loc ("CTL_Empty_template_name"),
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                } else if (!checkAbbrev(newAbbrev, model.getMimeType(panelLanguage))) {
                    DialogDisplayer.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            loc ("CTL_Rejected_template_name"),
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                } else {
                    CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
                    int i, rows = tableModel.getRowCount ();
                    for (i = 0; i < rows; i++) {
                        String abbrev = tableModel.getAbbreviation(i);
                        if (newAbbrev.equals (abbrev)) {
                            DialogDisplayer.getDefault ().notify (
                                new NotifyDescriptor.Message (
                                    loc ("CTL_Duplicate_template_name"),
                                    NotifyDescriptor.ERROR_MESSAGE
                                )
                            );
                            break;
                        }
                    }
                    if (i == rows) {
                        //rowIdx must be recalculated to view index
                        int rowIdx = tTemplates.convertRowIndexToView(tableModel.addCodeTemplate(newAbbrev));
                        tTemplates.getSelectionModel().setSelectionInterval(rowIdx, rowIdx);
                    }
                }
                
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        // Scroll to the bottom
                        spTemplates.getVerticalScrollBar().setValue(
                            spTemplates.getVerticalScrollBar().getMaximum());

                        // Show the extpanded text and place the focus in it
                        tabPane.setSelectedIndex(0);
                        epExpandedText.requestFocus ();
                    }
                });
            }
        } else if (e.getSource () == bRemove) {
            CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
            int index = tTemplates.convertRowIndexToModel(tTemplates.getSelectedRow());
            unsavedTemplateIndex = -1;
            tableModel.removeCodeTemplate(index);

            int rowCount = tableModel.getRowCount();
            if (index < rowCount) {
                tTemplates.getSelectionModel().setSelectionInterval(index, index);
            } else if (rowCount > 0) {
                tTemplates.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
            } else {
                bRemove.setEnabled (false);
            }
        } else if (e.getSource () == cbExpandTemplateOn) {
            switch (cbExpandTemplateOn.getSelectedIndex ()) {
                case 0:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, 0));
                    break;
                case 1:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_SPACE, KeyEvent.SHIFT_MASK));
                    break;
                case 2:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_TAB, 0));
                    break;
                case 3:
                    model.setExpander (KeyStroke.getKeyStroke (KeyEvent.VK_ENTER, 0));
                    break;
            }
        } else if (e.getSource() == cbOnExpandAction) {
            switch (cbOnExpandAction.getSelectedIndex()) {
                case 0:
                    model.setOnExpandAction(OnExpandAction.FORMAT);
                    break;
                case 1:
                    model.setOnExpandAction(OnExpandAction.INDENT);
                    break;
                default:
                    model.setOnExpandAction(OnExpandAction.NOOP);
                    break;
            }
        }
    }

    private boolean checkAbbrev (String abbrev, String mimeType) {
        MimePath mimePath = MimePath.get(mimeType);
        Preferences prefs = MimeLookup.getLookup(mimePath).lookup(Preferences.class);
        Acceptor acceptor = AbbrevDetection.getResetAcceptor(prefs, mimePath);
        for (int i = 0; i < abbrev.length(); i++) {
            if (acceptor.accept(abbrev.charAt(i)))
                return false;
        }
        return true;
    }
    
    public void valueChanged (ListSelectionEvent e) {
        saveCurrentTemplate ();
        // new line in code templates table has been selected
        int index = tTemplates.getSelectedRow ();
        if (index < 0) {
            epDescription.setText(""); //NOI18N
            epExpandedText.setText(""); //NOI18N
            bRemove.setEnabled (false);
            unsavedTemplateIndex = -1;
            return;
        }
        
        // Show details of the newly selected code tenplate
        CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
        // if the user sorted a column then the view-model mapping is not the same
        int convertRowIndexToModel = tTemplates.convertRowIndexToModel(index);
        // Don't use JEditorPane.setText(), because it goes through EditorKit.read()
        // and performs conversion as if the text was read from a file (eg. EOL
        // translations). See #130095 for details.
        setDocumentText(epDescription.getDocument(), tableModel.getDescription(convertRowIndexToModel));
        setDocumentText(epExpandedText.getDocument(), tableModel.getText(convertRowIndexToModel));
        selectedContexts = tableModel.getContexts(convertRowIndexToModel);
        lContexts.repaint();
        // Mark unmodified explicitly - setDocumentText() marked as modified
        unsavedTemplateIndex = -1;
        bRemove.setEnabled(true);
        if(index != convertRowIndexToModel) { // probably user sorted a column, so make the selection visible
            tTemplates.scrollRectToVisible(new Rectangle(tTemplates.getCellRect(index, 0, true)));
        }
    }
    
    private static void setDocumentText(Document doc, String text) {
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, null);
        } catch (BadLocationException ble) {
            LOG.log(Level.WARNING, null, ble);
        }
    }
    
    private void saveCurrentTemplate() {
        if (unsavedTemplateIndex < 0) {
            return;
        }

        CodeTemplatesModel.TM tableModel = (CodeTemplatesModel.TM)tTemplates.getModel();
        // Don't use JEditorPane.getText(), because it goes through EditorKit.write()
        // and performs conversion as if the text was written to a file (eg. EOL
        // translations). See #130095 for details.
        try {
            tableModel.setDescription(unsavedTemplateIndex, CharSequenceUtilities.toString(DocumentUtilities.getText(epDescription.getDocument(), 0, epDescription.getDocument().getLength())));
            tableModel.setText(unsavedTemplateIndex, CharSequenceUtilities.toString(DocumentUtilities.getText(epExpandedText.getDocument(), 0, epExpandedText.getDocument().getLength())));
            unsavedTemplateIndex = -1;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }
        firePropertyChange(OptionsPanelController.PROP_CHANGED, null, null);
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        // XXX: hack for #113802
        if (e.getKeyCode() == 32) {
            e.consume();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    private void textModified() {
        if (unsavedTemplateIndex < 0) {
            int row = tTemplates.getSelectedRow();
            unsavedTemplateIndex = row < 0 ? -1 : tTemplates.convertRowIndexToModel(row);
        }
        editParametersButton.setEnabled(ParameterValidator.containsNonReservedParameter(epExpandedText.getText()));
    }

    public void insertUpdate(DocumentEvent e) {
        textModified();
    }

    public void removeUpdate(DocumentEvent e) {
        textModified();
    }

    public void changedUpdate(DocumentEvent e) {
    }

    // UI form .................................................................
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lLanguage = new javax.swing.JLabel();
        cbLanguage = new javax.swing.JComboBox();
        lTemplates = new javax.swing.JLabel();
        bNew = new javax.swing.JButton();
        bRemove = new javax.swing.JButton();
        lExplandTemplateOn = new javax.swing.JLabel();
        cbExpandTemplateOn = new javax.swing.JComboBox();
        lOnExpandAction = new javax.swing.JLabel();
        cbOnExpandAction = new javax.swing.JComboBox();
        jSplitPane1 = new javax.swing.JSplitPane();
        tabPane = new javax.swing.JTabbedPane();
        spExpandedText = new javax.swing.JScrollPane();
        epExpandedText = new javax.swing.JEditorPane();
        spDescription = new javax.swing.JScrollPane();
        epDescription = new javax.swing.JEditorPane();
        spTemplates = new javax.swing.JScrollPane();
        tTemplates = new javax.swing.JTable();
        insertParameterButton = new javax.swing.JButton();
        editParametersButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lLanguage.setLabelFor(cbLanguage);
        lLanguage.setText("Language:");

        cbLanguage.setNextFocusableComponent(tTemplates);

        lTemplates.setLabelFor(tTemplates);
        lTemplates.setText("Templates:");

        bNew.setText("New");
        bNew.setNextFocusableComponent(bRemove);

        bRemove.setText("Remove");

        lExplandTemplateOn.setLabelFor(cbExpandTemplateOn);
        lExplandTemplateOn.setText("Expand Template on:");

        cbExpandTemplateOn.setNextFocusableComponent(cbOnExpandAction);

        lOnExpandAction.setLabelFor(cbOnExpandAction);
        lOnExpandAction.setText("On Template Expand:");

        cbOnExpandAction.setNextFocusableComponent(bNew);

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        tabPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabPane.setFocusCycleRoot(true);
        tabPane.setNextFocusableComponent(cbExpandTemplateOn);

        spExpandedText.setViewportView(epExpandedText);

        tabPane.addTab("tab1", spExpandedText);

        spDescription.setViewportView(epDescription);

        tabPane.addTab("tab2", spDescription);

        jSplitPane1.setBottomComponent(tabPane);

        tTemplates.setAutoCreateRowSorter(true);
        tTemplates.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Abbreviation", "Expanded Text", "Description"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tTemplates.setFocusCycleRoot(true);
        spTemplates.setViewportView(tTemplates);

        jSplitPane1.setLeftComponent(spTemplates);

        insertParameterButton.setText(org.openide.util.NbBundle.getMessage(CodeTemplatesPanel.class, "CTL_Insert_Parameter")); // NOI18N
        insertParameterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertParameterButtonActionPerformed(evt);
            }
        });

        editParametersButton.setText(org.openide.util.NbBundle.getMessage(CodeTemplatesPanel.class, "CTL_Edit_Parameters")); // NOI18N
        editParametersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editParametersButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 343, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(bNew, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(bRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lLanguage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lTemplates)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(insertParameterButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                        .addComponent(lExplandTemplateOn)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbExpandTemplateOn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lOnExpandAction)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbOnExpandAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(editParametersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lLanguage)
                    .addComponent(cbLanguage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lTemplates)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bRemove)
                        .addGap(0, 240, Short.MAX_VALUE))
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(insertParameterButton)
                    .addComponent(editParametersButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbExpandTemplateOn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lOnExpandAction)
                    .addComponent(cbOnExpandAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lExplandTemplateOn))
                .addGap(5, 5, 5))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void insertParameterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertParameterButtonActionPerformed
        String language = cbLanguage.getSelectedItem().toString();
        showPopup(Arrays.stream(CodeTemplateHint.values())
                .filter(hint -> hint.getLanguages().contains(language))
                .collect(Collectors.toList()));
    }//GEN-LAST:event_insertParameterButtonActionPerformed

    private void showPopup(List<CodeTemplateHint> hints) {
        SwingUtilities.invokeLater(() -> {
            try {
                Rectangle caretRectangle = epExpandedText.modelToView(epExpandedText.getCaretPosition());
                if (caretRectangle == null) {
                    return;
                }
                Point where = new Point(
                        (int) caretRectangle.getX(), 
                        (int) (caretRectangle.getY() + caretRectangle.getHeight()));
                SwingUtilities.convertPointToScreen(where, epExpandedText);
                PopupUtil.showPopup(
                        new CodeTemplateParametersPanel(epExpandedText, hints),
                        (Frame) SwingUtilities.getAncestorOfClass(Frame.class, epExpandedText),
                        where.getX(),
                        where.getY(),
                        true,
                        caretRectangle.getHeight());
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }
    
    private void editParametersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editParametersButtonActionPerformed
        showEditParametersDialog();
    }//GEN-LAST:event_editParametersButtonActionPerformed
    
    private void showEditParametersDialog() {
        CodeTemplateParametersDialog dialog = 
                new CodeTemplateParametersDialog(cbLanguage.getSelectedItem().toString(), epExpandedText.getText());
        if (dialog.isOkButtonPressed()) {
            epExpandedText.setText(
                    ParametrizedTextBuilder.build(dialog.getTableData(), epExpandedText.getText()));
        }
        dialog.close();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bNew;
    private javax.swing.JButton bRemove;
    private javax.swing.JComboBox cbExpandTemplateOn;
    private javax.swing.JComboBox cbLanguage;
    private javax.swing.JComboBox cbOnExpandAction;
    private javax.swing.JButton editParametersButton;
    private javax.swing.JEditorPane epDescription;
    private javax.swing.JEditorPane epExpandedText;
    private javax.swing.JButton insertParameterButton;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JLabel lExplandTemplateOn;
    private javax.swing.JLabel lLanguage;
    private javax.swing.JLabel lOnExpandAction;
    private javax.swing.JLabel lTemplates;
    private javax.swing.JScrollPane spDescription;
    private javax.swing.JScrollPane spExpandedText;
    private javax.swing.JScrollPane spTemplates;
    private javax.swing.JTable tTemplates;
    private javax.swing.JTabbedPane tabPane;
    // End of variables declaration//GEN-END:variables

    private javax.swing.JScrollPane spContexts;
    private JList<String> lContexts;
    private Set<String> selectedContexts;

    private class ListRenderer implements ListCellRenderer<String> {

        private final JCheckBox renderer = new JCheckBox();

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
            renderer.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            renderer.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            renderer.setText(value);
            renderer.setSelected(selectedContexts != null && selectedContexts.contains(value));
            renderer.setOpaque(true);
            return renderer;
        }
    }
    
    private class CheckListener implements MouseListener, KeyListener {

        public void mouseEntered(MouseEvent e) {}

        public void mouseExited(MouseEvent e) {}

        public void mousePressed(MouseEvent e) {}

        public void mouseReleased(MouseEvent e) {}

        public void mouseClicked(MouseEvent e) {
            if (!e.isPopupTrigger()) {
                contextsModified();
                e.consume();
            }
        }

        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                contextsModified();
                e.consume();
            }
        }
        
        public void keyReleased(KeyEvent e) {}

        public void keyTyped(KeyEvent e) {}

        private void contextsModified() {
            String value = lContexts.getSelectedValue();
            if (selectedContexts != null && !selectedContexts.remove(value)) {
                selectedContexts.add(value);
            }
            lContexts.repaint();
            if (unsavedTemplateIndex < 0) {
                int row = tTemplates.getSelectedRow();
                unsavedTemplateIndex = row < 0 ? -1 : tTemplates.convertRowIndexToModel(row);
            }
        }
    }
    
    private static class ParametrizedTextBuilder {
        
        private static String build(List<?> data, String parametrizedText) {
            ParametrizedTextParser parser = new ParametrizedTextParser(null, parametrizedText);
            parser.parse();
            Map<Integer, Object> parametrizedFragmentsByOrdinals = parser.getParametrizedFragmentsByOrdinals();
            StringBuilder insertTextBuffer = new StringBuilder();
            int row = 0;
            for (int ordinal = 0; ordinal < parametrizedFragmentsByOrdinals.size(); ordinal++) {
                if (ordinal % 2 == 0) {
                    String fragment = (String) parametrizedFragmentsByOrdinals.get(ordinal);
                    fragment = fragment.replaceAll("\\$", "\\$\\$"); //NOI18N
                    insertTextBuffer.append(fragment);
                } else {
                    CodeTemplateParameterImpl paramImpl =
                            (CodeTemplateParameterImpl) parametrizedFragmentsByOrdinals.get(ordinal);
                    String paramName = paramImpl.getName();
                    if (!paramName.equals("<null>") && paramImpl.isSlave()) { //NOI18N
                        insertTextBuffer.append("${").append(paramImpl.getName()).append("}"); //NOI18N
                    } else if (paramName.equals(CodeTemplateParameter.CURSOR_PARAMETER_NAME)) {
                        insertTextBuffer.append("${cursor}"); //NOI18N
                    } else if (paramName.equals(CodeTemplateParameter.SELECTION_PARAMETER_NAME)) {
                        insertTextBuffer.append("${selection}"); //NOI18N
                    } else if (paramName.equals(CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME)) {
                        insertTextBuffer.append("${no-format}"); //NOI18N
                    } else if (paramName.equals(CodeTemplateParameter.NO_INDENT_PARAMETER_NAME)) {
                        insertTextBuffer.append("${no-indent}"); //NOI18N
                    } else {
                        insertTextBuffer.append("${"); //NOI18N
                        int numberOfColumns = 6;
                        for (int column = 0; column < numberOfColumns; column++) {
                            if (row < data.size()) {
                                switch (column) {
                                    case 0: { //Name
                                        String name = (String) ((List) data.get(row)).get(column);
                                        if (!name.isEmpty()) {
                                            insertTextBuffer.append(name);
                                        }
                                        break;
                                    }
                                    case 1: { //Hint
                                        String hint = (String) (((List) data.get(row)).get(column));
                                        if (!hint.isEmpty()) {
                                            insertTextBuffer.append(" ").append(hint); //NOI18N
                                        }
                                        break;
                                    }
                                    case 2: { //Default value
                                        String defaultValue = (String) ((List) data.get(row)).get(column);
                                        if (!defaultValue.isEmpty()) {
                                            insertTextBuffer.append(" default=\"").append(defaultValue).append("\""); //NOI18N
                                        }
                                        break;
                                    }
                                    case 3: { //Ordering
                                        Object orderingHint = ((List) data.get(row)).get(column);
                                        if (orderingHint instanceof String) {
                                            String ordering = (String) ((List) data.get(row)).get(column);
                                            if (!ordering.isEmpty()) {
                                                insertTextBuffer.append(" ordering=").append(ordering); //NOI18N
                                            }
                                        } else {
                                            Integer ordering = (Integer) ((List) data.get(row)).get(column);
                                            insertTextBuffer.append(" ordering=").append(ordering); //NOI18N
                                        }
                                        break;
                                    }
                                    case 4: { //Completion
                                        boolean completionInvoke = (boolean) ((List) data.get(row)).get(column);
                                        if (completionInvoke) {
                                            insertTextBuffer.append(" completionInvoke"); //NOI18N
                                        }
                                        break;
                                    }
                                    default: { //Editable
                                        boolean editable = (boolean) ((List) data.get(row)).get(column);
                                        if (!editable) {
                                            insertTextBuffer.append(" editable=false"); //NOI18N
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                        row++;
                        insertTextBuffer.append("}"); //NOI18N
                    }
                }
            }
            return insertTextBuffer.toString();
        }
        
    }
    
    private static class ParameterValidator {
        
        private static boolean containsNonReservedParameter(String parametrizedText) {
            ParametrizedTextParser parser = new ParametrizedTextParser(null, parametrizedText);
            parser.parse();
            Map<Integer, Object> parametrizedFragmentsByOrdinals = parser.getParametrizedFragmentsByOrdinals();
            int numberOfFragments = parametrizedFragmentsByOrdinals.size();
            for (int idx = 1; idx < numberOfFragments; idx += 2) {
                CodeTemplateParameterImpl paramImpl = (CodeTemplateParameterImpl) parametrizedFragmentsByOrdinals.get(idx);
                if (!isReservedParameter(paramImpl) 
                        && panelLanguage != null 
                        && hasValidHints(paramImpl, panelLanguage)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isReservedParameter(CodeTemplateParameterImpl paramImpl) {
            String paramName = paramImpl.getName();
            return paramName.equals(CodeTemplateParameter.CURSOR_PARAMETER_NAME)
                    || paramName.equals(CodeTemplateParameter.SELECTION_PARAMETER_NAME)
                    || paramName.equals(CodeTemplateParameter.NO_FORMAT_PARAMETER_NAME)
                    || paramName.equals(CodeTemplateParameter.NO_INDENT_PARAMETER_NAME);
        }
        
        private static boolean hasValidHints(CodeTemplateParameterImpl paramImpl, String language) {
            Function<String, Boolean> isSupportedHint = hintName -> {
                        return Arrays.stream(CodeTemplateHint.values())
                                .filter(hint -> hint.getLanguages().contains(language)
                                        || hint.getLanguages().contains(CodeTemplateHint.ALL_LANGUAGES))
                                .anyMatch(hint -> hint.getName().equals(hintName));
                    };
            Function<CodeTemplateParameterImpl, Boolean> checkHints = parameter -> {
                Set<String> hints = parameter.getHints().keySet();
                Iterator<String> iterator = hints.iterator();
                boolean valid = true;
                while (iterator.hasNext()) {
                    if (!isSupportedHint.apply(iterator.next())) {
                        valid = false;
                        break;
                    }
                }
                return valid;
            };
            return checkHints.apply(paramImpl);
        }
        
    }
    
}
