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
package org.netbeans.modules.php.dbgp;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.project.api.PhpOptions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;

/**
 *
 * @author Radek Matous
 */
public class ConnectionErrMessage extends JPanel {

    private static final long serialVersionUID = -2227584113811663220L;
    private final String message;
    private static final String NEWLINE = System.getProperty("line.separator"); // NOI18N

    public static void showMe(int seconds) {
        ConnectionErrMessage panel = new ConnectionErrMessage(seconds);

        NotifyDescriptor messageDescriptor = new NotifyDescriptor.Message("");//NOI18N
        DialogDescriptor descr2 = new DialogDescriptor(panel, messageDescriptor.getTitle(),
                true, new Object[]{DialogDescriptor.OK_OPTION}, null, DialogDescriptor.BOTTOM_ALIGN, null, null);
        DialogDisplayer.getDefault().createDialog(descr2).setVisible(true);
    }

    /**
     * Creates new form ConnectionErrMessage.
     */
    private ConnectionErrMessage(int seconds) {
        message = createMessage(seconds);
        initComponents();
        emptyLabel.setText(" "); // NOI18N to avoid hiding the bottom components
    }

    @NbBundle.Messages({
        "ConnectionErrMessage.xdebug.remote_host=xdebug.remote_host=localhost (or hostname)", // xdebug2
        "# {0} - port",
        "ConnectionErrMessage.xdebug.remote_port=xdebug.remote_port={0} (default port: 9000)", // xdebug2
        "ConnectionErrMessage.xdebug.client_host=xdebug.client_host=localhost (or hostname)", // xdebug3
        "# {0} - port",
        "ConnectionErrMessage.xdebug.client_port=xdebug.client_port={0} (default port: 9003)", // xdebug3
    })
    private static String createMessage(int seconds) {
        String debuggerPort = String.valueOf(PhpOptions.getInstance().getDebuggerPort());
        String xdebugIdekey = String.format("xdebug.idekey=\"%s\"", PhpOptions.getInstance().getDebuggerSessionId()); // NOI18N
        StringBuilder sb = new StringBuilder();
        sb.append("<h4>Xdebug 2</h4>"); // NOI18N
        sb.append("<ul>"); // NOI18N
        sb.append("<li>").append("xdebug.remote_enable=on").append("</li>"); // NOI18N
        sb.append("<li>").append("xdebug.remote_handler=dbgp").append("</li>"); // NOI18N
        sb.append("<li>").append(Bundle.ConnectionErrMessage_xdebug_remote_host()).append("</li>"); // NOI18N
        sb.append("<li>").append(Bundle.ConnectionErrMessage_xdebug_remote_port(debuggerPort)).append("</li>"); // NOI18N
        sb.append("<li>").append(xdebugIdekey).append("</li>"); // NOI18N
        sb.append("</ul>"); // NOI18N
        sb.append("<h4>Xdebug 3</h4>"); // NOI18N
        sb.append("<ul>"); // NOI18N
        sb.append("<li>").append("xdebug.mode=debug").append("</li>"); // NOI18N
        sb.append("<li>").append(Bundle.ConnectionErrMessage_xdebug_client_host()).append("</li>"); // NOI18N
        sb.append("<li>").append(Bundle.ConnectionErrMessage_xdebug_client_port(debuggerPort)).append("</li>"); // NOI18N
        sb.append("<li>").append(xdebugIdekey).append("</li>"); // NOI18N
        sb.append("</ul>"); // NOI18N
        return "<html>" + NbBundle.getMessage(ConnectionErrMessage.class, "MSG_ErrDebugSession", seconds, sb.toString()) + "</html>";//NOI18N
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageTextLabel = new JLabel();
        noteLabel = new JLabel();
        debuggerPortOptionLabel = new JLabel();
        optionsLabel = new JLabel();
        learnMoreLabel = new JLabel();
        emptyLabel = new JLabel();
        copySettingsLabel = new JLabel();
        copySettingsXdebug2Button = new JButton();
        copySettingsXdebug3Button = new JButton();

        messageTextLabel.setText(message);

        noteLabel.setText("<html><i>Note:</i></html>");

        debuggerPortOptionLabel.setText("<html>If you use Xdebug 2 with default port, please set 9000 to the Debugger Port option.</html>");

        optionsLabel.setText("<html><a href=\"#\">Options...</a></html>");
        optionsLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                optionsLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                optionsLabelMouseEntered(evt);
            }
        });

        learnMoreLabel.setText("<html><a href=\"#\">Learn more about Xdebug</a>");
        learnMoreLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                learnMoreLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                learnMoreLabelMouseEntered(evt);
            }
        });

        emptyLabel.setText("EMPTY"); // NOI18N

        copySettingsLabel.setText("Copy example settings to clipboard:");

        copySettingsXdebug2Button.setText("Xdebug 2");
        copySettingsXdebug2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copySettingsXdebug2ButtonActionPerformed(evt);
            }
        });

        copySettingsXdebug3Button.setText("Xdebug 3");
        copySettingsXdebug3Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                copySettingsXdebug3ButtonActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(emptyLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(debuggerPortOptionLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(messageTextLabel, GroupLayout.PREFERRED_SIZE, 550, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(copySettingsLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(copySettingsXdebug2Button)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(copySettingsXdebug3Button)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageTextLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(copySettingsLabel)
                    .addComponent(copySettingsXdebug2Button)
                    .addComponent(copySettingsXdebug3Button))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(noteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(debuggerPortOptionLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(learnMoreLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(emptyLabel)
                .addContainerGap())
        );

        messageTextLabel.getAccessibleContext().setAccessibleDescription("Error Message");

        getAccessibleContext().setAccessibleName("Error Message Form");
        getAccessibleContext().setAccessibleDescription("Error Message Form");
    }// </editor-fold>//GEN-END:initComponents

    private void learnMoreLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMouseEntered
        setHandCursor(evt);
    }//GEN-LAST:event_learnMoreLabelMouseEntered

    private void learnMoreLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_learnMoreLabelMousePressed
        showUrl("https://xdebug.org/docs"); // NOI18N
    }//GEN-LAST:event_learnMoreLabelMousePressed

    private void optionsLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMouseEntered
        setHandCursor(evt);
    }//GEN-LAST:event_optionsLabelMouseEntered

    private void optionsLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMousePressed
        Utils.openPhpOptionsDialog();
    }//GEN-LAST:event_optionsLabelMousePressed

    @NbBundle.Messages({
        "# {0} - settings",
        "ConnectionErrMessage.copied=Copied.\n{0}",
    })
    private void copySettingsXdebug2ButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copySettingsXdebug2ButtonActionPerformed
        copySettings(createXdebug2Settings());
    }//GEN-LAST:event_copySettingsXdebug2ButtonActionPerformed

    private void copySettingsXdebug3ButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_copySettingsXdebug3ButtonActionPerformed
        copySettings(createXdebug3Settings());
    }//GEN-LAST:event_copySettingsXdebug3ButtonActionPerformed

    private void showUrl(String url) {
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(url));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void setHandCursor(MouseEvent evt) {
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void copySettings(String settings) {
        copyToClipboard(settings);
        showDialog(Bundle.ConnectionErrMessage_copied(settings));
    }

    private void copyToClipboard(String contents) {
        Clipboard clipboard = Lookup.getDefault().lookup(Clipboard.class);
        if (clipboard == null) {
            clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        StringSelection selection = new StringSelection(contents);
        clipboard.setContents(selection, selection);
    }

    private void showDialog(String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
    }

    private String createXdebug2Settings() {
        StringBuilder sb = new StringBuilder();
        sb.append("xdebug.remote_enable=on").append(NEWLINE); // NOI18N
        sb.append("xdebug.remote_handler=dbgp").append(NEWLINE); // NOI18N
        sb.append("xdebug.client_port=localhost").append(NEWLINE); // NOI18N
        sb.append("xdebug.remote_port=").append(String.valueOf(PhpOptions.getInstance().getDebuggerPort())).append(NEWLINE); // NOI18N
        sb.append(String.format("xdebug.idekey=\"%s\"", PhpOptions.getInstance().getDebuggerSessionId())); // NOI18N
        return sb.toString();
    }

    private String createXdebug3Settings() {
        StringBuilder sb = new StringBuilder();
        sb.append("xdebug.mode=debug").append(NEWLINE); // NOI18N
        sb.append("xdebug.client_host=localhost").append(NEWLINE); // NOI18N
        sb.append("xdebug.client_port=").append(String.valueOf(PhpOptions.getInstance().getDebuggerPort())).append(NEWLINE); // NOI18N
        sb.append(String.format("xdebug.idekey=\"%s\"", PhpOptions.getInstance().getDebuggerSessionId())); // NOI18N
        return sb.toString();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel copySettingsLabel;
    private JButton copySettingsXdebug2Button;
    private JButton copySettingsXdebug3Button;
    private JLabel debuggerPortOptionLabel;
    private JLabel emptyLabel;
    private JLabel learnMoreLabel;
    private JLabel messageTextLabel;
    private JLabel noteLabel;
    private JLabel optionsLabel;
    // End of variables declaration//GEN-END:variables
}
