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

package org.netbeans.modules.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ItemSelectable;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.JTextComponent;
import org.netbeans.api.search.ReplacePattern;
import org.netbeans.api.search.SearchHistory;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchPattern.MatchType;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.ui.ComponentUtils;
import org.netbeans.api.search.ui.FileNameController;
import org.netbeans.api.search.ui.ScopeController;
import org.netbeans.api.search.ui.ScopeOptionsController;
import org.netbeans.api.search.ui.SearchPatternController;
import org.netbeans.api.search.ui.SearchPatternController.Option;
import org.netbeans.modules.search.ui.FormLayoutHelper;
import org.netbeans.modules.search.ui.LinkButtonPanel;
import org.netbeans.modules.search.ui.PatternChangeListener;
import org.netbeans.modules.search.ui.ShorteningCellRenderer;
import org.netbeans.modules.search.ui.TextFieldFocusListener;
import org.netbeans.modules.search.ui.UiUtils;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author  Marian Petras
 */
final class BasicSearchForm extends JPanel implements ChangeListener,
                                                      ItemListener {

    private final String preferredSearchScopeType;
    private ChangeListener usabilityChangeListener;
    private BasicSearchCriteria searchCriteria = new BasicSearchCriteria();
    private SearchScopeDefinition[] extraSearchScopes;
    private boolean searchInGeneratedSetAutomatically = false;
    private PropertyChangeListener topComponentRegistryListener;

    /** Creates new form BasicSearchForm */
    BasicSearchForm(String preferredSearchScopeType,
            boolean searchAndReplace, BasicSearchCriteria initialCriteria,
            SearchScopeDefinition... extraSearchScopes) {

        this.preferredSearchScopeType = preferredSearchScopeType;
        this.extraSearchScopes = extraSearchScopes;
        initComponents(searchAndReplace);
        initAccessibility(searchAndReplace);
        initHistory();
        if (searchAndReplace && (searchCriteria.getReplaceExpr() == null)) {
            /* We must set the initial replace string, otherwise it might not
             * be initialized at all if the user keeps the field "Replace With:"
             * empty. One of the side-effects would be that method
             * BasicSearchCriteria.isSearchAndReplace() would return 'false'. */
            searchCriteria.setReplaceExpr("");                        //NOI18N
        }
        initInteraction(searchAndReplace);
        setValuesOfComponents(initialCriteria, searchAndReplace);
        setContextAwareOptions(searchAndReplace);
    }

    /**
     * Set values of form components.
     *
     * Interaction must be already set up when we set values, otherwise state of
     * the dialog might not be corresponding to the values, e.g. the Find dialog
     * could be disabled although valid values are entered.
     */
    private void setValuesOfComponents(
            BasicSearchCriteria initialCriteria, boolean searchAndReplace) {
        
        if (initialCriteria != null) {
            initValuesFromCriteria(initialCriteria, searchAndReplace);
        } else {
            initValuesFromHistory(searchAndReplace);
        }
        if (searchAndReplace) {
            updateReplacePatternColor();
        }
        useCurrentlySelectedText();
        setSearchCriteriaValues();
        updateTextToFindInfo();
        updateFileNamePatternInfo();
    }

    /**
     * Set currently selected text (in editor) as "Text to find" value.
     */
    public void useCurrentlySelectedText() {
        Node[] arr = TopComponent.getRegistry().getActivatedNodes();
        if (arr.length > 0) {
            EditorCookie ec = arr[0].getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                JEditorPane recentPane = NbDocument.findRecentEditorPane(ec);
                if (recentPane != null) {
                    String initSearchText = recentPane.getSelectedText();
                    if (initSearchText != null) {
                        cboxTextToFind.setSearchPattern(SearchPattern.create(
                                initSearchText, false, false, false));
                        searchCriteria.setTextPattern(initSearchText);
                        return;
                    }
                }
            }
        }
        searchCriteria.setTextPattern(
                cboxTextToFind.getSearchPattern().getSearchExpression());
    }
    
    /**
     * Set options that depend on current context, and listeners that ensure
     * they stay valid when the context changes.
     */
    private void setContextAwareOptions(boolean searchAndReplace) {
        if (!searchAndReplace) {
            updateSearchInGeneratedForActiveTopComponent();
            topComponentRegistryListener = (PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals(
                        TopComponent.Registry.PROP_ACTIVATED)) {
                    updateSearchInGeneratedForActiveTopComponent();
                }
            };
            TopComponent.getRegistry().addPropertyChangeListener(
                    WeakListeners.propertyChange(topComponentRegistryListener,
                    TopComponent.getRegistry()));
        }
    }

    /**
     * Update searching in generated sources. If Files window is selected,
     * Search In Generated Sources option should be checked automatically.
     */
    private void updateSearchInGeneratedForActiveTopComponent() {
        assert searchCriteria != null && !searchCriteria.isSearchAndReplace();
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null && tc.getHelpCtx() != null
                && "ProjectTab_Files".equals( //NOI18N
                tc.getHelpCtx().getHelpID())) {
            if (!scopeSettingsPanel.isSearchInGenerated()) {
                scopeSettingsPanel.setSearchInGenerated(true);
                searchInGeneratedSetAutomatically = true;
            }
        } else if (searchInGeneratedSetAutomatically) {
            scopeSettingsPanel.setSearchInGenerated(false);
            searchInGeneratedSetAutomatically = false;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     * 
     * @param  searchAndReplace  {@code true} if components for
     *				 search &amp; replace should be created;
     *                           {@code false} otherwise
     */
    @SuppressWarnings("unchecked")
    private void initComponents(final boolean searchAndReplace) {

        lblTextToFind = new JLabel();
        JComboBox<String> box = new JComboBox<>();
        box.setEditor(new MultiLineComboBoxEditor(box));
        cboxTextToFind = ComponentUtils.adjustComboForSearchPattern(box);
        lblTextToFind.setLabelFor(cboxTextToFind.getComponent());
        btnTestTextToFind = new JButton();
        lblTextToFindHint = new JLabel();
        lblTextToFindHint.setEnabled(false);
        setLengthFilter(cboxTextToFind.getComponent());

        if (searchAndReplace) {
            lblReplacement = new JLabel();
            cboxReplacement = new JComboBox<>();
            cboxReplacement.setEditor(new MultiLineComboBoxEditor(cboxReplacement));
            cboxReplacement.setEditable(true);
            cboxReplacement.setRenderer(new ShorteningCellRenderer());
            lblReplacement.setLabelFor(cboxReplacement);
            chkPreserveCase = new JCheckBox();
            setLengthFilter(cboxReplacement);
        }

        lblScope = new JLabel();
        cboxScope = ComponentUtils.adjustComboForScope(new JComboBox<>(),
                preferredSearchScopeType, extraSearchScopes);
        lblScope.setLabelFor(cboxScope.getComponent());

        lblFileNamePattern = new JLabel();
        lblFileNameHint = new JLabel();
        lblFileNameHint.setEnabled(false);
        cboxFileNamePattern = ComponentUtils.adjustComboForFileName(
                new JComboBox<String>());
        lblFileNamePattern.setLabelFor(cboxFileNamePattern.getComponent());
        setLengthFilter(cboxFileNamePattern.getComponent());
        
        chkWholeWords = new JCheckBox();
        chkCaseSensitive = new JCheckBox();
        textToFindType = new TextToFindTypeComboBox();

        TextPatternCheckBoxGroup.bind(
                chkCaseSensitive, chkWholeWords, textToFindType, chkPreserveCase);

        setMnemonics(searchAndReplace);
        
        initFormPanel(searchAndReplace);
        updateTextToFindInfo();
        this.add(formPanel);

        /* find the editor components of combo-boxes: */
        Component cboxEditorComp;
        if (cboxReplacement != null) {
            cboxEditorComp = cboxReplacement.getEditor().getEditorComponent();
            replacementPatternEditor = (JTextComponent) cboxEditorComp;
        }
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    /**
     * Initialize form panel and add appropriate components to it.
     */
    protected void initFormPanel(boolean searchAndReplace) {

        formPanel = new SearchFormPanel();
        formPanel.addRow(lblTextToFind, cboxTextToFind.getComponent(), true);
        JPanel hintAndButtonPanel = initHintAndButtonPanel();
        formPanel.addRow(new JLabel(), hintAndButtonPanel);
        initContainingTextOptionsRow(searchAndReplace);
        if (searchAndReplace) {
            formPanel.addRow(lblReplacement, cboxReplacement, true);
        }
        formPanel.addSeparator();
        formPanel.addRow(lblScope, cboxScope.getComponent());
        initScopeOptionsRow(searchAndReplace);
        formPanel.addSeparator();
        formPanel.addRow(lblFileNamePattern, cboxFileNamePattern.getComponent());
        formPanel.addRow(new JLabel(), lblFileNameHint);
        formPanel.addRow(new JLabel(), scopeSettingsPanel.getFileNameComponent());
        formPanel.addEmptyLine();
    }

    /**
     * Initialize panel for controls for text pattern options and add it to the
     * form panel.
     */
    private void initContainingTextOptionsRow(boolean searchAndReplace) {

        JPanel jp = new JPanel();
        JPanel typeWithLabelPanel = new JPanel();
        typeWithLabelPanel.setLayout(new BoxLayout(typeWithLabelPanel,
                BoxLayout.LINE_AXIS));
        JLabel typeLabel = new JLabel();
        typeLabel.setLabelFor(textToFindType);
        lclz(typeLabel, "BasicSearchForm.textToFindType.label.text");   //NOI18N
        typeLabel.setBorder(new EmptyBorder(0, 10, 0, 5));
        typeWithLabelPanel.add(typeLabel);
        typeWithLabelPanel.add(textToFindType);
        if (searchAndReplace) {
            FormLayoutHelper flh = new FormLayoutHelper(jp,
                    FormLayoutHelper.DEFAULT_COLUMN,
                    FormLayoutHelper.DEFAULT_COLUMN);
            flh.addRow(chkCaseSensitive, chkPreserveCase);
            flh.addRow(chkWholeWords, typeWithLabelPanel);
            jp.setMaximumSize(jp.getMinimumSize());

            formPanel.addRow(new JLabel(), jp);
        } else {
            jp.setLayout(new BoxLayout(jp, BoxLayout.LINE_AXIS));
            jp.add(chkCaseSensitive);
            jp.add(chkWholeWords);
            jp.add(typeWithLabelPanel);
            formPanel.addRow(new JLabel(), jp);
        }
    }

    private void initScopeOptionsRow(boolean searchAndReplace) {
        this.scopeSettingsPanel = ComponentUtils.adjustPanelsForOptions(
                new JPanel(), new JPanel(), searchAndReplace,
                cboxFileNamePattern);
        formPanel.addRow(new JLabel(), scopeSettingsPanel.getComponent());
    }

    /**
     */
    private void initAccessibility(boolean searchAndReplace) {
        chkCaseSensitive.getAccessibleContext().setAccessibleDescription(
                UiUtils.getText(
                "BasicSearchForm.chkCaseSensitive."                     //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        textToFindType.getAccessibleContext().setAccessibleDescription(
                UiUtils.getText(
                "BasicSearchForm.textToFindType."                       //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        chkWholeWords.getAccessibleContext().setAccessibleDescription(
                UiUtils.getText(
                "BasicSearchForm.chkWholeWords."                        //NOI18N
                + "AccessibleDescription"));                            //NOI18N
        if (searchAndReplace) {
            cboxReplacement.getAccessibleContext().setAccessibleDescription(
                    UiUtils.getText(
                    "BasicSearchForm.cbox.Replacement."                 //NOI18N
                    + "AccessibleDescription"));                        //NOI18N
            chkPreserveCase.getAccessibleContext().setAccessibleDescription(
                    UiUtils.getText(
                    "BasicSearchForm.chkPreserveCase."                  //NOI18N
                    + "AccessibleDescription"));                        //NOI18N
        }
    }

    /**
     * Fills text and sets values of check-boxes according to the current
     * search criteria.
     */
    private void initValuesFromCriteria(BasicSearchCriteria initialCriteria,
            boolean searchAndReplace) {
        cboxTextToFind.setSearchPattern(initialCriteria.getSearchPattern());
        if (cboxReplacement != null) {
            cboxReplacement.setSelectedItem(new ReplaceModelItem(
                    ReplacePattern.create(initialCriteria.getReplaceExpr(),
                    initialCriteria.isPreserveCase())));
        }

        selectChk(chkPreserveCase, initialCriteria.isPreserveCase());
        scopeSettingsPanel.setFileNameRegexp(initialCriteria.isFileNameRegexp());
        scopeSettingsPanel.setUseIgnoreList(initialCriteria.isUseIgnoreList());
        cboxFileNamePattern.setRegularExpression(initialCriteria.isFileNameRegexp());
        cboxFileNamePattern.setFileNamePattern(initialCriteria.getFileNamePatternExpr());
        if (!searchAndReplace) {
            scopeSettingsPanel.setSearchInArchives(
                    initialCriteria.isSearchInArchives());
            scopeSettingsPanel.setSearchInGenerated(
                    initialCriteria.isSearchInGenerated());
        }
    }

    private static void selectChk(JCheckBox checkbox, boolean value) {
        if (checkbox != null) {
            checkbox.setSelected(value);
        }
    }
    
    /**
     */
    private void initInteraction(final boolean searchAndReplace) {
        /* set up updating of the validity status: */
        

        final TextFieldFocusListener focusListener = new TextFieldFocusListener();
        if (replacementPatternEditor != null) {
            replacementPatternEditor.addFocusListener(focusListener);
        }

        if (replacementPatternEditor != null) {
            replacementPatternEditor.getDocument().addDocumentListener(
                    new ReplacementPatternListener());
        }
        
        textToFindType.addItemListener(this);
        cboxTextToFind.bindMatchTypeComboBox(textToFindType);
        cboxTextToFind.bind(Option.MATCH_CASE, chkCaseSensitive);
        cboxTextToFind.bind(Option.WHOLE_WORDS, chkWholeWords);
        textToFindType.addActionListener((ActionEvent e) -> {
        });

        boolean regexp = textToFindType.isRegexp();
        boolean caseSensitive = chkCaseSensitive.isSelected();
        chkWholeWords.setEnabled(!regexp);
        if (searchAndReplace) {
            chkPreserveCase.addItemListener(this);
            chkPreserveCase.setEnabled(!regexp && !caseSensitive);
        }
        searchCriteria.setUsabilityChangeListener(this);

        scopeSettingsPanel.addChangeListener((ChangeEvent e) -> {
            searchCriteria.setSearchInArchives(
                    scopeSettingsPanel.isSearchInArchives());
            searchCriteria.setSearchInGenerated(
                    scopeSettingsPanel.isSearchInGenerated());
            searchCriteria.setUseIgnoreList(
                    scopeSettingsPanel.isUseIgnoreList());
        });

        cboxFileNamePattern.addChangeListener((ChangeEvent e) -> {
            searchCriteria.setFileNamePattern(
                    cboxFileNamePattern.getFileNamePattern());
            searchCriteria.setFileNameRegexp(
                    cboxFileNamePattern.isRegularExpression());
            updateFileNamePatternInfo();
        });

        cboxTextToFind.addChangeListener((ChangeEvent e) -> {
            SearchPattern sp = cboxTextToFind.getSearchPattern();
            searchCriteria.setTextPattern(sp.getSearchExpression());
            searchCriteria.setMatchType(sp.getMatchType());
            searchCriteria.setWholeWords(sp.isWholeWords());
            searchCriteria.setCaseSensitive(sp.isMatchCase());
        });
        initButtonInteraction();
    }

    private void initButtonInteraction() {
        btnTestTextToFind.addActionListener((ActionEvent e) -> openTextPatternSandbox());
    }

    private void openTextPatternSandbox() {

        SearchPattern sp = cboxTextToFind.getSearchPattern();
        String expr = sp.getSearchExpression() == null ? "" // NOI18N
                : sp.getSearchExpression();
        boolean matchCase = chkCaseSensitive.isSelected();

        PatternSandbox.openDialog(new PatternSandbox.TextPatternSandbox(
                expr, matchCase) {

            @Override
            protected void onApply(String newExpr, boolean newMatchCase) {
                cboxTextToFind.setSearchPattern(SearchPattern.create(
                        newExpr, false, newMatchCase, true));
            }
        }, btnTestTextToFind);
    }

    /**
     * Initializes pop-ups of combo-boxes with last entered patterns and
     * expressions. The combo-boxes' text-fields remain empty.
     */
    private void initHistory() {

        List<ReplaceModelItem> entries = new ArrayList<>(10);
        if (cboxReplacement != null) {
            for (ReplacePattern replacePattern
                    : SearchHistory.getDefault().getReplacePatterns()) {
                entries.add(0, new ReplaceModelItem(replacePattern));
            }
            if (!entries.isEmpty()) {
                cboxReplacement.setModel(new ListComboBoxModel<>(entries, true));
            }
        }
    }
    
    /**
     */
    private void initValuesFromHistory(final boolean searchAndReplace) {
        final FindDialogMemory memory = FindDialogMemory.getDefault();

        if (memory.isFileNamePatternSpecified()
                && cboxFileNamePattern.getComponent().getItemCount() != 0) {
            cboxFileNamePattern.getComponent().setSelectedIndex(0);
        }
        cboxFileNamePattern.setRegularExpression(memory.isFilePathRegex());
        if (cboxReplacement != null && cboxReplacement.getItemCount() != 0
                && FindDialogMemory.getDefault().isReplacePatternSpecified()) {
            cboxReplacement.setSelectedIndex(0);
        }

        chkWholeWords.setSelected(memory.isWholeWords());
        chkCaseSensitive.setSelected(memory.isCaseSensitive());
        textToFindType.setSelectedItem(memory.getMatchType());

        scopeSettingsPanel.setFileNameRegexp(memory.isFilePathRegex());
        scopeSettingsPanel.setUseIgnoreList(memory.IsUseIgnoreList());
        if (searchAndReplace) {
            chkPreserveCase.setSelected(memory.isPreserveCase());
        } else {
            scopeSettingsPanel.setSearchInArchives(memory.isSearchInArchives());
            scopeSettingsPanel.setSearchInGenerated(
                    memory.isSearchInGenerated());
        }
    }
    
    private void setSearchCriteriaValues() {
        searchCriteria.setWholeWords(chkWholeWords.isSelected());
        searchCriteria.setCaseSensitive(chkCaseSensitive.isSelected());
        searchCriteria.setMatchType(textToFindType.getSelectedMatchType());
        searchCriteria.setFileNameRegexp(scopeSettingsPanel.isFileNameRegExp());
        searchCriteria.setUseIgnoreList(scopeSettingsPanel.isUseIgnoreList());
        searchCriteria.setSearchInArchives(
                scopeSettingsPanel.isSearchInArchives());
        searchCriteria.setSearchInGenerated(
                scopeSettingsPanel.isSearchInGenerated());
        if (chkPreserveCase != null) {
            searchCriteria.setPreserveCase(chkPreserveCase.isSelected());
        }
    }

    @Override
    public boolean requestFocusInWindow() {
	return cboxTextToFind.getComponent().requestFocusInWindow();
    }

    /**
     * Sets proper color of replace pattern.
     */
    private void updateReplacePatternColor() {
        boolean wasInvalid = invalidReplacePattern;
        invalidReplacePattern = searchCriteria.isReplacePatternInvalid();
        if (invalidReplacePattern != wasInvalid) {
            if (defaultTextColor == null) {
                assert !wasInvalid;
                defaultTextColor = cboxReplacement.getForeground();
            }
            replacementPatternEditor.setForeground(
                    invalidReplacePattern ? getErrorTextColor()
                                       : defaultTextColor);
        }
    }

    private static boolean isBackrefSyntaxUsed(String text) {
        final int len = text.length();
        if (len < 2) {
            return false;
        }
        String textToSearch = text.substring(0, len - 1);
        int startIndex = 0;
        int index;
        while ((index = textToSearch.indexOf('\\', startIndex)) != -1) {
            char c = text.charAt(index + 1);
            if (c == '\\') {
                startIndex = index + 1;
            } else if ((c >= '0') && (c <= '9')) {
                return true;
            } else {
                startIndex = index + 2;
            }
        }
        return false;
    }
    
    private Color getErrorTextColor() {
        if (errorTextColor == null) {
            errorTextColor = UIManager.getDefaults()
                             .getColor("TextField.errorForeground");    //NOI18N
            if (errorTextColor == null) {
                errorTextColor = Color.RED;
            }
        }
        return errorTextColor;
    }
    
    void setUsabilityChangeListener(ChangeListener l) {
        usabilityChangeListener = l;
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        if (usabilityChangeListener != null) {
            usabilityChangeListener.stateChanged(new ChangeEvent(this));
        }
    }
    
    /**
     * Called when some of the check-boxes is selected or deselected.
     * 
     * @param  e  event object holding information about the change
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        final ItemSelectable toggle = e.getItemSelectable();
        final boolean selected = (e.getStateChange() == ItemEvent.SELECTED);
        if (toggle == textToFindType) {
            if (cboxReplacement != null){
                updateReplacePatternColor();
            }
            updateTextToFindInfo();
        } else if (toggle == chkPreserveCase) {
            searchCriteria.setPreserveCase(selected);
        } else {
            assert false;
        }
    }

    private void updateTextToFindInfo() {
        String key;
        switch (cboxTextToFind.getSearchPattern().getMatchType()) {
            case LITERAL:
                key = "BasicSearchForm.cboxTextToFind.info.literal";    //NOI18N
                break;
            case BASIC:
                key = "BasicSearchForm.cboxTextToFind.info";            //NOI18N
                break;
            case REGEXP:
                key = "BasicSearchForm.cboxTextToFind.info.re";         //NOI18N
                break;
            default:
                key = "BasicSearchForm.cboxTextToFind.info";            //NOI18N
        }
        String text = UiUtils.getText(key);
        cboxTextToFind.getComponent().setToolTipText(text);
        lblTextToFindHint.setText(text);
        btnTestTextToFindPanel.setButtonEnabled(
                searchCriteria.getSearchPattern().isRegExp());
    }

    private void updateFileNamePatternInfo() {
        lblFileNameHint.setText(UiUtils.getFileNamePatternsExample(
                cboxFileNamePattern.isRegularExpression()));
    }

    /**
     * Called when the criteria in the Find dialog are confirmed by the user
     * and the search is about to be started.
     * This method just passes the message to the criteria object.
     */
    void onOk() {
        searchCriteria.onOk();

        final FindDialogMemory memory = FindDialogMemory.getDefault();

        if (searchCriteria.isTextPatternUsable()) {
            SearchHistory.getDefault().add(getCurrentSearchPattern());
            memory.setTextPatternSpecified(true);
        } else {
            memory.setTextPatternSpecified(false);
        }
        if (searchCriteria.isFileNamePatternUsable()) {
            memory.storeFileNamePattern(
                    searchCriteria.getFileNamePatternExpr());
            memory.setFileNamePatternSpecified(true);
        } else {
            memory.setFileNamePatternSpecified(false);
        }
        if (replacementPatternEditor != null) {
            String replaceText = replacementPatternEditor.getText();
            SearchHistory.getDefault().addReplace(ReplacePattern.create(
                    replaceText, chkPreserveCase.isSelected()));
            FindDialogMemory.getDefault().setReplacePatternSpecified(
                    replaceText != null && !replaceText.isEmpty());
        }
        memory.setWholeWords(chkWholeWords.isSelected());
        memory.setCaseSensitive(chkCaseSensitive.isSelected());
        memory.setMatchType(textToFindType.getSelectedMatchType());
        if (searchCriteria.isSearchAndReplace()) {
            memory.setPreserveCase(chkPreserveCase.isSelected());
        } else {
            memory.setSearchInArchives(scopeSettingsPanel.isSearchInArchives());
            if (!searchInGeneratedSetAutomatically) {
                memory.setSearchInGenerated(
                        scopeSettingsPanel.isSearchInGenerated());
            }
        }
        memory.setFilePathRegex(scopeSettingsPanel.isFileNameRegExp());
        memory.setUseIgnoreList(scopeSettingsPanel.isUseIgnoreList());
        if (cboxScope.getSelectedScopeId() != null
                && !SearchPanel.isOpenedForSelection()) {
            memory.setScopeTypeId(cboxScope.getSelectedScopeId());
        }
    }

    /**
     * Read current dialog contents as a SearchPattern.
     *
     * @return SearchPattern for the contents of the current dialog. Null if the
     * * search string is empty, meaning that the dialog is empty.
     */
    private SearchPattern getCurrentSearchPattern() {
        return cboxTextToFind.getSearchPattern();
    }

    /**
     */
    public SearchInfo getSearchInfo() {
        return cboxScope.getSearchInfo();
    }

    public String getSelectedScopeName() {
        return cboxScope.getSelectedScopeTitle();
    }

    /** */
    BasicSearchCriteria getBasicSearchCriteria() {
        return searchCriteria;
    }
    
    boolean isUsable() {
        return (cboxScope.getSearchInfo() != null)
               && searchCriteria.isUsable();
    }

    private void setMnemonics(boolean searchAndReplace) {

        lclz(lblTextToFind, "BasicSearchForm.lblTextToFind.text");      //NOI18N
        lclz(lblScope, "BasicSearchForm.lblScope.text");                //NOI18N
        lclz(lblFileNamePattern,
                "BasicSearchForm.lblFileNamePattern.text");             //NOI18N
        lclz(chkWholeWords, "BasicSearchForm.chkWholeWords.text");      //NOI18N
        lclz(chkCaseSensitive, "BasicSearchForm.chkCaseSensitive.text");//NOI18N

        btnTestTextToFind.setText(UiUtils.getHtmlLink(
                "BasicSearchForm.btnTestTextToFind.text"));             //NOI18N
        btnTestTextToFind.setToolTipText(UiUtils.getText(
                "BasicSearchForm.btnTestTextToFind.tooltip"));          //NOI18N
       

        if (searchAndReplace) {
            lclz(lblReplacement, "BasicSearchForm.lblReplacement.text");//NOI18N
            lclz(chkPreserveCase,
                    "BasicSearchForm.chkPreserveCase.text");            //NOI18N
        } else {
          
        }
    }

    private void lclz(AbstractButton ab, String msg) {
        UiUtils.lclz(ab, msg);
    }

    private void lclz(JLabel l, String msg) {
        UiUtils.lclz(l, msg);
    }

    private static final Logger watcherLogger = Logger.getLogger(
            "org.netbeans.modules.search.BasicSearchForm.FileNamePatternWatcher");//NOI18N

    private void setLengthFilter(JComboBox<?> cb) {
        Component editorComponent = cb.getEditor().getEditorComponent();
        if (editorComponent instanceof JTextComponent) {
            JTextComponent tc = (JTextComponent) editorComponent;
            Document document = tc.getDocument();
            if (document instanceof AbstractDocument) {
                AbstractDocument ad = (AbstractDocument) document;
                ad.setDocumentFilter(lengthFilter);
            }
        }
    }

    private SearchPatternController cboxTextToFind;
    private JComboBox<ReplaceModelItem> cboxReplacement;
    private FileNameController cboxFileNamePattern;
    private JCheckBox chkWholeWords;
    private JCheckBox chkCaseSensitive;
    private TextToFindTypeComboBox textToFindType;
    private JCheckBox chkPreserveCase;
    private JTextComponent replacementPatternEditor;
    protected SearchFormPanel formPanel;
    private JButton btnTestTextToFind;
    private LinkButtonPanel btnTestTextToFindPanel;
    private JLabel lblTextToFind;
    private JLabel lblTextToFindHint;
    private ScopeController cboxScope;
    private JLabel lblFileNamePattern;    
    private JLabel lblFileNameHint;
    private JLabel lblScope;
    private JLabel lblReplacement;
    private Color errorTextColor, defaultTextColor;
    private boolean invalidTextPattern = false;
    private boolean invalidReplacePattern = false;
    private ScopeOptionsController scopeSettingsPanel;
    private final DocumentFilter lengthFilter = new LengthFilter();

    private JPanel initHintAndButtonPanel() {
        btnTestTextToFindPanel = new LinkButtonPanel(btnTestTextToFind);
        btnTestTextToFindPanel.setButtonEnabled(
                searchCriteria.getSearchPattern().isRegExp());
        lblTextToFindHint.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                (int) btnTestTextToFindPanel.getPreferredSize().getHeight()));
        JPanel hintAndButtonPanel = new JPanel();
        hintAndButtonPanel.setLayout(
                new BoxLayout(hintAndButtonPanel, BoxLayout.LINE_AXIS));
        hintAndButtonPanel.add(lblTextToFindHint);
        hintAndButtonPanel.add(btnTestTextToFindPanel);
        return hintAndButtonPanel;
    }

    /**
     * Form panel to which rows can be added.
     */
    private final class SearchFormPanel extends JPanel {

        private int row = 0;

        public SearchFormPanel() {
            super();
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(5, 5, 5, 5));
        }

        public void addRow(JComponent label, JComponent component) {
            addRow(label, component, false);
        }

        public void addRow(JComponent label, JComponent component, boolean fillVertical) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.gridy = row;
            c.weightx = 0;
            c.weighty = 0;
            c.insets = new Insets(5, 5, 5, 5);
            add(label, c);

            c.gridx = 1;
            c.weightx = 1;
            if (fillVertical) {
                c.weighty = 1;
                c.fill = GridBagConstraints.BOTH;
            } else {
                c.weighty = 0;
                c.fill = GridBagConstraints.HORIZONTAL;
            }
            add(component, c);

            row++;
        }

        public void addSeparator() {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = row;
            c.gridwidth = 2;
            c.weightx = 1;
            c.insets = new Insets(5, 5, 5, 5);
            c.fill = GridBagConstraints.HORIZONTAL;
            JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
            add(separator, c);
            row++;
        }

        public void addEmptyLine() {
            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = row;
            c.gridwidth = 2;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0;
            c.weightx = 0.1;
            JLabel emptyLabel = new JLabel();
            emptyLabel.setPreferredSize(new Dimension(0, 0));
            emptyLabel.setMinimumSize(new Dimension(0, 0));
            add(emptyLabel, c);
            row++;
        }
    }

    /**
     * Class for controlling which settings of text pattern depend on other
     * settings and how.
     */
    static class TextPatternCheckBoxGroup implements ItemListener {

        private JCheckBox matchCase;
        private JCheckBox wholeWords;
        private TextToFindTypeComboBox textToFindType;
        private JCheckBox preserveCase;
        private boolean lastPreserveCaseValue;
        private boolean lastWholeWordsValue;

        private TextPatternCheckBoxGroup(
                JCheckBox matchCase,
                JCheckBox wholeWords,
                TextToFindTypeComboBox textToFindType,
                JCheckBox preserveCase) {

            this.matchCase = matchCase;
            this.wholeWords = wholeWords;
            this.textToFindType = textToFindType;
            this.preserveCase = preserveCase;
        }

        private void initListeners() {
            this.matchCase.addItemListener(this);
            this.wholeWords.addItemListener(this);
            this.textToFindType.addItemListener(this);
            if (this.preserveCase != null) {
                this.preserveCase.addItemListener(this);
            }
        }

        private void matchCaseChanged() {
            updatePreserveCaseAllowed();
        }

        private void regexpChanged() {
            updateWholeWordsAllowed();
            updatePreserveCaseAllowed();
        }

        private void updateWholeWordsAllowed() {
            if (textToFindType.isRegexp() == wholeWords.isEnabled()) {
                if (textToFindType.isRegexp()) {
                    lastWholeWordsValue = wholeWords.isSelected();
                    wholeWords.setSelected(false);
                    wholeWords.setEnabled(false);
                } else {
                    wholeWords.setEnabled(true);
                    wholeWords.setSelected(lastWholeWordsValue);
                }
            }
        }

        private void updatePreserveCaseAllowed() {
            if (preserveCase == null) {
                return;
            }
            if (preserveCase.isEnabled()
                    == (textToFindType.isRegexp() || matchCase.isSelected())) {
                if (preserveCase.isEnabled()) {
                    lastPreserveCaseValue = preserveCase.isSelected();
                    preserveCase.setSelected(false);
                    preserveCase.setEnabled(false);
                } else {
                    preserveCase.setEnabled(true);
                    preserveCase.setSelected(lastPreserveCaseValue);
                }
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            ItemSelectable is = e.getItemSelectable();
            if (is == matchCase) {
                matchCaseChanged();
            } else if (is == textToFindType) {
                regexpChanged();
            }
        }

        static void bind(JCheckBox matchCase,
                JCheckBox wholeWords,
                TextToFindTypeComboBox regexp,
                JCheckBox preserveCase) {

            TextPatternCheckBoxGroup tpcbg = new TextPatternCheckBoxGroup(
                    matchCase, wholeWords, regexp, preserveCase);
            tpcbg.initListeners();
        }
    }

    private class ReplacementPatternListener extends PatternChangeListener {

        public ReplacementPatternListener() {
        }

        @Override
        public void handleComboBoxChange(String text) {
            searchCriteria.setReplaceExpr(text);
            if (cboxReplacement != null) {
                updateReplacePatternColor();
            }
        }
    }

    private static class ReplaceModelItem {

        private ReplacePattern replacePattern;

        public ReplaceModelItem(ReplacePattern replacePattern) {
            this.replacePattern = replacePattern;
        }

        public ReplacePattern getReplacePattern() {
            return replacePattern;
        }

        @Override
        public String toString() {
            return replacePattern.getReplaceExpression();
        }
    }

    private static class TextToFindTypeComboBox extends JComboBox<MatchType> {

        public TextToFindTypeComboBox() {
            super(new MatchType[]{MatchType.LITERAL,
                MatchType.BASIC, MatchType.REGEXP});
        }

        public boolean isRegexp() {
            return isSelected(MatchType.REGEXP);
        }

        public boolean isBasic() {
            return isSelected(MatchType.BASIC);
        }

        public boolean isLiteral() {
            return isSelected(MatchType.LITERAL);
        }

        public boolean isSelected(MatchType type) {
            return getSelectedItem() == type;
        }

        private MatchType getSelectedMatchType() {
            Object selected = getSelectedItem();
            if (selected instanceof MatchType) {
                return (MatchType) selected;
            } else {
                throw new IllegalStateException("MatchType expected");  //NOI18N
            }
        }
    }

    @NbBundle.Messages({
        "# {0} - Maximal number of characters in search field",
        "MSG_TextTooLong=The text cannot be pasted into search field."
                + " Limit is {0} characters."
    })
    private static final class LengthFilter extends DocumentFilter {

        private static final int LIMIT = Integer.getInteger(
                "nb.search.field.limit", 10000);

        @Override
        public void replace(FilterBypass fb, int offset, int length,
                String text, AttributeSet attrs) throws BadLocationException {
            int currentLength = fb.getDocument().getLength();
            int newTextLength = text == null ? 0 : text.length();
            int newLength = currentLength + newTextLength - length;
            if (newLength <= LIMIT) {
                super.replace(fb, offset, length, text, attrs);
            } else {
                limitReached();
            }
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string,
                AttributeSet attr) throws BadLocationException {
            int currentLength = fb.getDocument().getLength();
            int newLength = currentLength + string.length();
            if (newLength <= LIMIT) {
                super.insertString(fb, offset, string, attr);
            } else {
                limitReached();
            }
        }

        private void limitReached() {
            String msg = Bundle.MSG_TextTooLong(LIMIT);
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(
                            msg, NotifyDescriptor.WARNING_MESSAGE));
            Logger.getLogger(BasicSearchForm.class.getName())
                    .log(Level.INFO, msg);
        }
    }

    static final class MultiLineComboBoxEditor implements ComboBoxEditor {

        private final JTextArea area = new JTextArea();

        public MultiLineComboBoxEditor(JComboBox<?> reference) {
            area.setWrapStyleWord(false);
            area.setLineWrap(false);

            Border border = ((JComponent)reference.getEditor().getEditorComponent()).getBorder();
            
            if (border != null) {
                area.setBorder(border);
            }

            // retain standard focus traversal behavior
            area.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERS‌​AL_KEYS, null);
            area.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERS‌​AL_KEYS, null);

            // dispatch enter to parent; set line breaks on shift+enter
            area.getInputMap().put(KeyStroke.getKeyStroke("shift ENTER"), "insert-break");
            area.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "text-submit");
        }

        @Override
        public void setItem(Object item) {
            String text = Objects.toString(item, "");
            if (!text.equals(area.getText())) { // see BasicComboBoxEditor.setItem(item) or JDK-4530952
                area.setText(text);
            }
        }

        @Override
        public Object getItem() {
            return area.getText();
        }

        @Override
        public Component getEditorComponent() {
            return area;
        }

        @Override
        public void selectAll() {
            area.selectAll();
            area.requestFocus();
        }

        @Override
        public void addActionListener(ActionListener l) { }

        @Override
        public void removeActionListener(ActionListener l) { }

    }
}
