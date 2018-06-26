/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.glassfish.common.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.glassfish.common.GlassFishLogger;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.CANCEL_OPTION;
import org.openide.util.NbBundle;

/**
 * Set or change GlassFish password panel.
 * <p/>
 * @author Tomas Kraus
 */
public class GlassFishPassword extends CommonPasswordPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = GlassFishLogger.get(GlassFishPassword.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Display GlassFish password panel to change GlassFish password.
     <p/>
     * Password is stored in server instance properties and returned
     * by this method. Properties are persisted.
     * <p/>
     * @param instance GlassFish server instance.
     * @return Password {@see String} when password was successfully changed
     *         or <code>null</code> otherwise.
     */
    public static String setPassword(final GlassfishInstance instance) {
        String title = NbBundle.getMessage(
                GlassFishPassword.class, "GlassFishPassword.title");
        String message = NbBundle.getMessage(
                GlassFishPassword.class,
                "GlassFishPassword.message", instance.getDisplayName());
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                null, title, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, null);
        GlassFishPassword panel
                = new GlassFishPassword(notifyDescriptor, instance, message);
        Object button = DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (button == CANCEL_OPTION) {
            return null;
        }
        String password = panel.getPassword();
        instance.setAdminPassword(password);
        try {
            GlassfishInstance.writeInstanceToFile(instance);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO,
                    "Could not store GlassFish server attributes", ex);
        }
        return password;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Password verification label. */
    private final String passwordVerifyLabelText;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new GlassFish password panel.
     * <p/>
     * @param descriptor User notification descriptor.
     * @param instance   GlassFish server instance used to search
     *                   for supported platforms.
     * @param message    Message text.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public GlassFishPassword(final NotifyDescriptor descriptor,
            final GlassfishInstance instance, final String message) {
        super(descriptor, instance, message);
        this.passwordVerifyLabelText = NbBundle.getMessage(
                GlassFishPassword.class,
                "GlassFishPassword.passwordVerifyLabel");
        initComponents();
        initFileds(passwordValid());
        password.getDocument()
                .addDocumentListener(initPasswordValidateListener());
        passwordVerify.getDocument()
                .addDocumentListener(initPasswordValidateListener());
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters and Setters                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get password value from form.
     * <p/>
     * @return Password value from form.
     */
    String getPassword() {
        return new String(password.getPassword());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Validators                                                             //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Validate password fields.
     * <p/>
     * Password fields have the same length and content.
     * <p/>
     * @return <code>true</code> when password fields are valid
     *         or <code>false</code> otherwise.
     */
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    final boolean passwordValid() {
        char[] password1 = password.getPassword();
        char[] password2 = passwordVerify.getPassword();
        if (password1.length == password2.length) {
            boolean valid = true;
            int i = 0;
            while(valid && i < password1.length) {
                valid = password1[i] == password2[i];
                i += 1;
            }
            return valid;
        } else {
            return false;
        }        
    }

    /**
     * Create event listener to validate account field on the fly.
     */
    private DocumentListener initPasswordValidateListener() {
        return new ComponentFieldListener() {
            @Override
            void processEvent() {
                valid = passwordValid();
                setDescriptorButtons(descriptor, valid);
            }
        };
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Clear form fields to remove password {@see String}s from them.
     */
    public void clear() {
        this.password.setText("");
        this.passwordVerify.setText("");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generated GUI code                                                     //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        passwordVerifyLabel = new javax.swing.JLabel();
        userText = new javax.swing.JTextField();
        password = new javax.swing.JPasswordField();
        passwordVerify = new javax.swing.JPasswordField();

        setMaximumSize(new java.awt.Dimension(500, 200));
        setMinimumSize(new java.awt.Dimension(500, 150));
        setPreferredSize(new java.awt.Dimension(500, 150));

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, this.message);
        messageLabel.setMaximumSize(new java.awt.Dimension(93, 15));
        messageLabel.setMinimumSize(new java.awt.Dimension(93, 15));
        messageLabel.setPreferredSize(new java.awt.Dimension(15, 15));

        org.openide.awt.Mnemonics.setLocalizedText(userLabel, this.userLabelText);
        userLabel.setMaximumSize(new java.awt.Dimension(93, 15));
        userLabel.setMinimumSize(new java.awt.Dimension(93, 15));
        userLabel.setPreferredSize(new java.awt.Dimension(93, 15));

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, this.passwordLabelText);

        org.openide.awt.Mnemonics.setLocalizedText(passwordVerifyLabel, this.passwordVerifyLabelText);

        userText.setEditable(false);
        userText.setText(this.instance.getAdminUser());

        password.setText(this.instance.getAdminPassword());

        passwordVerify.setText(this.instance.getAdminPassword());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(userLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(passwordLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(passwordVerifyLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userText, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                            .addComponent(password)
                            .addComponent(passwordVerify))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(userLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(passwordLabel)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(passwordVerifyLabel)
                    .addComponent(passwordVerify, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordVerify;
    private javax.swing.JLabel passwordVerifyLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userText;
    // End of variables declaration//GEN-END:variables
}
