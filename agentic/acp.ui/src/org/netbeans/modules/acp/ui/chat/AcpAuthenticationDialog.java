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
package org.netbeans.modules.acp.ui.chat;

import com.agentclientprotocol.sdk.error.AcpErrorCodes;
import com.agentclientprotocol.sdk.spec.AcpError;
import com.agentclientprotocol.sdk.spec.AcpSchema.AuthMethod;
import com.agentclientprotocol.sdk.spec.AcpSchema.AuthMethodAgent;
import java.awt.BorderLayout;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import org.netbeans.modules.acp.api.AcpConnection;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

@NbBundle.Messages({
    "TTL_Authentication=Agent Authentication",
    "MSG_Authentication=Select an authentication method for {0}.",
    "MSG_AuthenticationUnavailable=The agent requires authentication but did not offer a supported sign-in method. Sign in using the agent's own tools or check its configured credentials, then try again."
})
final class AcpAuthenticationDialog {

    private AcpAuthenticationDialog() {
    }

    static boolean isAuthenticationRequired(Throwable failure) {
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof AcpError error && error.getCode() == AcpErrorCodes.AUTHENTICATION_REQUIRED) {
                return true;
            }
        }
        return false;
    }

    static String chooseMethod(AcpConnection connection, BooleanSupplier isCurrent) {
        List<AuthMethod> methods = connection.getAuthenticationMethods().stream()
                .filter(method -> method instanceof AuthMethodAgent)
                .toList();
        return Mutex.EVENT.readAccess(() -> {
            if (!isCurrent.getAsBoolean()) {
                return null;
            }
            if (methods.isEmpty()) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        Bundle.MSG_AuthenticationUnavailable(), NotifyDescriptor.WARNING_MESSAGE));
                return null;
            }
            JComboBox<String> choices = new JComboBox<>(methods.stream()
                    .map(method -> method.name() != null ? method.name() : method.id())
                    .toArray(String[]::new));
            JPanel panel = new JPanel(new BorderLayout(0, 6));
            panel.add(new JLabel(Bundle.MSG_Authentication(connection.getDescriptor().getName())), BorderLayout.NORTH);
            panel.add(choices, BorderLayout.CENTER);
            JTextArea description = new JTextArea(3, 45);
            description.setEditable(false);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setOpaque(false);
            Runnable updateDescription = () -> {
                String text = methods.get(choices.getSelectedIndex()).description();
                description.setText(text != null ? text : "");
            };
            choices.addActionListener(event -> updateDescription.run());
            updateDescription.run();
            panel.add(description, BorderLayout.SOUTH);
            NotifyDescriptor.Confirmation dialog = new NotifyDescriptor.Confirmation(panel,
                    Bundle.TTL_Authentication(), NotifyDescriptor.OK_CANCEL_OPTION);
            if (DialogDisplayer.getDefault().notify(dialog) != NotifyDescriptor.OK_OPTION
                    || !isCurrent.getAsBoolean()) {
                return null;
            }
            return methods.get(choices.getSelectedIndex()).id();
        });
    }
}
