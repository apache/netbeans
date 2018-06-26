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
package org.netbeans.modules.php.project.ui.customizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

public class TestingProviderPanel extends JPanel {

    private static final ActionListener NOOP_ACTION_LISTENER = new NoopActionListener();

    private final ProjectCustomizer.Category category;
    private final PhpProjectProperties uiProps;
    private final String providerIdentifier;
    private final JComponent providerComponent;
    private final ActionListener originalStoreListener;
    private final ActionListener originalOkButtonListener;

    // @GuardedBy("EDT")
    private boolean originalCategoryValid;
    // @GuardedBy("EDT")
    private String originalErrorMessage;


    TestingProviderPanel(ProjectCustomizer.Category category, PhpProjectProperties uiProps,
            String providerIdentifier, JComponent providerComponent) {
        assert EventQueue.isDispatchThread();
        assert category != null;
        assert uiProps != null;
        assert providerIdentifier != null;
        assert providerComponent != null;

        this.category = category;
        this.uiProps = uiProps;
        this.providerIdentifier = providerIdentifier;
        this.providerComponent = providerComponent;
        originalStoreListener = category.getStoreListener();
        originalOkButtonListener = category.getOkButtonListener();
        rememberValues();

        initComponents();
        init();
    }

    private void init() {
        assert EventQueue.isDispatchThread();
        if (uiProps.getTestingProviders().contains(providerIdentifier)) {
            showProviderPanel();
        } else {
            hideProviderPanel();
        }
    }

    public String getProviderIdentifier() {
        return providerIdentifier;
    }

    public void showProviderPanel() {
        assert EventQueue.isDispatchThread();
        // switch ui
        notActiveLabel.setVisible(false);
        providerPanel.add(providerComponent, BorderLayout.CENTER);
        // restore values
        category.setStoreListener(originalStoreListener);
        category.setOkButtonListener(originalOkButtonListener);
        category.setErrorMessage(originalErrorMessage);
        category.setValid(originalCategoryValid);
    }

    public void hideProviderPanel() {
        assert EventQueue.isDispatchThread();
        rememberValues();
        // switch ui
        category.setStoreListener(NOOP_ACTION_LISTENER);
        category.setOkButtonListener(NOOP_ACTION_LISTENER);
        notActiveLabel.setVisible(true);
        providerPanel.remove(providerComponent);
        category.setErrorMessage(null);
        category.setValid(true);
    }

    private void rememberValues() {
        assert EventQueue.isDispatchThread();
        originalCategoryValid = category.isValid();
        originalErrorMessage = category.getErrorMessage();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        notActiveLabel = new JLabel();
        providerPanel = new JPanel();

        Mnemonics.setLocalizedText(notActiveLabel, NbBundle.getMessage(TestingProviderPanel.class, "TestingProviderPanel.notActiveLabel.text")); // NOI18N

        providerPanel.setLayout(new BorderLayout());

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(providerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(notActiveLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(notActiveLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(providerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel notActiveLabel;
    private JPanel providerPanel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class NoopActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // noop
        }

    }

}
