/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.search.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.search.SearchHistory;
import org.netbeans.api.search.SearchPattern;
import org.netbeans.api.search.SearchPattern.MatchType;
import org.netbeans.modules.search.FindDialogMemory;
import org.netbeans.modules.search.ui.PatternChangeListener;
import org.netbeans.modules.search.ui.ShorteningCellRenderer;
import org.netbeans.modules.search.ui.TextFieldFocusListener;
import org.netbeans.modules.search.ui.UiUtils;
import org.openide.util.Parameters;

/**
 * Controller for a combo box for selection and editing of search patterns.
 *
 * @author jhavlin
 * @since api.search/1.1
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class SearchPatternController
        extends ComponentController<JComboBox> {

    private JTextComponent textToFindEditor;

    /**
     * Options of search patterns.
     */
    public enum Option {
        MATCH_CASE, WHOLE_WORDS, REGULAR_EXPRESSION
    }

    private final Map<Option, AbstractButton> bindings =
            new EnumMap<>(Option.class);
    private final Map<Option, Boolean> options =
            new EnumMap<>(Option.class);
    private JComboBox matchTypeComboBox = null;
    private MatchType matchType = MatchType.LITERAL;
    private final ItemListener listener;
    private boolean valid;
    private Color defaultTextColor = null;

    SearchPatternController(final JComboBox component) {
        super(component);
        component.setEditable(true);
        Component cboxEditorComp = component.getEditor().getEditorComponent();
        component.setRenderer(new ShorteningCellRenderer());
        textToFindEditor = (JTextComponent) cboxEditorComp;
        textToFindEditor.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        patternChanged();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        patternChanged();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        patternChanged();
                    }
                });
        initHistory();
        valid = checkValid();
        updateTextPatternColor();
        textToFindEditor.addFocusListener(new TextFieldFocusListener());
        textToFindEditor.getDocument().addDocumentListener(
                new TextToFindChangeListener());

        component.addItemListener((ItemEvent e) -> {
            Object si = component.getSelectedItem();
            if (si instanceof ModelItem) {
                SearchPattern sp = ((ModelItem) si).sp;
                for (Map.Entry<Option, AbstractButton> be :
                        bindings.entrySet()) {
                    switch (be.getKey()) {
                        case MATCH_CASE:
                            be.getValue().setSelected(sp.isMatchCase());
                            break;
                        case WHOLE_WORDS:
                            be.getValue().setSelected(sp.isWholeWords());
                            break;
                        case REGULAR_EXPRESSION:
                            be.getValue().setSelected(sp.isRegExp());
                            break;
                    }
                }
                if (matchTypeComboBox != null) {
                    matchTypeComboBox.setSelectedItem(sp.getMatchType());
                    // set only to match type that is supported by the combo
                    matchType =
                            (MatchType) matchTypeComboBox.getSelectedItem();
                } else {
                    matchType = sp.getMatchType();
                }
                options.put(Option.MATCH_CASE, sp.isMatchCase());
                options.put(Option.WHOLE_WORDS, sp.isWholeWords());
                options.put(Option.REGULAR_EXPRESSION, sp.isRegExp());
            }
        });

        listener = (ItemEvent e) -> {
            for (Map.Entry<Option, AbstractButton> entry :
                    bindings.entrySet()) {
                if (entry.getValue() == e.getSource()) {
                    setOption(entry.getKey(),
                            e.getStateChange() == ItemEvent.SELECTED);
                    break;
                }
            }
        };
    }

    private void initHistory() {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        final List<SearchPattern> data =
                SearchHistory.getDefault().getSearchPatterns();

        for (SearchPattern sp : data) {
            model.addElement(new ModelItem(sp));
        }

        component.setModel(model);

        if (data.size() > 0) {
            setSearchPattern(data.get(0));
        }
        if (!FindDialogMemory.getDefault().isTextPatternSpecified()) {
            component.setSelectedItem("");                              //NOI18N
        }
    }

    /**
     * Get text currently shown in the combo box editor.
     */
    private @NonNull String getText() {
        String s = textToFindEditor.getText();
        return s == null ? "" : s;                                      //NOI18N
    }

    /**
     * Set text to show in combo box editor
     *
     * @param text Text to show in the component editor.
     */
    private void setText(@NullAllowed String text) {
        component.setSelectedItem(text == null ? "" : text);            //NOI18N
    }

    /**
     * Get current value of an option of the search pattern.
     */
    private boolean getOption(@NonNull Option option) {
        Parameters.notNull("option", option);                           //NOI18N
        return Boolean.TRUE.equals(options.get(option));
    }

    /**
     * Set value of a search pattern option. Bound abstract button will be
     * selected or deselected accordingly.
     */
    private void setOption(@NonNull Option option, boolean value) {
        Parameters.notNull("option", option);                           //NOI18N
        options.put(option, value);
        AbstractButton button = bindings.get(option);
        if (button != null) {
            button.setSelected(value);
        }
        if (option == Option.REGULAR_EXPRESSION) {
            if ((matchType == MatchType.REGEXP) != value) {
                setMatchType(value ? MatchType.REGEXP : MatchType.LITERAL);
            }
            updateValidity();
        }
        fireChange();
    }

    /**
     * Set value of a search pattern option. The correct item in corresponding
     * combo box will be selected accordingly.
     */
    private void setMatchType(MatchType newMatchType) {
        Parameters.notNull("matchType", matchType);                     //NOI18N
        if (matchTypeComboBox != null) {
            // use only match types contained in the combo box
            if (matchTypeComboBox.getSelectedItem() != newMatchType)
            matchTypeComboBox.setSelectedItem(newMatchType);
            matchType = (MatchType) matchTypeComboBox.getSelectedItem();
        } else {
            matchType = newMatchType;
        }
        if (matchTypeComboBox != null
                && matchTypeComboBox.getSelectedItem() != matchType) {
            matchTypeComboBox.setSelectedItem(matchType);
        }
        if (getOption(Option.REGULAR_EXPRESSION)
                != (MatchType.REGEXP == matchType)) {
            setOption(Option.REGULAR_EXPRESSION, matchType == MatchType.REGEXP);
        }
        updateValidity();
        fireChange();
    }

    /**
     * Get search pattern currently represented by this controller.
     */
    public @NonNull SearchPattern getSearchPattern() {
        return SearchPattern.create(getText(),
                getOption(Option.WHOLE_WORDS),
                getOption(Option.MATCH_CASE),
                matchType);
    }

    /**
     * Set text and options to represent a search pattern.
     */
    public void setSearchPattern(@NonNull SearchPattern searchPattern) {
        Parameters.notNull("searchPattern", searchPattern);             //NOI18N
        setText(searchPattern.getSearchExpression());
        setOption(Option.WHOLE_WORDS, searchPattern.isWholeWords());
        setOption(Option.MATCH_CASE, searchPattern.isMatchCase());
        setMatchType(searchPattern.getMatchType());
    }

    /**
     * Bind an abstract button (usually checkbox) to a SearchPattern option.
     *
     * @param option Option whose value the button should represent.
     * @param button Button to control and display the option.
     */
    public void bind(@NonNull final Option option,
            @NonNull final AbstractButton button) {
        Parameters.notNull("option", option);                           //NOI18N
        Parameters.notNull("button", button);                           //NOI18N

        if (bindings.containsKey(option)) {
            throw new IllegalStateException(
                    "Already binded with option " + option);           // NOI18N
        }

        bindings.put(option, button);
        button.setSelected(getOption(option));
        button.addItemListener((ItemEvent e) -> setOption(option, button.isSelected()));
    }

    /**
     * Bind Match Type option to a combo box.
     *
     * @param comboBox Combo box to control and display the match type. The
     * model of the combo box can contain only items of type {@link MatchType}.
     * {@link MatchType#LITERAL} and {@link MatchType#REGEXP} are mandatory in
     * the model.
     *
     * @since api.search/1.11
     */
    public void bindMatchTypeComboBox(@NonNull final JComboBox comboBox) {
        Parameters.notNull("comboBox", comboBox);                       //NOI18N

        boolean regexpFound = false, literalFound = false;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            if (comboBox.getItemAt(i) == MatchType.LITERAL) {
                literalFound = true;
            } else if (comboBox.getItemAt(i) == MatchType.REGEXP) {
                regexpFound = true;
            } else if (!(comboBox.getItemAt(i) instanceof MatchType)) {
                throw new IllegalArgumentException("Model of the combo "//NOI18N
                        + "box can contain only MatchType items");      //NOI18N
            }
        }
        if (!(regexpFound && literalFound)) {
            throw new IllegalArgumentException(
                    "At least MatchType.LITERAL and MatchType.REGEXP " //NOI18N
                    + "must be contained in the combo box model.");     //NOI18N
        }
        if (matchTypeComboBox != null) {
            throw new IllegalStateException(
                    "Already bound with option MATCH_TYPE");            //NOI18N
        }
        this.matchTypeComboBox = comboBox;
        comboBox.setEditable(false);
        setMatchType(this.matchType); //update match type, check it is supported
        comboBox.setSelectedItem(matchType);
        comboBox.addItemListener((ItemEvent e) -> setMatchType((MatchType) comboBox.getSelectedItem()));
    }

    /**
     * Unbind a button from a SearchPattern option.
     */
    public void unbind(@NonNull Option option, @NonNull AbstractButton button) {
        Parameters.notNull("option", option);                           //NOI18N
        Parameters.notNull("button", button);                           //NOI18N
        bindings.remove(option);
        button.removeItemListener(listener);
    }

    private class TextToFindChangeListener extends PatternChangeListener {

        @Override
        public void handleComboBoxChange(String text) {
            patternChanged();
        }
    }

    private void patternChanged() {
        updateValidity();
        fireChange();
    }

    private void updateValidity() {
        boolean wasValid = valid;
        valid = checkValid();
        if (valid != wasValid) {
            updateTextPatternColor();
        }
    }

    private boolean checkValid() {
        String expr = getText();
        if (!getOption(Option.REGULAR_EXPRESSION) || expr == null) {
            return true;
        } else {
            try {
                Pattern p = Pattern.compile(getText());
                return true;
            } catch (PatternSyntaxException p) {
                return false;
            }
        }
    }

    /**
     * Sets proper color of text pattern.
     */
    private void updateTextPatternColor() {
        Color dfltColor = getDefaultTextColor(); // need to be here to init
        textToFindEditor.setForeground(
                valid ? dfltColor : UiUtils.getErrorTextColor());
    }

    private Color getDefaultTextColor() {
        if (defaultTextColor == null) {
            defaultTextColor = component.getForeground();
        }
        return defaultTextColor;
    }

    private static class ModelItem {

        final SearchPattern sp;

        public ModelItem(SearchPattern sp) {
            this.sp = sp;
        }

        @Override
        public String toString() {
            return sp.getSearchExpression();
        }
    }
}
