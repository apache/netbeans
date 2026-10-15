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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import org.netbeans.modules.php.editor.CodeUtils;
import static org.netbeans.modules.php.editor.indent.FmtOptions.*;
import org.netbeans.modules.php.editor.indent.FmtOptions.CategorySupport;
import static org.netbeans.modules.php.editor.indent.FmtOptions.CategorySupport.OPTION_ID;


/**
 *
 * @author  phrebejk
 */
public class FmtAlignment extends javax.swing.JPanel {

    @StaticResource
    private static final String PREVIEW_FILE = "org/netbeans/modules/php/editor/indent/ui/Spaces.php"; // NOI18N
    private static final Logger LOGGER = Logger.getLogger(FmtAlignment.class.getName());

    public FmtAlignment() {
        initComponents();
        nlElseCheckBox.putClientProperty(OPTION_ID, PLACE_ELSE_ON_NEW_LINE);
        nlWhileCheckBox.putClientProperty(OPTION_ID, PLACE_WHILE_ON_NEW_LINE);
        nlCatchCheckBox.putClientProperty(OPTION_ID, PLACE_CATCH_ON_NEW_LINE);
        nlFinallyCheckBox.putClientProperty(OPTION_ID, PLACE_FINALLY_ON_NEW_LINE);
        nlModifiersCheckBox.putClientProperty(OPTION_ID, PLACE_NEW_LINE_AFTER_MODIFIERS);
        amMethodParamsCheckBox.putClientProperty(OPTION_ID, ALIGN_MULTILINE_METHOD_PARAMS);
        amCallArgsCheckBox.putClientProperty(OPTION_ID, ALIGN_MULTILINE_CALL_ARGS);
        amImplementsCheckBox1.putClientProperty(OPTION_ID, ALIGN_MULTILINE_IMPLEMENTS);

        amArrayInitCheckBox1.putClientProperty(OPTION_ID, ALIGN_MULTILINE_ARRAY_INIT);
        amArrayInitCheckBox1.setVisible(false);
        amAssignCheckBox1.putClientProperty(OPTION_ID, ALIGN_MULTILINE_ASSIGNMENT);
        amAssignCheckBox1.setVisible(false);
        amBinaryOpCheckBox1.putClientProperty(OPTION_ID, ALIGN_MULTILINE_BINARY_OP);
        amBinaryOpCheckBox1.setVisible(false);
        amForCheckBox1.putClientProperty(OPTION_ID, ALIGN_MULTILINE_FOR);
        amForCheckBox1.setVisible(false);
        amParenthesizedCheckBox1.putClientProperty(OPTION_ID, ALIGN_MULTILINE_PARENTHESIZED);
        amParenthesizedCheckBox1.setVisible(false);
        amTernaryOpCheckBox1.putClientProperty(OPTION_ID, ALIGN_MULTILINE_TERNARY_OP);
        amTernaryOpCheckBox1.setVisible(false);

        gmlAssignmentCheckBox.putClientProperty(OPTION_ID, GROUP_ALIGNMENT_ASSIGNMENT);
        gmlArrayInitializerCheckBox.putClientProperty(OPTION_ID, GROUP_ALIGNMENT_ARRAY_INIT);
        gmlMatchArmArrowCheckBox.putClientProperty(OPTION_ID, GROUP_ALIGNMENT_MATCH_ARM_ARROW);
    }

    public static PreferencesCustomizer.Factory getController() {
        String preview = CodeUtils.EMPTY_STRING;
        try {
            preview = Utils.loadPreviewText(FmtBlankLines.class.getClassLoader().getResourceAsStream(PREVIEW_FILE));
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return new CategorySupport.Factory("alignment", FmtAlignment.class, preview); // NOI18N
//        return new CategorySupport.Factory("alignment", FmtAlignment.class, //NOI18N
//                org.openide.util.NbBundle.getMessage(FmtAlignment.class, "SAMPLE_AlignBraces"), // NOI18N
//                new String[] { FmtOptions.wrapAnnotations, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapArrayInit, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapAssert, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapAssignOps, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapBinaryOps, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapChainedMethodCalls, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapDoWhileStatement, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapEnumConstants, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapExtendsImplementsKeyword, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapExtendsImplementsList, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapFor, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapForStatement, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapIfStatement, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapMethodCallArgs, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapAnnotationArgs, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapMethodParams, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapTernaryOps, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapThrowsKeyword, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapThrowsList, WrapStyle.WRAP_ALWAYS.name() },
//                new String[] { FmtOptions.wrapWhileStatement, WrapStyle.WRAP_ALWAYS.name() }  );
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newLinesLabel = new javax.swing.JLabel();
        nlElseCheckBox = new javax.swing.JCheckBox();
        nlWhileCheckBox = new javax.swing.JCheckBox();
        nlCatchCheckBox = new javax.swing.JCheckBox();
        nlModifiersCheckBox = new javax.swing.JCheckBox();
        multilineAlignmentLabel = new javax.swing.JLabel();
        amMethodParamsCheckBox = new javax.swing.JCheckBox();
        amCallArgsCheckBox = new javax.swing.JCheckBox();
        amImplementsCheckBox1 = new javax.swing.JCheckBox();
        amArrayInitCheckBox1 = new javax.swing.JCheckBox();
        amBinaryOpCheckBox1 = new javax.swing.JCheckBox();
        amTernaryOpCheckBox1 = new javax.swing.JCheckBox();
        amAssignCheckBox1 = new javax.swing.JCheckBox();
        amForCheckBox1 = new javax.swing.JCheckBox();
        amParenthesizedCheckBox1 = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        groupMultilineAlignmentLabel = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        gmlAssignmentCheckBox = new javax.swing.JCheckBox();
        gmlArrayInitializerCheckBox = new javax.swing.JCheckBox();
        gmlMatchArmArrowCheckBox = new javax.swing.JCheckBox();
        nlFinallyCheckBox = new javax.swing.JCheckBox();
        noteLabel = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_Alignment")); // NOI18N
        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(newLinesLabel, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_al_newLines")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nlElseCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_nl_Else")); // NOI18N
        nlElseCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(nlWhileCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_nl_While")); // NOI18N
        nlWhileCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(nlCatchCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_nl_Catch")); // NOI18N
        nlCatchCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(nlModifiersCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_nl_Modifiers")); // NOI18N
        nlModifiersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(multilineAlignmentLabel, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_al_multilineAlignment")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(amMethodParamsCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_MethodParams")); // NOI18N
        amMethodParamsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amCallArgsCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_CallArgs")); // NOI18N
        amCallArgsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amImplementsCheckBox1, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_an_Implements")); // NOI18N
        amImplementsCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amArrayInitCheckBox1, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_ArrayInit")); // NOI18N
        amArrayInitCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amBinaryOpCheckBox1, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_BinaryOp")); // NOI18N
        amBinaryOpCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amTernaryOpCheckBox1, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_TernaryOp")); // NOI18N
        amTernaryOpCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amAssignCheckBox1, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_Assign")); // NOI18N
        amAssignCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amForCheckBox1, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_For")); // NOI18N
        amForCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(amParenthesizedCheckBox1, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_am_Paren")); // NOI18N
        amParenthesizedCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(groupMultilineAlignmentLabel, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "LBL_al_AccrosMultilineAllignment")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(gmlAssignmentCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.gmlAssignmentCheckBox.text")); // NOI18N
        gmlAssignmentCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        org.openide.awt.Mnemonics.setLocalizedText(gmlArrayInitializerCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.gmlArrayInitializerCheckBox.text")); // NOI18N
        gmlArrayInitializerCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        org.openide.awt.Mnemonics.setLocalizedText(gmlMatchArmArrowCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.gmlMatchArmArrowCheckBox.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(nlFinallyCheckBox, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlFinallyCheckBox.text")); // NOI18N
        nlFinallyCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        org.openide.awt.Mnemonics.setLocalizedText(noteLabel, org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.noteLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(multilineAlignmentLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator2))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(newLinesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(nlWhileCheckBox)
                                            .addComponent(nlElseCheckBox)
                                            .addComponent(nlFinallyCheckBox)
                                            .addComponent(amMethodParamsCheckBox)
                                            .addComponent(amImplementsCheckBox1)
                                            .addComponent(amAssignCheckBox1)
                                            .addComponent(amParenthesizedCheckBox1)
                                            .addComponent(amForCheckBox1))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(nlCatchCheckBox)
                                            .addComponent(nlModifiersCheckBox)
                                            .addComponent(gmlArrayInitializerCheckBox)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(167, 167, 167)
                                        .addComponent(amCallArgsCheckBox))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(167, 167, 167)
                                        .addComponent(amBinaryOpCheckBox1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(167, 167, 167)
                                        .addComponent(amArrayInitCheckBox1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(167, 167, 167)
                                        .addComponent(amTernaryOpCheckBox1)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 6, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(groupMultilineAlignmentLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator3))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(gmlMatchArmArrowCheckBox)
                                    .addComponent(gmlAssignmentCheckBox))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(newLinesLabel)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nlModifiersCheckBox)
                    .addComponent(nlElseCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nlWhileCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nlFinallyCheckBox)
                    .addComponent(nlCatchCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(groupMultilineAlignmentLabel)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gmlAssignmentCheckBox)
                    .addComponent(gmlArrayInitializerCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gmlMatchArmArrowCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(multilineAlignmentLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(amMethodParamsCheckBox)
                    .addComponent(amCallArgsCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(amImplementsCheckBox1)
                    .addComponent(amBinaryOpCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(amAssignCheckBox1)
                    .addComponent(amArrayInitCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(amParenthesizedCheckBox1)
                    .addComponent(amTernaryOpCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(amForCheckBox1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(noteLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        newLinesLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.newLinesLabel.AccessibleContext.accessibleName")); // NOI18N
        newLinesLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.newLinesLabel.AccessibleContext.accessibleDescription")); // NOI18N
        nlElseCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlElseCheckBox.AccessibleContext.accessibleName")); // NOI18N
        nlElseCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlElseCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        nlWhileCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlWhileCheckBox.AccessibleContext.accessibleName")); // NOI18N
        nlWhileCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlWhileCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        nlCatchCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlCatchCheckBox.AccessibleContext.accessibleName")); // NOI18N
        nlCatchCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlCatchCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        nlModifiersCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlModifiersCheckBox.AccessibleContext.accessibleName")); // NOI18N
        nlModifiersCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.nlModifiersCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        multilineAlignmentLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.multilineAlignmentLabel.AccessibleContext.accessibleName")); // NOI18N
        multilineAlignmentLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.multilineAlignmentLabel.AccessibleContext.accessibleDescription")); // NOI18N
        amMethodParamsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amMethodParamsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        amMethodParamsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amMethodParamsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        amCallArgsCheckBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amCallArgsCheckBox.AccessibleContext.accessibleName")); // NOI18N
        amCallArgsCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amCallArgsCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        amImplementsCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amImplementsCheckBox1.AccessibleContext.accessibleName")); // NOI18N
        amImplementsCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amImplementsCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N
        amArrayInitCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amArrayInitCheckBox1.AccessibleContext.accessibleName")); // NOI18N
        amArrayInitCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amArrayInitCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N
        amBinaryOpCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amBinaryOpCheckBox1.AccessibleContext.accessibleName")); // NOI18N
        amBinaryOpCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amBinaryOpCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N
        amTernaryOpCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amTernaryOpCheckBox1.AccessibleContext.accessibleName")); // NOI18N
        amTernaryOpCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amTernaryOpCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N
        amAssignCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amAssignCheckBox1.AccessibleContext.accessibleName")); // NOI18N
        amAssignCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amAssignCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N
        amForCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amForCheckBox1.AccessibleContext.accessibleName")); // NOI18N
        amForCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amForCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N
        amParenthesizedCheckBox1.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amParenthesizedCheckBox1.AccessibleContext.accessibleName")); // NOI18N
        amParenthesizedCheckBox1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.amParenthesizedCheckBox1.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(FmtAlignment.class, "FmtAlignment.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox amArrayInitCheckBox1;
    private javax.swing.JCheckBox amAssignCheckBox1;
    private javax.swing.JCheckBox amBinaryOpCheckBox1;
    private javax.swing.JCheckBox amCallArgsCheckBox;
    private javax.swing.JCheckBox amForCheckBox1;
    private javax.swing.JCheckBox amImplementsCheckBox1;
    private javax.swing.JCheckBox amMethodParamsCheckBox;
    private javax.swing.JCheckBox amParenthesizedCheckBox1;
    private javax.swing.JCheckBox amTernaryOpCheckBox1;
    private javax.swing.JCheckBox gmlArrayInitializerCheckBox;
    private javax.swing.JCheckBox gmlAssignmentCheckBox;
    private javax.swing.JCheckBox gmlMatchArmArrowCheckBox;
    private javax.swing.JLabel groupMultilineAlignmentLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JLabel multilineAlignmentLabel;
    private javax.swing.JLabel newLinesLabel;
    private javax.swing.JCheckBox nlCatchCheckBox;
    private javax.swing.JCheckBox nlElseCheckBox;
    private javax.swing.JCheckBox nlFinallyCheckBox;
    private javax.swing.JCheckBox nlModifiersCheckBox;
    private javax.swing.JCheckBox nlWhileCheckBox;
    private javax.swing.JLabel noteLabel;
    // End of variables declaration//GEN-END:variables

}
