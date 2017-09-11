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
package org.netbeans.modules.form.assistant;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import javax.swing.*;

import org.netbeans.modules.form.FormLoaderSettings;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * Assistant view.
 *
 * @author Jan Stola
 */
public class AssistantView extends JPanel {
    private JLabel messageLabel;
    private AssistantModel model;
    
    public AssistantView(AssistantModel model) {
        this.model = model;

        Listener listener = new Listener();
        model.addPropertyChangeListener(listener);

        setBackground(FormLoaderSettings.getInstance().getFormDesignerBackgroundColor());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getForeground()));
        setToolTipText(NbBundle.getMessage(getClass(), "TOOLTIP_HelpBar")); // NOI18N

        // Message label
        messageLabel = new JLabel();
        messageLabel.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/form/resources/lightbulb.gif", true)); // NOI18N

        // Close button
        JButton closeButton = new JButton("x"); // NOI18N
        closeButton.setFont(Font.getFont("SansSerif")); // NOI18N
        closeButton.setOpaque(false);
        closeButton.setFocusPainted(false);
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.addActionListener(listener);
        // Workaround for GroupLayout.BASELINE == GroupLayout.CENTER bug
        JPanel panel = new JPanel(new BorderLayout(0,0));
        panel.setOpaque(false);
        panel.add(closeButton);

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addGap(12)
                .addComponent(messageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(12));
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addGap(2)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(messageLabel, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)
                    .addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(2));
    }

    private class Listener implements ActionListener, PropertyChangeListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            FormLoaderSettings.getInstance().setAssistantShown(false);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String[] messages = model.getMessages();
            String message = null;
            if (messages != null) {
                int index = (int)(Math.random()*messages.length);
                message = messages[index];
            }
            if (model.getAdditionalContext() != null) {
                messages = model.getAdditionalMessages();
                if (messages != null) {
                    int index = (int)(Math.random()*messages.length);
                    message = "<html>" + message + "<br>" + messages[index]; // NOI18N
                }
            }
            Object[] params = model.getParameters();
            if (params != null) {
                message = MessageFormat.format(message, params);
            }
            messageLabel.setText(message);
        }

    }

}
