/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.completion.JavaClassNbDebugEditorKit;
import org.netbeans.modules.debugger.jpda.ui.completion.JavaMethodNbDebugEditorKit;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * @author  Jan Jancura
 */
// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class MethodBreakpointPanel extends JPanel implements Controller {
// ====
public class MethodBreakpointPanel extends JPanel implements Controller, org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    private static final String         HELP_ID = "debug.add.breakpoint.java.method"; // NOI18N
    private ConditionsPanel             conditionsPanel;
    private ActionsPanel                actionsPanel; 
    private MethodBreakpoint            breakpoint;
    private boolean                     createBreakpoint = false;
    private JEditorPane                 epClassName;
    private JScrollPane                 spClassName;
    private JEditorPane                 epMethodName;
    private JScrollPane                 spMethodName;
    
    
    private static MethodBreakpoint createBreakpoint () {
        String className;
        try {
            className = EditorContextBridge.getMostRecentClassName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            className = "";
        }
        String methodName;
        try {
            methodName = EditorContextBridge.getMostRecentMethodName();
        } catch (java.awt.IllegalComponentStateException icsex) {
            methodName = "";
        }
        MethodBreakpoint mb = MethodBreakpoint.create (
            className,
            methodName
        );
        try {
            mb.setMethodSignature(EditorContextBridge.getMostRecentMethodSignature());
        } catch (java.awt.IllegalComponentStateException icsex) {}
        mb.setPrintText (
            NbBundle.getBundle (MethodBreakpointPanel.class).getString 
                ("CTL_Method_Breakpoint_Print_Text")
        );
        return mb;
    }
    
    
    /** Creates new form LineBreakpointPanel */
    public MethodBreakpointPanel () {
        this (createBreakpoint ());
        createBreakpoint = true;
    }
    
    /** Creates new form LineBreakpointPanel */
    public MethodBreakpointPanel (MethodBreakpoint b) {
        breakpoint = b;
        initComponents ();
        
        String className = "";
        String[] cf = b.getClassFilters ();
        className = ClassBreakpointPanel.concatClassFilters(cf);
        String tooltipText = NbBundle.getMessage(MethodBreakpointPanel.class, "TTT_TF_Field_Breakpoint_Class_Name");
        Pair<JScrollPane, JEditorPane> editorCC = ClassBreakpointPanel.addClassNameEditorCC(JavaClassNbDebugEditorKit.MIME_TYPE, null, className, tooltipText);
        spClassName = editorCC.first();
        epClassName = editorCC.second();
        epClassName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MethodBreakpointPanel.class, "ACSN_Method_Breakpoint_ClassName"));
        epClassName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MethodBreakpointPanel.class, "ACSD_Method_Breakpoint_ClassName"));
        jLabel3.setLabelFor(spClassName);
        HelpCtx.setHelpIDString(epClassName, HELP_ID);
        panelClassName.add(java.awt.BorderLayout.CENTER, spClassName);
        
        editorCC = ClassBreakpointPanel.addClassNameEditorCC(JavaMethodNbDebugEditorKit.MIME_TYPE, null, className, org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "TTT_TF_Method_Breakpoint_Method_Name"));
        spMethodName = editorCC.first();
        epMethodName = editorCC.second();
        jLabel1.setLabelFor(spMethodName);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(spMethodName, gridBagConstraints);
        epMethodName.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "ACSD_TF_Method_Breakpoint_Method_Name")); // NOI18N
        epMethodName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                epMethodName.getDocument().putProperty("class-name", epClassName.getText());
            }
            @Override
            public void focusLost(FocusEvent e) {}
        });
        
        if ("".equals (b.getMethodName ())) {
            epMethodName.setText (org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "Method_Breakpoint_ALL_METHODS"));
            cbAllMethods.setSelected (true);
            epMethodName.setEnabled (false);
        } else {
            epMethodName.setText (b.getMethodName () + " " + createParamTypesFromSignature(b.getMethodSignature()));
        }
        cbBreakpointType.addItem (NbBundle.getMessage(MethodBreakpointPanel.class, "LBL_Method_Breakpoint_Type_Entry"));
        cbBreakpointType.addItem (NbBundle.getMessage(MethodBreakpointPanel.class, "LBL_Method_Breakpoint_Type_Exit"));
        cbBreakpointType.addItem (NbBundle.getMessage(MethodBreakpointPanel.class, "LBL_Method_Breakpoint_Type_Entry_or_Exit"));
        switch (b.getBreakpointType ()) {
            case MethodBreakpoint.TYPE_METHOD_ENTRY:
                cbBreakpointType.setSelectedIndex (0);
                break;
            case MethodBreakpoint.TYPE_METHOD_EXIT:
                cbBreakpointType.setSelectedIndex (1);
                break;
            case (MethodBreakpoint.TYPE_METHOD_ENTRY | MethodBreakpoint.TYPE_METHOD_EXIT):
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
    
    /** @return comma-separated parameter types */
    private static String createParamTypesFromSignature(String signature) {
        if (signature == null || signature.length() == 0) return "";
        int end = signature.lastIndexOf(")");
        if (end < 0) {
            ErrorManager.getDefault().notify(new IllegalArgumentException("Bad signature: "+signature));
            return "";
        }
        StringBuilder paramTypes = new StringBuilder("(");
        int[] s = new int[] { 1 }; // skipping the opening '('
        while (s[0] < signature.length()) {
            if (signature.charAt(s[0]) == ')') {
                break; // We're done
            }
            if (s[0] > 1) paramTypes.append(',');
            paramTypes.append(getType(signature, s));
        }
        paramTypes.append(')');
        return paramTypes.toString();
    }
    
    /** @param paramTypes comma-separated parameter types */
    private static String createSignatureFromParamTypes(String paramTypes) {
        StringBuilder signature = new StringBuilder("(");
        int s = 0;
        int e;
        while (s < paramTypes.length()) {
            e = paramTypes.indexOf(',', s);
            if (e < 0) {
                e = paramTypes.length();
            }
            String type = paramTypes.substring(s, e);
            signature.append(getSignature(type.trim()));
            s = e + 1;
        }
        signature.append(')');
        // ignoring return type
        return signature.toString();
    }
    
    private static String getSignature(String javaType) {
        if (javaType.equals("boolean")) {
            return "Z";
        } else if (javaType.equals("byte")) {
            return "B";
        } else if (javaType.equals("char")) {
            return "C";
        } else if (javaType.equals("short")) {
            return "S";
        } else if (javaType.equals("int")) {
            return "I";
        } else if (javaType.equals("long")) {
            return "J";
        } else if (javaType.equals("float")) {
            return "F";
        } else if (javaType.equals("double")) {
            return "D";
        } else if (javaType.endsWith("[]")) {
            return "["+getSignature(javaType.substring(0, javaType.length() - 2));
        } else {
            return "L"+javaType.replace('.', '/')+";";
        }
    }
    
    private static String getType(String signature, int[] pos) throws IllegalArgumentException {
        char c = signature.charAt(pos[0]);
        if (c == 'Z') {
            pos[0]++;
            return "boolean";
        }
        if (c == 'B') {
            pos[0]++;
            return "byte";
        }
        if (c == 'C') {
            pos[0]++;
            return "char";
        }
        if (c == 'S') {
            pos[0]++;
            return "short";
        }
        if (c == 'I') {
            pos[0]++;
            return "int";
        }
        if (c == 'J') {
            pos[0]++;
            return "long";
        }
        if (c == 'F') {
            pos[0]++;
            return "float";
        }
        if (c == 'D') {
            pos[0]++;
            return "double";
        }
        if (c == 'L') {
            pos[0]++;
            int typeEnd = signature.indexOf(";", pos[0]);
            if (typeEnd < 0) {
                throw new IllegalArgumentException("Bad signature: '"+signature+"', 'L' not followed by ';' at position "+pos[0]);
            }
            String type = signature.substring(pos[0], typeEnd);
            type = type.replace('/', '.');
            pos[0] = typeEnd + 1;
            return type;
        }
        if (c == '[') {
            pos[0]++;
            String type = getType(signature, pos);
            return type + "[]";
        }
        throw new IllegalArgumentException("Bad signature: '"+signature+"', unrecognized element '"+c+"' at position "+pos[0]);
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerBreakpointMethodJPDA"); // NOI18N
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
        cbAllMethods = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        panelClassName = new javax.swing.JPanel();
        stopOnLabel = new javax.swing.JLabel();
        cbBreakpointType = new javax.swing.JComboBox();
        cPanel = new javax.swing.JPanel();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "L_Method_Breakpoint_BorderTitle"))); // NOI18N
        pSettings.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "L_Method_Breakpoint_Class_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "ACSD_L_Method_Breakpoint_Class_Name")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbAllMethods, org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "CB_Method_Breakpoint_All_Methods")); // NOI18N
        cbAllMethods.setToolTipText(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "TTT_CB_Method_Breakpoint_All_Methods")); // NOI18N
        cbAllMethods.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbAllMethods.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAllMethodsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(cbAllMethods, gridBagConstraints);
        cbAllMethods.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "ACSD_CB_Method_Breakpoint_All_Methods")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "L_Method_Breakpoint_Method_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "ACSD_L_Method_Breakpoint_Method_Name")); // NOI18N

        panelClassName.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(panelClassName, gridBagConstraints);

        stopOnLabel.setLabelFor(cbBreakpointType);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(stopOnLabel, bundle.getString("L_Method_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(stopOnLabel, gridBagConstraints);
        stopOnLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "ACSD_StopOn_LBL")); // NOI18N

        cbBreakpointType.setToolTipText(bundle.getString("TTT_CB_Class_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(cbBreakpointType, gridBagConstraints);
        cbBreakpointType.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "ACSN_CB_Method_Breakpoint_Type")); // NOI18N

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

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "ACSN_MethodBreakpointPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void cbAllMethodsActionPerformed (java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbAllMethodsActionPerformed
    {//GEN-HEADEREND:event_cbAllMethodsActionPerformed
        if (cbAllMethods.isSelected ()) {
            epMethodName.setText (org.openide.util.NbBundle.getMessage(MethodBreakpointPanel.class, "Method_Breakpoint_ALL_METHODS"));
            epMethodName.setEnabled (false);
        } else {
            epMethodName.setText ("");
            epMethodName.setEnabled (true);
        }
    }//GEN-LAST:event_cbAllMethodsActionPerformed

    
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
        //String className = ((String) tfPackageName.getText ()).trim ();
        //if (className.length () > 0)
        //    className += '.';
        String className = epClassName.getText ().trim ();
        breakpoint.setClassFilters (new String[] {className});
        if (!cbAllMethods.isSelected ()) {
            String methodAndSignature = epMethodName.getText ().trim ();
            String methodName;
            String signature;
            int index = methodAndSignature.indexOf("(");
            if (index < 0) {
                methodName = methodAndSignature;
                signature = null;
            } else {
                methodName = methodAndSignature.substring(0, index).trim();
                int end = methodAndSignature.indexOf(")", index);
                if (end < 0) {
                    end = methodAndSignature.length();
                }
                signature = methodAndSignature.substring(index + 1, end);
                signature = createSignatureFromParamTypes(signature);
            }
            breakpoint.setMethodName (methodName);
            breakpoint.setMethodSignature(signature);
        } else {
            breakpoint.setMethodName ("");
        }
        switch (cbBreakpointType.getSelectedIndex ()) {
            case 0:
                breakpoint.setBreakpointType (MethodBreakpoint.TYPE_METHOD_ENTRY);
                break;
            case 1:
                breakpoint.setBreakpointType (MethodBreakpoint.TYPE_METHOD_EXIT);
                break;
            case 2:
                breakpoint.setBreakpointType (MethodBreakpoint.TYPE_METHOD_ENTRY | MethodBreakpoint.TYPE_METHOD_EXIT);
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
        if (epClassName.getText().trim ().length() == 0 || (epMethodName.getText().trim ().length() == 0 && !cbAllMethods.isSelected())) {
            return NbBundle.getMessage(MethodBreakpointPanel.class, "MSG_No_Class_or_Method_Name_Spec");
        }
        return null;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cPanel;
    private javax.swing.JCheckBox cbAllMethods;
    private javax.swing.JComboBox cbBreakpointType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JPanel panelClassName;
    private javax.swing.JLabel stopOnLabel;
    // End of variables declaration//GEN-END:variables

}
