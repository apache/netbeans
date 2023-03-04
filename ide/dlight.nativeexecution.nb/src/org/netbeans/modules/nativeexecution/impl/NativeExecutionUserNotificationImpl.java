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
package org.netbeans.modules.nativeexecution.impl;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import org.netbeans.modules.nativeexecution.api.util.Shell;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;
import org.netbeans.modules.nativeexecution.ui.ShellValidationStatusPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author masha
 */
@ServiceProvider (service = NativeExecutionUserNotification.class, position = 100)
public class NativeExecutionUserNotificationImpl extends NativeExecutionUserNotification {
    
     private volatile Component parentWindow = null;


    private static int toNotifyDesctiptor(Descriptor type) {
        switch (type) {
            case ERROR:
                return NotifyDescriptor.ERROR_MESSAGE;
            case WARNING:
                return NotifyDescriptor.WARNING_MESSAGE;
            default:
                return NotifyDescriptor.WARNING_MESSAGE;
        }
    }

    @Override
    public void notify(String message, Descriptor type) {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(message,
                        toNotifyDesctiptor(type)));
    }

    @Override
    public void notify(String message) {
        DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(message));
    }

    @Override
    public void showErrorNotification(String title, String shortText, String longText) {
        ImageIcon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/nativeexecution/impl/error.png", false); //NOI18N
        longText = "<html>" + longText + "</html>"; // NOI18N
        NotificationDisplayer.getDefault().notify(title, icon, new JLabel(shortText), new JLabel(longText),
                NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.ERROR);
    }

    private JComponent createMessageComponent(String message) {
        final JTextArea textArea = new JTextArea(message.trim());
        int fontSize = textArea.getFont().getSize();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, fontSize)); //NOI18N
        textArea.setEditable(false);
        textArea.setOpaque(false);
        return textArea;
    }

    @Override
    public void showInfoNotification(String title, String shortMessage, String longMesage) {
        final Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/nativeexecution/impl/exclamation.gif", false); // NOI18N
//        final String titleAndBriefMessage = NbBundle.getMessage(RemoteUserInfo.class, "TITLE_Message_Ex", 
//                env.getDisplayName(), getBriefMessage(message));
        final JComponent baloonComponent = createMessageComponent(longMesage);
        final JComponent popupDetails = createMessageComponent(longMesage); // createPopupDetails(titleAndBriefMessage, message, icon);
        NotificationDisplayer.getDefault().notify(title, icon, baloonComponent, popupDetails,
                NotificationDisplayer.Priority.NORMAL, NotificationDisplayer.Category.INFO);

    }

    @Override
    public boolean confirmShellStatusValiation(String title, String header, String footer, Shell shell) {
        final ShellValidationStatusPanel errorPanel = new ShellValidationStatusPanel(header, footer, shell);

        final JButton noButton = new JButton("No"); // NOI18N
        errorPanel.setActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                noButton.setEnabled(!errorPanel.isRememberDecision());
            }
        });

        DialogDescriptor dd = new DialogDescriptor(errorPanel,
                title,
                true,
                new Object[]{DialogDescriptor.YES_OPTION, noButton},
                noButton,
                DialogDescriptor.DEFAULT_ALIGN, null, null);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dd.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dialog.dispose();
        }

        Object response = dd.getValue();

        if (response == DialogDescriptor.YES_OPTION && errorPanel.isRememberDecision()) {
            NbPreferences.forModule(WindowsSupport.class).put(shell.toString(), "yes"); // NOI18N
        }
        return response == DialogDescriptor.YES_OPTION;
    }

    @Override
    public boolean showYesNoQuestion(String title, String text) {
        Object[] options = {"yes", "no"}; // NOI18N
        int foo;
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                parentWindow = WindowManager.getDefault().getMainWindow();
            }
        });        
         foo = JOptionPane.showOptionDialog(parentWindow, text,
                        title, // NOI18N
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[0]);
         return foo == 0;
            
    }

    @Override
    public void notifyStatus(String message) {
        StatusDisplayer.getDefault().setStatusText(message);
    }    
    
}
