/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.python.source.ui;

import javax.swing.JPanel;
import org.netbeans.modules.options.editor.spi.PreferencesCustomizer;
import static org.netbeans.modules.python.source.ui.FmtOptions.*;
import static org.netbeans.modules.python.source.ui.FmtOptions.CategorySupport.OPTION_ID;

/**
 * Preferences for formatting related to spaces
 * 
 * @author  Tor Norbye
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
