/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.discovery.performance;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLEditorKit;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 *
 */
@Messages({
    "NotifyProjectProblem.action.text=Details"
    ,"NotifyProjectProblem.open.message.text=Slow File System Detected"
    ,"# {0} - details"
    ,"NotifyProjectProblem.open.explanation.text=The IDE has detected slowness while opening/creating the project.<br>\n"
                                               +"Most probably this is caused by poor overall file system performance.<br>\n"
                                               +"{0}"
    ,"NotifyProjectProblem.infinite.create.message.text=Slow File Access Detected"
    ,"# {0} - details"
    ,"NotifyProjectProblem.infinite.create.explanation.text=The IDE has detected too long file access.<br>\n"
                                                           +"{0}"
    ,"NotifyProjectProblem.read.message.text=Slow File System Detected"
    ,"# {0} - details"
    ,"NotifyProjectProblem.read.explanation.text=The IDE has detected slowness while reading project files.<br>\n"
                                               +"Most probably this is caused by poor overall file system performance.<br>\n"
                                               +"{0}"
    ,"NotifyProjectProblem.parse.message.text=Slow File System Detected"
    ,"# {0} - details"
    ,"NotifyProjectProblem.parse.explanation.text=The IDE has detected slowness while parsing project files.<br>\n"
                                                +"Most probably this is caused by poor overall file system performance.<br>\n"
                                                +"{0}"
    ,"NotifyProjectProblem.infinite.parse.message.text=Slow File Parsing Detected"
    ,"# {0} - details"
    ,"NotifyProjectProblem.infinite.parse.explanation.text=The IDE has detected too long file parsing.<br>\n"
                                                         +"Most probably this is caused by a bug in the IDE or too big file.<br>\n"
                                                         +"{0}"
})
public class NotifyProjectProblem extends javax.swing.JPanel {
    public static final int CREATE_PROBLEM = 1;
    public static final int INFINITE_CREATE_PROBLEM = 2;
    public static final int READ_PROBLEM = 3;
    public static final int PARSE_PROBLEM = 4;
    public static final int INFINITE_PARSE_PROBLEM = 5;

    /**
     * Creates new form NotifyProjectProblem
     */
    private NotifyProjectProblem(PerformanceIssueDetector detector, String details) {
        initComponents();
        explanation.setEditorKit(new HTMLEditorKit());
        explanation.setBackground(getBackground());
        explanation.setForeground(getForeground());
        explanation.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        explanation.setText(details);
    }

    public static void showNotification(final PerformanceIssueDetector detector, int problem, final String details) {
        
        final String title;
        final String explanation;
        final String shortDescription;
        switch (problem) {
            case CREATE_PROBLEM: {
                // ignore details
                explanation = Bundle.NotifyProjectProblem_open_explanation_text(""); // NOI18N
                shortDescription = Bundle.NotifyProjectProblem_open_message_text();
                title = shortDescription;
                break;
            }
            case INFINITE_CREATE_PROBLEM: {
                explanation = Bundle.NotifyProjectProblem_infinite_create_explanation_text(details);
                shortDescription = Bundle.NotifyProjectProblem_infinite_create_message_text();
                title = shortDescription;
                break;
            }
            case READ_PROBLEM: {
                // ignore details
                explanation = Bundle.NotifyProjectProblem_read_explanation_text(""); // NOI18N
                shortDescription = Bundle.NotifyProjectProblem_read_message_text();
                title = shortDescription;
                break;
            }
            case PARSE_PROBLEM: {
                // ignore details
                explanation = Bundle.NotifyProjectProblem_parse_explanation_text(""); // NOI18N
                shortDescription = Bundle.NotifyProjectProblem_parse_message_text();
                title = shortDescription;
                break;
            }
            case INFINITE_PARSE_PROBLEM: {
                explanation = Bundle.NotifyProjectProblem_infinite_parse_explanation_text(details);
                shortDescription = Bundle.NotifyProjectProblem_infinite_parse_message_text();
                title = shortDescription;
                break;
            }
            default:
                throw new IllegalArgumentException();
        }

        ActionListener onClickAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                NotifyProjectProblem panel = new NotifyProjectProblem(detector, explanation);
                DialogDescriptor descriptor = new DialogDescriptor(panel, title, true,
                        new Object[]{DialogDescriptor.CLOSED_OPTION}, DialogDescriptor.CLOSED_OPTION,
                        DialogDescriptor.DEFAULT_ALIGN, null, null);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(descriptor);
                try {
                    dlg.setVisible(true);
                } catch (Throwable th) {
                    if (!(th.getCause() instanceof InterruptedException)) {
                        throw new RuntimeException(th);
                    }
                    descriptor.setValue(DialogDescriptor.CANCEL_OPTION);
                } finally {
                    dlg.dispose();
                }
            }
        };
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/discovery/performance/exclamation.gif", false); // NOI18N
        final Notification notification = NotificationDisplayer.getDefault().notify(shortDescription, icon,
                Bundle.NotifyProjectProblem_action_text(), onClickAction, NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.WARNING); // NOI18N
        //RP.post(new Runnable() {
        //    @Override
        //    public void run() {
        //        notification.clear();
        //    }
        //}, 150 * 1000);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        explanation = new javax.swing.JTextPane();

        setPreferredSize(new java.awt.Dimension(350, 250));
        setLayout(new java.awt.BorderLayout());

        scrollPane.setViewportView(explanation);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane explanation;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
