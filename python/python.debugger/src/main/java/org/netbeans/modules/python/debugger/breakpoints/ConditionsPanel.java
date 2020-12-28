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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.python.debugger.breakpoints;

import java.awt.Dimension;

import javax.swing.JEditorPane;
import org.netbeans.api.debugger.Breakpoint.HIT_COUNT_FILTERING_STYLE;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.modules.python.debugger.gui.WatchPanel;

/**
 * Panel for Python breakpoint conditions
 * 
 * @author  Jean-Yves Mengant
 */
public class ConditionsPanel extends javax.swing.JPanel {

  /** Creates new form ConditionsPanel */
  public ConditionsPanel(String helpId) {
    initComponents();
    HelpCtx.setHelpIDString(tfCondition, helpId);
    tfConditionFieldForUI = new javax.swing.JTextField();
    tfConditionFieldForUI.setEnabled(false);
    tfConditionFieldForUI.setToolTipText(tfCondition.getToolTipText());
    java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    add(tfConditionFieldForUI, gridBagConstraints);

    conditionCheckBoxActionPerformed(null);
    cbWhenHitCountActionPerformed(null);
    int preferredHeight = tfConditionFieldForUI.getPreferredSize().height;
    if (spCondition.getPreferredSize().height > preferredHeight) {
      preferredHeight = spCondition.getPreferredSize().height;
      tfConditionFieldForUI.setPreferredSize(new java.awt.Dimension(tfConditionFieldForUI.getPreferredSize().width, preferredHeight));
    }
    tfHitCountFilter.setPreferredSize(
            new Dimension(8 * tfHitCountFilter.getFontMetrics(tfHitCountFilter.getFont()).charWidth('8'),
            tfHitCountFilter.getPreferredSize().height));
    cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
              NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.equals"), // NOI18N
              NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.greater"), // NOI18N
              NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.multiple") // NOI18N
            }));
  }

  // Data Show:
  public void showCondition(boolean show) {
    conditionCheckBox.setVisible(show);
    if (show) {
      conditionCheckBoxActionPerformed(null);
    } else {
      spCondition.setVisible(show);
      tfCondition.setVisible(show);
      tfConditionFieldForUI.setVisible(show);
    }
  }

  public void setCondition(String condition) {
    if (condition == null) {
      return;
    }
    tfCondition.setText(condition);
    conditionCheckBox.setSelected(condition.length() > 0);
    conditionCheckBoxActionPerformed(null);
  }

  public void setupConditionPaneContext(String url, int line) {
    WatchPanel.setupContext(tfCondition, url, line);
  }

  public void setHitCountFilteringStyle(HIT_COUNT_FILTERING_STYLE style) {
    cbHitStyle.setSelectedIndex((style != null) ? style.ordinal() : 0);
  }

  public void setHitCount(int hitCount) {
    if (hitCount > 0) {
      cbWhenHitCount.setSelected(true);
      tfHitCountFilter.setText(Integer.toString(hitCount));
    } else {
      cbWhenHitCount.setSelected(false);
      tfHitCountFilter.setText("");
    }
    cbWhenHitCountActionPerformed(null);
  }

  public String getCondition() {
    if (conditionCheckBox.isSelected()) {
      return tfCondition.getText().trim();
    } else {
      return "";
    }
  }

  public HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle() {
    if (!cbWhenHitCount.isSelected()) {
      return null;
    } else {
      return HIT_COUNT_FILTERING_STYLE.values()[cbHitStyle.getSelectedIndex()];
    }
  }

  public int getHitCount() {
    if (!cbWhenHitCount.isSelected()) {
      return 0;
    }
    String hcfStr = tfHitCountFilter.getText().trim();
    try {
      int hitCount = Integer.parseInt(hcfStr);
      return hitCount;
    } catch (NumberFormatException nfex) {
      return 0;
    }
  }

  public String validateMsg() {
    String hcfStr = tfHitCountFilter.getText().trim();
    if (cbWhenHitCount.isSelected()) {
      if (hcfStr.length() > 0) {
        int hitCountFilter;
        try {
          hitCountFilter = Integer.parseInt(hcfStr);
        } catch (NumberFormatException e) {
          return NbBundle.getMessage(ConditionsPanel.class, "MSG_Bad_Hit_Count_Filter_Spec", hcfStr);
        }
        if (hitCountFilter <= 0) {
          return NbBundle.getMessage(ConditionsPanel.class, "MSG_NonPositive_Hit_Count_Filter_Spec");
        }
      } else {
        return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Hit_Count_Filter_Spec");
      }
    }
    if (conditionCheckBox.isSelected() && tfCondition.getText().trim().length() == 0) {
      return NbBundle.getMessage(ConditionsPanel.class, "MSG_No_Condition_Spec");
    }
    return null;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    conditionCheckBox = new javax.swing.JCheckBox();
    panelHitCountFilter = new javax.swing.JPanel();
    tfHitCountFilter = new javax.swing.JTextField();
    cbHitStyle = new javax.swing.JComboBox();
    cbWhenHitCount = new javax.swing.JCheckBox();
    spCondition = new javax.swing.JScrollPane();
    tfCondition = new JEditorPane("text/x-java", "");

    setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "L_Conditions_Breakpoint_BorderTitle"))); // NOI18N
    setLayout(new java.awt.GridBagLayout());

    org.openide.awt.Mnemonics.setLocalizedText(conditionCheckBox, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.conditionCheckBox.text")); // NOI18N
    conditionCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_TF_Line_Breakpoint_Condition")); // NOI18N
    conditionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    conditionCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        conditionCheckBoxActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    add(conditionCheckBox, gridBagConstraints);

    panelHitCountFilter.setLayout(new java.awt.GridBagLayout());

    tfHitCountFilter.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_TF_Hit_Count")); // NOI18N
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    panelHitCountFilter.add(tfHitCountFilter, gridBagConstraints);
    tfHitCountFilter.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSN_HitCountTF")); // NOI18N

    cbHitStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "equals to", "is greater then", "is multiple of" }));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.RELATIVE;
    panelHitCountFilter.add(cbHitStyle, gridBagConstraints);
    cbHitStyle.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSN_CB_HitCount")); // NOI18N
    cbHitStyle.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_CB_HitCount")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(cbWhenHitCount, org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.cbWhenHitCount.text")); // NOI18N
    cbWhenHitCount.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "TTT_TF_Hit_Count")); // NOI18N
    cbWhenHitCount.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
    cbWhenHitCount.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbWhenHitCountActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    panelHitCountFilter.add(cbWhenHitCount, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    add(panelHitCountFilter, gridBagConstraints);

    spCondition = WatchPanel.createScrollableLineEditor(tfCondition);
    spCondition.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.spCondition.toolTipText")); // NOI18N

    tfCondition.setContentType("text/x-java");
    tfCondition.setToolTipText(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ConditionsPanel.tfCondition.toolTipText")); // NOI18N
    spCondition.setViewportView(tfCondition);
    tfCondition.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSN_ConditionTF")); // NOI18N

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    add(spCondition, gridBagConstraints);

    getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ConditionsPanel.class, "ACSD_Conditions")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents

private void cbWhenHitCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbWhenHitCountActionPerformed
  boolean isSelected = cbWhenHitCount.isSelected();
  cbHitStyle.setEnabled(isSelected);
  tfHitCountFilter.setEnabled(isSelected);
}//GEN-LAST:event_cbWhenHitCountActionPerformed

private void conditionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_conditionCheckBoxActionPerformed
  boolean isSelected = conditionCheckBox.isSelected();
  if (isSelected) {
    spCondition.setVisible(true);
    tfConditionFieldForUI.setVisible(false);
    tfCondition.requestFocusInWindow();
  } else {
    spCondition.setVisible(false);
    tfConditionFieldForUI.setText(tfCondition.getText());
    tfConditionFieldForUI.setVisible(true);
  }
  revalidate();
  repaint();
}//GEN-LAST:event_conditionCheckBoxActionPerformed
  private javax.swing.JTextField tfConditionFieldForUI;
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox cbHitStyle;
  private javax.swing.JCheckBox cbWhenHitCount;
  private javax.swing.JCheckBox conditionCheckBox;
  private javax.swing.JPanel panelHitCountFilter;
  private javax.swing.JScrollPane spCondition;
  private javax.swing.JEditorPane tfCondition;
  private javax.swing.JTextField tfHitCountFilter;
  // End of variables declaration//GEN-END:variables
}
