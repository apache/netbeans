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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.mercurial.ui.log;

import org.netbeans.modules.mercurial.HgModuleConfig;
import org.openide.util.NbBundle;

/**
 * Packages search criteria in Search History panel.
 *
 * @author Maros Sandor
 */
class SearchCriteriaPanel extends javax.swing.JPanel {
    
    /** Creates new form SearchCriteriaPanel */
    public SearchCriteriaPanel() {
        initComponents();
        showMergesChkBox.setSelected(HgModuleConfig.getDefault().getShowHistoryMerges());
        tfLimit.setText(Integer.toString(SearchExecutor.DEFAULT_LIMIT));
    }

    public String getFrom() {
        String s = tfFrom.getText().trim();
        if(s.length() == 0) {
            return null;
        }
        return s;
    }

    public String getTo() {
        String s = tfTo.getText().trim();
        if(s.length() == 0) {
            return null;
        }
        return s;
    }

    /**
     *
     * @return limit for shown changesets, -1 for no limit
     */
    public int getLimit() {
        String s = tfLimit.getText().trim();
        Integer retval = -1;
        try {
            retval = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            retval = -1;
        }
        if (retval <= 0) {
            retval = -1;
        }
        return retval;
    }

    void setForIncoming() {
        fromInfoLabel.setText(NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_FromToOutOrIncomingHint"));
        toInfoLabel.setText(NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_FromToOutOrIncomingHint"));
        fromLabel.setToolTipText(NbBundle.getMessage(SearchHistoryPanel.class,  "TT_IncomingFrom"));
        toLabel.setToolTipText(NbBundle.getMessage(SearchHistoryPanel.class,  "TT_IncomingTo"));
        showMergesChkBox.setToolTipText(NbBundle.getMessage(SearchHistoryPanel.class,  "TT_IncomingShowMerges"));

        tfFrom.setText(NbBundle.getMessage(SearchHistoryPanel.class,  "TTF_IncomingFrom"));
        tfFrom.setEnabled(false);
    }
    
    void setForOut() {
        fromInfoLabel.setText(NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_FromToOutOrIncomingHint"));
        toInfoLabel.setText(NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_FromToOutOrIncomingHint"));
        fromLabel.setToolTipText(NbBundle.getMessage(SearchHistoryPanel.class,  "TT_OutFrom"));
        toLabel.setToolTipText(NbBundle.getMessage(SearchHistoryPanel.class,  "TT_OutTo"));
        showMergesChkBox.setToolTipText(NbBundle.getMessage(SearchHistoryPanel.class,  "TT_OutShowMerges"));
        
        tfFrom.setText(NbBundle.getMessage(SearchHistoryPanel.class,  "TTF_OutFrom"));
        tfFrom.setEnabled(false);
    }

    public void setFrom(String from) {
        if (from == null) from = "";  // NOI18N
        tfFrom.setText(from);
    }

    public void setTo(String to) {
        if (to == null) to = "";  // NOI18N
        tfTo.setText(to);
    }
    
    void setLimit (int limit) {
        if (limit > 0) {
            tfLimit.setText(Integer.toString(limit));
        } else {
            tfLimit.setText("");
        }
    }

    public String getBranch () {
        return tfBranch.getText().trim();
    }
    
    public void setBranch (String branchName) {
        tfBranch.setText(branchName);
    }
    
    boolean isIncludeMerges() {
        return showMergesChkBox.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fromLabel = new javax.swing.JLabel();
        fromInfoLabel = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        toInfoLabel = new javax.swing.JLabel();
        toLabel1 = new javax.swing.JLabel();
        toLabel2 = new javax.swing.JLabel();
        showMergesChkBox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 12, 0, 11));

        fromLabel.setLabelFor(tfFrom);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/mercurial/ui/log/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(fromLabel, bundle.getString("CTL_UseFrom")); // NOI18N
        fromLabel.setToolTipText(bundle.getString("TT_From")); // NOI18N

        tfFrom.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(fromInfoLabel, bundle.getString("CTL_FromToHint")); // NOI18N

        toLabel.setLabelFor(tfTo);
        org.openide.awt.Mnemonics.setLocalizedText(toLabel, bundle.getString("CTL_UseTo")); // NOI18N
        toLabel.setToolTipText(bundle.getString("TT_To")); // NOI18N

        tfTo.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(toInfoLabel, bundle.getString("CTL_FromToHint")); // NOI18N

        toLabel1.setLabelFor(tfLimit);
        org.openide.awt.Mnemonics.setLocalizedText(toLabel1, bundle.getString("CTL_UseLimit")); // NOI18N
        toLabel1.setToolTipText(bundle.getString("TT_Limit")); // NOI18N

        tfLimit.setColumns(10);

        toLabel2.setLabelFor(tfBranch);
        org.openide.awt.Mnemonics.setLocalizedText(toLabel2, bundle.getString("CTL_UseBranch")); // NOI18N
        toLabel2.setToolTipText(bundle.getString("TT_Branch")); // NOI18N

        tfBranch.setColumns(10);

        org.openide.awt.Mnemonics.setLocalizedText(btnSelectBranch, org.openide.util.NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_SelectBranch")); // NOI18N
        btnSelectBranch.setToolTipText(org.openide.util.NbBundle.getMessage(SearchCriteriaPanel.class, "TT_SelectBranch")); // NOI18N

        showMergesChkBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(showMergesChkBox, org.openide.util.NbBundle.getMessage(SearchCriteriaPanel.class, "CTL_ShowMerge")); // NOI18N
        showMergesChkBox.setToolTipText(org.openide.util.NbBundle.getMessage(SearchCriteriaPanel.class, "TT_ShowMerges")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(toLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(toLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tfLimit, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(showMergesChkBox))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(tfBranch)
                                .addGap(18, 18, 18)
                                .addComponent(btnSelectBranch)))
                        .addGap(30, 30, 30)
                        .addComponent(fromLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(toLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toInfoLabel)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tfFrom)
                        .addComponent(fromInfoLabel)
                        .addComponent(tfTo)))
                .addGap(14, 14, 14))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fromLabel)
                    .addComponent(tfFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fromInfoLabel)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toInfoLabel)
                .addContainerGap(28, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(toLabel2)
                    .addComponent(btnSelectBranch)
                    .addComponent(tfBranch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(showMergesChkBox, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(toLabel1)
                    .addComponent(tfLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton btnSelectBranch = new javax.swing.JButton();
    private javax.swing.JLabel fromInfoLabel;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JCheckBox showMergesChkBox;
    final javax.swing.JTextField tfBranch = new javax.swing.JTextField();
    final javax.swing.JTextField tfFrom = new javax.swing.JTextField();
    final javax.swing.JTextField tfLimit = new javax.swing.JTextField();
    final javax.swing.JTextField tfTo = new javax.swing.JTextField();
    private javax.swing.JLabel toInfoLabel;
    private javax.swing.JLabel toLabel;
    private javax.swing.JLabel toLabel1;
    private javax.swing.JLabel toLabel2;
    // End of variables declaration//GEN-END:variables
    
}
