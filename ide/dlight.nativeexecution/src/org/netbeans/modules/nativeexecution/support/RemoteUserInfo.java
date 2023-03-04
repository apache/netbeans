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
package org.netbeans.modules.nativeexecution.support;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CancellationException;
//import javax.swing.JOptionPane;
//import javax.swing.Icon;
//import javax.swing.JComponent;
//import javax.swing.JOptionPane;
//import javax.swing.JTextArea;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.PasswordManager;
import org.netbeans.modules.nativeexecution.spi.support.NativeExecutionUserNotification;
import org.netbeans.modules.nativeexecution.spi.support.PasswordProvider;
import org.netbeans.modules.nativeexecution.spi.support.PasswordProviderFactory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class RemoteUserInfo implements UserInfo, UIKeyboardInteractive {

    private static final Object lock = RemoteUserInfo.class.getName() + "Lock"; // NOI18N
    private static final PasswordManager pm = PasswordManager.getInstance();
//    private final Component parent;
    private final ExecutionEnvironment env;
  //  private volatile Component parentWindow = null;
    private final boolean allowInterraction;
    private char[] secret = null;

    public RemoteUserInfo(ExecutionEnvironment env, boolean allowToAskForPassword) {
        this.env = env;
        this.allowInterraction = allowToAskForPassword;
//        Mutex.EVENT.readAccess(new Runnable() {
//            @Override
//            public void run() {
//                parentWindow = WindowManager.getDefault().getMainWindow();
//            }
//        });
//        parent = parentWindow;
    }

    @Override
    public String getPassphrase() {
        return getSecret();
    }

    @Override
    public String getPassword() {
        return getSecret();
    }

    public String getSecret() {
        String result = null;
        synchronized (lock) {
            char[] saved = pm.getPassword(env);
            if (saved != null) {
                result = new String(saved);
            } else if (secret != null) {
                result = new String(secret);
                Arrays.fill(secret, 'x');
                secret = null;
            }
        }
        return result;
    }

    @Override
    public boolean promptPassword(String message) {
        return promptSecret(PasswordProvider.SecretType.PASSWORD, message);
    }

    @Override
    public boolean promptPassphrase(String message) {
        return promptSecret(PasswordProvider.SecretType.PASSPHRASE, message);
    }

    private boolean promptSecret(PasswordProvider.SecretType secretType, String message) {
        synchronized (lock) {
            if (pm.getPassword(env) != null) {
                return true;
            }

            if (!allowInterraction) {
                return false;
            }
            Collection<? extends PasswordProviderFactory> factories = 
                    Lookup.getDefault().lookupAll(PasswordProviderFactory.class);
            if (factories.isEmpty()) {
                return false;
            }
            PasswordProvider passwordProvider = null;
            for (PasswordProviderFactory factory : factories) {
                passwordProvider = factory.create(secretType);
                if (passwordProvider != null) {
                    break;
                }
            }
            if (passwordProvider == null) {
                return false;
            }
            if (!passwordProvider.askPassword(env, message)) {
                throw new CancellationException(loc("USER_AUTH_CANCELED")); // NOI18N 
            }
            secret = passwordProvider.getPassword();
            pm.storePassword(env, secret, passwordProvider.isRememberPassword());
            passwordProvider.clearPassword();
//            PromptPasswordDialog dlg;
//            switch (secretType) {
//                case PASSWORD:
//                    dlg = new PasswordDlg();
//                    break;
//                case PASSPHRASE:
//                    dlg = new CertPassphraseDlg();
//                    break;
//                default:
//                    throw new InternalError("Wrong secret type"); // NOI18N
//            }
//
//            if (!dlg.askPassword(env, message)) {
//                throw new CancellationException(loc("USER_AUTH_CANCELED")); // NOI18N
//            }
//
//            secret = dlg.getPassword();
//            pm.storePassword(env, secret, dlg.isRememberPassword());
//            dlg.clearPassword();
        }

        return true;
    }

    @Override
    public boolean promptYesNo(String str) {

        synchronized (lock) {
            if (RemoteUserInfo.isUnitTestMode() || RemoteUserInfo.isStandalone()) {
                System.err.println(str+" yes"); // NOI18N
                return true;
            }
            return NativeExecutionUserNotification.getDefault().showYesNoQuestion(loc("TITLE_YN_Warning"), str); //NOI18N              
        }
    }

    // copy-paste from CndUtils
    private static boolean isStandalone() {
        if ("true".equals(System.getProperty ("cnd.command.line.utility"))) { // NOI18N
            return true;
        }
        return !RemoteUserInfo.class.getClassLoader().getClass().getName().startsWith("org.netbeans."); // NOI18N
    }
    
    // copy-paste from CndUtils
    private static boolean isUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.unittest"); // NOI18N
    }

    /** returns true if the line consists of equal characters, e.g. "########" */
    private boolean isSeparatorLine(String line) {
        //line = line.trim();
        if (line.isEmpty()) {
            return false;
        } else {
            char c = line.charAt(0);
            for (int i = 0; i < line.length(); i++) {
                if (line.charAt(i) != c) {
                    return false;
                }
            }
            return true;
        }
    }
    
    /** 
     * if the line is framed (e.g. "# the message #"), 
     * returns the line w/o frame, trimmed ("the message")
     */
    private String tryRemovingFrame(String line) {
        //line = line.trim();
        if (!line.isEmpty()) {
            char c = line.charAt(0);
            if (!Character.isLetterOrDigit(c)) {
                if (line.charAt(line.length()-1) == c) {
                    line = line.substring(1, line.length() - 1).trim();
                }
            }
        }
        return line;
    }

    private String getBriefMessage(String message) {
        final int maxLen = 80; // max length for a single-line message
        //final int maxLines = 3; // max lines to display for a multy-line message
        if (message.length() <= maxLen) {
            return message;
        } else {
            // Judging by my own experience and issue #247298
            // https://bugzilla-attachments-247298.netbeans.org/bugzilla/attachment.cgi?id=149364
            // banner messages often contain
            // a) frames made from  "################" or "----" or alike are quite usual for such messages.
            // b) framed lines like "# message text #"
            // Let's try to handle this
            if (message.contains("\n")) { //NOI18N
                String[] lines = message.split("\n"); //NOI18N
                StringBuilder sb = new StringBuilder();
//                int lineCount = 0;
                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        // skip
                        continue;
                    } else if (isSeparatorLine(line)) {
                        // skip
                        continue;
                    } else {
                        line = tryRemovingFrame(line).trim();
                        if (! line.isEmpty()) {
                            return line + "..."; // NOI18N
//                            if (++lineCount > maxLines) {
//                                sb.append("\n..."); //NOI18N
//                                break;
//                            } else {
//                                sb.append(line).append('\n');
//                            }
                        }
                    }
                }               
                return ""; // sb.toString(); //NOI18N
            } else {
                return message.substring(0, maxLen) + "..."; //NOI18N
            }
        }
    }


        
//    private JComponent createPopupDetails(final String title, final String message, final Icon icon) {
//        ActionListener actionListener = new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                final JPanel wrapperPanel = new JPanel();
//                wrapperPanel.setLayout(new BoxLayout(wrapperPanel, BoxLayout.X_AXIS));
//                JComponent textArea = createMessageComponent(message);
//                wrapperPanel.add(textArea);
//                wrapperPanel.add(Box.createHorizontalStrut(32));
//                JOptionPane.showMessageDialog(parent, wrapperPanel, title, JOptionPane.INFORMATION_MESSAGE, icon);
//            }
//        };
//        JButton popupDetails = new JButton(title);
//        popupDetails.addActionListener(actionListener);
//        popupDetails.setFocusable(false);
//        popupDetails.setBorder(BorderFactory.createEmptyBorder());
//        popupDetails.setBorderPainted(false);
//        popupDetails.setFocusPainted(false);
//        popupDetails.setOpaque(false);
//        popupDetails.setContentAreaFilled(false);
//        popupDetails.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//        popupDetails.setForeground(Color.blue);
//        return popupDetails;
//    }
    
    @Override
    public void showMessage(final String message) {        
          final String titleAndBriefMessage = NbBundle.getMessage(RemoteUserInfo.class, "TITLE_Message_Ex", 
                env.getDisplayName(), getBriefMessage(message));
        NativeExecutionUserNotification.getDefault().showInfoNotification(titleAndBriefMessage, titleAndBriefMessage, message);        
    }

    @Override
    public String[] promptKeyboardInteractive(String destination,
            String name,
            String instruction,
            String[] prompt,
            boolean[] echo) {

        if (prompt.length == 1 && !echo[0]) {
            // this is a password request
            if (!promptPassword(loc("MSG_PasswordInteractive", // NOI18N
                    destination, prompt[0]))) {
                return null;
            } else {
                return new String[]{getPassword()};
            }
        } else {
            // AK:
            // What else it could ask about?
            // There was a code here that constructed dialog with all prompts
            // based on promt / echo arrays.
            // As I don't know usecases for it, I removed it ;)

            return null;
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(RemoteUserInfo.class, key, params);
    }


}
