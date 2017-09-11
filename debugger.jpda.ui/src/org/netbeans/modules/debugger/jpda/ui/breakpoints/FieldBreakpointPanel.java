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

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ResourceBundle;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.completion.JavaClassNbDebugEditorKit;
import org.netbeans.modules.debugger.jpda.ui.completion.JavaFieldNbDebugEditorKit;
import org.netbeans.spi.debugger.ui.Controller;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * @author  Jan Jancura
 */
// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class FieldBreakpointPanel extends JPanel implements Controller {
// ====
public class FieldBreakpointPanel extends JPanel implements Controller, org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    private static final String         HELP_ID = "debug.add.breakpoint.java.field"; // NOI18N
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private FieldBreakpoint             breakpoint;
    private boolean                     createBreakpoint = false;
    private JEditorPane                 epClassName;
    private JScrollPane                 spClassName;
    private JEditorPane                 epFieldName;
    private JScrollPane                 spFieldName;
    
    private static FieldBreakpoint creteBreakpoint () {
        String className;
        try {
            className = EditorContextBridge.getMostRecentClassName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            className = "";
        }
        String fieldName;
        try {
            fieldName = EditorContextBridge.getMostRecentFieldName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            fieldName = "";
        }
        FieldBreakpoint mb = FieldBreakpoint.create (
            className,
            fieldName,
            FieldBreakpoint.TYPE_ACCESS | FieldBreakpoint.TYPE_MODIFICATION
        );
        mb.setPrintText (
            NbBundle.getBundle (FieldBreakpointPanel.class).getString 
                ("CTL_Field_Breakpoint_Print_Text")
        );
        return mb;
    }
    
    /** Creates new form LineBreakpointPanel */
    public FieldBreakpointPanel () {
        this (creteBreakpoint ());
        createBreakpoint = true;
    }
    
    /** Creates new form LineBreakpointPanel */
    public FieldBreakpointPanel (FieldBreakpoint b) {
        breakpoint = b;
        initComponents();
        
        String className = b.getClassName ();

        ResourceBundle bundle = NbBundle.getBundle(FieldBreakpointPanel.class);
        String tooltipText = bundle.getString("TTT_TF_Field_Breakpoint_Class_Name");
        Pair<JScrollPane, JEditorPane> editorCC = ClassBreakpointPanel.addClassNameEditorCC(JavaClassNbDebugEditorKit.MIME_TYPE, pSettings, className, tooltipText);
        spClassName = editorCC.first();
        epClassName = editorCC.second();
        epClassName.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Method_Breakpoint_ClassName"));
        epClassName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Field_Breakpoint_ClassName"));
        HelpCtx.setHelpIDString(epClassName, HELP_ID);
        jLabel3.setLabelFor(spClassName);
        
        editorCC = ClassBreakpointPanel.addClassNameEditorCC(JavaFieldNbDebugEditorKit.MIME_TYPE, null, b.getFieldName(), bundle.getString("TTT_TF_Field_Breakpoint_Field_Name"));
        spFieldName = editorCC.first();
        epFieldName = editorCC.second();
        
        jLabel1.setLabelFor(spFieldName);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(spFieldName, gridBagConstraints);
        epFieldName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Field_Breakpoint_Field_Name")); // NOI18N
        epFieldName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                epFieldName.getDocument().putProperty("class-name", epClassName.getText());
            }
            @Override
            public void focusLost(FocusEvent e) {}
        });
        
        cbBreakpointType.addItem (bundle.getString("LBL_Field_Breakpoint_Type_Access"));
        cbBreakpointType.addItem (bundle.getString("LBL_Field_Breakpoint_Type_Modification"));
        cbBreakpointType.addItem (bundle.getString("LBL_Field_Breakpoint_Type_Access_or_Modification"));
        switch (b.getBreakpointType ()) {
            case FieldBreakpoint.TYPE_ACCESS:
                cbBreakpointType.setSelectedIndex (0);
                break;
            case FieldBreakpoint.TYPE_MODIFICATION:
                cbBreakpointType.setSelectedIndex (1);
                break;
            case (FieldBreakpoint.TYPE_ACCESS | FieldBreakpoint.TYPE_MODIFICATION):
                cbBreakpointType.setSelectedIndex (2);
                break;
        }
        
        conditionsPanel = new ConditionsPanel(HELP_ID);
        conditionsPanel.setupConditionPaneContext();
        conditionsPanel.showClassFilter(false);
        conditionsPanel.setCondition(b.getCondition());
        conditionsPanel.setHitCountFilteringStyle(b.getHitCountFilteringStyle());
        conditionsPanel.setHitCount(b.getHitCountFilter());
        cPanel.add(conditionsPanel, "Center");
        
        actionsPanel = new ActionsPanel (b);
        pActions.add (actionsPanel, "Center");
        // <RAVE>
        // The help IDs for the AddBreakpointPanel panels have to be different from the
        // values returned by getHelpCtx() because they provide different help
        // in the 'Add Breakpoint' dialog and when invoked in the 'Breakpoints' view
        putClientProperty("HelpID_AddBreakpointPanel", HELP_ID); // NOI18N
        // </RAVE>
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerBreakpointFieldJPDA"); // NOI18N
    }
    // </RAVE>
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbBreakpointType = new javax.swing.JComboBox();
        cPanel = new javax.swing.JPanel();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Field_Breakpoint_BorderTitle"))); // NOI18N
        pSettings.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("L_Field_Breakpoint_Class_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Field_Breakpoint_Class_Name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("L_Field_Breakpoint_Field_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Field_Breakpoint_Field_Name")); // NOI18N

        jLabel4.setLabelFor(cbBreakpointType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, bundle.getString("L_Field_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Field_Breakpoint_Type")); // NOI18N

        cbBreakpointType.setToolTipText(bundle.getString("TTT_CB_Field_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(cbBreakpointType, gridBagConstraints);
        cbBreakpointType.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CB_Field_Breakpoint_Type")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        cPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(cPanel, gridBagConstraints);

        pActions.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pActions, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(FieldBreakpointPanel.class, "ACSN_FieldBreakpoint")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    
    // Controller implementation ...............................................
    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok () {
        String msg = valiadateMsg();
        if (msg == null) {
            msg = conditionsPanel.valiadateMsg();
        }
        if (msg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return false;
        }
        actionsPanel.ok ();
        String className = epClassName.getText ().trim ();
        breakpoint.setClassName (className);
        breakpoint.setFieldName (epFieldName.getText ().trim ());
        switch (cbBreakpointType.getSelectedIndex ()) {
            case 0:
                breakpoint.setBreakpointType (FieldBreakpoint.TYPE_ACCESS);
                break;
            case 1:
                breakpoint.setBreakpointType (FieldBreakpoint.TYPE_MODIFICATION);
                break;
            case 2:
                breakpoint.setBreakpointType (FieldBreakpoint.TYPE_ACCESS | FieldBreakpoint.TYPE_MODIFICATION);
                break;
        }
        breakpoint.setCondition (conditionsPanel.getCondition());
        breakpoint.setHitCountFilter(conditionsPanel.getHitCount(),
                conditionsPanel.getHitCountFilteringStyle());
        
        if (createBreakpoint) 
            DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        return true;
    }
    
    /**
     * Called when "Cancel" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean cancel () {
        return true;
    }
    
    private String valiadateMsg () {
        if (epClassName.getText().trim ().length() == 0 || epFieldName.getText().trim ().length() == 0) {
            return NbBundle.getMessage(FieldBreakpointPanel.class, "MSG_No_Class_or_Field_Name_Spec");
        }
        return null;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cPanel;
    private javax.swing.JComboBox cbBreakpointType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    // End of variables declaration//GEN-END:variables
    
}
