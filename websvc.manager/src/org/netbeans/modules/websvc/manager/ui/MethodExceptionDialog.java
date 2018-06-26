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

package org.netbeans.modules.websvc.manager.ui;

import java.awt.Dialog;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * This dialog will show exceptions encountered while a user is testing
 * a web service client method.
 * 
 * @author David Botterill
 * @author Jan Stola
 */
public class MethodExceptionDialog extends JPanel {

    public MethodExceptionDialog(Throwable exception) {
        initComponents();
        
        // Only show the root cause of the exception if there was an exception during the method call
        Throwable throwable = exception;
        if (throwable instanceof InvocationTargetException) {
            throwable = ((InvocationTargetException)throwable).getTargetException();
        }
        initMessage(throwable);
    }

    public void showDialog(JComponent invoker) {
        DialogDescriptor dlg = new DialogDescriptor(
                this,
                NbBundle.getMessage(this.getClass(), "CLIENT_EXCEPTION"), // NOI18N
                false,
                NotifyDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null);
        dlg.setOptions(new Object[] { okButton });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setPreferredSize(new Dimension(500,300));
        dialog.setLocationRelativeTo(invoker);
        dialog.setVisible(true);
    }
    
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());

        messagePane = new javax.swing.JEditorPane();
        messagePane.setContentType("text/html"); // NOI18N
        messagePane.setEditable(false);
        messagePane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(this.getClass(), 
                "MethodExceptionDialog.messagePane.ACC_name")); // NOI18N
        messagePane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(this.getClass(), 
                "MethodExceptionDialog.messagePane.ACC_desc")); // NOI18N


        scrollPane = new javax.swing.JScrollPane();
        scrollPane.setViewportView(messagePane);
        add(scrollPane, java.awt.BorderLayout.CENTER);
    }
    
    private void initMessage(Throwable exception) {
        Throwable cause = exception;
        StringBuilder builder = new StringBuilder();
        builder.append("<html><body>"); // NOI18N
        while (cause != null) {
            builder.append("<h3>"); // NOI18N
            builder.append(cause.getLocalizedMessage());
            builder.append("</h3>"); // NOI18N
            builder.append(cause.getClass().getName()).append(" at <br>"); // NOI18N
            for (StackTraceElement element : cause.getStackTrace()) {
                builder.append(escape(element.toString()));
                builder.append("<br>"); // NOI18N
            }
            cause = cause.getCause();
        }
        builder.append("</body></html>"); // NOI18N
        messagePane.setText(builder.toString());
    }

    private String escape(String line) {
        return line.replaceAll("<", "&lt;").replaceAll(">", "&gt;"); // NOI18N
    }

    private final JButton okButton = new JButton(NbBundle.getMessage(this.getClass(), "OPTION_OK")); // NOI18N
    private JEditorPane messagePane;
    private JScrollPane scrollPane;
    
}
