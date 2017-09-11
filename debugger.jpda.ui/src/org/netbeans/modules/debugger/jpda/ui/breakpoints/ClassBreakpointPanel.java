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

import java.util.ResourceBundle;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.completion.JavaClassNbDebugEditorKit;
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
// public class ClassBreakpointPanel extends JPanel implements Controller {
// ====
public class ClassBreakpointPanel extends JPanel implements Controller, org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    private static final String         HELP_ID = "debug.add.breakpoint.java.class"; // NOI18N
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private ClassLoadUnloadBreakpoint   breakpoint;
    private boolean                     createBreakpoint = false;
    private JEditorPane                 epClassName;
    private JScrollPane                 spClassName;
    
    private static ClassLoadUnloadBreakpoint creteBreakpoint () {
        String className;
        try {
            className = EditorContextBridge.getMostRecentClassName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            className = "";
        }
        ClassLoadUnloadBreakpoint mb = ClassLoadUnloadBreakpoint.create (
            className,
            false, 
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED
        );
        mb.setPrintText (
            NbBundle.getBundle (ClassBreakpointPanel.class).getString 
                ("CTL_Class_Breakpoint_Print_Text")
        );
        return mb;
    }
    
    
    /** Creates new form LineBreakpointPanel */
    public ClassBreakpointPanel () {
        this (creteBreakpoint ());
        createBreakpoint = true;
    }
    
    /** Creates new form LineBreakpointPanel */
    public ClassBreakpointPanel (ClassLoadUnloadBreakpoint b) {
        breakpoint = b;
        initComponents ();
        
        String className = ""; // NOI18N
        String[] cf = b.getClassFilters ();
        className = ClassBreakpointPanel.concatClassFilters(cf);

        ResourceBundle bundle = NbBundle.getBundle(ClassBreakpointPanel.class);
        String tooltipText = bundle.getString("TTT_TF_Class_Breakpoint_Class_Name");
        Pair<JScrollPane, JEditorPane> editorCC = addClassNameEditorCC(JavaClassNbDebugEditorKit.MIME_TYPE, pSettings, className, tooltipText);
        spClassName = editorCC.first();
        epClassName = editorCC.second();
        epClassName.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_Method_Breakpoint_ClassName"));
        epClassName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Class_Breakpoint_ClassName"));
        HelpCtx.setHelpIDString(epClassName, HELP_ID);
        jLabel3.setLabelFor(spClassName);
        
        cbBreakpointType.addItem (bundle.getString("LBL_Class_Breakpoint_Type_Prepare"));
        cbBreakpointType.addItem (bundle.getString("LBL_Class_Breakpoint_Type_Unload"));
        cbBreakpointType.addItem (bundle.getString("LBL_Class_Breakpoint_Type_Prepare_or_Unload"));
        switch (b.getBreakpointType ()) {
            case ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED:
                cbBreakpointType.setSelectedIndex (0);
                break;
            case ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED:
                cbBreakpointType.setSelectedIndex (1);
                break;
            case ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED:
                cbBreakpointType.setSelectedIndex (2);
                break;
        }
        
        conditionsPanel = new ConditionsPanel(HELP_ID);
        conditionsPanel.setupConditionPaneContext();
        cPanel.add(conditionsPanel, "Center");
        conditionsPanel.showExclusionClassFilter(true);
        conditionsPanel.showCondition(false);
        conditionsPanel.setClassExcludeFilter(b.getClassExclusionFilters());
        conditionsPanel.setHitCountFilteringStyle(b.getHitCountFilteringStyle());
        conditionsPanel.setHitCount(b.getHitCountFilter());
        
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
    public HelpCtx getHelpCtx() {
       return new HelpCtx("NetbeansDebuggerBreakpointClassJPDA"); // NOI18N
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
        jLabel4 = new javax.swing.JLabel();
        cbBreakpointType = new javax.swing.JComboBox();
        cPanel = new javax.swing.JPanel();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Class_Breakpoint_BorderTitle"))); // NOI18N
        pSettings.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("L_Class_Breakpoint_Class_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Class_Breakpoint_Class_Name")); // NOI18N

        jLabel4.setLabelFor(cbBreakpointType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, bundle.getString("L_Class_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(bundle.getString("ASCD_L_Class_Breakpoint_Type")); // NOI18N

        cbBreakpointType.setToolTipText(bundle.getString("TTT_CB_Class_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(cbBreakpointType, gridBagConstraints);
        cbBreakpointType.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CB_Class_Breakpoint_Type")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        cPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
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
        breakpoint.setClassFilters(ConditionsPanel.getFilter(className));
        breakpoint.setClassExclusionFilters(conditionsPanel.getClassExcludeFilter());//parseClassFilters(className));
        
        switch (cbBreakpointType.getSelectedIndex ()) {
            case 0:
                breakpoint.setBreakpointType (ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
                break;
            case 1:
                breakpoint.setBreakpointType (ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED);
                break;
            case 2:
                breakpoint.setBreakpointType (ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED_UNLOADED);
                break;
        }
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
        if (epClassName.getText().trim ().length() == 0) {
            return NbBundle.getMessage(ClassBreakpointPanel.class, "MSG_No_Class_Name_Spec");
        }
        return null;
    }
    
    static String concatClassFilters(String[] cf) {
        if (cf.length > 0) {
            StringBuilder sb = new StringBuilder(cf[0]);
            for (int i = 1; i < cf.length; i++) {
                sb.append(", ");
                sb.append(cf[i]);
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    static JTextField addClassNameEditor(JComponent comp, String className, String tooltipText) {
        JTextField textField = new JTextField(className); // NOI18N
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        comp.add(textField, gridBagConstraints);
        textField.setToolTipText(tooltipText);
        return textField;
    }
    
    static Pair<JScrollPane, JEditorPane> addClassNameEditorCC(String mimeType, JComponent comp, String className, String tooltipText) {
        JComponent [] editorComponents = Utilities.createSingleLineEditor(mimeType);
        JScrollPane sle = (JScrollPane) editorComponents[0];
        JEditorPane epClassName = (JEditorPane) editorComponents[1];
        epClassName.setText(className);
        if (comp != null) {
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
            comp.add(sle, gridBagConstraints);
        }
        sle.setToolTipText(tooltipText);
        epClassName.setToolTipText(tooltipText);
        return Pair.<JScrollPane, JEditorPane>of(sle, epClassName);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cPanel;
    private javax.swing.JComboBox cbBreakpointType;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    // End of variables declaration//GEN-END:variables
    
}
