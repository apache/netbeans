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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.project.Project;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsFromGroup;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsFromGroup.TestGroupProperties;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.BreakpointsExpandableGroup.FileItem;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.BreakpointsExpandableGroup.FilesGroup;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.BreakpointsExpandableGroup.ProjectItem;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.BreakpointsExpandableGroup.ProjectsGroup;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.BreakpointsExpandableGroup.TypeItem;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.BreakpointsExpandableGroup.TypesGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author  jj97931
 */
public class ActionsPanel extends javax.swing.JPanel {

    // [TODO] move property name constant to JPDABreakpoint
    private static final String DEFAULT_SUSPEND_ACTION = "default.suspend.action"; // NOI18N
    
    private JPDABreakpoint  breakpoint;
    private int defaultSuspendAction;
    private int checkedSuspendAction;
    private Preferences preferences = NbPreferences.forModule(JPDABreakpoint.class).node("debugging"); // NOI18N
    private static final Object NONE_BREAKPOINT_GROUP = new NoneBreakpointGroup();

    /** Creates new form LineBreakpointPanel */
    public ActionsPanel (JPDABreakpoint b) {
        breakpoint = b;
        initComponents ();

        ResourceBundle bundle = NbBundle.getBundle(ActionsPanel.class);
        org.openide.awt.Mnemonics.setLocalizedText(defaultActionCheckBox, bundle.getString("LBL_Use_As_Default_Option")); // NOI18N
        defaultActionCheckBox.setToolTipText(bundle.getString("TTT_Use_As_Default_Option"));
        checkBoxPanel.setPreferredSize(defaultActionCheckBox.getPreferredSize());
        
        cbSuspend.addItem (bundle.getString("LBL_CB_Actions_Panel_Suspend_None"));
        cbSuspend.addItem (bundle.getString("LBL_CB_Actions_Panel_Suspend_Current"));
        cbSuspend.addItem (bundle.getString("LBL_CB_Actions_Panel_Suspend_All"));
        switch (b.getSuspend ()) {
            case JPDABreakpoint.SUSPEND_NONE:
                cbSuspend.setSelectedIndex (0);
                break;
            case JPDABreakpoint.SUSPEND_EVENT_THREAD:
                cbSuspend.setSelectedIndex (1);
                break;
            case JPDABreakpoint.SUSPEND_ALL:
                cbSuspend.setSelectedIndex (2);
                break;
        }
        defaultSuspendAction = preferences.getInt(DEFAULT_SUSPEND_ACTION, 1);
        checkedSuspendAction = defaultSuspendAction;
        
        if (defaultSuspendAction == cbSuspend.getSelectedIndex()) {
            defaultActionCheckBox.setVisible(false);
        } else {
            defaultActionCheckBox.setVisible(true);
            defaultActionCheckBox.setSelected(false);
        }
        
        if (b.getPrintText () != null)
            tfPrintText.setText (b.getPrintText ());
        tfPrintText.setPreferredSize(new Dimension(
                30*tfPrintText.getFontMetrics(tfPrintText.getFont()).charWidth('W'),
                tfPrintText.getPreferredSize().height));
        tfPrintText.setCaretPosition(0);
        
        enableGroupCheckBox.setVisible(false);
        disableGroupCheckBox.setVisible(false);
        Object[] groups = getGroups();
        Set<Breakpoint> breakpointsToEnable = breakpoint.getBreakpointsToEnable();
        Set<Breakpoint> breakpointsToDisable = breakpoint.getBreakpointsToDisable();
        BreakpointsFromGroup bfgToEnable = null;
        BreakpointsFromGroup bfgToDisable = null;
        if (breakpointsToEnable instanceof BreakpointsFromGroup) {
            bfgToEnable = (BreakpointsFromGroup) breakpointsToEnable;
        }
        if (breakpointsToDisable instanceof BreakpointsFromGroup) {
            bfgToDisable = (BreakpointsFromGroup) breakpointsToDisable;
        }
        fillGroups((OutlineComboBox) enableGroupComboBox, groups, bfgToEnable);
        fillGroups((OutlineComboBox) disableGroupComboBox, groups, bfgToDisable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        cbSuspend = new javax.swing.JComboBox();
        checkBoxPanel = new javax.swing.JPanel();
        defaultActionCheckBox = new javax.swing.JCheckBox();
        enableGroupCheckBox = new javax.swing.JCheckBox();
        enableGroupLabel = new javax.swing.JLabel();
        enableGroupComboBox = new OutlineComboBox();
        disableGroupCheckBox = new javax.swing.JCheckBox();
        disableGroupLabel = new javax.swing.JLabel();
        disableGroupComboBox = new OutlineComboBox();
        jLabel2 = new javax.swing.JLabel();
        tfPrintText = new javax.swing.JTextField();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Actions_Panel_BorderTitle"))); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(cbSuspend);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("L_Actions_Panel_Suspend")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("ASCD_L_Actions_Panel_Suspend")); // NOI18N

        cbSuspend.setToolTipText(bundle.getString("TTT_CB_Actions_Panel_Suspend")); // NOI18N
        cbSuspend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbSuspendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(cbSuspend, gridBagConstraints);
        cbSuspend.getAccessibleContext().setAccessibleDescription(bundle.getString("ASCD_CB_Actions_Panel_Suspend")); // NOI18N

        checkBoxPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(defaultActionCheckBox, "jCheckBox1");
        defaultActionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultActionCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        checkBoxPanel.add(defaultActionCheckBox, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(checkBoxPanel, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(enableGroupCheckBox, org.openide.util.NbBundle.getMessage(ActionsPanel.class, "MSG_EnableGroup")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(enableGroupCheckBox, gridBagConstraints);

        enableGroupLabel.setLabelFor(enableGroupComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(enableGroupLabel, org.openide.util.NbBundle.getMessage(ActionsPanel.class, "MSG_EnableGroup")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(enableGroupLabel, gridBagConstraints);

        enableGroupComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(enableGroupComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(disableGroupCheckBox, org.openide.util.NbBundle.getMessage(ActionsPanel.class, "MSG_DisableGroup")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(disableGroupCheckBox, gridBagConstraints);

        disableGroupLabel.setLabelFor(disableGroupComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(disableGroupLabel, org.openide.util.NbBundle.getMessage(ActionsPanel.class, "MSG_DisableGroup")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(disableGroupLabel, gridBagConstraints);

        disableGroupComboBox.setEditable(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 3, 3);
        add(disableGroupComboBox, gridBagConstraints);

        jLabel2.setLabelFor(tfPrintText);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("L_Actions_Panel_Print_Text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionsPanel.class, "ACSD_PrintText")); // NOI18N

        tfPrintText.setToolTipText(bundle.getString("TTT_TF_Actions_Panel_Print_Text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(tfPrintText, gridBagConstraints);
        tfPrintText.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Actions_Panel_Print_Text")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ActionsPanel.class, "ACSD_Actions")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void defaultActionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultActionCheckBoxActionPerformed
    checkedSuspendAction = cbSuspend.getSelectedIndex();
}//GEN-LAST:event_defaultActionCheckBoxActionPerformed

private void cbSuspendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSuspendActionPerformed
    int selectedIndex = cbSuspend.getSelectedIndex();
    if (defaultSuspendAction == selectedIndex) {
        defaultActionCheckBox.setVisible(false);
    } else {
        defaultActionCheckBox.setVisible(true);
        defaultActionCheckBox.setSelected(false);
    }
    checkedSuspendAction = defaultSuspendAction;
}//GEN-LAST:event_cbSuspendActionPerformed
    
    /**
     * Called when "Ok" button is pressed.
     */
    public void ok () {
        String printText = tfPrintText.getText ();
        if (printText.trim ().length () > 0)
            breakpoint.setPrintText (printText.trim ());
        else
            breakpoint.setPrintText (null);
        
        switch (cbSuspend.getSelectedIndex ()) {
            case 0:
                breakpoint.setSuspend (JPDABreakpoint.SUSPEND_NONE);
                break;
            case 1:
                breakpoint.setSuspend (JPDABreakpoint.SUSPEND_EVENT_THREAD);
                break;
            case 2:
                breakpoint.setSuspend (JPDABreakpoint.SUSPEND_ALL);
                break;
        }
        if (checkedSuspendAction != defaultSuspendAction) {
            preferences.putInt(DEFAULT_SUSPEND_ACTION, checkedSuspendAction);
        }
        Object breakpointsToEnableGroup = enableGroupComboBox.getSelectedItem();
        breakpoint.setBreakpointsToEnable(createBreakpointsSet(breakpointsToEnableGroup));
        Object breakpointsToDisableGroup = disableGroupComboBox.getSelectedItem();
        breakpoint.setBreakpointsToDisable(createBreakpointsSet(breakpointsToDisableGroup));
        /*
        if (breakpointsToDisableGroup == null || breakpointsToDisableGroup == NONE_BREAKPOINT_GROUP) {
            breakpoint.setBreakpointsToDisable(Collections.EMPTY_SET);
        } else {
            TestGroupProperties tgp = createTestProperties(breakpointsToDisableGroup);
            if (tgp != null) {
                breakpoint.setBreakpointsToDisable(new BreakpointsFromGroup(tgp));
            } else {
                String customGroup = (String) breakpointsToDisableGroup;
                customGroup = customGroup.trim();
                if (!customGroup.isEmpty()) {
                    breakpoint.setBreakpointsToDisable(new BreakpointsFromGroup(customGroup));
                } else {
                    breakpoint.setBreakpointsToDisable(Collections.EMPTY_SET);
                }
            }
        }
         */
    }
    
    private static Set<Breakpoint> createBreakpointsSet(Object selectedGroup) {
        if (selectedGroup == null || selectedGroup == NONE_BREAKPOINT_GROUP) {
            return Collections.EMPTY_SET;
        } else {
            TestGroupProperties tgp = createTestProperties(selectedGroup);
            if (tgp != null) {
                return new BreakpointsFromGroup(tgp);
            } else {
                String customGroup = (String) selectedGroup;
                customGroup = customGroup.trim();
                if (!customGroup.isEmpty()) {
                    return new BreakpointsFromGroup(customGroup);
                } else {
                    return Collections.EMPTY_SET;
                }
            }
        }
    }
    
    /*
    private static Object getItem(JComboBox cb, BreakpointsFromGroup bfg) {
        String groupName = bfg.getGroupName();
        if (groupName != null) {
            return groupName;
        }
        TestGroupProperties testProperties = bfg.getTestProperties();
        
        FileObject fo = testProperties.getFileObject();
        if (fo != null) {
            
        }
    }
     */
    
    private static TestGroupProperties createTestProperties(Object group) {
        if (group instanceof FileItem) {
            return new TestGroupProperties(((FileItem) group).getFileObject());
        }
        if (group instanceof ProjectItem) {
            return new TestGroupProperties(((ProjectItem) group).getProject());
        }
        if (group instanceof TypeItem) {
            return new TestGroupProperties(((TypeItem) group).getType());
        }
        return null;
    }
    
    private void fillGroupNames(OutlineComboBox cb, Object[] groupNames) {
        //DefaultComboBoxModel cbm = new DefaultComboBoxModel(groupNames);
        cb.setItems(groupNames);
        cb.setSelectedIndex(0);
    }
    
    private Object[] fillGroups(OutlineComboBox cb, Object[] groups, BreakpointsFromGroup groupToSelect) {
        int index = groups.length - 3;
        FilesGroup fg = new FilesGroup();
        ProjectsGroup pg = new ProjectsGroup();
        TypesGroup tg = new TypesGroup();
        groups[index++] = fg;
        groups[index++] = pg;
        groups[index++] = tg;
        cb.setItems(groups);
        if (groupToSelect == null) {
            cb.setSelectedIndex(0);
        } else {
            String groupName = groupToSelect.getGroupName();
            if (groupName != null) {
                cb.setSelectedItem(groupName);
            } else {
                TestGroupProperties tgp = groupToSelect.getTestGroupProperties();
                FileObject fo = tgp.getFileObject();
                if (fo != null) {
                    FileItem[] items = fg.getItems();
                    for (FileItem fi : items) {
                        if (fo.equals(fi.getFileObject())) {
                            cb.getModel().setSelectedItem(fg); // To expand it and fill the items
                            cb.setSelectedItem(fi);
                            break;
                        }
                    }
                }
                Project project = tgp.getProject();
                if (project != null) {
                    ProjectItem[] items = pg.getItems();
                    for (ProjectItem pi : items) {
                        if (project.equals(pi.getProject())) {
                            cb.getModel().setSelectedItem(pg); // To expand it and fill the items
                            cb.setSelectedItem(pi);
                            break;
                        }
                    }
                }
                String type = tgp.getType();
                if (type != null) {
                    TypeItem[] items = tg.getItems();
                    for (TypeItem ti : items) {
                        if (type.equals(ti.getType())) {
                            cb.getModel().setSelectedItem(tg); // To expand it and fill the items
                            cb.setSelectedItem(ti);
                            break;
                        }
                    }
                }
            }
        }
        return groups;
    }
    
    private static String[] getGroupNames() {
        Set<String> groupNamesSorted = new TreeSet<String>();
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().getBreakpoints ();
        for (int i = 0; i < bs.length; i++) {
            String gn = bs[i].getGroupName();
            groupNamesSorted.add(gn);
        }
        groupNamesSorted.remove(""); // Remove the defalt group
        List<String> groupNames = new ArrayList<String>(groupNamesSorted);
        groupNames.add(0, NbBundle.getMessage(ActionsPanel.class, "LBL_NoneBreakpointGroup"));
        return groupNames.toArray(new String[0]);
    }
    
    private static Object[] getGroups() {
        Set<String> groupNamesSorted = new TreeSet<String>();
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().getBreakpoints ();
        for (int i = 0; i < bs.length; i++) {
            String gn = bs[i].getGroupName();
            groupNamesSorted.add(gn);
        }
        groupNamesSorted.remove(""); // Remove the defalt group
        Object[] groups = new Object[1 + groupNamesSorted.size() + 3]; // 3 expandable groups
        groups[0] = NONE_BREAKPOINT_GROUP;
        int i = 1;
        for (String gn : groupNamesSorted) {
            groups[i++] = gn;
        }
        return groups;
    }
    
    private static final class NoneBreakpointGroup {

        @Override
        public String toString() {
            return NbBundle.getMessage(ActionsPanel.class, "LBL_NoneBreakpointGroup");
        }
        
        public static Object valueOf(String newString) {
            if (newString.isEmpty() || NbBundle.getMessage(ActionsPanel.class, "LBL_NoneBreakpointGroup").equals(newString)) {
                return NONE_BREAKPOINT_GROUP;
            } else {
                return newString;
            }
        }
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbSuspend;
    private javax.swing.JPanel checkBoxPanel;
    private javax.swing.JCheckBox defaultActionCheckBox;
    private javax.swing.JCheckBox disableGroupCheckBox;
    private javax.swing.JComboBox disableGroupComboBox;
    private javax.swing.JLabel disableGroupLabel;
    private javax.swing.JCheckBox enableGroupCheckBox;
    private javax.swing.JComboBox enableGroupComboBox;
    private javax.swing.JLabel enableGroupLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JTextField tfPrintText;
    // End of variables declaration//GEN-END:variables
    
}
