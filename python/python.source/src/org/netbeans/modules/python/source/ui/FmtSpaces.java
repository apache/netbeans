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

package org.netbeans.modules.python.source.ui;

import javax.swing.JPanel;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import static org.netbeans.modules.python.source.ui.FmtOptions.*;
import static org.netbeans.modules.python.source.ui.FmtOptions.CategorySupport.OPTION_ID;

/**
 * Preferences for formatting related to spaces
 * 
 */
public class FmtSpaces extends JPanel {
    public FmtSpaces() {
        initComponents();

        addAroundOp.putClientProperty(OPTION_ID, addSpaceAroundOperators);
        addAfterComma.putClientProperty(OPTION_ID, addSpaceAfterComma);
        removeBeforeSep.putClientProperty(OPTION_ID, removeSpaceBeforeSep);
        removeInParam.putClientProperty(OPTION_ID, removeSpaceInParamAssign);
        removeInParen.putClientProperty(OPTION_ID, removeSpaceInParens);
        collapseSpacesCb.putClientProperty(OPTION_ID, collapseSpaces);
    }

    public static PreferencesCustomizer.Factory getController() {
        return new CategorySupport.Factory("spaces", FmtSpaces.class, // NOI18N
                org.openide.util.NbBundle.getMessage(FmtSpaces.class, "SAMPLE_Spaces"));
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addAroundOp = new javax.swing.JCheckBox();
        removeInParam = new javax.swing.JCheckBox();
        removeInParen = new javax.swing.JCheckBox();
        addAfterComma = new javax.swing.JCheckBox();
        removeBeforeSep = new javax.swing.JCheckBox();
        collapseSpacesCb = new javax.swing.JCheckBox();

        setName(org.openide.util.NbBundle.getMessage(FmtSpaces.class, "LBL_Spaces")); // NOI18N
        setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(addAroundOp, org.openide.util.NbBundle.getMessage(FmtSpaces.class, "FmtSpaces.addAroundOp.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeInParam, org.openide.util.NbBundle.getMessage(FmtSpaces.class, "FmtSpaces.removeInParam.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeInParen, org.openide.util.NbBundle.getMessage(FmtSpaces.class, "FmtSpaces.removeInParen.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(addAfterComma, org.openide.util.NbBundle.getMessage(FmtSpaces.class, "FmtSpaces.addAfterComma.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(removeBeforeSep, org.openide.util.NbBundle.getMessage(FmtSpaces.class, "FmtSpaces.removeBeforeSep.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(collapseSpacesCb, org.openide.util.NbBundle.getMessage(FmtSpaces.class, "FmtSpaces.collapseSpacesCb.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addAroundOp)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(removeInParam))
                    .addComponent(removeInParen)
                    .addComponent(addAfterComma)
                    .addComponent(removeBeforeSep)
                    .addComponent(collapseSpacesCb))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(addAroundOp)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeInParam)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeInParen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addAfterComma)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeBeforeSep)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(collapseSpacesCb)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox addAfterComma;
    private javax.swing.JCheckBox addAroundOp;
    private javax.swing.JCheckBox collapseSpacesCb;
    private javax.swing.JCheckBox removeBeforeSep;
    private javax.swing.JCheckBox removeInParam;
    private javax.swing.JCheckBox removeInParen;
    // End of variables declaration//GEN-END:variables
    
}
