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
