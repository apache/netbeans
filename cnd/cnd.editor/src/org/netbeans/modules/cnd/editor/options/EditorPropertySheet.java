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

package org.netbeans.modules.cnd.editor.options;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.PreviewPreferencesModel.Filter;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 */
public class EditorPropertySheet extends javax.swing.JPanel
        implements ActionListener, PropertyChangeListener, PreferenceChangeListener {

    private static final boolean TRACE = false;

    private final EditorOptionsPanelController topController;
    private boolean loaded = false;
    private final CodeStyle.Language language;
    private String lastChangedproperty;
    //private String defaultStyles;
    //private Map<String, PreviewPreferences> preferences = new HashMap<String, PreviewPreferences>();
    private PreviewPreferencesModel preferencesModel;
    private Filter filter;
    private PropertySheet holder;
    private boolean propertyChanged = false;
    private boolean stylesChanged = false;

    EditorPropertySheet(EditorOptionsPanelController topControler, CodeStyle.Language language, PreviewPreferencesModel preferencesModel, Filter filter) {
        this.topController = topControler;
        this.language = language;
        this.preferencesModel = preferencesModel;
        this.filter = filter;
        initComponents();

        overrideGlobalOptions.addActionListener(EditorPropertySheet.this);

        holder = new PropertySheet();
        holder.setOpaque(false);
        holder.setDescriptionAreaVisible(false);
        holder.setPreferredSize(new Dimension(250,150));
        GridBagConstraints fillConstraints = new GridBagConstraints();
        fillConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        fillConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fillConstraints.weightx = 1.0;
        fillConstraints.weighty = 1.0;
        categoryPanel.add(holder, fillConstraints);

        manageStyles.setMinimumSize(new Dimension(126,26));
        switch (filter) {
            case Alignment:
                setName(getString("Filter_Alignment_name")); // NOI18N
                break;
            case All:
                setName(getString("Filter_All_name")); // NOI18N
                break;
            case BlankLines:
                setName(getString("Filter_BlankLines_name")); // NOI18N
                break;
            case Braces:
                setName(getString("Filter_Braces_name")); // NOI18N
                break;
            case Spaces:
                setName(getString("Filter_Spaces_name")); // NOI18N
                break;
            case TabsAndIndents:
                setName(getString("Filter_TabsAndIndents_name")); // NOI18N
                break;
        }
        this.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getSource() == EditorPropertySheet.this) {
                    initLanguageCategory();
                }
            }
        });
    }

    private void initLanguageMap(){
        preferencesModel.initLanguageMap(language);
    }

    private void initLanguageCategory(){
        styleComboBox.removeActionListener(this);
        final Map<String, PreviewPreferences> prefences = preferencesModel.getPrefences(language);
        if (prefences == null) {
            return;
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        List<EntryWrapper> list = new ArrayList<EntryWrapper>();
        for(Map.Entry<String, PreviewPreferences> entry : prefences.entrySet()) {
            list.add(new EntryWrapper(entry));
        }
        Collections.sort(list);
        int index = 0;
        int i = 0;
        for(EntryWrapper entry : list) {
            if (entry.name.equals(preferencesModel.getLanguageDefaultStyle(language))) {
                index = i;
            }
            model.addElement(entry);
            i++;
        }
        styleComboBox.setModel(model);
        styleComboBox.setSelectedIndex(index);
        EntryWrapper entry = (EntryWrapper)styleComboBox.getSelectedItem();
        initSheets(entry.preferences);
        preferencesModel.setLanguageDefaultStyle(language, entry.name);
        styleComboBox.addActionListener(this);
        repaintPreview();
    }

    private PreviewPreferences lastSheetPreferences = null;

    private void initSheets(PreviewPreferences preferences){
        if (TRACE) {
            System.out.println("Set properties for "+preferences.getLanguage()+" "+preferences.getStyleId()); // NOI18N
        }
        if (lastSheetPreferences != null){
            lastSheetPreferences.removePreferenceChangeListener(this);
        }
        overrideGlobalOptions.setSelected(preferences.getBoolean(EditorOptions.overrideTabIndents, (Boolean)EditorOptions.getDefault(language, preferences.getStyleId(), EditorOptions.overrideTabIndents)));
        Sheet sheet = new Sheet();
        Sheet.Set set;
        if (filter == Filter.All || filter == Filter.TabsAndIndents) {
            overrideGlobalOptions.setVisible(true);
            set = new Sheet.Set();
            set.setName("Indents"); // NOI18N
            set.setDisplayName(getString("LBL_TabsAndIndents")); // NOI18N
            set.setShortDescription(getString("HINT_TabsAndIndents")); // NOI18N
            if (overrideGlobalOptions.isSelected()) {
                set.put(new IntNodeProp(language, preferences, EditorOptions.indentSize));
                set.put(new BooleanNodeProp(language, preferences, EditorOptions.expandTabToSpaces));
                set.put(new IntNodeProp(language, preferences, EditorOptions.tabSize));
            } else {
                set.put(new IntNodeProp(language, preferences, EditorOptions.indentSize, EditorOptions.getGlobalIndentSize()));
                set.put(new BooleanNodeProp(language, preferences, EditorOptions.expandTabToSpaces, EditorOptions.getGlobalExpandTabs()));
                set.put(new IntNodeProp(language, preferences, EditorOptions.tabSize, EditorOptions.getGlobalTabSize()));
            }
            set.put(new IntNodeProp(language, preferences, EditorOptions.statementContinuationIndent));
            set.put(new IntNodeProp(language, preferences, EditorOptions.constructorListContinuationIndent));
            set.put(new PreprocessorIndentProperty(language, preferences, EditorOptions.indentPreprocessorDirectives));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.sharpAtStartLine));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.indentNamespace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.indentCasesFromSwitch));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.absoluteLabelIndent));
            set.put(new VisibilityIndentProperty(language, preferences, EditorOptions.indentVisibility));
            set.put(new  BooleanNodeProp(language, preferences, EditorOptions.spaceKeepExtra));
            sheet.put(set);
        } else {
            overrideGlobalOptions.setVisible(false);
        }

        if (filter == Filter.All || filter == Filter.Braces) {
            set = new Sheet.Set();
            set.setName("BracesPlacement"); // NOI18N
            set.setDisplayName(getString("LBL_BracesPlacement")); // NOI18N
            set.setShortDescription(getString("HINT_BracesPlacement")); // NOI18N
            set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceNamespace));
            set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceClass));
            set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceDeclaration));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.ignoreEmptyFunctionBody));
            set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceLambda));
            set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBraceSwitch));
            set.put(new BracePlacementProperty(language, preferences, EditorOptions.newLineBeforeBrace));
            sheet.put(set);
        }

        if (filter == Filter.All || filter == Filter.Alignment) {
            set = new Sheet.Set();
            set.setName("MultilineAlignment"); // NOI18N
            set.setDisplayName(getString("LBL_MultilineAlignment")); // NOI18N
            set.setShortDescription(getString("HINT_MultilineAlignment")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineMethodParams));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineCallArgs));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineArrayInit));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineFor));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineIfCondition));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineWhileCondition));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.alignMultilineParen));
            sheet.put(set);
        }

        if (filter == Filter.All || filter == Filter.Braces) {
            set = new Sheet.Set();
            set.setName("NewLine"); // NOI18N
            set.setDisplayName(getString("LBL_NewLine")); // NOI18N
            set.setShortDescription(getString("HINT_NewLine")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineFunctionDefinitionName));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineCatch));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineElse));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.newLineWhile));
            sheet.put(set);
        }

        if (filter == Filter.All || filter == Filter.Spaces) {
            set = new Sheet.Set();
            set.setName("SpacesBeforeKeywords"); // NOI18N
            set.setDisplayName(getString("LBL_BeforeKeywords")); // NOI18N
            set.setShortDescription(getString("HINT_BeforeKeywords")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeCatch));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeElse));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeWhile));
            sheet.put(set);

            set = new Sheet.Set();
            set.setName("SpacesBeforeParentheses"); // NOI18N
            set.setDisplayName(getString("LBL_BeforeParentheses")); // NOI18N
            set.setShortDescription(getString("HINT_BeforeParentheses")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeMethodDeclParen));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeMethodCallParen));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeCatchParen));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeForParen));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeIfParen));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeSwitchParen));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeWhileParen));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeKeywordParen));
            sheet.put(set);

            set = new Sheet.Set();
            set.setName("SpacesAroundOperators"); // NOI18N
            set.setDisplayName(getString("LBL_AroundOperators")); // NOI18N
            set.setShortDescription(getString("HINT_AroundOperators")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundAssignOps));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundBinaryOps));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundTernaryOps));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAroundUnaryOps));
            sheet.put(set);

            set = new Sheet.Set();
            set.setName("SpacesBeforeLeftBracess"); // NOI18N
            set.setDisplayName(getString("LBL_BeforeLeftBraces")); // NOI18N
            set.setShortDescription(getString("HINT_BeforeLeftBraces")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeClassDeclLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeMethodDeclLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeLambdaLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeArrayInitLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeCatchLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeDoLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeElseLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeForLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeIfLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeSwitchLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeTryLeftBrace));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeWhileLeftBrace));
            sheet.put(set);

            set = new Sheet.Set();
            set.setName("SpacesWithinParentheses"); // NOI18N
            set.setDisplayName(getString("LBL_WithinParentheses")); // NOI18N
            set.setShortDescription(getString("HINT_WithinParentheses")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinMethodDeclParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinMethodCallParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinBraces));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinCatchParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinForParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinIfParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinSwitchParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinTypeCastParens));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceWithinWhileParens));
            sheet.put(set);

            set = new Sheet.Set();
            set.setName("SpacesOther"); // NOI18N
            set.setDisplayName(getString("LBL_Other_Spaces")); // NOI18N
            set.setShortDescription(getString("HINT_Other_Spaces")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeComma));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterComma));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeSemi));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterSemi));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceBeforeColon));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterColon));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterTypeCast));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.spaceAfterOperatorKeyword));
            sheet.put(set);
        }

        if (filter == Filter.All || filter == Filter.BlankLines) {
            set = new Sheet.Set();
            set.setName("BlankLines"); // NOI18N
            set.setDisplayName(getString("LBL_BlankLines")); // NOI18N
            set.setShortDescription(getString("HINT_BlankLines")); // NOI18N
            set.put(new IntNodeProp(language, preferences, EditorOptions.blankLinesBeforeClass));
            //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterClass));
            set.put(new IntNodeProp(language, preferences, EditorOptions.blankLinesAfterClassHeader));
            //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesBeforeFields));
            //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterFields));
            set.put(new IntNodeProp(language, preferences, EditorOptions.blankLinesBeforeMethods));
            //set.put(new IntNodeProp(currentLanguage, preferences, EditorOptions.blankLinesAfterMethods));
            sheet.put(set);
        }

        if (filter == Filter.All || filter == Filter.TabsAndIndents) {
            set = new Sheet.Set();
            set.setName("Other"); // NOI18N
            set.setDisplayName(getString("LBL_Other")); // NOI18N
            set.setShortDescription(getString("HINT_Other")); // NOI18N
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.addLeadingStarInComment));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.useBlockComment));
            set.put(new BooleanNodeProp(language, preferences, EditorOptions.useInlineKeyword));
            sheet.put(set);
        }

        final DummyNode[] dummyNodes = new DummyNode[1];
        dummyNodes[0] = new DummyNode(sheet, "Sheet"); // NOI18N
        holder.setNodes(dummyNodes);

        preferences.addPreferenceChangeListener(this);
        lastSheetPreferences = preferences;
    }

    void load() {
        loaded = false;
        initLanguageMap();
        initLanguageCategory();
        loaded = true;
        repaintPreview();
        propertyChanged = false;
        stylesChanged = false;
    }

    void store() {
        if (filter != Filter.All) {
            return;
        }
        EditorOptions.setCurrentProfileId(language, preferencesModel.getLanguageDefaultStyle(language));
        StringBuilder buf = new StringBuilder();
        for(Map.Entry<String, PreviewPreferences> prefEntry : preferencesModel.getLanguagePreferences(language).entrySet()){
            String style = prefEntry.getKey();
            if (buf.length() > 0){
                buf.append(',');
            }
            buf.append(style);
            PreviewPreferences pref = prefEntry.getValue();
            Preferences toSave = EditorOptions.getPreferences(language, style);
            if (style.equals(preferencesModel.getLanguageDefaultStyle(language))){
                EditorOptions.setPreferences(CodeStyle.getDefault(language, null), toSave);
            }
            for(String key : EditorOptions.keys()){
                Object o = EditorOptions.getDefault(language, style, key);
                if (o instanceof Boolean) {
                    Boolean v = pref.getBoolean(key, (Boolean) o);
                    if (!o.equals(v)) {
                        toSave.putBoolean(key, v);
                    } else {
                        toSave.remove(key);
                    }
                } else if (o instanceof Integer) {
                    Integer v = pref.getInt(key, (Integer) o);
                    if (!o.equals(v)) {
                        toSave.putInt(key, v);
                    } else {
                        toSave.remove(key);
                    }
                } else {
                    String v = pref.get(key, o.toString());
                    if (!o.equals(v)) {
                        toSave.put(key, v);
                    } else {
                        toSave.remove(key);
                    }
                }
            }
            if (style.equals(preferencesModel.getLanguageDefaultStyle(language))){
                EditorOptions.updateSimplePreferences(language, CodeStyle.getDefault(language, null));
            }
        }
        EditorOptions.setAllStyles(language, buf.toString());
        preferencesModel.clear(language);
        holder.setNodes(null);
        propertyChanged = false;
        stylesChanged = false;
    }

    void cancel() {
        holder.setNodes(null);
        if (filter != Filter.All) {
            return;
        }
        preferencesModel.clear(language);
        propertyChanged = false;
        stylesChanged = false;
    }

    // Change in the combo
    @Override
    public void actionPerformed(ActionEvent e) {
        lastChangedproperty = null;
        if (styleComboBox.equals(e.getSource())){
            EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
            if (category != null) {
                preferencesModel.setLanguageDefaultStyle(language, category.name);
                initSheets(category.preferences);
                repaintPreview();
            }
        } else if (overrideGlobalOptions.equals(e.getSource())) {
            EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
            if (category != null) {
                category.preferences.putBoolean(EditorOptions.overrideTabIndents, overrideGlobalOptions.isSelected());
                preferencesModel.setLanguageDefaultStyle(language, category.name);
                initSheets(category.preferences);
                repaintPreview();
            }
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        lastChangedproperty = evt.getKey();
        change();
    }


    // Change in some of the subpanels
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        change();
    }

    private void change(){
        if ( !loaded ) {
            return;
        }
        firePrefsChanged();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                // Notify the main controler that the page has changed
                topController.changed(stylesChanged || propertyChanged);
                // Repaint the preview
                repaintPreview();
            }
        };
        if(SwingUtilities.isEventDispatchThread()){
            run.run();
        } else {
            SwingUtilities.invokeLater(run);
        }
    }
    
    private void firePrefsChanged() {
        boolean isChanged = false;
        Map<String, PreviewPreferences> languagePreferences = preferencesModel.getLanguagePreferences(language);
        if (languagePreferences != null) {
            Set<String> keys = EditorOptions.keys();
            for (String style : languagePreferences.keySet()) {
                for(String key : keys) {
                    String currentValue = languagePreferences.get(style).get(key, null);
                    String savedValue = EditorOptions.getPreferences(language, style).get(key, null);
                    if(currentValue == null) {
                        isChanged |= savedValue != null;
                    } else {
                        isChanged |= savedValue == null ? !EditorOptions.getDefault(language, style, key).toString().equals(currentValue) : !savedValue.equals(currentValue);
                    }
                }
            }
        }
        propertyChanged = isChanged;
    }

    public void repaintPreview() {
        EntryWrapper category = (EntryWrapper)styleComboBox.getSelectedItem();
        if (category == null) {
            category = (EntryWrapper)styleComboBox.getItemAt(0);
        }
        JEditorPane previewPane = (JEditorPane) topController.getPreviewComponent();
        if (loaded) {
            PreviewPreferences p = new PreviewPreferences(category.preferences,
                    category.preferences.getLanguage(), category.preferences.getStyleId());
            p.makeAllKeys(category.preferences);
            p.putBoolean(EditorOptions.overrideTabIndents, overrideGlobalOptions.isSelected());
            if (!overrideGlobalOptions.isSelected()){
                p.putBoolean(EditorOptions.expandTabToSpaces, EditorOptions.getGlobalExpandTabs());
                p.putInt(EditorOptions.tabSize, EditorOptions.getGlobalTabSize());
                p.putInt(EditorOptions.indentSize, EditorOptions.getGlobalIndentSize());
           }
            p.putInt(SimpleValueNames.TAB_SIZE, p.getInt(EditorOptions.tabSize, EditorOptions.tabSizeDefault));
            p.putBoolean(SimpleValueNames.EXPAND_TABS, p.getBoolean(EditorOptions.expandTabToSpaces, EditorOptions.expandTabToSpacesDefault));
            p.putInt(SimpleValueNames.SPACES_PER_TAB, p.getInt(EditorOptions.indentSize, EditorOptions.indentSizeDefault));
            p.putInt(SimpleValueNames.INDENT_SHIFT_WIDTH, p.getInt(EditorOptions.indentSize, EditorOptions.indentSizeDefault));
            
            // Bug 248397 - C/C++ Header tab size and indentation doesn't work properly 
            if (true) {
                // Attempt to pass tabsize to editor. Use closed and forbidden SPI.
                // See org.netbeans.modules.editor.indent.spi.CodeStylePreferences.getPreferences()
                // and org.netbeans.modules.options.indentation.FormattingPanel.propertyChange()
                // It is a result of fixing bug 142146 - [65cat] Clean up the formatting settings customizers
                // The fix does not take into account CND code style specific. CND preferences is a bunch of code styles.
                Document doc = previewPane.getDocument();
                doc.putProperty("Tools-Options->Editor->Formatting->Preview - Preferences", p); //NOI18N
                doc.putProperty(SimpleValueNames.TAB_SIZE,  p.getInt(EditorOptions.tabSize, EditorOptions.tabSizeDefault));
            } else {
                // Alternative simple fix.
                // Do not use tabs because passing tabsize to editor is tricky.
                p.putBoolean(EditorOptions.expandTabToSpaces, true);
            }
            
            previewPane.setIgnoreRepaint(true);
            refreshPreview(previewPane, p);
            previewPane.setIgnoreRepaint(false);
            previewPane.scrollRectToVisible(new Rectangle(0,0,10,10) );
            previewPane.repaint(100);
        }
    }

    private String getPreviewText(){
        String suffix;
        switch (language){
            case C:
                suffix = "_c"; // NOI18N
                break;
            case HEADER:
                suffix = "_cpp"; // NOI18N
                break;
            case CPP:
            default:
                suffix = "_cpp"; // NOI18N
                break;
        }
        if (lastChangedproperty != null) {
            if (lastChangedproperty.startsWith("space")) { // NOI18N
                return loadPreviewExample("SAMPLE_Spaces"+suffix); // NOI18N
            } else if (lastChangedproperty.startsWith("blank")) { // NOI18N
                return loadPreviewExample("SAMPLE_BlankLines"+suffix); // NOI18N
            }  else if (lastChangedproperty.startsWith("align") || // NOI18N
                        lastChangedproperty.startsWith("new")) { // NOI18N
                return loadPreviewExample("SAMPLE_AlignBraces"+suffix); // NOI18N
            }
        }
        return loadPreviewExample("SAMPLE_TabsIndents"+suffix); // NOI18N
    }

    private String loadPreviewExample(String example) {
        return NbBundle.getMessage(EditorPropertySheet.class, example);
    }

    private void refreshPreview(JEditorPane pane, Preferences p) {
        pane.setText(getPreviewText());
        final BaseDocument bd = (BaseDocument) pane.getDocument();
        EntryWrapper entry = (EntryWrapper) styleComboBox.getSelectedItem();
        final CodeStyle codeStyle = EditorOptions.createCodeStyle(language, entry.name, p, false);
        bd.runAtomicAsUser(new Runnable() {

            @Override
            public void run() {
                try {
                    new Reformatter(bd, codeStyle).reformat();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private static String getString(String key) {
        return NbBundle.getMessage(EditorPropertySheet.class, key);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked") // NOI18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        styleComboBox = new javax.swing.JComboBox();
        categoryPanel = new javax.swing.JPanel();
        manageStyles = new javax.swing.JButton();
        overrideGlobalOptions = new javax.swing.JCheckBox();

        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(styleComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "LBL_Style_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(jLabel1, gridBagConstraints);

        styleComboBox.setMaximumSize(new java.awt.Dimension(100, 25));
        styleComboBox.setPreferredSize(new java.awt.Dimension(100, 25));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        add(styleComboBox, gridBagConstraints);

        categoryPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        categoryPanel.setOpaque(false);
        categoryPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 6);
        add(categoryPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manageStyles, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "EditorPropertySheet.manageStyles.text")); // NOI18N
        manageStyles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageStylesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(manageStyles, gridBagConstraints);

        overrideGlobalOptions.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(overrideGlobalOptions, org.openide.util.NbBundle.getMessage(EditorPropertySheet.class, "LBL_OverrideGlobalOptions")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 6);
        add(overrideGlobalOptions, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

private void manageStylesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageStylesActionPerformed
    Map<String,PreviewPreferences> clone =  preferencesModel.clonePreferences(language);
    ManageStylesPanel stylesPanel = new ManageStylesPanel(language, clone);
    DialogDescriptor dd = new DialogDescriptor(stylesPanel, getString("MANAGE_STYLES_DIALOG_TITLE")); // NOI18N
    DialogDisplayer.getDefault().notify(dd);
    if (dd.getValue() == DialogDescriptor.OK_OPTION) {
        preferencesModel.resetPreferences(language, clone);
        initLanguageCategory();
        //change();
        Set<String> saved = new HashSet<String>();
        saved.addAll(EditorOptions.getAllStyles(language));
        stylesChanged = !clone.keySet().equals(saved);
        topController.changed(stylesChanged || propertyChanged);
    }
}//GEN-LAST:event_manageStylesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel categoryPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton manageStyles;
    private javax.swing.JCheckBox overrideGlobalOptions;
    private javax.swing.JComboBox styleComboBox;
    // End of variables declaration//GEN-END:variables

    private static class EntryWrapper implements Comparable<EntryWrapper> {
        private final String name;
        private final String displayName;
        private final PreviewPreferences preferences;
        private EntryWrapper(Map.Entry<String, PreviewPreferences> enrty){
            this.name = enrty.getKey();
            this.preferences = enrty.getValue();
            displayName = EditorOptions.getStyleDisplayName(preferences.getLanguage(),name);
        }

        @Override
        public String toString() {
            return displayName;
        }

        @Override
        public int compareTo(EntryWrapper o) {
            return this.displayName.compareTo(o.displayName);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + (this.displayName != null ? this.displayName.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final EntryWrapper other = (EntryWrapper) obj;
            if ((this.displayName == null) ? (other.displayName != null) : !this.displayName.equals(other.displayName)) {
                return false;
            }
            return true;
        }
    }

    private static class DummyNode extends AbstractNode {
        public DummyNode(Sheet sheet, String name) {
            super(Children.LEAF);
            if (sheet != null) {
                setSheet(sheet);
            }
            setName(name);
        }
    }

}
