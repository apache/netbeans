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
package org.netbeans.modules.php.editor.indent.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import static org.netbeans.modules.php.editor.indent.FmtOptions.*;
import org.netbeans.modules.php.editor.indent.FmtOptions.CategorySupport;
import static org.netbeans.modules.php.editor.indent.FmtOptions.CategorySupport.OPTION_ID;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Hrebejk, Petr Pisl
 */
public class FmtBlankLines extends javax.swing.JPanel {

    @StaticResource
    private static final String PREVIEW_FILE = "org/netbeans/modules/php/editor/indent/ui/BlankLines.php"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(FmtBlankLines.class.getName());
    private static final long serialVersionUID = 4919823026214465409L;

    public FmtBlankLines() {
        initComponents();
        scrollPane1.getVerticalScrollBar().setUnitIncrement(20);
        bNamespaceField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_NAMESPACE);
        aNamespaceField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_NAMESPACE);
        bUseField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_USE);
        aUseField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_USE);
        betweenUseTypesField.putClientProperty(OPTION_ID, BLANK_LINES_BETWEEN_USE_TYPES);
        bUseTraitField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_USE_TRAIT);
        afterUseTraitTextField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_USE_TRAIT);
        bClassField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_CLASS);
        aClassField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_CLASS);
        aClassHeaderField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_CLASS_HEADER);
        bFieldsField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_FIELDS);
        betweenFields.putClientProperty(OPTION_ID, BLANK_LINES_BETWEEN_FIELDS);
        endOfFileCheckBox.putClientProperty(OPTION_ID, BLANK_LINES_EOF);
        cbGroupFields.putClientProperty(OPTION_ID, BLANK_LINES_GROUP_FIELDS_WITHOUT_DOC_AND_ATTRIBUTES);
        aFieldsField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_FIELDS);
        bMethodsField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_FUNCTION);
        aMethodsField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_FUNCTION);
        bFunctionEndField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_FUNCTION_END);
        bClassEndField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_CLASS_END);
        aOpenPHPTagField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_OPEN_PHP_TAG);
        aOpenPHPTagHTMLField.putClientProperty(OPTION_ID, BLANK_LINES_AFTER_OPEN_PHP_TAG_IN_HTML);
        bClosePHPTagField.putClientProperty(OPTION_ID, BLANK_LINES_BEFORE_CLOSE_PHP_TAG);
        maxPreservedBlankField.putClientProperty(OPTION_ID, BLANK_LINES_MAX_PRESERVED);

        bNamespaceField.addKeyListener(new NumericKeyListener());
        aNamespaceField.addKeyListener(new NumericKeyListener());
        bUseField.addKeyListener(new NumericKeyListener());
        aUseField.addKeyListener(new NumericKeyListener());
        betweenUseTypesField.addKeyListener(new NumericKeyListener());
        bUseTraitField.addKeyListener(new NumericKeyListener());
        afterUseTraitTextField.addKeyListener(new NumericKeyListener());
        bClassField.addKeyListener(new NumericKeyListener());
        aClassField.addKeyListener(new NumericKeyListener());
        bClassEndField.addKeyListener(new NumericKeyListener());
        aClassHeaderField.addKeyListener(new NumericKeyListener());
        bFieldsField.addKeyListener(new NumericKeyListener());
        betweenFields.addKeyListener(new NumericKeyListener());
        aFieldsField.addKeyListener(new NumericKeyListener());
        bMethodsField.addKeyListener(new NumericKeyListener());
        aMethodsField.addKeyListener(new NumericKeyListener());
        bFunctionEndField.addKeyListener(new NumericKeyListener());
        aOpenPHPTagField.addKeyListener(new NumericKeyListener());
        aOpenPHPTagHTMLField.addKeyListener(new NumericKeyListener());
        bClosePHPTagField.addKeyListener(new NumericKeyListener());

        Dimension dimension = new Dimension((int) jPanel1.getPreferredSize().getWidth() + Utils.POSSIBLE_SCROLL_BAR_WIDTH, (int) scrollPane1.getMinimumSize().getHeight());
        scrollPane1.setMinimumSize(dimension);
    }

    public static PreferencesCustomizer.Factory getController() {
        String preview = ""; // NOI18N
        try {
            preview = Utils.loadPreviewText(FmtBlankLines.class.getClassLoader().getResourceAsStream(PREVIEW_FILE));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        return new CategorySupport.Factory("blank-lines", FmtBlankLines.class, preview); // NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane1 = new JScrollPane();
        jPanel1 = new JPanel();
        aFieldsField = new JTextField();
        bFieldsField = new JTextField();
        aFieldsLabel = new JLabel();
        bClosePHPTagLabel = new JLabel();
        aClassHeaderField = new JTextField();
        bFieldsLabel = new JLabel();
        aClassField = new JTextField();
        aClassHeaderLabel = new JLabel();
        bClassField = new JTextField();
        aClassLabel = new JLabel();
        bMethodsField = new JTextField();
        bMethodsLabel = new JLabel();
        aMethodsLabel = new JLabel();
        aMethodsField = new JTextField();
        bFunctionEndLabel = new JLabel();
        bFunctionEndField = new JTextField();
        bClassEndLabel = new JLabel();
        bClassEndField = new JTextField();
        aOpenPHPTagLebel = new JLabel();
        aOpenPHPTagField = new JTextField();
        bNamespaceField = new JTextField();
        bNamespaceLabel = new JLabel();
        bClassLabel = new JLabel();
        aOpenPHPTagHTMLField = new JTextField();
        bUseField = new JTextField();
        bUseLabel = new JLabel();
        bClosePHPTagField = new JTextField();
        aNamespaceField = new JTextField();
        aOpenPHPTagHTMLLabel = new JLabel();
        aNamespaceLabel = new JLabel();
        aUseField = new JTextField();
        aUseLabel = new JLabel();
        betweenFieldsLabel = new JLabel();
        betweenFields = new JTextField();
        cbGroupFields = new JCheckBox();
        bUseTraitLabel = new JLabel();
        bUseTraitField = new JTextField();
        maxPreservedBlankField = new JTextField();
        maxPreservedBlankLabel = new JLabel();
        betweenUseTypesLabel = new JLabel();
        betweenUseTypesField = new JTextField();
        endOfFileCheckBox = new JCheckBox();
        afterUseTraitLabel = new JLabel();
        afterUseTraitTextField = new JTextField();

        setName(NbBundle.getMessage(FmtBlankLines.class, "LBL_BlankLines")); // NOI18N
        setOpaque(false);
        setRequestFocusEnabled(false);
        setLayout(new BorderLayout());

        scrollPane1.setRequestFocusEnabled(false);

        jPanel1.setOpaque(false);

        aFieldsField.setColumns(5);

        bFieldsField.setColumns(5);

        aFieldsLabel.setLabelFor(aFieldsField);
        Mnemonics.setLocalizedText(aFieldsLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterFields")); // NOI18N

        bClosePHPTagLabel.setLabelFor(bClosePHPTagField);
        Mnemonics.setLocalizedText(bClosePHPTagLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClosePHPTag")); // NOI18N

        aClassHeaderField.setColumns(5);

        bFieldsLabel.setLabelFor(bFieldsField);
        Mnemonics.setLocalizedText(bFieldsLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeFields")); // NOI18N

        aClassField.setColumns(5);

        aClassHeaderLabel.setLabelFor(aClassHeaderField);
        Mnemonics.setLocalizedText(aClassHeaderLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterClassHeader")); // NOI18N

        bClassField.setColumns(5);

        aClassLabel.setLabelFor(aClassField);
        Mnemonics.setLocalizedText(aClassLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterClass")); // NOI18N

        bMethodsField.setColumns(5);

        bMethodsLabel.setLabelFor(bMethodsField);
        Mnemonics.setLocalizedText(bMethodsLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeMethods")); // NOI18N

        aMethodsLabel.setLabelFor(aMethodsField);
        Mnemonics.setLocalizedText(aMethodsLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterMethods")); // NOI18N

        aMethodsField.setColumns(5);

        bFunctionEndLabel.setLabelFor(bFunctionEndField);
        Mnemonics.setLocalizedText(bFunctionEndLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeMethodsEnd")); // NOI18N

        bFunctionEndField.setColumns(5);

        bClassEndLabel.setLabelFor(bClassEndField);
        Mnemonics.setLocalizedText(bClassEndLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClassEnd")); // NOI18N

        bClassEndField.setColumns(5);

        aOpenPHPTagLebel.setLabelFor(aOpenPHPTagField);
        Mnemonics.setLocalizedText(aOpenPHPTagLebel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterPHPOpenTag")); // NOI18N

        aOpenPHPTagField.setText(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagField.text")); // NOI18N

        bNamespaceField.setColumns(5);

        bNamespaceLabel.setLabelFor(bNamespaceField);
        Mnemonics.setLocalizedText(bNamespaceLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeNameSpace")); // NOI18N

        bClassLabel.setLabelFor(bClassField);
        Mnemonics.setLocalizedText(bClassLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeClass")); // NOI18N

        bUseField.setColumns(5);

        bUseLabel.setLabelFor(bUseField);
        Mnemonics.setLocalizedText(bUseLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeUse")); // NOI18N

        aNamespaceField.setColumns(5);

        aOpenPHPTagHTMLLabel.setLabelFor(aOpenPHPTagHTMLField);
        Mnemonics.setLocalizedText(aOpenPHPTagHTMLLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blBeforeOpenTagInHTML")); // NOI18N

        aNamespaceLabel.setLabelFor(aNamespaceField);
        Mnemonics.setLocalizedText(aNamespaceLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterNamespace")); // NOI18N

        aUseField.setColumns(5);

        aUseLabel.setLabelFor(aUseField);
        Mnemonics.setLocalizedText(aUseLabel, NbBundle.getMessage(FmtBlankLines.class, "LBL_blAfterImports")); // NOI18N

        Mnemonics.setLocalizedText(betweenFieldsLabel, NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.betweenFieldsLabel.text")); // NOI18N

        betweenFields.setText(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.betweenFields.text")); // NOI18N

        Mnemonics.setLocalizedText(cbGroupFields, NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.cbGroupFields.text")); // NOI18N

        Mnemonics.setLocalizedText(bUseTraitLabel, NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bUseTraitLabel.text")); // NOI18N

        bUseTraitField.setText(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bUseTraitField.text")); // NOI18N

        Mnemonics.setLocalizedText(maxPreservedBlankLabel, NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.maxPreservedBlankLabel.text")); // NOI18N

        betweenUseTypesLabel.setLabelFor(betweenUseTypesField);
        Mnemonics.setLocalizedText(betweenUseTypesLabel, NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.betweenUseTypesLabel.text")); // NOI18N

        betweenUseTypesField.setColumns(5);
        betweenUseTypesField.setText(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.betweenUseTypesField.text")); // NOI18N

        Mnemonics.setLocalizedText(endOfFileCheckBox, NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.endOfFileCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(afterUseTraitLabel, NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.afterUseTraitLabel.text")); // NOI18N

        afterUseTraitTextField.setText(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.afterUseTraitTextField.text")); // NOI18N

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(betweenFieldsLabel)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addComponent(bClosePHPTagLabel)
                                    .addComponent(aOpenPHPTagHTMLLabel))
                                .addComponent(bMethodsLabel)
                                .addComponent(bFunctionEndLabel)
                                .addComponent(aMethodsLabel)
                                .addComponent(aOpenPHPTagLebel)
                                .addComponent(aFieldsLabel))
                            .addComponent(bFieldsLabel)
                            .addComponent(aClassLabel)
                            .addComponent(bClassEndLabel)
                            .addComponent(aClassHeaderLabel)
                            .addComponent(bClassLabel)
                            .addComponent(aUseLabel)
                            .addComponent(bUseLabel)
                            .addComponent(aNamespaceLabel)
                            .addComponent(bNamespaceLabel)
                            .addComponent(bUseTraitLabel)
                            .addComponent(maxPreservedBlankLabel)
                            .addComponent(afterUseTraitLabel))
                        .addGap(12, 12, 12)
                        .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(aMethodsField, GroupLayout.Alignment.LEADING)
                                    .addComponent(bMethodsField, GroupLayout.Alignment.LEADING)
                                    .addComponent(aFieldsField, GroupLayout.Alignment.LEADING)
                                    .addComponent(bFunctionEndField, GroupLayout.Alignment.LEADING)
                                    .addComponent(betweenFields, GroupLayout.Alignment.LEADING)
                                    .addComponent(bFieldsField, GroupLayout.Alignment.LEADING)
                                    .addComponent(aClassField, GroupLayout.Alignment.LEADING)
                                    .addComponent(bClassEndField, GroupLayout.Alignment.LEADING)
                                    .addComponent(aClassHeaderField, GroupLayout.Alignment.LEADING)
                                    .addComponent(bClassField, GroupLayout.Alignment.LEADING)
                                    .addComponent(aUseField, GroupLayout.Alignment.LEADING)
                                    .addComponent(bUseField, GroupLayout.Alignment.LEADING)
                                    .addComponent(aNamespaceField, GroupLayout.Alignment.LEADING)
                                    .addComponent(bNamespaceField, GroupLayout.Alignment.LEADING)
                                    .addComponent(betweenUseTypesField, GroupLayout.Alignment.LEADING))
                                .addComponent(aOpenPHPTagField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(bClosePHPTagField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addComponent(aOpenPHPTagHTMLField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(maxPreservedBlankField)
                                .addComponent(bUseTraitField)
                                .addComponent(afterUseTraitTextField))))
                    .addComponent(cbGroupFields, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(betweenUseTypesLabel)
                    .addComponent(endOfFileCheckBox))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {aClassField, aClassHeaderField, aFieldsField, aMethodsField, aNamespaceField, aOpenPHPTagField, aOpenPHPTagHTMLField, aUseField, bClassEndField, bClassField, bClosePHPTagField, bFieldsField, bFunctionEndField, bMethodsField, bNamespaceField, bUseField, bUseTraitField, betweenFields});

        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bNamespaceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bNamespaceLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aNamespaceField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(aNamespaceLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bUseField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bUseLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(betweenUseTypesLabel)
                    .addComponent(betweenUseTypesField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aUseField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(aUseLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bClassField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bClassLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aClassHeaderField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(aClassHeaderLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bClassEndField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bClassEndLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aClassField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(aClassLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bFieldsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFieldsLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(betweenFieldsLabel)
                    .addComponent(betweenFields, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aFieldsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(aFieldsLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bMethodsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bMethodsLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aMethodsField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(aMethodsLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bFunctionEndField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bFunctionEndLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aOpenPHPTagLebel)
                    .addComponent(aOpenPHPTagField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(aOpenPHPTagHTMLField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(aOpenPHPTagHTMLLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bClosePHPTagLabel)
                    .addComponent(bClosePHPTagField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bUseTraitField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(bUseTraitLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(afterUseTraitLabel)
                    .addComponent(afterUseTraitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(maxPreservedBlankLabel)
                    .addComponent(maxPreservedBlankField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endOfFileCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbGroupFields, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        aFieldsField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aFieldsField.AccessibleContext.accessibleDescription")); // NOI18N
        bFieldsField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bFieldsField.AccessibleContext.accessibleDescription")); // NOI18N
        aFieldsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aFieldsLabel.AccessibleContext.accessibleName")); // NOI18N
        aFieldsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aFieldsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bClosePHPTagLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClosePHPTagLabel.AccessibleContext.accessibleName")); // NOI18N
        bClosePHPTagLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClosePHPTagLabel.AccessibleContext.accessibleDescription")); // NOI18N
        aClassHeaderField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aClassHeaderField.AccessibleContext.accessibleDescription")); // NOI18N
        bFieldsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bFieldsLabel.AccessibleContext.accessibleName")); // NOI18N
        bFieldsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bFieldsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        aClassField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aClassField.AccessibleContext.accessibleDescription")); // NOI18N
        aClassHeaderLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aClassHeaderLabel.AccessibleContext.accessibleName")); // NOI18N
        aClassHeaderLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aClassHeaderLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bClassField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClassField.AccessibleContext.accessibleDescription")); // NOI18N
        aClassLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aClassLabel.AccessibleContext.accessibleName")); // NOI18N
        aClassLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aClassLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bMethodsField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bMethodsField.AccessibleContext.accessibleDescription")); // NOI18N
        bMethodsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bMethodsLabel.AccessibleContext.accessibleName")); // NOI18N
        bMethodsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bMethodsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        aMethodsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aMethodsLabel.AccessibleContext.accessibleName")); // NOI18N
        aMethodsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aMethodsLabel.AccessibleContext.accessibleDescription")); // NOI18N
        aMethodsField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aMethodsField.AccessibleContext.accessibleName")); // NOI18N
        aMethodsField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aMethodsField.AccessibleContext.accessibleDescription")); // NOI18N
        bFunctionEndLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bFunctionEndLabel.AccessibleContext.accessibleName")); // NOI18N
        bFunctionEndLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bFunctionEndLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bFunctionEndField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bFunctionEndField.AccessibleContext.accessibleName")); // NOI18N
        bFunctionEndField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bFunctionEndField.AccessibleContext.accessibleDescription")); // NOI18N
        bClassEndLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClassEndLabel.AccessibleContext.accessibleName")); // NOI18N
        bClassEndLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClassEndLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bClassEndField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClassEndField.AccessibleContext.accessibleName")); // NOI18N
        bClassEndField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClassEndField.AccessibleContext.accessibleDescription")); // NOI18N
        aOpenPHPTagLebel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagLebel.AccessibleContext.accessibleName")); // NOI18N
        aOpenPHPTagLebel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagLebel.AccessibleContext.accessibleDescription")); // NOI18N
        aOpenPHPTagField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagField.AccessibleContext.accessibleName")); // NOI18N
        aOpenPHPTagField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagField.AccessibleContext.accessibleDescription")); // NOI18N
        bNamespaceField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bNamespaceField.AccessibleContext.accessibleDescription")); // NOI18N
        bNamespaceLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bNamespaceLabel.AccessibleContext.accessibleName")); // NOI18N
        bNamespaceLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bNamespaceLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bClassLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClassLabel.AccessibleContext.accessibleName")); // NOI18N
        bClassLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClassLabel.AccessibleContext.accessibleDescription")); // NOI18N
        aOpenPHPTagHTMLField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagHTMLField.AccessibleContext.accessibleDescription")); // NOI18N
        bUseField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bUseField.AccessibleContext.accessibleDescription")); // NOI18N
        bUseLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bUseLabel.AccessibleContext.accessibleName")); // NOI18N
        bUseLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bUseLabel.AccessibleContext.accessibleDescription")); // NOI18N
        bClosePHPTagField.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClosePHPTagField.AccessibleContext.accessibleName")); // NOI18N
        bClosePHPTagField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.bClosePHPTagField.AccessibleContext.accessibleDescription")); // NOI18N
        aNamespaceField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aNamespaceField.AccessibleContext.accessibleDescription")); // NOI18N
        aOpenPHPTagHTMLLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagHTMLLabel.AccessibleContext.accessibleName")); // NOI18N
        aOpenPHPTagHTMLLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aOpenPHPTagHTMLLabel.AccessibleContext.accessibleDescription")); // NOI18N
        aNamespaceLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aNamespaceLabel.AccessibleContext.accessibleName")); // NOI18N
        aNamespaceLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aNamespaceLabel.AccessibleContext.accessibleDescription")); // NOI18N
        aUseField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aUseField.AccessibleContext.accessibleDescription")); // NOI18N
        aUseLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aUseLabel.AccessibleContext.accessibleName")); // NOI18N
        aUseLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.aUseLabel.AccessibleContext.accessibleDescription")); // NOI18N
        maxPreservedBlankLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.maxPreservedBlankLabel.AccessibleContext.accessibleName")); // NOI18N
        maxPreservedBlankLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.maxPreservedBlankLabel.AccessibleContext.accessibleDescription")); // NOI18N
        betweenUseTypesLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.betweenUseTypesLabel.AccessibleContext.accessibleDescription")); // NOI18N

        scrollPane1.setViewportView(jPanel1);
        jPanel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.jPanel1.AccessibleContext.accessibleName")); // NOI18N
        jPanel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.jPanel1.AccessibleContext.accessibleDescription")); // NOI18N

        add(scrollPane1, BorderLayout.CENTER);
        scrollPane1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.scrollPane1.AccessibleContext.accessibleName")); // NOI18N
        scrollPane1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.scrollPane1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtBlankLines.class, "FmtBlankLines.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JTextField aClassField;
    private JTextField aClassHeaderField;
    private JLabel aClassHeaderLabel;
    private JLabel aClassLabel;
    private JTextField aFieldsField;
    private JLabel aFieldsLabel;
    private JTextField aMethodsField;
    private JLabel aMethodsLabel;
    private JTextField aNamespaceField;
    private JLabel aNamespaceLabel;
    private JTextField aOpenPHPTagField;
    private JTextField aOpenPHPTagHTMLField;
    private JLabel aOpenPHPTagHTMLLabel;
    private JLabel aOpenPHPTagLebel;
    private JTextField aUseField;
    private JLabel aUseLabel;
    private JLabel afterUseTraitLabel;
    private JTextField afterUseTraitTextField;
    private JTextField bClassEndField;
    private JLabel bClassEndLabel;
    private JTextField bClassField;
    private JLabel bClassLabel;
    private JTextField bClosePHPTagField;
    private JLabel bClosePHPTagLabel;
    private JTextField bFieldsField;
    private JLabel bFieldsLabel;
    private JTextField bFunctionEndField;
    private JLabel bFunctionEndLabel;
    private JTextField bMethodsField;
    private JLabel bMethodsLabel;
    private JTextField bNamespaceField;
    private JLabel bNamespaceLabel;
    private JTextField bUseField;
    private JLabel bUseLabel;
    private JTextField bUseTraitField;
    private JLabel bUseTraitLabel;
    private JTextField betweenFields;
    private JLabel betweenFieldsLabel;
    private JTextField betweenUseTypesField;
    private JLabel betweenUseTypesLabel;
    private JCheckBox cbGroupFields;
    private JCheckBox endOfFileCheckBox;
    private JPanel jPanel1;
    private JTextField maxPreservedBlankField;
    private JLabel maxPreservedBlankLabel;
    private JScrollPane scrollPane1;
    // End of variables declaration//GEN-END:variables

}
