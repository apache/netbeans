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
        return line.replace("<", "&lt;").replace(">", "&gt;"); // NOI18N
    }

    private final JButton okButton = new JButton(NbBundle.getMessage(this.getClass(), "OPTION_OK")); // NOI18N
    private JEditorPane messagePane;
    private JScrollPane scrollPane;
    
}
