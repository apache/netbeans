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

package org.netbeans.modules.web.debug.actions;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.debug.util.Utils;

import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;


/** 
 * AddJspWatch action.
 *
 * @author Martin Grebac
 */
public class AddJspWatchAction extends CallableSystemAction {

    private static String watchHistory = ""; // NOI18N
   
//    public AddWatchAction () {
//        putValue (
//            Action.NAME, 
//            NbBundle.getMessage (
//                AddWatchAction.class, 
//                "CTL_New_Watch"
//            )
//        );
//        putValue (
//            Action.SMALL_ICON, 
//            Utils.getIcon (
//                "org/netbeans/modules/debugger/resources/actions/NewWatch" // NOI18N
//            )
//        );
//    }

    protected boolean asynchronous () {
        return false;
    }

    public String getName () {
        return NbBundle.getMessage (
            AddJspWatchAction.class, "CTL_New_Watch"
        );
    }
    
    public HelpCtx getHelpCtx () {
        return new HelpCtx (AddJspWatchAction.class);

    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "org/netbeans/modules/debugger/resources/actions/NewWatch.gif"; // NOI18N
    }

    
    public void performAction () {
        ResourceBundle bundle = NbBundle.getBundle (AddJspWatchAction.class);

        JPanel panel = new JPanel();
        panel.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_WatchPanel")); // NOI18N
        JTextField textField;
        JLabel textLabel = new JLabel (bundle.getString ("CTL_Watch_Name")); // NOI18N
        textLabel.setBorder (new EmptyBorder (0, 0, 0, 10));
        panel.setLayout (new BorderLayout ());
        panel.setBorder (new EmptyBorder (11, 12, 1, 11));
        panel.add ("West", textLabel); // NOI18N
        panel.add ("Center", textField = new JTextField (25)); // NOI18N
        textField.getAccessibleContext ().setAccessibleDescription (bundle.getString ("ACSD_CTL_Watch_Name")); // NOI18N
        textField.setBorder (
            new CompoundBorder (textField.getBorder (), 
            new EmptyBorder (2, 0, 2, 0))
        );
        textLabel.setDisplayedMnemonic (
            bundle.getString ("CTL_Watch_Name_Mnemonic").charAt (0) // NOI18N
        );


        String t = null;//Utils.getELIdentifier();
//        Utils.log("Watch: ELIdentifier = " + t);
        
        boolean isScriptlet = Utils.isScriptlet();
        Utils.log("Watch: isScriptlet: " + isScriptlet);
        
        if ((t == null) && (isScriptlet)) {
            t = Utils.getJavaIdentifier();
            Utils.log("Watch: javaIdentifier = " + t);
        }
        
        if (t != null) {
            textField.setText(t);
        } else {
            textField.setText(watchHistory);
        }
        textField.selectAll ();        
        textLabel.setLabelFor (textField);
        textField.requestFocus ();

        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor (
            panel, 
            bundle.getString ("CTL_Watch_Title") // NOI18N
        );
        dd.setHelpCtx (new HelpCtx ("debug.add.watch"));
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (dd);
        dialog.setVisible(true);
        dialog.dispose ();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        String watch = textField.getText();
        if ((watch == null) || (watch.trim ().length () == 0)) {
            return;
        }
        
        String s = watch;
        int i = s.indexOf (';');
        while (i > 0) {
            String ss = s.substring (0, i).trim ();
            if (ss.length () > 0)
                DebuggerManager.getDebuggerManager ().createWatch (ss);
            s = s.substring (i + 1);
            i = s.indexOf (';');
        }
        s = s.trim ();
        if (s.length () > 0)
            DebuggerManager.getDebuggerManager ().createWatch (s);
        
        watchHistory = watch;
        
        // open watches view
//        new WatchesAction ().actionPerformed (null); TODO
    }
}

//        // if EL expression
//        if ((watch.startsWith("$")) && (watch.endsWith("}"))) {
//            watch = watch.replace("\"", "\\\"");
//            watch = "pageContext.getExpressionEvaluator().evaluate(\"" + watch +
//                                "\", java.lang.String.class, (javax.servlet.jsp.PageContext)pageContext, null)";
//            Utils.log("Watch: watch = " + watch);
//        }
//
//        w.setVariableName(watch);
//        if (w instanceof JPDAWatch) {
//            Utils.log("it is jpda watch");
//            //((JPDAWatch)w).setDescription(var);
//        }
