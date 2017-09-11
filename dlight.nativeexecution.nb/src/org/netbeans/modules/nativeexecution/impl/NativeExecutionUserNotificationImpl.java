/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
