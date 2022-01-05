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

package org.netbeans.modules.php.editor.options;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
@org.netbeans.api.annotations.common.SuppressWarnings({"SE_BAD_FIELD_STORE"})
public class CodeCompletionPanel extends JPanel {
    private static final long serialVersionUID = -24730122182427272L;
    private final Map<String, Object> id2Saved = new HashMap<>();

    public static enum CodeCompletionType {
        SMART,
        FULLY_QUALIFIED,
        UNQUALIFIED;

        public static CodeCompletionType resolve(String value) {
            if (value != null) {
                try {
                    return valueOf(value);
                } catch (IllegalArgumentException ex) {
                    // ignored
                }
            }
            return SMART;
        }
    }

    public static enum VariablesScope {
        ALL,
        CURRENT_FILE;

        public static VariablesScope resolve(String value) {
            if (value != null) {
                try {
                    return valueOf(value);
                } catch (IllegalArgumentException ex) {
                    // ignored
                }
            }
            return ALL;
        }
    }

    static final String PHP_AUTO_COMPLETION_FULL = "phpAutoCompletionFull"; // NOI18N
    static final String PHP_AUTO_COMPLETION_VARIABLES = "phpAutoCompletionVariables"; // NOI18N
    static final String PHP_AUTO_COMPLETION_TYPES = "phpAutoCompletionTypes"; // NOI18N
    static final String PHP_AUTO_COMPLETION_NAMESPACES = "phpAutoCompletionNamespaces"; // NOI18N
    static final String PHP_CODE_COMPLETION_STATIC_METHODS = "phpCodeCompletionStaticMethods"; // NOI18N
    static final String PHP_CODE_COMPLETION_NON_STATIC_METHODS = "phpCodeCompletionNonStaticMethods"; // NOI18N
    static final String PHP_CODE_COMPLETION_VARIABLES_SCOPE = "phpCodeCompletionVariablesScope"; // NOI18N
    static final String PHP_CODE_COMPLETION_TYPE = "phpCodeCompletionType"; // NOI18N
    static final String PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING = "phpCodeCompletionSmartParametersPreFilling"; //NOI18N
    static final String PHP_AUTO_COMPLETION_SMART_QUOTES = "phpCodeCompletionSmartQuotes"; //NOI18N
    static final String PHP_AUTO_STRING_CONCATINATION = "phpCodeCompletionStringAutoConcatination"; //NOI18N
    static final String PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL = "phpAutoCompletionUseLowercaseTrueFalseNull"; //NOI18N
    static final String PHP_AUTO_COMPLETION_COMMENT_ASTERISK = "phpAutoCompletionCommentAsterisk"; // NOI18N

    // default values
    static final boolean PHP_AUTO_COMPLETION_FULL_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_VARIABLES_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_TYPES_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_NAMESPACES_DEFAULT = true;
    static final boolean PHP_CODE_COMPLETION_STATIC_METHODS_DEFAULT = true;
    static final boolean PHP_CODE_COMPLETION_NON_STATIC_METHODS_DEFAULT = false;
    static final boolean PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_SMART_QUOTES_DEFAULT = true;
    static final boolean PHP_AUTO_STRING_CONCATINATION_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL_DEFAULT = true;
    static final boolean PHP_AUTO_COMPLETION_COMMENT_ASTERISK_DEFAULT = true;

    private final Preferences preferences;
    private final ItemListener defaultCheckBoxListener = new DefaultCheckBoxListener();
    private final ItemListener defaultRadioButtonListener = new DefaultRadioButtonListener();

    public CodeCompletionPanel(Preferences preferences) {
        assert preferences != null;

        this.preferences = preferences;

        initComponents();

        initAutoCompletion();
        initCodeCompletionForMethods();
        initCodeCompletionForVariables();
        initCodeCompletionType();
        id2Saved.put(PHP_AUTO_COMPLETION_FULL, autoCompletionFullRadioButton.isSelected());
        id2Saved.put(PHP_AUTO_COMPLETION_VARIABLES, autoCompletionVariablesCheckBox.isSelected());
        id2Saved.put(PHP_AUTO_COMPLETION_TYPES, autoCompletionTypesCheckBox.isSelected());
        id2Saved.put(PHP_AUTO_COMPLETION_NAMESPACES, autoCompletionNamespacesCheckBox.isSelected());
        id2Saved.put(PHP_CODE_COMPLETION_STATIC_METHODS, codeCompletionStaticMethodsCheckBox.isSelected());
        id2Saved.put(PHP_CODE_COMPLETION_NON_STATIC_METHODS, codeCompletionNonStaticMethodsCheckBox.isSelected());
        VariablesScope variablesScope = VariablesScope.resolve(preferences.get(PHP_CODE_COMPLETION_VARIABLES_SCOPE, null));
        id2Saved.put(PHP_CODE_COMPLETION_VARIABLES_SCOPE, variablesScope == null ? null : variablesScope.name());
        CodeCompletionType type = CodeCompletionType.resolve(preferences.get(PHP_CODE_COMPLETION_TYPE, null));
        id2Saved.put(PHP_CODE_COMPLETION_TYPE, type == null ? null : type.name());
        id2Saved.put(PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING, codeCompletionSmartParametersPreFillingCheckBox.isSelected());
        id2Saved.put(PHP_AUTO_COMPLETION_SMART_QUOTES, autoCompletionSmartQuotesCheckBox.isSelected());
        id2Saved.put(PHP_AUTO_STRING_CONCATINATION, autoStringConcatenationCheckBox.isSelected());
        id2Saved.put(PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL, trueFalseNullCheckBox.isSelected());
        id2Saved.put(PHP_AUTO_COMPLETION_COMMENT_ASTERISK, autoCompletionCommentAsteriskCheckBox.isSelected());
    }

    public static PreferencesCustomizer.Factory getCustomizerFactory() {
        return new PreferencesCustomizer.Factory() {

            @Override
            public PreferencesCustomizer create(Preferences preferences) {
                return new CodeCompletionPreferencesCustomizer(preferences);
            }
        };
    }

    private void initAutoCompletion() {
        // full
        autoCompletionFullRadioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setAutoCompletionState(false);
                }
            }
        });
        autoCompletionCustomizeRadioButton.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setAutoCompletionState(true);
                }
            }
        });
        boolean autoCompletionFull = preferences.getBoolean(
                PHP_AUTO_COMPLETION_FULL,
                PHP_AUTO_COMPLETION_FULL_DEFAULT);
        if (autoCompletionFull) {
            autoCompletionFullRadioButton.setSelected(true);
        } else {
            autoCompletionCustomizeRadioButton.setSelected(true);
        }
        autoCompletionFullRadioButton.addItemListener(defaultRadioButtonListener);
        autoCompletionCustomizeRadioButton.addItemListener(defaultRadioButtonListener);

        // specific
        boolean autoCompletionVariables = preferences.getBoolean(
                PHP_AUTO_COMPLETION_VARIABLES,
                PHP_AUTO_COMPLETION_VARIABLES_DEFAULT);
        autoCompletionVariablesCheckBox.setSelected(autoCompletionVariables);
        autoCompletionVariablesCheckBox.addItemListener(defaultCheckBoxListener);

        boolean autoCompletionTypes = preferences.getBoolean(
                PHP_AUTO_COMPLETION_TYPES,
                PHP_AUTO_COMPLETION_TYPES_DEFAULT);
        autoCompletionTypesCheckBox.setSelected(autoCompletionTypes);
        autoCompletionTypesCheckBox.addItemListener(defaultCheckBoxListener);

        boolean autoCompletionNamespaces = preferences.getBoolean(
                PHP_AUTO_COMPLETION_NAMESPACES,
                PHP_AUTO_COMPLETION_NAMESPACES_DEFAULT);
        autoCompletionNamespacesCheckBox.setSelected(autoCompletionNamespaces);
        autoCompletionNamespacesCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionSmartQuotes = preferences.getBoolean(
                PHP_AUTO_COMPLETION_SMART_QUOTES,
                PHP_AUTO_COMPLETION_SMART_QUOTES_DEFAULT);
        autoCompletionSmartQuotesCheckBox.setSelected(codeCompletionSmartQuotes);
        autoCompletionSmartQuotesCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionStringAutoConcatination = preferences.getBoolean(
                PHP_AUTO_STRING_CONCATINATION,
                PHP_AUTO_STRING_CONCATINATION_DEFAULT);
        autoStringConcatenationCheckBox.setSelected(codeCompletionStringAutoConcatination);
        autoStringConcatenationCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionUseLowercaseTrueFalseNull = preferences.getBoolean(
                PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL,
                PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL_DEFAULT);
        trueFalseNullCheckBox.setSelected(codeCompletionUseLowercaseTrueFalseNull);
        trueFalseNullCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionCommentAsterisk = preferences.getBoolean(
                PHP_AUTO_COMPLETION_COMMENT_ASTERISK,
                PHP_AUTO_COMPLETION_COMMENT_ASTERISK_DEFAULT);
        autoCompletionCommentAsteriskCheckBox.setSelected(codeCompletionCommentAsterisk);
        autoCompletionCommentAsteriskCheckBox.addItemListener(defaultCheckBoxListener);
    }

    private void initCodeCompletionForMethods() {
        boolean codeCompletionStaticMethods = preferences.getBoolean(
                PHP_CODE_COMPLETION_STATIC_METHODS,
                PHP_CODE_COMPLETION_STATIC_METHODS_DEFAULT);
        codeCompletionStaticMethodsCheckBox.setSelected(codeCompletionStaticMethods);
        codeCompletionStaticMethodsCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionNonStaticMethods = preferences.getBoolean(
                PHP_CODE_COMPLETION_NON_STATIC_METHODS,
                PHP_CODE_COMPLETION_NON_STATIC_METHODS_DEFAULT);
        codeCompletionNonStaticMethodsCheckBox.setSelected(codeCompletionNonStaticMethods);
        codeCompletionNonStaticMethodsCheckBox.addItemListener(defaultCheckBoxListener);

        boolean codeCompletionSmartParametersPreFilling = preferences.getBoolean(
                PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING,
                PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING_DEFAULT);
        codeCompletionSmartParametersPreFillingCheckBox.setSelected(codeCompletionSmartParametersPreFilling);
        codeCompletionSmartParametersPreFillingCheckBox.addItemListener(defaultCheckBoxListener);
    }

    private void initCodeCompletionForVariables() {
        VariablesScope variablesScope = VariablesScope.resolve(preferences.get(PHP_CODE_COMPLETION_VARIABLES_SCOPE, null));
        switch (variablesScope) {
            case ALL:
                allVariablesRadioButton.setSelected(true);
                break;
            case CURRENT_FILE:
                currentFileVariablesRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown variables scope: " + variablesScope);
        }
        allVariablesRadioButton.addItemListener(defaultRadioButtonListener);
        currentFileVariablesRadioButton.addItemListener(defaultRadioButtonListener);
    }

    private void initCodeCompletionType() {
        CodeCompletionType type = CodeCompletionType.resolve(preferences.get(PHP_CODE_COMPLETION_TYPE, null));
        switch (type) {
            case SMART:
                smartRadioButton.setSelected(true);
                break;
            case FULLY_QUALIFIED:
                fullyQualifiedRadioButton.setSelected(true);
                break;
            case UNQUALIFIED:
                unqualifiedRadioButton.setSelected(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown code completion type: " + type);
        }
        smartRadioButton.addItemListener(defaultRadioButtonListener);
        fullyQualifiedRadioButton.addItemListener(defaultRadioButtonListener);
        unqualifiedRadioButton.addItemListener(defaultRadioButtonListener);
    }

    void validateData() {
        preferences.putBoolean(PHP_AUTO_COMPLETION_FULL, autoCompletionFullRadioButton.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_VARIABLES, autoCompletionVariablesCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_TYPES, autoCompletionTypesCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_NAMESPACES, autoCompletionNamespacesCheckBox.isSelected());

        preferences.putBoolean(PHP_CODE_COMPLETION_STATIC_METHODS, codeCompletionStaticMethodsCheckBox.isSelected());
        preferences.putBoolean(PHP_CODE_COMPLETION_NON_STATIC_METHODS, codeCompletionNonStaticMethodsCheckBox.isSelected());
        preferences.putBoolean(PHP_CODE_COMPLETION_SMART_PARAMETERS_PRE_FILLING, codeCompletionSmartParametersPreFillingCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_SMART_QUOTES, autoCompletionSmartQuotesCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_STRING_CONCATINATION, autoStringConcatenationCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_USE_LOWERCASE_TRUE_FALSE_NULL, trueFalseNullCheckBox.isSelected());
        preferences.putBoolean(PHP_AUTO_COMPLETION_COMMENT_ASTERISK, autoCompletionCommentAsteriskCheckBox.isSelected());

        VariablesScope variablesScope = null;
        if (allVariablesRadioButton.isSelected()) {
            variablesScope = VariablesScope.ALL;
        } else if (currentFileVariablesRadioButton.isSelected()) {
            variablesScope = VariablesScope.CURRENT_FILE;
        }
        assert variablesScope != null;
        preferences.put(PHP_CODE_COMPLETION_VARIABLES_SCOPE, variablesScope.name());

        CodeCompletionType type = null;
        if (smartRadioButton.isSelected()) {
            type = CodeCompletionType.SMART;
        } else if (fullyQualifiedRadioButton.isSelected()) {
            type = CodeCompletionType.FULLY_QUALIFIED;
        } else if (unqualifiedRadioButton.isSelected()) {
            type = CodeCompletionType.UNQUALIFIED;
        }
        assert type != null;
        preferences.put(PHP_CODE_COMPLETION_TYPE, type.name());
    }

    void setAutoCompletionState(boolean enabled) {
        autoCompletionVariablesCheckBox.setEnabled(enabled);
        autoCompletionTypesCheckBox.setEnabled(enabled);
        autoCompletionNamespacesCheckBox.setEnabled(enabled);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        codeCompletionTypeButtonGroup = new ButtonGroup();
        codeCompletionVariablesScopeButtonGroup = new ButtonGroup();
        autoCompletionButtonGroup = new ButtonGroup();
        enableAutocompletionLabel = new JLabel();
        autoCompletionFullRadioButton = new JRadioButton();
        autoCompletionCustomizeRadioButton = new JRadioButton();
        autoCompletionVariablesCheckBox = new JCheckBox();
        autoCompletionTypesCheckBox = new JCheckBox();
        autoCompletionNamespacesCheckBox = new JCheckBox();
        methodCodeCompletionLabel = new JLabel();
        codeCompletionStaticMethodsCheckBox = new JCheckBox();
        codeCompletionNonStaticMethodsCheckBox = new JCheckBox();
        codeCompletionVariablesScopeLabel = new JLabel();
        allVariablesRadioButton = new JRadioButton();
        currentFileVariablesRadioButton = new JRadioButton();
        codeCompletionTypeLabel = new JLabel();
        smartRadioButton = new JRadioButton();
        smartInfoLabel = new JLabel();
        fullyQualifiedRadioButton = new JRadioButton();
        fullyQualifiedInfoLabel = new JLabel();
        unqualifiedRadioButton = new JRadioButton();
        unqualifiedInfoLabel = new JLabel();
        codeCompletionSmartParametersPreFillingCheckBox = new JCheckBox();
        autoCompletionSmartQuotesLabel = new JLabel();
        autoCompletionSmartQuotesCheckBox = new JCheckBox();
        autoStringConcatenationCheckBox = new JCheckBox();
        useLowercaseLabel = new JLabel();
        trueFalseNullCheckBox = new JCheckBox();
        autoCompletionCommentAsteriskLabel = new JLabel();
        autoCompletionCommentAsteriskCheckBox = new JCheckBox();

        enableAutocompletionLabel.setLabelFor(autoCompletionFullRadioButton);
        Mnemonics.setLocalizedText(enableAutocompletionLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.enableAutocompletionLabel.text")); // NOI18N

        autoCompletionButtonGroup.add(autoCompletionFullRadioButton);
        Mnemonics.setLocalizedText(autoCompletionFullRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionFullRadioButton.text")); // NOI18N

        autoCompletionButtonGroup.add(autoCompletionCustomizeRadioButton);
        Mnemonics.setLocalizedText(autoCompletionCustomizeRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionCustomizeRadioButton.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionVariablesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionVariablesCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionTypesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTypesCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionNamespacesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionNamespacesCheckBox.text")); // NOI18N

        methodCodeCompletionLabel.setLabelFor(codeCompletionStaticMethodsCheckBox);
        Mnemonics.setLocalizedText(methodCodeCompletionLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.methodCodeCompletionLabel.text")); // NOI18N

        codeCompletionStaticMethodsCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(codeCompletionStaticMethodsCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionStaticMethodsCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(codeCompletionNonStaticMethodsCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionNonStaticMethodsCheckBox.text")); // NOI18N

        codeCompletionVariablesScopeLabel.setLabelFor(allVariablesRadioButton);
        Mnemonics.setLocalizedText(codeCompletionVariablesScopeLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionVariablesScopeLabel.text")); // NOI18N

        codeCompletionVariablesScopeButtonGroup.add(allVariablesRadioButton);
        Mnemonics.setLocalizedText(allVariablesRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.allVariablesRadioButton.text")); // NOI18N

        codeCompletionVariablesScopeButtonGroup.add(currentFileVariablesRadioButton);
        Mnemonics.setLocalizedText(currentFileVariablesRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.currentFileVariablesRadioButton.text")); // NOI18N

        codeCompletionTypeLabel.setLabelFor(smartRadioButton);
        Mnemonics.setLocalizedText(codeCompletionTypeLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionTypeLabel.text")); // NOI18N

        codeCompletionTypeButtonGroup.add(smartRadioButton);
        Mnemonics.setLocalizedText(smartRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartRadioButton.text")); // NOI18N

        smartInfoLabel.setLabelFor(smartRadioButton);
        Mnemonics.setLocalizedText(smartInfoLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartInfoLabel.text")); // NOI18N

        codeCompletionTypeButtonGroup.add(fullyQualifiedRadioButton);
        Mnemonics.setLocalizedText(fullyQualifiedRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedRadioButton.text")); // NOI18N

        fullyQualifiedInfoLabel.setLabelFor(fullyQualifiedRadioButton);
        Mnemonics.setLocalizedText(fullyQualifiedInfoLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedInfoLabel.text")); // NOI18N

        codeCompletionTypeButtonGroup.add(unqualifiedRadioButton);
        Mnemonics.setLocalizedText(unqualifiedRadioButton, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedRadioButton.text")); // NOI18N

        unqualifiedInfoLabel.setLabelFor(unqualifiedRadioButton);
        Mnemonics.setLocalizedText(unqualifiedInfoLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedInfoLabel.text")); // NOI18N

        codeCompletionSmartParametersPreFillingCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(codeCompletionSmartParametersPreFillingCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionSmartParametersPreFillingCheckBox.text")); // NOI18N

        autoCompletionSmartQuotesLabel.setLabelFor(autoCompletionSmartQuotesCheckBox);
        Mnemonics.setLocalizedText(autoCompletionSmartQuotesLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionSmartQuotesLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionSmartQuotesCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionSmartQuotesCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(autoStringConcatenationCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoStringConcatenationCheckBox.text")); // NOI18N

        useLowercaseLabel.setLabelFor(trueFalseNullCheckBox);
        Mnemonics.setLocalizedText(useLowercaseLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.useLowercaseLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(trueFalseNullCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.trueFalseNullCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionCommentAsteriskLabel, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionCommentAsteriskLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(autoCompletionCommentAsteriskCheckBox, NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionCommentAsteriskCheckBox.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(autoCompletionCustomizeRadioButton)
                            .addComponent(autoCompletionFullRadioButton)
                            .addComponent(methodCodeCompletionLabel)
                            .addComponent(codeCompletionNonStaticMethodsCheckBox)
                            .addComponent(codeCompletionStaticMethodsCheckBox)
                            .addComponent(enableAutocompletionLabel)
                            .addComponent(currentFileVariablesRadioButton)
                            .addComponent(allVariablesRadioButton)
                            .addComponent(codeCompletionVariablesScopeLabel)
                            .addComponent(codeCompletionSmartParametersPreFillingCheckBox)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(autoCompletionTypesCheckBox)
                                    .addComponent(autoCompletionVariablesCheckBox)
                                    .addComponent(autoCompletionNamespacesCheckBox)))
                            .addComponent(autoCompletionSmartQuotesLabel)
                            .addComponent(autoCompletionSmartQuotesCheckBox)
                            .addComponent(autoStringConcatenationCheckBox)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(codeCompletionTypeLabel)
                            .addComponent(smartRadioButton)
                            .addComponent(fullyQualifiedRadioButton)
                            .addComponent(unqualifiedRadioButton)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                    .addComponent(smartInfoLabel)
                                    .addComponent(fullyQualifiedInfoLabel)
                                    .addComponent(unqualifiedInfoLabel)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(useLowercaseLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(trueFalseNullCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(autoCompletionCommentAsteriskLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(autoCompletionCommentAsteriskCheckBox)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enableAutocompletionLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionFullRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionCustomizeRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionVariablesCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionTypesCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionNamespacesCheckBox)
                .addGap(18, 18, 18)
                .addComponent(methodCodeCompletionLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(codeCompletionStaticMethodsCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(codeCompletionNonStaticMethodsCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(codeCompletionSmartParametersPreFillingCheckBox)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(codeCompletionVariablesScopeLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(allVariablesRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(currentFileVariablesRadioButton)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(codeCompletionTypeLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(smartRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(smartInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(fullyQualifiedRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(fullyQualifiedInfoLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(unqualifiedRadioButton)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(unqualifiedInfoLabel)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(autoCompletionSmartQuotesLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionSmartQuotesCheckBox)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(autoStringConcatenationCheckBox)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(useLowercaseLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(trueFalseNullCheckBox)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(autoCompletionCommentAsteriskLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(autoCompletionCommentAsteriskCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        enableAutocompletionLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.enableAutocompletionLabel.AccessibleContext.accessibleName")); // NOI18N
        enableAutocompletionLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.enableAutocompletionLabel.AccessibleContext.accessibleDescription")); // NOI18N
        autoCompletionFullRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionFullRadioButton.AccessibleContext.accessibleName")); // NOI18N
        autoCompletionFullRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionFullRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        autoCompletionCustomizeRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionCustomizeRadioButton.AccessibleContext.accessibleName")); // NOI18N
        autoCompletionCustomizeRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionCustomizeRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        autoCompletionVariablesCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionVariablesCheckBox.AccessibleContext.accessibleName")); // NOI18N
        autoCompletionVariablesCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionVariablesCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        autoCompletionTypesCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTypesCheckBox.AccessibleContext.accessibleName")); // NOI18N
        autoCompletionTypesCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionTypesCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        autoCompletionNamespacesCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionNamespacesCheckBox.AccessibleContext.accessibleName")); // NOI18N
        autoCompletionNamespacesCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.autoCompletionNamespacesCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        methodCodeCompletionLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.methodCodeCompletionLabel.AccessibleContext.accessibleName")); // NOI18N
        methodCodeCompletionLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.methodCodeCompletionLabel.AccessibleContext.accessibleDescription")); // NOI18N
        codeCompletionStaticMethodsCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionStaticMethodsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        codeCompletionStaticMethodsCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionStaticMethodsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        codeCompletionNonStaticMethodsCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionNonStaticMethodsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        codeCompletionNonStaticMethodsCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionNonStaticMethodsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        codeCompletionVariablesScopeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionVariablesScopeLabel.AccessibleContext.accessibleName")); // NOI18N
        codeCompletionVariablesScopeLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionVariablesScopeLabel.AccessibleContext.accessibleDescription")); // NOI18N
        allVariablesRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.allVariablesRadioButton.AccessibleContext.accessibleName")); // NOI18N
        allVariablesRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.allVariablesRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        currentFileVariablesRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.currentFileVariablesRadioButton.AccessibleContext.accessibleName")); // NOI18N
        currentFileVariablesRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.currentFileVariablesRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        codeCompletionTypeLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionTypeLabel.AccessibleContext.accessibleName")); // NOI18N
        codeCompletionTypeLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.codeCompletionTypeLabel.AccessibleContext.accessibleDescription")); // NOI18N
        smartRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartRadioButton.AccessibleContext.accessibleName")); // NOI18N
        smartRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        smartInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.smartInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        fullyQualifiedRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedRadioButton.AccessibleContext.accessibleName")); // NOI18N
        fullyQualifiedRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        fullyQualifiedInfoLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedInfoLabel.AccessibleContext.accessibleName")); // NOI18N
        fullyQualifiedInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.fullyQualifiedInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N
        unqualifiedRadioButton.getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedRadioButton.AccessibleContext.accessibleName")); // NOI18N
        unqualifiedRadioButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedRadioButton.AccessibleContext.accessibleDescription")); // NOI18N
        unqualifiedInfoLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.unqualifiedInfoLabel.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CodeCompletionPanel.class, "CodeCompletionPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JRadioButton allVariablesRadioButton;
    private ButtonGroup autoCompletionButtonGroup;
    private JCheckBox autoCompletionCommentAsteriskCheckBox;
    private JLabel autoCompletionCommentAsteriskLabel;
    private JRadioButton autoCompletionCustomizeRadioButton;
    private JRadioButton autoCompletionFullRadioButton;
    private JCheckBox autoCompletionNamespacesCheckBox;
    private JCheckBox autoCompletionSmartQuotesCheckBox;
    private JLabel autoCompletionSmartQuotesLabel;
    private JCheckBox autoCompletionTypesCheckBox;
    private JCheckBox autoCompletionVariablesCheckBox;
    private JCheckBox autoStringConcatenationCheckBox;
    private JCheckBox codeCompletionNonStaticMethodsCheckBox;
    private JCheckBox codeCompletionSmartParametersPreFillingCheckBox;
    private JCheckBox codeCompletionStaticMethodsCheckBox;
    private ButtonGroup codeCompletionTypeButtonGroup;
    private JLabel codeCompletionTypeLabel;
    private ButtonGroup codeCompletionVariablesScopeButtonGroup;
    private JLabel codeCompletionVariablesScopeLabel;
    private JRadioButton currentFileVariablesRadioButton;
    private JLabel enableAutocompletionLabel;
    private JLabel fullyQualifiedInfoLabel;
    private JRadioButton fullyQualifiedRadioButton;
    private JLabel methodCodeCompletionLabel;
    private JLabel smartInfoLabel;
    private JRadioButton smartRadioButton;
    private JCheckBox trueFalseNullCheckBox;
    private JLabel unqualifiedInfoLabel;
    private JRadioButton unqualifiedRadioButton;
    private JLabel useLowercaseLabel;
    // End of variables declaration//GEN-END:variables

    private final class DefaultRadioButtonListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                validateData();
            }
        }
    }

    private final class DefaultCheckBoxListener implements ItemListener {
        @Override
        public void itemStateChanged(ItemEvent e) {
            validateData();
        }
    }

    static final class CodeCompletionPreferencesCustomizer implements PreferencesCustomizer {

        private final Preferences preferences;
        private CodeCompletionPanel component;

        private CodeCompletionPreferencesCustomizer(Preferences preferences) {
            this.preferences = preferences;
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx("org.netbeans.modules.php.editor.options.CodeCompletionPanel");
        }

        @Override
        public JComponent getComponent() {
            if (component == null) {
                component = new CodeCompletionPanel(preferences);
            }
            return component;
        }
    }

    String getSavedValue(String key) {
        return id2Saved.get(key).toString();
    }

    public static final class CustomCustomizerImpl extends PreferencesCustomizer.CustomCustomizer {

        @Override
        public String getSavedValue(PreferencesCustomizer customCustomizer, String key) {
            if (customCustomizer instanceof CodeCompletionPreferencesCustomizer) {
                return ((CodeCompletionPanel) customCustomizer.getComponent()).getSavedValue(key);
            }
            return null;
        }
    }
}
