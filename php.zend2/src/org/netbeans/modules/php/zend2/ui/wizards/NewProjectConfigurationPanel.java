/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.zend2.ui.wizards;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.zend2.options.Zend2Options;
import org.netbeans.modules.php.zend2.ui.options.Zend2OptionsPanelController;
import org.netbeans.modules.php.zend2.validation.OptionsValidator;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 * Zend 2 new project panel.
 */
public class NewProjectConfigurationPanel extends JPanel implements ChangeListener {

    private static final long serialVersionUID = 154687646389784L;

    private final ChangeSupport changeSupport = new ChangeSupport(this);


    public NewProjectConfigurationPanel() {
        initComponents();

        init();
        initListeners();
    }

    @Override
    public void addNotify() {
        Zend2Options.getInstance().addChangeListener(this);
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        Zend2Options.getInstance().removeChangeListener(this);
        super.removeNotify();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getErrorMessage() {
        ValidationResult validationResult = new OptionsValidator()
                .validate(Zend2Options.getInstance().getSkeleton())
                .getResult();
        if (validationResult.hasErrors()) {
            return validationResult.getErrors().get(0).getMessage();
        }
        return null;
    }

    public String getWarningMessage() {
        ValidationResult validationResult = new OptionsValidator()
                .validate(Zend2Options.getInstance().getSkeleton())
                .getResult();
        if (validationResult.hasWarnings()) {
            return validationResult.getWarnings().get(0).getMessage();
        }
        return null;
    }

    private void init() {
        // work around - keep the label on the right side
        optionsLabel.setMaximumSize(optionsLabel.getPreferredSize());
    }

    private void initListeners() {
        infoLabel.addPropertyChangeListener("enabled", new PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                enableOptionsLabel();
            }
        });
        enableOptionsLabel();
    }

    void enableOptionsLabel() {
        optionsLabel.setVisible(infoLabel.isEnabled());
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel = new JLabel();
        optionsLabel = new JLabel();

        Mnemonics.setLocalizedText(infoLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.infoLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(NewProjectConfigurationPanel.class, "NewProjectConfigurationPanel.optionsLabel.text")); // NOI18N
        optionsLabel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                optionsLabelMouseEntered(evt);
            }
            public void mousePressed(MouseEvent evt) {
                optionsLabelMousePressed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(infoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 91, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void optionsLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_optionsLabelMouseEntered

    private void optionsLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMousePressed
        OptionsDisplayer.getDefault().open(Zend2OptionsPanelController.getOptionsPath());
    }//GEN-LAST:event_optionsLabelMousePressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel infoLabel;
    private JLabel optionsLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

}
