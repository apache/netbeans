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
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import static org.netbeans.modules.php.editor.indent.FmtOptions.*;
import org.netbeans.modules.php.editor.indent.FmtOptions.CategorySupport;
import static org.netbeans.modules.php.editor.indent.FmtOptions.CategorySupport.OPTION_ID;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Pisl
 */
public class FmtWrapping extends javax.swing.JPanel implements FocusListener {

    private static final Logger LOGGER = Logger.getLogger(FmtWrapping.class.getName());

    public FmtWrapping() {
        initComponents();

        scrollPane.getViewport().setBackground(java.awt.SystemColor.controlLtHighlight);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);

        groupUseListCombo.putClientProperty(OPTION_ID, WRAP_GROUP_USE_LIST);
        groupUseListCombo.addFocusListener(this);
        extendsImplementsKeywordCombo.putClientProperty(OPTION_ID, WRAP_EXTENDS_IMPLEMENTS_KEYWORD);
        extendsImplementsKeywordCombo.addFocusListener(this);
        extendsImplementsListCombo.putClientProperty(OPTION_ID, WRAP_EXTENDS_IMPLEMENTS_LIST);
        extendsImplementsListCombo.addFocusListener(this);
        methodParamsCombo.putClientProperty(OPTION_ID, WRAP_METHOD_PARAMS);
        methodParamsCombo.addFocusListener(this);
        methodCallArgsCombo.putClientProperty(OPTION_ID, WRAP_METHOD_CALL_ARGS);
        methodCallArgsCombo.addFocusListener(this);
        chainedMethodCallsCombo.putClientProperty(OPTION_ID, WRAP_CHAINED_METHOD_CALLS);
        chainedMethodCallsCombo.addFocusListener(this);
        arrayInitCombo.putClientProperty(OPTION_ID, WRAP_ARRAY_INIT);
        arrayInitCombo.addFocusListener(this);
        forCombo.putClientProperty(OPTION_ID, WRAP_FOR);
        forCombo.addFocusListener(this);
        forStatementCombo.putClientProperty(OPTION_ID, WRAP_FOR_STATEMENT);
        forStatementCombo.addFocusListener(this);
        ifStatementCombo.putClientProperty(OPTION_ID, WRAP_IF_STATEMENT);
        ifStatementCombo.addFocusListener(this);
        whileStatementComboBox.putClientProperty(OPTION_ID, WRAP_WHILE_STATEMENT);
        whileStatementComboBox.addFocusListener(this);
        doWhileStatementCombo.putClientProperty(OPTION_ID, WRAP_DO_WHILE_STATEMENT);
        doWhileStatementCombo.addFocusListener(this);
        binaryOpsCombo.putClientProperty(OPTION_ID, WRAP_BINARY_OPS);
        binaryOpsCombo.addFocusListener(this);
        ternaryOpsCombo.putClientProperty(OPTION_ID, WRAP_TERNARY_OPS);
        ternaryOpsCombo.addFocusListener(this);
        coalescingOpsCombo.putClientProperty(OPTION_ID, WRAP_COALESCING_OPS);
        coalescingOpsCombo.addFocusListener(this);
        assignOpsCombo.putClientProperty(OPTION_ID, WRAP_ASSIGN_OPS);
        assignOpsCombo.addFocusListener(this);
        cbOpenCloseBlockBrace.putClientProperty(OPTION_ID, WRAP_BLOCK_BRACES);
        cbStatements.putClientProperty(OPTION_ID, WRAP_STATEMENTS_ON_THE_LINE);
        wrapAfterBinOpsCheckBox.putClientProperty(OPTION_ID, WRAP_AFTER_BIN_OPS);
        wrapAfterAssignOpsCheckBox.putClientProperty(OPTION_ID, WRAP_AFTER_ASSIGN_OPS);
        groupUseBracesCheckBox.putClientProperty(OPTION_ID, WRAP_GROUP_USE_BRACES);
        wrapMethodParamsAfterLeftParenCheckBox.putClientProperty(OPTION_ID, WRAP_METHOD_PARAMS_AFTER_LEFT_PAREN);
        wrapMethodParamsRightParenCheckBox.putClientProperty(OPTION_ID, WRAP_METHOD_PARAMS_RIGHT_PAREN);
        wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox.putClientProperty(OPTION_ID, WRAP_METHOD_PARAMS_KEEP_PAREN_AND_BRACE_ON_THE_SAME_LINE);
        wrapMethodCallArgsAfterLeftParenCheckBox.putClientProperty(OPTION_ID, WRAP_METHOD_CALL_ARGS_AFTER_LEFT_PAREN);
        wrapMethodCallArgsRightParenCheckBox.putClientProperty(OPTION_ID, WRAP_METHOD_CALL_ARGS_RIGHT_PAREN);
        wrapForAfterLeftParenCheckBox.putClientProperty(OPTION_ID, WRAP_FOR_AFTER_LEFT_PAREN);
        wrapForRightParenCheckBox.putClientProperty(OPTION_ID, WRAP_FOR_RIGHT_PAREN);
    }

    public static PreferencesCustomizer.Factory getController() {
        String preview = ""; // NOI18N
        try {
            preview = Utils.loadPreviewText(FmtWrapping.class.getClassLoader().getResourceAsStream("org/netbeans/modules/php/editor/indent/ui/Wrapping.php")); // NOI18N
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return new CategorySupport.Factory("wrapping", FmtWrapping.class, preview); // NOI18N
    }

    @Override
    public void focusGained(FocusEvent e) {
        scrollPane.getViewport().scrollRectToVisible(e.getComponent().getBounds());
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new JScrollPane();
        panel1 = new JPanel();
        groupUseListLabel = new JLabel();
        groupUseListCombo = new JComboBox<>();
        extendsImplemetsKeywordLabel = new JLabel();
        extendsImplementsKeywordCombo = new JComboBox();
        extendsImplementsListLabel = new JLabel();
        extendsImplementsListCombo = new JComboBox();
        methodParamsLabel = new JLabel();
        methodParamsCombo = new JComboBox();
        methodCallArgsLabel = new JLabel();
        methodCallArgsCombo = new JComboBox();
        chainedMethodCallsLabel = new JLabel();
        chainedMethodCallsCombo = new JComboBox();
        arrayInitLabel = new JLabel();
        arrayInitCombo = new JComboBox();
        forLabel = new JLabel();
        forCombo = new JComboBox();
        forStatementLabel = new JLabel();
        forStatementCombo = new JComboBox();
        ifStatementLabel = new JLabel();
        ifStatementCombo = new JComboBox();
        whileStatementLabel = new JLabel();
        whileStatementComboBox = new JComboBox();
        doWhileStatementLabel = new JLabel();
        doWhileStatementCombo = new JComboBox();
        binaryOpsLabel = new JLabel();
        binaryOpsCombo = new JComboBox();
        ternaryOpsLabel = new JLabel();
        ternaryOpsCombo = new JComboBox();
        coalescingOpsLabel = new JLabel();
        coalescingOpsCombo = new JComboBox<>();
        assignOpsLabel = new JLabel();
        assignOpsCombo = new JComboBox();
        cbOpenCloseBlockBrace = new JCheckBox();
        cbStatements = new JCheckBox();
        wrapAfterBinOpsCheckBox = new JCheckBox();
        wrapAfterAssignOpsCheckBox = new JCheckBox();
        groupUseBracesCheckBox = new JCheckBox();
        wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox = new JCheckBox();
        wrapMethodParamsAfterLeftParenCheckBox = new JCheckBox();
        wrapMethodParamsRightParenCheckBox = new JCheckBox();
        wrapMethodCallArgsAfterLeftParenCheckBox = new JCheckBox();
        wrapMethodCallArgsRightParenCheckBox = new JCheckBox();
        wrapForAfterLeftParenCheckBox = new JCheckBox();
        wrapForRightParenCheckBox = new JCheckBox();

        setName(NbBundle.getMessage(FmtWrapping.class, "LBL_Wrapping")); // NOI18N
        setOpaque(false);
        setLayout(new BorderLayout());

        panel1.setFocusCycleRoot(true);
        panel1.setFocusTraversalPolicy(new FocusTraversalPolicy() {
            public Component getDefaultComponent(Container focusCycleRoot){
                return cbStatements;
            }//end getDefaultComponent

            public Component getFirstComponent(Container focusCycleRoot){
                return cbStatements;
            }//end getFirstComponent

            public Component getLastComponent(Container focusCycleRoot){
                return cbStatements;
            }//end getLastComponent

            public Component getComponentAfter(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  cbOpenCloseBlockBrace){
                    return cbStatements;
                }
                if(aComponent ==  assignOpsCombo){
                    return cbOpenCloseBlockBrace;
                }
                if(aComponent ==  chainedMethodCallsCombo){
                    return arrayInitCombo;
                }
                if(aComponent ==  methodCallArgsCombo){
                    return chainedMethodCallsCombo;
                }
                if(aComponent ==  methodParamsCombo){
                    return methodCallArgsCombo;
                }
                if(aComponent ==  extendsImplementsListCombo){
                    return methodParamsCombo;
                }
                if(aComponent ==  doWhileStatementCombo){
                    return binaryOpsCombo;
                }
                if(aComponent ==  extendsImplementsKeywordCombo){
                    return extendsImplementsListCombo;
                }
                if(aComponent ==  ternaryOpsCombo){
                    return assignOpsCombo;
                }
                if(aComponent ==  binaryOpsCombo){
                    return ternaryOpsCombo;
                }
                if(aComponent ==  whileStatementComboBox){
                    return doWhileStatementCombo;
                }
                if(aComponent ==  forStatementCombo){
                    return ifStatementCombo;
                }
                if(aComponent ==  ifStatementCombo){
                    return whileStatementComboBox;
                }
                if(aComponent ==  arrayInitCombo){
                    return forCombo;
                }
                if(aComponent ==  forCombo){
                    return forStatementCombo;
                }
                return cbStatements;//end getComponentAfter
            }
            public Component getComponentBefore(Container focusCycleRoot, Component aComponent){
                if(aComponent ==  cbStatements){
                    return cbOpenCloseBlockBrace;
                }
                if(aComponent ==  cbOpenCloseBlockBrace){
                    return assignOpsCombo;
                }
                if(aComponent ==  arrayInitCombo){
                    return chainedMethodCallsCombo;
                }
                if(aComponent ==  chainedMethodCallsCombo){
                    return methodCallArgsCombo;
                }
                if(aComponent ==  methodCallArgsCombo){
                    return methodParamsCombo;
                }
                if(aComponent ==  methodParamsCombo){
                    return extendsImplementsListCombo;
                }
                if(aComponent ==  binaryOpsCombo){
                    return doWhileStatementCombo;
                }
                if(aComponent ==  extendsImplementsListCombo){
                    return extendsImplementsKeywordCombo;
                }
                if(aComponent ==  assignOpsCombo){
                    return ternaryOpsCombo;
                }
                if(aComponent ==  ternaryOpsCombo){
                    return binaryOpsCombo;
                }
                if(aComponent ==  doWhileStatementCombo){
                    return whileStatementComboBox;
                }
                if(aComponent ==  ifStatementCombo){
                    return forStatementCombo;
                }
                if(aComponent ==  whileStatementComboBox){
                    return ifStatementCombo;
                }
                if(aComponent ==  forCombo){
                    return arrayInitCombo;
                }
                if(aComponent ==  forStatementCombo){
                    return forCombo;
                }
                return cbStatements;//end getComponentBefore

            }});

            groupUseListLabel.setLabelFor(groupUseListCombo);
            Mnemonics.setLocalizedText(groupUseListLabel, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.groupUseListLabel.text")); // NOI18N

            groupUseListCombo.setModel(new DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            extendsImplemetsKeywordLabel.setLabelFor(extendsImplementsKeywordCombo);
            Mnemonics.setLocalizedText(extendsImplemetsKeywordLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_extendsImplementsKeyword")); // NOI18N

            extendsImplementsKeywordCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            extendsImplementsListLabel.setLabelFor(extendsImplementsListCombo);
            Mnemonics.setLocalizedText(extendsImplementsListLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_extendsImplementsList")); // NOI18N

            extendsImplementsListCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            methodParamsLabel.setLabelFor(methodParamsCombo);
            Mnemonics.setLocalizedText(methodParamsLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_methodParameters")); // NOI18N

            methodParamsCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            methodCallArgsLabel.setLabelFor(methodCallArgsCombo);
            Mnemonics.setLocalizedText(methodCallArgsLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_methodCallArgs")); // NOI18N

            methodCallArgsCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            chainedMethodCallsLabel.setLabelFor(chainedMethodCallsCombo);
            Mnemonics.setLocalizedText(chainedMethodCallsLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_chainedMethodCalls")); // NOI18N

            chainedMethodCallsCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            arrayInitLabel.setLabelFor(arrayInitCombo);
            Mnemonics.setLocalizedText(arrayInitLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_arrayInit")); // NOI18N

            arrayInitCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            forLabel.setLabelFor(forCombo);
            Mnemonics.setLocalizedText(forLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_for")); // NOI18N

            forCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            forStatementLabel.setLabelFor(forStatementCombo);
            Mnemonics.setLocalizedText(forStatementLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_forStatement")); // NOI18N

            forStatementCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            ifStatementLabel.setLabelFor(ifStatementCombo);
            Mnemonics.setLocalizedText(ifStatementLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_ifStatement")); // NOI18N

            ifStatementCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            whileStatementLabel.setLabelFor(whileStatementComboBox);
            Mnemonics.setLocalizedText(whileStatementLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_whileStatement")); // NOI18N

            whileStatementComboBox.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            doWhileStatementLabel.setLabelFor(doWhileStatementCombo);
            Mnemonics.setLocalizedText(doWhileStatementLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_doWhileStatement")); // NOI18N

            doWhileStatementCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            binaryOpsLabel.setLabelFor(binaryOpsCombo);
            Mnemonics.setLocalizedText(binaryOpsLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_binaryOps")); // NOI18N

            binaryOpsCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            ternaryOpsLabel.setLabelFor(ternaryOpsCombo);
            Mnemonics.setLocalizedText(ternaryOpsLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_ternaryOps")); // NOI18N

            ternaryOpsCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            coalescingOpsLabel.setLabelFor(coalescingOpsCombo);
            Mnemonics.setLocalizedText(coalescingOpsLabel, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.coalescingOpsLabel.text")); // NOI18N

            coalescingOpsCombo.setModel(new DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            assignOpsLabel.setLabelFor(assignOpsCombo);
            Mnemonics.setLocalizedText(assignOpsLabel, NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_assignOps")); // NOI18N

            assignOpsCombo.setModel(new DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

            Mnemonics.setLocalizedText(cbOpenCloseBlockBrace, NbBundle.getMessage(FmtWrapping.class, "cb_wrp_open_close_block_brace")); // NOI18N

            Mnemonics.setLocalizedText(cbStatements, NbBundle.getMessage(FmtWrapping.class, "cb_wrp_Statements")); // NOI18N
            cbStatements.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    cbStatementsActionPerformed(evt);
                }
            });

            wrapAfterBinOpsCheckBox.setMnemonic('A');
            Mnemonics.setLocalizedText(wrapAfterBinOpsCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapAfterBinOpsCheckBox.text_1")); // NOI18N

            wrapAfterAssignOpsCheckBox.setMnemonic('r');
            Mnemonics.setLocalizedText(wrapAfterAssignOpsCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapAfterAssignOpsCheckBox.text_1")); // NOI18N

            Mnemonics.setLocalizedText(groupUseBracesCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.groupUseBracesCheckBox.text_1")); // NOI18N

            Mnemonics.setLocalizedText(wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox.text_1")); // NOI18N
            wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox.setToolTipText(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox.toolTipText")); // NOI18N

            Mnemonics.setLocalizedText(wrapMethodParamsAfterLeftParenCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapMethodParamsAfterLeftParenCheckBox.text_1")); // NOI18N

            Mnemonics.setLocalizedText(wrapMethodParamsRightParenCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapMethodParamsRightParenCheckBox.text")); // NOI18N

            Mnemonics.setLocalizedText(wrapMethodCallArgsAfterLeftParenCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapMethodCallArgsAfterLeftParenCheckBox.text_1")); // NOI18N

            Mnemonics.setLocalizedText(wrapMethodCallArgsRightParenCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapMethodCallArgsRightParenCheckBox.text")); // NOI18N

            Mnemonics.setLocalizedText(wrapForAfterLeftParenCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapForAfterLeftParenCheckBox.text_1")); // NOI18N

            Mnemonics.setLocalizedText(wrapForRightParenCheckBox, NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.wrapForRightParenCheckBox.text")); // NOI18N

            GroupLayout panel1Layout = new GroupLayout(panel1);
            panel1.setLayout(panel1Layout);
            panel1Layout.setHorizontalGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(panel1Layout.createSequentialGroup()
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(panel1Layout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(extendsImplemetsKeywordLabel)
                                .addComponent(methodCallArgsLabel))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(methodParamsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(methodCallArgsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(groupUseListCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(extendsImplementsKeywordCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(extendsImplementsListCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(chainedMethodCallsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(arrayInitCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(forCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(forStatementCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(ifStatementCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(whileStatementComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(doWhileStatementCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(binaryOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(ternaryOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(assignOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(coalescingOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addGroup(panel1Layout.createSequentialGroup()
                            .addContainerGap()
                            .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(wrapAfterAssignOpsCheckBox)
                                .addComponent(cbOpenCloseBlockBrace)
                                .addComponent(cbStatements)))
                        .addGroup(panel1Layout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addComponent(wrapAfterBinOpsCheckBox))
                        .addGroup(panel1Layout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addComponent(groupUseBracesCheckBox))
                        .addGroup(panel1Layout.createSequentialGroup()
                            .addGap(12, 12, 12)
                            .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(groupUseListLabel)
                                .addComponent(extendsImplementsListLabel)
                                .addComponent(methodParamsLabel)
                                .addComponent(chainedMethodCallsLabel)
                                .addComponent(arrayInitLabel)
                                .addComponent(forLabel)
                                .addComponent(forStatementLabel)
                                .addComponent(ifStatementLabel)
                                .addComponent(whileStatementLabel)
                                .addComponent(doWhileStatementLabel)
                                .addComponent(binaryOpsLabel)
                                .addComponent(ternaryOpsLabel)
                                .addComponent(assignOpsLabel)
                                .addGroup(panel1Layout.createSequentialGroup()
                                    .addGap(12, 12, 12)
                                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(wrapMethodParamsRightParenCheckBox)
                                        .addComponent(wrapMethodParamsAfterLeftParenCheckBox)
                                        .addComponent(wrapMethodCallArgsAfterLeftParenCheckBox)
                                        .addComponent(wrapMethodCallArgsRightParenCheckBox)
                                        .addComponent(wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox)
                                        .addComponent(wrapForAfterLeftParenCheckBox)
                                        .addComponent(wrapForRightParenCheckBox)))
                                .addComponent(coalescingOpsLabel))))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            panel1Layout.setVerticalGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(panel1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(groupUseListLabel)
                        .addComponent(groupUseListCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(groupUseBracesCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(extendsImplemetsKeywordLabel)
                        .addComponent(extendsImplementsKeywordCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(extendsImplementsListLabel)
                        .addComponent(extendsImplementsListCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(methodParamsLabel)
                        .addComponent(methodParamsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGap(1, 1, 1)
                    .addComponent(wrapMethodParamsAfterLeftParenCheckBox)
                    .addGap(10, 10, 10)
                    .addComponent(wrapMethodParamsRightParenCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(methodCallArgsLabel)
                        .addComponent(methodCallArgsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(wrapMethodCallArgsAfterLeftParenCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(wrapMethodCallArgsRightParenCheckBox)
                    .addGap(9, 9, 9)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(chainedMethodCallsLabel)
                        .addComponent(chainedMethodCallsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(arrayInitLabel)
                        .addComponent(arrayInitCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(forLabel)
                        .addComponent(forCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(wrapForAfterLeftParenCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(wrapForRightParenCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(forStatementLabel)
                        .addComponent(forStatementCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(ifStatementLabel)
                        .addComponent(ifStatementCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(whileStatementLabel)
                        .addComponent(whileStatementComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(doWhileStatementLabel)
                        .addComponent(doWhileStatementCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(binaryOpsLabel)
                        .addComponent(binaryOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(wrapAfterBinOpsCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(ternaryOpsLabel)
                        .addComponent(ternaryOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(coalescingOpsLabel)
                        .addComponent(coalescingOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(panel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(assignOpsLabel)
                        .addComponent(assignOpsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(wrapAfterAssignOpsCheckBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cbOpenCloseBlockBrace)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(cbStatements)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            extendsImplemetsKeywordLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplemetsKeywordLabel.AccessibleContext.accessibleName")); // NOI18N
            extendsImplemetsKeywordLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplemetsKeywordLabel.AccessibleContext.accessibleDescription")); // NOI18N
            extendsImplementsKeywordCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplementsKeywordCombo.AccessibleContext.accessibleName")); // NOI18N
            extendsImplementsKeywordCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplementsKeywordCombo.AccessibleContext.accessibleDescription")); // NOI18N
            extendsImplementsListLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplementsListLabel.AccessibleContext.accessibleName")); // NOI18N
            extendsImplementsListLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplementsListLabel.AccessibleContext.accessibleDescription")); // NOI18N
            extendsImplementsListCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplementsListCombo.AccessibleContext.accessibleName")); // NOI18N
            extendsImplementsListCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.extendsImplementsListCombo.AccessibleContext.accessibleDescription")); // NOI18N
            methodParamsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodParamsLabel.AccessibleContext.accessibleName")); // NOI18N
            methodParamsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodParamsLabel.AccessibleContext.accessibleDescription")); // NOI18N
            methodParamsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodParamsCombo.AccessibleContext.accessibleName")); // NOI18N
            methodParamsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodParamsCombo.AccessibleContext.accessibleDescription")); // NOI18N
            methodCallArgsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodCallArgsLabel.AccessibleContext.accessibleName")); // NOI18N
            methodCallArgsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodCallArgsLabel.AccessibleContext.accessibleDescription")); // NOI18N
            methodCallArgsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodCallArgsCombo.AccessibleContext.accessibleName")); // NOI18N
            methodCallArgsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.methodCallArgsCombo.AccessibleContext.accessibleDescription")); // NOI18N
            chainedMethodCallsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.chainedMethodCallsLabel.AccessibleContext.accessibleName")); // NOI18N
            chainedMethodCallsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.chainedMethodCallsLabel.AccessibleContext.accessibleDescription")); // NOI18N
            chainedMethodCallsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.chainedMethodCallsCombo.AccessibleContext.accessibleName")); // NOI18N
            chainedMethodCallsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.chainedMethodCallsCombo.AccessibleContext.accessibleDescription")); // NOI18N
            arrayInitLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.arrayInitLabel.AccessibleContext.accessibleName")); // NOI18N
            arrayInitLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.arrayInitLabel.AccessibleContext.accessibleDescription")); // NOI18N
            arrayInitCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.arrayInitCombo.AccessibleContext.accessibleName")); // NOI18N
            arrayInitCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.arrayInitCombo.AccessibleContext.accessibleDescription")); // NOI18N
            forLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forLabel.AccessibleContext.accessibleName")); // NOI18N
            forLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forLabel.AccessibleContext.accessibleDescription")); // NOI18N
            forCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forCombo.AccessibleContext.accessibleName")); // NOI18N
            forCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forCombo.AccessibleContext.accessibleDescription")); // NOI18N
            forStatementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forStatementLabel.AccessibleContext.accessibleName")); // NOI18N
            forStatementLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forStatementLabel.AccessibleContext.accessibleDescription")); // NOI18N
            forStatementCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forStatementCombo.AccessibleContext.accessibleName")); // NOI18N
            forStatementCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.forStatementCombo.AccessibleContext.accessibleDescription")); // NOI18N
            ifStatementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ifStatementLabel.AccessibleContext.accessibleName")); // NOI18N
            ifStatementLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ifStatementLabel.AccessibleContext.accessibleDescription")); // NOI18N
            ifStatementCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ifStatementCombo.AccessibleContext.accessibleName")); // NOI18N
            ifStatementCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ifStatementCombo.AccessibleContext.accessibleDescription")); // NOI18N
            whileStatementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.whileStatementLabel.AccessibleContext.accessibleName")); // NOI18N
            whileStatementLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.whileStatementLabel.AccessibleContext.accessibleDescription")); // NOI18N
            whileStatementComboBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.whileStatementComboBox.AccessibleContext.accessibleName")); // NOI18N
            whileStatementComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.whileStatementComboBox.AccessibleContext.accessibleDescription")); // NOI18N
            doWhileStatementLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.doWhileStatementLabel.AccessibleContext.accessibleName")); // NOI18N
            doWhileStatementLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.doWhileStatementLabel.AccessibleContext.accessibleDescription")); // NOI18N
            doWhileStatementCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.doWhileStatementCombo.AccessibleContext.accessibleName")); // NOI18N
            doWhileStatementCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.doWhileStatementCombo.AccessibleContext.accessibleDescription")); // NOI18N
            binaryOpsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.binaryOpsLabel.AccessibleContext.accessibleName")); // NOI18N
            binaryOpsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.binaryOpsLabel.AccessibleContext.accessibleDescription")); // NOI18N
            binaryOpsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.binaryOpsCombo.AccessibleContext.accessibleName")); // NOI18N
            binaryOpsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.binaryOpsCombo.AccessibleContext.accessibleDescription")); // NOI18N
            ternaryOpsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ternaryOpsLabel.AccessibleContext.accessibleName")); // NOI18N
            ternaryOpsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ternaryOpsLabel.AccessibleContext.accessibleDescription")); // NOI18N
            ternaryOpsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ternaryOpsCombo.AccessibleContext.accessibleName")); // NOI18N
            ternaryOpsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.ternaryOpsCombo.AccessibleContext.accessibleDescription")); // NOI18N
            coalescingOpsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.coalescingOpsLabel.AccessibleContext.accessibleName")); // NOI18N
            coalescingOpsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.coalescingOpsLabel.AccessibleContext.accessibleDescription")); // NOI18N
            assignOpsLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.assignOpsLabel.AccessibleContext.accessibleName")); // NOI18N
            assignOpsLabel.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.assignOpsLabel.AccessibleContext.accessibleDescription")); // NOI18N
            assignOpsCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.assignOpsCombo.AccessibleContext.accessibleName")); // NOI18N
            assignOpsCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.assignOpsCombo.AccessibleContext.accessibleDescription")); // NOI18N
            cbOpenCloseBlockBrace.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.cbOpenCloseBlockBrace.AccessibleContext.accessibleName")); // NOI18N
            cbOpenCloseBlockBrace.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.cbOpenCloseBlockBrace.AccessibleContext.accessibleDescription")); // NOI18N
            cbStatements.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.cbStatements.AccessibleContext.accessibleName")); // NOI18N
            cbStatements.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.cbStatements.AccessibleContext.accessibleDescription")); // NOI18N

            scrollPane.setViewportView(panel1);
            panel1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.panel1.AccessibleContext.accessibleName")); // NOI18N
            panel1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.panel1.AccessibleContext.accessibleDescription")); // NOI18N

            add(scrollPane, BorderLayout.CENTER);
            scrollPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.scrollPane.AccessibleContext.accessibleName")); // NOI18N
            scrollPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.scrollPane.AccessibleContext.accessibleDescription")); // NOI18N

            getAccessibleContext().setAccessibleName(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.AccessibleContext.accessibleName")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(FmtWrapping.class, "FmtWrapping.AccessibleContext.accessibleDescription")); // NOI18N
        }// </editor-fold>//GEN-END:initComponents

    private void cbStatementsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_cbStatementsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbStatementsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox arrayInitCombo;
    private JLabel arrayInitLabel;
    private JComboBox assignOpsCombo;
    private JLabel assignOpsLabel;
    private JComboBox binaryOpsCombo;
    private JLabel binaryOpsLabel;
    private JCheckBox cbOpenCloseBlockBrace;
    private JCheckBox cbStatements;
    private JComboBox chainedMethodCallsCombo;
    private JLabel chainedMethodCallsLabel;
    private JComboBox<String> coalescingOpsCombo;
    private JLabel coalescingOpsLabel;
    private JComboBox doWhileStatementCombo;
    private JLabel doWhileStatementLabel;
    private JComboBox extendsImplementsKeywordCombo;
    private JComboBox extendsImplementsListCombo;
    private JLabel extendsImplementsListLabel;
    private JLabel extendsImplemetsKeywordLabel;
    private JComboBox forCombo;
    private JLabel forLabel;
    private JComboBox forStatementCombo;
    private JLabel forStatementLabel;
    private JCheckBox groupUseBracesCheckBox;
    private JComboBox<String> groupUseListCombo;
    private JLabel groupUseListLabel;
    private JComboBox ifStatementCombo;
    private JLabel ifStatementLabel;
    private JComboBox methodCallArgsCombo;
    private JLabel methodCallArgsLabel;
    private JComboBox methodParamsCombo;
    private JLabel methodParamsLabel;
    private JPanel panel1;
    private JScrollPane scrollPane;
    private JComboBox ternaryOpsCombo;
    private JLabel ternaryOpsLabel;
    private JComboBox whileStatementComboBox;
    private JLabel whileStatementLabel;
    private JCheckBox wrapAfterAssignOpsCheckBox;
    private JCheckBox wrapAfterBinOpsCheckBox;
    private JCheckBox wrapForAfterLeftParenCheckBox;
    private JCheckBox wrapForRightParenCheckBox;
    private JCheckBox wrapMethodCallArgsAfterLeftParenCheckBox;
    private JCheckBox wrapMethodCallArgsRightParenCheckBox;
    private JCheckBox wrapMethodParamsAfterLeftParenCheckBox;
    private JCheckBox wrapMethodParamsKeepParenAndBraceOnSameLineCheckBox;
    private JCheckBox wrapMethodParamsRightParenCheckBox;
    // End of variables declaration//GEN-END:variables

}
