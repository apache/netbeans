/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.ui.wizard;

import java.awt.Cursor;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.event.ChangeListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptionsValidator;
import org.netbeans.modules.javascript.nodejs.ui.options.NodeJsOptionsPanelController;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public final class ExpressPanelVisual extends JPanel implements PreferenceChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final AtomicInteger listenerCount = new AtomicInteger();


    public ExpressPanelVisual() {
        initComponents();

        init();
    }

    private void init() {
        // ui
        enableExpressCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                lessCheckBox.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        // validation
        DefaultItemListener defaultItemListener = new DefaultItemListener();
        enableExpressCheckBox.addItemListener(defaultItemListener);
        lessCheckBox.addItemListener(defaultItemListener);
    }

    boolean isExpressEnabled() {
        return enableExpressCheckBox.isSelected();
    }

    boolean isLessEnabled() {
        return lessCheckBox.isSelected();
    }

    String getErrorMessage() {
        if (!isExpressEnabled()) {
            return null;
        }
        ValidationResult result = new NodeJsOptionsValidator()
                .validateExpress()
                .getResult();
        if (result.isFaultless()) {
            return null;
        }
        if (result.hasErrors()) {
            return result.getFirstErrorMessage();
        }
        return result.getFirstWarningMessage();
    }

    void addChangeListener(ChangeListener listener) {
        if (listenerCount.getAndIncrement() == 0) {
            NodeJsOptions.getInstance().addPreferenceChangeListener(this);
        }
        changeSupport.addChangeListener(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        if (listenerCount.decrementAndGet() == 0) {
            NodeJsOptions.getInstance().removePreferenceChangeListener(this);
        }
        changeSupport.removeChangeListener(listener);
    }

    void fireChange() {
        changeSupport.fireChange();
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        fireChange();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        enableExpressCheckBox = new JCheckBox();
        optionsLabel = new JLabel();
        lessCheckBox = new JCheckBox();

        Mnemonics.setLocalizedText(enableExpressCheckBox, NbBundle.getMessage(ExpressPanelVisual.class, "ExpressPanelVisual.enableExpressCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(optionsLabel, NbBundle.getMessage(ExpressPanelVisual.class, "ExpressPanelVisual.optionsLabel.text")); // NOI18N
        optionsLabel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                optionsLabelMousePressed(evt);
            }
            public void mouseEntered(MouseEvent evt) {
                optionsLabelMouseEntered(evt);
            }
        });

        lessCheckBox.setSelected(true);
        Mnemonics.setLocalizedText(lessCheckBox, NbBundle.getMessage(ExpressPanelVisual.class, "ExpressPanelVisual.lessCheckBox.text")); // NOI18N
        lessCheckBox.setEnabled(false);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(enableExpressCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lessCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(enableExpressCheckBox)
                    .addComponent(optionsLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(lessCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void optionsLabelMouseEntered(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMouseEntered
        evt.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }//GEN-LAST:event_optionsLabelMouseEntered

    private void optionsLabelMousePressed(MouseEvent evt) {//GEN-FIRST:event_optionsLabelMousePressed
        OptionsDisplayer.getDefault().open(NodeJsOptionsPanelController.OPTIONS_PATH);
    }//GEN-LAST:event_optionsLabelMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JCheckBox enableExpressCheckBox;
    private JCheckBox lessCheckBox;
    private JLabel optionsLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private final class DefaultItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            fireChange();
        }

    }

}
